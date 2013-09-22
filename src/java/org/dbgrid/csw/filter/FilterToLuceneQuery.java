/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.NumericUtils;
import org.dbgrid.csw.schema.search.SearchConfig;
import org.dbgrid.csw.search.lucene.GeometryAccessor;
import org.dbgrid.csw.search.lucene.GeometryFilter;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.LiteralExpressionImpl;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.joda.time.DateTime;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

/**
 *
 * @author kimoto
 */
public class FilterToLuceneQuery extends DefaultFilterVisitor {

    JAXBContext context;
    Unmarshaller unmarshller;
    SearchConfig config;
    String configFile;
    String srsName;
    //String searchType;

    public FilterToLuceneQuery() {

        try {
            ResourceBundle rb = ResourceBundle.getBundle("csw");
            configFile = rb.getString("lucene.field.definition.file");
            context = JAXBContext.newInstance("org.dbgrid.csw.schema.search:");
            // Unmarshaller生成
            unmarshller = context.createUnmarshaller();
            config = (SearchConfig) unmarshller.unmarshal(new File(configFile));
            this.srsName = "epsg4326";
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
    }
    public FilterToLuceneQuery(String srsName) {

        try {
            ResourceBundle rb = ResourceBundle.getBundle("csw");
            configFile = rb.getString("lucene.field.definition.file");
            context = JAXBContext.newInstance("org.dbgrid.csw.schema.search:");
            // Unmarshaller生成
            unmarshller = context.createUnmarshaller();
            config = (SearchConfig) unmarshller.unmarshal(new File(configFile));
            String[] srsNameArray = srsName.split(":");
            this.srsName = "epsg" + srsNameArray[srsNameArray.length - 1];
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Object visit(And operation, Object object) {
        BooleanQuery bq = new BooleanQuery();
        List<Filter> list = operation.getChildren();
        for (Filter filter : list) {
            //Filter subFilter = list.get(i);
            Object accept = filter.accept(this, null);
            if (accept != null) {
                bq.add((Query) accept, Occur.MUST);
            }
        }
        return bq;
    }

    @Override
    public Object visit(Or operation, Object object) {
        BooleanQuery bq = new BooleanQuery();
        List<Filter> list = operation.getChildren();
        for (Filter filter : list) {
            //Filter subFilter = list.get(i);
            Object accept = filter.accept(this, null);
            if (accept != null) {
                bq.add((Query) accept, Occur.SHOULD);
            }
        }
        /*
        for (int i = 0; i < list.size(); i++) {
            Filter subFilter = list.get(i);
            Object accept = subFilter.accept(this, null);
            if (accept != null) {
                bq.add((Query) accept, Occur.SHOULD);
            }
        }*/
        return bq;
    }

    @Override
    public Object visit(Not operation, Object object) {
        BooleanQuery bq = new BooleanQuery();
        Filter subFilter = operation.getFilter();
        Object accept = subFilter.accept(this, null);
        if (accept != null) {
            bq.add((Query) accept, Occur.MUST_NOT);
        }
        return bq;
    }

    @Override
    public Object visit(PropertyIsLessThan operation, Object object) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;
        String type = null;

        while (iter.hasNext()) {
            field = (String) iter.next();
            type = (String) fieldInfo.get(field);
        }
        Object literal = null;

        if (expression2 instanceof AttributeExpressionImpl) {
            literal = (AttributeExpressionImpl) expression2;

        } else if (expression2 instanceof LiteralExpressionImpl) {
            literal = (LiteralExpressionImpl) expression2;
        }

        return getComparisonQuery("PropertyIsLessThan", field, type, null, literal.toString());


    }

    @Override
    public Object visit(PropertyIsLessThanOrEqualTo operation, Object object) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;
        String type = null;

        while (iter.hasNext()) {
            field = (String) iter.next();
            type = (String) fieldInfo.get(field);
        }
        Object literal = null;

        if (expression2 instanceof AttributeExpressionImpl) {
            literal = (AttributeExpressionImpl) expression2;

        } else if (expression2 instanceof LiteralExpressionImpl) {
            literal = (LiteralExpressionImpl) expression2;
        }


        return getComparisonQuery("PropertyIsLessThanOrEqualTo", field, type, null, literal.toString());


    }

    @Override
    public Object visit(PropertyIsGreaterThan operation, Object object) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;
        String type = null;

        while (iter.hasNext()) {
            field = (String) iter.next();
            type = (String) fieldInfo.get(field);
        }

        Object literal = null;

        if (expression2 instanceof AttributeExpressionImpl) {
            literal = (AttributeExpressionImpl) expression2;

        } else if (expression2 instanceof LiteralExpressionImpl) {
            literal = (LiteralExpressionImpl) expression2;
        }

        return getComparisonQuery("PropertyIsGreaterThan", field, type, literal.toString(), null);


    }

