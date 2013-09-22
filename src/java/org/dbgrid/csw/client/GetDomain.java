/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.client;

import net.opengis.cat.csw._2_0.GetDomainType;

/**
 *
 * @author kimoto
 */
public class GetDomain {
    
    
    public GetDomainType createGetDomainObject(){
        GetDomainType getDomain = new GetDomainType();
        
        getDomain.setService("CSW");
        getDomain.setVersion("2.0.2");
        
        return getDomain;
        
    }
}
