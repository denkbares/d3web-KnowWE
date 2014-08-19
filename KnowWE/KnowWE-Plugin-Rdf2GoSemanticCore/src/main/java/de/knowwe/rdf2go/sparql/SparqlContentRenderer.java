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
package de.knowwe.rdf2go.sparql;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ontoware.rdf2go.model.QueryResultTable;

import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.rdf2go.sparql.utils.SparqlRenderResult;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class SparqlContentRenderer implements Renderer {

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult result) {

		KnowWEUtils.cleanupSectionCookies(user, Pattern.compile("^SparqlRenderer-(.+)$"), 1);

		Section<SparqlMarkupType> markupSection = Sections.findAncestorOfType(sec,
				SparqlMarkupType.class);
		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(markupSection);
		if (core == null) {
			// we render an empty div, otherwise the ajax rerendering does not
			// work properly
			result.appendHtmlElement("div", "");
			return;
		}

		/*
		 * Show query text above of query result
		 */
		String showQueryFlag = DefaultMarkupType.getAnnotation(markupSection,
				SparqlMarkupType.RENDER_QUERY);
		if (showQueryFlag != null && showQueryFlag.equalsIgnoreCase("true")) {
			/*
			 * we need an opening html element around all the content as for
			 * some reason the ajax insert onyl inserts one (the first) html
			 * element into the page
			 */
			result.appendHtml("<div>");

			/*
			 * render query text
			 */
			result.appendHtml("<span>");
			DelegateRenderer.getInstance().render(sec, user, result);
			result.appendHtml("</span>");
		}

		String sparqlString = Rdf2GoUtils.createSparqlString(core, sec.getText());

		try {
			if (sparqlString.toLowerCase().startsWith("construct")) {
				result.appendHtml("<tt>");
				result.append(sec.getText());
				result.appendHtml("</tt>");
			}
			else {

				RenderOptions renderOpts = new RenderOptions(sec.getID());

				// Default values
				String startRow = "1";
				String navigationLimit = "100";
				Map<String, String> sortMap = new LinkedHashMap<String, String>();
				// Get values out of JSON Cookie
				if (getJSONCookieString(sec, user) != null) {
					JSONObject json = new JSONObject(Strings.decodeURL(getJSONCookieString(sec,
							user)));
					if (!json.isNull("navigationOffset")) {
						startRow = json.getString("navigationOffset");
					}
					if (!json.isNull("navigationLimit")) {
						navigationLimit = json.getString("navigationLimit");
					}
					if (!json.isNull("sorting")) {
						JSONArray jsonArray = json.getJSONArray("sorting");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject sortPair = jsonArray.getJSONObject(i);
							@SuppressWarnings("unchecked")
							Iterator<String> it = sortPair.keys();
							String key = it.next();
							sortMap.put(key, sortPair.getString(key));
						}
						renderOpts.setSortingMap(sortMap);
					}
				}

				setRenderOptions(markupSection, renderOpts);

				if (renderOpts.isBorder()) result.appendHtml("<div class='border'>");
				if (renderOpts.isSorting()) {
					sparqlString = modifyOrderByInSparqlString(sortMap,
							sparqlString);
				}

				SparqlRenderResult resultEntry;

				renderOpts.setRdf2GoCore(core);

				if (renderOpts.isNavigation()) {
					// do not show navigation bar if LIMIT or OFFSET is set in
					// markup
					// sparqlString =
					// addOffsetAndLimitToSparqlString(sparqlString,
					// markupSection,
					// navigationOffset, navigationLimit);

					if (navigationLimit.equals("All")) {
						renderOpts.setShowAll(true);
					}
					else {
						renderOpts.setNavigationLimit(navigationLimit);
						renderOpts.setNavigationOffset(startRow);
					}
				}
				QueryResultTable resultSet = null;
				try {
					resultSet = core.sparqlSelect(sparqlString, true, getTimeout(markupSection));
				}
				catch (RuntimeException e) {
					result.appendHtml("<span class='warning'>"
							+ e.getMessage() + "</span>");
					Log.warning("Exception while executing SPARQL", e);
				}
				if (resultSet != null) {
					resultEntry = SparqlResultRenderer.getInstance().renderQueryResult(
							resultSet, renderOpts, user);
					if (renderOpts.isNavigation() && !renderOpts.isRawOutput()) {
						renderTableSizeSelector(navigationLimit, sec.getID(),
								resultEntry.getSize(), result);
						renderNavigation(startRow, navigationLimit,
								resultEntry.getSize(), sec.getID(), result);

					}

					result.appendHtml(resultEntry.getHTML());
				}
				if (renderOpts.isBorder()) result.appendHtml("</div>");

				if (showQueryFlag != null && showQueryFlag.equalsIgnoreCase("true")) {
					/*
					 * we need an opening html element around all the content as
					 * for some reason the ajax insert onyl inserts one (the
					 * first) html element into the page
					 */
					result.appendHtml("</div>");
				}

			}
		}
		catch (JSONException e) {
			Log.severe("JSONException while rendering SPARQL", e);
		}
	}

	private long getTimeout(Section<SparqlMarkupType> markupSection) {
		String timeoutString = DefaultMarkupType.getAnnotation(markupSection, SparqlMarkupType.TIMEOUT);
		long timeOutMillis = Rdf2GoCore.DEFAULT_TIMEOUT;
		if (timeoutString != null) {
			timeOutMillis = (long) (Double.parseDouble(timeoutString) * TimeUnit.SECONDS.toMillis(1));
		}
		return timeOutMillis;
	}

	private void setRenderOptions(Section<SparqlMarkupType> markupSection, RenderOptions renderOpts) {
		renderOpts.setRawOutput(checkAnnotation(markupSection, SparqlMarkupType.RAW_OUTPUT));
		renderOpts.setSorting(checkSortingAnnotation(markupSection,
				SparqlMarkupType.SORTING));
		renderOpts.setZebraMode(checkAnnotation(markupSection, SparqlMarkupType.ZEBRAMODE, true));
		renderOpts.setTree(Boolean.valueOf(DefaultMarkupType.getAnnotation(markupSection,
				SparqlMarkupType.TREE)));
		renderOpts.setBorder(checkAnnotation(markupSection, SparqlMarkupType.BORDER, true));
		renderOpts.setNavigation(checkAnnotation(markupSection, SparqlMarkupType.NAVIGATION));
	}

	private boolean checkSortingAnnotation(Section<SparqlMarkupType> markupSection, String sorting) {
		String annotationString = DefaultMarkupType.getAnnotation(markupSection,
				sorting);
		return annotationString == null || annotationString.equals("true");
	}

	private boolean checkAnnotation(Section<?> markupSection, String annotationName, boolean defaultValue) {
		String annotationString = DefaultMarkupType.getAnnotation(markupSection,
				annotationName);
		return annotationString == null ? defaultValue : annotationString.equals("true");
	}

	private boolean checkAnnotation(Section<?> markupSection, String annotationName) {
		return checkAnnotation(markupSection, annotationName, false);
	}

	private String modifyOrderByInSparqlString(Map<String, String> sortOrder, String sparqlString) {
		StringBuilder sb = new StringBuilder(sparqlString);
		if (sortOrder.isEmpty()) {
			return sb.toString();
		}
		String sparqlTempString = sparqlString.toLowerCase();
		int orderBy = sparqlTempString.lastIndexOf("order by");
		int limit = sparqlTempString.indexOf("limit", orderBy);
		int offset = sparqlTempString.indexOf("offset", orderBy);
		int nextStatement;
		if (limit > 0 && offset > 0) {
			nextStatement = (limit < offset) ? limit : offset;
		}
		else if (limit > 0 && offset < 0) {
			nextStatement = limit;
		}
		else if (limit < 0 && offset > 0) {
			nextStatement = offset;
		}
		else {
			nextStatement = -1;
		}

		StringBuilder sbOrder = new StringBuilder();
		Collection<String> keyCollection = sortOrder.keySet();
		Iterator<String> keyIt = keyCollection.iterator();
		Collection<String> valueCollection = sortOrder.values();
		Iterator<String> valIt = valueCollection.iterator();

		while (keyIt.hasNext()) {
			sbOrder.append(" " + valIt.next() + "(?" + keyIt.next() + ")");
		}

		if (orderBy == -1) {
			if (nextStatement == -1) {
				sb.append(" ORDER BY" + sbOrder.toString());
			}
			else {
				sb.replace(nextStatement, nextStatement, " ORDER BY" + sbOrder.toString());
			}
		}
		else {
			if (nextStatement != -1) {
				sb.replace(orderBy, nextStatement, " ORDER BY" + sbOrder.toString());
			}
			else {
				sb.replace(orderBy, sb.length(), " ORDER BY" + sbOrder.toString());
			}
		}

		return sb.toString();
	}

	private void renderTableSizeSelector(String selectedSize, String id, int max, RenderResult result) {

		result.appendHtml("<div class='toolBar'>");

		String[] sizeArray = getReasonableSizeChoices(max);

		result.appendHtml("<span class=fillText>Show </span>"
				+ "<select id='showLines" + id + "'"
				+ " onchange=\"KNOWWE.plugin.semantic.actions.refreshSparqlRenderer('"
				+ id + "');\">");
		for (String size : sizeArray) {
			if (size.equals(selectedSize)) {
				result.appendHtml("<option selected='selected' value='" + size + "'>" + size
						+ "</option>");
			}
			else {
				result.appendHtml("<option value='" + size + "'>" + size
						+ "</option>");
			}
		}
		result.appendHtml("</select><span class=fillText> lines of </span>  " + max);

		result.appendHtml("<div class='toolSeparator'></div>");
		result.appendHtml("</div>");

	}

	private void renderNavigation(String from, String selectedSize, int max, String id, RenderResult result) {
		int fromInt = Integer.parseInt(from);
		int selectedSizeInt;
		if (!(selectedSize.equals("All"))) {
			selectedSizeInt = Integer.parseInt(selectedSize);
		}
		else {
			selectedSizeInt = max;
		}
		result.appendHtml("<div class='toolBar avoidMenu'>");
		renderToolbarButton(
				"begin.png", "KNOWWE.plugin.semantic.actions.begin('"
						+ id + "', this)",
				(fromInt > 1), result
		);
		renderToolbarButton(
				"back.png", "KNOWWE.plugin.semantic.actions.back('"
						+ id + "', this)",
				(fromInt > 1), result
		);
		result.appendHtml("<span class=fillText> Lines </span>");
		result.appendHtml("<input size=3 id='fromLine" + id + "' type=\"field\" onchange=\"KNOWWE.plugin.semantic.actions.refreshSparqlRenderer('"
				+ id + "', this);\" value='"
				+ from + "'>");
		result.appendHtml("<span class=fillText> to </span>" + (fromInt + selectedSizeInt - 1));
		renderToolbarButton(
				"forward.png", "KNOWWE.plugin.semantic.actions.forward('"
						+ id + "', this)",
				(!selectedSize.equals("All") && (fromInt + selectedSizeInt - 1 < max)), result
		);
		renderToolbarButton(
				"end.png", "KNOWWE.plugin.semantic.actions.end('"
						+ id + "','" + max + "', this)",
				(!selectedSize.equals("All") && (fromInt + selectedSizeInt - 1 < max)), result
		);
		result.appendHtml("</div>");

	}

	private void renderToolbarButton(String icon, String action, boolean enabled, RenderResult builder) {
		int index = icon.lastIndexOf('.');
		String suffix = icon.substring(index);
		icon = icon.substring(0, index);
		if (enabled) {
			builder.appendHtml("<a onclick=\"");
			builder.appendHtml(action);
			builder.appendHtml(";\">");
		}
		builder.appendHtml("<span class='toolButton ");
		builder.appendHtml(enabled ? "enabled" : "disabled");
		builder.appendHtml("'>");
		builder.appendHtml("<img src='KnowWEExtension/navigation_icons/");
		builder.appendHtml(icon);
		if (!enabled) builder.appendHtml("_deactivated");
		builder.appendHtml(suffix).appendHtml("' /></span>");
		if (enabled) {
			builder.appendHtml("</a>");
		}
	}

	private String getJSONCookieString(Section<?> sec, UserContext user) {
		return getCookie(user, "SparqlRenderer-" + sec.getID(), null);
	}

	private String getCookie(UserContext user, String cookieName, String defaultValue) {
		if (user != null && user.getRequest() != null && user.getRequest().getCookies() != null) {
			for (Cookie cookie : user.getRequest().getCookies()) {
				if (cookie.getName().equals(cookieName)) {
					return cookie.getValue();
				}
			}
		}
		return defaultValue;
	}

	private String[] getReasonableSizeChoices(int max) {
		List<String> sizes = new LinkedList<String>();
		String[] sizeArray = new String[] {
				"10", "20", "50", "100", "1000" };
		for (String size : sizeArray) {
			if (Integer.parseInt(size) < max) {
				sizes.add(size);
			}
		}
		sizes.add("All");

		return sizes.toArray(new String[sizes.size()]);

	}

}
