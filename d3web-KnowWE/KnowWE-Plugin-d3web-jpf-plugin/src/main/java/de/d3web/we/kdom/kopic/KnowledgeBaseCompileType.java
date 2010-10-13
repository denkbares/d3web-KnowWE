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

package de.d3web.we.kdom.kopic;

import java.util.Arrays;
import java.util.List;

import de.d3web.we.core.packaging.PackageCompileType;
import de.d3web.we.core.packaging.PackageReference;
import de.d3web.we.kdom.Section;

/**
 * Implementation of a compile type to handle the uses-annotations of a
 * knowledge base markup.
 * 
 * @author volker_belli
 * @created 13.10.2010
 */
public class KnowledgeBaseCompileType extends PackageCompileType {

	@Override
	protected void init() {
	}

	@Override
	public List<String> getPackagesToCompile(Section<? extends PackageReference> section) {
		return Arrays.asList(section.getOriginalText().trim());
	}

}
