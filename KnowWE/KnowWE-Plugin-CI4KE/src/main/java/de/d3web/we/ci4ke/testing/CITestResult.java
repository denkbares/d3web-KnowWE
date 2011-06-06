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

package de.d3web.we.ci4ke.testing;

public final class CITestResult implements Comparable<CITestResult> {

	public enum Type {
		SUCCESSFUL,
		FAILED,
		ERROR
	}

	private final Type type;
	private final String message;
	private final String configuration;

	public CITestResult(Type type) {
		this(type, null, null);
	}

	public CITestResult(Type type, String message) {
		this(type, message, null);
	}

	public CITestResult(Type type, String message, String configuration) {
		this.type = type;
		this.message = message;
		this.configuration = configuration;
	}

	public boolean isSuccessful() {
		return type == Type.SUCCESSFUL;
	}

	public Type getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public String getConfiguration() {
		return configuration;
	}

	@Override
	public String toString() {
		return type.toString() + " - " + getMessage();
	}

	/**
	 * SUCCESSFUL < FAILED < ERROR
	 */
	@Override
	public int compareTo(CITestResult tr) {
		return type.compareTo(tr.getType());
	}

	/**
	 * Returns if the test result described by this object has a message
	 * 
	 * @created 30.05.2011
	 * @return if this result has a message
	 */
	public boolean hasMessage() {
		return this.message != null && !this.message.isEmpty();
	}

	/**
	 * Returns if the test result described by this object has a configuration
	 * 
	 * @created 30.05.2011
	 * @return if this result has a configuration
	 */
	public boolean hasConfiguration() {
		return this.configuration != null && !this.configuration.isEmpty();
	}
}
