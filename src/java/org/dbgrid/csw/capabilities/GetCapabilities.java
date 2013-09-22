/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.capabilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;
import net.opengis.cat.csw._2_0.CapabilitiesType;
import net.opengis.ows.AddressType;
import net.opengis.ows.CodeType;
import net.opengis.ows.ContactType;
import net.opengis.ows.DCP;
import net.opengis.ows.DomainType;
import net.opengis.ows.HTTP;
import net.opengis.ows.KeywordsType;
import net.opengis.ows.ObjectFactory;
import net.opengis.ows.OnlineResourceType;
import net.opengis.ows.Operation;
import net.opengis.ows.OperationsMetadata;
import net.opengis.ows.RequestMethodType;
import net.opengis.ows.ResponsiblePartySubsetType;
import net.opengis.ows.ServiceIdentification;
import net.opengis.ows.ServiceProvider;
import net.opengis.ows.TelephoneType;

/**
 *
 * @author kimoto
 */
public class GetCapabilities {

    public CapabilitiesType createGetCapabilitiesResponse() {
        final String separateStr = ",";
        
        CapabilitiesType capabilities = new CapabilitiesType();

        ResourceBundle resource = ResourceBundle.getBundle("csw");
        Properties definitionProperties = new Properties();
        try {
            definitionProperties.load(new FileInputStream(resource.getString("csw.capabilities.definition.file")));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        capabilities.setVersion(definitionProperties.getProperty("csw.capabilities.version"));
        capabilities.setUpdateSequence(definitionProperties.getProperty("csw.capabilities.updateSequence"));

        ServiceIdentification serviceIdentification = new ServiceIdentification();
        CodeType code = new CodeType();
        code.setValue(definitionProperties.getProperty("csw.capabilities.ServiceIdentification.ServiceType"));
        serviceIdentification.setServiceType(code);

        serviceIdentification.getServiceTypeVersion().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.ServiceIdentification.ServiceTypeVersion").split(separateStr)));
        
        serviceIdentification.setTitle(definitionProperties.getProperty("csw.capabilities.ServiceIdentification.Title"));
        serviceIdentification.setAbstract(definitionProperties.getProperty("csw.capabilities.ServiceIdentification.Abstract"));
        
        KeywordsType keywordTypes = new KeywordsType();
        keywordTypes.getKeyword().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.ServiceIdentification.Keyword").split(separateStr)));
        
        serviceIdentification.getKeywords().addAll(Arrays.asList(keywordTypes));
        serviceIdentification.setFees(definitionProperties.getProperty("csw.capabilities.ServiceIdentification.Fees"));
        
        capabilities.setServiceIdentification(serviceIdentification);
        
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProviderName(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ProviderName"));
        
        OnlineResourceType provoderSite = new  OnlineResourceType();
        provoderSite.setHref(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ProviderSite.href"));
        
        ResponsiblePartySubsetType serviceContact = new ResponsiblePartySubsetType();
        
        serviceContact.setIndividualName(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ServiceContact.IndividualName"));
        serviceContact.setPositionName(definitionProperties.getProperty("csw.capabilities.ServiceProvider.PositionName"));
        ContactType contact = new ContactType();
        
        TelephoneType telephone = new TelephoneType();
        telephone.getVoice().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ContactInfo.Phone.Voice").split(separateStr)));
        telephone.getFacsimile().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ContactInfo.Phone.Facsimile").split(separateStr)));
        contact.setPhone(telephone);
        
        AddressType address = new AddressType();
        address.getDeliveryPoint().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ContactInfo.Address.DeliveryPoint").split(separateStr)));
        address.setCity(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ContactInfo.Address.City"));
        address.setAdministrativeArea(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ContactInfo.Address.AdministrativeArea"));
        address.setPostalCode(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ContactInfo.Address.PostalCode"));
        address.setCountry(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ContactInfo.Address.Country"));
        address.getElectronicMailAddress().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ContactInfo.Address.ElectronicMailAddress").split(separateStr)));
        contact.setAddress(address);
        
        OnlineResourceType onlineSite = new  OnlineResourceType();
        
        onlineSite.setHref(definitionProperties.getProperty("csw.capabilities.ServiceProvider.ProviderSite.href"));
        
        contact.setOnlineResource(onlineSite);
        
        serviceContact.setContactInfo(contact);
        serviceProvider.setServiceContact(serviceContact);
        serviceProvider.setProviderSite(provoderSite);
        
        OperationsMetadata operationMetadata = new OperationsMetadata();
        
        Operation getCapabilitiesOperation = new Operation();
        DCP dcp = new DCP();
        HTTP http = new HTTP();
        
        String getHref = definitionProperties.getProperty("csw.capabilities.OperationMetadata.DCP.HTTP").split(separateStr)[0];
        String postHref = definitionProperties.getProperty("csw.capabilities.OperationMetadata.DCP.HTTP").split(separateStr)[1];
        ObjectFactory factory = new ObjectFactory();
        RequestMethodType getMethod = new RequestMethodType();
        getMethod.setHref(getHref);
        http.getGetOrPost().add(factory.createHTTPGet(getMethod));
        
        RequestMethodType postMethod = new RequestMethodType();
        postMethod.setHref(postHref);
        http.getGetOrPost().add(factory.createHTTPPost(postMethod));
        dcp.setHTTP(http);
        getCapabilitiesOperation.getDCP().add(dcp);
        
        operationMetadata.getOperation().add(getCapabilitiesOperation);
                
        Operation getRecordsOperation = new Operation();
        getRecordsOperation.getDCP().add(dcp);
        
        getRecordsOperation.setName("GetRecords");
        
        DomainType typeNameDomain = new DomainType();
        typeNameDomain.setName("TypeName");
        typeNameDomain.getValue().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.OperationMetadata.GetRecords.TypeName").split(separateStr)));
        getRecordsOperation.getParameter().add(typeNameDomain);
        
        DomainType outputFormatDomain = new DomainType();
        outputFormatDomain.setName("outputFormat");
        outputFormatDomain.getValue().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.OperationMetadata.GetRecords.outputFormat").split(separateStr)));
        getRecordsOperation.getParameter().add(outputFormatDomain);
        
        DomainType outputSchemaDomain = new DomainType();
        outputSchemaDomain.setName("outputSchema");
        outputSchemaDomain.getValue().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.OperationMetadata.GetRecords.outputSchema").split(separateStr)));
        getRecordsOperation.getParameter().add(outputSchemaDomain);
        
        DomainType resultTypeDomain = new DomainType();
        resultTypeDomain.setName("resultType");
        resultTypeDomain.getValue().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.OperationMetadata.GetRecords.resultType").split(separateStr)));
        getRecordsOperation.getParameter().add(resultTypeDomain);
        
        DomainType elementSetNameDomain = new DomainType();
        elementSetNameDomain.setName("ElementSetName");
        elementSetNameDomain.getValue().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.OperationMetadata.GetRecords.ElementSetName").split(separateStr)));
        getRecordsOperation.getParameter().add(elementSetNameDomain);
        
        DomainType constraintLanguageDomain = new DomainType();
        constraintLanguageDomain.setName("CONSTRAINTLANGUAGE");
        constraintLanguageDomain.getValue().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.OperationMetadata.GetRecords.CONSTRAINTLANGUAGE").split(separateStr)));
        getRecordsOperation.getParameter().add(constraintLanguageDomain);
        
        operationMetadata.getOperation().add(getRecordsOperation);
        
        Operation getDomainOperation = new Operation();
        getDomainOperation.setName("GetDomain");
        
        DomainType parameterNameDomain = new DomainType();
        parameterNameDomain.setName("ParameterDomain");
        parameterNameDomain.getValue().addAll(Arrays.asList(definitionProperties.getProperty("csw.capabilities.OperationMetadata.GetDomain.ParameterName").split(separateStr)));
        getDomainOperation.getParameter().add(parameterNameDomain);
        
        operationMetadata.getOperation().add(getDomainOperation);
        
        capabilities.setOperationsMetadata(operationMetadata);
        capabilities.setServiceProvider(serviceProvider);
        return capabilities;
    }
}
