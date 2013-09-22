/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author kimoto
 */
public class CSWRequest {

    public static Marshaller createMarshaller() {
        Marshaller marshaller = null;
        try {
            JAXBContext context = JAXBContext.newInstance(
                    "net.opengis.cat.csw._2_0:"
                    + "net.opengis.gml:"
                    + "net.opengis.ows:"
                    + "net.opengis.ogc:"
                    + "oasis.names.tc.ebxml_regrep.xsd.rim._3:");
            marshaller = context.createMarshaller();
            try {
                marshaller.setProperty("jaxb.formatted.output", true);
                marshaller.setProperty("jaxb.encoding", Charset.defaultCharset().name());
                marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new Prefix());

            } catch (PropertyException e) {
                e.printStackTrace();
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return marshaller;

    }

    public static Unmarshaller createUnmarshaller() {
        Unmarshaller unmarshaller = null;
        try {
            JAXBContext context = JAXBContext.newInstance(
                    "net.opengis.cat.csw._2_0:"
                    + "net.opengis.gml:"
                    + "net.opengis.ogc:"
                    + "net.opengis.ows:"
                    + "oasis.names.tc.ebxml_regrep.xsd.rim._3");
            unmarshaller = context.createUnmarshaller();


        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return unmarshaller;

    }

    public String getXMLString(Object requestObject) {
        String returnXML = null;
        
        try {
            Marshaller m = createMarshaller();
            m.setProperty("jaxb.formatted.output", true);
            m.setProperty("jaxb.encoding", Charset.defaultCharset().name());
            m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new Prefix());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            m.marshal(requestObject, stream);
            
            String objectClass = requestObject.getClass().getSimpleName();
            
            returnXML = stream.toString().replaceAll(StringUtils.uncapitalize(objectClass), StringUtils.remove(objectClass, "Type"));  
            
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return returnXML;
    }
    
    public Object cswRequest(Object requestObject, String serverUrl) {
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(serverUrl);
        StringBuilder buf = new StringBuilder();
        Object returnObject = null;
        
        try {
            RequestEntity requestEntity = new StringRequestEntity(getXMLString(requestObject),  "application/x-java-serialized-object", "utf-8");
            post.setRequestEntity(requestEntity);

            client.executeMethod(post);
            try (InputStream is = post.getResponseBodyAsStream()) {
                returnObject = SerializationUtils.deserialize(is);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        post.releaseConnection();

        return returnObject;
    }
}
