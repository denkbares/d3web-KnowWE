/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.compile.variables;

import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Defines a variable holding the URL of another wiki. The variable is used instead of the URL by markups importing
 * from that wiki (<code>@wikiVariable</code> of <code>%%InterWikiImport</code>). With <code>@package</code>, the
 * same variable can get a different value per variant, so the same import markup imports from a different wiki
 * depending on the variant.
 * <p>
 * The definitions are available through the {@link VariableIndex} as soon as their article is registered.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 16.11.2026
 */
public class InterWikiVariableMarkup extends DefaultMarkupType {

	public static final String NAME_ANNOTATION = "name";
	public static final String VALUE_ANNOTATION = "value";
	public static final String DISPLAY_NAME_ANNOTATION = "displayName";

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("InterWikiVariable");
		MARKUP.addAnnotation(NAME_ANNOTATION, true, Pattern.compile("[\\w.-]+"));
		// the name is a term, so the usages of a variable can be found from its definitions and the other way round
		MARKUP.addAnnotationContentType(NAME_ANNOTATION, InterWikiVariableDefinition::new);
		MARKUP.addAnnotation(VALUE_ANNOTATION, true);
		MARKUP.addAnnotation(DISPLAY_NAME_ANNOTATION, false);
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.setTemplate("""
				%%InterWikiVariable
				@name: «variable-name»
				@value: «url-of-the-wiki»
				@package: «package-name»
				%
				""");
		MARKUP.setDocumentation("""
				Defines a variable for the URL of another wiki, to be used by import markups instead of the URL itself,
				e.g. with <code>@wikiVariable</code> of <code>%%InterWikiImport</code>.
				<p>To import from different wikis depending on the variant, define the variable once per variant with
				the same name and a different value, each in a package that is only compiled by that variant.</p>
				""");
		MARKUP.getAnnotation(NAME_ANNOTATION)
				.setDocumentation("The name of the variable. Allowed are letters, digits, '_', '.' and '-'.")
				.setExample("@name: sourceWiki");
		MARKUP.getAnnotation(VALUE_ANNOTATION)
				.setDocumentation("The value of the variable, usually the URL of the wiki to import from.")
				.setExample("@value: https://wiki.example.com");
		MARKUP.getAnnotation(DISPLAY_NAME_ANNOTATION)
				.setDocumentation("The name to show for this value instead of the name of the variable, e.g. in " +
								  "the header of an import using the variable. Useful to tell the wikis of the " +
								  "variants apart.")
				.setExample("@displayName: Source wiki A");
		MARKUP.addExample("""
				%%InterWikiVariable
				@name: sourceWiki
				@value: https://wiki.example.com
				@displayName: Source wiki A
				@package: vehicle-a
				%
				""");
	}

	public InterWikiVariableMarkup() {
		super(MARKUP);
		addCompileScript(new AttachmentArticleCheck());
		setRenderer(new InterWikiVariableRenderer());
	}

	/**
	 * Returns the name of the variable defined by the given section, or null if no name is given.
	 */
	@Nullable
	public static String getVariableName(Section<? extends InterWikiVariableMarkup> section) {
		return trimmedAnnotation(section, NAME_ANNOTATION);
	}

	/**
	 * Returns the value of the variable defined by the given section, or null if no value is given.
	 */
	@Nullable
	public static String getVariableValue(Section<? extends InterWikiVariableMarkup> section) {
		return trimmedAnnotation(section, VALUE_ANNOTATION);
	}

	/**
	 * Returns the name to show for the value of the variable defined by the given section, or null if none is given.
	 * Use the name of the variable then.
	 */
	@Nullable
	public static String getDisplayName(Section<? extends InterWikiVariableMarkup> section) {
		return trimmedAnnotation(section, DISPLAY_NAME_ANNOTATION);
	}

	@Nullable
	private static String trimmedAnnotation(Section<? extends InterWikiVariableMarkup> section, String name) {
		String value = Strings.trim(DefaultMarkupType.getAnnotation(section, name));
		return Strings.isBlank(value) ? null : value;
	}

	/**
	 * The {@link VariableIndex} ignores definitions in compiled attachments, as the attachments to compile themselves
	 * depend on the variables. This script tells the user so.
	 */
	private static class AttachmentArticleCheck extends DefaultGlobalScript<InterWikiVariableMarkup> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<InterWikiVariableMarkup> section) throws CompilerMessage {
			if (KnowWEUtils.isAttachmentArticle(section.getArticle())) {
				throw CompilerMessage.warning("Variables defined in imported or attached content are ignored. " +
											  "Define the variable on a page of this wiki instead.");
			}
		}
	}
}
