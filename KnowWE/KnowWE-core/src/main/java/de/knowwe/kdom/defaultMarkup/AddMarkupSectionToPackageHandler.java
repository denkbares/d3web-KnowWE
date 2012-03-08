/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
import java.util.List;

import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.compile.packaging.KnowWEPackageManager;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

public class AddMarkupSectionToPackageHandler extends SubtreeHandler<DefaultMarkupType> {

	public AddMarkupSectionToPackageHandler() {
		super(true);
	}

	@Override
	public Collection<Message> create(KnowWEArticle article, Section<DefaultMarkupType> section) {
		if (!section.get().isIgnoringPackageCompile()) {
			String value = null;

			List<Section<? extends AnnotationContentType>> annotationContents = DefaultMarkupType.getAnnotationContentSections(
					section, KnowWEPackageManager.PACKAGE_ATTRIBUTE_NAME);
			KnowWEPackageManager packageManager = KnowWEEnvironment.getInstance().getPackageManager(
					article.getWeb());
			for (Section<?> annotationContent : annotationContents) {
				value = annotationContent.getText();
				packageManager.addSectionToPackage(section, value);
			}
		}
		return Messages.noMessage();
	}

}
