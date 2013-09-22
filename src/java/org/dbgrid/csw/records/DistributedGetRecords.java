/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.records;

import net.opengis.cat.csw._2_0.GetRecordsType;
import org.dbgrid.csw.client.CSWRequest;

/**
 *
 * @author kimoto
 */
public class DistributedGetRecords extends Thread {
    private String serverUrl;
    private GetRecordsType getRecords;
    private Object result;
    
    
            
    @Override
    public void run() {
        CSWRequest request = new CSWRequest();
        result = request.cswRequest(getGetRecords(), getServerUrl());
        
    }

    /**
     * @return the serverUrl
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * @param serverUrl the serverUrl to set
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * @return the getRecords
     */
    public GetRecordsType getGetRecords() {
        return getRecords;
    }

    /**
     * @param getRecords the getRecords to set
     */
    public void setGetRecords(GetRecordsType getRecords) {
        this.getRecords = getRecords;
    }

    /**
     * @return the resultList
     */
    public Object getResult() {
        return result;
    }

    /**
     * @param resultList the resultList to set
     */
    public void setResultList(String resultList) {
        this.result = resultList;
    }
    
}
