/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import net.opengis.cat.csw._2_0.GetRecordsResponseType;
import net.opengis.cat.csw._2_0.InsertType;
import net.opengis.cat.csw._2_0.SearchResultsType;
import net.opengis.cat.csw._2_0.TransactionType;
import org.apache.lucene.document.Document;
import org.dbgrid.csw.sort.DateComparatorAsc;
import org.dbgrid.csw.sort.DateComparatorDesc;
import org.dbgrid.csw.sort.DoubleComparatorAsc;
import org.dbgrid.csw.sort.DoubleComparatorDesc;
import org.dbgrid.csw.sort.IntegerComparatorAsc;
import org.dbgrid.csw.sort.IntegerComparatorDesc;
import org.dbgrid.csw.sort.SortUtils;

/**
 *
 * @author kimoto
 */
public class CreateGetRecordsResponse {
    JAXBContext context;
    ResourceBundle resource;
    Unmarshaller unmarshaller;
    Marshaller marshaller;

    public CreateGetRecordsResponse(JAXBContext context, ResourceBundle resource, Unmarshaller unmarshaller, Marshaller marshaller) {
        this.context = context;
        this.resource = resource;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }


    private Comparator getSortComparator(String sortStr) {
        SortUtils sort = new SortUtils(context, resource, unmarshaller, marshaller);
        
        HashMap fieldInformation = sort.getFieldInformation();
        HashMap luceneSortField = (HashMap) fieldInformation.get(sortStr.split(":")[0]);

        Set keySet = luceneSortField.keySet();
        Iterator iter = keySet.iterator();
        String type = null;
        while (iter.hasNext()) {
            type = (String) luceneSortField.get((String) iter.next());
        }
        Comparator comparator = null;

        switch (type) {
            case "datetime":
                if (sortStr.split(":")[1].equalsIgnoreCase("asc")) {
                    comparator = new DateComparatorAsc();
                } else {
                    comparator = new DateComparatorDesc();
                }
                break;
            case "double":
                if (sortStr.split(":")[1].equalsIgnoreCase("asc")) {
                    comparator = new DoubleComparatorAsc();
                } else {
                    comparator = new DoubleComparatorDesc();
                }
                break;
            case "integer":
                if (sortStr.split(":")[1].equalsIgnoreCase("asc")) {
                    comparator = new IntegerComparatorAsc();
                } else {
                    comparator = new IntegerComparatorDesc();
                }
                break;
            default:

        }
        return comparator;
    }


    private ArrayList<String> createUrlList(ArrayList<Document> result, String sortStr) {
        ArrayList<String> urlList = new ArrayList<>();

        if (sortStr != null) {
            SortUtils sort = new SortUtils(context, resource, unmarshaller, marshaller);
            HashMap map = sort.getSortMap(result, sortStr);
            List<Map.Entry> entries = new ArrayList<>(map.entrySet());
            Collections.sort(entries, getSortComparator(sortStr));

            for (Map.Entry entry : entries) {
                org.apache.lucene.document.Document doc = (org.apache.lucene.document.Document) entry.getKey();
                urlList.add(doc.get("url"));
            }
        } else {
            for (org.apache.lucene.document.Document doc : result) {
                urlList.add(doc.get("url"));
            }
        }
        return urlList;
    }