    @Override
    public Object visit(PropertyIsGreaterThanOrEqualTo operation, Object object) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;
        String type = null;

        while (iter.hasNext()) {
            field = (String) iter.next();
            type = (String) fieldInfo.get(field);
        }
        Object literal = null;

        if (expression2 instanceof AttributeExpressionImpl) {
            literal = (AttributeExpressionImpl) expression2;

        } else if (expression2 instanceof LiteralExpressionImpl) {
            literal = (LiteralExpressionImpl) expression2;
        }

        return getComparisonQuery("PropertyIsGreaterThanOrEqualTo", field, type, literal.toString(), null);



    }

    @Override
    public Object visit(PropertyIsBetween operation, Object data) {
        Expression expression = operation.getExpression();
        Expression lower = operation.getLowerBoundary();
        Expression upper = operation.getUpperBoundary();

        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;
        String type = null;

        while (iter.hasNext()) {
            field = (String) iter.next();
            type = (String) fieldInfo.get(field);
        }

        Object lowerLiteral = null;

        if (lower instanceof AttributeExpressionImpl) {
            lowerLiteral = (AttributeExpressionImpl) lower;

        } else if (lower instanceof LiteralExpressionImpl) {
            lowerLiteral = (LiteralExpressionImpl) lower;
        }

        Object upperLiteral = null;

        if (upper instanceof AttributeExpressionImpl) {
            upperLiteral = (AttributeExpressionImpl) upper;

        } else if (upper instanceof LiteralExpressionImpl) {
            upperLiteral = (LiteralExpressionImpl) upper;
        }

        return getComparisonQuery("PropertyIsBetween", field, type, lowerLiteral.toString(), upperLiteral.toString());


    }

    @Override
    public Object visit(PropertyIsEqualTo operation, Object object) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        if (propName.contains("objectType")) {
            return new MatchAllDocsQuery();
        }

        if (expression2 instanceof AttributeExpressionImpl) {
            return new MatchAllDocsQuery();
        } else if (expression2 instanceof LiteralExpressionImpl) {
            HashMap fieldInfo = (HashMap) luceneField.get(propName);

            Set keySet = fieldInfo.keySet();
            Iterator iter = keySet.iterator();
            String field = null;
            String type = null;

            while (iter.hasNext()) {
                field = (String) iter.next();
                type = (String) fieldInfo.get(field);
            }
            Object literal = null;

            literal = (LiteralExpressionImpl) expression2;
            return getComparisonQuery("PropertyIsEqualTo", field, type, literal.toString(), literal.toString());
        }



        return null;

    }

    @Override
    public Object visit(PropertyIsNotEqualTo operation, Object object) {
        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;
        String type = null;

        while (iter.hasNext()) {
            field = (String) iter.next();
            type = (String) fieldInfo.get(field);
        }
        Object literal = null;

        if (expression2 instanceof AttributeExpressionImpl) {
            literal = (AttributeExpressionImpl) expression2;

        } else if (expression2 instanceof LiteralExpressionImpl) {
            literal = (LiteralExpressionImpl) expression2;
        }

        return getComparisonQuery("PropertyIsNotEqualTo", field, type, literal.toString(), literal.toString());


    }

    @Override
    public Object visit(PropertyIsLike filter, Object data) {
        Expression expression = filter.getExpression();

        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression;
        
        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;
        String type = null;

        while (iter.hasNext()) {
            field = (String) iter.next();
            type = (String) fieldInfo.get(field);
        }
        
        String literal = filter.getLiteral();
        
        System.out.println(field + literal);
        
        WildcardQuery query = new WildcardQuery(new Term(field, literal));
        return query;
    }

    @Override
    public Object visit(PropertyIsNull filter, Object data) {
        return super.visit(filter, data);
    }

    @Override
    public Object visit(Equals operation, Object object) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;


        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry searchGeometry = null;

        if (expression2 instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl literalExp = (LiteralExpressionImpl) expression2;
            searchGeometry = (Geometry) literalExp.getValue();
        }
        return getSpatialFilteredQuery(searchGeometry, Equals.NAME, field + srsName);

    }

    @Override
    public Object visit(Disjoint operation, Object object) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;


        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry searchGeometry = null;

        if (expression2 instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl literalExp = (LiteralExpressionImpl) expression2;
            searchGeometry = (Geometry) literalExp.getValue();
        }
        return getSpatialFilteredQuery(searchGeometry, Disjoint.NAME, field + srsName);

    }

    @Override
    public Object visit(Intersects operation, Object object) {
        
        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();

        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();
        
        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;

        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry searchGeometry = null;

        if (expression2 instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl literalExp = (LiteralExpressionImpl) expression2;
            searchGeometry = (Geometry) literalExp.getValue();
        }
        return getSpatialFilteredQuery(searchGeometry, Intersects.NAME, field + srsName);

    }

    @Override
    public Object visit(Touches operation, Object object) {


        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;


        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry searchGeometry = null;

        if (expression2 instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl literalExp = (LiteralExpressionImpl) expression2;
            searchGeometry = (Geometry) literalExp.getValue();
        }
        return getSpatialFilteredQuery(searchGeometry, Touches.NAME, field + srsName);

    }

    @Override
    public Object visit(Crosses operation, Object object) {


        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;


        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry searchGeometry = null;

        if (expression2 instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl literalExp = (LiteralExpressionImpl) expression2;
            searchGeometry = (Geometry) literalExp.getValue();
        }
        return getSpatialFilteredQuery(searchGeometry, Crosses.NAME, field + srsName);

    }

    @Override
    public Object visit(Within operation, Object object) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;


        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry searchGeometry = null;

        if (expression2 instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl literalExp = (LiteralExpressionImpl) expression2;
            searchGeometry = (Geometry) literalExp.getValue();
        }
        return getSpatialFilteredQuery(searchGeometry, Within.NAME, field + srsName);



    }

    @Override
    public Object visit(Overlaps operation, Object object) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;


        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry searchGeometry = null;

        if (expression2 instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl literalExp = (LiteralExpressionImpl) expression2;
            searchGeometry = (Geometry) literalExp.getValue();
        }
        return getSpatialFilteredQuery(searchGeometry, Overlaps.NAME, field + srsName);

    }

    @Override
    public Object visit(Contains operation, Object data) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;


        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry searchGeometry = null;

        if (expression2 instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl literalExp = (LiteralExpressionImpl) expression2;
            searchGeometry = (Geometry) literalExp.getValue();
        }
        return getSpatialFilteredQuery(searchGeometry, Contains.NAME, field + srsName);

    }

    @Override
    public Object visit(Beyond operation, Object data) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;


        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry searchGeometry = null;

        if (expression2 instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl literalExp = (LiteralExpressionImpl) expression2;
            searchGeometry = (Geometry) literalExp.getValue();
        }
        Double distance = operation.getDistance();
        String unit = operation.getDistanceUnits();

        return getSpatialDistanceFilteredQuery(searchGeometry, Beyond.NAME, field + srsName, distance.toString(), unit);

    }

    @Override
    public Object visit(DWithin operation, Object data) {

        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();

        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;


        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry searchGeometry = null;

        if (expression2 instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl literalExp = (LiteralExpressionImpl) expression2;
            searchGeometry = (Geometry) literalExp.getValue();
        }
        Double distance = operation.getDistance();
        String unit = operation.getDistanceUnits();

        return getSpatialDistanceFilteredQuery(searchGeometry, DWithin.NAME, field + srsName, distance.toString(), unit);

    }

    @Override
    public Object visit(BBOX operation, Object data) {


        Expression expression1 = operation.getExpression1();
        Expression expression2 = operation.getExpression2();
        AttributeExpressionImpl attr = (AttributeExpressionImpl) expression1;

        HashMap luceneField = getFieldInformation();
        String propName = attr.getPropertyName();
        HashMap fieldInfo = (HashMap) luceneField.get(propName);

        Set keySet = fieldInfo.keySet();
        Iterator iter = keySet.iterator();
        String field = null;

        while (iter.hasNext()) {
            field = (String) iter.next();
        }

        Geometry bboxObj = null;
        BooleanQuery bq = new BooleanQuery();

        if (expression2 instanceof BBoxExpression) {
            BBoxExpression bboxExp = (BBoxExpression) expression2;
            bboxObj = (Polygon) bboxExp.getValue();

            Coordinate[] coords = bboxObj.getCoordinates();
            double x_bbox_lower = coords[0].x;
            double y_bbox_lower = coords[0].y;
            double x_bbox_upper = coords[0].x;
            double y_bbox_upper = coords[0].y;

            for (int i = 1; i < coords.length; i++) {
                if (x_bbox_lower > coords[i].x) {
                    x_bbox_lower = coords[i].x;
                }

                if (x_bbox_upper < coords[i].x) {
                    x_bbox_upper = coords[i].x;
                }

                if (y_bbox_lower > coords[i].y) {
                    y_bbox_lower = coords[i].y;
                }

                if (y_bbox_upper < coords[i].y) {
                    y_bbox_upper = coords[i].y;
                }
            }
            // lowerXの判定
            bq.add(yfixBooleanQuery("lowerY", x_bbox_lower, x_bbox_upper, y_bbox_lower, y_bbox_upper), Occur.SHOULD);
            bq.add(yfixBooleanQuery("upperY", x_bbox_lower, x_bbox_upper, y_bbox_lower, y_bbox_upper), Occur.SHOULD);
            bq.add(xfixBooleanQuery("lowerX", x_bbox_lower, x_bbox_upper, y_bbox_lower, y_bbox_upper), Occur.SHOULD);
            bq.add(xfixBooleanQuery("upperX", x_bbox_lower, x_bbox_upper, y_bbox_lower, y_bbox_upper), Occur.SHOULD);
            bq.add(containsQuery(x_bbox_lower, x_bbox_upper, y_bbox_lower, y_bbox_upper), Occur.SHOULD);
            bq.add(overrapQuery(x_bbox_lower, x_bbox_upper, y_bbox_lower, y_bbox_upper),Occur.SHOULD);
            /*
            Query query1 = new TermRangeQuery("upperX", null, NumericUtils.doubleToPrefixCoded(x_bbox_lower), true, true);
            Query query2 = new TermRangeQuery("lowerX", NumericUtils.doubleToPrefixCoded(x_bbox_upper), null, true, true);
            Query query3 = new TermRangeQuery("upperY", null, NumericUtils.doubleToPrefixCoded(y_bbox_lower), true, true);
            Query query4 = new TermRangeQuery("lowerY", NumericUtils.doubleToPrefixCoded(y_bbox_upper), null, true, true);
            BooleanQuery bq = new BooleanQuery();
            bq.add(query1, Occur.SHOULD);
            bq.add(query2, Occur.SHOULD);
            bq.add(query3, Occur.SHOULD);
            bq.add(query4, Occur.SHOULD);
            
            nbq.add(new MatchAllDocsQuery(), Occur.SHOULD);
            nbq.add(bq, Occur.MUST_NOT);
             */

        }
        //return getSpatialFilteredQuery(bboxObj, operation.NAME, field);
        return getBBOXQuery(bq, bboxObj, BBOX.NAME, field + srsName);


    }

    public BooleanQuery containsQuery(double x_bbox_lower, double x_bbox_upper, double y_bbox_lower, double y_bbox_upper) {
        BooleanQuery containsQuery = new BooleanQuery();
        containsQuery.add(new TermRangeQuery("lowerX", NumericUtils.doubleToPrefixCoded(x_bbox_lower), NumericUtils.doubleToPrefixCoded(x_bbox_upper), true, true), Occur.MUST);
        containsQuery.add(new TermRangeQuery("lowerY", NumericUtils.doubleToPrefixCoded(y_bbox_lower), NumericUtils.doubleToPrefixCoded(y_bbox_upper), true, true), Occur.MUST);
        containsQuery.add(new TermRangeQuery("upperX", NumericUtils.doubleToPrefixCoded(x_bbox_lower), NumericUtils.doubleToPrefixCoded(x_bbox_upper), true, true), Occur.MUST);
        containsQuery.add(new TermRangeQuery("upperY", NumericUtils.doubleToPrefixCoded(y_bbox_lower), NumericUtils.doubleToPrefixCoded(y_bbox_upper), true, true), Occur.MUST);


        return containsQuery;
    }

    public BooleanQuery overrapQuery(double x_bbox_lower, double x_bbox_upper, double y_bbox_lower, double y_bbox_upper) {
        BooleanQuery overrapQuery = new BooleanQuery();
        overrapQuery.add(new TermRangeQuery("lowerX", null, NumericUtils.doubleToPrefixCoded(x_bbox_lower), true, true), Occur.MUST);
        overrapQuery.add(new TermRangeQuery("upperX", NumericUtils.doubleToPrefixCoded(x_bbox_upper), null, true, true), Occur.MUST);
        overrapQuery.add(new TermRangeQuery("lowerY", null, NumericUtils.doubleToPrefixCoded(y_bbox_lower), true, true), Occur.MUST);
        overrapQuery.add(new TermRangeQuery("upperY", NumericUtils.doubleToPrefixCoded(y_bbox_upper), null, true, true), Occur.MUST);


        return overrapQuery;
    }

    public BooleanQuery yfixBooleanQuery(String yName, double x_bbox_lower, double x_bbox_upper, double y_bbox_lower, double y_bbox_upper) {


        BooleanQuery xBool = new BooleanQuery();

        // x_bbox_lower <= lowerX <= x_bbox_upper
        xBool.add(new TermRangeQuery("lowerX", NumericUtils.doubleToPrefixCoded(x_bbox_lower), NumericUtils.doubleToPrefixCoded(x_bbox_upper), true, true), Occur.SHOULD);

        // lowerX <= x_bbox_lower AND upperX >= x_bbox_upper
        BooleanQuery subbq = new BooleanQuery();
        subbq.add(new TermRangeQuery("lowerX", null, NumericUtils.doubleToPrefixCoded(x_bbox_lower), true, true), Occur.MUST);
        subbq.add(new TermRangeQuery("upperX", NumericUtils.doubleToPrefixCoded(x_bbox_upper), null, true, true), Occur.MUST);
        xBool.add(subbq, Occur.SHOULD);

        // x_bbox_lower <= upperX <= x_bbox_upper
        xBool.add(new TermRangeQuery("upperX", NumericUtils.doubleToPrefixCoded(x_bbox_lower), NumericUtils.doubleToPrefixCoded(x_bbox_upper), true, true), Occur.SHOULD);

        BooleanQuery yFixBool = new BooleanQuery();
        yFixBool.add(new TermRangeQuery(yName, NumericUtils.doubleToPrefixCoded(y_bbox_lower), NumericUtils.doubleToPrefixCoded(y_bbox_upper), true, true), Occur.MUST);
        yFixBool.add(xBool, Occur.SHOULD);

        return yFixBool;

    }

    public BooleanQuery xfixBooleanQuery(String xName, double x_bbox_lower, double x_bbox_upper, double y_bbox_lower, double y_bbox_upper) {


        BooleanQuery yBool = new BooleanQuery();

        // x_bbox_lower <= lowerX <= x_bbox_upper
        yBool.add(new TermRangeQuery("lowerY", NumericUtils.doubleToPrefixCoded(y_bbox_lower), NumericUtils.doubleToPrefixCoded(y_bbox_upper), true, true), Occur.SHOULD);

        // lowerX <= x_bbox_lower AND upperX >= x_bbox_upper
        BooleanQuery subbq = new BooleanQuery();
        subbq.add(new TermRangeQuery("lowerY", null, NumericUtils.doubleToPrefixCoded(y_bbox_lower), true, true), Occur.MUST);
        subbq.add(new TermRangeQuery("upperY", NumericUtils.doubleToPrefixCoded(y_bbox_upper), null, true, true), Occur.MUST);
        yBool.add(subbq, Occur.SHOULD);

        // x_bbox_lower <= upperX <= x_bbox_upper
        yBool.add(new TermRangeQuery("upperY", NumericUtils.doubleToPrefixCoded(y_bbox_lower), NumericUtils.doubleToPrefixCoded(y_bbox_upper), true, true), Occur.SHOULD);

        BooleanQuery yFixBool = new BooleanQuery();
        yFixBool.add(new TermRangeQuery(xName, NumericUtils.doubleToPrefixCoded(y_bbox_lower), NumericUtils.doubleToPrefixCoded(y_bbox_upper), true, true), Occur.MUST);
        yFixBool.add(yBool, Occur.SHOULD);

        return yFixBool;

    }
