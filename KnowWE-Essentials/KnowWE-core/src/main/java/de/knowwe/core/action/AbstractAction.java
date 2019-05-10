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

import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Abstract implementation of the Action Interface (KnowWEActions or Servlets).
 * <p/>
 * Please note that this standard implementation returns false for the isAdminAction()-Method. If you want to implement
 * an action which is only executable for admins you should implement the Action Interface
 *
 * @author Sebastian Furth
 * @see Action
 */
public abstract class AbstractAction implements Action {

	/**
	 * Returns always false - which means that your action can be executed by every user. If you want to implement a
	 * "AdminAction" you should consider implementing the Action interface instead of extending AbstractAction.
	 */
	@Override
	public boolean isAdminAction() {
		return false;
	}

	/**
	 * Returns the section that is denoted for the specified action context. The section is usually referred by the URL
	 * parameter "SectionID" ({@link Attributes#SECTION_ID}), but for campatibility reasons this method also supports
	 * "KdomNodeId". If no section is denoted, or if the specified section is not found, or if the user does not have
	 * read access to the section, an appropriate error is created.
	 *
	 * @param context the action context to get the section for
	 * @return the section for the action
	 */
	@NotNull
	public static Section<?> getSection(UserActionContext context) throws IOException {
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		if (sectionId == null) sectionId = context.getParameter("KdomNodeId"); // compatibility
		if (sectionId == null) {
			fail(context, HttpServletResponse.SC_NOT_FOUND,
					"The request did not contain a section id, unable to execute action.");
		}
		Section<?> section = Sections.get(sectionId);
		if (section == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND,
					"The referenced section was not found. " +
							"Maybe the page content is outdated. Please reload.");
			throw new IOException("Section with id '" + sectionId + "' was not found");
		}
		KnowWEUtils.assertCanView(section, context);
		return section;
	}

	/**
	 * Returns the section that is denoted for the specified action context. The section is usually referred by the URL
	 * parameter "SectionID" ({@link Attributes#SECTION_ID}), but for campatibility reasons this method also supports
	 * "KdomNodeId". If no section is denoted, or if the specified section is not found, or if the section is not of the
	 * specified expected type, or if the user does not have read access to the section, an appropriate error is
	 * created.
	 *
	 * @param context the action context to get the section for
	 * @return the section for the action
	 */
	@NotNull
	public static <T extends Type> Section<T> getSection(UserActionContext context, Class<T> type) throws IOException {
		Section<?> section = getSection(context);
		if (!type.isInstance(section.get())) {
			failUnexpected(context, "The request refers a section of an unexpected type.");
		}
		return Sections.cast(section, type);
	}

	/**
	 * Fails with the specified error code and message. The context's response is sent the the error code and message,
	 * and an IOException is thrown with the specified message as well.
	 *
	 * @param context  the context to send the error code to
	 * @param httpCode the fail error code, see {@link HttpServletResponse}#SC_... constants
	 * @param message  the fail message
	 */
	@Contract("_, _, _ -> fail")
	public static void fail(UserActionContext context, int httpCode, String message) throws IOException {
		throw new SendError(httpCode, message);
	}

	/**
	 * Fails with the error code 417 (EXPECTATION FAILED) and the specified message. The context's response is sent the
	 * the error code and message, and an IOException is thrown with the specified message as well.
	 *
	 * @param context the context to send the error code to
	 * @param message the fail message
	 */
	@Contract("_, _ -> fail")
	public static void failUnexpected(UserActionContext context, String message) throws IOException {
		fail(context, HttpServletResponse.SC_EXPECTATION_FAILED, message);
	}

	/**
	 * Fails with the error code 417 (EXPECTATION FAILED) and the message that the page is outdated. The context's
	 * response is sent the the error code and message, and an IOException is thrown with the specified message as well.
	 * This method is usually used if a expectations fails, because some action parameters are no longer matches the
	 * wiki content, e.g. is some section id or KDOM structure has changed because of already modified page contents.
	 *
	 * @param context the context to send the error code to
	 */
	@Contract("_ -> fail")
	public static void failOutdated(UserActionContext context) throws IOException {
		fail(context, HttpServletResponse.SC_EXPECTATION_FAILED,
				"The page content seems to be outdated. Please reload.");
	}

	/**
	 * Fails with the error code 500 (INTERNAL SERVER ERROR) and the message that an internal error has occurred. The
	 * context's response is sent the the error code and message, and an IOException is thrown as well.
	 *
	 * @param context the context to send the error code to
	 */
	@Contract("_ -> fail")
	public static void failInternal(UserActionContext context) throws IOException {
		fail(context, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"An unexpected internal error occurred. Please retry or contact support.");
	}

	/**
	 * Fails with the error code 500 (INTERNAL SERVER ERROR) and the message that an internal error has occurred. The
	 * context's response is sent the the error code and message, and an IOException is thrown as well.
	 *
	 * @param context the context to send the error code to
	 */
	@Contract("_, _ -> fail")
	public static void failInternal(UserActionContext context, Throwable cause) throws IOException {
		throw new SendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"An unexpected " + cause.getClass().getSimpleName() + " occurred. " +
						"Please retry or contact support.");
	}

	@Override
	public abstract void execute(UserActionContext context) throws IOException;
}
