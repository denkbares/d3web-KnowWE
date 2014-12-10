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
package de.knowwe.testcases.download;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.progress.LongOperation;
import de.knowwe.core.utils.progress.LongOperationToolProvider;
import de.knowwe.tools.Tool;
import de.knowwe.util.Icon;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 09.09.2013
 */
public class CasesZipToolProvider extends LongOperationToolProvider {

	public CasesZipToolProvider() {
		super(Icon.FILE_ZIP, "Download all cases",
				"Downloads all available test cases as a zip file", Tool.CATEGORY_DOWNLOAD);

	}

	@Override
	public LongOperation getOperation(Section<?> section) {
		return new CasesZipOperation(section.getArticle(), section.getTitle() + "Cases.zip");
	}

}
