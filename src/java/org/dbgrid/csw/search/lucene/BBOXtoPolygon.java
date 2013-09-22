/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dbgrid.csw.search.lucene;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author kimoto
 */
public class BBOXtoPolygon {

    public static Geometry bboxToPolygon(String bboxStr){
        String bboxCore = bboxStr.replace("BBOX", "").replace("(", "").replace(")", "");
        String[] posArray = bboxCore.split(",");
        String x_bbox_lower = posArray[0].trim().split(" ")[0];
        String y_bbox_lower = posArray[0].trim().split(" ")[1];
        String x_bbox_upper = posArray[1].trim().split(" ")[0];
        String y_bbox_upper = posArray[1].trim().split(" ")[1];

        Coordinate[] bboxCoordinates = new Coordinate[5];
        for (int i = 0; i < bboxCoordinates.length; i++) {
            bboxCoordinates[i] = new Coordinate();
        }
        bboxCoordinates[0].x = new Double(x_bbox_lower);
        bboxCoordinates[0].y = new Double(y_bbox_lower);
        bboxCoordinates[1].x = new Double(x_bbox_upper);
        bboxCoordinates[1].y = new Double(y_bbox_lower);
        bboxCoordinates[2].x = new Double(x_bbox_upper);
        bboxCoordinates[2].y = new Double(y_bbox_upper);
        bboxCoordinates[3].x = new Double(x_bbox_lower);
        bboxCoordinates[3].y = new Double(y_bbox_upper);
        bboxCoordinates[4].x = new Double(x_bbox_lower);
        bboxCoordinates[4].y = new Double(y_bbox_lower);

        GeometryFactory bboxFactory = new GeometryFactory();
        LinearRing bboxRing = bboxFactory.createLinearRing(bboxCoordinates);
        Polygon bboxPolygon = new Polygon(bboxRing, null, bboxFactory);

        return bboxPolygon;

    }

}
