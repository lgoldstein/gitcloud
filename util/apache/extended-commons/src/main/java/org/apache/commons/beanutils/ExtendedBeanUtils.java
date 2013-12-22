/*
 * Copyright 2013 Lyor Goldstein
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.beanutils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.AbstractExtendedTransformer;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedMapUtils;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.collections15.ExtendedTransformer;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ExtendedArrayUtils;
import org.apache.commons.lang3.ExtendedCharSequenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ExtendedMethodUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Lyor G.
 */
public class ExtendedBeanUtils {
    // copied from Instrospector where they are package scope
    public static final String GET_PREFIX = "get";
    public static final String SET_PREFIX = "set";
    public static final String IS_PREFIX = "is";

    protected ExtendedBeanUtils() {
        super();
    }

    /**
     * A {@link Transformer} that retrieves the getter {@link Method}
     * from a {@link Pair} obtained via one of the <code>describeBean</code>
     * methods
     * @see #describeBean(Class)
     */
    public static final Transformer<Pair<Method,?>,Method> GETTER_VALUE=
            new Transformer<Pair<Method,?>,Method>() {
                @Override
                public Method transform(Pair<Method, ?> p) {
                    if (p == null) {
                        return null;
                    } else {
                        return p.getLeft();
                    }
                }
        };

    /**
     * A {@link Transformer} that retrieves the getter {@link Method}
     * from a {@link Pair} obtained via one of the <code>describeBean</code>
     * methods
     * @see #describeBean(Class)
     */
    public static final Transformer<Pair<?,Method>,Method> SETTER_VALUE=
            new Transformer<Pair<?,Method>,Method>() {
                @Override
                public Method transform(Pair<?, Method> p) {
                    if (p == null) {
                        return null;
                    } else {
                        return p.getRight();
                    }
                }
        };

    /**
     * @param beanClass The bean's class
     * @param attrsMap The attribute getters {@link Map} - key=the attribute
     * name, value=a {@link Pair} whose left-hand is the getter {@link Method}
     * to be used for the attribute.
     * @return A {@link Transformer} that returns a {@link SortedMap} of the
     * bean properties where key=the property name (case <U>insensitive</U>)
     * value=the property value. <B>Note:</B> <code>null</code> property
     * values are not mapped
     * @throws IllegalArgumentException if no bean class or attributes provided
     * @throws NoSuchElementException if no getter found for an attribute
     * @throws IllegalStateException if same attribute re-mapped
     */
    public static final <E> Transformer<E,SortedMap<String,Object>> beanToPropertiesTransformer(Class<E> beanClass, final Map<String,? extends Pair<Method,?>> attrsMap) {
        if (beanClass == null) {
            throw new IllegalArgumentException("No bean class provided");
        }
        if (ExtendedMapUtils.isEmpty(attrsMap)) {
            throw new IllegalArgumentException("No attributes");
        }

        return new Transformer<E,SortedMap<String,Object>>() {
            @Override
            public SortedMap<String,Object> transform(E input) {
                SortedMap<String,Object>  valuesMap=new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
                for (Map.Entry<String,? extends Pair<Method,?>> pe : attrsMap.entrySet()) {
                    String          name=pe.getKey();
                    Pair<Method,?>  pair=pe.getValue();
                    Method          getter=GETTER_VALUE.transform(pair);
                    if (getter == null) {
                        throw new NoSuchElementException("No getter for property=" + name);
                    }
                    
                    Object  value=ExtendedMethodUtils.invoke(getter, input);
                    if (value == null) {
                        if (valuesMap.containsKey(name)) {
                            throw new IllegalStateException("Multiple (null) values for property=" + name);
                        }
                    } else {
                        Object  prev=valuesMap.put(name, value);
                        if (prev != null) {
                            throw new IllegalStateException("Multiple values for property=" + name);
                        }
                    }
                }

                return valuesMap;
            }
        };
    }

