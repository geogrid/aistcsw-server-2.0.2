/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.search.lucene;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.geotools.geometry.jts.JTSFactoryFinder;

/**
 *
 * @author kimoto
 */
public class GeometryFilter extends Filter {

    private GeometryAccessor accessor;

    public GeometryFilter(GeometryAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
//        OpenBitSet bits = new OpenBitSet(reader.maxDoc());
        OpenBitSet bits = new OpenBitSet(1000);
        Geometry searchGeometry = accessor.getGeometry();
        long startTime = System.currentTimeMillis();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        WKTReader wktReader = new WKTReader(geometryFactory);

        //System.out.println(reader.maxDoc());
        for (int i = 0; i < reader.maxDoc(); i++) {
            //for (int i = 0; i < 10; i++) {
            Document document = reader.document(i);
            
            String[] polygonValues = document.getValues(accessor.getField());

            for (int j = 0; j < polygonValues.length; j++) {
                Geometry targetGeometry = null;

                // WKTReader
                // GMLのCQL→Geometryオブジェクトに変換できる
                //

                try {
                    
                    //           String convertStr = convert(polygonValues[j]);
                    targetGeometry = wktReader.read(polygonValues[j].split(";")[0]);
                    //                  targetGeometry = wktReader.read(convertStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                boolean check = false;

                switch (accessor.getOperationType()) {
                    case "BinarySpatialOpsType":
                        check = spatialOperation(searchGeometry, targetGeometry, accessor.getOperation());
                        break;
                    case "DistanceBufferType":
                        check = spatialOperation(searchGeometry, targetGeometry, accessor.getOperation(), accessor.getDistance(), accessor.getUnit());
                        break;
                    default:
                    //Exception

                }

                if (check) {
                    TermDocs termDocs = reader.termDocs();
                    //System.out.println(polygonValues[j]);

                    termDocs.seek(new Term(accessor.getField(), polygonValues[j]));
                    while (termDocs.next()) {
                        bits.set(termDocs.doc());
                    }
                }
                /*if (spatialOperation(searchGeometry, targetGeometry, accessor.getOperation())) {
                 TermDocs termDocs = reader.termDocs();
                 termDocs.seek(new Term(accessor.getField(), polygonValues[j]));
                 while (termDocs.next()) {
                 bits.set(termDocs.doc());
                 }
                 }*/



            }


        }

        long endTime = System.currentTimeMillis();
        long spatialExecTime = endTime - startTime;

        //System.out.println("Spatial Exec Time : " + spatialExecTime);
        return bits;
    }

    public String createPolygonStr(Coordinate[] coord) {
        StringBuilder builder = new StringBuilder();
        builder.append("POLYGON(");
        builder.append("(");
        for (int i = 0; i < coord.length; i++) {
            builder.append(coord[i].x);
            builder.append(" ");
            builder.append(coord[i].y);
            if (i != coord.length - 1) {
                builder.append(",");
            }
        }
        builder.append(")");
        builder.append(")");
        return builder.toString();
    }

    public boolean spatialOperation(Geometry searchGeometry, Geometry targetGeometry, String operation) {

        if (operation.equalsIgnoreCase("EQUALS")) {
            return searchGeometry.equals(targetGeometry);
        } else if (operation.equalsIgnoreCase("DISJOINT")) {
            return searchGeometry.disjoint(targetGeometry);
        } else if (operation.equalsIgnoreCase("INTERSECTS")) {
            return searchGeometry.intersects(targetGeometry);
        } else if (operation.equalsIgnoreCase("TOUCHES")) {
            return searchGeometry.touches(targetGeometry);
        } else if (operation.equalsIgnoreCase("CROSSES")) {
            return searchGeometry.crosses(targetGeometry);
        } else if (operation.equalsIgnoreCase("WITHIN")) {
            return searchGeometry.within(targetGeometry);
        } else if (operation.equalsIgnoreCase("OVERLAPS")) {
            return searchGeometry.overlaps(targetGeometry);
        } else if (operation.equalsIgnoreCase("CONTAINS")) {
            return searchGeometry.contains(targetGeometry);
        } else if (operation.equalsIgnoreCase("RELATE")) {
            // あとでみなおし
            return searchGeometry.relate(targetGeometry, operation);
        } else if (operation.equalsIgnoreCase("BBOX")) {
            return !searchGeometry.disjoint(targetGeometry);
        } else {
            return false;
        }
    }

    public boolean spatialOperation(Geometry searchGeometry, Geometry targetGeometry, String operation, String distance, String unit) {
        boolean retVal = false;
        //System.out.println(searchGeometry.distance(targetGeometry));
        if (operation.equalsIgnoreCase("DWITHIN")) {
            if (searchGeometry.distance(targetGeometry) <= new Double(distance)) {
                retVal = true;
            } else {
                retVal = false;
            }

        } else if (operation.equalsIgnoreCase("BEYOND")) {
            if (searchGeometry.distance(targetGeometry) > new Double(distance)) {
                retVal = true;
            } else {
                retVal = false;
            }
        } else {
        }

        return retVal;
    }
}
