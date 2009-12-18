/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shiro.mgt;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.cache.CacheManagerAware;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.SessionListenerRegistrar;
import org.apache.shiro.session.mgt.*;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAOAware;
import org.apache.shiro.util.LifecycleUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;


/**
 * Shiro support of a {@link SecurityManager} class hierarchy that delegates all
 * {@link org.apache.shiro.session.Session session} operations to a wrapped {@link org.apache.shiro.session.mgt.SessionManager SessionManager}
 * instance.  That is, this class implements the methods in the
 * {@link SessionManager SessionManager} interface, but in reality, those methods are merely passthrough calls to
 * the underlying 'real' {@code SessionManager} instance.
 * <p/>
 * The remaining {@code SecurityManager} methods not implemented by this class or its parents are left to be
 * implemented by subclasses.
 * <p/>
 * In keeping with the other classes in this hierarchy and Shiro's desire to minimize configuration whenever
 * possible, suitable default instances for all dependencies will be created upon instantiation.
 *
 * @author Les Hazlewood
 * @since 0.9
 */
public abstract class SessionsSecurityManager extends AuthorizingSecurityManager
        implements SessionListenerRegistrar, SessionFactoryAware, SessionDAOAware {

    /**
     * The internal delegate <code>SessionManager</code> used by this security manager that manages all the
     * application's {@link Session Session}s.
     */
    private SessionManager sessionManager;

    /**
     * Default no-arg constructor, internally creates a suitable default {@link SessionManager SessionManager} delegate
     * instance.
     */
    public SessionsSecurityManager() {
        super();
        this.sessionManager = new DefaultSessionManager();
        applyCacheManagerToSessionManager();
    }

    /**
     * Sets the underlying delegate {@link SessionManager} instance that will be used to support this implementation's
     * <tt>SessionManager</tt> method calls.
     * <p/>
     * This <tt>SecurityManager</tt> implementation does not provide logic to support the inherited
     * <tt>SessionManager</tt> interface, but instead delegates these calls to an internal
     * <tt>SessionManager</tt> instance.
     * <p/>
     * If a <tt>SessionManager</tt> instance is not set, a default one will be automatically created and
     * initialized appropriately for the the existing runtime environment.
     *
     * @param sessionManager delegate instance to use to support this manager's <tt>SessionManager</tt> method calls.
     */
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        afterSessionManagerSet();
    }

    protected void afterSessionManagerSet() {
        applyCacheManagerToSessionManager();
    }

    /**
     * Returns this security manager's internal delegate {@link SessionManager SessionManager}.
     *
     * @return this security manager's internal delegate {@link SessionManager SessionManager}.
     * @see #setSessionManager(org.apache.shiro.session.mgt.SessionManager) setSessionManager
     */
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

    /**
     * Calls {@link org.apache.shiro.mgt.AuthorizingSecurityManager#afterCacheManagerSet() super.afterCacheManagerSet()} and then immediately calls
     * {@link #applyCacheManagerToSessionManager() applyCacheManagerToSessionManager()} to ensure the
     * <code>CacheManager</code> is applied to the SessionManager as necessary.
     */
    protected void afterCacheManagerSet() {
        super.afterCacheManagerSet();
        applyCacheManagerToSessionManager();
    }

    /**
     * @since 1.0
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        SessionManager sm = getSessionManager();
        if (sm instanceof SessionFactoryAware) {
            ((SessionFactoryAware) sm).setSessionFactory(sessionFactory);
        } else {
            String msg = "The underlying session manager is null or does not implement the " +
                    SessionFactory.class.getName() + " interface, which is required if the underlying " +
                    "instance is to receive the sessionFactory argument.";
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * @since 1.0
     */
    public void setSessionDAO(SessionDAO sessionDAO) {
        SessionManager sm = getSessionManager();
        if (sm instanceof SessionDAOAware) {
            ((SessionDAOAware) sm).setSessionDAO(sessionDAO);
        } else {
            String msg = "The underlying SessionManager is null or does not implement the " +
                    SessionDAOAware.class.getName() + " interface, which is required if it is " +
                    "to receive the sessionDAO argument.";
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Ensures the internal delegate <code>SessionManager</code> is injected with the newly set
     * {@link #setCacheManager CacheManager} so it may use it for its internal caching needs.
     * <p/>
     * Note:  This implementation only injects the CacheManager into the SessionManager if the SessionManager
     * instance implements the {@link CacheManagerAware CacheManagerAware} interface.
     */
    protected void applyCacheManagerToSessionManager() {
        if (this.sessionManager instanceof CacheManagerAware) {
            ((CacheManagerAware) this.sessionManager).setCacheManager(getCacheManager());
        }
    }

    /**
     * This is a convenience method that allows registration of SessionListeners with the underlying delegate
     * SessionManager at startup.
     * <p/>
     * This is more convenient than having to configure your own SessionManager instance, inject the listeners on
     * it, and then set that SessionManager instance as an attribute of this class.  Instead, you can just rely
     * on the <tt>SecurityManager</tt> to apply these <tt>SessionListener</tt>s on your behalf.
     * <p/>
     * One notice however: The underlying SessionManager delegate must implement the
     * {@link SessionListenerRegistrar SessionListenerRegistrar} interface in order for these listeners to
     * be applied.  If it does not implement this interface, it is considered a configuration error and an exception
     * will be thrown.
     *
     * @param sessionListeners the <tt>SessionListener</tt>s to register with the underlying delegate
     *                         <tt>SessionManager</tt> at startup.
     */
    public void setSessionListeners(Collection<SessionListener> sessionListeners) {
        assertSessionListenerSupport();
        ((SessionListenerRegistrar) this.sessionManager).setSessionListeners(sessionListeners);
    }

    private void assertSessionManager(Class<? extends SessionManager> requiredType) {
        if (this.sessionManager == null) {
            throw new IllegalStateException("SessionManager is null - cannot configure property!");
        }
        if (!(requiredType.isInstance(this.sessionManager))) {
            String msg = "Property configuration failed.  The target property is only configurable when the " +
                    "underlying SessionManager instance is a part of the " +
                    "[" + requiredType.getName() + "] class hierarchy.  " +
                    "The current SessionManager is of type [" + this.sessionManager.getClass().getName() + "].  " +
                    "This might occur for example if you're trying to set the validation interval or auto session " +
                    "creation in a servlet container-backed session environment ('http' session mode).  If that is " +
                    "the case however, that property is only useful when using 'native' session mode and using " +
                    "Shiro enterprise sessions which do not rely on a servlet container.";
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Passthrough configuration property to the underlying {@link AbstractSessionManager AbstractSessionManager}
     * instance.  Please read the
     * {@link org.apache.shiro.session.mgt.AbstractSessionManager#getGlobalSessionTimeout() AbstractSessionManager.getGlobalSessionTimeout()}
     * for more.
     *
     * @return the time in milliseconds that any {@link Session Session} may remain idle before expiring.
     * @throws IllegalStateException if the underlying {@code SessionManager} instance is not a subclass of
     *                               {@link AbstractSessionManager AbstractSessionManager}.
     * @see org.apache.shiro.session.mgt.AbstractSessionManager#getGlobalSessionTimeout()
     */
    public long getGlobalSessionTimeout() {
        assertSessionManager(AbstractSessionManager.class);
        return ((AbstractSessionManager) this.sessionManager).getGlobalSessionTimeout();
    }

    /**
     * Passthrough configuration property to the underlying {@link AbstractSessionManager AbstractSessionManager}
     * instance.  Please read the
     * {@link org.apache.shiro.session.mgt.AbstractSessionManager#setGlobalSessionTimeout(long) AbstractSessionManager.setGlobalSessionTimeout(long)}
     * for more.
     *
     * @param globalSessionTimeout the time in milliseconds that any {@link Session Session} may remain idle before expiring.
     * @throws IllegalStateException if the underlying {@code SessionManager} instance is not a subclass of
     *                               {@link org.apache.shiro.session.mgt.AbstractSessionManager AbstractSessionManager}.
     * @see org.apache.shiro.session.mgt.AbstractSessionManager#setGlobalSessionTimeout(long)
     */
    public void setGlobalSessionTimeout(long globalSessionTimeout) {
        assertSessionManager(AbstractSessionManager.class);
        ((AbstractSessionManager) this.sessionManager).setGlobalSessionTimeout(globalSessionTimeout);
    }

    /**
     * Ensures the internal SessionManager instance is an <code>instanceof</code>
     * {@link org.apache.shiro.session.SessionListenerRegistrar SessionListenerRegistrar} to ensure that any
     * listeners attempting to be registered can actually do so with the internal delegate instance.
     *
     * @throws IllegalStateException if the internal delegate SessionManager instance does not implement the
     *                               <code>SessionListenerRegistrar</code> interface.
     */
    private void assertSessionListenerSupport() throws IllegalStateException {
        if (!(this.sessionManager instanceof SessionListenerRegistrar)) {
            String msg = "SessionListener registration failed:  The underlying SessionManager instance of " +
                    "type [" + sessionManager.getClass().getName() + "] does not implement the " +
                    SessionListenerRegistrar.class.getName() + " interface and therefore cannot support " +
                    "session notifications.";
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Asserts the internal delegate <code>SessionManager</code> instance
     * {@link #assertSessionListenerSupport() supports session listener registration} and then
     * {@link SessionListenerRegistrar#add adds} the listener to the
     * delegate instance.
     *
     * @param listener the <code>SessionListener</code> to register for session events.
     */
    public void add(SessionListener listener) {
        assertSessionListenerSupport();
        ((SessionListenerRegistrar) this.sessionManager).add(listener);
    }

    /**
     * Removes the specified listener from receiving session events from the internal delegate
     * {@link org.apache.shiro.session.mgt.SessionManager} instance.
     *
     * @param listener the listener to remove that no longer wishes to be notified of session events.
     * @return <code>true</code> if the listener was removed from the internal delegate <code>SessionManager</code>
     *         instance, <code>false</code> otherwise.
     */
    public boolean remove(SessionListener listener) {
        return (this.sessionManager instanceof SessionListenerRegistrar) &&
                ((SessionListenerRegistrar) this.sessionManager).remove(listener);
    }

    public Serializable start(String host) throws AuthorizationException {
        return this.sessionManager.start(host);
    }

    public Serializable start(Map initData) throws AuthorizationException {
        return this.sessionManager.start(initData);
    }

    public Date getStartTimestamp(Serializable sessionId) {
        return this.sessionManager.getStartTimestamp(sessionId);
    }

    public Date getLastAccessTime(Serializable sessionId) {
        return this.sessionManager.getLastAccessTime(sessionId);
    }

    public boolean isValid(Serializable sessionId) {
        return this.sessionManager.isValid(sessionId);
    }

    public void checkValid(Serializable sessionId) throws InvalidSessionException {
        this.sessionManager.checkValid(sessionId);
    }

    public long getTimeout(Serializable sessionId) throws InvalidSessionException {
        return this.sessionManager.getTimeout(sessionId);
    }

    public void setTimeout(Serializable sessionId, long maxIdleTimeInMillis) throws InvalidSessionException {
        this.sessionManager.setTimeout(sessionId, maxIdleTimeInMillis);
    }

    public void touch(Serializable sessionId) throws InvalidSessionException {
        this.sessionManager.touch(sessionId);
    }

    public String getHost(Serializable sessionId) {
        return this.sessionManager.getHost(sessionId);
    }

    public void stop(Serializable sessionId) throws InvalidSessionException {
        this.sessionManager.stop(sessionId);
    }

    public Collection<Object> getAttributeKeys(Serializable sessionId) {
        return this.sessionManager.getAttributeKeys(sessionId);
    }

    public Object getAttribute(Serializable sessionId, Object key) throws InvalidSessionException {
        return this.sessionManager.getAttribute(sessionId, key);
    }

    public void setAttribute(Serializable sessionId, Object key, Object value) throws InvalidSessionException {
        this.sessionManager.setAttribute(sessionId, key, value);
    }

    public Object removeAttribute(Serializable sessionId, Object key) throws InvalidSessionException {
        return this.sessionManager.removeAttribute(sessionId, key);
    }

    public void destroy() {
        LifecycleUtils.destroy(getSessionManager());
        this.sessionManager = null;
    }
}
