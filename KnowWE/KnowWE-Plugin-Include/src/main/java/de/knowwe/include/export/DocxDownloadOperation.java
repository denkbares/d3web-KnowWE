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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

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
	private String report = null;

	public DocxDownloadOperation(Section<?> section) {
		super(section.getArticle(), section.getTitle() + ".docx");
		this.section = section;
	}

	@Override
	public void execute(File resultFile, ProgressListener listener) throws IOException, InterruptedException {
		FileOutputStream stream = new FileOutputStream(resultFile);
		try {
			ExportManager export = new ExportManager();
			XWPFDocument document = export.createDocument(section);

			PackageProperties properties = document.getPackage().getPackageProperties();
			properties.setRevisionProperty("13");
			properties.setCreatorProperty("Volker POI");
			properties.setTitleProperty("MMP Qatar Test Document");

			document.write(stream);
		}
		catch (InvalidFormatException e) {
			throw new IOException("invalid format", e);
		}
		finally {
			stream.close();
		}
	}

	@Override
	public String getReport() {
		return report;
	}

}
