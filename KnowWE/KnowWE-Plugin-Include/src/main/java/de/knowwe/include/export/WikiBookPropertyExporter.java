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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.types.CSSType;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class WikiBookPropertyExporter implements Exporter<CSSType> {

	private static final Pattern PATTERN = Pattern.compile(
			"%%\\((?:.*;)?class:\\s*wikiBook-([\\w-_.:]+)\\s*(?:;[^)]*)?\\)\\s*(.*?)\\s*[/%]%",
			Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

	@Override
	public boolean canExport(Section<CSSType> section) {
		Matcher matcher = PATTERN.matcher(section.getText());
		return matcher.find();
	}

	@Override
	public Class<CSSType> getSectionType() {
		return CSSType.class;
	}

	@Override
	public void export(Section<CSSType> section, DocumentBuilder manager) throws ExportException {
		Matcher matcher = PATTERN.matcher(section.getText());
		if (matcher.find()) {
			String property = matcher.group(1);
			String value = matcher.group(2);
			manager.getModel().setProperty(property, value);
		}
	}
}
