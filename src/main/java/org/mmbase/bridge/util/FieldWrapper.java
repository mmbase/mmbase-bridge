/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.util;

import java.util.Locale;
import java.util.Collection;
import org.mmbase.util.LocalizedString;
import org.mmbase.bridge.*;
import org.mmbase.datatypes.DataType;


/**
 * Wraps another Field (and makes it unmodifiable). You can use this if you want to implement Field, and want to base that
 * implementation on a existing <code>Field</code> instance.
 *
 * To implement a modifiable field, you need to override the setters too.
 *
 * @author  Michiel Meeuwissen
 * @version $Id$
 * @since   MMBase-1.8.1
 */

public class FieldWrapper implements Field {
    protected final Field field;

    public FieldWrapper(Field field)  {
        this.field = field;
    }
    public NodeManager getNodeManager() {
        throw new UnsupportedOperationException();
    }

    public int getState() {
        return field.getState();
    }

    public DataType<?> getDataType() { return field.getDataType(); }
    public boolean isUnique() { return field.isUnique(); }
    public boolean hasIndex() { return field.hasIndex(); }
    public int getType() {  return field.getType(); }
    public int getListItemType() { return field.getListItemType(); }
    public int getSearchPosition() { return field.getSearchPosition(); }
    public int getListPosition() { return field.getListPosition(); }
    public int getEditPosition() { return field.getEditPosition(); }
    public int getStoragePosition() { return field.getStoragePosition(); }
    @SuppressWarnings("deprecation")
    public String getGUIType() { return field.getGUIType(); }
    public boolean isRequired() { return field.isRequired(); }
    public int getMaxLength() { return field.getMaxLength(); }
    public Collection<String> validate(Object value) { return field.validate(value); }
    public boolean isVirtual() { return field.isVirtual(); }
    public boolean isReadOnly() { return field.isReadOnly(); }
    public String getName() { return field.getName(); }
    public String getGUIName() { return field.getGUIName(); }
    public String getGUIName(Locale locale) { return field.getGUIName(locale); }
    public LocalizedString getLocalizedGUIName() { return field.getLocalizedGUIName(); }
    public void setGUIName(String g, Locale locale) { throw new UnsupportedOperationException(); }
    public void setGUIName(String g) { throw new UnsupportedOperationException(); }
    public LocalizedString getLocalizedDescription() { return field.getLocalizedDescription(); }
    public String getDescription(Locale locale) { return field.getDescription(locale); }
    public String getDescription() { return field.getDescription(); }
    public void setDescription(String description, Locale locale) { throw new UnsupportedOperationException(); }
    public void setDescription(String description) { throw new UnsupportedOperationException(); }
    public int compareTo(Field f) { return field.compareTo(f); }


    public Field getField() {
        return field;
    }

    public String toString() {
        return getClass().getName() + ":" + Fields.getStateDescription(getState()) + ":" + getStoragePosition() + ":" + getNodeManager().getName() + ":" + getName();
    }
}
