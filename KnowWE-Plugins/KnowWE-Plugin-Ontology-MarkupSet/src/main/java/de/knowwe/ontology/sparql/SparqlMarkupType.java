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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.ontology.ci.RegisteredNameType;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.util.Color;

public class SparqlMarkupType extends DefaultMarkupType {

	public static final String RAW_OUTPUT = "rawOutput";
	public static final String NAVIGATION = "navigation";
	public static final String ZEBRA_MODE = "zebramode";
	public static final String TREE = "tree";
	public static final String SORTING = "sorting";
	public static final String COLUMN_SORTING = "columnSorting";
	public static final String FILTERING = "filtering";
	public static final String BORDER = "border";
	public static final String NAME = "name";
	public static final String RENDER_QUERY = "showQuery";
	public static final String RENDER_MODE = "renderMode";

	public static final String CONSTRUCT = "construct";
	public static final String TIMEOUT = "timeout";
	public static final String LOG_LEVEL = "logLevel";
	public static final String COLUMN_STYLE = "columnStyle";  // usage: @columnStyle: columnName style value
	public static final String TABLE_STYLE = "tableStyle";  // usage: @tableStyle: style value
	public static final String ALLOW_JSPWIKI_MARKUP = "allowJSPWikiMarkup";
	public static final String COLUMN_WIDTH = "columnWidth";
	private static final DefaultMarkup MARKUP;

	public static final String MARKUP_NAME = "Sparql";
	public static final String DISABLED_FILTERING_KEY = "disabledFiltering";

	static {
		MARKUP = new DefaultMarkup(MARKUP_NAME);
		MARKUP.addContentType(new SparqlContentType());
		MARKUP.addAnnotation(RAW_OUTPUT, false, "true", "false");
		MARKUP.addAnnotation(NAVIGATION, false, "true", "false");
		MARKUP.addAnnotationRenderer(NAVIGATION, NothingRenderer.getInstance());
		MARKUP.addAnnotation(RENDER_QUERY, false, "true", "false");
		MARKUP.addAnnotationRenderer(RENDER_QUERY, NothingRenderer.getInstance());
		MARKUP.addAnnotation(ZEBRA_MODE, false, "true", "false");
		MARKUP.addAnnotationRenderer(ZEBRA_MODE, NothingRenderer.getInstance());
		MARKUP.addAnnotation(LOG_LEVEL, false, Color.WARNING.name(),
				Color.ERROR.name());
		MARKUP.addAnnotationRenderer(LOG_LEVEL, NothingRenderer.getInstance());
		MARKUP.addAnnotation(TREE, false, "true", "false");
		MARKUP.addAnnotationRenderer(TREE, NothingRenderer.getInstance());
		MARKUP.addAnnotation(SORTING, false, "true", "false");
		MARKUP.addAnnotationRenderer(SORTING, NothingRenderer.getInstance());
		MARKUP.addAnnotation(FILTERING, false, "true", "false");
		MARKUP.addAnnotationRenderer(FILTERING, NothingRenderer.getInstance());
		MARKUP.addAnnotation(BORDER, false, "true", "false");
		MARKUP.addAnnotationRenderer(BORDER, NothingRenderer.getInstance());
		MARKUP.addAnnotation(RENDER_MODE, false, "PlainText", "HTML", "ToolMenu");
		MARKUP.addAnnotationRenderer(RENDER_MODE, NothingRenderer.getInstance());
		MARKUP.addAnnotation(AsynchronousRenderer.ASYNCHRONOUS, false, "true", "false");
		MARKUP.addAnnotationRenderer(AsynchronousRenderer.ASYNCHRONOUS, NothingRenderer.getInstance());
		MARKUP.addAnnotation(TIMEOUT, false, Pattern.compile("\\d+(\\.\\d+)?|" + TimeStampType.DURATION));
		MARKUP.addAnnotationRenderer(TIMEOUT, NothingRenderer.getInstance());
		MARKUP.addAnnotation(CONSTRUCT, false);
		MARKUP.addAnnotationContentType(CONSTRUCT, new ConstructAnnotationType());
		MARKUP.getAnnotation(CONSTRUCT)
				.setDocumentation("Specify a template how statements should be generated from the result of the query, similar to CONSTRUCT queries.<br>" +
								  "Example: @" + CONSTRUCT + ": ?uri skos:prefLabel ?name");
		MARKUP.addAnnotation(NAME, false);
		MARKUP.addAnnotationRenderer(NAME, NothingRenderer.getInstance());
		MARKUP.addAnnotationContentType(NAME, new RegisteredNameType(SparqlMarkupType.class));
		MARKUP.addAnnotation(COLUMN_SORTING, false, Pattern.compile("\\H+\\h+(" + Arrays.stream(RenderOptions.ColumnSortingType
				.values()).map(Enum::name).collect(Collectors.joining("|")) + ")"));
		MARKUP.addAnnotationRenderer(COLUMN_SORTING, NothingRenderer.getInstance());
		MARKUP.getAnnotation(COLUMN_SORTING)
				.setDocumentation("By default, columns are sorted lexicographically according to their XSD type. Use this annotation to try and force a different sorting criteria.<p>" +
								  "Example for sorting a column 'Formatted_Date' containing a date and additional style and text by that date:<br>" +
								  "@" + COLUMN_SORTING + ": Formatted_Date date<br>" +
								  "Example for sorting a column 'Name' according to their XSD type (this is the default for all columns anyway):<br>"
								  + "@" + COLUMN_SORTING + ": Name value");
		MARKUP.addAnnotation(COLUMN_STYLE, false);
		MARKUP.getAnnotation(COLUMN_STYLE)
				.setDocumentation("Set styles for a specific column of the SPARQL table. Any HTML/CSS style should work.<p>" +
								  "Example for setting the width of column 'Name' to 100px:<br>" +
								  "@" + COLUMN_STYLE + ": Name width 100px<br>" +
								  "Example for disabling filtering for columns 'Name':<br>"
								  + "@" + COLUMN_STYLE + ": Name filter disable");
		MARKUP.addAnnotationRenderer(COLUMN_STYLE, NothingRenderer.getInstance());
		MARKUP.addAnnotation(TABLE_STYLE, false);
		MARKUP.addAnnotationRenderer(TABLE_STYLE, NothingRenderer.getInstance());
		MARKUP.getAnnotation(TABLE_STYLE)
				.setDocumentation("Set styles for the SPARQL table. Any HTML/CSS style should work.<p>" +
								  "Example for setting the width of the table to 1000px:<br>" +
								  "@" + TABLE_STYLE + ": width 1000px");
		MARKUP.addAnnotation(ALLOW_JSPWIKI_MARKUP, false);
		MARKUP.addAnnotationRenderer(ALLOW_JSPWIKI_MARKUP, NothingRenderer.getInstance());
		MARKUP.addAnnotation(COLUMN_WIDTH, false);
		MARKUP.addAnnotationRenderer(COLUMN_WIDTH, NothingRenderer.getInstance());
		PackageManager.addPackageAnnotation(MARKUP);
	}

