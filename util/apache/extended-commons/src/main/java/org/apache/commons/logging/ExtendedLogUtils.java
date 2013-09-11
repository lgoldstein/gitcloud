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

package org.apache.commons.logging;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections15.AbstractExtendedPredicate;
import org.apache.commons.collections15.Closure;
import org.apache.commons.collections15.ClosureUtils;
import org.apache.commons.collections15.ExtendedCollectionUtils;
import org.apache.commons.collections15.ExtendedPredicate;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.PredicateUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.ExtendedCharSequenceUtils;

/**
 * @author Lyor G.
 */
public final class ExtendedLogUtils {
    private ExtendedLogUtils() {
        throw new UnsupportedOperationException("No instance");
    }

    /**
     * @param logger The {@link Log}-ger instance to use - ignored if <code>null</code>
     * @param level The {@link Level} to use - ignored if <code>null</code>
     * @param prefix The prefix to append to each logged value
     * @param values The {@link Collection} of values to log - ignored if <code>null</code>/empty
     * @see #logAll(Log, Level, String, Transformer, Collection)
     */
    public static void logAll(Log logger, Level level, String prefix, Collection<?> values) {
        logAll(logger, level, prefix, null, values);
    }
    
    /**
     * @param logger The {@link Log}-ger instance to use - ignored if <code>null</code>
     * @param level The {@link Level} to use - ignored if <code>null</code>
     * @param prefix The prefix to append to each logged value
     * @param xformer The {@link Transformer} to apply before logging the value.
     * If <code>null</code> then the {@link String#valueOf(Object)} is used
     * on the logged value
     * @param values The {@link Collection} of values to log - ignored if <code>null</code>/empty
     * @see #isLoggable(Log, Level)
     */
    public static <SRC> void logAll(Log logger, Level level, String prefix, Transformer<? super SRC,?> xformer, Collection<? extends SRC> values) {
        if ((logger == null) || (level == null) || (!isLoggable(logger, level)) || ExtendedCollectionUtils.isEmpty(values)) {
            return;
        }
        
        final StringBuilder sb=new StringBuilder(ExtendedCharSequenceUtils.getSafeLength(prefix) + (values.size() * 16))
                                        .append(prefix)
                                        .append(' ')
                                        ;
        final int               sbLen=sb.length();
        final Closure<Object>   l=loggingClosure(logger, level);
        for (SRC v : values) {
            sb.setLength(sbLen);

            Object  o=(xformer == null) ? v : xformer.transform(v);
            sb.append(o);
            l.execute(sb.toString());
        }
    }

    /**
     * A {@link Predicate#evaluate(Object)} that returns <code>true</code>
     * if it is provided a non-<code>null</code> logger that has {@link Log#isErrorEnabled()}
     */
    public static final ExtendedPredicate<Log> ERROR_ENABLED=
        new AbstractExtendedPredicate<Log>(Log.class) {
            @Override
            public boolean evaluate(Log logger) {
                if ((logger == null) || (!logger.isErrorEnabled())) {
                    return false;
                } else {
                    return true;
                }
            }
        };

    /**
     * A {@link Predicate#evaluate(Object)} that returns <code>true</code>
     * if it is provided a non-<code>null</code> logger that has {@link Log#isWarnEnabled()}
     */
    public static final ExtendedPredicate<Log> WARN_ENABLED=
        new AbstractExtendedPredicate<Log>(Log.class) {
            @Override
            public boolean evaluate(Log logger) {
                if ((logger == null) || (!logger.isWarnEnabled())) {
                    return false;
                } else {
                    return true;
                }
            }
        };

    /**
     * A {@link Predicate#evaluate(Object)} that returns <code>true</code>
     * if it is provided a non-<code>null</code> logger that has {@link Log#isInfoEnabled()}
     */
    public static final ExtendedPredicate<Log> INFO_ENABLED=
        new AbstractExtendedPredicate<Log>(Log.class) {
            @Override
            public boolean evaluate(Log logger) {
                if ((logger == null) || (!logger.isInfoEnabled())) {
                    return false;
                } else {
                    return true;
                }
            }
        };

