/*
 * Copyright (C) 2005 Jeremy Haile, Les Hazlewood
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

package org.jsecurity.authz.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>
 * Requires the current executor to have a particular role in order to execute the
 * annotated method.  If the executor's associated
 * {@link org.jsecurity.authz.AuthorizationContext AuthorizationContext} determines that the
 * executor does not have the specified role, the method will not be executed.
 * </p>
 * For example,<br>
 * <blockquote><pre>
 * &#64;HasRole("myRoleName")
 * void someMethod();
 * </pre>
 * </blockquote>
 *
 * <b>*Usage Note*:</b> You should not use this annotation if your application has a <em>dynamic</em>
 * security model and the annotated role might be deleted.  That is, if your application allows the
 * creation and deletion of groups and/or roles <em>during runtime</em>, this annotation might not
 * make sense - the annotated Role may be deleted.
 *
 * <p>If you require such dynamic functionality, only the
 * {@link Implies Implies} annotation makes sense - Permission configuration does not change for
 * an application since permissions directly correspond to how the application's functionality is
 * programmed.
 *
 * @see org.jsecurity.authz.AuthorizationContext#hasRole(String)
 *
 * @since 0.1
 * @author Jeremy Haile
 * @author Les Hazlewood
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HasRole {

    /**
     * The name of the role required to be granted this authorization.
     */
    String value();

}

