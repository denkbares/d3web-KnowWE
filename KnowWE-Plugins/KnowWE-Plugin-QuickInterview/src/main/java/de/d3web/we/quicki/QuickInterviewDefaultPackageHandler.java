/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.d3web.we.quicki;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * 
 * @author stefan
 * @created 22.08.2013
 */
public class QuickInterviewDefaultPackageHandler extends DefaultGlobalScript<Type> {

	@Override
	public void compile(DefaultGlobalCompiler compiler, Section<Type> section) {
		TerminologyManager terminologyHandler = compiler.getTerminologyManager();

		String[] annotationStrings = DefaultMarkupType.getAnnotations(section,
				PackageManager.PACKAGE_ATTRIBUTE_NAME);
		// register definition for the default package if there is no
		// annotation
		// to specify another package
		if (annotationStrings.length == 0) {
			PackageManager packageManager = KnowWEUtils.getPackageManager(section.getArticleManager());
			@NotNull Set<String> defaultPackages = packageManager.getDefaultPackages(section.getArticle());
			for (String defaultPackage : defaultPackages) {
				terminologyHandler.registerTermReference(compiler,
						section,
						Package.class, new Identifier(defaultPackage));
			}
		}
		else {
			for (String annotationString : annotationStrings) {
				terminologyHandler.registerTermReference(compiler,
						section,
						Package.class, new Identifier(annotationString));

			}
		}
	}

}
