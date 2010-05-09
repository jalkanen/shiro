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
package org.apache.shiro.session.mgt;

import org.apache.shiro.session.*;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.util.ThreadContext;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Unit test for the {@link DefaultSessionManager DefaultSessionManager} implementation.
 */
public class DefaultSessionManagerTest {

    DefaultSessionManager sm = null;

    @Before
    public void setup() {
        ThreadContext.clear();
        sm = new DefaultSessionManager();
    }

    @After
    public void tearDown() {
        sm.destroy();
        ThreadContext.clear();
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void testGlobalTimeout() {

        long timeout = 200;

        //use the dao to simulate returned Sessions that are timed out:
        SessionDAO mockDAO = createMock(SessionDAO.class);
        Session mockSession = createNiceMock(Session.class);

        Serializable mockSessionId = UUID.randomUUID().toString();
        expect(mockSession.getId()).andReturn(mockSessionId).anyTimes();
        expect(mockDAO.create(isA(Session.class))).andReturn(mockSessionId);

        //this line verifies this test case - ensuring the DAO is called when an updated timeout:
        mockDAO.update(eqSessionTimeout(timeout));

        replay(mockDAO);

        sm.setSessionDAO(mockDAO);

        sm.setGlobalSessionTimeout(timeout);
        sm.start((String) null);

        verify(mockDAO);
    }

    @Test
    public void testSessionListenerStartNotification() {
        final boolean[] started = new boolean[1];
        SessionListener listener = new SessionListenerAdapter() {
            public void onStart(Session session) {
                started[0] = true;
            }
        };
        sm.getSessionListeners().add(listener);
        sm.start((Map) null);
        assertTrue(started[0]);
    }

    @Test
    public void testSessionListenerStopNotification() {
        final boolean[] stopped = new boolean[1];
        SessionListener listener = new SessionListenerAdapter() {
            public void onStop(Session session) {
                stopped[0] = true;
            }
        };
        sm.getSessionListeners().add(listener);
        Serializable id = sm.start((Map) null);
        sm.stop(id);
        assertTrue(stopped[0]);
    }

    @Test
    public void testSessionListenerExpiredNotification() {
        final boolean[] expired = new boolean[1];
        SessionListener listener = new SessionListenerAdapter() {
            public void onExpiration(Session session) {
                expired[0] = true;
            }
        };
        sm.getSessionListeners().add(listener);
        sm.setGlobalSessionTimeout(100);
        Serializable id = sm.start((Map) null);
        sleep(150);
        try {
            sm.checkValid(id);
            fail("check should have thrown an exception.");
        } catch (InvalidSessionException expected) {
            //do nothing - expected.
        }
        assertTrue(expired[0]);
    }

    @Test
    public void testSessionDeleteOnExpiration() {
        sm.setGlobalSessionTimeout(100);

        SessionDAO sessionDAO = createMock(SessionDAO.class);
        sm.setSessionDAO(sessionDAO);

        String sessionId1 = UUID.randomUUID().toString();
        final SimpleSession session1 = new SimpleSession();
        session1.setId(sessionId1);

        final Session[] activeSession = new SimpleSession[]{session1};
        sm.setSessionFactory(new SessionFactory() {
            public Session createSession(Map initData) {
                return activeSession[0];
            }
        });

        expect(sessionDAO.create(eq(session1))).andReturn(sessionId1);
        sessionDAO.update(eq(session1));
        expectLastCall().anyTimes();
        replay(sessionDAO);
        Serializable id = sm.start((String) null);
        assertNotNull(id);
        verify(sessionDAO);
        reset(sessionDAO);

        expect(sessionDAO.readSession(sessionId1)).andReturn(session1).anyTimes();
        sessionDAO.update(eq(session1));
        replay(sessionDAO);
        sm.setTimeout(sessionId1, 1);
        verify(sessionDAO);
        reset(sessionDAO);

        sleep(20);

        expect(sessionDAO.readSession(sessionId1)).andReturn(session1);
        sessionDAO.update(eq(session1)); //update's the stop timestamp
        sessionDAO.delete(session1);
        replay(sessionDAO);

        //Try to access the same session, but it should throw an UnknownSessionException due to timeout:
        try {
            sm.getTimeout(sessionId1);
            fail("Session with id [" + sessionId1 + "] should have expired due to timeout.");
        } catch (ExpiredSessionException expected) {
            //expected
        }

        verify(sessionDAO); //verify that the delete call was actually made on the DAO
    }

    public static <T extends Session> T eqSessionTimeout(long timeout) {
        EasyMock.reportMatcher(new SessionTimeoutMatcher(timeout));
        return null;
    }

    private static class SessionTimeoutMatcher implements IArgumentMatcher {

        private final long timeout;

        public SessionTimeoutMatcher(long timeout) {
            this.timeout = timeout;
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("eqSession(timeout=").append(this.timeout).append(")");
        }

        public boolean matches(Object o) {
            return o instanceof Session && ((Session) o).getTimeout() == this.timeout;
        }
    }
}