    /**
     * 
     * @param factory A {@link Factory} to be used to retrieve a bean instance to be manipulated
     * @param attrsMap The attribute setters {@link Map} - key=the attribute
     * name, value=a {@link Pair} whose right-hand is the setter {@link Method}
     * to be used for the attribute.
     * @return A {@link Transformer} that retrieves a new instance and then
     * updates its data using the provided setters from a {@link Map} of values
     * @see #propertiesToBeanTransformer(Object, Map)
     */
    public static final <E> Transformer<Map<String,?>,E> propertiesToBeanTransformer(
            final Factory<? extends E> factory, final Map<String,? extends Pair<?,Method>> attrsMap) {
        if (factory == null) {
            throw new IllegalArgumentException("No factory provided");
        }
        
        if (ExtendedMapUtils.isEmpty(attrsMap)) {
            throw new IllegalArgumentException("No attributes");
        }
        
        return new Transformer<Map<String,?>,E>() {
            @Override
            public E transform(Map<String,?> valuesMap) {
                Transformer<Map<String,?>,E>    delegate=propertiesToBeanTransformer((E) factory.create(), attrsMap);
                return delegate.transform(valuesMap);
            }
        };
    }

    /**
     * @param instance The instance be manipulated
     * @param attrsMap The attribute setters {@link Map} - key=the attribute
     * name, value=a {@link Pair} whose right-hand is the setter {@link Method}
     * to be used for the attribute.
     * @return A {@link Transformer} that updates the instance data using
     * the provided setters from a {@link Map} of values whose keys are assumed
     * to match the attributes' names
     * @throws IllegalArgumentException if no instance or attributes provided
     */
    public static final <E> Transformer<Map<String,?>,E> propertiesToBeanTransformer(final E instance, final Map<String,? extends Pair<?,Method>> attrsMap) {
        if (instance == null) {
            throw new IllegalArgumentException("No instance provided");
        }
        
        if (ExtendedMapUtils.isEmpty(attrsMap)) {
            throw new IllegalArgumentException("No attributes");
        }
        
        return new Transformer<Map<String,?>,E>() {
            @Override
            public E transform(Map<String,?> valuesMap) {
                if (ExtendedMapUtils.isEmpty(valuesMap)) {
                    return instance;
                }

                for (Map.Entry<String,? extends Pair<?,Method>> ae : attrsMap.entrySet()) {
                    String          name=ae.getKey();
                    Pair<?,Method>  pair=ae.getValue();
                    Method          setter=SETTER_VALUE.transform(pair);
                    if (setter == null) {
                        continue;   // ignore non settable values
                    }
                    
                    Object  value=valuesMap.get(name);
                    ExtendedMethodUtils.invoke(setter, instance, value);
                }

                return instance;
            }
        };
    }

    /**
     * A {@link Predicate} that returns <code>true</code> if the getter
     * {@link Method} in the right-hand of a {@link Pair} is non-<code>null</code>
     * <B>Note:</B> the predicate does not distinguish between a <code>null</code>
     * {@link Pair} and one that has no getter method
     */
    public static final Predicate<Pair<Method,Method>>   READABLE_PAIR=new Predicate<Pair<Method,Method>>() {
            @Override
            public boolean evaluate(Pair<Method, Method> pair) {
                if (GETTER_VALUE.transform(pair) == null) {
                    return false;
                } else {
                    return true;
                }
            }
        };

    // does the same as filterNonReadableAttributes only returns the modified map instead of the removed entries
    public static final <M extends Map<String,Pair<Method,Method>>> M removeNonReadableAttributes(M attrsMap) {
        filterNonReadableAttributes(attrsMap);
        return attrsMap;
    }
            
