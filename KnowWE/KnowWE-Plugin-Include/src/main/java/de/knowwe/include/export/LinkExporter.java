/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.include.export;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.types.LinkType;

/**
 * Exporter for exporting links. They will be exported in the same style as the
 * surrounding text.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class LinkExporter implements Exporter<LinkType> {

	@Override
	public boolean canExport(Section<LinkType> section) {
		return true;
	}

	@Override
	public Class<LinkType> getSectionType() {
		return LinkType.class;
	}

	@Override
	public void export(Section<LinkType> section, DocumentBuilder manager) throws ExportException {
		String text = section.getText();
		int from = text.indexOf('[');
		int to = text.lastIndexOf('|');
		if (to == -1) to = text.lastIndexOf(']');
		manager.append(text.substring(from + 1, to).trim());
	}

}
