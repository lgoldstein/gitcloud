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

package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang3.ExtendedArrayUtils;

/**
 * @author Lyor G.
 * @since Jun 5, 2013 3:13:35 PM
 */
public class ExtendedFileFilterUtils extends FileFilterUtils {
    public ExtendedFileFilterUtils () {
        super();
    }

    /**
     * Converts a {@link FileFilter} into an {@link ExtendedPredicate} whose
     * {@link Predicate#evaluate(Object)} method returns the {@link FileFilter#accept(File)}
     * @param filter The {@link FileFilter} to convert - ignored if <code>null</code>
     * @return The encapsulating {@link Predicate}
     */
    public static final ExtendedPredicate<File> asPredicate(final FileFilter filter) {
        if (filter == null) {
            return null;
        } else {
            return new AbstractExtendedPredicate<File>(File.class) {
                @Override
                public boolean evaluate(File file) {
                    return filter.accept(file);
                }
            };
        }
    }

    /**
     * @param predicate A {@link Predicate} that evaluates {@link File}-s
     * @return An equivalent {@link IOFileFilter}
     */
    public static final IOFileFilter asFileFilter(final Predicate<? super File> predicate) {
        if (predicate == null) {
            return null;
        } else {
            return new AbstractFileFilter() {
                @Override
                public boolean accept (File file) {
                    return predicate.evaluate(file);
                }
                
                @Override
                public String toString() {
                    return "asFileFilter(" + predicate + ")";
                }
            };
        }
    }

    /**
     * @param filter The original {@link FileFilter}
     * @return An {@link IOFileFilter} that accepts all the files that are
     * rejected by the original one
     * @throws IllegalArgumentException if no original filter instance
     */
    public static final IOFileFilter complementingFilter(final FileFilter filter) {
    	if (filter == null) {
    		throw new IllegalArgumentException("No filter to complement");
    	}
    	
    	return new AbstractFileFilter() {
    		@Override
    		public boolean accept (File pathname) {
    			if (filter.accept(pathname)) {
    				return false;
    			} else {
    				return true;
    			}
    		}
            
            @Override
            public String toString() {
                return "complementingFilter(" + filter + ")";
            }
    	};
    }

    /**
     * @param conjunctive <code>true</code>=create a <I>conjunctive</I> (AND),
     * <code>false</code> create a <I>disjunctive</I> (OR) filter
     * @param filters  The {@link FileFilter}s to compose the filter from
     * @return A composite {@link IOFileFilter} of all the others where acceptance
     * depends on the junction type and the results of the component filters
     * <B>Note:</B> for a <U>conjunctive</U> filter, the invocation of the
     * component filters {@link FileFilter#accept(File)} stops at the <U>first</U>
     * filter that returns <code>false</code>. For a <U>disjunctive</U> filter
     * the invocation stops at the  <U>first</U> filter that returns <code>true</code>.
     */
    public static IOFileFilter createFileFilter(final boolean conjunctive, final Collection<? extends FileFilter> filters) {
        return new AbstractFileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (ExtendedCollectionUtils.size(filters) <= 0) {
                    return !conjunctive;
                }
                
                for (FileFilter filter : filters) {
                    boolean result=filter.accept(pathname);
                    if (conjunctive) {
                        if (!result) {
                            return false;
                        }
                    } else {
                        if (result) {
                            return true;
                        }
                    }
                }
                
                /*
                 * If reached this stage then either
                 * 
                 *  - all filters accepted the path (for conjunctive)
                 *  - all filters rejected the path (for disjunctive)
                 */
                return conjunctive;
            }
        };
    }

    /**
     * @param conjunctive <code>true</code>=create a <I>conjunctive</I> (AND),
     * <code>false</code> create a <I>disjunctive</I> (OR) filter
     * @param filters  The {@link FileFilter}s to compose the filter from
     * @return A composite {@link IOFileFilter} of all the others where acceptance
     * depends on the junction type and the results of the component filters.
     * <B>Note:</B> for a <U>conjunctive</U> filter, the invocation of the
     * component filters {@link FileFilter#accept(File)} stops at the <U>first</U>
     * filter that returns <code>false</code>. For a <U>disjunctive</U> filter
     * the invocation stops at the  <U>first</U> filter that returns <code>true</code>.
     * @see #createFileFilter(boolean, Collection)
     */
    public static IOFileFilter createFileFilter(boolean conjunctive, FileFilter ... filters) {
    	return createFileFilter(conjunctive, ExtendedArrayUtils.asList(filters));
    }

    /**
     * @param filters The {@link FileFilter}s to compose the filter from
     * @return A composite {@link IOFileFilter} of all the others where acceptance
     * is <code>true</code> if <U>at least one</U> filter component accepts the file
     * @see #createFileFilter(boolean, Collection)
     */
    public static IOFileFilter createDisjunctiveFileFilter(Collection<? extends FileFilter> filters) {
    	return createFileFilter(false, filters);
    }

    /**
     * @param filters The {@link FileFilter}s to compose the filter from
     * @return A composite {@link IOFileFilter} of all the others where acceptance
     * is <code>true</code> if <U>at least one</U> filter component accepts the file
     * @see #createFileFilter(boolean, Collection)
     */
    public static IOFileFilter createDisjunctiveFileFilter(FileFilter ... filters) {
    	return createDisjunctiveFileFilter(ExtendedArrayUtils.asList(filters));
    }

    /**
     * @param filters The {@link FileFilter}s to compose the filter from
     * @return A composite {@link IOFileFilter} of all the others where acceptance
     * is <code>true</code> only if <U>all</U> filter components accept the file
     * @see #createFileFilter(boolean, Collection)
     */
    public static IOFileFilter createConjunctiveFileFilter(Collection<? extends FileFilter> filters) {
    	return createFileFilter(true, filters);
    }

    /**
     * @param filters The {@link FileFilter}s to compose the filter from
     * @return A composite {@link IOFileFilter} of all the others where acceptance
     * is <code>true</code> only if <U>all</U> filter components accept the file
     * @see #createFileFilter(boolean, Collection)
     */
    public static IOFileFilter createConjunctiveFileFilter(FileFilter ... filters) {
    	return createConjunctiveFileFilter(ExtendedArrayUtils.asList(filters));
    }
}
