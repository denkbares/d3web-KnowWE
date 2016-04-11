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
import de.knowwe.jspwiki.types.TTType;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class CodeStyleExporter implements Exporter<TTType> {

	@Override
	public boolean canExport(Section<TTType> section) {
		return true;
	}

	@Override
	public Class<TTType> getSectionType() {
		return TTType.class;
	}

	@Override
	public void export(Section<TTType> section, DocumentBuilder manager) throws ExportException {
		manager.setCode(true);
		manager.export(section.getChildren());
		manager.setCode(false);
	}

}
