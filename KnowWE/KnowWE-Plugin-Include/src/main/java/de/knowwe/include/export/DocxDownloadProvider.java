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
import de.knowwe.core.utils.progress.LongOperation;
import de.knowwe.core.utils.progress.LongOperationToolProvider;
import de.knowwe.util.Icon;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class DocxDownloadProvider extends LongOperationToolProvider {

	public DocxDownloadProvider() {
		super(Icon.DOCUMENT_WORD.getPath(), "Download MS-Word",
				"Downloads the contents in Microsoft Word 2007+ format");
	}

	@Override
	public LongOperation getOperation(Section<?> section) {
		return new DocxDownloadOperation(section);
	}
}
