/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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

package de.knowwe.core.compile.packaging;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.PredicateParser;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.utils.Patterns;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Represents a rule about whether a section should be compiled by a PackageCompiler.<br>
 * <p>
 * <b>Simple Example:</b> package-A AND NOT package-B AND package-C<br>
 * This would mean, that the section is only compiled by package compiler compiling package-A and package-C but not
 * package-B
 * </p>
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 27.10.2020
 */
public class PackageRule extends AbstractType {

	private static final PredicateParser PREDICATE_PARSER = getPredicateParser();
	private static final PredicateParser.ParsedPredicate DEFAULT = generateDefault();

	@NotNull
	private static PredicateParser getPredicateParser() {
		PredicateParser predicateParser = new PredicateParser();
		predicateParser.checkVariables(s -> true);
		predicateParser.isBoolean(s -> true);
		return predicateParser;
	}

	@NotNull
	private static PredicateParser.ParsedPredicate generateDefault() {
		try {
			return PREDICATE_PARSER.parse(PackageManager.DEFAULT_PACKAGE);
		}
		catch (PredicateParser.ParseException e) {
			throw new IllegalStateException(e);
		}
	}

	public PackageRule() {
		this(true);
	}

	public PackageRule(boolean warnForNotCompiledPackage) {
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		PackageTerm packageTerm = new PackageTerm(warnForNotCompiledPackage);
		// we create a term for quoted elements or optionally for everything except the control tokens of PredicateParser
		packageTerm.setSectionFinder(new RegexSectionFinder(Pattern.compile(Patterns.QUOTED + "|\\b(?:(?!AND|OR|NOT)[^|&!<>()=~\\s]+)\\b")));
		addChildType(packageTerm);
		addChildType(new SyntaxType());
		addCompileScript(new DefaultGlobalScript<PackageRule>() {
			@Override
			public void compile(DefaultGlobalCompiler compiler, Section<PackageRule> section) throws CompilerMessage {
				try {
					PREDICATE_PARSER.parse(section.getText());
				}
				catch (PredicateParser.ParseException e) {
					throw CompilerMessage.error("Unable to parse package rule: " + e.getMessage());
				}
			}
		});
	}

	public boolean isOrdinaryPackage(Section<PackageRule> section) {
		return $(section).successor(PackageTerm.class).count() == 1 && $(section).successor(SyntaxType.class).isEmpty();
	}

	@NotNull
	public String getOrdinaryPackage(Section<PackageRule> section) {
		String packageName = $(section).successor(PackageTerm.class).mapFirst(s -> s.get().getTermName(s));
		if (packageName == null) {
			// this should not happen if #isOrdinaryPackage was checke before calling this
			throw new IllegalStateException("No package term found in package rule: " + section.getText());
		}
		return packageName;
	}

	@NotNull
	public PredicateParser.ParsedPredicate getRule(Section<PackageRule> section) {
		return parseRule(section.getText());
	}

	public static PredicateParser.ParsedPredicate parseRule(String packageRule) {
		try {
			return PREDICATE_PARSER.parse(packageRule);
		}
		catch (PredicateParser.ParseException e) {
			// we don't need error handling here, because we already show KDOM messages to the user for invalid rules
			return DEFAULT;
		}
	}

	/**
	 * Checks whether the given set of package names satisfy the package rule of this section.
	 *
	 * @param section      the section containing the package rule
	 * @param packageNames the package names to check
	 * @return true if the given package names satisfy this package rule, false otherwise
	 */
	public boolean test(Section<PackageRule> section, String... packageNames) {
		return section.get().getRule(section).test(new PackagesValueProvider(packageNames));
	}

	private static class SyntaxType extends AbstractType {
		public SyntaxType() {
			setSectionFinder(AllTextFinderTrimmed.getInstance());
			setRenderer((section, user, result) -> result.appendHtmlElement("span", section.getText(), "style", "color: grey"));
		}
	}

	public static class PackagesValueProvider implements PredicateParser.ValueProvider {

		private final Set<String> packageNames;

		public PackagesValueProvider(String... packageNames) {
			this.packageNames = new LinkedHashSet<>();
			Collections.addAll(this.packageNames, packageNames);
		}

		@Override
		public @Nullable Collection<String> get(@NotNull String variable) {
			return packageNames.contains(variable) ? List.of("true") : List.of("false");
		}
	}
}
