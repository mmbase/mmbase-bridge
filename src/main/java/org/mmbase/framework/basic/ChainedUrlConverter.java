/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.framework.basic;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.mmbase.bridge.*;
import org.mmbase.framework.*;
import org.mmbase.util.functions.Parameters;
import org.mmbase.util.functions.Parameter;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**
 * Keeps track of several UrlConverters and chains them one after another.
 * If the outcome of an UrlConverter is not <code>null</code> its result is returned. The
 * question remains whether we want UrlConverters to be realy chained so that the
 * outcome of a converter can be added to the outcome of its preceder.
 *
 * @author Michiel Meeuwissen
 * @author Andr&eacute; van Toly
 * @version $Id$
 * @since MMBase-1.9
 */
public class ChainedUrlConverter implements UrlConverter {
    private static final long serialVersionUID = 0L;
    private static final Logger log = Logging.getLoggerInstance(ChainedUrlConverter.class);


    public static Parameter<Class> URLCONVERTER_PARAM = new Parameter<Class>("urlconverter", new org.mmbase.datatypes.BasicDataType<Class>("class", Class.class) {
        @Override
            protected Class cast(Object value, Cloud cloud, Node node, Field field) throws org.mmbase.datatypes.CastException {
                try {
                    Object preCast = preCast(value, cloud, node, field);
                    if (preCast == null) return null;
                    Class cast = org.mmbase.util.Casting.toType(Class.class, cloud, preCast);
                    return cast;
                } catch (IllegalArgumentException iae) {
                    log.info(iae.getMessage());
                    return null;
                }
            }
        });

    public static String URLCONVERTER = "org.mmbase.urlconverter";

    /**
     * List containing the UrlConverters found in the framework configuration.
     */
    private final List<UrlConverter> uclist = new ArrayList<UrlConverter>();
    private final List<Parameter<?>>   parameterDefinition = new ArrayList<Parameter<?>>();
    {
        parameterDefinition.add(URLCONVERTER_PARAM);
    }

    /**
     * Adds the UrlConverters to the list.
     */
    public void add(UrlConverter u) {
        uclist.add(u);
        for (Parameter<?> p : u.getParameterDefinition()) {
            if (! parameterDefinition.contains(p)) {
                parameterDefinition.add(p);
            }
        }
    }

    public boolean contains(UrlConverter u) {
        return uclist.contains(u);
    }

    @Override
    public Parameter<?>[] getParameterDefinition() {
        return parameterDefinition.toArray(new Parameter[parameterDefinition.size()]);
    }



//     public static class Link {
//         public final static Link NULL = new Link(null, null);
//         public final Block block;
//         public final UrlConverter converter;
//         public Link(UrlConverter converter, Block b) {
//             this.block = b;
//             this.converter = converter;
//         }
//         public String getUrl() {
//         }
//     }

//     public Link chain(String path, Parameters frameworkParameters) throws FrameworkException {
//         for (UrlConverter uc : uclist) {
//             Block b = uc.getBlock(path, frameworkParameters);
//             if (b != null) {
//                 return new Link(uc, b);
//             }
//         }
//         return Link.NULL;
//     }


    /**
     * The default weight of the UrlConverters. An Url proposal by an UrlConverter receives a weight
     * upon which is decided which one should resolve the request.
     */
    @Override
    public int getDefaultWeight() {
        return 0;
    }

    @Override
    public boolean isFilteredMode(Parameters frameworkParameters) throws FrameworkException {
        for (UrlConverter uc : uclist) {
            if (uc.isFilteredMode(frameworkParameters)) return true;
        }
        return false;
    }

    /**
     * Upon examining the user request an 'nice' URL is proposed by an UrlConverter to resolve the request.
     * The proposed url receives a weight.
     */
    protected Url getProposal(Url u, Parameters frameworkParameters) {
        HttpServletRequest request = BasicUrlConverter.getUserRequest(frameworkParameters.get(Parameter.REQUEST));
        UrlConverter current  = (UrlConverter) request.getAttribute(URLCONVERTER);
        Class<?> preferred    = frameworkParameters.get(URLCONVERTER_PARAM);
        Url b = u;
        if (preferred != null && ! preferred.isInstance(u.getUrlConverter())) {
            int q = b.getWeight();
            b = new BasicUrl(b, Math.min(q, q - 10000));
        }
        if (current != null && u.getUrlConverter() != current) {
            int q = b.getWeight();
            b = new BasicUrl(b, Math.min(q, q - 10000));
        }
        return b;
    }

    /**
     * The URL to be printed in a page, the 'nice' url. This method requests an url proposal from
     * {@link #getProposal} and decides upon their weight which one prevails.
     */
    @Override
    public Url getUrl(String path,
                      Map<String, ?> params,
                      Parameters frameworkParameters, boolean escapeAmps) throws FrameworkException {
        Url result = Url.NOT;
        Parameters fwParams = null;
        if (log.isDebugEnabled()) {
            log.debug("Producing " + path + " " + params + " " + frameworkParameters);
        }
        for (UrlConverter uc : uclist) {
            try {
                Parameters clone = new Parameters(frameworkParameters);
                Url proposal = getProposal(uc.getUrl(path, params, clone, escapeAmps), clone);
                if (proposal.getWeight() > result.getWeight()) {
                    result = proposal;
                    fwParams = clone;
                }
            } catch (UnsupportedOperationException uoe) {
                log.debug(uoe);
            }
        }
        if (result == Url.NOT) {
            log.debug("Could not produce URL for " + path + " " + params + " " + frameworkParameters);
        }
        if (fwParams != null) {
            frameworkParameters.setAll(fwParams);
        }
        return result;
    }


    /**
     * Basically the same as {@link #getUrl} but for a Processor url.
     */
    @Override
    public Url getProcessUrl(String path,
                                Map<String, ?> params,
                                Parameters frameworkParameters, boolean escapeAmps) throws FrameworkException {
        Url result = Url.NOT;
        Parameters fwParams = null;
        if (log.isDebugEnabled()) {
            log.debug("Producing process url " + path + " " + params + " " + frameworkParameters);
        }
        for (UrlConverter uc : uclist) {
            Parameters clone = new Parameters(frameworkParameters);
            Url proposal = getProposal(uc.getProcessUrl(path, params, clone, escapeAmps), clone);
            if (proposal.getWeight() > result.getWeight()) {
                result = proposal;
                fwParams = clone;
            }
        }
        if (fwParams != null) {
            frameworkParameters.setAll(fwParams);
        }
        return result;
    }


    /**
     * The 'technical' url. The 'nice' urls received by FrameworkFilter resolve to these. This method
     * decides upon their weight which of the proposed technical url's by the UrlConverters matches
     * the 'nice' url.
     */
    @Override
    public Url getInternalUrl(String path,
                              Map<String, ?> params,
                              Parameters frameworkParameters) throws FrameworkException {
        Url result = Url.NOT;
        Parameters fwParams = null;
        for (UrlConverter uc : uclist) {
            Parameters clone = new Parameters(frameworkParameters);
            try {
                Url proposal = getProposal(uc.getInternalUrl(path, params, clone), clone);
                if (proposal.getWeight() > result.getWeight()) {
                    result = proposal;
                    fwParams = clone;

                }
            } catch (NotFoundException nfe) {
                throw nfe;
            } catch (RuntimeException re) {
                log.warn(re.getMessage(), re);
            }

        }
        if (fwParams != null) {
            frameworkParameters.setAll(fwParams);
        }
        return result;
    }

    @Override
    public String toString() {
        return "ChainedUrlConverter" + uclist;
    }

}
