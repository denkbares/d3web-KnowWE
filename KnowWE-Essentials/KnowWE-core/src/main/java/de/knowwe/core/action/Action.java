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

package de.knowwe.core.action;

import java.io.IOException;

/**
 * Interface for Actions. Actions are used for user interactions in KnowWE. The most important method of this interface
 * is execute() because this method will be called when an action is executed.
 * <p>
 * An action declares who may call it via {@link #requiredAccess()}: {@link Access#NONE} opens it to everybody,
 * {@link Access#ADMIN} restricts it to admins, {@link Access#READ} and {@link Access#WRITE} require the matching
 * access to the resources it touches. How the access is enforced is the dispatcher's business, which asks the action
 * rather than knowing it.
 *
 * @author Sebastian Furth
 * @created Mar 9, 2011
 */
public interface Action {

	String XML = "application/xml; charset=UTF-8";
	String HTML = "text/html; charset=UTF-8";
	String JSON = "application/json; charset=UTF-8";
	String PLAIN_TEXT = "text/plain; charset=UTF-8";
	String TURTLE = "text/turtle; charset=UTF-8";
	String ZIP = "application/zip";
	String BINARY = "application/x-bin";

	/**
	 * The access a caller needs to execute an action.
	 * <ul>
	 *     <li>{@link #NONE}: anybody, so the action must reveal nothing that is not public anyway, such as whether
	 *     the wiki is up. The overriding method should say in its javadoc what makes the action harmless.</li>
	 *     <li>{@link #AUTH}: any signed in user; the action only touches the user's own session and needs no check of
	 *     a wiki resource. Use this only where no resource permission can be evaluated, e.g. a session or an external
	 *     operation that is attributed to the user.</li>
	 *     <li>{@link #READ}: read access to the resources the action touches, as decided by the wiki's permission
	 *     configuration. Anonymous users pass exactly where the wiki grants them read access.</li>
	 *     <li>{@link #WRITE}: write access to the resources the action touches, as decided by the wiki's permission
	 *     configuration.</li>
	 *     <li>{@link #ADMIN}: only admins; the dispatcher enforces it, so the action needs no check of its own.</li>
	 *     <li>{@link #HELPER}: the action checks its access in a helper the enforcer cannot see, such as a shared
	 *     support class called by several actions. The rule then expects no check in the action itself, so the helper
	 *     has to enforce the access; use this only where the check genuinely lives elsewhere and name the helper in
	 *     the javadoc of the overriding method.</li>
	 * </ul>
	 */
	enum Access {
		NONE, AUTH, READ, WRITE, ADMIN, HELPER;

		/**
		 * Whether a user has to be signed in before the action runs, independent of the permission check it performs.
		 * {@link #AUTH} and {@link #ADMIN} require it. {@link #READ} and {@link #WRITE} do not: their check
		 * ({@code KnowWEUtils#assertCanView}/{@code assertCanWrite}) consults the wiki's permission configuration, so
		 * anonymous users are served exactly where the wiki grants them access. {@link #HELPER} is excluded as well:
		 * the helper decides who may execute, and it may as well serve anonymous users.
		 */
		public boolean requiresAuthentication() {
			return this == AUTH || this == ADMIN;
		}

		/**
		 * Whether only admins may execute with this access level.
		 */
		public boolean isAdmin() {
			return this == ADMIN;
		}
	}

	/**
	 * Executes the Action.
	 *
	 * @param context the context for this action
	 * @created Mar 9, 2011
	 */
	void execute(UserActionContext context) throws IOException;

	/**
	 * The access a caller needs to execute this action. It defaults to {@link Access#WRITE}: an action is assumed to
	 * write a wiki resource until it states otherwise, so that a missing statement fails the build rather than
	 * leaving the action under-checked. An action that needs less overrides this method and says in its javadoc why.
	 *
	 * @return the access a caller needs
	 */
	default Access requiredAccess() {
		return Access.WRITE;
	}

	/**
	 * Exception that can be thrown to signal a defined error code to the web page.
	 */
	class SendError extends IOException {
		private static final long serialVersionUID = 8501144599737106114L;
		private final int httpErrorCode;

		public SendError(int httpErrorCode) {
			this.httpErrorCode = httpErrorCode;
		}

		public SendError(int httpErrorCode, String message) {
			super(message);
			this.httpErrorCode = httpErrorCode;
		}

		public SendError(int httpErrorCode, String message, Throwable cause) {
			super(message, cause);
			this.httpErrorCode = httpErrorCode;
		}

		public SendError(int httpErrorCode, Throwable cause) {
			super(cause);
			this.httpErrorCode = httpErrorCode;
		}

		public int getHttpErrorCode() {
			return httpErrorCode;
		}
	}
}
