/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dbgrid.csw;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import java.util.Enumeration;
import java.util.ResourceBundle;



/**
 *
 * @author kimoto
 */
public class Prefix extends NamespacePrefixMapper{
    @Override
    public String getPreferredPrefix(String namespaceUri,String suggestion,boolean requirePrefix){
        ResourceBundle resource = ResourceBundle.getBundle("prefix");
        Enumeration<String> list = resource.getKeys();
        while(list.hasMoreElements()){
            String uri = list.nextElement();
            if(namespaceUri.equals(resource.getString(uri))){
                return uri.substring("uri.".length());
            }
        }
        
        return null;
    }
    
}
