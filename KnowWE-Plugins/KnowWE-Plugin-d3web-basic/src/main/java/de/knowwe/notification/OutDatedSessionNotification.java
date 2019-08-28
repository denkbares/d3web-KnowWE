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

import de.knowwe.core.kdom.parsing.Section;
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

	private final String id;
	private final String title;

	public OutDatedSessionNotification(String sectionId, String kbName) {
		this.title = kbName;
		this.id = sectionId;
	}

	public OutDatedSessionNotification(Section<?> section, String kbName) {
		this.title = kbName;
		this.id = section.getID();
	}

	@Override
	public String getMessage() {
		StringBuilder message = new StringBuilder();
		message.append("The session for <em>");
		message.append(title);
		message.append("</em> is based on an outdated version of the knowledge base. ");
		message.append("<a style='color:red;cursor: pointer;' ");
		message.append("onclick='KNOWWE.plugin.d3webbasic.actions.resetSession(\"")
				.append(id)
				.append("\")'>Click here</a> to reset your current session and use the new version of the knowledge base.<br>");
		message.append("You can ignore this message, if you want to continue with the current session and outdated knowledge base for now.");
		return message.toString();
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public Type getType() {
		return Type.WARNING;
	}
}
