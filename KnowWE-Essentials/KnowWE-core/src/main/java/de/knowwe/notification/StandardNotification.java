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
 * 
 * @author Sebastian Furth
 * @created 20.04.2012
 */
public class StandardNotification implements Notification {

	private final String message;
	private final Type type;
	private final String id;

	public StandardNotification(String message, Type type) {
		this(message, type, null);
	}

	public StandardNotification(String message, Type type, String id) {
		if (message == null || type == null) {
			throw new NullPointerException();
		}
		if (message.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.message = message;
		this.type = type;
		if (id == null) id = Integer.toString(hashCode());
		this.id = id;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StandardNotification other = (StandardNotification) obj;
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		}
		else if (!message.equals(other.message)) {
			return false;
		}
		return true;
	}

}
