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
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAction.class);

	/**
	 * Get the local storage from the user context. Check out KNOWWE.helper.setToLocalSectionStorage(sectionId, key,
	 * value) and KNOWWE.helper.getLocalSectionStorage(sectionId) on the client side (KnowWE-helper.js).
	 * <p>
	 * For the section storage to be available in the user context, it has to be set in the data field of the ajax
	 * request on the client side. When a section is rerendered using jq$(elementWithSectionId).rerender(...), the
	 * section storage for the section with that section id will automatically be provided in the user context.
	 * If you are using a custom ajax request, you have to add the section storage manually the following way:
	 * <pre>
	 *    jq$.ajax({
	 *        url: KNOWWE.core.util.getURL({ action: "<your-action-name>" }),
	 *        type: 'post',
	 *        data: {
	 *            localSectionStorage: KNOWWE.helper.getLocalSectionStorage(sectionId, true)
	 *        },
	 *        success: function() {....},
	 *        ...
	 *    })
	 * </pre>
	 *
	 * @param user the user context of the request
	 * @return a JSONObject with the local section storage
	 */
	@NotNull
	public static JSONObject getLocalSectionStorage(UserContext user) {
		String sectionStorage = user.getParameter(Attributes.LOCAL_SECTION_STORAGE);
		if (sectionStorage == null) return new JSONObject();
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(sectionStorage);
		}
		catch (JSONException e) {
			LOGGER.warn("Exception while parsing json", e);
			return new JSONObject();
		}

		return jsonObject;
	}

	/**
	 * Get the local section storage from the section, if no user context containing the section storage is available.
	 * The normal {@link #getLocalSectionStorage(UserContext)} method has to be called before, for this method to work
	 * (will happen on rerender).
	 *
	 * @param section the section for which a local section storage is available
	 * @return the local section storage of the section
	 * @see #getLocalSectionStorage(UserContext)
	 */
	public static JSONObject getLocalSectionStorage(Section<?> section) {
		return section.getObject(Attributes.LOCAL_SECTION_STORAGE);
	}

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
	public static Section<?> getSection(UserContext context) throws IOException {
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		if (sectionId == null) sectionId = context.getParameter("KdomNodeId"); // compatibility
		if (sectionId == null) {
			fail(context, HttpServletResponse.SC_NOT_FOUND,
					"The request did not contain a section id, unable to execute action.");
		}
		Section<?> section = Sections.get(sectionId);
		if (section == null) {
			fail(context, HttpServletResponse.SC_NOT_FOUND, "The referenced section was not found. " +
					"Maybe the page content is outdated. Please reload.");
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
	public static void fail(UserContext context, int httpCode, String message) throws IOException {
		throw new SendError(httpCode, Environment.getInstance().getWikiConnector().renderWikiSyntax(message));
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
