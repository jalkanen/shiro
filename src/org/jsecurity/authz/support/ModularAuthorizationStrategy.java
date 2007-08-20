/*
 * Copyright (C) 2005-2007 Jeremy Haile
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

package org.jsecurity.authz.support;

import org.jsecurity.authz.AuthorizedAction;
import org.jsecurity.context.SecurityContext;

import java.util.Map;


/**
 * Strategy that determines whether or not a user is authorized based on the
 * votes of all {@link AuthorizationModule}s provided by
 * the {@link ModularRealmAuthorizer}.
 *
 * @since 0.1
 * @author Jeremy Haile
 */
public interface ModularAuthorizationStrategy {

    /**
     * Determines if a user is authorized to perform the given action based
     * on a set of {@link AuthorizationVote}s that were returned from a
     * set of {@link AuthorizationModule}s.
     *
     * @param context the context of the user being authorized.
     * @param action the action that the user is requesting authorization for.
     * @param votes the votes returned by {@link AuthorizationModule}s.
     * @return true if the user should be authorized based on the votes, or
     * false otherwise.
     */
    boolean isAuthorized( SecurityContext context,
                          AuthorizedAction action,
                          Map<AuthorizationModule, AuthorizationVote> votes );
    
}
