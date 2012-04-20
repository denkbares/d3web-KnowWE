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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.knowwe.core.report.Message.Type;
import de.knowwe.notification.Notification;
import de.knowwe.notification.NotificationManager;

/**
 * Returns all {@link Notifications} associated to the user's
 * {@link NotificationManager} in JSON.
 * 
 * @author Sebastian Furth
 * @created 20.04.2012
 */
public class GetNotificationsAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		NotificationManager manager = NotificationManager.getNotificationManager(context);
		List<Notification> notifications = new ArrayList<Notification>(manager.getNotifications());
		Collections.reverse(notifications);

		Writer w = context.getWriter();
		w.write("{ \"notifications\" : [");
		for (int i = 0; i < notifications.size(); i++) {
			Notification notification = notifications.get(i);
			// id
			w.write("{ \"id\" : \"");
			w.write(notification.getID());
			// type
			w.write("\", \"type\" : \"");
			if (notification.getType().equals(Type.ERROR)) {
				w.write("error");
			}
			else {
				w.write("warning");
			}
			// message
			w.write("\", \"message\" : \"");
			w.write(notification.getMessage());
			w.write("\" }");
			// the last element doesn't need a komma...
			if (i < notifications.size() - 1) {
				w.write(", ");
			}
		}
		w.write("] }");
	}

}