	public SparqlMarkupType() {
		super(MARKUP);
		this.setRenderer(new SparqlMarkupRenderer());
		this.addCompileScript(new StyleCollectorScript());
	}

	private static class SparqlMarkupRenderer extends Rdf2GoCoreCheckRenderer {

		@Override
		protected String getTitleName(Section<?> section, UserContext user) {
			String title = super.getTitleName(section, user);
			String name = getAnnotation(section, NAME);
			if (!Strings.isBlank(name)) title += ": " + name;
			return title;
		}
	}

	/**
	 * Compiler Script that checks the StyleOptions and stores StyleOptions.
	 * Displays an error if necessary style information is missing.
	 * Stores the StyleOptions in the section so that they can be used.
	 */
	private static class StyleCollectorScript extends DefaultGlobalCompiler.DefaultGlobalScript<SparqlMarkupType> {

		public static final String INVALID_CSS_KEYS = "invalidCssKeys";

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<SparqlMarkupType> section) throws CompilerMessage {
			checkStyle(section, TABLE_STYLE);
			checkStyle(section, COLUMN_STYLE);
			checkStyle(section, COLUMN_WIDTH);
		}

		private void checkStyle(Section<SparqlMarkupType> markupSection, String annotationName) throws CompilerMessage {
			String[] annotationStrings = DefaultMarkupType.getAnnotations(markupSection, annotationName);

			for (String annotationString : annotationStrings) {
				if (Strings.equals(annotationName, COLUMN_STYLE)) {
					annotationString = annotationString.replaceAll("\r", "");
					String[] lines = annotationString.split("\n+");
					if (lines.length != 1) {
						String column = cleanStyleString(lines[0]);
						for (int i = 1; i < lines.length; i++) {
							String[] annoStringArray = Strings.trim(lines[i]).split(" +", 3);
							for (int j = 0; j < annoStringArray.length - 1; j++) {
								annoStringArray[j] = cleanStyleString(annoStringArray[j]);
							}
							if (annoStringArray.length < 2) {
								throw CompilerMessage.error("The style '" + COLUMN_STYLE + "' does not include all necessary information. It has to consist of <columnName> <styleName> <style>");
							}
							handleStyle(markupSection, column, annoStringArray[0], annoStringArray[1]);
						}
					}
					else {
						String[] annoStringArray = annotationString.split(" ", 3);
						for (int i = 0; i < annoStringArray.length; i++) {
							annoStringArray[i] = cleanStyleString(annoStringArray[i]);
						}
						if (annoStringArray.length < 3) {
							throw CompilerMessage.error("The style '" + COLUMN_STYLE + "' does not include all necessary information. It has to consist of <columnName> <styleName> <style>");
						}
						handleStyle(markupSection, annoStringArray[0], annoStringArray[1], annoStringArray[2]);
					}
				}
				else if (Strings.equals(annotationName, TABLE_STYLE)) {
					String[] annoStringArray = annotationString.split(" ", 2);
					if (annoStringArray.length < 2) {
						throw CompilerMessage.error("The style '" + TABLE_STYLE + "' does not include all necessary information. It has to consist of <styleName> <style>");
					}
					handleInvalidCssKey(markupSection, annoStringArray[0]);
					addStyle(markupSection, annotationName, new RenderOptions.StyleOption("table", annoStringArray[0], annoStringArray[1]));
				}
				else if (Strings.equals(annotationName, COLUMN_WIDTH)) {
					String[] annoStringArray = annotationString.split(" ", 2);
					if (annoStringArray.length < 2) {
						throw CompilerMessage.error("The style '" + COLUMN_WIDTH + "' does not include all necessary information. It has to consist of <columnName> <columnWidth>");
					}
					addStyle(markupSection, annotationName, new RenderOptions.StyleOption(annoStringArray[0], "max-width", annoStringArray[1]));
				}
			}

			Set<String> invalidCssKeys = markupSection.getObjectOrDefault(null, INVALID_CSS_KEYS, Collections.emptySet());
			if (!invalidCssKeys.isEmpty()) {
				throw CompilerMessage.error(createUnknownCssExceptionMessage(invalidCssKeys));
			}
		}

