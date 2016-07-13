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
import java.util.LinkedList;
import java.util.List;

import com.denkbares.progress.ParallelProgress;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.progress.AjaxProgressListener;
import de.knowwe.core.utils.progress.FileDownloadOperation;
import de.knowwe.include.IncludeMarkup;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.util.Icon;

/**
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class DocxDownloadOperation extends FileDownloadOperation {

	private final Section<?> section;
	private ExportManager export = null;

	public DocxDownloadOperation(Section<?> section) {
		super(section.getArticle(), section.getTitle() + ".docx");
		this.section = section;
	}

	private void before(UserActionContext user, AjaxProgressListener listener) throws IOException {
		this.export = new ExportManager(section);

		// check read access for all articles
		for (Article article : export.getIncludedArticles()) {
			if (!KnowWEUtils.canView(article, user)) {
				addMessage(Messages.error(
						"User is not allowed to view article '" +
								article.getTitle() + "'"));
			}
		}

		// check if increase of version is required:
		// version is available and article is older that included ones
		Article article = user.getArticle();
		if (KnowWEUtils.getLastModified(article).before(export.getLastModified())
				&& getNextVersion() != null) {
			addMessage(Messages.warning("The version number appears to be out-dated. " +
					"Please update and download the word file again."));
		}
	}

	@Override
	public List<Tool> getActions(UserActionContext context) {
		List<Tool> actions = super.getActions(context);
		if (export == null) return actions;
		if (hasError()) return actions;
		if (!export.isNewVersionRequired()) return actions;

		// check if next version is available
		String nextVersion = getNextVersion();
		if (nextVersion == null) return actions;

		// create update tool action
		String versionSectionID = DefaultMarkupType.getAnnotationContentSection(section,
				IncludeMarkup.ANNOTATION_VERSION).getID();
		String jsAction = "KNOWWE.plugin.include.updateVersion(" +
				"'" + versionSectionID + "', " + nextVersion + ");";

		// make a copy and add update tool
		actions = new LinkedList<>(actions);
		actions.add(new DefaultTool(
				Icon.EDIT,
				"Update Version to " + nextVersion,
				"Increments the version number of the document to be downloaded.",
				jsAction, Tool.CATEGORY_EXECUTE));
		return actions;
	}

	@Override
	public Icon getFileIcon() {
		return Icon.FILE_WORD;
	}

	/**
	 * Creates the increased version number. The method return null if no updated number can be
	 * provided.
	 *
	 * @return the next version number
	 * @created 16.02.2014
	 */
	private String getNextVersion() {
		// if we passed the previous check, we know there is a version available
		Section<? extends AnnotationContentType> versionSection =
				DefaultMarkupType.getAnnotationContentSection(section,
						IncludeMarkup.ANNOTATION_VERSION);
		if (versionSection == null) return null;

		// if update is required inc version and write back to wiki
		try {
			String version = versionSection.getText();
			int dot = version.lastIndexOf('.');
			String major = version.substring(0, dot + 1);
			String minor = version.substring(dot + 1);
			return major + String.valueOf(Integer.parseInt(minor) + 1);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public void execute(UserActionContext context, File resultFile, AjaxProgressListener listener) throws IOException, InterruptedException {
		before(context, listener);
		ParallelProgress progress = new ParallelProgress(listener, 4f, 4f);
		progress.updateProgress(0, 0.2f, ExportManager.MSG_CREATE);
		if (hasError()) return;
		try (FileOutputStream stream = new FileOutputStream(resultFile)) {
			ExportModel model = export.createExport(progress.getSubTaskProgressListener(1));
			for (Message message : model.getMessages()) {
				addMessage(message);
			}
			progress.updateProgress(0, 0.2f, ExportManager.MSG_SAVE);
			model.getDocument().write(stream);
			progress.updateProgress(0, 1f);
		}
		catch (ExportException e) {
			addMessage(Messages.error(e.getMessage()));
		}
	}

}
