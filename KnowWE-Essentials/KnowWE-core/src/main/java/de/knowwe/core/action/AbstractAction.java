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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Environment;
import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Abstract implementation of the Action Interface (KnowWEActions or Servlets).
 *
 * @author Sebastian Furth
 * @see Action
 */
public abstract class AbstractAction implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAction.class);

	/**
	 * The historical name of the section id parameter, still sent by InstantEdit, TaskManagement and Todos.
	 */
	private static final String KDOM_NODE_ID = "KdomNodeId";

	/**
	 * Case variants of {@link Attributes#SECTION_ID} we have already complained about, so each one is logged once.
	 */
	private static final Set<String> REPORTED_SECTION_ID_ALIASES = ConcurrentHashMap.newKeySet();

	/**
	 * Name of the html attribute carrying the key of a section's local storage, see {@link
	 * #getLocalSectionStorageKey(Section)}.
	 */
	public static final String LOCAL_SECTION_STORAGE_KEY_ATTRIBUTE = "storage-key";

	private static final String LOCAL_SECTION_STORAGE_KEY = "localSectionStorageKey";

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
		if (sectionStorage == null) {
			String sectionId = user.getParameter(Attributes.SECTION_ID);
			if (sectionId != null) {
				JSONObject jsonObject = (JSONObject) user.getSession()
						.getAttribute(getHttpSessionAttributeLocalSectionStorageKey(user.getEnvironment(), sectionId));
				if (jsonObject != null) return jsonObject;

			}
		}
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
	 * Get the local storage of the specified section. If the current request is about that very section, the storage
	 * provided with the request is used, see {@link #getLocalSectionStorage(UserContext)}. Otherwise, e.g. while
	 * rendering the full page or while rendering another section, the copy last stored in the http session is used.
	 * That copy is only available for users that are known to the wiki, see
	 * KNOWWE.helper.setToLocalSectionStorage(sectionId, key, value) on the client side (KnowWE-helper.js).
	 *
	 * @param user      the user context of the request
	 * @param sectionId the id of the section to get the local storage for
	 * @return a JSONObject with the local section storage
	 */
	@NotNull
	public static JSONObject getLocalSectionStorage(UserContext user, String sectionId) {
		if (sectionId.equals(user.getParameter(Attributes.SECTION_ID))) return getLocalSectionStorage(user);
		if (user.getSession() == null) return new JSONObject();
		Object stored = user.getSession().getAttribute(getHttpSessionAttributeLocalSectionStorageKey(user.getEnvironment(), sectionId));
		return (stored instanceof JSONObject jsonObject) ? jsonObject : new JSONObject();
	}

	public static void storeLocalSectionStorage(UserActionContext context) {
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		String value = context.getParameter("value");
		if (sectionId != null) {
			context.getSession()
					.setAttribute(getHttpSessionAttributeLocalSectionStorageKey(context.getEnvironment(), sectionId), new JSONObject(value));
		}
	}

	@NotNull
	private static String getHttpSessionAttributeLocalSectionStorageKey(@NotNull Environment environment, String sectionId) {
		return Attributes.LOCAL_SECTION_STORAGE + "_" + getLocalSectionStorageKey(environment, sectionId);
	}

	/**
	 * Returns the key the local storage of the section with the specified id is kept under, see {@link
	 * #getLocalSectionStorageKey(Section)}. If there is no such section (any more), the id itself is returned.
	 *
	 * @param environment the environment the section belongs to
	 * @param sectionId   the id of the section to get the storage key for
	 * @return the key of the section's local storage
	 */
	@NotNull
	public static String getLocalSectionStorageKey(@NotNull Environment environment, String sectionId) {
		Section<?> section = Sections.get(environment, sectionId);
		return (section == null) ? sectionId : getLocalSectionStorageKey(section);
	}

	/**
	 * Returns the key the local storage of the specified section is kept under, in the browser as well as in the http
	 * session. The key has to be rendered as attribute {@link #LOCAL_SECTION_STORAGE_KEY_ATTRIBUTE} to the element
	 * carrying the section id, so that KNOWWE.helper (KnowWE-helper.js) is able to use it.
	 * <p>
	 * Other than the section id, which changes with every edit of the article and every restart of the wiki, the key
	 * remains the same as long as the article is not renamed and no section of the same type is inserted before the
	 * specified one. Therefore, the user's settings for a section (e.g. pagination or filters) are preserved.
	 *
	 * @param section the section to get the storage key for
	 * @return the key of the section's local storage
	 */
	/**
	 * Leading part of the storage keys: the name of the former single web, kept so the keys stay the same as the ones
	 * browsers already store.
	 */
	private static final String STORAGE_KEY_PREFIX = "default_web";

	@NotNull
	public static String getLocalSectionStorageKey(Section<?> section) {
		String key = section.getObject(LOCAL_SECTION_STORAGE_KEY);
		if (key != null) return key;
		// number all sections of the same type at once, so that pages with many of them are not scanned repeatedly
		Class<? extends Type> typeClass = section.get().getClass();
		int index = 0;
		for (Section<? extends Type> other : Sections.successors(section.getArticle().getRootSection(), typeClass)) {
			if (other.get().getClass() != typeClass) continue;
			other.storeObject(LOCAL_SECTION_STORAGE_KEY, STORAGE_KEY_PREFIX + "/" + section.getTitle()
					+ "/" + typeClass.getName() + "/" + index++);
		}
		key = section.getObject(LOCAL_SECTION_STORAGE_KEY);
		if (key != null) return key;
		// not part of its article's KDOM, so there is nothing to identify it by
		key = section.getID();
		section.storeObject(LOCAL_SECTION_STORAGE_KEY, key);
		return key;
	}

	/**
	 * Returns the section that is denoted for the specified action context. The section is usually referred by the URL
	 * parameter "SectionID" ({@link Attributes#SECTION_ID}), but for compatibility reasons this method also supports
	 * "KdomNodeId" and the parameter written in a different case. If no section is denoted, or if the specified
	 * section is not found, or if the user does not have read access to the section, an appropriate error is created.
	 *
	 * @param context the action context to get the section for
	 * @return the section for the action
	 */
	@NotNull
	public static Section<?> getSection(UserContext context) throws IOException {
		String sectionId = getSectionId(context);
		if (sectionId == null) {
			fail(context, HttpServletResponse.SC_NOT_FOUND,
					"The request did not contain a section id, unable to execute action.");
		}
		Section<?> section = Sections.get(context, sectionId);
		if (section == null) {
			fail(context, HttpServletResponse.SC_NOT_FOUND, "The referenced section was not found. " +
					"Maybe the page content is outdated. Please reload.");
		}
		KnowWEUtils.assertCanView(section, context);
		return section;
	}

	/**
	 * Returns the section id denoted by the request, or null if there is none.
	 * <p>
	 * Besides {@link Attributes#SECTION_ID} this also accepts the parameter written in a different case, because
	 * clients used to send "sectionID" or "sectionId" as well. Those are logged once each, so the remaining clients
	 * can be migrated and the leniency can be removed again. Parameters with an own name like "section" are
	 * deliberately not accepted: a request may denote several sections, and silently picking the wrong one would
	 * apply the access check to a section the action does not use afterwards.
	 *
	 * @param context the action context to get the section id from
	 * @return the section id of the request, or null if the request does not denote a section
	 */
	@Nullable
	static String getSectionId(UserContext context) {
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		if (sectionId != null) return sectionId;

		for (Map.Entry<String, String> parameter : context.getParameters().entrySet()) {
			String name = parameter.getKey();
			if (name.equalsIgnoreCase(Attributes.SECTION_ID) && !name.equals(Attributes.SECTION_ID)) {
				if (REPORTED_SECTION_ID_ALIASES.add(name)) {
					LOGGER.warn("A request denotes its section by '{}', please use '{}' instead",
							name, Attributes.SECTION_ID);
				}
				return parameter.getValue();
			}
		}

		// the historical name of the parameter, still sent by a couple of plugins, so it is not reported
		return context.getParameter(KDOM_NODE_ID);
	}

	/**
	 * Returns the section that is denoted for the specified action context. The section is usually referred by the URL
	 * parameter "SectionID" ({@link Attributes#SECTION_ID}), but for compatibility reasons this method also supports
	 * "KdomNodeId" and the parameter written in a different case. If no section is denoted, or if the specified
	 * section is not found, or if the section is not of the specified expected type, or if the user does not have
	 * read access to the section, an appropriate error is created.
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
		throw new SendError(httpCode, context.getEnvironment().getWikiConnector().renderWikiSyntax(message));
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
