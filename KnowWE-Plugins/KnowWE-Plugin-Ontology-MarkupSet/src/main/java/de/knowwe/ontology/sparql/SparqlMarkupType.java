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
import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.NothingRenderer;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupCompileScript;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.rdf2go.sparql.utils.RenderOptions;
import de.knowwe.util.Color;

public class SparqlMarkupType extends DefaultMarkupType {

	public static final String RAW_OUTPUT = "rawOutput";
	public static final String NAVIGATION = "navigation";
	public static final String ZEBRAMODE = "zebramode";
	public static final String TREE = "tree";
	public static final String SORTING = "sorting";
	public static final String BORDER = "border";
	public static final String NAME = "name";
	public static final String RENDER_QUERY = "showQuery";
	public static final String RENDER_MODE = "renderMode";
	public static final String TIMEOUT = "timeout";
	public static final String LOG_LEVEL = "logLevel";
	public static final String COLUMNSTYLE = "columnStyle";  // usage: @columnStyle: columnName style value
	public static final String TABLESTYLE = "tableStyle";  // usage: @tableStyle: style value
	public static final String ALLOW_JSPWIKIMARKUP = "allowJSPWikiMarkup";
	public static final String COLUMNWIDTH = "columnWidth";
	private static final DefaultMarkup MARKUP;

	public static final String MARKUP_NAME = "Sparql";

