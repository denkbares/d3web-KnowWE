/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.action;

import java.io.IOException;

import de.knowwe.notification.NotificationManager;

/**
 * Removes the {@link Notification} from the user's {@link NotificationManager}
 * using the parameter <tt>notificationid</tt>.
 * 
 * @author Sebastian Furth
 * @created 20.04.2012
 */
public class RemoveNotificationAction extends AbstractAction {

	private static final String NOTIFICATIONID = "notificationid";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String notificationID = context.getParameter(NOTIFICATIONID);
		if (notificationID != null) {
			NotificationManager.removeNotification(context, notificationID);
		}
	}

}
