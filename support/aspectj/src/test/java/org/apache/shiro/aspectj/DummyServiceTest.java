package org.apache.shiro.aspectj;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author J-C Desrochers
 * @author Kalle Korhonen
 */
public class DummyServiceTest {

    private static DummyService SECURED_SERVICE;
    private static DummyService RESTRICTED_SERVICE;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger log = Logger.getLogger(AspectjAnnotationsAuthorizingMethodInterceptor.class);
        log.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        log.setLevel(Level.TRACE);

        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiroDummyServiceTest.ini");
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);

        SECURED_SERVICE = new SecuredDummyService();
        RESTRICTED_SERVICE = new RestrictedDummyService();
    }

    private Subject subject;

    @Before
    public void setUp() throws Exception {
        subject = SecurityUtils.getSubject();
    }

    @After
    public void tearDown() throws Exception {
        subject.logout();
    }


    private void loginAsGuest() {
        subject.login(new UsernamePasswordToken("john", "doe"));
    }

    private void loginAsUser() {
        subject.login(new UsernamePasswordToken("joe", "bob"));
    }

    private void loginAsAdmin() {
        subject.login(new UsernamePasswordToken("root", "secret"));
    }


    // TEST ANONYMOUS

    @Test
    public void testAnonymous_asAnonymous() throws Exception {
        SECURED_SERVICE.anonymous();
    }

    @Test
    public void testAnonymous_asGuest() throws Exception {
        loginAsGuest();
        SECURED_SERVICE.anonymous();
    }

    @Test
    public void testAnonymous_asUser() throws Exception {
        loginAsUser();
        SECURED_SERVICE.anonymous();
    }

    @Test
    public void testAnonymous_asAdmin() throws Exception {
        loginAsAdmin();
        SECURED_SERVICE.anonymous();
    }


    // TEST GUEST

    @Test(expected = UnauthenticatedException.class)
    public void testGuest_asAnonymous() throws Exception {
        SECURED_SERVICE.guest();
    }

    @Test
    public void testGuest_asGuest() throws Exception {
        loginAsGuest();
        SECURED_SERVICE.guest();
    }

    @Test
    public void testGuest_asUser() throws Exception {
        loginAsUser();
        SECURED_SERVICE.guest();
    }

    @Test
    public void testGuest_asAdmin() throws Exception {
        loginAsAdmin();
        SECURED_SERVICE.guest();
    }


    // TEST PEEK

    @Test(expected = UnauthenticatedException.class)
    public void testPeek_asAnonymous() throws Exception {
        SECURED_SERVICE.peek();
    }

    @Test
    public void testPeek_asGuest() throws Exception {
        loginAsGuest();
        SECURED_SERVICE.peek();
    }

    @Test
    public void testPeek_asUser() throws Exception {
        loginAsUser();
        SECURED_SERVICE.peek();
    }

    @Test
    public void testPeek_asAdmin() throws Exception {
        loginAsAdmin();
        SECURED_SERVICE.peek();
    }


    // TEST RETRIEVE

    @Test(expected = UnauthenticatedException.class)
    //UnauthenticatedException per SHIRO-146
    public void testRetrieve_asAnonymous() throws Exception {
        SECURED_SERVICE.retrieve();
    }

    @Test(expected = UnauthorizedException.class)
    public void testRetrieve_asGuest() throws Exception {
        loginAsGuest();
        SECURED_SERVICE.retrieve();
    }

    @Test
    public void testRetrieve_asUser() throws Exception {
        loginAsUser();
        SECURED_SERVICE.retrieve();
    }

    @Test
    public void testRetrieve_asAdmin() throws Exception {
        loginAsAdmin();
        SECURED_SERVICE.retrieve();
    }


    // TEST CHANGE

    @Test(expected = UnauthenticatedException.class)
    //UnauthenticatedException per SHIRO-146
    public void testChange_asAnonymous() throws Exception {
        SECURED_SERVICE.change();
    }

    @Test(expected = UnauthorizedException.class)
    public void testChange_asGuest() throws Exception {
        loginAsGuest();
        SECURED_SERVICE.change();
    }

    @Test(expected = UnauthorizedException.class)
    public void testChange_asUser() throws Exception {
        loginAsUser();
        SECURED_SERVICE.change();
    }

    @Test
    public void testChange_asAdmin() throws Exception {
        loginAsAdmin();
        SECURED_SERVICE.change();
    }


    // TEST RETRIEVE RESTRICTED

    @Test(expected = UnauthenticatedException.class)
    //UnauthenticatedException per SHIRO-146
    public void testRetrieveRestricted_asAnonymous() throws Exception {
        RESTRICTED_SERVICE.retrieve();
    }

    @Test(expected = UnauthorizedException.class)
    public void testRetrieveRestricted_asGuest() throws Exception {
        loginAsGuest();
        RESTRICTED_SERVICE.retrieve();
    }

    @Test(expected = UnauthorizedException.class)
    public void testRetrieveRestricted_asUser() throws Exception {
        loginAsUser();
        RESTRICTED_SERVICE.retrieve();
    }

    @Test
    public void testRetrieveRestricted_asAdmin() throws Exception {
        loginAsAdmin();
        RESTRICTED_SERVICE.retrieve();
    }

}
