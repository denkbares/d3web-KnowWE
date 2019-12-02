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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
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
		String annotationString = DefaultMarkupType.getAnnotation(markupSection,
				annotationName);
		return annotationString == null ? defaultValue : annotationString.equals("true");
	}

	public static long getTimeout(Section<? extends DefaultMarkupType> markupSection) {
		String timeoutString = DefaultMarkupType.getAnnotation(markupSection, SparqlMarkupType.TIMEOUT);
		long timeOutMillis = Rdf2GoCore.DEFAULT_TIMEOUT;
		if (timeoutString != null) {
			try {
				timeOutMillis = TimeStampType.getTimeInMillis(timeoutString);
			}
			catch (NumberFormatException e) {
				// if we can not parse (because there is no time unit maybe, we just try parseDouble
				timeOutMillis = (long) (Double.parseDouble(timeoutString) * TimeUnit.SECONDS.toMillis(1));
				// if this also fails, we will have the default timeout
			}
		}
		return timeOutMillis;
	}

	@Override
	public String getSparqlQuery(Section<? extends SparqlType> section, UserContext context) {
		Section<SparqlMarkupType> markupSection = Sections.ancestor(section, SparqlMarkupType.class);
		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(markupSection);
		return Rdf2GoUtils.createSparqlString(core, section.getText());
	}

	@Override
	public RenderOptions getRenderOptions(Section<? extends SparqlType> section, UserContext context) {
		Section<DefaultMarkupType> markupSection = Sections.ancestor(section, DefaultMarkupType.class);

		RenderOptions renderOpts = new RenderOptions(section.getID());

		Rdf2GoCore core = Rdf2GoUtils.getRdf2GoCore(markupSection);
		renderOpts.setRdf2GoCore(core);
		setRenderOptions(markupSection, renderOpts);

		return renderOpts;
	}

	private void setRenderOptions(Section<DefaultMarkupType> markupSection, RenderOptions renderOpts) {
		renderOpts.setRawOutput(checkAnnotation(markupSection, SparqlMarkupType.RAW_OUTPUT));
		renderOpts.setSorting(checkSortingAnnotation(markupSection,
				SparqlMarkupType.SORTING));
		renderOpts.setZebraMode(checkAnnotation(markupSection, SparqlMarkupType.ZEBRAMODE, true));
		renderOpts.setTree(Boolean.parseBoolean(DefaultMarkupType.getAnnotation(markupSection,
				SparqlMarkupType.TREE)));
		renderOpts.setBorder(checkAnnotation(markupSection, SparqlMarkupType.BORDER, true));
		renderOpts.setNavigation(checkAnnotation(markupSection, SparqlMarkupType.NAVIGATION, true));
		renderOpts.setColor(checkColor(markupSection, SparqlMarkupType.LOG_LEVEL, Color.NONE));
		renderOpts.setColumnStyles(checkStyle(markupSection, SparqlMarkupType.COLUMNSTYLE));
		renderOpts.setTableStyles(checkStyle(markupSection, SparqlMarkupType.TABLESTYLE));
		renderOpts.setAllowJSPWikiMarkup(checkAnnotation(markupSection, SparqlMarkupType.ALLOW_JSPWIKIMARKUP, true));

		renderOpts.setTimeout(getTimeout(markupSection));
	}

	private Color checkColor(Section<DefaultMarkupType> markupSection, String logLevel, Color none) {
		String logLevelString = DefaultMarkupType.getAnnotation(markupSection,
				logLevel);
		if (logLevelString == null) {
			return none;
		}
		switch (logLevelString.toLowerCase()) {
			case "warning":
				return Color.WARNING;
			case "error":
				return Color.ERROR;
			default:
				return none;
		}
	}

	private List<RenderOptions.StyleOption> checkStyle(Section<DefaultMarkupType> markupSection, String annotationName) {
		String[] annotationStrings = DefaultMarkupType.getAnnotations(markupSection,
				annotationName);
		List<RenderOptions.StyleOption> styles = new ArrayList<>();

		for (String annotationString : annotationStrings) {
			if (Strings.equals(annotationName, SparqlMarkupType.COLUMNSTYLE)) {
				String[] annoStringArray = annotationString.split(" ", 3);
				for (int i = 0; i < annoStringArray.length; i++) {
					if (annoStringArray[i].endsWith(":")) {
						annoStringArray[i] = annoStringArray[i].substring(0, annoStringArray[i].length()-1);
					}
				}
				if (annoStringArray.length < 3) {
					Log.severe("The style '" + annotationString + "' does not include all necessary information. It has to consist of <columnName styleName style>");
					continue;
				}
				styles.add(new RenderOptions.StyleOption(annoStringArray[0], annoStringArray[1], annoStringArray[2]));

			} else if (Strings.equals(annotationName, SparqlMarkupType.TABLESTYLE)) {
				String[] annoStringArray = annotationString.split(" ", 2);
				if (annoStringArray.length < 2) {
					Log.severe("The style '" + annotationString + "' does not include all necessary information. It has to consist of <styleName style>");
					continue;
				}
				styles.add(new RenderOptions.StyleOption("table", annoStringArray[0], annoStringArray[1]));
			}
		}
		return styles;
	}

	private boolean checkSortingAnnotation(Section<DefaultMarkupType> markupSection, String sorting) {
		String annotationString = DefaultMarkupType.getAnnotation(markupSection,
				sorting);
		return annotationString == null || annotationString.equals("true");
	}

	private boolean checkAnnotation(Section<?> markupSection, String annotationName) {
		return checkAnnotation(markupSection, annotationName, false);
	}
}
