/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge;

/**
 * A list of modules
 *
 * @author Pierre van Rooden
 * @version $Id$
 */
public interface ModuleList extends BridgeList<Module> {

    /**
     * Returns the Module at the indicated position in the list
     * @param index the position of the Module to retrieve
     * @return Module at the indicated position
     */
    Module getModule(int index);

    /**
     * Returns an type-specific iterator for this list.
     * @return Module iterator
     */
    ModuleIterator moduleIterator();

}
