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

package de.knowwe.kdom.defaultMarkup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.PredicateParser;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.AttachmentManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageRule;
import de.knowwe.core.kdom.basicType.AttachmentCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Common functionality for the package registration and unregistration of DefaultMarkups
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 06.11.2020
 */
public abstract class DefaultMarkupPackageScript {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMarkupPackageScript.class);
	private static final String REGISTERED_PACKAGE_KEY = "RegisteredPackages";
	/**
	 * Package names that can be used in a package rule as they are, all others have to be quoted
	 */
	private static final Pattern PLAIN_RULE_VARIABLE = Pattern.compile("[A-Za-z_][\\w.-]*");
	private static final Set<String> RULE_KEYWORDS = Set.of("AND", "OR", "NOT", "null", "true", "false");

	@NotNull
	protected List<String> getAllPackageNames(PackageInfo packageInfo) {
		return Stream.concat(packageInfo.packageNames.stream(), packageInfo.packageRules.stream()
						.flatMap((PredicateParser.ParsedPredicate parsedPredicate) -> parsedPredicate.getVariables().stream()))
				.collect(Collectors.toList());
	}

	/**
	 * Returns the packages the given default markup section belongs to according to the defined annotations. If there
	 * are no such annotations, the default packages for the article are returned. In case the section is part of an
	 * article based on a compiled attachment, we also check the compiling %%Attachment markups for packages, and
	 * whether the compiling markup restricts the attachment to certain compilers (see
	 * {@link AttachmentCompileType#getCompileConditionSections(Section, String)}).
	 *
	 * @param section the section to be check for packages
	 * @created 12.03.2012
	 */
	protected PackageInfo getPackageInfo(Section<? extends DefaultMarkupType> section) {
		List<PredicateParser.ParsedPredicate> packageRules = new ArrayList<>();
		List<String> packageNames = new ArrayList<>();
		$(DefaultMarkupType.getAnnotationContentSections(section, PackageManager.PACKAGE_ATTRIBUTE_NAME))
				.successor(PackageRule.class)
				.forEach(packageRule -> {
					if (packageRule.get().isOrdinaryPackage(packageRule)) {
						packageNames.add(packageRule.get().getOrdinaryPackage(packageRule));
					}
					else {
						packageRules.add(packageRule.get().getRule(packageRule));
					}
				});

		if (packageNames.isEmpty() && packageRules.isEmpty()) {
			packageNames.addAll(KnowWEUtils.getPackageManager(section).getDefaultPackages(section.getArticle()));
			packageRules.addAll(KnowWEUtils.getPackageManager(section).getDefaultPackageRules(section.getArticle()));
		}
		ArticleManager articleManager = section.getArticleManager();
		if (!(articleManager instanceof DefaultArticleManager defaultArticleManager)) {
			return new PackageInfo(packageNames, packageRules);
		}

		// check if this is an article based on a compiled attachment, and if yes, get the compiling markup
		AttachmentManager attachmentManager = defaultArticleManager.getAttachmentManager();
		Section<AttachmentCompileType> compilingSection = null;
		Section<DefaultMarkupType> compilingMarkup = null;
		for (Section<AttachmentCompileType> candidate : attachmentManager.getCompilingAttachmentSections(section.getArticle())) {
			compilingMarkup = $(candidate).closest(AttachmentCompileType.class).closest(DefaultMarkupType.class).getFirst();
			if (compilingMarkup == null) continue;
			compilingSection = candidate;
			break;
		}
		if (compilingSection == null) {
			return new PackageInfo(packageNames, packageRules);
		}

		// if we only have the default package, get the package info from the compiling %%Attachment markup
		if (packageRules.isEmpty()
				&& packageNames.size() == 1 && packageNames.get(0).equals(PackageManager.DEFAULT_PACKAGE)) {
			PackageInfo attachmentMarkupPackageInfo = getPackageInfo(compilingMarkup);
			if (!attachmentMarkupPackageInfo.isEmpty()) {
				packageNames.clear();
				packageNames.addAll(attachmentMarkupPackageInfo.packageNames);
				packageRules.addAll(attachmentMarkupPackageInfo.packageRules);
			}
		}

		// the compiling markup may restrict the compilers of this attachment to the ones also compiling certain
		// other sections, e.g. if it compiles a different attachment for each variant
		Collection<? extends Section<?>> conditionSections = compilingSection.get()
				.getCompileConditionSections(compilingSection, section.getArticle().getTitle());
		if (conditionSections != null) {
			return restrictToConditionSections(new PackageInfo(packageNames, packageRules), conditionSections);
		}
		return new PackageInfo(packageNames, packageRules);
	}

	/**
	 * Restricts the given package info to the compilers that also compile at least one of the given sections. The
	 * result consists of a single package rule: (given packages) AND (packages of the condition sections).
	 *
	 * @param packageInfo       the package info to restrict
	 * @param conditionSections the sections of which one must be compiled too
	 * @return the restricted package info, empty (and that way not compiled at all) if the condition cannot be
	 * satisfied
	 */
	@NotNull
	private PackageInfo restrictToConditionSections(PackageInfo packageInfo, Collection<? extends Section<?>> conditionSections) {
		Set<String> conditionAlternatives = new LinkedHashSet<>();
		for (Section<?> conditionSection : conditionSections) {
			Section<DefaultMarkupType> conditionMarkup = $(conditionSection).closest(DefaultMarkupType.class).getFirst();
			if (conditionMarkup == null) continue;
			conditionAlternatives.addAll(getAlternatives(getPackageInfo(conditionMarkup)));
		}
		Set<String> packageAlternatives = getAlternatives(packageInfo);
		if (conditionAlternatives.isEmpty() || packageAlternatives.isEmpty()) {
			return new PackageInfo(List.of(), List.of());
		}

		String rule = toDisjunction(packageAlternatives) + " AND " + toDisjunction(conditionAlternatives);
		PredicateParser.ParsedPredicate parsedRule = PackageRule.parseRule(rule);
		if (!rule.equals(parsedRule.getCondition())) {
			// parseRule falls back to the default package for invalid rules, we must not compile there
			LOGGER.warn("Unable to create package rule for compiled attachment: " + rule);
			return new PackageInfo(List.of(), List.of());
		}
		return new PackageInfo(List.of(), List.of(parsedRule));
	}

	/**
	 * Returns the packages and package rules of the given package info as expressions for a package rule. A section
	 * with this package info is compiled if any of them is satisfied.
	 */
	@NotNull
	private static Set<String> getAlternatives(PackageInfo packageInfo) {
		Set<String> alternatives = new LinkedHashSet<>();
		for (String packageName : packageInfo.packageNames) {
			alternatives.add(toRuleVariable(packageName));
		}
		for (PredicateParser.ParsedPredicate packageRule : packageInfo.packageRules) {
			alternatives.add("(" + packageRule.getCondition().trim() + ")");
		}
		return alternatives;
	}

	@NotNull
	private static String toDisjunction(Set<String> alternatives) {
		if (alternatives.size() == 1) return alternatives.iterator().next();
		return "(" + String.join(" OR ", alternatives) + ")";
	}

	/**
	 * Returns the package name as it has to be written in a package rule, quoted if required.
	 */
	@NotNull
	private static String toRuleVariable(String packageName) {
		if (PLAIN_RULE_VARIABLE.matcher(packageName).matches() && !RULE_KEYWORDS.contains(packageName)) {
			return packageName;
		}
		return packageName.contains("\"") ? "'" + packageName + "'" : "\"" + packageName + "\"";
	}

	protected void storePackageInfo(Section<?> section, PackageInfo packagesToCompile) {
		section.storeObject(REGISTERED_PACKAGE_KEY, packagesToCompile);
	}

	protected PackageInfo getStoredPackageInfo(Section<DefaultMarkupType> section) {
		return section.getObject(REGISTERED_PACKAGE_KEY);
	}

	protected boolean compilesPackageViaPatternMatch(String packageName, Section<? extends PackageCompileType> compileSection) {
		return Stream.of(compileSection.get().getPackagePatterns(compileSection))
				.anyMatch(p -> p.matcher(packageName).matches());
	}

	protected boolean isCompileMarkup(Section<DefaultMarkupType> section) {
		return section.get() instanceof DefaultMarkupPackageCompileType;
	}

	protected static final class PackageInfo {

		public final List<String> packageNames;
		public final List<PredicateParser.ParsedPredicate> packageRules;

		public PackageInfo(List<String> packageNames, List<PredicateParser.ParsedPredicate> packageRules) {
			this.packageNames = packageNames;
			this.packageRules = packageRules;
		}

		public boolean isEmpty() {
			return packageNames.isEmpty() && packageRules.isEmpty();
		}
	}
}