	static {
		MARKUP = new DefaultMarkup(MARKUP_NAME);
		MARKUP.addContentType(new SparqlContentType());
		MARKUP.addAnnotation(RAW_OUTPUT, false, "true", "false");
		MARKUP.addAnnotation(NAVIGATION, false, "true", "false");
		MARKUP.addAnnotationRenderer(NAVIGATION, NothingRenderer.getInstance());
		MARKUP.addAnnotation(RENDER_QUERY, false, "true", "false");
		MARKUP.addAnnotationRenderer(RENDER_QUERY, NothingRenderer.getInstance());
		MARKUP.addAnnotation(ZEBRAMODE, false, "true", "false");
		MARKUP.addAnnotationRenderer(ZEBRAMODE, NothingRenderer.getInstance());
		MARKUP.addAnnotation(LOG_LEVEL, false, Color.WARNING.name(),
				Color.ERROR.name());
		MARKUP.addAnnotationRenderer(LOG_LEVEL, NothingRenderer.getInstance());
		MARKUP.addAnnotation(TREE, false, "true", "false");
		MARKUP.addAnnotationRenderer(TREE, NothingRenderer.getInstance());
		MARKUP.addAnnotation(SORTING, false, "true", "false");
		MARKUP.addAnnotationRenderer(SORTING, NothingRenderer.getInstance());
		MARKUP.addAnnotation(BORDER, false, "true", "false");
		MARKUP.addAnnotationRenderer(BORDER, NothingRenderer.getInstance());
		MARKUP.addAnnotation(RENDER_MODE, false, "PlainText", "HTML", "ToolMenu");
		MARKUP.addAnnotationRenderer(RENDER_MODE, NothingRenderer.getInstance());
		MARKUP.addAnnotation(AsynchronousRenderer.ASYNCHRONOUS, false, "true", "false");
		MARKUP.addAnnotationRenderer(AsynchronousRenderer.ASYNCHRONOUS, NothingRenderer.getInstance());
		MARKUP.addAnnotation(TIMEOUT, false, Pattern.compile("\\d+(\\.\\d+)?|" + TimeStampType.DURATION));
		MARKUP.addAnnotationRenderer(TIMEOUT, NothingRenderer.getInstance());
		MARKUP.addAnnotation(NAME, false);
		MARKUP.addAnnotationRenderer(NAME, NothingRenderer.getInstance());
		MARKUP.addAnnotation(COLUMNSTYLE, false);
		MARKUP.getAnnotation(COLUMNSTYLE)
				.setDocumentation("Set styles for a specific column of the SPARQL table. Any HTML/CSS style should work.<p>" +
						"Example for setting the width of column 'Name' to 100px:<br>" +
						"@" + COLUMNSTYLE + ": Name width 100px");
		MARKUP.addAnnotationRenderer(COLUMNSTYLE, NothingRenderer.getInstance());
		MARKUP.addAnnotation(TABLESTYLE, false);
		MARKUP.addAnnotationRenderer(TABLESTYLE, NothingRenderer.getInstance());
		MARKUP.getAnnotation(TABLESTYLE)
				.setDocumentation("Set styles for the SPARQL table. Any HTML/CSS style should work.<p>" +
						"Example for setting the width of the table to 1000px:<br>" +
						"@" + TABLESTYLE + ": width 1000px");
		MARKUP.addAnnotation(ALLOW_JSPWIKIMARKUP, false);
		MARKUP.addAnnotationRenderer(ALLOW_JSPWIKIMARKUP, NothingRenderer.getInstance());
		MARKUP.addAnnotation(COLUMNWIDTH, false);
		MARKUP.addAnnotationRenderer(COLUMNWIDTH, NothingRenderer.getInstance());
		// TODO: replace class SparqlNameRegistrationScript by content type
		// m.addAnnotationContentType(NAME, new SparqlNameDefinition());
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
	private class StyleCollectorScript extends DefaultGlobalCompiler.DefaultGlobalScript<SparqlMarkupType> {
		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<SparqlMarkupType> section) throws CompilerMessage {
			section.storeObject(TABLESTYLE, new ArrayList<RenderOptions.StyleOption>());
			section.storeObject(COLUMNSTYLE, new ArrayList<RenderOptions.StyleOption>());
			section.storeObject(COLUMNWIDTH, new ArrayList<RenderOptions.StyleOption>());

			try {
				section.storeObject(TABLESTYLE, checkStyle(section, TABLESTYLE));
				section.storeObject(COLUMNSTYLE, checkStyle(section, COLUMNSTYLE));
				section.storeObject(COLUMNWIDTH, checkStyle(section, COLUMNWIDTH));
			} catch (IncorrectStyleOptionException e) {
				throw CompilerMessage.error(e.getMessage());
			}
		}


		private List<RenderOptions.StyleOption> checkStyle(Section<SparqlMarkupType> markupSection, String annotationName) throws IncorrectStyleOptionException {
			String[] annotationStrings = DefaultMarkupType.getAnnotations(markupSection,
					annotationName);
			List<RenderOptions.StyleOption> styles = new ArrayList<>();

			for (String annotationString : annotationStrings) {
				if (Strings.equals(annotationName, COLUMNSTYLE)) {
					annotationString = annotationString.replaceAll("\r", "");
					String[] lines = annotationString.split("\n");
					if (lines.length != 1){
						String column = cleanStyleString(lines[0]);
						for (int i = 1; i < lines.length; i++) {
							String[] annoStringArray = lines[i].split(" ", 3);
							for (int j = 0; j < annoStringArray.length - 1; j++) {
								annoStringArray[j] = cleanStyleString(annoStringArray[j]);
							}
							if (annoStringArray.length < 2) {
								throw new IncorrectStyleOptionException("The style '" + COLUMNSTYLE + "' does not include all necessary information. It has to consist of <columnName> <styleName> <style>");
							}
							styles.add(new RenderOptions.StyleOption(column, annoStringArray[0], annoStringArray[1]));
						}
					} else {
						String[] annoStringArray = annotationString.split(" ", 3);
						for (int i = 0; i < annoStringArray.length; i++) {
							annoStringArray[i] = cleanStyleString(annoStringArray[i]);
						}
						if (annoStringArray.length < 3) {
							throw new IncorrectStyleOptionException("The style '" + COLUMNSTYLE + "' does not include all necessary information. It has to consist of <columnName> <styleName> <style>");
						}
						styles.add(new RenderOptions.StyleOption(annoStringArray[0], annoStringArray[1], annoStringArray[2]));
					}
				}
				else if (Strings.equals(annotationName, TABLESTYLE)) {
					String[] annoStringArray = annotationString.split(" ", 2);
					if (annoStringArray.length < 2) {
						throw new IncorrectStyleOptionException("The style '" + TABLESTYLE + "' does not include all necessary information. It has to consist of <styleName> <style>");
					}
					styles.add(new RenderOptions.StyleOption("table", annoStringArray[0], annoStringArray[1]));
				}
				else if (Strings.equals(annotationName, COLUMNWIDTH)) {
					String[] annoStringArray = annotationString.split(" ", 2);
					if (annoStringArray.length < 2) {
						throw new IncorrectStyleOptionException("The style '" + COLUMNWIDTH + "' does not include all necessary information. It has to consist of <columnName> <columnWidth>");
					}
					styles.add(new RenderOptions.StyleOption(annoStringArray[0], "max-width", annoStringArray[1]));
				}
			}
			return styles;
		}

		/**
		 * remove a semicolon or colon in the end of the string
		 * @param styleString: string to be cleaned
		 * @return cleaned String
		 */
		private String cleanStyleString(String styleString) {
			if (styleString.endsWith(":") || styleString.endsWith(";")) {
				return styleString.substring(0, styleString.length() - 1);
			}
			return styleString;
		}

		public class IncorrectStyleOptionException extends Exception {
			public IncorrectStyleOptionException(String errorMessage) {
				super(errorMessage);
			}
		}
	}

}
