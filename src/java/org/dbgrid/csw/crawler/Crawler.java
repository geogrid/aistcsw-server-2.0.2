/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.crawler;

import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;
import org.dbgrid.csw.schema.crawler.CrawlerConfig;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author kimoto
 */
public class Crawler {

    CrawlerConfig config;
    ArrayList<IndexingData> indexingData;
    static ArrayList<org.apache.lucene.document.Document> documentData;
    ArrayList<String> analyzerList;
    String identifier;

    public void setConfig(String configFile) {
        // JAXBContext生成
        JAXBContext context;
        try {
            context = JAXBContext.newInstance("org.dbgrid.csw.schema.crawler:");
            Unmarshaller unmarshller = context.createUnmarshaller();

            config = (CrawlerConfig) unmarshller.unmarshal(new File(configFile));
        } catch (JAXBException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /* ディレクトリ再帰 */
    public void mainCrawler(File data) {
        documentData = new ArrayList<>();
        analyzerList = new ArrayList<>();

        if (data.isDirectory()) {
            File[] dataFiles = data.listFiles();

            for (File file : dataFiles) {
                if (file.isDirectory()) {
                    mainCrawler(file);
                } else {
                    crawler(file);
                }
            }
        } else {
            crawler(data);

        }
    }

    public void crawler(File data) {
        try {
            String filePath = data.getAbsolutePath();
            crawler(new FileInputStream(data), filePath);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    public void crawler(InputStream dataFile, String saveDir) {
        indexingData = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder builder = dbFactory.newDocumentBuilder();

            Document doc = builder.parse(dataFile);
            JXPathContext context = JXPathContext.newContext(doc);

            ResourceBundle rb = ResourceBundle.getBundle("prefix");
            Enumeration keys = rb.getKeys();

            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                context.registerNamespace(key, rb.getString(key));
            }

            List<CrawlerConfig.LuceneField.Property> fieldList = config.getLuceneField().getProperty();

            for (int i = 0; i < fieldList.size(); i++) {
                String field = fieldList.get(i).getField();
                String dataType = fieldList.get(i).getDatatype().toLowerCase();
                String analyzer = fieldList.get(i).getAnalyzer();
                switch (dataType) {
                    case "bbox":
                        CrawlerConfig.LuceneField.Property.Xpath.Bbox bbox = fieldList.get(i).getXpath().getBbox();

                        switch (bbox.getBboxType()) {
                            case "NWES":
                                break;

                            case "Envelope":
                                String srsNamePath = "(" + bbox.getEnvelope().getSrsName() + ")";
                                Double count = (Double) context.getValue("count(" + srsNamePath + ")");

                                String lowerXpath = "(" + fieldList.get(i).getXpath().getBbox().getEnvelope().getLower() + ")";
                                String upperXpath = "(" + fieldList.get(i).getXpath().getBbox().getEnvelope().getUpper() + ")";
                                for (int j = 1; j <= count; j++) {
                                    String srsNameStr = (String) context.getValue(srsNamePath + "[" + j + "]");
                                    String lowerStr = (String) context.getValue(lowerXpath + "[" + j + "]");
                                    String upperStr = (String) context.getValue(upperXpath + "[" + j + "]");

                                    String lowerX = lowerStr.split(" ")[0];
                                    String lowerY = lowerStr.split(" ")[1];
                                    String upperX = upperStr.split(" ")[0];
                                    String upperY = upperStr.split(" ")[1];

                                    ArrayList<String> values = new ArrayList<>();
                                    values.add(lowerX + " " + lowerY);
                                    values.add(upperX + " " + lowerY);
                                    values.add(upperX + " " + upperY);
                                    values.add(lowerX + " " + upperY);
                                    values.add(lowerX + " " + lowerY);
                                    convert(values, field);

                                }
                                break;
                        }
                        break;
                    case "polygon":
                        String polygonPath = fieldList.get(i).getXpath().getPolygon().getExterior();
                        Double polygonCount = (Double) context.getValue("count(" + polygonPath + ")");
                        String poslistStr;
                        if (polygonCount.intValue() == 1) {
                            // count = 1 -> gml:posList
                            String value = (String) context.getValue(polygonPath + "[1]");
                            poslistStr = value.replaceAll("\n", "");

                        } else {
                            // count > 1 -> gml:Pos
                            ArrayList<String> values = new ArrayList<>();

                            for (int j = 1; j <= polygonCount.intValue(); j++) {
                                values.add((String) context.getValue(polygonPath + "[" + j + "]"));
                            }
                            convert(values, field);
                        }
                        break;
                    default:
                        List<String> xpath = fieldList.get(i).getXpath().getStatement();
                        for (int k = 0; k < xpath.size(); k++) {
                            Iterator iter = context.iterate(xpath.get(k));
                            while (iter.hasNext()) {
                                String str = (String) iter.next();
                                if (field.equalsIgnoreCase("identifier")) {
                                    identifier = str;
                                    String filePath = saveDir;
                                    indexingData.add(new IndexingData("url", "url", filePath, "false"));
                                }
                                IndexingData bboxData = new IndexingData(field, dataType, str, analyzer);
                                indexingData.add(bboxData);
                            }
                        }
                        break;


                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        documentData.add(createDocument(indexingData));

    }

    public void convert(ArrayList<String> poslist, String field) {
        ResourceBundle rb = ResourceBundle.getBundle("proj");

        Enumeration keys = rb.getKeys();
        while (keys.hasMoreElements()) {
            String code = (String) keys.nextElement();
            ArrayList<Double> convertPosList = new ArrayList<>();
            String projCode = rb.getString(code);

            Projection proj = ProjectionFactory.fromPROJ4Specification(projCode.split(" "));

            for (int i = 0; i < poslist.size(); i++) {
                Point2D.Double point = new Point2D.Double(new Double(poslist.get(i).split(" ")[0]), new Double(poslist.get(i).split(" ")[1]));
                Point2D.Double convertPoint = proj.transform(point, new Point2D.Double());

                convertPosList.add(convertPoint.x);
                convertPosList.add(convertPoint.y);

            }
            getBoundingBox(convertPosList, "false");
            String polygonStr = polygonString(convertPosList, code);
            IndexingData polygonData = new IndexingData(field + code, "geometry", polygonStr, "false");
            indexingData.add(polygonData);
        }

    }

    public void convert(ArrayList<String> poslist, String field, String srsName) {
        ResourceBundle rb = ResourceBundle.getBundle("proj");

        Enumeration keys = rb.getKeys();
        while (keys.hasMoreElements()) {
            String code = (String) keys.nextElement();
            ArrayList<Double> convertPosList = new ArrayList<>();
            String projCode = rb.getString(code);

            Projection proj = ProjectionFactory.fromPROJ4Specification(projCode.split(" "));

            for (int i = 0; i < poslist.size(); i++) {
                Point2D.Double orgPoint = new Point2D.Double(new Double(poslist.get(i).split(" ")[0]), new Double(poslist.get(i).split(" ")[1]));
                Point2D.Double point;

                if (!srsName.contains("4326")) {
                    point = proj.inverseTransform(orgPoint, new Point2D.Double());
                } else {
                    point = orgPoint;
                }

                Point2D.Double convertPoint = proj.transform(point, new Point2D.Double());

                convertPosList.add(convertPoint.x);
                convertPosList.add(convertPoint.y);

            }
            getBoundingBox(convertPosList, "false");
            String polygonStr = polygonString(convertPosList, code);
            IndexingData polygonData = new IndexingData(field + code, "geometry", polygonStr, "false");
            indexingData.add(polygonData);
        }

    }

    private void getBoundingBox(ArrayList coordList, String analyzer) {

        double[] xList = new double[coordList.size() / 2];
        double[] yList = new double[coordList.size() / 2];

        for (int i = 0; i < coordList.size(); i++) {
            if (i % 2 == 0) {
                xList[i / 2] = (double) coordList.get(i);
            } else {
                yList[i / 2] = (double) coordList.get(i);
            }
        }
        IndexingData lowerXData = new IndexingData("lowerX", "double", new Double(NumberUtils.min(xList)).toString(), analyzer);
        IndexingData lowerYData = new IndexingData("lowerY", "double", new Double(NumberUtils.min(yList)).toString(), analyzer);
        IndexingData upperXData = new IndexingData("upperX", "double", new Double(NumberUtils.max(xList)).toString(), analyzer);
        IndexingData upperYData = new IndexingData("upperY", "double", new Double(NumberUtils.max(xList)).toString(), analyzer);

        indexingData.add(lowerXData);
        indexingData.add(lowerYData);
        indexingData.add(upperXData);
        indexingData.add(upperYData);
    }

    private String polygonString(ArrayList coordList, String code) {
        String polygonStr = "POLYGON((";
        for (int i = 0; i < coordList.size(); i++) {
            polygonStr += coordList.get(i);
            if (i % 2 != 0) {
                if (i != coordList.size() - 1) {
                    polygonStr += ",";
                }
            } else {
                polygonStr += " ";
            }
        }
        polygonStr += ")):" + code;

        return polygonStr;
    }

    public org.apache.lucene.document.Document createDocument(ArrayList<IndexingData> data) {
        org.apache.lucene.document.Document document = new org.apache.lucene.document.Document();
        for (IndexingData docData : data) {

            String field = docData.getField();
            String type = docData.getType().toLowerCase();
            String value = docData.getValue();
            String analyzer = docData.getAnalyzer();

            System.out.println(field + " " + value);

            Field.Index analyzerObj = analyzer.equals("false") ? Field.Index.NOT_ANALYZED : Field.Index.ANALYZED;
            analyzerList.add(analyzer);

            if (value != null && value.length() != 0) {
                switch (type) {
                    case "url":
                        document.add(new Field(field, value, Field.Store.YES, analyzerObj));
                        break;
                    case "string":
                        document.add(new Field(field, value, Field.Store.YES, analyzerObj));
                        break;
                    case "geometry":
                        document.add(new Field(field, value.split(":")[0], Field.Store.YES, analyzerObj));
                        break;
                    case "datetime":
                        String format = config.getDateFormat();
                        DateTime dateTime;
                        if (format == null) {
                            dateTime = new DateTime(value);

                        } else {
                            dateTime = DateTimeFormat.forPattern(format).parseDateTime(value);
                        }

                        Field dateField = new Field(field, DateTools.dateToString(dateTime.toDate(), DateTools.Resolution.MILLISECOND), Field.Store.YES, analyzerObj);
                        document.add(dateField);
                        break;
                    case "double":
                        Field doubleField = new Field(field, NumericUtils.doubleToPrefixCoded(new Double(value)), Field.Store.YES, analyzerObj);
                        document.add(doubleField);
                        break;
                    case "integer":
                        Field intField = new Field(field, NumericUtils.intToPrefixCoded(new Integer(value)), Field.Store.YES, analyzerObj);
                        document.add(intField);
                        break;
                    case "float":
                        Field floatField = new Field(field, NumericUtils.floatToPrefixCoded(new Float(value)), Field.Store.YES, analyzerObj);
                        document.add(floatField);
                        break;
                    case "long":
                        Field longField = new Field(field, NumericUtils.longToPrefixCoded(new Long(value)), Field.Store.YES, analyzerObj);
                        document.add(longField);
                        break;
                    default:
                    // Filed Definition Exception
                }
            }


        }

        return document;
    }

    public void indexing(String luceneIndexingDir) {
        try {
            Directory dir = FSDirectory.open(new File(luceneIndexingDir));
            IndexWriterConfig luceneConfig;
            try {

                for (int i = 0; i < documentData.size(); i++) {
                    String analyzer = analyzerList.get(i);
                    switch (analyzer) {
                        case "Standard":
                            luceneConfig = new IndexWriterConfig(Version.LUCENE_33, new StandardAnalyzer(Version.LUCENE_33));
                            break;
                        case "CJK":
                            luceneConfig = new IndexWriterConfig(Version.LUCENE_33, new CJKAnalyzer(Version.LUCENE_33));
                            break;
                        case "Whitespace":
                            luceneConfig = new IndexWriterConfig(Version.LUCENE_33, new WhitespaceAnalyzer(Version.LUCENE_33));
                            break;
                        case "Keyword":
                            luceneConfig = new IndexWriterConfig(Version.LUCENE_36, new KeywordAnalyzer());
                            break;
                        default:
                            luceneConfig = new IndexWriterConfig(Version.LUCENE_33, new StandardAnalyzer(Version.LUCENE_33));
                    }
                    try (IndexWriter writer = new IndexWriter(dir, luceneConfig)) {
//                        org.apache.lucene.document.Document doc = (org.apache.lucene.document.Document) documentData.get(i);
                        writer.addDocument((org.apache.lucene.document.Document) documentData.get(i));
                        writer.commit();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
