/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.d3web.we.solutionpanel;

import de.d3web.we.object.QuestionnaireReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.ReRenderSectionMarkerRenderer;

/**
 * This type defines the possible annotations of the ShowSoltions markup.
 * 
 * @author Joachim Baumeister (denkbares GmbH)
 * @created 22.10.2010
 */
public class ShowSolutionsType extends DefaultMarkupType {

	private static final String ANNOTATION_MASTER = "master";
	private static final String ANNOTATION_ABSTRACTIONS = "show_abstractions";
	private static final String ANNOTATION_SUGGESTED = "show_suggested";
	private static final String ANNOTATION_ESTABLISHED = "show_established";
	private static final String ANNOTATION_EXCLUDED = "show_excluded";
	private static final String ONLY_DERIVATIONS = "only_derivations";
	private static final String EXCEPT_DERIVATIONS = "except_derivations";
	private static final String SHOW_DIGITS = "show_digits";

	public enum BoolValue {
		TRUE, FALSE
	};

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("ShowSolutions");
		MARKUP.addAnnotation(ANNOTATION_MASTER, true);
		MARKUP.addAnnotation(ANNOTATION_ESTABLISHED, false, BoolValue.values());
		MARKUP.addAnnotation(ANNOTATION_SUGGESTED, false, BoolValue.values());
		MARKUP.addAnnotation(ANNOTATION_EXCLUDED, false, BoolValue.values());
		MARKUP.addAnnotation(ANNOTATION_ABSTRACTIONS, false, BoolValue.values());
		MARKUP.addAnnotation(ONLY_DERIVATIONS, false);
		MARKUP.addAnnotation(EXCEPT_DERIVATIONS, false);
		MARKUP.addAnnotation(SHOW_DIGITS, false);

		QuestionnaireReference qc = new QuestionnaireReference();
		qc.setSectionFinder(new AllTextFinderTrimmed());
		MARKUP.addAnnotationContentType(ONLY_DERIVATIONS, qc);
	}

	public ShowSolutionsType() {
		super(MARKUP);
		this.setIgnorePackageCompile(true);
		this.setRenderer(new ShowSolutionsRenderer());
		DefaultMarkupType.getContentType(this).setRenderer(
				new ReRenderSectionMarkerRenderer(new ShowSolutionsContentRenderer()));
	}

	public static String getText(Section<ShowSolutionsType> sec) {
		assert sec.get() instanceof ShowSolutionsType;
		return DefaultMarkupType.getContent(sec);
	}

	public static String getMaster(Section<ShowSolutionsType> section) {
		assert section.get() instanceof ShowSolutionsType;
		return DefaultMarkupType.getAnnotation(section, ANNOTATION_MASTER);
	}

	public static String[] getAllowedParents(Section<ShowSolutionsType> section) {
		assert section.get() instanceof ShowSolutionsType;
		return DefaultMarkupType.getAnnotations(section, ONLY_DERIVATIONS);
	}

	public static String[] getExcludedParents(Section<ShowSolutionsType> section) {
		assert section.get() instanceof ShowSolutionsType;
		return DefaultMarkupType.getAnnotations(section, EXCEPT_DERIVATIONS);
	}

	public static boolean shouldShowEstablished(Section<ShowSolutionsType> section) {
		if (foundAnnotation(section, ANNOTATION_ESTABLISHED)) {
			return shouldShow(section, ANNOTATION_ESTABLISHED);
		}
		// show established solutions by default, when no option was selected
		else {
			return true;
		}
	}

	private static boolean foundAnnotation(Section<ShowSolutionsType> section, String annotation) {
		assert section.get() instanceof ShowSolutionsType;
		String value = DefaultMarkupType.getAnnotation(section, annotation);
		if (!MARKUP.getAnnotation(annotation).matches(value)) return false;
		else return true;
	}

	public static boolean shouldShowSuggested(Section<ShowSolutionsType> section) {
		return shouldShow(section, ANNOTATION_SUGGESTED);
	}

	public static boolean shouldShowExcluded(Section<ShowSolutionsType> section) {
		return shouldShow(section, ANNOTATION_EXCLUDED);
	}

	public static boolean shouldShowAbstractions(Section<ShowSolutionsType> section) {
		return shouldShow(section, ANNOTATION_ABSTRACTIONS);
	}

	public static int numberOfShownDigits(Section<ShowSolutionsType> section) {
		assert section.get() instanceof ShowSolutionsType;
		String val = DefaultMarkupType.getAnnotation(section, SHOW_DIGITS);
		int iVal = 10;
		try {
			iVal = Integer.parseInt(val);
		}
		catch (NumberFormatException e) {
			// TODO some error handling here
		}
		return iVal;
	}

	private static boolean shouldShow(Section<ShowSolutionsType> section, String annotation) {
		assert section.get() instanceof ShowSolutionsType;
		String value = DefaultMarkupType.getAnnotation(section, annotation);
		if (!MARKUP.getAnnotation(annotation).matches(value)) return false;
		else return convert(BoolValue.valueOf(value.toUpperCase()));
	}

	private static boolean convert(BoolValue value) {
		if (value == BoolValue.TRUE) {
			return true;
		}
		else {
			return false;
		}

	}

}
