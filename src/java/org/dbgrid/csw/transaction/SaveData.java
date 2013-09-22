/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.opengis.cat.csw._2_0.InsertType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author kimoto
 */
public class SaveData {

    String saveFileName;
    String instrumentShortName;
    
    public String getSaveFileName(){
        return saveFileName;
    }
    
    public String getInstrumentShortName(){
        return instrumentShortName;
    }
    
    public void saveData(InsertType insert, Marshaller marshaller) {
        
        ResourceBundle csw = ResourceBundle.getBundle("csw");
        ResourceBundle aist = ResourceBundle.getBundle("aist");
        ResourceBundle prefix = ResourceBundle.getBundle("prefix");
        
        String rootDir = aist.getString("transaction.save.dir.root");
        
        List<Object> insertDataList = insert.getAny();
        for (Object insertData : insertDataList) {
            JAXBElement element = (JAXBElement) insertData;
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                marshaller.marshal(element, bos);

                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setNamespaceAware(true);
                DocumentBuilder builder = dbFactory.newDocumentBuilder();

                Document doc = builder.parse(bis);
                JXPathContext context = JXPathContext.newContext(doc);

                Enumeration keys = prefix.getKeys();

                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    context.registerNamespace(key, prefix.getString(key));
                }

//                instrumentShortName = (String) context.getValue(aist.getString("instrumentShortNameXpath"));
//                String saveDirName = (String) context.getValue( aist.getString("save.directory.name"));
                String saveDirName = aist.getString("save.directory.name");
//                String identifier = (String) context.getValue( aist.getString(instrumentShortName +".identifierXpath"));
                String identifier = (String) context.getValue( aist.getString("save.data.identifier.xpath"));
                
//                DateTime acquisitionDate = new DateTime((String) context.getValue(acquisitionDateXpath),DateTimeZone.forID(csw.getString("csw.timezone")));
//                DateTime acquisitionDate = new DateTime((String) context.getValue(aist.getString(instrumentShortName + ".dateXpath")),DateTimeZone.forID(csw.getString("csw.timezone")));
                DateTime acquisitionDate = new DateTime((String) context.getValue(aist.getString("save.data.date.xpath")),DateTimeZone.forID(csw.getString("csw.timezone")));
                
                //File saveDir = new File(rootDir + instrumentShortName + File.separator + acquisitionDate.toString("yyyy") + File.separator + acquisitionDate.toString("MMdd"));
                File saveDir = new File(rootDir + saveDirName + File.separator + acquisitionDate.toString("yyyy") + File.separator + acquisitionDate.toString("MMdd"));
                
                if(!saveDir.exists()){
                    FileUtils.forceMkdir(saveDir);
                }
                
                saveFileName = saveDir + File.separator + URLEncoder.encode(identifier + ".xml", "UTF-8");
                File saveFile = new File(saveFileName);
                
                if(saveFile.exists()){
                    FileUtils.forceDelete(saveFile);
                }
                
                marshaller.marshal(insertData, new FileOutputStream(saveFileName));
                
            } catch (JAXBException | ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(SaveData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
}
