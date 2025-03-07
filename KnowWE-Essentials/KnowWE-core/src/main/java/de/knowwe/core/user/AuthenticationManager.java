/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.knowwe.core.user;

/**
 * Simple interface for authentication related
 *
 * @author Sebastian Furth
 * @created Mar 16, 2011
 */
public interface AuthenticationManager {

	/**
	 * Returns whether the user has administration rights.
	 *
	 * @return user is an admin or not
	 * @created 14.10.2010
	 */
	boolean userIsAdmin();

	/**
	 * Returns whether the user is authenticated or asserted or not.
	 *
	 * @return boolean asserted state of the user
	 * @created 01.02.2011
	 */
	boolean userIsAsserted();

	/**
	 * Returns whether the user is authenticated or not.
	 *
	 * @return boolean authentication state of the user
	 */
	boolean userIsAuthenticated();

	/**
	 * Returns name of the current user
	 *
 	 * @return the name of the current user
	 */
	String getUserName();

	/**
	 * Returns mail address of the current user
	 *
	 * @return  mail address of the current user
	 */
	String getMailAddress();

}
