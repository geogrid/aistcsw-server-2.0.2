/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.transaction;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.dbgrid.csw.filter.FilterToLuceneQuery;
import org.opengis.filter.Filter;

/**
 *
 * @author kimoto
 */
public class TransactionDelete {

    public Object delete(Filter filter, String srsName) {
        FilterToLuceneQuery cswFilter = new FilterToLuceneQuery(srsName);
        Query query = (Query) filter.accept(cswFilter, null);
        ResourceBundle resource = ResourceBundle.getBundle("csw");

        String luceneIndexingDir = resource.getString("lucene.index.dir");
        
        try {
            File[] listFiles = new File(luceneIndexingDir).listFiles();
            
            for (File file : listFiles) {
                IndexWriterConfig luceneConfig = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36));

                try (Directory dir = FSDirectory.open(file);IndexWriter writer = new IndexWriter(dir, luceneConfig)) {
                    int beforeDocNum = writer.numDocs();
                    writer.deleteDocuments(query);
                    writer.commit();
                    int afterDocNum = writer.numDocs();
                    
                }

            }

        } catch (IOException e) {
        }
        return null;

    }
}
