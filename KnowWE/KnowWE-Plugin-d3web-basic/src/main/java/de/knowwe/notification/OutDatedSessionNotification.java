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
package de.knowwe.notification;

import de.knowwe.core.report.Message.Type;

/**
 * This is a special class for out dated session notifications. The main
 * difference to {@link StandardNotification} is that the message is predefined
 * and that the article compiling the knowledge base is used as ID.
 * 
 * @author Sebastian Furth
 * @created 20.04.2012
 */
public class OutDatedSessionNotification implements Notification {

	private final String kbArticle;

	public OutDatedSessionNotification(String kbArticle) {
		if (kbArticle == null) {
			throw new NullPointerException();
		}
		if (kbArticle.isEmpty()) {
			throw new NullPointerException();
		}
		this.kbArticle = kbArticle;
	}

	@Override
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append("The session for <em>");
		message.append(kbArticle);
		message.append("</em> is based on an out dated version of the knowledge base. ");
		message.append("You should consider a ");
		message.append("<a href='#' ");
		message.append("onclick='javascript:KNOWWE.plugin.d3webbasic.actions.resetSession(\"");
		message.append(kbArticle);
		message.append("\");KNOWWE.notification.loadNotifications();'>reset</a>.");
		return message.toString();
	}

	@Override
	public String getID() {
		return kbArticle;
	}

	@Override
	public Type getType() {
		return Type.WARNING;
	}

}
