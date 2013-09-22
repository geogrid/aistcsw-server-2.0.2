/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.records;

import java.io.Serializable;
import java.util.ArrayList;
import org.apache.lucene.document.Document;

/**
 *
 * @author kimoto
 */
public class GetRecordsResult implements Serializable{
    ArrayList<org.apache.lucene.document.Document> result;
    int hits;
    
    public GetRecordsResult(ArrayList<Document> result, int hits ){
        this.result = result;
        this.hits = hits;
    }
    
    public ArrayList<Document> getResult(){
        return result;
    }
    
    public int getHits(){
        return hits;
    }
}
