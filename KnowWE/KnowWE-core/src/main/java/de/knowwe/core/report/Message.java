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

/**
 * 
 * Message tied to Sections in the KDOM.
 * 
 * 
 * @author Jochen
 * @author Albrecht Striffler (denkbares GmbH)
 * 
 */
public final class Message implements Comparable<Message>, Serializable {

	private static final long serialVersionUID = 686699156806288497L;

	public enum Type {
		INFO, WARNING, ERROR
	}

	private final String text;

	private final Type type;

	public Message(Type type, String text) {
		this.type = type;
		this.text = text;
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
	 * Returns the verbalization of this message. Will be rendered into the wiki
	 * page by the given MessageRenderer of the Type of the Section.
	 */
	public String getVerbalization() {
		return this.text;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Message) {
			Message otherMsg = (Message) obj;
			if (otherMsg.type.equals(this.type)
					&& otherMsg.getVerbalization().equals(this.getVerbalization())) {
				return true;
			}
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
