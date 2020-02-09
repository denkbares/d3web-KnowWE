/*
 * Copyright (C) ${year} denkbares GmbH, Germany
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
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Strings;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Implementation of a compile type to handle the uses-annotations of a default markup. This Type does not add a
 * specific compiler, add a Compiler when you add this Type to your parent type with a new {@link
 * PackageRegistrationScript}.
 *
 * @author Albrecht Striffler, Volker Belli (denkbares GmbH)
 * @created 13.10.2010
 */
public class DefaultMarkupPackageCompileType extends DefaultMarkupType implements PackageCompileType {

	public DefaultMarkupPackageCompileType(DefaultMarkup markup) {
		super(markup);

		this.addCompileScript(Priority.HIGH, new PackageCompileSectionRegistrationScript());
		this.addCompileScript(Priority.LOW, new PackageWithoutSectionsWarningScript());
	}


	@Override
	public String[] getPackagesToCompile(Section<? extends PackageCompileType> section) {

		PackageManager packageManager = Compilers.getPackageRegistrationCompiler(section).getPackageManager();
		Set<String> allPackageNames = packageManager.getAllPackageNames();

		Stream<String> packagesStream = Stream.of(getPackages(section));
		Stream<String> packagesFromPatternsStream = Stream.of(getPackagePatterns(section))
				.flatMap(pattern -> allPackageNames.stream()
						.filter(packageName -> pattern.matcher(packageName).matches()));

		return Stream.concat(packagesStream, packagesFromPatternsStream).toArray(String[]::new);
	}

	@Override
	public Pattern[] getPackagePatterns(Section<? extends PackageCompileType> section) {
		return $(DefaultMarkupType.getAnnotationContentSections(section, PackageManager.COMPILE_ATTRIBUTE_NAME))
				.successor(PackagePattern.class)
				.map(s -> s.get().getPattern(s))
				.toArray(Pattern[]::new);
	}

	@NotNull
	@Override
	public String[] getPackages(Section<?> section) {
		List<Section<? extends AnnotationContentType>> compileAnnotations =
				DefaultMarkupType.getAnnotationContentSections(section, PackageManager.COMPILE_ATTRIBUTE_NAME);

		if (compileAnnotations.isEmpty()) {
			return KnowWEUtils.getPackageManager(section).getDefaultPackages(section.getArticle());
		}
		return $(compileAnnotations).successor(PackageTerm.class).map(s -> s.get().getTermName(s)).toArray(String[]::new);
	}

	private static class PackageCompileSectionRegistrationScript extends PackageRegistrationScript<DefaultMarkupPackageCompileType> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<DefaultMarkupPackageCompileType> section) throws CompilerMessage {
			compiler.getPackageManager().registerPackageCompileSection(section);
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<DefaultMarkupPackageCompileType> section) {
			compiler.getPackageManager().unregisterPackageCompileSection(section);
		}
	}

	/**
	 * Script to check whether the compiled packages contain any packages
	 */
	private class PackageWithoutSectionsWarningScript extends PackageRegistrationScript<DefaultMarkupPackageCompileType> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<DefaultMarkupPackageCompileType> section) {
			String[] packagesToCompile = getPackagesToCompile(section);
			Collection<Section<?>> sectionsOfPackages = compiler.getPackageManager().getSectionsOfPackage(
					packagesToCompile);
			boolean emptyPackages = sectionsOfPackages.isEmpty()
					// the parents markup section does not count
					|| (sectionsOfPackages.size() == 1
					&& sectionsOfPackages.contains(section));
			if (emptyPackages) {
				String packagesString = Strings.concat(" ,", packagesToCompile);
				if (packagesToCompile.length > 1) {
					packagesString = "s '" + packagesString + "' do";
				}
				else {
					packagesString = " '" + packagesString + "' does";
				}
				Message warning = Messages.warning("The package" + packagesString + " not contain any sections.");
				Messages.storeMessage(compiler, section, getClass(), warning);
			}
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<DefaultMarkupPackageCompileType> section) {
			Messages.clearMessages(compiler, section, getClass());
		}
	}
}
