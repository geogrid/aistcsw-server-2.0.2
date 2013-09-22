/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.crawler;

import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import org.dbgrid.csw.schema.crawler.CrawlerConfig;

/**
 *
 * @author kimoto
 */
public class CrawlerNamespaceContext implements NamespaceContext{

    CrawlerConfig config;

    public CrawlerNamespaceContext(CrawlerConfig config) {
        this.config = config;
    }

    @Override
    public String getNamespaceURI(String prefix) {

        if (prefix == null) {
            throw new NullPointerException("Null prefix");

        } else {
            return getNamespace(prefix);
        }

    }

    @Override
    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }

    public String getNamespace(String prefix) {
        List<String> namespaceList = config.getNamespaceList().getNamespace();

        for (int i = 0; i < namespaceList.size(); i++) {
            String[] namespace = namespaceList.get(i).split("=");
            if (prefix.equals("") && namespace.length == 1) {
                return namespace[0];
            } else if (prefix.equals(namespace[0])) {
                return namespace[1];
            }

        }
        return null;
    }
}
