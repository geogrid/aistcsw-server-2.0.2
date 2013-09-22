/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.sort;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.util.NumericUtils;
import org.dbgrid.csw.CSWService;
import org.dbgrid.csw.schema.search.SearchConfig;
import org.joda.time.DateTime;

/**
 *
 * @author kimoto
 */
public class SortUtils {

    JAXBContext context;
    ResourceBundle resource;
    Unmarshaller unmarshaller;
    Marshaller marshaller;

    public SortUtils(JAXBContext context, ResourceBundle resource, Unmarshaller unmarshaller, Marshaller marshaller) {
        this.context = context;
        this.resource = resource;
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

    public HashMap getSortMap(ArrayList<org.apache.lucene.document.Document> result, String sortStr) {
        String sortFieldType = getSortFieldType(sortStr);
        HashMap map = new HashMap();
        for (int i = 0; i < result.size(); i++) {
            String[] values = result.get(i).getValues(sortStr.split(":")[0]);
            for (int j = 0; j < values.length; j++) {
                try {
                    String value;

                    switch (sortFieldType) {
                        case "datetime":
                            value = new DateTime(DateTools.stringToDate(values[j]).getTime()).toDateTimeISO().toString();
                            break;
                        case "double":
                            value = new Double(NumericUtils.prefixCodedToDouble(values[j])).toString();
                            break;
                        case "integer":
                            value = new Integer(NumericUtils.prefixCodedToInt(values[j])).toString();
                            break;
                        case "float":
                            value = new Float(NumericUtils.prefixCodedToFloat(values[j])).toString();
                            break;
                        case "long":
                            value = new Long(NumericUtils.prefixCodedToLong(values[j])).toString();
                            break;
                        default:
                            value = values[j];
                    }
                    map.put(result.get(i), value);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        }
        return map;
    }

    public String getSortFieldType(String sortStr) {
        HashMap fieldInformation = getFieldInformation();
        HashMap luceneSortField = (HashMap) fieldInformation.get(sortStr.split(":")[0]);

        Set keySet = luceneSortField.keySet();
        Iterator iter = keySet.iterator();
        String type = null;
        while (iter.hasNext()) {
            type = (String) luceneSortField.get((String) iter.next());
        }
        return type;
    }

    public HashMap<String, HashMap<String, String>> getFieldInformation() {
        HashMap<String, HashMap<String, String>> fieldInformation = new HashMap<>();
        try {
            String configFile = resource.getString("lucene.field.definition.file");

            Unmarshaller unmarshller = context.createUnmarshaller();

            SearchConfig config = (SearchConfig) unmarshller.unmarshal(new File(configFile));

            List<SearchConfig.LuceneField.Property> propList = config.getLuceneField().getProperty();

            for (SearchConfig.LuceneField.Property property : propList) {
                String field = property.getField();
                String type = property.getDatatype();
                String key = property.getKey().get(0);

                HashMap<String, String> luceneField = new HashMap<>();
                luceneField.put(field, type);
                fieldInformation.put(key, luceneField);
            }

        } catch (JAXBException ex) {
            Logger.getLogger(CSWService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fieldInformation;
    }
}
