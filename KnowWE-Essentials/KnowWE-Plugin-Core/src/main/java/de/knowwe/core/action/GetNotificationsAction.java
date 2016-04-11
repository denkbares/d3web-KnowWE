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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.notification.Notification;
import de.knowwe.notification.NotificationManager;

/**
 * Returns all {@link Notification}s associated to the user's
 * {@link NotificationManager} in JSON.
 * 
 * @author Sebastian Furth
 * @created 20.04.2012
 */
public class GetNotificationsAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		NotificationManager manager = NotificationManager.getNotificationManager(context);
		List<Notification> notifications = new ArrayList<>(manager.getNotifications());
		Collections.reverse(notifications);
		JSONArray response = new JSONArray();
		try {
			for (int i = 0; i < notifications.size(); i++) {
				Notification notification = notifications.get(i);
				JSONObject message = new JSONObject();
				message.put("id", notification.getID());
				message.put("type", notification.getType().toString().toLowerCase());
				message.put("message", notification.getMessage());
				response.put(message);
			}
			response.write(context.getWriter());
		}
		catch (JSONException e) {
			throw new IOException(e);
		}
	}
}