    /**
     * A {@link Predicate#evaluate(Object)} that returns <code>true</code>
     * if it is provided a non-<code>null</code> logger that has {@link Log#isDebugEnabled()}
     */
    public static final ExtendedPredicate<Log> DEBUG_ENABLED=
        new AbstractExtendedPredicate<Log>(Log.class) {
            @Override
            public boolean evaluate(Log logger) {
                if ((logger == null) || (!logger.isDebugEnabled())) {
                    return false;
                } else {
                    return true;
                }
            }
        };

    /**
     * A {@link Predicate#evaluate(Object)} that returns <code>true</code>
     * if it is provided a non-<code>null</code> logger that has {@link Log#isTraceEnabled()}
     */
    public static final ExtendedPredicate<Log> TRACE_ENABLED=
        new AbstractExtendedPredicate<Log>(Log.class) {
            @Override
            public boolean evaluate(Log logger) {
                if ((logger == null) || (!logger.isTraceEnabled())) {
                    return false;
                } else {
                    return true;
                }
            }
        };

    /**
     * @param logger The {@link Log}-ger to be evaluated
     * @param level The log {@link Level} to be evaluated
     * @return <code>true</code> if this logger is enabled for the specified level
     * @see #loggablePredicate(Level)
     */
    public static boolean isLoggable(Log logger, Level level) {
        Predicate<Log> predicate=loggablePredicate(level);
        return predicate.evaluate(logger);
    }

    /**
     * @param level The log {@link Level} to be evaluated
     * @return A {@link Predicate#evaluate(Object)} that returns <code>true</code>
     * if the matching {@link Log}-gger instance level is enabled.
     * <UL>
     *      <LI>{@link Level#OFF} - {@link PredicateUtils#falsePredicate()}</LI>
     *      <LI>{@link Level#SEVERE} - {@link #ERROR_ENABLED}</LI>
     *      <LI>{@link Level#WARNING} - {@link #WARN_ENABLED}</LI>
     *      <LI>{@link Level#INFO}/{@link Level#ALL} - {@link #INFO_ENABLED}</LI>
     *      <LI>{@link Level#CONFIG}/{@link Level#FINE} - {@link #DEBUG_ENABLED}</LI>
     *      <LI>All others - {@link #TRACE_ENABLED}</LI>
     * </UL>
     */
    public static Predicate<Log> loggablePredicate(Level level) {
        if (level == null) {
            throw new IllegalArgumentException("No level provided");
        }
        
        if (Level.OFF.equals(level)) {
            return PredicateUtils.falsePredicate();
        } if (Level.SEVERE.equals(level)) {
            return ERROR_ENABLED;
        } else if (Level.WARNING.equals(level)) {
            return WARN_ENABLED;
        } else if (Level.INFO.equals(level) || Level.ALL.equals(level)) {
            return INFO_ENABLED;
        } else if (Level.CONFIG.equals(level) || Level.FINE.equals(level)) {
            return DEBUG_ENABLED;
        } else {
            return TRACE_ENABLED;
        }
    }
    /**
     * @param logger The {@link Log}-ger instance to use
     * @param level The log {@link Level} mapped as follows:</BR>
     * <UL>
     *      <LI>{@link Level#OFF} - {@link #nologClosure(Log)}</LI>
     *      <LI>{@link Level#SEVERE} - {@link #errorClosure(Log)}</LI>
     *      <LI>{@link Level#WARNING} - {@link #warnClosure(Log)}</LI>
     *      <LI>{@link Level#INFO}/{@link Level#ALL} - {@link #infoClosure(Log)}</LI>
     *      <LI>{@link Level#CONFIG}/{@link Level#FINE} - {@link #debugClosure(Log)}</LI>
     *      <LI>All others - {@link #traceClosure(Log)}</LI>
     * </UL>
     * @return A closure whose {@link Closure#execute(Object)} method logs
     * the {@link String#valueOf(Object)} value of its argument if
     * the specific level is enabled
     */
    public static <T> Closure<T> loggingClosure(Log logger, Level level) {
        if (level == null) {
            throw new IllegalArgumentException("No level provided");
        }
        
        if (Level.OFF.equals(level)) {
            return nologClosure(logger);
        } if (Level.SEVERE.equals(level)) {
            return errorClosure(logger);
        } else if (Level.WARNING.equals(level)) {
            return warnClosure(logger);
        } else if (Level.INFO.equals(level) || Level.ALL.equals(level)) {
            return infoClosure(logger);
        } else if (Level.CONFIG.equals(level) || Level.FINE.equals(level)) {
            return debugClosure(logger);
        } else {
            return traceClosure(logger);
        }
    }

