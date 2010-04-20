/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.datatypes;

import java.util.*;
import org.mmbase.datatypes.util.xml.DataTypeDefinition;
import org.mmbase.util.logging.*;
import java.util.concurrent.*;

/**
 * A DataTypeCollector is a collection of named DataTypes. So, you can add and request DataType
 * objects from this by a String. It also facilitates 'chaining' because you can add other
 * DataTypeCollectors to it. It will delegate searching of a datatype to them, if a certain key is
 * not available.
 * <br />
 * This object also knowns how to 'lock' its DataType's using it's 'signature'. I have no idea where
 * that is good for.
 *
 * @author Pierre van Rooden
 * @since  MMBase-1.8
 * @version $Id$
 */

public final class DataTypeCollector {

    private static final Logger log = Logging.getLoggerInstance(DataTypeCollector.class);

    // Map of datatypes local to this collector
    private Map<String,BasicDataType<?>> dataTypes = new ConcurrentHashMap<String,BasicDataType<?>>(); // String -> BasicDataType
    private Map<String,Set<DataType<?>>> specializations = new ConcurrentHashMap<String,Set<DataType<?>>>(); // String -> Set
    private Map<DataType<?>, Object> roots = new ConcurrentHashMap<DataType<?>, Object>(); // All datatypes which did't inherit from another datatype (this should normally be (a subset of) the 'database types' of mmbase)

    // the object to finish datatypes with
    private Object signature = null;

    // dependent collectors
    private List<DataTypeCollector> collectors = new CopyOnWriteArrayList<DataTypeCollector>();

    // the DataTypeCollector used to store datatypes accessible throughout the application
    static DataTypeCollector systemDataTypeCollector;

    /**
     * Creates the DataTypeCollector used to store datatypes accessible throughout the application.
     * Called by the {@link DataTypes} class.
     */
    static DataTypeCollector createSystemDataTypeCollector() {
        if (systemDataTypeCollector == null) {
            Object signature = new String( "SYSTEM_" + System.currentTimeMillis());
            systemDataTypeCollector = new DataTypeCollector(signature);
            return systemDataTypeCollector;
        } else {
            throw new SecurityException("System datatype collector already defined, may not be created twice.");
        }
    }

    /**
     * @param signature the object used to finish a data type for this collector.
     */
    public DataTypeCollector(Object signature) {
         this.signature = signature;
    }

    public DataTypeDefinition getDataTypeDefinition() {
        return new DataTypeDefinition(this);
    }

    /**
     * Adds a datatype collector on which this collector depends.
     * when trying to obtain a datatype or datatype instance, if
     * the current collector does not contain the datatype, it tries to obtain
     * it from any colelctors it depends on.
     * @param collector the dataType collector to add
     */
     public void addCollector(DataTypeCollector collector) {
         collectors.add(collector);
     }

    /**
     * Set local datatypes of the collector
     * @param dataTypes a <code>Map</code> containing the datatypes
     */
    public void setDataTypes(Map<String,BasicDataType<?>> dataTypes) {
        this.dataTypes = dataTypes;
        if (log.isDebugEnabled()) log.debug("DataTypes for collector with signature " + signature + ":" +dataTypes);
    }

    /**
     * Set local datatypes of the collector
     */
    public Map<String, BasicDataType<?>> getDataTypes() {
        return dataTypes;
    }

    /**
     * Get a datatype defined for this collector.
     * @param name the name of the datatype to retrieve
     * @return  a {@link DataType} with the given name, as defined for this collector, or <code>null</code>
     *      if no datatype is defined.
     */
    public BasicDataType<?> getDataType(String name) {
        return getDataType(name, false);
    }

    /**
     * Adds a datatype to this collector.
     * The datatype should have a unique id. If it has no id (i.e. getName() returns an empty string), it is not added.
     * If the datatype overrides an existing datatype, a warning is logged.
     * @param dataType the datatype to add
     * @return if applicable, the old (original) datatype with the same id as the dattype that was being added, <code>null</code>
     *    if it is not applicable.
     */
    public BasicDataType<?> addDataType(BasicDataType<?> dataType) {
        String name = dataType.getName();
        if (name == null || "".equals(name)) {
            // not a proper id, so do not add
            return null;
        } else {
            BasicDataType<?> origin = dataType.getOrigin();
            if (origin != null) {
                if (origin.equals(getDataType(origin.getName()))) { // origin is also in this collector
                    Set<DataType<?>> spec = specializations.get(origin.getName());
                    // TODO, not sure that this stuff with specializations goes ok when using 'parent' collectors.
                    // Does not matter very much, because you problably want to use this functionlaity mainly on the System Collector
                    if (spec == null) {
                        spec = new HashSet<DataType<?>>();
                        specializations.put(origin.getName(), spec);
                    }
                    spec.add(dataType);
                } else {
                    roots.put(dataType, new Object());
                }
            } else {
                roots.put(dataType, new Object());
            }
            BasicDataType<?> old = dataTypes.put(name, dataType);
            if (old != null && old != dataType) {
                log.warn("Replaced " + name + " " + old  + " with " + dataType);
            }
            return old;
        }
    }

