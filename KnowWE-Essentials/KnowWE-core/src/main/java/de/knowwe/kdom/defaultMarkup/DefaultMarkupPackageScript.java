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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.PredicateParser;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.AttachmentManager;
import de.knowwe.core.DefaultArticleManager;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageRule;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.attachment.AttachmentMarkup;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Common functionality for the package registration and unregistration of DefaultMarkups
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 06.11.2020
 */
public abstract class DefaultMarkupPackageScript {
	private static final String REGISTERED_PACKAGE_KEY = "RegisteredPackages";

	@NotNull
	protected List<String> getAllPackageNames(PackageInfo packageInfo) {
		return Stream.concat(packageInfo.packageNames.stream(), packageInfo.packageRules.stream()
				.flatMap((PredicateParser.ParsedPredicate parsedPredicate) -> parsedPredicate.getVariables().stream()))
				.collect(Collectors.toList());
	}

	/**
	 * Returns the packages the given default markup section belongs to according to the defined annotations. If there
	 * are no such annotations, the default packages for the article are returned. In case the section is part of an
	 * article based on a compiled attachment, we also check the compiling %%Attachment markups for packages.
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
		// if we only have the default package, check if this is an article based on an compiled attachment
		// and if yes, get package info from compiling %%Attachment markups
		ArticleManager articleManager = section.getArticleManager();
		if (packageRules.isEmpty()
				&& packageNames.size() == 1 && packageNames.get(0).equals(PackageManager.DEFAULT_PACKAGE)
				&& articleManager instanceof DefaultArticleManager) {
			AttachmentManager attachmentManager = ((DefaultArticleManager) articleManager).getAttachmentManager();
			PackageInfo attachmentMarkupPackageInfo = attachmentManager.getCompilingAttachmentSections(section
					.getArticle())
					.stream()
					.map(s -> $(s).ancestor(AttachmentMarkup.class).getFirst())
					.filter(Objects::nonNull)
					.map(this::getPackageInfo)
					.findFirst()
					.orElse(new PackageInfo(List.of(), List.of()));
			if (!attachmentMarkupPackageInfo.isEmpty()) {
				packageNames.clear();
				packageNames.addAll(attachmentMarkupPackageInfo.packageNames);
				packageRules.addAll(attachmentMarkupPackageInfo.packageRules);
			}
		}
		return new PackageInfo(packageNames, packageRules);
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
