/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.datatypes;

/**
 * A LengthDataType is a datatype that defines a length for its values ({@link #getLength(Object)}) ,
 * and restrictions on that (minimal an maximal length). Sometimes you may think 'size' in stead of
 * length, but we think that there is is not much difference between those...
 *
 * @author Pierre van Rooden
 * @author Michiel Meeuwissen
 * @version $Id$
 * @since MMBase-1.8
 */
public interface LengthDataType<E> extends DataType<E> {


    /**
     * In this method should be implemented how to calculate such a length for a certain value of
     * this datatype. There does not exist a generic interface for this, so the implementation
     * will cast to the expected type (String, byte[]..)
     */
    long getLength(Object value);

    /**
     * Returns the minimum length of values for this datatype.
     * @return the minimum length as an <code>int</code>, or 0 if there is no minimum length.
     */
    long getMinLength();

    /**
     * Returns the 'minLength' restriction, containing the value, errormessages, and fixed status of this attribute.
     * @return the restriction as a {@link DataType.Restriction}
     */
    DataType.Restriction<Long> getMinLengthRestriction();

    /**
     * Sets the minimum length of binary values for this datatype.
     * @param value the minimum length as an <code>long</code>, or 0 if there is no minimum length.
     * @throws Class Identifier: java.lang.UnsupportedOperationException if this datatype is finished
     */
    void setMinLength(long value);

    /**
     * Returns the maximum length of values for this datatype.
     * @return the maximum length as an <code>long</code>, or a very very big value
     * (<code>Long.MAX_VALUE</code>) if there is no maximum length.
     */
    long getMaxLength();

    /**
     * Returns the 'maxLength' restriction, containing the value, errormessages, and fixed status of this attribute.
     * @return the restriction as a {@link DataType.Restriction}
     */
    DataType.Restriction<Long> getMaxLengthRestriction();

    /**
     * Sets the maximum length of values for this datatype.
     * @param value see {@link #getMaxLength}
     * @throws Class Identifier: java.lang.UnsupportedOperationException if this datatype is finished
     */
    void setMaxLength(long value);

}
