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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

public class SparqlContentRenderer implements Renderer {

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult result) {

		KnowWEUtils.cleanupSectionCookies(user, Pattern.compile("^SparqlRenderer-(.+)$"), 1);

		Section<SparqlMarkupType> markupSection = Sections.ancestor(sec,
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

		if (sparqlString.toLowerCase().startsWith("construct")) {
			result.appendHtml("<tt>");
			result.append(sec.getText());
			result.appendHtml("</tt>");
		}
		else {
			RenderOptions renderOpts = new RenderOptions(markupSection.getID(), user);
			renderOpts.setRdf2GoCore(core);
			setRenderOptions(markupSection, renderOpts);

			SparqlResultRenderer.getInstance().renderSparqlQuery(sparqlString, renderOpts, user, result);

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

		renderOpts.setTimeout(getTimeout(markupSection));
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

	private void renderTableSizeSelector(RenderOptions options, String id, int max, RenderResult result) {

		result.appendHtml("<div class='toolBar'>");

		String[] sizeArray = getReasonableSizeChoices(max);

		result.appendHtml("<span class=fillText>Show </span>"
				+ "<select id='showLines" + id + "'"
				+ " onchange=\"KNOWWE.plugin.semantic.actions.refreshSparqlRenderer('"
				+ id + "', this);\">");
		for (String size : sizeArray) {
			if (size.equals(options.getNavigationLimit() + "")
					|| options.isShowAll() && size.equals("All")) {
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

	private void renderNavigation(RenderOptions options, int max, String id, RenderResult result) {
		int from = options.getNavigationOffset();
		int selectedSizeInt;
		if (options.isShowAll()) {
			selectedSizeInt = max;
		}
		else {
			selectedSizeInt = options.getNavigationLimit();
		}
		result.appendHtml("<div class='toolBar avoidMenu'>");
		renderToolbarButton(
				"begin.png", "KNOWWE.plugin.semantic.actions.begin('"
						+ id + "', this)",
				(from > 1), result
		);
		renderToolbarButton(
				"back.png", "KNOWWE.plugin.semantic.actions.back('"
						+ id + "', this)",
				(from > 1), result
		);
		result.appendHtml("<span class=fillText> Lines </span>");
		result.appendHtml("<input size=3 id='fromLine" + id + "' type=\"field\" onchange=\"KNOWWE.plugin.semantic.actions.refreshSparqlRenderer('"
				+ id + "', this);\" value='"
				+ from + "'>");
		result.appendHtml("<span class=fillText> to </span>" + (from + selectedSizeInt - 1));
		renderToolbarButton(
				"forward.png", "KNOWWE.plugin.semantic.actions.forward('"
						+ id + "', this)",
				(!options.isShowAll() && (from + selectedSizeInt - 1 < max)), result
		);
		renderToolbarButton(
				"end.png", "KNOWWE.plugin.semantic.actions.end('"
						+ id + "','" + max + "', this)",
				(!options.isShowAll() && (from + selectedSizeInt - 1 < max)), result
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
