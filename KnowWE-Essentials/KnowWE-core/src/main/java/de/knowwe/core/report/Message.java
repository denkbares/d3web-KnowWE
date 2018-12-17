/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.core.report;

import java.io.Serializable;

import com.denkbares.strings.Strings;

/**
 * Message tied to Sections in the KDOM.
 *
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @author Albrecht Striffler (denkbares GmbH)
 */
public final class Message implements Comparable<Message>, Serializable {

	private static final long serialVersionUID = 686699156806288497L;

	public enum Type {
		INFO, WARNING, ERROR
	}

	private final Type type;
	private final String text;
	private final String details;

	public Message(Type type, String text) {
		this(type, text, (String) null);
	}

	public Message(Type type, String text, String details) {
		this.type = type;
		this.text = text;
		this.details = details;
	}

	public Message(Type type, String text, Throwable e) {
		this(type, text, Strings.getStackTrace(e));
	}

	/**
	 * Returns the type of this message (error, warning or notice).
	 *
	 * @created 01.12.2011
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Returns the verbalization of this message. Will be rendered into the wiki page by the given MessageRenderer of
	 * the Type of the Section.
	 */
	public String getVerbalization() {
		return this.text;
	}

	/**
	 * Returns the details text of this message. Please note that the details are rarely used, mostly for exception
	 * information if an unexpected error has occurred. Therefore this information is not the primary interest of the
	 * normal user and shall only be displayed if the user requests these information. All normal/essential message
	 * information shall be placed in the message verbalization text.
	 *
	 * @return the message details
	 * @created 19.02.2014
	 * @see #getVerbalization()
	 */
	public String getDetails() {
		return details;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Message) {
			Message otherMsg = (Message) obj;
			return otherMsg.type == this.type
					&& otherMsg.getVerbalization().equals(this.getVerbalization());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.type.hashCode() + this.getVerbalization().hashCode();
	}

	@Override
	public String toString() {
		return getVerbalization();
	}

	@Override
	public int compareTo(Message o) {
		return getVerbalization().compareTo(o.getVerbalization());
	}
}
