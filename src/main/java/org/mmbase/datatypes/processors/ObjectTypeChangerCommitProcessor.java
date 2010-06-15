/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.datatypes.processors;

import org.mmbase.bridge.*;
import org.mmbase.util.logging.*;

/**
 *
 * It also implements simply copying the value of another field in such cases (using the
 * 'fieldName') parameter.
 *
 * @author Michiel Meeuwissen
 * @version $Id$
 * @since MMBase-1.9.1
 */

public class ObjectTypeChangerCommitProcessor implements CommitProcessor {

    private static final Logger log = Logging.getLoggerInstance(ObjectTypeChangerCommitProcessor.class);
    private static final long serialVersionUID = 1L;

    @Override
    public void commit(Node node, Field field) {
        if (! node.isNew()) {
            String bul = (String) node.getValue(field.getName());
            if (bul != null && ! node.getNodeManager().getName().equals(bul)) {
                log.service("Changing type of " + node + " to " + bul);
                node.setNodeManager(node.getCloud().getNodeManager(bul));
            }
        }
    }

    public static class Getter implements Processor {
        private static final long serialVersionUID = 1L;
        @Override
        public Object process(Node node, Field field, Object value) {
            String bul = (String) node.getValueWithoutProcess(field.getName());
            if (bul != null) {
                return bul;
            }  else {
                return node.getNodeManager().getName();
            }
        }
    }

}