    /**
     * Returns a set of all DataTypes in this collector which are directly inherited from the one with given name
     */
    public Collection<DataType<?>> getSpecializations(String name) {
        // TODO: see in addDataType
        Set<DataType<?>> set = specializations.get(name);
        if (set == null) {
            return Collections.emptySet();
        }
        else {
            return Collections.unmodifiableSet(set);
        }
    }

    /**
     * Recursively calls {@link #getSpecializations(String)} so that you can easily iterate also all indirectly specializaed versions of a certain DataType in this collector
     */
    public Iterator<DataType<?>> getAllSpecializations(String name) {
        final Iterator<DataType<?>> i = getSpecializations(name).iterator();
        return new Iterator<DataType<?>>() {
            DataType<?> next = i.hasNext() ? i.next() : null;
            Iterator<DataType<?>> subIterator = null;
            @Override
            public boolean hasNext() {
                return next != null || subIterator != null;
            }
            @Override
            public DataType<?> next() {
                if (subIterator != null) {
                    DataType<?> n = subIterator.next();
                    if (! subIterator.hasNext()) subIterator = null;
                    return n;
                }
                if (next != null) {
                    subIterator = getAllSpecializations(next.getName());
                    if (! subIterator.hasNext()) subIterator = null;
                    DataType<?> n = next;
                    if (i.hasNext()) {
                        next = i.next();
                    } else {
                        next = null;
                    }
                    return n;
                }
                throw new NoSuchElementException();
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns all DataTypes in this Collector which did not have an origina DataType (in this Collector).
     */
    public Set<DataType<?>> getRoots() {
        // TODO: see in addDataType
        return Collections.unmodifiableSet(roots.keySet());
    }

    /**
     * Get a datatype defined for this collector, and possibly any collectors it depends on.
     * The collector first searches among datatypes defined in its own set.
     * If that fails, it tries ot get it from any of the other collectors it may depend on, and eventually
     * from the main repository.
     * @param name the name of the datatype to retrieve
     * @param recursive if <code>true</code>, the datatype is also searched in collectors it depends on.
     * @return  a {@link DataType} with the given name, as defined for this collector, or <code>null</code>
     *      if no datatype is defined.
     */
    public BasicDataType<?> getDataType(String name, boolean recursive) {
        BasicDataType<?> dataType = dataTypes.get(name);
        if (this != systemDataTypeCollector && dataType == null && recursive) {
            for (Iterator<DataTypeCollector> i = collectors.iterator(); dataType == null && i.hasNext();) {
                DataTypeCollector collector = i.next();
                dataType = collector.getDataType(name, true);
            }
            if (dataType == null) {
                dataType = systemDataTypeCollector.getDataType(name, false);
            }
        }
        return dataType;
    }

    /**
     * Get a datatype instance through this collector.
     * The collector first searches among datatypes defined in its own set.
     * If that fails, it tries to get it from any of the other collectors it may depend on, and eventually
     * from the main repository.
     * If that fails too, it creates one itself based on the passed base datatype (if given).
     * @param name the name of the datatype to retrieve
     * @param baseDataType the datatype to base a new datatype on if it is not yet defined. Can be <code>null</code>.
     * @return  a {@link DataType} with the given name, as defined for this collector, or <code>null</code>
     *      if no datatype is defined and no base datatype was passed.
     */
    public BasicDataType<?> getDataTypeInstance(String name, DataType<?> baseDataType) {
        BasicDataType<?> dataType = getDataType(name, true);
        if (dataType == null && baseDataType == null) {
            return null;
        } else if (dataType == null) {
            return (BasicDataType<?>) baseDataType.clone(name);
        } else {
            return (BasicDataType<?>)dataType.clone();
        }
    }

    /**
     * Returns whether the dataType with the given name is part of the current collection.
     */
    public boolean contains(String name) {
        return dataTypes.containsKey(name);
    }

    /**
     * Returns whether the dataType is part of the current collection.
     */
    @SuppressWarnings("element-type-mismatch")
    public boolean contains(DataType<?> dataType) {
        return dataTypes.containsValue(dataType);
    }

    /**
     * Unlock a dataType so it can be changed or altered.
     * This will likely fail if the datatype is not part of this collector.
     */
    public void rewrite(DataType<?> dataType) {
        dataType.rewrite(signature);
    }

    /**
     * Lock a dataType so it can be changed or altered.
     * This will likely fail if the datatype is not part of this collector.
     */
    public void finish(DataType<?> dataType) {
        dataType.finish(signature);
    }

    void clear() {
        dataTypes.clear();
        specializations.clear();
        roots.clear();
        collectors.clear();
        signature = null;

    }

    @Override
    public String toString() {
        return signature +  ": " + dataTypes.values() + " " + collectors;
    }

}