    /**
     * Filters all the accessors that do not have a getter
     * @param attrsMap A {@link Map} of attributes accessors where key=attribute
     * name, value=a {@link Pair} of {@link Method}s where the left-hand is the
     * getter - ignored if <code>null</code>/empty
     * @return A {@link List} of all the {@link java.util.Map.Entry}-ies that were removed
     * where key=attribute name (case <U>insensitive</U>), value=a {@link Pair}
     * of {@link Method}s where the left-hand is a <code>null</code> getter
     * @see #READABLE_PAIR
     */
    public static final List<Map.Entry<String,Pair<Method,Method>>> filterNonReadableAttributes(Map<String,Pair<Method,Method>> attrsMap) {
        return ExtendedMapUtils.filterRejectedValues(attrsMap, READABLE_PAIR);
    }

    /**
     * A {@link Predicate} that returns <code>true</code> if the setter
     * {@link Method} in the right-hand of a {@link Pair} is non-<code>null</code>
     * <B>Note:</B> the predicate does not distinguish between a <code>null</code>
     * {@link Pair} and one that has no setter method
     */
    public static final Predicate<Pair<Method,Method>>   WRITEABLE_PAIR=new Predicate<Pair<Method,Method>>() {
            @Override
            public boolean evaluate(Pair<Method, Method> pair) {
                if (SETTER_VALUE.transform(pair) == null) {
                    return false;
                } else {
                    return true;
                }
            }
        };

    // does the same as filterNonModifiableAttributes only returns the modified map instead of the removed entries
    public static final <M extends Map<String,Pair<Method,Method>>> M removeNonModifiableAttributes(M attrsMap) {
        filterNonModifiableAttributes(attrsMap);
        return attrsMap;
    }

    /**
     * Filters all the accessors that do not have a setter
     * @param attrsMap A {@link Map} of attributes accessors where key=attribute
     * name, value=a {@link Pair} of {@link Method}s where the right-hand is the
     * setter - ignored if <code>null</code>/empty
     * @return A {@link List} of all the {@link java.util.Map.Entry}-ies that were removed
     * where key=attribute name (case <U>insensitive</U>), value=a {@link Pair}
     * of {@link Method}s where the right-hand is a <code>null</code> setter
     * @see #WRITEABLE_PAIR
     */
    public static final List<Map.Entry<String,Pair<Method,Method>>> filterNonModifiableAttributes(Map<String,Pair<Method,Method>> attrsMap) {
        return ExtendedMapUtils.filterRejectedValues(attrsMap, WRITEABLE_PAIR);
    }

    /**
     * @param beanClass The {@link Class} to be described (ignored if <code>null</code>)
     * @return A {@link SortedMap} of all the properties where key=the property name
     * (case <U>insensitive</U>, value={@link Pair} of {@link Method}-s where
     * the left one (if non-<code>null</code>) represents the getter and the
     * right one (if non-<code>null</code> represents the setter
     * @throws IllegalStateException if same property (with different case)
     * encountered or failed to extract bean information
     * @see #describeBean(BeanInfo)
     * @see Introspector#getBeanInfo(Class)
     */
    public static final SortedMap<String,Pair<Method,Method>> describeBean(Class<?> beanClass) {
        return describeBean(beanClass, true, false);
    }
    
