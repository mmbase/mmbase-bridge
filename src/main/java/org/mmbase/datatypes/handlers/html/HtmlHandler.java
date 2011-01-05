/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.datatypes.handlers.html;

import org.mmbase.datatypes.handlers.*;
import org.mmbase.datatypes.DataType;
import org.mmbase.bridge.*;
import org.mmbase.util.transformers.Xml;
import org.mmbase.util.transformers.CharTransformer;
import org.mmbase.util.logging.*;
import org.mmbase.util.*;
import org.mmbase.util.functions.Parameter;

import java.util.*;

/**
 * This is the base handler for the mime type text/html and application/xml+xhtml.
 *
 * @author Michiel Meeuwissen
 * @version $Id$
 * @since MMBase-1.9.1
 */

public abstract class HtmlHandler  extends AbstractHandler<String> {
    private static final Logger log = Logging.getLoggerInstance(HtmlHandler.class);


    public static final Parameter<String> SESSIONNAME = new Parameter<String>(HtmlHandler.class.getName() + ".SESSIONNAME", String.class);
    protected static final CharTransformer XML = new Xml(Xml.ESCAPE);


    private boolean setIfNotChanged = false;


    public void setSetIfNotChanged(boolean b) {
        setIfNotChanged = b;
    }

    /**
     * @since MMBase-1.9.6
     */
    protected String getClassName(Class<?> c) {
        String name = c.getName();
        name = name.substring(c.getPackage().getName().length() + 1);
        if (name.endsWith("DataType")) {
            name = name.substring(0, name.length() - "DataType".length());
        }
        return name.toLowerCase();
    }

    protected void appendClasses(Request request, StringBuilder buf, Node node, Field field) {
        buf.append("mm_validate");
        DataType dt = field.getDataType();
        for (String styleClass : dt.getStyleClasses()) {
            buf.append(" ");
            buf.append(styleClass);
        }
        {
            Class<?> sup = dt.getClass();
            while(DataType.class.isAssignableFrom(sup)) {
                if (sup.equals(org.mmbase.datatypes.BasicDataType.class)) {
                    break;
                }
                buf.append(" mm_dtclass_");
                buf.append(getClassName(sup));
                sup = sup.getSuperclass();
            }
            for (Class<?> c : dt.getClass().getInterfaces()) {
                if (DataType.class.isAssignableFrom(c)) {
                    buf.append(" mm_dtclass_");
                    buf.append(getClassName(c));
                }
            }
        }
        if (field.getClass().equals(org.mmbase.bridge.util.DataTypeField.class)) {
            buf.append(" mm_dt_");
            buf.append(field.getDataType().getName());
        } else {
            buf.append(" mm_f_");
            buf.append(field.getName());
            buf.append(" mm_nm_");
            buf.append(field.getNodeManager().getName());
        }
        /*
        FieldInfoTag.DataTypeOrigin o = tag.getOrigin();
        if (o != FieldInfoTag.DataTypeOrigin.FIELD) {
            buf.append(" mm_dto_").append(o.toString().toLowerCase());
        }
        */

        buf.append(" mm_n_");
        buf.append(node == null ? -1 :node.getNumber());
        if (dt instanceof org.mmbase.datatypes.LengthDataType) {
            buf.append(" mm_length_");
            Object value = getEvaluatedFieldValue(request, node, field);
            if (value != null) {
                try {
                    buf.append(((org.mmbase.datatypes.LengthDataType) dt).getLength(value));
                } catch (Exception e) {
                    log.warn(e.getMessage());
                    buf.append(node == null ? -1 : node.getSize(field.getName()));
                }
            } else {
                buf.append(0);
            }
        }

    }

    protected void appendNameId(StringBuilder buf, Request request, Field field) {
        buf.append("name=\"").append(request.getName(field)).append("\" ");
        buf.append("id=\"").append(id(request, field)).append("\" ");
    }

    public String id(Request request, Field field) {
        return id(request.getName(field));
    }

    protected String prefixError(Request request, Field f)  {
        String name = request.getName(f);
        return "mm_check" + (name.startsWith("_") ? name : ("_" + name));
    }

    @Override
    public String check(Request request, Node node, Field field, boolean errors) {
        Object fieldValue = request.getValue(field);
        final DataType<?> dt = field.getDataType();
        if (fieldValue == null) {
            log.debug("Field value not found in context, using existing value ");
            fieldValue = getFieldValue(request, node, field, node == null);
        } else if (fieldValue.equals("") && ! field.isRequired()) {
            log.debug("Field value found in context is empty, interpreting as null");
            fieldValue = null;
        }
        if (log.isDebugEnabled()) {
            log.debug("Value for field " + field + ": " + fieldValue + " and node " + node);
        }
        Collection<LocalizedString> col = dt.castAndValidate(fieldValue, node, field);
        if (col.size() == 0) {
            // do actually set the field, because some datatypes need cross-field checking
            // also in an mm:form, you can simply commit.
            if (node != null && ! field.isReadOnly()) {
                String fieldName = field.getName();
                Object oldValue = setIfNotChanged ? /* irrelevant */ null : node.getValue(fieldName);
                if (setIfNotChanged || (fieldValue == null ? oldValue != null : ! fieldValue.equals(oldValue))) {
                    try {
                        if(log.isDebugEnabled()) {
                            log.debug("Setting " + fieldName + " to " + (fieldValue == null ? "" : fieldValue.getClass().getName()) + " " + fieldValue);
                        }
                        if ("".equals(fieldValue) && interpretEmptyAsNull(field)) {
                            setValue(request, node, fieldName,  null);
                        } else {
                            setValue(request, node, fieldName,  fieldValue);
                        }
                    } catch (Throwable t) {
                        // may throw exception like 'You cannot change the field"
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("not Setting " + fieldName + " to " + fieldValue + " because already has that value");
                    }
                }
            }
            if (errors && ! field.isReadOnly()) {
                return "<div id=\"" + prefixError(request, field) + "\" class=\"mm_check_noerror\"> </div>";
            } else {
                return "";
            }
        } else {
            if (!field.isReadOnly()) {
                request.invalidate();
            }
            if (errors && ! field.isReadOnly()) {
                StringBuilder show = new StringBuilder("<div id=\"");
                show.append(prefixError(request, field));
                show.append("\" class=\"mm_check_error\">");
                Locale locale = request.getLocale();
                for (LocalizedString error : col) {
                    show.append("<span class='" + error.getKey() + "'>");
                    Xml.XMLEscape(error.get(locale), show);
                    show.append("</span>");
                }
                show.append("</div>");
                return show.toString();
            } else {
                return "";
            }
        }
    }

}