/*
    public HashMap<String, String> getGeoFieldInformation() {
        HashMap<String, String> fieldInformation = new HashMap<>();
        List<SearchConfig.LuceneField.Property> propList = config.getLuceneField().getProperty();
        for (int i = 0; i < propList.size(); i++) {
            String field = propList.get(i).getField();
            String type = propList.get(i).getDatatype();
            fieldInformation.put(field, type);
        }
        return fieldInformation;
    }
*/
    public HashMap<String, HashMap<String, String>> getFieldInformation() {
        HashMap<String, HashMap<String, String>> fieldInformation = new HashMap<>();
        List<SearchConfig.LuceneField.Property> propList = config.getLuceneField().getProperty();
        
        for (int i = 0; i < propList.size(); i++) {
            
            String field = propList.get(i).getField();
            String type = propList.get(i).getDatatype();
            String key = propList.get(i).getKey().get(0);
            
            /*String xpath = null;
            if (type.equals("polygon")) {
                xpath = propList.get(i).getXpath().getPolygon().getExterior();
            } else if (type.equals("bbox")) {
                xpath = propList.get(i).getXpath().getBbox().getEnvelope().getSrsName();
            } else if (type.equals("point")) {
                xpath = propList.get(i).getXpath().getPoint().getLatlon().getLong();
            } else {
                xpath = propList.get(i).getXpath().getStatement().get(0);
            }*/
            HashMap<String, String> luceneField = new HashMap<>();
            luceneField.put(field, type);
            fieldInformation.put(key, luceneField);
        }
        return fieldInformation;
    }

    public Query getComparisonQuery(String operationName, String fieldName, String dataType, Object minVal, Object maxVal) {
        Query query = null;
        
        switch (operationName) {
            case "PropertyIsEqualTo":
                query = getLuceneRangeQuery(fieldName, dataType, minVal, maxVal, true, true);
                //query = new TermQuery(new Term(fieldName, (String)minVal));
                break;
            case "PropertyIsNotEqualTo":
                BooleanQuery bq = new BooleanQuery();
                bq.add(new MatchAllDocsQuery(), Occur.MUST);
                bq.add(getLuceneRangeQuery(fieldName, dataType, minVal, maxVal, true, true), Occur.MUST_NOT);
                query = bq;
                break;
            case "PropertyIsLessThan":
                query = getLuceneRangeQuery(fieldName, dataType, null, maxVal, false, false);
                break;
            case "PropertyIsLessThanOrEqualTo":
                query = getLuceneRangeQuery(fieldName, dataType, null, maxVal, true, true);
                break;
            case "PropertyIsGreaterThan":
                query = getLuceneRangeQuery(fieldName, dataType, minVal, null, false, false);
                break;
            case "PropertyIsGreaterThanOrEqualTo":
                query = getLuceneRangeQuery(fieldName, dataType, minVal, null, true, true);
                break;
            case "PropertyIsBetween":
                query = getLuceneRangeQuery(fieldName, dataType, minVal, maxVal, true, true);
                break;
            default:
            // Exception
        }
        return query;
    }

    public Query getLuceneRangeQuery(String fieldName, String dataType, Object minVal, Object maxVal, boolean lower, boolean upper) {
        Query query = null;
        if (fieldName.equals("AnyText")) {
            dataType = "string";

        }
        switch (dataType.toLowerCase()) {
            case "string":
                String minStr = (String) minVal;
                String maxStr = (String) maxVal;

                query = new TermRangeQuery(fieldName, minStr.toLowerCase(), maxStr.toLowerCase(), lower, upper);
                break;
            case "integer":
                String minInt = null;
                String maxInt = null;

                if (minVal != null) {
                    minInt = NumericUtils.intToPrefixCoded((Integer) minVal);
                }

                if (maxVal != null) {
                    minInt = NumericUtils.intToPrefixCoded((Integer) maxVal);
                }
                query = new TermRangeQuery(fieldName, minInt, maxInt, lower, upper);
                //query = NumericRangeQuery.newIntRange(fieldName, (Integer) minVal, (Integer) maxVal, lower, upper);
                break;
            case "double":

                String minDouble = null;
                String maxDouble = null;

                if (minVal != null) {
                    minDouble = NumericUtils.doubleToPrefixCoded(new Double((String) minVal));
                }

                if (maxVal != null) {
                    maxDouble = NumericUtils.doubleToPrefixCoded(new Double((String) maxVal));
                }
                query = new TermRangeQuery(fieldName, minDouble, maxDouble, lower, upper);
                //query = NumericRangeQuery.newDoubleRange(fieldName, (Double) minVal, (Double) maxVal, lower, upper);
                break;
            case "float":
                String minFloat = null;
                String maxFloat = null;

                if (minVal != null) {
                    minFloat = NumericUtils.floatToPrefixCoded((Float) minVal);
                }

                if (maxVal != null) {
                    maxFloat = NumericUtils.floatToPrefixCoded((Float) maxVal);
                }
                query = new TermRangeQuery(fieldName, minFloat, maxFloat, lower, upper);
                //query = NumericRangeQuery.newFloatRange(fieldName, (Float) minVal, (Float) maxVal, lower, upper);
                break;
            case "long":
                String minLong = null;
                String maxLong = null;

                if (minVal != null) {
                    minLong = NumericUtils.longToPrefixCoded((Long) minVal);
                }

                if (maxVal != null) {
                    maxLong = NumericUtils.longToPrefixCoded((Long) maxVal);
                }
                query = new TermRangeQuery(fieldName, minLong, maxLong, lower, upper);
                //query = NumericRangeQuery.newLongRange(fieldName, (Long) minVal, (Long) maxVal, lower, upper);
                break;
            case "datetime":

                String minDate = null;
                String maxDate = null;

                if (minVal != null) {
                    String minValStr = (String) minVal;
                    DateTime minDateTime = new DateTime(minValStr);
                    minDate = DateTools.dateToString(minDateTime.toDate(), DateTools.Resolution.MILLISECOND);
                }

                if (maxVal != null) {
                    String maxValStr = (String) maxVal;
                    DateTime maxDateTime = new DateTime(maxValStr);
                    maxDate = DateTools.dateToString(maxDateTime.toDate(), DateTools.Resolution.MILLISECOND);
                }
                query = new TermRangeQuery(fieldName, minDate, maxDate, lower, upper);
                break;
            default:
            //exception
        }

        return query;
    }

    public FilteredQuery getBBOXQuery(Query bbox, Geometry searchGeometry, String operationName, String field) {
        System.out.println(field);
        
        GeometryAccessor accessor = new GeometryAccessor(searchGeometry, operationName, field, "BinarySpatialOpsType");

        org.apache.lucene.search.Filter filter = new GeometryFilter(accessor);
        FilteredQuery fq = new FilteredQuery(bbox, filter);
        return fq;
    }

    public FilteredQuery getSpatialFilteredQuery(Geometry searchGeometry, String operationName, String field) {
        System.out.println(field);
        GeometryAccessor accessor = new GeometryAccessor(searchGeometry, operationName, field, "BinarySpatialOpsType");

        org.apache.lucene.search.Filter filter = new GeometryFilter(accessor);
        FilteredQuery fq = new FilteredQuery(new MatchAllDocsQuery(), filter);
        return fq;
    }

    public FilteredQuery getSpatialDistanceFilteredQuery(Geometry searchGeometry, String operationName, String field, String distance, String unit) {

        GeometryAccessor accessor = new GeometryAccessor(searchGeometry, operationName, field, distance, unit, "DistanceBufferType");

        org.apache.lucene.search.Filter filter = new GeometryFilter(accessor);
        FilteredQuery fq = new FilteredQuery(new MatchAllDocsQuery(), filter);
        return fq;
    }
}
