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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.d3web.core.io.progress.ProgressListener;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.progress.FileDownloadOperation;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class DocxDownloadOperation extends FileDownloadOperation {

	private final Section<?> section;

	public DocxDownloadOperation(Section<?> section) {
		super(section.getArticle(), section.getTitle() + ".docx");
		this.section = section;
	}

	@Override
	public void execute(File resultFile, ProgressListener listener) throws IOException, InterruptedException {
		ExportManager export = new ExportManager();
		FileOutputStream stream = new FileOutputStream(resultFile);
		try {
			export.createDocument(section).write(stream);
		}
		finally {
			stream.close();
		}
	}

	@Override
	public String getReport() {
		return null;
	}

}
