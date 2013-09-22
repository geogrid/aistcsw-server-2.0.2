/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dbgrid.csw.sort;

import java.util.Comparator;
import java.util.Map;
import org.joda.time.DateTime;

/**
 *
 * @author kimoto
 */
public class DateComparatorAsc implements Comparator {

    @Override
    public int compare(Object o1, Object o2) {
        Map.Entry e1 = (Map.Entry) o1;
        Map.Entry e2 = (Map.Entry) o2;
        DateTime d1 = new DateTime((String) e1.getValue());
        DateTime d2 = new DateTime((String) e2.getValue());

        return d1.compareTo(d2);
    }
}
