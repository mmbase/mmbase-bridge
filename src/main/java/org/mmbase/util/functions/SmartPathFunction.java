/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.util.functions;

import java.util.Set;
import java.util.regex.Pattern;
import java.lang.reflect.*;
import org.mmbase.util.*;
import org.mmbase.cache.Cache;
import org.mmbase.util.logging.*;


/**
 * Returns the path to use for TREEPART, TREEFILE, LEAFPART and LEAFFILE.
 * The system searches in a provided base path for a filename that matches the supplied number/alias of
 * a node (possibly extended with a version number). See the documentation on the TREEPART SCAN command for more info.
 *
 * This class can be overridden to make an even smarter search possible.
 * Used to be {@link org.mmbase.module.core.SmartPathFunction}.
 *
 * @since MMBase-2.0
 * @version $Id$
 */
public class SmartPathFunction {
    private static final Logger log = Logging.getLoggerInstance(SmartPathFunction.class);

    protected static final Cache<String, String> smartPathCache = new Cache<String, String>(100) {
        @Override
        public String getName() {
            return "SmartPathCache";
        }
        @Override
        public String getDescription() {
            return "Caches the result of the 'smartpath' function";
        }
    };
    static {
        smartPathCache.putCache();
    }


    protected final Object parent;
    protected String nodeNumber;
    protected String version;
    protected String path;
    protected String documentRoot;
    protected boolean backwardsCompatible = true;

    protected ResourceLoader webRoot = ResourceLoader.getWebRoot();

    public SmartPathFunction(Object p) {
        parent = p;
    }

    /**
     * The number or alias of the node to filter on
     */
    public void setNodeNumber(String nm) {
        log.debug("Setting " + nodeNumber);
        if (nm != null && ! nm.equals("")) {
            nodeNumber = nm;
        }
    }

    public void setNode(org.mmbase.bridge.Node n) {
        if (nodeNumber == null || "".equals(nodeNumber)) {
            nodeNumber = "" + n.getNumber();
        }
    }
    /**
     * The version number (or <code>null</code> if not applicable) to filter on
     */
    public void setVersion(String v) {
        version = v;
    }

    /**
     * the root of the path to search.
     * @deprecated Use {@link #setLoader(ResourceLoader)}.
     */
    public void setRoot(String r) {
        documentRoot = r;
    }

    public void setLoader(ResourceLoader r) {
        webRoot = r;
    }
    public ResourceLoader getLoader() {
        return webRoot;
    }
    /**
     * The subpath of the path to search
     */
    @Required
    public void setPath(String p) {
        path = p;
    }

    public void setBackwardsCompatible(boolean b) {
        backwardsCompatible = b;
    }
    public boolean getBackwardsCompatible() {
        return backwardsCompatible;
    }

    public String smartKey() {
        return path + '.' + nodeNumber + '.' + version;
    }

    /**
     * The found path as a <code>String</code>, or <code>null</code> if not found
     */
    public final String smartpath() throws IllegalAccessException, InvocationTargetException {
        String result;
        String key = null;
        if (smartPathCache.isActive()) {
            key = smartKey();
            result = smartPathCache.get(key);
            if (result != null || smartPathCache.containsKey(key)) {
                return result;
            }
        }
        result = getSmartPath();

        if (key != null) {
            smartPathCache.put(key, result);
        }
        return result;
    }

    /**
     * The found path as a <code>String</code>, or <code>null</code> if not found
     */
    protected String getSmartPath() throws IllegalAccessException, InvocationTargetException {
        log.debug("Determining smartpath for node " + nodeNumber + " " + parent);
        if (backwardsCompatible) {
            try {
                Method m = parent.getClass().getMethod("getSmarthPath", String.class, String.class, String.class, String.class);
                return (String) m.invoke(parent, documentRoot, path, nodeNumber, version);
            } catch (NoSuchMethodException nsme) {
                log.error(nsme);
            }

        }
        log.debug("Doing NEW way");
        ResourceLoader child = webRoot.getChildResourceLoader(path);
        String node = nodeNumber;
        if (version != null) node += "\\." + version;
        Set s = child.getChildContexts(Pattern.compile(".*\\D" + node + "\\D.*"), false);
        if (s.isEmpty()) {
            return null;
        } else {
            return path + s.iterator().next() + "/";
        }
    }

}


