/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.storage.search.implementation;

import org.mmbase.storage.search.*;

/**
 * Basic implementation.
 *
 * @author Rob van Maris
 * @version $Id$
 * @since MMBase-1.7
 */

// this class would logically be abstract, but test-cases are instantiating it.
public class BasicConstraint implements Constraint, java.io.Serializable  {
    private static final long serialVersionUID = 1L;
    private boolean inverse = false;
    protected boolean modifiable = true;

    protected BasicConstraint() {}

    /**
     * @since MMBase-1.9.2
     */
    public void setUnmodifiable() {
        modifiable = false;
    }

    /**
     * Sets inverse.
     *
     * @return This <code>BasicConstraint</code> instance.
     * @param inverse The inverse value.
     */
    public BasicConstraint setInverse(boolean inverse) {
        if (! modifiable) throw new IllegalStateException();
        this.inverse = inverse;
        return this;
    }

    // javadoc is inherited
    public boolean isInverse() {
        return inverse;
    }

    // javadoc is inherited
    public int getBasicSupportLevel() {
        return SearchQueryHandler.SUPPORT_OPTIMAL;
    }

    // javadoc is inherited
    public boolean equals(Object obj) {
        // Must be same class (subclasses should override this)!
        if (obj != null && obj.getClass() == getClass()) {
            BasicConstraint constraint = (BasicConstraint) obj;
            return inverse == constraint.isInverse();
        } else {
            return false;
        }
    }

    // javadoc is inherited
    public int hashCode() {
        return (inverse? 0: 107);
    }
}