    /**
     * @param beanClass The {@link Class} to be described (ignored if <code>null</code>)
     * @param publicOnly If <code>true</code> then consider only <code>public</code>
     * {@link Method}-s otherwise consider all methods
     * @param makeAccessible If not only <code>public</code> methods considered and
     * a non-accessible method found whether to make it accessible
     * @return A {@link SortedMap} of all the properties where key=the property name
     * (case <U>insensitive</U>, value={@link Pair} of {@link Method}-s where
     * the left one (if non-<code>null</code>) represents the getter and the
     * right one (if non-<code>null</code> represents the setter
     * @throws IllegalStateException if same property (with different case)
     * encountered or failed to extract bean information
     */
    public static final SortedMap<String,Pair<Method,Method>> describeBean(Class<?> beanClass, boolean publicOnly, boolean makeAccessible) {
        if (beanClass == null) {
            return ExtendedMapUtils.emptySortedMap();
        }

        /*
         * NOTE: for interfaces, the "superclass" is always null, so if this
         * is an interface or an abstract class we need to "climb" the hierarchy
         */
        SortedMap<String,Pair<Method,Method>>   map;

        /*
         * For a Proxy we cannot use introspection as it would give us the
         * proxy methods and we want the interfaces it implements
         */
        if (Proxy.isProxyClass(beanClass)) {
            map = ExtendedMapUtils.emptySortedMap();
        } else {
            map = describeBeanProperties(beanClass, publicOnly, makeAccessible);
            if ((!beanClass.isInterface()) && (!Modifier.isAbstract(beanClass.getModifiers()))) {
                return map;
            }
        }
        
        List<Class<?>>  ifcs=ClassUtils.getAllInterfaces(beanClass);
        if (ExtendedCollectionUtils.isEmpty(ifcs)) {
            return map;
        }
        
        for (Class<?> ifc : ifcs) {
            SortedMap<String,Pair<Method,Method>> ifcMap=describeBeanProperties(ifc, true, false);
            if (ExtendedMapUtils.isEmpty(ifcMap)) {
                continue;
            }

            if (ExtendedMapUtils.isEmpty(map)) {
                map = ifcMap;
                continue;
            }
            
            for (Map.Entry<String,Pair<Method,Method>> pe : ifcMap.entrySet()) {
                String              name=pe.getKey();
                Pair<Method,Method> newPair=pe.getValue(), oldPair=map.get(name);
                if (oldPair == null) {
                    map.put(name, newPair);
                } else {
                    map.put(name, mergeAccessors(oldPair, newPair));
                }
            }
        }
        
        return map;
    }
    
    private static SortedMap<String,Pair<Method,Method>> describeBeanProperties(Class<?> beanClass, boolean publicOnly, boolean makeAccessible) {
        if (publicOnly) {
            try {
                return describeBean(Introspector.getBeanInfo(beanClass));
            } catch(IntrospectionException e) {
                throw new IllegalStateException("describeBeanProperties(" + beanClass.getSimpleName() + ")"
                                              + " failed (" + e.getClass().getSimpleName() + ")"
                                              + " to retrieve bean info: " + e.getMessage());
            }
        }
        
        SortedMap<String,Pair<Method,Method>>   map=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Class<?> clazz=beanClass; clazz != null; clazz = clazz.getSuperclass()) {
            Method[]    methods=clazz.getDeclaredMethods();
            for (Method m : methods) {
                String  name=toAttributeName(m);
                if (StringUtils.isEmpty(name)) {
                    continue;
                }

                if (makeAccessible && (!m.isAccessible())) {
                    m.setAccessible(true);
                }

                Pair<Method,Method> newPair=isGetter(m) ? Pair.<Method,Method>of(m, null) : Pair.<Method,Method>of(null, m);
                Pair<Method,Method> oldPair=map.get(name), mergedPair=oldPair == null ? newPair : mergeAccessors(oldPair, newPair);

                Method  gm=mergedPair.getLeft(), sm=mergedPair.getRight();
                if ((gm != null) && (sm != null) && (!isMatchingAccessorPair(gm, sm))) {
                    throw new IllegalStateException("describeBeanProperties(" + beanClass.getSimpleName() + ")[" + name + "] mismatched accessor pair");
                }

                map.put(name, mergedPair);
            }
        }
        
