/*
 * Copyright 2002-2012 the original author or authors.
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

package org.apache.sshd.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.AbstractLoggingBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.server.session.ServerSession;

/**
 * Useful base class for {@link Command} implementations
 * @author Lyor G.
 * @since Aug 29, 2013 9:21:12 AM
 */
public abstract class AbstractCommand
        extends AbstractLoggingBean
        implements Command, SessionAware {

    private InputStream _in;
    private OutputStream    _out, _err;
    private ExitCallback    _cbExit;
    private ServerSession   _session;

    protected AbstractCommand() {
        super();
    }

    protected AbstractCommand(Log log) {
        super(log);
    }

    protected AbstractCommand(Class<?> index) {
        super(index);
    }

    protected AbstractCommand(String index) {
        super(index);
    }

    protected AbstractCommand(LogFactory factory) {
        super(factory);
    }

    protected AbstractCommand(LogFactory factory, Class<?> index) {
        super(factory, index);
    }

    protected AbstractCommand(LogFactory factory, String index) {
        super(factory, index);
    }

    public InputStream getInputStream() {
        return _in;
    }

    @Override
    public void setInputStream(InputStream in) {
        _in = in;
    }

    public OutputStream getOutputStream() {
        return _out;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        _out = out;
    }

    public OutputStream getErrorStream() {
        return _err;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        _err = err;
    }

    public ExitCallback getExitCallback() {
        return _cbExit;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        _cbExit = callback;
    }

    public ServerSession getSession() {
        return _session;
    }

    @Override
    public void setSession(ServerSession session) {
        _session = session;
    }

    @Override
    public void destroy() {
        // remove the references so that GC may occur sooner
        if (_in != null) {
            _in = null; // debug breakpoint
        }
        
        if (_out != null) {
            _out = null; // debug breakpoint
        }
        
        if (_err != null) {
            _err = null; // debug breakpoint
        }
        
        if (_cbExit != null) {
            _cbExit = null; // debug breakpoint
        }

        if (_session != null) {
            _session = null;    // debug breakpoint
        }

        if (logger.isDebugEnabled()) {
            logger.debug("destroy()");
        }
    }

    /**
     * Useful method for converting non-{@link IOException}s that may occur
     * during the command's execution into such
     * @param t The original {@link Throwable}
     * @return If already an {@link IOException} then simply return the same
     * (cast) instance, otherwise wrap it inside an {@link IOException}
     */
    protected IOException toIOException(Throwable t) {
        if (t instanceof IOException) {
            return (IOException) t;
        } else {
            return new IOException(t.getClass().getSimpleName() + ": " + t.getMessage(), t);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
