/*
 * Copyright (C) 2005 Jeremy C. Haile
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the
 *
 * Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330
 * Boston, MA 02111-1307
 * USA
 *
 * Or, you may view it online at
 * http://www.opensource.org/licenses/lgpl-license.php
 */

package org.jsecurity.ri.authc.module.dao;

import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.ExpiredCredentialException;
import org.jsecurity.authc.IncorrectCredentialException;
import org.jsecurity.authc.LockedAccountException;
import org.jsecurity.authc.UnknownAccountException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authc.module.AuthenticationModule;
import org.jsecurity.authz.AuthorizationContext;
import org.jsecurity.ri.authc.password.PasswordMatcher;
import org.jsecurity.ri.authc.password.PlainTextPasswordMatcher;
import org.jsecurity.ri.authz.SimpleAuthorizationContext;
import org.jsecurity.ri.util.StringPrincipal;

import java.io.Serializable;
import java.security.Permission;
import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Module that authenticates a user by delegating the lookup of
 * authentication and authorization information to an {@link AuthenticationDAO}.
 * Users of JSecurity can create their own DAO, or use one of the provided
 * DAO implementations.</p>
 *
 * <p>This module is intended to encapsulate the generic behavior of
 * authenticating a user from a username and password based on the
 * {@link AuthenticationInfo} retrieved from the {@link AuthenticationDAO}.</p>
 *
 * @since 0.1
 * @author Jeremy Haile
 */
public class DAOAuthenticationModule implements AuthenticationModule {

    /*--------------------------------------------
    |             C O N S T A N T S             |
    ============================================*/

    /*--------------------------------------------
    |    I N S T A N C E   V A R I A B L E S    |
    ============================================*/
    /**
     * The DAO used to retrieve user authentication and authorization
     * information from a data store.
     */
    private AuthenticationDAO authenticationDao;

    /**
     * Password matcher used to determine if the provided password matches
     * the password stored in the data store.
     */
    private PasswordMatcher passwordMatcher = new PlainTextPasswordMatcher();

    /*--------------------------------------------
    |         C O N S T R U C T O R S           |
    ============================================*/

    /*--------------------------------------------
    |  A C C E S S O R S / M O D I F I E R S    |
    ============================================*/
    public void setAuthenticationDao(AuthenticationDAO authenticationDao) {
        this.authenticationDao = authenticationDao;
    }

    public void setPasswordMatcher(PasswordMatcher passwordMatcher) {
        this.passwordMatcher = passwordMatcher;
    }


    /*--------------------------------------------
    |               M E T H O D S               |
    ============================================*/

    public boolean supports(Class tokenClass) {
        return UsernamePasswordToken.class.isAssignableFrom( tokenClass );
    }


    public AuthorizationContext authenticate(AuthenticationToken token) throws AuthenticationException {

        Principal accountIdentifier = getPrincipal( token );
        Object credentials = getCredentials( token );

        AuthenticationInfo info;
        try {
            info = authenticationDao.getAuthenticationInfo( accountIdentifier );
        } catch (Exception e) {
            throw new AuthenticationException(
                "Account [" + accountIdentifier + "] could not be authenticated because an error " +
                "occurred during authentication.", e );
        }

        if( info == null ) {
            String msg = "No account information found for account [" + accountIdentifier + "]";
            throw new UnknownAccountException( msg );
        }

        if( info.isAccountLocked() ) {
            throw new LockedAccountException( "Account [" + accountIdentifier + "] is locked." );
        }

        if( info.isCredentialsExpired() ) {
            String msg = "The credentials for account [" + accountIdentifier + "] are expired";
            throw new ExpiredCredentialException( msg );
        }

        if( !passwordMatcher.doPasswordsMatch( (char[])credentials, (char[])info.getCredentials() ) ) {
            String msg = "The credentials provided for account [" +
                         accountIdentifier + "] did not match the expected credentials.";
            throw new IncorrectCredentialException( msg );
        }

        return buildAuthorizationContext( info );
    }


    protected AuthorizationContext buildAuthorizationContext(AuthenticationInfo info) {

        return new SimpleAuthorizationContext( info.getPrincipal(),
                                               info.getRoles(),
                                               info.getPermissions());
    }


    private Set<Permission> getPermissionsForRoles(Collection<? extends Serializable> roles) {
        return new HashSet<Permission>();
    }


    protected Principal getPrincipal(AuthenticationToken token) {
        return new StringPrincipal( ((UsernamePasswordToken)token).getUsername() );
    }

    private Object getCredentials(AuthenticationToken token) {
        return ((UsernamePasswordToken)token).getPassword();
    }



}