        return map;
    }

    /**
     * Checks that a getter/setter pair refer to the same attribute type
     * @param getter The getter {@link Method}
     * @param setter The setter {@link Method}
     * @return <code>true</code> <U>all</U> of the following holds:</BR>
     * <UL>
     *      <LI>Neither accessor is <code>null</code></LI>
     *      <LI>The getter is indeed a getter and the setter is a setter</LI>
     *      <LI>
     *      The setter's argument is assignable from the getter's return
     *      type (<B>Note:</B> we don't check equality in order to enable
     *      <U>co-variant return types</U>).
     *      </LI>
     * </UL>
     * @see #isGetter(Method)
     * @see #isSetter(Method)
     */
    public static final boolean isMatchingAccessorPair(Method getter, Method setter) {
        if ((getter == null) || (setter == null)) {
            return false;
        }

        if ((!isGetter(getter)) || (!isSetter(setter))) {
            return false;
        }
        
        Class<?>    gType=getter.getReturnType();
        Class<?>[]  params=setter.getParameterTypes();
        Class<?>    sType=params[0];
        // we check if assignable to allow for co-variant return
        if (sType.isAssignableFrom(gType)) {
            return true;
        } else {
            return false;
        }
    }

    private static Pair<Method,Method> mergeAccessors(Pair<Method,Method> oldPair, Pair<Method,Method> newPair) {
        Method  oldGetter=oldPair.getLeft(), newGetter=newPair.getLeft();
        Method  oldSetter=oldPair.getRight(), newSetter=newPair.getRight();
        Method  getter=mergeAccessors(oldGetter, newGetter);
        Method  setter=mergeAccessors(oldSetter, newSetter);
        if ((getter == oldGetter) && (setter == oldSetter)) {
            return oldPair;
        } else if ((getter == newGetter) && (setter == newSetter)) {
            return newPair;
        } else {
            return Pair.of(getter, setter);
        }
    }

    private static Method mergeAccessors(Method oldMethod, Method newMethod) {
        if (oldMethod == null) {
            return newMethod;
        } else if (newMethod == null) {
            return oldMethod;
        } else if (oldMethod == newMethod) {
            return oldMethod;
        }
        
        // prefer interface methods over non-interface ones
        Class<?>    oldContainer=oldMethod.getDeclaringClass(), newContainer=newMethod.getDeclaringClass();
        if (oldContainer.isInterface()) {
            return oldMethod;
        } else if (newContainer.isInterface()) {
            return newMethod;
        }
        
        // prefer a super-class over its derived one
        if (oldContainer.isAssignableFrom(newContainer)) {
            return oldMethod;
        } else if (newContainer.isAssignableFrom(oldContainer)) {
            return newMethod;
        }
        
        // prefer abstract classes over concrete ones
        if (Modifier.isAbstract(oldContainer.getModifiers())) {
            return oldMethod;
        } else if (Modifier.isAbstract(newContainer.getModifiers())) {
            return newMethod;
        }
        
        // if all else fails prefer the old method
        return oldMethod;
    }
    
    /**
     * Calculates the name of the counterpart accessor method name for getter or setter
     * 
     * @param m the getter/setter {@link Method}
     * @return the counterpart method name or <code>null</code> if m is not a setter, 
     *         getter or <code>null</code>
     * @see #isGetter(Method)
     * @see #isSetter(Method)
     */
    public static final String getCounterpartAccessorName(Method m) {
        String capitalizedAttrName = ExtendedCharSequenceUtils.capitalize(toAttributeName(m));
        
        if (StringUtils.isEmpty(capitalizedAttrName)) {
            return null;
        }
        
        if (isGetter(m)) {
            return SET_PREFIX + capitalizedAttrName;
        } else {
            Class<?> attributeType = getSetterAttributeType(m);
            
            if (Boolean.TYPE == attributeType) {
                return IS_PREFIX + capitalizedAttrName;
            } else {
                return GET_PREFIX + capitalizedAttrName;
            }
        }
    }
    
    /**
     * Calculates the attribute type of setter or a getter
     * 
     * @param m the getter/setter {@link Method}
     * @return the {@link Class} of the attribute or <code>null</code> if m is not a setter, 
     *         getter or <code>null</code>
     * @see #isGetter(Method)
     * @see #isSetter(Method)
     */
    public static final Class<?> getAttrributeType(Method m) {
        if (isGetter(m)) {
            return getGetterAttributeType(m);
        } else if (isSetter(m)) {
            return getSetterAttributeType(m);
        } else {
            return null;
        }
    }

    private static Class<?> getSetterAttributeType(Method m) {
        return m.getParameterTypes()[0];
    }

    private static Class<?> getGetterAttributeType(Method m) {
        return m.getReturnType();
    }
    
    /**
     * Returns the pure attribute name derived from either a getter
     * or setter method
     * @param m The {@link Method} to be evaluated
     * @return The pure attribute name - <code>null</code> if method
     * is <code>null</code> or not a getter/setter
     * @see #isGetter(Method)
     * @see #isSetter(Method)
     */
    public static final String toAttributeName(Method m) {
        if (isGetter(m)) {
            final String  name=m.getName();
            final String    prefix=ExtendedCharSequenceUtils.isProperPrefix(name, GET_PREFIX, true)
                                        ? GET_PREFIX
                                        : IS_PREFIX
                                        ;
            return ExtendedCharSequenceUtils.uncapitalize(name.substring(prefix.length()));
        } else if (isSetter(m)) {
            final String  name=m.getName();
            return ExtendedCharSequenceUtils.uncapitalize(name.substring(SET_PREFIX.length()));
        } else {
            return null;
        }
    }

    /**
     * An {@link ExtendedPredicate} that returns <code>true</code> if it evaluates
     * a getter {@link Method}
     * @see #isGetter(Method)
     */
    public static final ExtendedPredicate<Method>   IS_GETTER=
        new AbstractExtendedPredicate<Method>(Method.class) {
            @Override
            public boolean evaluate(Method m) {
                return isGetter(m);
            }
        };

    /**
     * @param m The {@link Method} to be checked
     * @return <code>true</code> if <U>all</U> of these conditions are satisfied:</BR>
     * <UL>
     *      <LI>Not <code>null</code></LI>
     *      <LI>Not static</LI>
     *      <LI>Starts with either {@link #GET_PREFIX} or {@link #IS_PREFIX}</LI>
     *      <LI>Has some extra characters after the prefix</LI>
     *      <LI>Has no arguments</LI>
     *      <LI>
     *      Return type is not <code>void</code> (<B>Note:</B> {@link Void} wrapper
     *      is considered a valid return type)
     *      </LI>
     *      <LI>
     *      If it has {@link #IS_PREFIX} then it must return a <code>boolean</code>
     *      (<B>Note:</B> {@link Boolean} wrapper is <U>not</U> considered a valid
     *      return type)
     *      </LI>
     * </UL>
     * @see #isGetterName(CharSequence)
     */
    public static final boolean isGetter(Method m) {
        if (m == null) {
            return false;
        }
        
        if (Modifier.isStatic(m.getModifiers())) {
            return false;
        }
        
        String  name=m.getName();
        if (!isGetterName(name)) {
            return false;
        }
        
        Class<?>[]  params=m.getParameterTypes();
        if (ExtendedArrayUtils.length(params) != 0) {
            return false;
        }
        
        Class<?>    returnType=m.getReturnType();
        if (Void.TYPE.isAssignableFrom(returnType)) {
            return false;
        }
        
        if (isBooleanGetterName(name) && (!Boolean.TYPE.isAssignableFrom(returnType))) {
            return false;
        }

        return true;
    }

    /**
     * @param name A candidate method name
     * @return <code>true</code> if the candidate method name is a valid
     * getter name - i.e., not {@code null}/empty and starts with either
     * {@link #GET_PREFIX} or {@link #IS_PREFIX}
     * @see #isBooleanGetterName(CharSequence)
     * @see #isValueGetterName(CharSequence)
     */
    public static final boolean isGetterName(CharSequence name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        } else if (isValueGetterName(name) || isBooleanGetterName(name)) {
           return true;
        } else {
            return false;
        }
    }

    /**
     * @param name A candidate method name
     * @return <code>true</code> if the candidate method name is a valid
     * <U>non-{@code boolean}</U> getter name -  i.e., not {@code null}/empty
     * and starts with {@link #GET_PREFIX}
     */
    public static final boolean isValueGetterName(CharSequence name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        } else if (ExtendedCharSequenceUtils.isProperPrefix(name, GET_PREFIX, true)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param name A candidate method name
     * @return <code>true</code> if the candidate method name is a valid
     * {@code boolean} getter name -  i.e., not {@code null}/empty and starts
     * with {@link #IS_PREFIX}
     */
    public static final boolean isBooleanGetterName(CharSequence name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        } else if (ExtendedCharSequenceUtils.isProperPrefix(name, IS_PREFIX, true)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * An {@link ExtendedPredicate} that returns <code>true</code> if it evaluates
     * a setter {@link Method}
     * @see #isSetter(Method)
     */
    public static final ExtendedPredicate<Method>   IS_SETTER=
        new AbstractExtendedPredicate<Method>(Method.class) {
            @Override
            public boolean evaluate(Method m) {
                return isSetter(m);
            }
        };

    /**
     * @param m The {@link Method} to be checked
     * @return <code>true</code> if <U>all</U> of these conditions are satisfied:</BR>
     * <UL>
     *      <LI>Not <code>null</code></LI>
     *      <LI>Not static</LI>
     *      <LI>Starts with {@link #SET_PREFIX}
     *      <LI>Has some extra characters after the prefix</LI>
     *      <LI>Has <U>exactly <B>one</B></U> argument</LI>
     *      <LI>
     *      The argument is not <code>void</code>  (<B>Note:</B> {@link Void} wrapper
     *      is considered a valid argument type)
     *      </LI>
     *      <LI>
     *      Return type is <code>void</code> (<B>Note:</B> {@link Void} wrapper
     *      is <U>not</U> considered a valid return type)
     *      </LI>
     * </UL>
     */
    public static final boolean isSetter(Method m) {
        if (m == null) {
            return false;
        }
        
        if (Modifier.isStatic(m.getModifiers())) {
            return false;
        }
        
        String  name=m.getName();
        if (!isSetterName(name)) {
            return false;
        }
        
        Class<?>[]  params=m.getParameterTypes();
        if (ExtendedArrayUtils.length(params) != 1) {
            return false;
        }
        
        Class<?>    propType=params[0];
        if (Void.TYPE.isAssignableFrom(propType)) {
            return false;
        }

        Class<?>    returnType=m.getReturnType();
        if (!Void.TYPE.isAssignableFrom(returnType)) {
            return false;
        }

        return true;
    }

    /**
     * @param name Candidate method name
     * @return {@code true} if the method name is not {@code null}/empty
     * and starts with {@link #SET_PREFIX}
     */
    public static final boolean isSetterName(CharSequence name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        } else if (ExtendedCharSequenceUtils.isProperPrefix(name, SET_PREFIX, true)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param beanInfo A {@link BeanInfo} description (ignored if <code>null</code>)
     * @return A {@link SortedMap} of all the properties where key=the property name
     * (case <U>insensitive</U>, value={@link Pair} of {@link Method}-s where
     * the left one (if non-<code>null</code>) represents the getter and the
     * right one (if non-<code>null</code> represents the setter
     * @throws IllegalStateException if same property (with different case)
     * encountered
     * @see #describeBean(PropertyDescriptor...)
     */
    public static final SortedMap<String,Pair<Method,Method>> describeBean(BeanInfo beanInfo) {
        if (beanInfo == null) {
            return ExtendedMapUtils.emptySortedMap();
        } else {
            return describeBean(beanInfo.getPropertyDescriptors());
        }
    }
    
    /**
     * @param descriptors A group of {@link PropertyDescriptor}-s (ignored if
     * <code>null</code>/empty)
     * @return A {@link SortedMap} of all the properties where key=the property name
     * (case <U>insensitive</U>, value={@link Pair} of {@link Method}-s where
     * the left one (if non-<code>null</code>) represents the getter and the
     * right one (if non-<code>null</code> represents the setter
     * @throws IllegalStateException if same property (with different case)
     * encountered
     * @see #describeBean(Collection)
     */
    public static final SortedMap<String,Pair<Method,Method>> describeBean(PropertyDescriptor ... descriptors) {
        return describeBean(ExtendedArrayUtils.asList(descriptors));
    }
    
    /**
     * An {@link ExtendedTransformer} that retrieves the {@link PropertyDescriptor#getReadMethod()}
     * value. <B>Note:</B> one cannot distinguish between a <code>null</code>
     * descriptor and one with no read {@link Method}
     */
    public static final ExtendedTransformer<PropertyDescriptor,Method>  PROPERTY_READER=
            new AbstractExtendedTransformer<PropertyDescriptor, Method>(PropertyDescriptor.class,Method.class) {
                @Override
                public Method transform(PropertyDescriptor prop) {
                    if (prop == null) {
                        return null;
                    } else {
                        return prop.getReadMethod();
                    }
                }
            };

    /**
     * An {@link ExtendedTransformer} that retrieves the {@link PropertyDescriptor#getWriteMethod()}
     * value. <B>Note:</B> one cannot distinguish between a <code>null</code>
     * descriptor and one with no write {@link Method}
     */
    public static final ExtendedTransformer<PropertyDescriptor,Method>  PROPERTY_WRITER=
            new AbstractExtendedTransformer<PropertyDescriptor, Method>(PropertyDescriptor.class,Method.class) {
                @Override
                public Method transform(PropertyDescriptor prop) {
                    if (prop == null) {
                        return null;
                    } else {
                        return prop.getWriteMethod();
                    }
                }
            };
    
    /**
     * A {@link Transformer} that extracts the read/write accessor {@link Method}s
     * from a {@link PropertyDescriptor} and returns them as a {@link Pair}
     * whose left-hand is the getter (if any) and right-hand the setter (if any).
     * If no descriptor or no accessors then returns <code>null</code>.
     */
    public static final Transformer<PropertyDescriptor,Pair<Method,Method>> PROPERTY_ACCESSORS=
            new Transformer<PropertyDescriptor,Pair<Method,Method>>() {
                @Override
                public Pair<Method, Method> transform(PropertyDescriptor prop) {
                    if (prop == null) {
                        return null;
                    }
                    Method  readMethod=PROPERTY_READER.transform(prop);
                    Method writeMethod=PROPERTY_WRITER.transform(prop);
                    if ((readMethod == null) && (writeMethod == null)) {
                        return null;       // strange, but does not matter
                    } else {
                        return Pair.of(readMethod, writeMethod);
                    }
                }
        };

    /**
     * @param descriptors A {@link Collection} of {@link PropertyDescriptor}-s
     * (ignored if <code>null</code>/empty)
     * @return A {@link SortedMap} of all the properties where key=the property name
     * (case <U>insensitive</U>, value={@link Pair} of {@link Method}-s where
     * the left one (if non-<code>null</code>) represents the getter and the
     * right one (if non-<code>null</code> represents the setter
     * @throws IllegalStateException if same property (with different case)
     * encountered
     */
    public static final SortedMap<String,Pair<Method,Method>> describeBean(Collection<? extends PropertyDescriptor> descriptors) {
        if (ExtendedCollectionUtils.isEmpty(descriptors)) {
            return ExtendedMapUtils.emptySortedMap();
        }
        
        SortedMap<String,Pair<Method,Method>>  result=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (PropertyDescriptor prop : descriptors) {
            String              name=prop.getName();
            Pair<Method,Method> pair=PROPERTY_ACCESSORS.transform(prop);
            if (pair == null) {
                continue;   // strange, but does not matter
            }
            
            Object  prev=result.put(name, pair);
            if (prev != null) {
                throw new IllegalStateException("Multiple descriptors for property=" + name + " (possible case sensitivity issue)");
            }
        }
        
        return result;
    }

}
