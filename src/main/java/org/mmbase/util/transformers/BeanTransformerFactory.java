/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.util.transformers;

import java.util.*;
import java.lang.reflect.*;
import org.mmbase.util.functions.*;

/**
 * BeanTransformerFactory takes simple {@link Transformer}s class-es and wraps them into parameterixed transformer factories.
 *
 *
 * @author Michiel Meeuwissen
 * @since MMBase-1.9.2
 * @version $Id$
 */

public class BeanTransformerFactory<T extends Transformer> implements ParameterizedTransformerFactory<T>  {


    private final Class<T> implementation;
    private final Parameter<?>[] definition;
    private final List<Method> setMethods = new ArrayList<Method>();

    public BeanTransformerFactory(Class<T> clazz) throws IllegalAccessException, InstantiationException, java.lang.reflect.InvocationTargetException,
        org.mmbase.datatypes.util.xml.DependencyException {
        implementation = clazz;
        definition = BeanFunction.getParameterDefinition(implementation.newInstance(), setMethods);
    }



    public T createTransformer(Parameters parameters) {
        try {
            T result = implementation.newInstance();
            BeanFunction.setParameters(result, parameters, setMethods);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Parameters createParameters() {
        return new Parameters(definition);
    }

}
