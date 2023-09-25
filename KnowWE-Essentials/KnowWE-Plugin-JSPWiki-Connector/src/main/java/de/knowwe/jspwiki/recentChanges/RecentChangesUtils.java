/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

package de.knowwe.jspwiki.recentChanges;

import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.compile.CompilationLocal;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.JSPWikiConnector;

import static java.time.ZoneId.systemDefault;

public class RecentChangesUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecentChangesUtils.class);
	private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
	private static final FastDateFormat TIME_FORMAT = FastDateFormat.getInstance("HH:mm:ss");
	private static final FastDateFormat DATE_TIME_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
	public static final String PAGE = "Page";
	public static final String LAST_MODIFIED = "Last Modified";
	public static final String AUTHOR = "Author";
	public static final String CHANGE_NOTES = "Change Notes";

	public static Set<Page> getRecentChangesFromJSPWiki() {
		// can take quite some time for large wikis, so we cache this...
		return CompilationLocal.getCached(KnowWEUtils.getDefaultArticleManager()
				.getCompilerManager(), "recentChangesFromJSPWiki", () -> {
			JSPWikiConnector wikiConnector = (JSPWikiConnector) Environment.getInstance().getWikiConnector();
			return wikiConnector.getPageManager().getRecentChanges();
		});
	}

	public static Set<Page> getRecentChangesWithFilter(Set<Page> sortedFilteredRecentChanges, UserContext user) {
		// can take quite some time for large wikis, so we cache this...
		boolean showPages = showPages(user);
		boolean showAttachments = isShowAttachments(user);
		boolean showIntermediate = showIntermediates(user);
		return CompilationLocal.getCached(KnowWEUtils.getDefaultArticleManager().getCompilerManager(),
				"recentChangesWithFilterFromJSPWiki-" + showPages + "-" + showAttachments + "-" + showIntermediate,
				() -> {
					Set<Page> recentChangesCleaned = new LinkedHashSet<>();
					for (Page page : sortedFilteredRecentChanges) {
						if (!(page instanceof Attachment) && showPages) {
							addPage(recentChangesCleaned, page, showIntermediate);
						}
						else if (page instanceof Attachment && showAttachments) {
							addPage(recentChangesCleaned, page, showIntermediate);
						}
					}
					return recentChangesCleaned;
				});
	}

	private static void addPage(Set<Page> filteredPages, Page page, boolean showIntermediate) {
		if (showIntermediate) {
			List<Page> versionHistory;
			try {
				versionHistory = ((JSPWikiConnector) Environment.getInstance().getWikiConnector()).getPageManager()
						.getVersionHistory(page.getName());
			}
			catch (Exception e) {
				versionHistory = List.of();
				LOGGER.error("Exception while getting version history", e);
			}
			filteredPages.addAll(versionHistory);
			if (versionHistory.isEmpty()) {    // sometimes version history doesn't contain current version if only one exists
				filteredPages.add(page);
			}
		}
		else {
			filteredPages.add(page);
		}
	}

	public static boolean showIntermediates(UserContext user) {
		return getCheckbox(user, "intermediate");
	}

	public static boolean isShowAttachments(UserContext user) {
		return getCheckbox(user, "attachment");
	}

	public static boolean showPages(UserContext user) {
		return getCheckbox(user, "page");
	}

	private static boolean getCheckbox(UserContext userContext, String checkBox) {
		JSONObject localSectionStorage = AbstractAction.getLocalSectionStorage(userContext);
		if (!localSectionStorage.isEmpty() && !isFilterActive(localSectionStorage) && !checkBox.equals("page")) {
			return false;
		}
		Boolean show = null;
		try {
			show = Boolean.valueOf(String.valueOf(localSectionStorage.get(checkBox)));
		}
		catch (Exception ignored) {
		}
		if (localSectionStorage.isEmpty() || show == null) {
			switch (checkBox) {
				case "page" -> {
					return true;
				}
				case "attachment", "intermediate" -> {
					return false;
				}
			}
		}

		return (boolean) localSectionStorage.get(checkBox);
	}

	static private boolean isFilterActive(JSONObject localSectionStorage) {
		JSONObject pagination = localSectionStorage.optJSONObject("pagination");
		if (pagination == null) return false;
		JSONObject filter = pagination.optJSONObject("filter");
		if (filter == null) return false;
		return filter.optBoolean("active");
	}

	public String toDateString(Date date) {
		return DATE_FORMAT.format(date);
	}

	@NotNull
	public String toDateOrTodayTimeString(Date date) {
		LocalDate today = LocalDate.now();
		LocalDate localDate = date.toInstant().atZone(systemDefault()).toLocalDate();
		FastDateFormat formatter;
		if (localDate.equals(today)) {
			formatter = TIME_FORMAT;
		}
		else {
			formatter = DATE_TIME_FORMAT;
		}
		return formatter.format(date);
	}

	public String getColumnValueByName(String columnName, Page page) {
		Object cell = getColumnObjectValueByName(columnName, page);
		if (cell instanceof Date date) {
			return toDateString(date);
		}
		else {
			return cell.toString();
		}
	}

	public Object getColumnObjectValueByName(String columnName, Page page) {
		switch (columnName) {
			case PAGE -> {
				return page.getName();
			}
			case LAST_MODIFIED -> {
				return page.getLastModified();
			}
			case AUTHOR -> {
				String author = page.getAuthor();
				return Objects.requireNonNullElse(author, "Unknown Author");
			}
			case CHANGE_NOTES -> {
				String changeNote = page.getAttribute("changenote");
				if (changeNote == null) {
					changeNote = "-";
				}
				return changeNote;
			}
		}
		return columnName;
	}
}
