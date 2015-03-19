/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.ontology.kdom.namespace;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 03.03.15.
 */
public class NamespaceFileReloadToolProvider implements ToolProvider {
	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		Tool reloadTool = getReloadTool(section, userContext);
		if (hasTools(section, userContext)) {
			return new Tool[] { reloadTool };
		}
		return new Tool[0];
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		Section<NamespaceFileAnnotationType> namespaceFileAnnotationTypeSection = Sections.successor(section, NamespaceFileAnnotationType.class);
		if (namespaceFileAnnotationTypeSection != null) {
			return true;
		}
		return false;
	}

	protected Tool getReloadTool(Section<?> section, UserContext userContext) {
		Section<NamespaceDefinition> namespaceDefinitionSection = Sections.successor(section, NamespaceDefinition.class);
		Section<NamespaceFileAnnotationType.FileNameType> fileNameTypeSection = Sections.successor(section, NamespaceFileAnnotationType.FileNameType.class);
		String namespaceUrl = namespaceDefinitionSection.getText();
		if (fileNameTypeSection != null) {
			String fileName = fileNameTypeSection.getText();

			String jsAction = "KNOWWE.core.plugin.reloadNamespaceFile.reloadFile('" + namespaceUrl + "', '" + fileName + "', '" + section
					.getTitle() + "');";
			return new DefaultTool(
					Icon.REFRESH,
					"Reload external ontology",
					"Reload namespace file from given URL",
					jsAction,
					Tool.CATEGORY_DOWNLOAD);
		}

		return null;
	}
}
