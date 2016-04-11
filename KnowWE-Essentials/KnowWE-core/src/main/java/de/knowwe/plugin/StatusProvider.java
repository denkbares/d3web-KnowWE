/*
 * Copyright (C) 2014 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.plugin;

import de.knowwe.core.user.UserContext;

/**
 * Provides the current status of the wiki. If the status changes, getStatus must return a different integer.
 * <p/>
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 11.08.2014
 */
public interface StatusProvider {

	/**
	 * Returns an int representing the current status of the wiki. If the int does not change between calls, it means
	 * in the scope of this StatusProvider, the wiki has not changed. If the int has changed, it means in the scope of
	 * this StatusProvider, the wiki has changed.
	 */
	int getStatus(UserContext context);

}
