/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.crawler;

/**
 *
 * @author kimoto
 */
public class IndexingData {

    String field;
    String type;
    String value;
    String analyzer;

    
    public IndexingData(String field, String type, String value, String anaylzer) {
        this.field = field;
        this.type = type;
        this.value = value;
        this.analyzer = anaylzer;
    }

    public String getField(){
        return field;
    }

    public String getType(){
        return type;
    }

    public String getValue(){
        return value;
    }
    
    public String getAnalyzer(){
        return analyzer;
    }
}
