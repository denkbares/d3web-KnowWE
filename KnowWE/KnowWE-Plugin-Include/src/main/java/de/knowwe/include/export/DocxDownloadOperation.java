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

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import de.d3web.core.io.progress.ProgressListener;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Message.Type;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.progress.FileDownloadOperation;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class DocxDownloadOperation extends FileDownloadOperation {

	private final Section<?> section;
	private StringBuilder report = null;
	private boolean hasError = false;

	public DocxDownloadOperation(Section<?> section) {
		super(section.getArticle(), section.getTitle() + ".docx");
		this.section = section;
	}

	@Override
	public void execute(File resultFile, ProgressListener listener) throws IOException, InterruptedException {
		this.report = null;
		this.hasError = false;

		ExportManager export = new ExportManager();
		FileOutputStream stream = new FileOutputStream(resultFile);
		try {
			XWPFDocument document = export.createDocument(section);
			document.write(stream);
		}
		catch (ExportException e) {
			appendMessage(Messages.error(e.getMessage()));
		}
		finally {
			for (Message message : export.getMessages()) {
				appendMessage(message);
			}
			stream.close();
			if (hasError) throw new InterruptedException();
		}
	}

	private void appendMessage(Message msg) {
		if (report == null) {
			report = new StringBuilder();
		}
		else {
			report.append("\n<br>");
		}
		report.append(msg.getType().name()).append(": ");
		report.append(msg.getVerbalization());
		hasError |= msg.getType().equals(Type.ERROR);
	}

	@Override
	public String getReport() {
		if (report == null) return null;
		return report.toString();
	}

}
