/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.records;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import net.opengis.cat.csw._2_0.GetRecordsType;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.dbgrid.csw.filter.FilterToLuceneQuery;
import org.dbgrid.csw.sort.SortUtils;
import org.opengis.filter.Filter;

/**
 *
 * @author kimoto
 */
public class GetRecords {

    JAXBContext context;
    ResourceBundle resource;
    Unmarshaller unmarshaller;
    Marshaller marshaller;
    String srsName;
    
    public GetRecords(JAXBContext context, ResourceBundle resource, Unmarshaller unmarshaller, Marshaller marshaller, String srsName) {
        this.context = context;
        this.resource = resource;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
        this.srsName = srsName;
        
    }
    
    int hitNum;

    public int getHitNum() {
        return hitNum;
    }

    public ArrayList<Document> getRecords(GetRecordsType getRecords, Filter filter) {
        ArrayList<Document> result = new ArrayList<>();

        FilterToLuceneQuery cswFilter = new FilterToLuceneQuery(srsName);
        Query query = (Query) filter.accept(cswFilter, null);

        try {
            String indexDir = resource.getString("lucene.index.dir");
            File[] indexSubDir = new File(indexDir).listFiles();

            Directory[] dirs = new Directory[indexSubDir.length];
            IndexReader[] readers = new IndexReader[indexSubDir.length];

            for (int i = 0; i < indexSubDir.length; i++) {
                dirs[i] = FSDirectory.open(indexSubDir[i].getAbsoluteFile());
                readers[i] = IndexReader.open(dirs[i]);

            }

            try (MultiReader reader = new MultiReader(readers)) {

                ExecutorService service = Executors.newFixedThreadPool(new Integer(resource.getString("csw.thread.num")));


                try (IndexSearcher searcher = new IndexSearcher(reader, service)) {
                    TopDocs hits = searcher.search(query, searcher.getIndexReader().numDocs());
                    hitNum = hits.totalHits;

                    ScoreDoc[] docs = hits.scoreDocs;
                    service.shutdown();

                    if (hitNum != 0) {
                        for (ScoreDoc doc : docs) {
                            result.add(searcher.doc(doc.doc));
                        }
                    }
                }
            }
            for (IndexReader reader : readers) {
                reader.close();
            }

            for (Directory dir : dirs) {
                dir.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

    public ArrayList<Document> getRecords(GetRecordsType getRecords, Filter filter, String sortStr) {
        
        String[] inputSortParam = sortStr.split(",");

        SortUtils sort = new SortUtils(context, resource, unmarshaller, marshaller);

        Sort sortObj = null;
        
        for (String luceneSortParameter : inputSortParam) {
            int sortType = 0;
            String sortField = luceneSortParameter.split(":")[0];
            String dataType = sort.getSortFieldType(sortField);
            if (dataType.equalsIgnoreCase("string")) {
                sortType = SortField.STRING;
            } else if (dataType.equalsIgnoreCase("int")) {
                sortType = SortField.INT;
            } else if (dataType.equalsIgnoreCase("datetime")) {
                sortType = SortField.STRING;
            }

            String sortOrder = luceneSortParameter.split(":")[1];
            if (sortOrder.equalsIgnoreCase("A") || sortOrder.equalsIgnoreCase("ASC")) {
                sortObj = new Sort(new SortField(sortField, sortType, false));
            } else {
                sortObj = new Sort(new SortField(sortField, sortType, true));
            }
        }

        ArrayList<Document> result = new ArrayList<>();

        FilterToLuceneQuery cswFilter = new FilterToLuceneQuery(srsName);
        Query query = (Query) filter.accept(cswFilter, null);

        try {
            String indexDir = resource.getString("lucene.index.dir");
            File[] indexSubDir = new File(indexDir).listFiles();

            Directory[] dirs = new Directory[indexSubDir.length];
            IndexReader[] readers = new IndexReader[indexSubDir.length];

            for (int i = 0; i < indexSubDir.length; i++) {
                dirs[i] = FSDirectory.open(indexSubDir[i].getAbsoluteFile());
                readers[i] = IndexReader.open(dirs[i]);

            }

            try (MultiReader reader = new MultiReader(readers)) {

                ExecutorService service = Executors.newFixedThreadPool(new Integer(resource.getString("csw.thread.num")));

                try (IndexSearcher searcher = new IndexSearcher(reader, service)) {
                    TopDocs hits = searcher.search(query, searcher.getIndexReader().numDocs(),sortObj);
                    hitNum = hits.totalHits;

                    ScoreDoc[] docs = hits.scoreDocs;
                    service.shutdown();

                    /*if (hitNum != 0) {
                        for (ScoreDoc doc : docs) {
                            result.add(searcher.doc(doc.doc));
                        }
                    }*/
                    int requestMaxRecords = getRecords.getMaxRecords().intValue();
                    int requestStartPosition = getRecords.getStartPosition().intValue();
                    int responseRecordsNum = requestMaxRecords + requestStartPosition;
                    
                    int returnNum = docs.length < responseRecordsNum ? docs.length : responseRecordsNum;
                    for(int i = 0; i < returnNum ; i++){
                        result.add(searcher.doc(docs[i].doc));
                    }
                    
                    /*
                    int requestMaxRecords = getRecords.getMaxRecords().intValue();
                    int searchMaxRecords = new Integer(resource.getString("csw.search.max"));
                    
                    int responseRecordsNum = requestMaxRecords < searchMaxRecords ? requestMaxRecords : searchMaxRecords;
                    
                    for(int i = 0; i < responseRecordsNum ; i++){
                        result.add(searcher.doc(docs[i].doc));
                    }*/
                }
            }
            for (IndexReader reader : readers) {
                reader.close();
            }

            for (Directory dir : dirs) {
                dir.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }
}