    /**
     * @param logger The {@link Log}-ger instance to use
     * @return A closure whose {@link Closure#execute(Object)} method logs
     * nothing when invoked
     */
    @SuppressWarnings("unchecked")
    public static final <T> Closure<T> nologClosure(final Log logger) {
        if (logger == null) {
            throw new IllegalArgumentException("No logger provided");
        }

        return ClosureUtils.nopClosure();
    }
    /**
     * @param logger The {@link Log}-ger instance to use
     * @return A closure whose {@link Closure#execute(Object)} method logs
     * the {@link String#valueOf(Object)} value of its argument if {@link Log#isErrorEnabled()}
     */
    public static final <T> Closure<T> errorClosure(final Log logger) {
        if (logger == null) {
            throw new IllegalArgumentException("No logger provided");
        }
        
        return new Closure<T>() {
            @Override
            public void execute(T input) {
                if (logger.isErrorEnabled()) {
                    logger.error(String.valueOf(input));
                }
            }
        };
    }

    /**
     * @param logger The {@link Log}-ger instance to use
     * @return A closure whose {@link Closure#execute(Object)} method logs
     * the {@link String#valueOf(Object)} value of its argument if {@link Log#isWarnEnabled()}
     */
    public static final <T> Closure<T> warnClosure(final Log logger) {
        if (logger == null) {
            throw new IllegalArgumentException("No logger provided");
        }
        
        return new Closure<T>() {
            @Override
            public void execute(T input) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.valueOf(input));
                }
            }
        };
    }

    /**
     * @param logger The {@link Log}-ger instance to use
     * @return A closure whose {@link Closure#execute(Object)} method logs
     * the {@link String#valueOf(Object)} value of its argument if {@link Log#isInfoEnabled()}
     */
    public static final <T> Closure<T> infoClosure(final Log logger) {
        if (logger == null) {
            throw new IllegalArgumentException("No logger provided");
        }
        
        return new Closure<T>() {
            @Override
            public void execute(T input) {
                if (logger.isInfoEnabled()) {
                    logger.info(String.valueOf(input));
                }
            }
        };
    }

    /**
     * @param logger The {@link Log}-ger instance to use
     * @return A closure whose {@link Closure#execute(Object)} method logs
     * the {@link String#valueOf(Object)} value of its argument if {@link Log#isDebugEnabled()}
     */
    public static final <T> Closure<T> debugClosure(final Log logger) {
        if (logger == null) {
            throw new IllegalArgumentException("No logger provided");
        }
        
        return new Closure<T>() {
            @Override
            public void execute(T input) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.valueOf(input));
                }
            }
        };
    }

    /**
     * @param logger The {@link Log}-ger instance to use
     * @return A closure whose {@link Closure#execute(Object)} method logs
     * the {@link String#valueOf(Object)} value of its argument if {@link Log#isTraceEnabled()}
     */
    public static final <T> Closure<T> traceClosure(final Log logger) {
        if (logger == null) {
            throw new IllegalArgumentException("No logger provided");
        }
        
        return new Closure<T>() {
            @Override
            public void execute(T input) {
                if (logger.isTraceEnabled()) {
                    logger.trace(String.valueOf(input));
                }
            }
        };
    }
    
    /**
     * Provides a wrapper around a J-U-L logger 
     * @param logger The {@link Logger} instance
     * @return A {@link Log} wrapper
     * @throws IllegalArgumentException if <code>null</code> logger instance to wrap
     */
    public static final Log wrapJULLoggger(final Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("No JUL logger instance");
        }
        
        return new AbstractJULWrapper() {
            @Override
            public boolean isEnabled(Level level) {
                return logger.isLoggable(level);
            }

            @Override
            public void log(Level level, Object message, Throwable t) {
                if (logger.isLoggable(level)) {
                    if (t == null) {
                        logger.log(level, String.valueOf(message));
                    } else {
                        logger.log(level, String.valueOf(message), t);
                    }
                }
            }
        };
    }
}
