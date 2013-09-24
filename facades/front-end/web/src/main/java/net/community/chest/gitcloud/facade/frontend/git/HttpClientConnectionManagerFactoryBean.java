/* Copyright 2013 Lyor Goldstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.community.chest.gitcloud.facade.frontend.git;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.AbstractLoggingBean;
import org.apache.http.HttpClientConnection;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.ConnPoolControl;
import org.apache.http.pool.PoolStats;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;
import org.springframework.util.SystemPropertyUtils;

/**
 * @author Lyor Goldstein
 * @since Sep 24, 2013 2:53:58 PM
 */
@Component
@ManagedResource(objectName="net.community.chest.gitcloud.facade.frontend.git:name=HttpClientConnectionManagerFactoryBean")
public class HttpClientConnectionManagerFactoryBean
           extends AbstractLoggingBean
           implements FactoryBean<HttpClientConnectionManager>, DisposableBean, ConnPoolControl<HttpRoute> {
    public static final String  TTL_SEC_CONFIG_PROP="gitcloud.frontend.git.http.conn.manager.ttl.sec";
        public static final long    DEFAULT_TTL_SEC_VALUE=30L;
        private static final String TTL_CONFIG_VALUE=SystemPropertyUtils.PLACEHOLDER_PREFIX
                                            + TTL_SEC_CONFIG_PROP
                                            + SystemPropertyUtils.VALUE_SEPARATOR
                                            + DEFAULT_TTL_SEC_VALUE
                                            + SystemPropertyUtils.PLACEHOLDER_SUFFIX;
    public static final String  MAX_PER_ROUTE_CONFIG_PROP="gitcloud.frontend.git.http.conn.manager.max.per.route";
        public static final long    DEFAULT_MAX_PER_ROUTE=32;
        private static final String MAX_PER_ROUTE_CONFIG_VALUE=SystemPropertyUtils.PLACEHOLDER_PREFIX
                                            + MAX_PER_ROUTE_CONFIG_PROP
                                            + SystemPropertyUtils.VALUE_SEPARATOR
                                            + DEFAULT_MAX_PER_ROUTE
                                            + SystemPropertyUtils.PLACEHOLDER_SUFFIX;
    public static final String  MAX_TOTAL_CONFIG_PROP="gitcloud.frontend.git.http.conn.manager.max.total";
        public static final long    DEFAULT_MAX_TOTAL=DEFAULT_MAX_PER_ROUTE * Byte.SIZE;
        private static final String MAX_TOTAL_CONFIG_VALUE=SystemPropertyUtils.PLACEHOLDER_PREFIX
                                            + MAX_TOTAL_CONFIG_PROP
                                            + SystemPropertyUtils.VALUE_SEPARATOR
                                            + DEFAULT_MAX_TOTAL
                                            + SystemPropertyUtils.PLACEHOLDER_SUFFIX;

    private final PoolingHttpClientConnectionManager   manager;

    @Inject
    public HttpClientConnectionManagerFactoryBean(
            @Value(TTL_CONFIG_VALUE) long ttlSeconds,
            @Value(MAX_PER_ROUTE_CONFIG_VALUE) int defaultMaxPerRoute,
            @Value(MAX_TOTAL_CONFIG_VALUE) int maxTotal) {
        Validate.isTrue(ttlSeconds > 0L, "Invalid TTL value: %d", ttlSeconds);
        Validate.isTrue(defaultMaxPerRoute > 0, "Invalid max. per-route value: %d", defaultMaxPerRoute);
        Validate.isTrue(maxTotal > 0, "Invalid max. total value: %d", maxTotal);

        // TODO use a different connections registry with an all-trusting HTTPS socket factory
        // TODO log the total stats and/or pre-route one at DEBUG/INFO level every few minutes/seconds or every N requests
        manager = new PoolingHttpClientConnectionManager(ttlSeconds, TimeUnit.SECONDS) {
                @Override
                @SuppressWarnings("synthetic-access")
                public ConnectionRequest requestConnection(final HttpRoute route, Object state) {
                    final ConnectionRequest   req=super.requestConnection(route, state);
                    if (logger.isTraceEnabled()) {
                        return new ConnectionRequest() {
                            @Override
                            public boolean cancel() {
                                logger.trace("requestConnection(" + route + ") cancelling");
                                return req.cancel();
                            }
                            
                            @Override
                            public HttpClientConnection get(long timeout, TimeUnit tunit)
                                    throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
                                HttpClientConnection    conn=req.get(timeout, tunit);
                                logger.trace("requestConnection(" + route + ")[" + timeout + " " + tunit + "]: " + conn);
                                return conn;
                            }
                        };
                    }
                    
                    return req;
                }
    
                @Override
                @SuppressWarnings("synthetic-access")
                public void releaseConnection(HttpClientConnection managedConn, Object state, long keepalive, TimeUnit tunit) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("releaseConnection(" + keepalive + " " + tunit + "]: " + managedConn);
                    }
                    super.releaseConnection(managedConn, state, keepalive, tunit);
                }

                @Override
                @SuppressWarnings("synthetic-access")
                public void connect(HttpClientConnection managedConn, HttpRoute route, int connectTimeout, HttpContext context)
                        throws IOException {
                    if (logger.isTraceEnabled()) {
                        logger.trace("connect(" + route + ")[timeout=" + connectTimeout + "]: " + managedConn);
                    }
                    super.connect(managedConn, route, connectTimeout, context);
                }
            };
        manager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        manager.setMaxTotal(maxTotal);
    }

    @Override
    @ManagedAttribute(description="Max. allowed concurrent HTTP connections in total")
    public int getMaxTotal() {
        return manager.getMaxTotal();
    }

    @Override
    public void setMaxTotal(int max) {
        logger.info("setMaxTotal(" + max + ")");
        manager.setMaxTotal(max);
    }

    @Override
    @ManagedAttribute(description="Max. allowed concurrent HTTP connections per route")
    public int getDefaultMaxPerRoute() {
        return manager.getDefaultMaxPerRoute();
    }

    @Override
    public void setDefaultMaxPerRoute(int max) {
        logger.info("setDefaultMaxPerRoute(" + max + ")");
        manager.setDefaultMaxPerRoute(max);
    }

    @Override
    public int getMaxPerRoute(HttpRoute route) {
        return manager.getMaxPerRoute(route);
    }

    @Override
    public void setMaxPerRoute(HttpRoute route, int max) {
        logger.info("setMaxPerRoute(" + route + ")[" + max + "]");
        manager.setMaxPerRoute(route, max);
    }

    @Override
    public PoolStats getTotalStats() {
        return manager.getTotalStats();
    }

    @Override
    public PoolStats getStats(HttpRoute route) {
        return manager.getStats(route);
    }

    @Override
    public void destroy() throws Exception {
        HttpClientConnectionManager mgr=getObject();
        logger.info("destroy()");
        mgr.shutdown();
    }

    @Override
    public HttpClientConnectionManager getObject() throws Exception {
        return manager;
    }

    @Override
    public Class<?> getObjectType() {
        return HttpClientConnectionManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
