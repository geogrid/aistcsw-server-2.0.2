/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.sort;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author kimoto
 */
public class IntegerComparatorDesc implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Map.Entry e1 = (Map.Entry) o1;
        Map.Entry e2 = (Map.Entry) o2;
        Integer d1 = (Integer) e1.getValue();
        Integer d2 = (Integer) e2.getValue();

        return d2.compareTo(d1);
    }
}
