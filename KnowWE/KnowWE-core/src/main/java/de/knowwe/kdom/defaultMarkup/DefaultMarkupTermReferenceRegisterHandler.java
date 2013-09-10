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
package de.knowwe.kdom.defaultMarkup;

import java.util.Collection;
import java.util.Set;

import de.d3web.strings.Identifier;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * 
 * @author Stefan Plehn
 * @created 12.07.2013
 */
public class DefaultMarkupTermReferenceRegisterHandler extends SubtreeHandler<DefaultMarkupType> {

	public DefaultMarkupTermReferenceRegisterHandler() {
		super(false);
	}

	@Override
	public Collection<Message> create(Article article, Section<DefaultMarkupType> section) {
		// register packages as reference
		TerminologyManager terminologyHandler = KnowWEUtils.getTerminologyManager(article);
		String annotationString = DefaultMarkupType.getAnnotation(section,
				PackageManager.PACKAGE_ATTRIBUTE_NAME);
		if (annotationString != null) {

			terminologyHandler.registerTermReference(section, Package.class,
					new Identifier(annotationString));
		}
		else {
			PackageManager packageManager = Environment.getInstance().getPackageManager(
					article.getWeb());
			Set<String> defaultPackages = packageManager.getDefaultPackages(article);
			for (String defaultPackage : defaultPackages) {
				terminologyHandler.registerTermReference(section,
						Package.class,
						new Identifier(defaultPackage));
			}
		}
		return Messages.noMessage();
	}

}
