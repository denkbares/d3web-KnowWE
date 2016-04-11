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

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

/**
 * An exporter that is responsible to skip a specific type, including all
 * sub-sections below that type.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 12.02.2014
 */
public class HideExporter<T extends Type> implements Exporter<T> {

	private final Class<T> typeToHide;

	public HideExporter(Class<T> typeToHide) {
		this.typeToHide = typeToHide;
	}

	@Override
	public Class<T> getSectionType() {
		return typeToHide;
	}

	@Override
	public boolean canExport(Section<T> section) {
		// we match all sections of that class
		return true;
	}

	@Override
	public void export(Section<T> section, DocumentBuilder manager) throws ExportException {
		// but simply export nothing for them
	}

}
