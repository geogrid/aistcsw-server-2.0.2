/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.opengis.cat.csw._2_0.CapabilitiesType;
import net.opengis.cat.csw._2_0.DeleteType;
import net.opengis.cat.csw._2_0.GetRecordsType;
import net.opengis.cat.csw._2_0.InsertType;
import net.opengis.cat.csw._2_0.QueryConstraintType;
import net.opengis.cat.csw._2_0.QueryType;
import net.opengis.cat.csw._2_0.ResultType;
import net.opengis.cat.csw._2_0.TransactionType;
import net.opengis.cat.csw._2_0.UpdateType;
import net.opengis.ogc.FilterType;
import net.opengis.ogc.SortByType;
import net.opengis.ogc.SortPropertyType;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.dbgrid.csw.capabilities.GetCapabilities;
import org.dbgrid.csw.crawler.Crawler;
import org.dbgrid.csw.filter.GetSrsName;
import org.dbgrid.csw.records.DistributedGetRecords;
import org.dbgrid.csw.records.GetRecords;
import org.dbgrid.csw.records.GetRecordsResult;
import org.dbgrid.csw.response.CreateGetRecordsResponse;
import org.dbgrid.csw.transaction.SaveData;
import org.dbgrid.csw.transaction.TransactionDelete;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.opengis.filter.Filter;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author kimoto
 */
public class CSWService extends HttpServlet {

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Unmarshaller unmarshller = getUnmarshaller();
        Marshaller marshaller = getMarshaller();
        ResourceBundle resource = ResourceBundle.getBundle("csw");

        Enumeration<String> parameterList = request.getParameterNames();

        String requestParam = null;

        while (parameterList.hasMoreElements()) {
            String param = parameterList.nextElement();

            if (param.equalsIgnoreCase("request")) {
                requestParam = param;
            }
        }
        switch (request.getParameter(requestParam).toLowerCase()) {
            case "GetRecords":
                break;
            case "getcapabilities":
                GetCapabilities capabilities = new GetCapabilities();
                CapabilitiesType getCapabilities = capabilities.createGetCapabilitiesResponse();
                response.setContentType("text/xml; charset=UTF-8");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    marshaller.marshal(getCapabilities, bos);

                    response.getWriter().println(bos.toString().replaceAll("capabilitiesType", "Capabilities"));
                } catch (JAXBException e) {
                    e.printStackTrace();
                }

