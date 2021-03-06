//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.11.06 at 02:30:42 午後 JST 
//


package org.dbgrid.csw.schema.search;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LuceneField">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="property" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="datatype" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="field" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="analyzer" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                             &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "luceneField"
})
@XmlRootElement(name = "SearchConfig")
public class SearchConfig {

    @XmlElement(name = "LuceneField", required = true)
    protected SearchConfig.LuceneField luceneField;

    /**
     * Gets the value of the luceneField property.
     * 
     * @return
     *     possible object is
     *     {@link SearchConfig.LuceneField }
     *     
     */
    public SearchConfig.LuceneField getLuceneField() {
        return luceneField;
    }

    /**
     * Sets the value of the luceneField property.
     * 
     * @param value
     *     allowed object is
     *     {@link SearchConfig.LuceneField }
     *     
     */
    public void setLuceneField(SearchConfig.LuceneField value) {
        this.luceneField = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="property" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="datatype" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="field" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="analyzer" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *                   &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "property"
    })
    public static class LuceneField {

        @XmlElement(required = true)
        protected List<SearchConfig.LuceneField.Property> property;

        /**
         * Gets the value of the property property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the property property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getProperty().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link SearchConfig.LuceneField.Property }
         * 
         * 
         */
        public List<SearchConfig.LuceneField.Property> getProperty() {
            if (property == null) {
                property = new ArrayList<SearchConfig.LuceneField.Property>();
            }
            return this.property;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="datatype" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="field" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="analyzer" type="{http://www.w3.org/2001/XMLSchema}string"/>
         *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "datatype",
            "field",
            "analyzer",
            "key"
        })
        public static class Property {

            @XmlElement(required = true)
            protected String datatype;
            @XmlElement(required = true)
            protected String field;
            @XmlElement(required = true)
            protected String analyzer;
            @XmlElement(required = true)
            protected List<String> key;

            /**
             * Gets the value of the datatype property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getDatatype() {
                return datatype;
            }

            /**
             * Sets the value of the datatype property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setDatatype(String value) {
                this.datatype = value;
            }

            /**
             * Gets the value of the field property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getField() {
                return field;
            }

            /**
             * Sets the value of the field property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setField(String value) {
                this.field = value;
            }

            /**
             * Gets the value of the analyzer property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getAnalyzer() {
                return analyzer;
            }

            /**
             * Sets the value of the analyzer property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setAnalyzer(String value) {
                this.analyzer = value;
            }

            /**
             * Gets the value of the key property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the key property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getKey().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             * 
             * 
             */
            public List<String> getKey() {
                if (key == null) {
                    key = new ArrayList<String>();
                }
                return this.key;
            }

        }

    }

}
