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
package org.apache.shiro.io;

import org.apache.shiro.ShiroException;


/**
 * Root exception when a problem occurs acquiring or processing a resource.
 * <p/>
 * <p/>
 * <b>Do not use this! It will be removed prior to 1.0 final!</b>
 *
 * @author Les Hazlewood
 * @since 0.9
 * @deprecated use {@link org.apache.shiro.util.Factory} implementations to generate the Shiro
 *             components. See {@link org.apache.shiro.config.IniSecurityManagerFactory} as an example.
 *             <b>Will be removed prior to 1.0 final!</b>
 */
@Deprecated
public class ResourceException extends ShiroException {

    /**
     * Creates a new ResourceException.
     */
    public ResourceException() {
        super();
    }

    /**
     * Constructs a new ResourceException.
     *
     * @param message the reason for the exception
     */
    public ResourceException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceException.
     *
     * @param cause the underlying Throwable that caused this exception to be thrown.
     */
    public ResourceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ResourceException.
     *
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be thrown.
     */
    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
