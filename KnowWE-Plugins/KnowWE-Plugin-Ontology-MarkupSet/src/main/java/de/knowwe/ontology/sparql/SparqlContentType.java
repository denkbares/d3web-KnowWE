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
package de.knowwe.ontology.sparql;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderPlusEmpty;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.util.Color;

import static de.knowwe.core.kdom.parsing.Sections.$;

public class SparqlContentType extends AbstractType implements SparqlType {

	public SparqlContentType() {
		this.setSectionFinder(AllTextFinderPlusEmpty.getInstance());
		this.setRenderer(new AsynchronousRenderer(new ReRenderSectionMarkerRenderer(new SparqlContentDecoratingRenderer())));

		// add type (with very low priority) consuming all plain text and rendering it while masking JSPWiki syntax
		AnonymousType queryText = new AnonymousType("QueryText");
		queryText.setSectionFinder(AllTextFinder.getInstance());
		queryText.setRenderer((section, user, result) -> result.append(KnowWEUtils.maskJSPWikiMarkup(Strings.encodeHtml(section
				.getText()))));
		this.addChildType(100, queryText);
	}

	public static boolean isConstructQuery(Section<?> section) {
		return (section.getParent().get() instanceof SparqlMarkupType) && Strings.startsWithIgnoreCase(section.getText()
				.trim(), "construct");
	}

	public static boolean checkAnnotation(Section<?> markupSection, String annotationName, boolean defaultValue) {
		String annotationString = DefaultMarkupType.getAnnotation(markupSection, annotationName);
		return annotationString == null ? defaultValue : annotationString.equals("true");
	}

	public static long getTimeout(Section<? extends DefaultMarkupType> markupSection) {
		return getTimeout(markupSection, Rdf2GoCore.DEFAULT_TIMEOUT);
	}

	public static long getTimeout(Section<? extends DefaultMarkupType> markupSection, long defaultTimeOut) {
		String timeoutString = DefaultMarkupType.getAnnotation(markupSection, SparqlMarkupType.TIMEOUT);
		if (timeoutString != null) {
			try {
				defaultTimeOut = TimeStampType.getTimeInMillis(timeoutString);
			}
			catch (NumberFormatException e) {
				// if we can not parse (because there is no time unit maybe, we just try parseDouble
				defaultTimeOut = (long) (Double.parseDouble(timeoutString) * TimeUnit.SECONDS.toMillis(1));
				// if this also fails, we will have the default timeout
			}
		}
		return defaultTimeOut;
	}

	@Override
	@Nullable
	public String getSparqlQuery(Section<? extends SparqlType> section, UserContext context) {
		Section<SparqlMarkupType> markupSection = Sections.ancestor(section, SparqlMarkupType.class);
		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(context, markupSection);
		if (core == null) return null;
		return Rdf2GoUtils.createSparqlString(core, section.getText());
	}

	@Override
	public RenderOptions getRenderOptions(Section<? extends SparqlType> section, UserContext context) {

		RenderOptions renderOpts = new RenderOptions(section.getID());
		Section<DefaultMarkupType> markupSection = $(section).closest(DefaultMarkupType.class).getFirst();

		renderOpts.setRdf2GoCore(Rdf2GoUtils.getRdf2GoCore(context, markupSection));
		renderOpts.setRawOutput(checkAnnotation(markupSection, SparqlMarkupType.RAW_OUTPUT));
		setColumnSorting(markupSection, renderOpts);
		renderOpts.setSorting(checkAnnotation(markupSection, SparqlMarkupType.SORTING, true));
		renderOpts.setFiltering(checkAnnotation(markupSection, SparqlMarkupType.FILTERING, true));
		renderOpts.setZebraMode(checkAnnotation(markupSection, SparqlMarkupType.ZEBRA_MODE, true));
		renderOpts.setTree(checkAnnotation(markupSection, SparqlMarkupType.TREE, false));
		renderOpts.setBorder(checkAnnotation(markupSection, SparqlMarkupType.BORDER, true));
		renderOpts.setNavigation(checkAnnotation(markupSection, SparqlMarkupType.NAVIGATION, true));
		renderOpts.setColor(checkColor(markupSection, SparqlMarkupType.LOG_LEVEL, Color.NONE));
		renderOpts.setColumnStyles(getStyles(markupSection, SparqlMarkupType.COLUMN_STYLE));
		getColumnsWithDisabledFiltering(markupSection).forEach(renderOpts::disableFilterForColumn);
		renderOpts.setColumnStyles(getStyles(markupSection, SparqlMarkupType.COLUMN_STYLE));
		renderOpts.setTableStyles(getStyles(markupSection, SparqlMarkupType.TABLE_STYLE));
		renderOpts.setAllowJSPWikiMarkup(checkAnnotation(markupSection, SparqlMarkupType.ALLOW_JSPWIKI_MARKUP, true));
		renderOpts.setColumnWidth(getStyles(markupSection, SparqlMarkupType.COLUMN_WIDTH));
		renderOpts.setTimeout(getTimeout(markupSection));
		renderOpts.setRenderMode(getRenderMode(markupSection));

		return renderOpts;
	}

	private void setColumnSorting(Section<DefaultMarkupType> markupSection, RenderOptions renderOpts) {
		for (String annotation : DefaultMarkupType.getAnnotations(markupSection, SparqlMarkupType.COLUMN_SORTING)) {
			String[] split = annotation.split("\\h+");
			if (split.length != 2) continue;
			try {
				renderOpts.setColumnSorting(split[0], RenderOptions.ColumnSortingType.valueOf(split[1]));
			}
			catch (IllegalArgumentException e) {
				// nothing to do, error message should already be shown due do invalid annotation format
			}
		}
	}

	protected Set<String> getColumnsWithDisabledFiltering(Section<DefaultMarkupType> markupSection) {
		if (markupSection == null) return Collections.emptySet();
		return markupSection.getObjectOrDefault(null, SparqlMarkupType.DISABLED_FILTERING_KEY, Collections.emptySet());
	}

	private RenderOptions.RenderMode getRenderMode(Section<?> section) {
		String annotation = DefaultMarkupType.getAnnotation(section, SparqlMarkupType.RENDER_MODE);
		if (annotation == null) return RenderOptions.RenderMode.HTML;
		try {
			return RenderOptions.RenderMode.valueOf(annotation);
		}
		catch (IllegalArgumentException e) {
			return RenderOptions.RenderMode.HTML;
		}
	}

	protected List<RenderOptions.StyleOption> getStyles(Section<DefaultMarkupType> markupSection, String columnstyle) {
		return markupSection == null ? Collections.emptyList() : markupSection.getObjectOrDefault(null, columnstyle, Collections
				.emptyList());
	}

	private Color checkColor(Section<DefaultMarkupType> markupSection, String logLevel, Color none) {
		String logLevelString = DefaultMarkupType.getAnnotation(markupSection,
				logLevel);
		if (logLevelString == null) {
			return none;
		}
		return switch (logLevelString.toLowerCase()) {
			case "warning" -> Color.WARNING;
			case "error" -> Color.ERROR;
			default -> none;
		};
	}

	private boolean checkAnnotation(Section<?> markupSection, String annotationName) {
		return checkAnnotation(markupSection, annotationName, false);
	}
}
