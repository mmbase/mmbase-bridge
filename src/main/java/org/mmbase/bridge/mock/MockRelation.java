/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.mock;

import java.util.*;
import org.mmbase.bridge.*;

/**
 * @author  Michiel Meeuwissen
 * @version $Id: MockNode.java 40297 2009-12-26 15:50:20Z michiel $
 * @since   MMBase-2.0
 */

public class MockRelation extends MockNode implements Relation {

    MockRelation(Map<String, Object> map, MockCloud cloud, NodeManager nm, boolean isNew) {
        super(new HashMap<String, Object>(map), cloud, nm, isNew);
    }

    @Override
    public RelationManager getRelationManager() {
        int role = getIntValue("rnumber");
        return new MockRelationManager(cloud, cloud.getCloudContext().getRole(role), "object", "object");
    }

    @Override
    public void setSource(Node n) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setDestination(Node n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getSource() {
        return getNodeValue("snumber");
    }
    @Override
    public Node getDestination() {
        return getNodeValue("dnumber");
    }
}