		private void handleStyle(Section<SparqlMarkupType> markupSection, String columnName, String styleName, String styleValue) {
			if (styleName.equalsIgnoreCase("filter") && Strings.containsIgnoreCase(styleValue, "disable")) {
				addColumnWithDisabledFilter(markupSection, columnName);
			}
			else {
				addStyle(markupSection, COLUMN_STYLE, new RenderOptions.StyleOption(columnName, styleName, styleValue));
				handleInvalidCssKey(markupSection, styleName);
			}
		}

		private void handleInvalidCssKey(Section<SparqlMarkupType> markupSection, String styleName) {
			if (!cssKeyIsValid(styleName)) {
				markupSection.computeIfAbsent(null, INVALID_CSS_KEYS,
								(compiler, sparqlMarkupTypeSection) -> new LinkedHashSet<>())
						.add(styleName);
			}
		}

		private void addColumnWithDisabledFilter(Section<SparqlMarkupType> markupSection, String columnName) {
			markupSection.computeIfAbsent(null, DISABLED_FILTERING_KEY, (compiler, sparqlMarkupTypeSection) -> new HashSet<>())
					.add(columnName);
		}

		private void addStyle(Section<SparqlMarkupType> markupSection, String annotationName, RenderOptions.StyleOption styleOption) {
			markupSection.computeIfAbsent(null, annotationName, (compiler, sparqlMarkupTypeSection) -> new ArrayList<>())
					.add(styleOption);
		}

		/**
		 * check validity of a css key by regex
		 *
		 * @param key css key to be validated
		 * @return true if key is valid, false if not
		 */
		private boolean cssKeyIsValid(String key) {
			return key.matches("[a-z]+(-?[a-z]+)*");  // a valid css solely consists of words and "-" in between
		}

		private String createUnknownCssExceptionMessage(Collection<String> invalidCssKeys) {
			StringBuilder messageBuffer = new StringBuilder();
			messageBuffer.append("The css key(s) ");
			invalidCssKeys.forEach(messageBuffer::append);
			messageBuffer.append(" are not known by the system and may be wrong");
			return messageBuffer.toString();
		}

		/**
		 * remove a semicolon or colon in the end of the string
		 *
		 * @param styleString: string to be cleaned
		 * @return cleaned String
		 */
		private String cleanStyleString(String styleString) {
			if (styleString.endsWith(":") || styleString.endsWith(";")) {
				return Strings.trim(styleString.substring(0, styleString.length() - 1));
			}
			return Strings.trim(styleString);
		}
	}
}
