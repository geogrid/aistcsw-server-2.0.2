/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw;

import java.util.ResourceBundle;
import net.opengis.ows.ExceptionReport;
import net.opengis.ows.ExceptionType;

/**
 *
 * @author kimoto
 */
public class CSWExceptionReport {
    public ExceptionReport createErrorReport(String errorCode){
        ResourceBundle resource = ResourceBundle.getBundle("error");
        String[] errorMessage = resource.getString(errorCode).split(":");
        ExceptionReport report = new ExceptionReport();
        ExceptionType exception = new ExceptionType();
        exception.setExceptionCode(errorMessage[0]);
        exception.setLocator(errorMessage[1]);
        exception.getExceptionText().add(errorMessage[2]);
        report.getException().add(exception);
        return report;
    }
}
