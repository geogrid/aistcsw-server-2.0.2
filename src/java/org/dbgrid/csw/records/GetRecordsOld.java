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
import org.opengis.filter.Filter;

/**
 *
 * @author kimoto
 */
public class GetRecordsOld {
    public ArrayList<String> getSortParamList(String sortParam){
        ArrayList<String> sortParamList = new ArrayList<>();
        
        String[] originalList = sortParam.split(",");
        for(int i = 0; i < originalList.length; i++){
            sortParamList.add(originalList[i].split(":")[0]);
        }
        return sortParamList;
        
    }
    
    public TopDocs getRecords1(GetRecordsType getRecords, Filter filter) {
        // Convert GetRecords Filter to Lucene Query
        TopDocs hits = null;
        ResourceBundle resource = ResourceBundle.getBundle("csw");
            
            
        FilterToLuceneQuery cswFilter = new FilterToLuceneQuery("");
        Query query = (Query) filter.accept(cswFilter, null);
        int hitNum = 0;
        
        long startSearchTime = System.currentTimeMillis();
                
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
                    hits = searcher.search(query, searcher.getIndexReader().numDocs());
                    //result.setTotalHits(hits.totalHits);
                    /*
                    ScoreDoc[] docs = hits.scoreDocs;
                    service.shutdown();
                    
                    for (int i = 0; i < docs.length; i++) {
                        Document doc = searcher.doc(docs[i].doc);
                        System.out.println(doc.get("url"));
                        for(int j = 0; j < getSortParamList(sortParam).size(); j++){
                            System.out.println(getSortParamList(sortParam).get(j));
                            System.out.println(doc.get(getSortParamList(sortParam).get(j)));
                        }
                    }
                    hitNum = docs.length;*/
                }
            }
            for(int i = 0; i < readers.length; i++){
                readers[i].close();
            }
            for(int i = 0; i < indexSubDir.length; i++){
                dirs[i].close();
            }
            

        } catch (IOException e) {
            e.printStackTrace();
        }
        long endSearchTime = System.currentTimeMillis();

        long searchExecTime = endSearchTime - startSearchTime;
        System.out.println(searchExecTime + " : " + hitNum);
        /*try {
            configFile.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Result result = new Result();
        result.setDocs(hits);*/
        
        return hits;

    }

        
    public ArrayList<Document> getRecords(GetRecordsType getRecords, Filter filter) {
        ArrayList<Document> result = new ArrayList<>();
        
        ResourceBundle resource = ResourceBundle.getBundle("csw");
            
        FilterToLuceneQuery cswFilter = new FilterToLuceneQuery("");
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
                    //result.setTotalHits(hits.totalHits);
                    
                    ScoreDoc[] docs = hits.scoreDocs;
                    service.shutdown();
                    
                    for (int i = 0; i < docs.length; i++) {
                        result.add(searcher.doc(docs[i].doc));
                    }
                    
                }
            }
            for(int i = 0; i < readers.length; i++){
                readers[i].close();
            }
            for(int i = 0; i < indexSubDir.length; i++){
                dirs[i].close();
            }
            

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return result;

    }

    public TopDocs getRecords(GetRecordsType getRecords, Filter filter, String sortParam) {
        // Convert GetRecords Filter to Lucene Query
        TopDocs hits = null;
        
        Sort sortObj;
        ResourceBundle resource = ResourceBundle.getBundle("csw");
        String[] sortParameter = resource.getString("lucene.sort.parameter").split(",");

        String[] inputSortParam = sortParam.split(":");
        int sortType = 0;

        for (String luceneSortParameter : sortParameter) {
            if (luceneSortParameter.startsWith(inputSortParam[0])) {
                String dataType = luceneSortParameter.split(":")[1];
                if (dataType.equalsIgnoreCase("string")) {
                    sortType = SortField.STRING;
                } else if (dataType.equalsIgnoreCase("int")) {
                    sortType = SortField.INT;
                }else if(dataType.equalsIgnoreCase("datetime")){
                    sortType = SortField.STRING;
                }
            }
        }

        if (inputSortParam[1].equalsIgnoreCase("A") || inputSortParam[1].equalsIgnoreCase("ASC")) {
            sortObj = new Sort(new SortField(inputSortParam[0], sortType, false));
        } else {
            sortObj = new Sort(new SortField(inputSortParam[0], sortType, true));
        }
        
        FilterToLuceneQuery cswFilter = new FilterToLuceneQuery("");
        Query query = (Query) filter.accept(cswFilter, null);
        int hitNum = 0;
        
        long startSearchTime = System.currentTimeMillis();
                
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
                    hits = searcher.search(query, searcher.getIndexReader().numDocs(),sortObj);
                    //result.setTotalHits(hits.totalHits);
                    /*
                    ScoreDoc[] docs = hits.scoreDocs;
                    service.shutdown();
                    
                    for (int i = 0; i < docs.length; i++) {
                        Document doc = searcher.doc(docs[i].doc);
                        System.out.println(doc.get("url"));
                        for(int j = 0; j < getSortParamList(sortParam).size(); j++){
                            System.out.println(getSortParamList(sortParam).get(j));
                            System.out.println(doc.get(getSortParamList(sortParam).get(j)));
                        }
                    }
                    hitNum = docs.length;*/
                }
            }
            for(int i = 0; i < readers.length; i++){
                readers[i].close();
            }
            for(int i = 0; i < indexSubDir.length; i++){
                dirs[i].close();
            }
            

        } catch (IOException e) {
            e.printStackTrace();
        }
        long endSearchTime = System.currentTimeMillis();

        long searchExecTime = endSearchTime - startSearchTime;
        System.out.println(searchExecTime + " : " + hitNum);
        /*try {
            configFile.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        */
        return hits;

    }
    
}
