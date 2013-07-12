/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.we.knowledgebase;

import java.util.Collection;
import java.util.Set;

import de.d3web.strings.Identifier;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageTermReference;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;


/**
 * 
 * @author Stefan Plehn
 * @created 12.07.2013
 */
public class KnowledgeBaseTermDefinitionRegisterHandler extends SubtreeHandler<KnowledgeBaseType> {

	@Override
	public Collection<Message> create(Article article, Section<KnowledgeBaseType> section) {

		TerminologyManager terminologyHandler = KnowWEUtils.getGlobalTerminologyManager(article.getWeb());
		String annotationString = DefaultMarkupType.getAnnotation(section,
				KnowledgeBaseType.ANNOTATION_COMPILE);

		// register definition for the default package if there is no annotation
		// to specify another package
		if (annotationString == null) {
			PackageManager packageManager = Environment.getInstance().getPackageManager(
					article.getWeb());
			Set<String> defaultPackages = packageManager.getDefaultPackages(article);
			for (String defaultPackage : defaultPackages) {
				terminologyHandler.registerTermDefinition(section,
						PackageTermReference.class,
						new Identifier(defaultPackage));
			}
		}
		return Messages.noMessage();

	}

}
