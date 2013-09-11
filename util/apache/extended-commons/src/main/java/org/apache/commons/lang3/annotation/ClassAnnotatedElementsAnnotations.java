/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.lang3.annotation;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.functors.AllPredicate;
import org.apache.commons.lang3.ExtendedArrayUtils;

abstract class ClassAnnotatedElementsAnnotations<T, E extends AnnotatedElement, A extends AnnotatedElementAnnotationsHolder<E>> 
                implements Serializable, Iterable<A> {
    
    private static final long serialVersionUID = 2867089364464520496L;

    private final Map<String, A> holdersMap;
    private final Class<T> sourceClass;

    protected ClassAnnotatedElementsAnnotations(Class<T> srcClass, Map<String, A> map) {
        if (srcClass == null) {
            throw new IllegalArgumentException("srcClass can't be null");
        }
        
        if (map == null) {
            throw new IllegalArgumentException("map can't be null");
        }
        this.holdersMap = map;
        this.sourceClass = srcClass;
    }
    
    public final int size() {
        return holdersMap.size();
    }

    public final A findByName(String name) {
        return holdersMap.get(name);
    }
    
    public Class<T> getSourceClass() {
        return sourceClass;
    }

    @SafeVarargs
    public final boolean filter(Predicate<A>... holderPredicates) {
        int length = ExtendedArrayUtils.length(holderPredicates);
        
        if (length == 0) {
            throw new IllegalArgumentException("holderPredicates can't be null or empty");
        }
        
        int prevSize = size();
        final Predicate<A> toUse;
        
        if (length > 1) {
            toUse = new AllPredicate<>(holderPredicates);
        } else {
            toUse = holderPredicates[0];
        } 
        
        CollectionUtils.filter(holdersMap.values(), toUse);
        return size() < prevSize;
    }

    @Override
    public Iterator<A> iterator() {
        return new DelegatingIterator();
    }

    abstract static class AbstractBuilder<T, E extends AnnotatedElement, A extends AnnotatedElementAnnotationsHolder<E>> {
        final Collection<A> holders;

        private AnnotatedElementAnnotationsHolder.AbstractBuilder<E, A> holderBuilder;

        protected AbstractBuilder() {
            this.holders = new ArrayList<>();
        }

        public final AbstractBuilder<T, E, A> withAnnotatedElement(E element) {
            if (element == null) {
                throw new IllegalArgumentException("element can't be null");
            }

            clearAndAddHolder();
            holderBuilder = createHolderBuilder(element);

            return this;
        }

        public final AbstractBuilder<T, E, A> withAnnotations(Annotation... annotations) {
            if (holderBuilder == null) {
                throw new IllegalStateException("current holder builder is null");
            }

            holderBuilder.withAnnotations(annotations);
            return this;
        }

        public final AbstractBuilder<T, E, A> withBuilder(AnnotatedElementAnnotationsHolder.AbstractBuilder<E, A> builder) {
            if (builder == null) {
                throw new IllegalArgumentException("builder can't be null");
            }

            clearAndAddHolder();
            this.holderBuilder = builder;
            return this;
        }

        private boolean clearAndAddHolder() {
            final boolean was;

            if (holderBuilder != null) {
                A holder = holderBuilder.build();
                holders.add(holder);
                was = true;
            } else {
                was = false;
            }

            holderBuilder = null;
            return was;
        }

        private Map<String, A> createMap() {
            Map<String, A> map = new HashMap<>();

            for (A holder : holders) {
                map.put(holder.getAnnotatedElementName(), holder);
            }

            return map;
        }

        protected abstract AnnotatedElementAnnotationsHolder.AbstractBuilder<E, A> createHolderBuilder(E element);

        @SuppressWarnings("unchecked")
        protected <C extends ClassAnnotatedElementsAnnotations<T, E, A>> C build(Class<T> srcClass) {
            clearAndAddHolder();
            return (C) build(srcClass, createMap());
        }
        
        protected abstract ClassAnnotatedElementsAnnotations<T, E, A> build(Class<T> srcClass, Map<String, A> holdersMap);
    }

    private class DelegatingIterator implements Iterator<A> {
        private final Iterator<A> delegate;

        @SuppressWarnings("synthetic-access")
		public DelegatingIterator() {
            this.delegate = ClassAnnotatedElementsAnnotations.this.holdersMap.values().iterator();
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public A next() {
            return delegate.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
    }
}
