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
package de.knowwe.core.compile.packaging;

import java.util.List;
import java.util.Set;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * @author Stefan Plehn
 * @created 25.04.2013
 */
public class MasterAnnotationWarningHandler extends DefaultGlobalScript<DefaultMarkupType> {

	@Override
	public void compile(DefaultGlobalCompiler compiler, Section<DefaultMarkupType> section) {

		PackageManager packageManager = KnowWEUtils.getPackageManager(section);
		Set<String> compilingArticles = packageManager.getCompilingArticles();

		List<Section<? extends AnnotationContentType>> annotationContents = DefaultMarkupType.getAnnotationContentSections(
				section, PackageManager.MASTER_ATTRIBUTE_NAME);

		for (Section<? extends AnnotationContentType> annotationContent : annotationContents) {
			String master = annotationContent.getText();
			if (!compilingArticles.contains(master)) {
				Message error = Messages.error("Error in @master annotation: \n '"
						+ master
						+ "' is not a valid master page, i.e. this page doesn't exist or doesn't contain a knowledge base.");
				Messages.storeMessage(section, this.getClass(), error);
				return;
			}
		}
		Messages.clearMessages(section, this.getClass());
	}
}