    public GetRecordsResponseType createGetRecordsResponse(ArrayList<Document> result,String sortStr, BigInteger startPosition, BigInteger maxRecords) {
        ArrayList<String> urlList = createUrlList(result, sortStr);
        
        GetRecordsResponseType response = new GetRecordsResponseType();
        SearchResultsType searchResultType = new SearchResultsType();
        // 戻りの個数
        // startPosition = 1 -> maxRecordsまで返す
        // startPosition > 1 && startPosition < maxRecords -> maxRecords - startPosition + 1個返す
        // startPosition > maxRecords -> 1つも返さない

        Integer resultNum = new Integer(urlList.size());
        searchResultType.setNumberOfRecordsMatched(new BigInteger(resultNum.toString()));
        //searchResultType.setNumberOfRecordsMatched(new BigInteger(hits.toString()));

        Integer startPositionInt = startPosition.intValue();
        Integer maxRecordsInt = maxRecords.intValue();

        maxRecordsInt = (resultNum - startPositionInt + 1 < maxRecordsInt ? resultNum - startPositionInt + 1 : maxRecordsInt);
        Integer returnNum = (resultNum < maxRecordsInt ? resultNum : maxRecordsInt);
        searchResultType.setNumberOfRecordsReturned(new BigInteger(returnNum.toString()));
        int returnPos = (startPositionInt + returnNum - 1 < resultNum ? startPositionInt + returnNum - 1 : resultNum);
        for (int i = startPositionInt - 1; i < returnPos; i++) {
            try {

                FileInputStream fis = new FileInputStream(new File(urlList.get(i)));
                JAXBElement object = (JAXBElement) unmarshaller.unmarshal(fis);
//                                        searchResultType.getAny().add(object);

                if (object.getValue() instanceof TransactionType) {
                    TransactionType transaction = (TransactionType) object.getValue();
                    List<Object> transactionList = transaction.getInsertOrUpdateOrDelete();
                    for (int j = 0; j < transactionList.size(); j++) {
                        if (transactionList.get(j) instanceof InsertType) {
                            InsertType insert = (InsertType) transactionList.get(j);
                            List<Object> anyList = insert.getAny();
                            for (int k = 0; k < anyList.size(); k++) {
                                searchResultType.getAny().add(anyList.get(j));
                            }
                        }
                    }

                } else {
                    searchResultType.getAny().add(object);
                }
            } catch (FileNotFoundException | JAXBException e) {
                e.printStackTrace();
            }

        }

        response.setSearchResults(searchResultType);


        return response;
    }

    public GetRecordsResponseType createGetRecordsResponse(ArrayList<Document> result,String sortStr, BigInteger startPosition, BigInteger maxRecords, Integer hits) {
        ArrayList<String> urlList = createUrlList(result, sortStr);
        
        GetRecordsResponseType response = new GetRecordsResponseType();
        SearchResultsType searchResultType = new SearchResultsType();
        // 戻りの個数
        // startPosition = 1 -> maxRecordsまで返す
        // startPosition > 1 && startPosition < maxRecords -> maxRecords - startPosition + 1個返す
        // startPosition > maxRecords -> 1つも返さない

        Integer resultNum = new Integer(urlList.size());
        
        //searchResultType.setNumberOfRecordsMatched(new BigInteger(hits));
        searchResultType.setNumberOfRecordsMatched(new BigInteger(hits.toString()));

        Integer startPositionInt = startPosition.intValue();
        Integer maxRecordsInt = maxRecords.intValue();

        maxRecordsInt = (resultNum - startPositionInt + 1 < maxRecordsInt ? resultNum - startPositionInt + 1 : maxRecordsInt);
        Integer returnNum = (resultNum < maxRecordsInt ? resultNum : maxRecordsInt);
        searchResultType.setNumberOfRecordsReturned(new BigInteger(returnNum.toString()));
        int returnPos = (startPositionInt + returnNum - 1 < resultNum ? startPositionInt + returnNum - 1 : resultNum);
        for (int i = startPositionInt - 1; i < returnPos; i++) {
            try {

                FileInputStream fis = new FileInputStream(new File(urlList.get(i)));
                JAXBElement object = (JAXBElement) unmarshaller.unmarshal(fis);
//                                        searchResultType.getAny().add(object);

                if (object.getValue() instanceof TransactionType) {
                    TransactionType transaction = (TransactionType) object.getValue();
                    List<Object> transactionList = transaction.getInsertOrUpdateOrDelete();
                    for (int j = 0; j < transactionList.size(); j++) {
                        if (transactionList.get(j) instanceof InsertType) {
                            InsertType insert = (InsertType) transactionList.get(j);
                            List<Object> anyList = insert.getAny();
                            for (int k = 0; k < anyList.size(); k++) {
                                searchResultType.getAny().add(anyList.get(j));
                            }
                        }
                    }

                } else {
                    searchResultType.getAny().add(object);
                }
            } catch (FileNotFoundException | JAXBException e) {
                e.printStackTrace();
            }

        }

        response.setSearchResults(searchResultType);


        return response;
    }
}
