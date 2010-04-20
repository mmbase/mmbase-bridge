/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.framework;

import java.io.Writer;
import java.io.IOException;

import org.mmbase.util.functions.Parameters;
import org.mmbase.util.functions.Parameter;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Abstract renderer implementation which implements getType and getBlock.
 *
 * @author Michiel Meeuwissen
 * @version $Id$
 * @since MMBase-1.9
 */
abstract public class AbstractRenderer implements Renderer {

    private static final Logger log = Logging.getLoggerInstance(AbstractRenderer.class);

    protected final Type type;
    private final Block parent;

    public AbstractRenderer(Type t, Block p) {
        type = t;
        parent = p;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Block getBlock() {
        return parent;
    }

    @Override
    public java.net.URI getUri() {
        return null;
    }
    @Override
    public java.net.URI getUri(Parameters blockParameters, RenderHints hints) {
        return getUri();
    }

    /**
     * This utility method, which of course must be used in conjuction with {@link #decorateOutro}
     * can be used in concretizations of this class, for easy production of the div which is
     * requested by the contract of a ({@link Renderer.Type#BODY}) renderer for text/html.
     */
    protected void decorateIntro(RenderHints hints, Writer w, String extraClass)  throws IOException {
        w.write("<div id=\"" + hints.getId() + "\"");
        w.write(" class=\"");
        if (extraClass != null) {
            w.write(extraClass);
            w.write(' ');
        }
        w.write("mm_c c_");
        w.write(getBlock().getComponent().getName());
        w.write(" b_");
        w.write(getBlock().getName());
        w.write(" " + hints.getStyleClass());
        w.write("\">");
    }
    protected void decorateOutro(RenderHints hints, Writer w) throws IOException {
        w.write("</div>");
    }
    /**
     * @inheritDoc
     *
     * On default, a renderer has no parameters of itself
     */
    @Override
    public Parameter[] getParameters() {
        return new Parameter[] {};
    }

    @Override
    public abstract void render(Parameters blockParameters, Writer w, RenderHints hints) throws FrameworkException;

}
