/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dbgrid.csw.search.lucene;

import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author kimoto
 */
public class GeometryAccessor {
    Geometry geometry;
    String operation;
    String field;
    String distance;
    String unit;
    String operationType;
    
    public GeometryAccessor(Geometry geometry, String operation, String field, String operationType){
        this.geometry = geometry;
        this.operation = operation;
        this.field = field;
        this.operationType = operationType;
    }
        
    public GeometryAccessor(Geometry geometry, String operation, String field, String distance, String unit, String operationType){
        this.geometry = geometry;
        this.operation = operation;
        this.field = field;
        this.distance = distance;
        this.unit = unit;
        this.operationType = operationType;
    }
    
    public Geometry getGeometry(){
        return geometry;
    }

    public String getOperation(){
        return operation;
    }

    public String getField(){
        return field;
    }
    
    public String getDistance(){
        return distance;
    }
    
    public String getUnit(){
        return  unit;
    }
    
    public String getOperationType(){
        return operationType;
    }
}
