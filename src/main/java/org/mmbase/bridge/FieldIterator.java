/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge;
import java.util.ListIterator;

/**
 * An iterator with {@link #nextField} and {#link previousField} methods. Note that since java 1.5/MMBase
 * 1.9 the methods {@link #next} and {@link #previous} return {@link Field}s too because this
 * Iterator now implements <code>ListIterator&lt;Field&gt;</code>.
 *
 * @author Pierre van Rooden
 * @version $Id$
 */
public interface FieldIterator extends ListIterator<Field> {

    /**
     * Returns the next element in the iterator as a Field
     * @return next Field
     */
    Field nextField();

    /**
     * Returns the previous element in the iterator as a Field
     * @return previous Field
     */
    Field previousField();

}
