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
public class DoubleComparatorAsc implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Map.Entry e1 = (Map.Entry) o1;
        Map.Entry e2 = (Map.Entry) o2;
        Double d1 = new Double((String) e1.getValue());
        Double d2 = new Double((String) e2.getValue());

        return d1.compareTo(d2);
    }
}
