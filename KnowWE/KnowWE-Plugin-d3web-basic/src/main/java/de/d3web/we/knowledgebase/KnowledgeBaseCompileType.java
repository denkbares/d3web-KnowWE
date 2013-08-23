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

package de.d3web.we.knowledgebase;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Implementation of a compile type to handle the uses-annotations of a
 * knowledge base markup.
 * 
 * @author volker_belli
 * @created 13.10.2010
 */
public class KnowledgeBaseCompileType extends PackageCompileType {

	public KnowledgeBaseCompileType() {
		this.setSectionFinder(new RegexSectionFinder(".*", Pattern.DOTALL));
		addChildType(new KnowledgeBaseNameType());
	}

	@Override
	public Collection<String> getPackagesToCompile(Section<? extends PackageCompiler> section) {
		Section<KnowledgeBaseType> kbSection = Sections.findAncestorOfType(section,
				KnowledgeBaseType.class);
		String[] uses = DefaultMarkupType.getAnnotations(kbSection,
				KnowledgeBaseType.ANNOTATION_COMPILE);
		if (uses.length == 0) {
			return Environment.getInstance().getPackageManager(section.getWeb()).getDefaultPackages(
					section.getArticle());
		}
		return Arrays.asList(uses);
	}
}
