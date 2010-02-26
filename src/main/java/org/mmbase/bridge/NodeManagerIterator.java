/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge;

import java.util.ListIterator;

/**
 * An iterator for {@link NodeManager}s.
 * @see NodeIterator
 *
 * @author Pierre van Rooden
 * @version $Id$
 */
public interface NodeManagerIterator extends ListIterator<NodeManager> {

    /**
     * Returns the next element in the iterator as a NodeManager
     * @return next node manager
     */
    public NodeManager nextNodeManager();

    /**
     * Returns the previous element in the iterator as a NodeManager
     * @return previous node manager
     * @since MMBase-1.7
     */
    public NodeManager previousNodeManager();

}