                break;
            case "GetDomain":
                break;
        }

    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Unmarshaller unmarshller = getUnmarshaller();
        Marshaller marshaller = getMarshaller();
        ResourceBundle resource = ResourceBundle.getBundle("csw");

        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            builder.append(line);
        }
        System.out.println(builder.toString());
        Object inputXML = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document inputDocument = factory.newDocumentBuilder().parse(new ByteArrayInputStream(builder.toString().getBytes("UTF-8")));
            JAXBElement element = (JAXBElement) unmarshller.unmarshal(inputDocument);
            inputXML = element.getValue();
        } catch (SAXException ex) {
            Logger.getLogger(CSWService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException | JAXBException ex) {
            Logger.getLogger(CSWService.class.getName()).log(Level.SEVERE, null, ex);
        }

        String inputXMLtype = inputXML.getClass().getSimpleName();
        
        switch (inputXMLtype) {
            case "GetCapabilitiesType":
                GetCapabilities capabilities = new GetCapabilities();
                CapabilitiesType getCapabilities = capabilities.createGetCapabilitiesResponse();
                response.setContentType("text/xml; charset=UTF-8");

                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    marshaller.marshal(getCapabilities, bos);
                    response.getWriter().println(bos.toString().replaceAll("capabilitiesType", "Capabilities"));
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
                break;

            case "GetRecordsType":
                GetRecordsType requestRecords = (GetRecordsType) inputXML;

                // Basic Information
                ResultType resultType = requestRecords.getResultType();
                BigInteger startPosition = requestRecords.getStartPosition();
                BigInteger maxRecords = requestRecords.getMaxRecords();

                QueryType getRecordQuery = (QueryType) requestRecords.getAbstractQuery().getValue();
                QueryConstraintType getRecordConstraint = getRecordQuery.getConstraint();
                String getRecordCqlText = getRecordConstraint.getCqlText();
                FilterType getRecordFilterType = getRecordConstraint.getFilter();

                GetSrsName g = new GetSrsName();
                String srsName = g.getSrsName(getRecordFilterType, marshaller);

                if (srsName == null) {
                    srsName = resource.getString("csw.default.projection");
                }

                String getRecordVersion = getRecordConstraint.getVersion();

                // Distributed Search
                // Default hopCount = 0 (Not Distributed)
                Integer hopCount;
                if (requestRecords.getDistributedSearch() != null) {
                    hopCount = requestRecords.getDistributedSearch().getHopCount().intValue();
                } else {
                    hopCount = 0;
                }

                // Sort Patameter
                SortByType sortBy = getRecordQuery.getSortBy();
                ArrayList<String> sortParamList = new ArrayList<>();
                String sortStr = null;

                if (sortBy != null) {
                    List<SortPropertyType> list = sortBy.getSortProperty();

                    for (SortPropertyType sortParam : list) {
                        sortParamList.add(sortParam.getPropertyName().getContent().get(0).toString() + ":" + sortParam.getSortOrder().value());
                    }
                    sortStr = StringUtils.join(sortParamList, ",");
                }

                Filter getRecordFilter = null;

                if (getRecordCqlText != null) {
                    getRecordFilter = getCQLFilter(getRecordCqlText);
                } else if (getRecordFilterType != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        marshaller.marshal(getRecordFilterType, bos);
                    } catch (JAXBException ex) {
                        Logger.getLogger(CSWService.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    getRecordFilter = getXMLFilter(bos.toString(), getRecordVersion);

                } else {
                    // Exception null
                }

                if (getRecordFilter != null) {
                    if (hopCount == 0) {
                        // Luceneへの問い合わせ
                        GetRecords records = new GetRecords(getJaxbContext(), resource, unmarshller, marshaller, srsName);
                        ArrayList<org.apache.lucene.document.Document> result;
                        if (sortStr != null) {
                            result = records.getRecords(requestRecords, getRecordFilter, sortStr);

                        } else {
                            result = records.getRecords(requestRecords, getRecordFilter);

                        }
                        int hits = records.getHitNum();

                        switch (resource.getString("csw.parallel.root")) {
                            case "yes":

                                if (resource.getString("csw.parallel.server.count").equals("0")) {

                                    response.setContentType("text/xml; charset=UTF-8");

                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    try {
                                        CreateGetRecordsResponse create = new CreateGetRecordsResponse(getJaxbContext(), resource, unmarshller, marshaller);
                                        marshaller.marshal(create.createGetRecordsResponse(result, null, startPosition, maxRecords, hits), bos);
                                        response.getWriter().println(bos.toString().replaceAll("getRecordsResponseType", "GetRecordsResponse"));
                                    } catch (JAXBException e) {
                                        e.printStackTrace();
                                    }
                                } else {

                                    if (resource.getString("csw.parallel.own.search").equals("yes")) {
                                        response.setContentType("application/x-java-serialized-object");
                                        GetRecordsResult resultObj = new GetRecordsResult(result, hits);
                                        SerializationUtils.serialize(resultObj, response.getOutputStream());

                                    } else {


                                        response.setContentType("text/xml; charset=UTF-8");

                                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                        try {
                                            CreateGetRecordsResponse create = new CreateGetRecordsResponse(getJaxbContext(), resource, unmarshller, marshaller);
                                            marshaller.marshal(create.createGetRecordsResponse(result, null, startPosition, maxRecords, hits), bos);
                                            response.getWriter().println(bos.toString().replaceAll("getRecordsResponseType", "GetRecordsResponse"));
                                        } catch (JAXBException e) {
                                            e.printStackTrace();
                                        }


                                    }


                                }


                                // Output GetRecordsResponse
                                /*response.setContentType("text/xml; charset=UTF-8");

                                 ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                 try {
                                 CreateGetRecordsResponse create = new CreateGetRecordsResponse(getJaxbContext(), resource, unmarshller, marshaller);
                                 marshaller.marshal(create.createGetRecordsResponse(result, null, startPosition, maxRecords, hits), bos);
                                 response.getWriter().println(bos.toString().replaceAll("getRecordsResponseType", "GetRecordsResponse"));
                                 } catch (JAXBException e) {
                                 e.printStackTrace();
                                 }*/

                                break;
                            case "no":
                                response.setContentType("application/x-java-serialized-object");
                                GetRecordsResult resultObj = new GetRecordsResult(result, hits);
                                SerializationUtils.serialize(resultObj, response.getOutputStream());
                                break;
                            default:
                            //error
                        }
                    } else {

                        ArrayList<org.apache.lucene.document.Document> result = new ArrayList<>();
                        int hits = 0;

                        hopCount--;
                        requestRecords.getDistributedSearch().setHopCount(BigInteger.valueOf(hopCount));

                        Integer parallelServerCount = new Integer(resource.getString("csw.parallel.server.count"));
                        //Integer distServerCount = new Integer(resource.getString("csw.distributed.server.count"));

                        DistributedGetRecords[] parallelRecordsList = new DistributedGetRecords[parallelServerCount];

                        for (int i = 1; i <= parallelServerCount; i++) {

                            parallelRecordsList[i - 1] = new DistributedGetRecords();
                            parallelRecordsList[i - 1].setGetRecords(requestRecords);
                            parallelRecordsList[i - 1].setServerUrl(resource.getString("csw.parallel.server.name." + i));
                            parallelRecordsList[i - 1].start();

                        }

                        /*
                         for (int i = parallelServerCount + 1; i <= distServerCount; i++) {

                         distRecordsList[i - 1] = new DistributedGetRecords();
                         distRecordsList[i - 1].setGetRecords(requestRecords);
                         distRecordsList[i - 1].setServerUrl(resource.getString("csw.distributed.server.name." + i));
                         distRecordsList[i - 1].start();

                         }

                         for (int i = 0; i < distServerCount + parallelServerCount; i++) {
                         try {
                         distRecordsList[i].join();
                         } catch (InterruptedException ex) {
                         ex.printStackTrace();
                         }
                         }*/

                        for (int i = 0; i < parallelServerCount; i++) {
                            try {
                                parallelRecordsList[i].join();
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }

                        for (int i = 0; i < parallelServerCount; i++) {
                            GetRecordsResult resultObj = (GetRecordsResult) parallelRecordsList[i].getResult();

                            //ArrayList<org.apache.lucene.document.Document> docs = (ArrayList<org.apache.lucene.document.Document>) parallelRecordsList[i].getResult();
                            result.addAll(resultObj.getResult());
                            hits += resultObj.getHits();

                        }

                        switch (resource.getString("csw.parallel.root")) {
                            case "yes":
                                response.setContentType("text/xml; charset=UTF-8");

                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                try {
                                    CreateGetRecordsResponse create = new CreateGetRecordsResponse(getJaxbContext(), resource, unmarshller, marshaller);
                                    marshaller.marshal(create.createGetRecordsResponse(result, sortStr, startPosition, maxRecords, hits), bos);
                                    response.getWriter().println(bos.toString().replaceAll("getRecordsResponseType", "GetRecordsResponse"));
                                } catch (JAXBException e) {
                                    e.printStackTrace();
                                }


                                /*
                                 response.setContentType("text/xml; charset=UTF-8");

                                 ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                 try {
                                 CreateGetRecordsResponse create = new CreateGetRecordsResponse(getJaxbContext(), resource, unmarshller, marshaller);
                                 marshaller.marshal(create.createGetRecordsResponse(result, sortStr, startPosition, maxRecords, hits), bos);
                                 response.getWriter().println(bos.toString().replaceAll("getRecordsResponseType", "GetRecordsResponse"));
                                 } catch (JAXBException e) {
                                 e.printStackTrace();
                                 }
                                 */
                                break;
                            case "no":
                                response.setContentType("application/x-java-serialized-object");
                                SerializationUtils.serialize(result, response.getOutputStream());
                                break;
                            default:
                            //error
                        }

                        break;
                    }

                }
                break;
            case "TransactionType":
                TransactionType transaction = (TransactionType) inputXML;
                List<Object> transactionList = transaction.getInsertOrUpdateOrDelete();
                ResourceBundle transactionProperty = ResourceBundle.getBundle(resource.getString("csw.transaction.property"));

                for (Object transactionObject : transactionList) {

                    String type = transactionObject.getClass().getSimpleName();

                    switch (type) {
                        case "InsertType":
                            InsertType insert = (InsertType) transactionObject;
                            SaveData saveData = new SaveData();
                            saveData.saveData(insert, getMarshaller());
                            Crawler crawler = new Crawler();
/*                            crawler.setConfig(transactionProperty.getString("indexing.config." + saveData.getInstrumentShortName()));
                            crawler.mainCrawler(new File(saveData.getSaveFileName()));
                            crawler.indexing(transactionProperty.getString("indexing.dir." + saveData.getInstrumentShortName()));*/
                            crawler.setConfig(transactionProperty.getString("index.config"));
                            crawler.mainCrawler(new File(saveData.getSaveFileName()));
                            crawler.indexing(transactionProperty.getString("index.dir"));
                            break;
                        case "DeleteType":
                            DeleteType delete = (DeleteType) transactionObject;

                            QueryConstraintType deleteConstraint = delete.getConstraint();
                            String deleteCqlText = deleteConstraint.getCqlText();
                            FilterType deleteFilterType = deleteConstraint.getFilter();

                            GetSrsName g1 = new GetSrsName();
                            String deleteSrsName = g1.getSrsName(deleteFilterType, marshaller);

                            if (deleteSrsName == null) {
                                deleteSrsName = resource.getString("csw.default.projection");
                            }

                            Filter deleteFilter = null;

                            if (deleteCqlText != null) {
                                deleteFilter = getCQLFilter(deleteCqlText);
                            } else if (deleteFilterType != null) {
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                try {
                                    marshaller.marshal(deleteFilterType, bos);
                                } catch (JAXBException ex) {
                                    Logger.getLogger(CSWService.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                deleteFilter = getXMLFilter(bos.toString(), deleteConstraint.getVersion());

                            } else {
                                // Exception null
                            }

                            if (deleteFilter != null) {
                                // Luceneへの問い合わせ
                                TransactionDelete transactionDelete = new TransactionDelete();
                                transactionDelete.delete(deleteFilter, deleteSrsName);
                            }

                            break;

                        case "UpdateType":
                            UpdateType update = (UpdateType) transactionObject;

                        default:
                    }

                    break;
                }
                break;


            default:
        }

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private JAXBContext getJaxbContext() {
        JAXBContext context = null;
        ResourceBundle contextResource = ResourceBundle.getBundle("context");
        Integer contextListNum = new Integer(contextResource.getString("csw.context.list.num"));
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= contextListNum; i++) {
            sb.append(contextResource.getString("csw.context.list." + i));
        }

        try {
            context = JAXBContext.newInstance(sb.toString());
        } catch (JAXBException ex) {
            Logger.getLogger(CSWService.class.getName()).log(Level.SEVERE, null, ex);
        }


        return context;
    }

    private Marshaller getMarshaller() {
        Marshaller marshaller = null;

        try {
            marshaller = getJaxbContext().createMarshaller();
        } catch (JAXBException ex) {
            Logger.getLogger(CSWService.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (marshaller != null) {
            try {
                // 出力XML整形オプション
                marshaller.setProperty("jaxb.formatted.output", true);
                // 出力XMLエンコーディングオプション(デフォルトUTF-8)
                marshaller.setProperty("jaxb.encoding", "UTF-8");
                // 名前空間のプレフィックスの指定のためのオプション
                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new Prefix());
            } catch (PropertyException ex) {
                Logger.getLogger(CSWService.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return marshaller;
    }

    private Unmarshaller getUnmarshaller() {
        Unmarshaller unmarshaller = null;

        try {
            unmarshaller = getJaxbContext().createUnmarshaller();
        } catch (JAXBException ex) {
            Logger.getLogger(CSWService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return unmarshaller;
    }

    private Filter getCQLFilter(String cqlText) {
        Filter filter = null;
        try {
            filter = CQL.toFilter(cqlText);
        } catch (CQLException e) {
            e.printStackTrace();
        }

        return filter;
    }

    public Filter getXMLFilter(String filterXML, String version) {
        Filter filter = null;
        Configuration config;

        switch (version) {
            case "2.0":
                config = new org.geotools.filter.v2_0.FESConfiguration();
                break;
            case "1.0.1":
                config = new org.geotools.filter.v1_0.OGCConfiguration();
                break;
            case "1.1.0":
                config = new org.geotools.filter.v1_1.OGCConfiguration();
                break;
            default:
                config = new org.geotools.filter.v1_1.OGCConfiguration();
        }

        InputStream is = new ByteArrayInputStream(filterXML.getBytes());
        Parser parser = new Parser(config);

        try {
            filter = (Filter) parser.parse(is);
        } catch (IOException | SAXException | ParserConfigurationException ex) {
            Logger.getLogger(CSWService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return filter;

    }
}
