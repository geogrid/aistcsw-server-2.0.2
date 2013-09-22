/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.client;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import net.opengis.cat.csw._2_0.DistributedSearchType;
import net.opengis.cat.csw._2_0.ElementSetNameType;
import net.opengis.cat.csw._2_0.ElementSetType;
import net.opengis.cat.csw._2_0.GetRecordsType;
import net.opengis.cat.csw._2_0.ObjectFactory;
import net.opengis.cat.csw._2_0.QueryConstraintType;
import net.opengis.cat.csw._2_0.QueryType;
import net.opengis.cat.csw._2_0.ResultType;
import net.opengis.ogc.FilterType;
import net.opengis.ogc.PropertyNameType;
import net.opengis.ogc.SortByType;
import net.opengis.ogc.SortOrderType;
import net.opengis.ogc.SortPropertyType;

/**
 *
 * @author kimoto
 */
public class GetRecords {

    private String resultType;
    private String uri;
    private String outputFormat;
    private String outputSchema;
    private Long startPosition;
    private Long maxRecords;
    private String cql_text;
    private String filterString;
    private String filterVersion;
    private String elementName;
    private String sortBy;
    private Long hopCount;
    private String typeNames;
    
    public GetRecordsType createGetRecordsObject() {
        GetRecordsType records = new GetRecordsType();

        // Fixed values of "CSW"
        records.setService("CSW");

        //Fixed value of 2.0.2 
        records.setVersion("2.0.2");

        // Zero or one (Optional)
        if (getResultType() != null) {
            switch (getResultType()) {
                case "hits":
                    records.setResultType(ResultType.HITS);
                    break;
                case "results":
                    records.setResultType(ResultType.RESULTS);
                    break;
                case "validate":
                    records.setResultType(ResultType.VALIDATE);
                    break;
                default:
                    records.setResultType(ResultType.HITS);
                    break;
            }

        }

        // Zero or one (Optional)
        if (getUri() != null) {
            records.setRequestId(getUri());
        }

        // Zero or one (Optional)
        if (getOutputFormat() == null) {
            // Default value
            records.setOutputFormat("application/xml");
        } else {
            records.setOutputFormat(getOutputFormat());
        }

        // Zero or one (Optional)
        if (getOutputSchema() == null) {
            // Default value
            records.setOutputSchema("http://www.opengis.net/cat/csw/2.0.2");
        } else {
            records.setOutputSchema(getOutputSchema());
        }

        if (getStartPosition() == null) {
            records.setStartPosition(BigInteger.ONE);
        } else {
            records.setStartPosition(BigInteger.valueOf(getStartPosition()));
        }

        if (getMaxRecords() == null) {
            records.setMaxRecords(BigInteger.TEN);
        } else {
            records.setMaxRecords(BigInteger.valueOf(getMaxRecords()));
        }

        QueryType query = new QueryType();
        
        String[] typeNamesList = getTypeNames().split(",");
        for(int i = 0; i < typeNamesList.length; i++){
            query.getTypeNames().add(new QName(typeNamesList[i]));
        }
        
        QueryConstraintType constraint = new QueryConstraintType();
        
        if (getCql_text() != null) {
            constraint.setCqlText(getCql_text());
        }
        
        
        if (getFilterString() != null) {
            System.out.println(getFilterString());
            
            constraint.setVersion(getFilterVersion());
            Unmarshaller unmarshaller = CSWRequest.createUnmarshaller();
            try {
                JAXBElement element = (JAXBElement)unmarshaller.unmarshal(new ByteArrayInputStream(getFilterString().getBytes()));
                
                constraint.setFilter((FilterType)element.getValue());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        query.setConstraint(constraint);

        System.out.println(getElementName());
        
        ElementSetNameType elementSetName = new ElementSetNameType();
        elementSetName.getTypeNames().add(new QName("rim:RegistryPackage"));
        if (getElementName() == null) {
            setElementName("summary");
        }
        
        switch (getElementName()) {
            case "full":
                elementSetName.setValue(ElementSetType.FULL);
                break;
            case "summary":
                elementSetName.setValue(ElementSetType.SUMMARY);
                break;
            case "brief":
                elementSetName.setValue(ElementSetType.BRIEF);
                break;
            default:
                elementSetName.setValue(ElementSetType.FULL);
                break;
        }

        query.setElementSetName(elementSetName);

        if (getSortBy() != null) {
            SortByType sort = new SortByType();
            SortPropertyType sortProp = new SortPropertyType();

            String[] sortList = getSortBy().split(",");
            for (int i = 0; i < sortList.length; i++) {
                PropertyNameType propName = new PropertyNameType();
                propName.getContent().add(sortList[i].split(":")[0]);
                sortProp.setPropertyName(propName);

                switch (sortList[i].split(":")[1]) {
                    case "A":
                        sortProp.setSortOrder(SortOrderType.ASC);
                        break;
                    case "D":
                        sortProp.setSortOrder(SortOrderType.DESC);
                        break;
                    default:
                    //exception
                }
                sort.getSortProperty().add(sortProp);
            }
            query.setSortBy(sort);


        }
        

        ObjectFactory factory = new ObjectFactory();

        records.setAbstractQuery(factory.createQuery(query));

        if (getHopCount() != null) {
            DistributedSearchType distSearch = new DistributedSearchType();
            distSearch.setHopCount(BigInteger.valueOf(getHopCount()));
            records.setDistributedSearch(distSearch);
        }

        return records;

    }

    /**
     * @return the resultType
     */
    public String getResultType() {
        return resultType;
    }

    /**
     * @param resultType the resultType to set
     */
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the outputFormat
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * @param outputFormat the outputFormat to set
     */
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * @return the outputSchema
     */
    public String getOutputSchema() {
        return outputSchema;
    }

    /**
     * @param outputSchema the outputSchema to set
     */
    public void setOutputSchema(String outputSchema) {
        this.outputSchema = outputSchema;
    }

    /**
     * @return the startPosition
     */
    public Long getStartPosition() {
        return startPosition;
    }

    /**
     * @param startPosition the startPosition to set
     */
    public void setStartPosition(Long startPosition) {
        this.startPosition = startPosition;
    }

    /**
     * @return the maxRecords
     */
    public Long getMaxRecords() {
        return maxRecords;
    }

    /**
     * @param maxRecords the maxRecords to set
     */
    public void setMaxRecords(Long maxRecords) {
        this.maxRecords = maxRecords;
    }

    /**
     * @return the cql_text
     */
    public String getCql_text() {
        return cql_text;
    }

    /**
     * @param cql_text the cql_text to set
     */
    public void setCql_text(String cql_text) {
        this.cql_text = cql_text;
    }

    /**
     * @return the filterString
     */
    public String getFilterString() {
        return filterString;
    }

    /**
     * @param filterString the filterString to set
     */
    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    /**
     * @return the filterVersion
     */
    public String getFilterVersion() {
        return filterVersion;
    }

    /**
     * @param filterVersion the filterVersion to set
     */
    public void setFilterVersion(String filterVersion) {
        this.filterVersion = filterVersion;
    }

    /**
     * @return the elementName
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * @param elementName the elementName to set
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    /**
     * @return the sortBy
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * @param sortBy the sortBy to set
     */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * @return the hopCount
     */
    public Long getHopCount() {
        return hopCount;
    }

    /**
     * @param hopCount the hopCount to set
     */
    public void setHopCount(Long hopCount) {
        this.hopCount = hopCount;
    }

    /**
     * @return the typeNames
     */
    public String getTypeNames() {
        return typeNames;
    }

    /**
     * @param typeNames the typeNames to set
     */
    public void setTypeNames(String typeNames) {
        this.typeNames = typeNames;
    }
}
