/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.filter;

import org.dbgrid.csw.transaction.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.opengis.ogc.FilterType;
import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author kimoto
 */
public class GetSrsName {

    final String getEnvelopeSrsName = "//gml:Envelope/@srsName";
    final String getPolygonSrsName = "";
    
    public String getSrsName(FilterType filter, Marshaller marshaller) {
        String srsName = null;
        ResourceBundle rb = ResourceBundle.getBundle("prefix");


        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            marshaller.marshal(filter, bos);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder builder = dbFactory.newDocumentBuilder();

            Document doc = builder.parse(bis);
            JXPathContext context = JXPathContext.newContext(doc);

            Enumeration keys = rb.getKeys();

            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                context.registerNamespace(key, rb.getString(key));
            }
            
            Iterator iter = context.iterate(getEnvelopeSrsName);
            while(iter.hasNext()){
                srsName = (String)iter.next();
            }
            
        } catch (JAXBException | ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SaveData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return srsName;
    }
    
}
