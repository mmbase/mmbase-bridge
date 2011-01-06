/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge;

import java.util.ListIterator;

/**
 * A list of nodes
 *
 * @author Pierre van Rooden
 * @version $Id$
 */
public interface RelationManagerIterator extends ListIterator<RelationManager> {

    /**
     * Returns the next element in the iterator as a RelationManager
     * @return next RelationManager
     */
    RelationManager nextRelationManager();

    /**
     * Returns the previous element in the iterator as a RelationManager
     * @return previous RelationManager
     * @since MMBase-1.7
     */
    RelationManager previousRelationManager();

}
