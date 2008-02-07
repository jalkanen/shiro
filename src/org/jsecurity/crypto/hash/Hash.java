/*
 * Copyright (C) 2005-2008 Les Hazlewood
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
package org.jsecurity.crypto.hash;

/**
 * @author Les Hazlewood
 * @since 1.0
 */
public interface Hash {

    /**
     * Returns this Hash's byte array, that is, the hashed value of the original input source.
     * @return this Hash's byte array, that is, the hashed value of the original input source.
     * @see #toHex
     * @see #toBase64
     */
    byte[] getBytes();

    /**
     * Returns a Hex encoding of this Hash's {@link #getBytes byte array}.
     * @return a Hex encoding of this Hash's {@link #getBytes byte array}.
     */
    String toHex();

    /**
     * Returns a Base65 encoding of this Hash's {@link #getBytes byte array}.
     * @return a Base64 encoding of this Hash's {@link #getBytes byte array}.
     */
    String toBase64();
}