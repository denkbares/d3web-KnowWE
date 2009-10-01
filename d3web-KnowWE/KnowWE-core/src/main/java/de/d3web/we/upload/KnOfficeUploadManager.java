package de.d3web.we.upload;

import java.io.File;
import java.util.Collection;

import org.apache.commons.fileupload.FileItem;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.SemanticCore;
import de.d3web.we.taghandler.ImportKnOfficeHandler;
import de.d3web.we.taghandler.OwlUploadHandler;
import de.d3web.we.utils.KopicWriter;

public class KnOfficeUploadManager {

	private static KnOfficeUploadManager instance = null;

	public static synchronized KnOfficeUploadManager getInstance() {
		if (instance == null) {
			instance = new KnOfficeUploadManager();

		}
		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public String manageUpload(Collection<FileItem> fileItems) {

		KopicWriter kopicWriter = new KopicWriter();

		String pagename = "DefaultKnOfficeUploadPage";
		for (FileItem fileItem : fileItems) {
			if (fileItem.getFieldName().equals(
					ImportKnOfficeHandler.KEY_WIKIPAGE)) {
				String p = fileItem.getString();
				if (p != null && p.length() > 0) {
					pagename = p;
				}

			}
			if (fileItem.getFieldName().equals(
					ImportKnOfficeHandler.KEY_SOLUTIONS)) {
				String data = fileItem.getString();
				kopicWriter.appendSolutions(data);
			}
			if (fileItem.getFieldName().equals(
					ImportKnOfficeHandler.KEY_QUESTIONNAIRES)) {
				String data = fileItem.getString();
				kopicWriter.appendQuestionnaires(data);
			}
			if (fileItem.getFieldName().equals(
					ImportKnOfficeHandler.KEY_DECISIONTREE)) {
				String data = fileItem.getString();
				kopicWriter.appendQuestions(data);
			}
			if (fileItem.getFieldName()
					.equals(ImportKnOfficeHandler.KEY_CONFIG)) {
				String data = fileItem.getString();
				kopicWriter.appendConfig(data);
			}
			if (fileItem.getFieldName().equals(
					ImportKnOfficeHandler.KEY_COVERINGLISTS)) {
				String data = fileItem.getString();
				kopicWriter.appendCoveringLists(data);
			}
			if (fileItem.getFieldName().equals(
					ImportKnOfficeHandler.KEY_DECISIONTABLE)) {
				String text = fileItem.getString();
				String path = KnowWEEnvironment.getInstance().getContext()
						.getRealPath("");
				if (text != null && text.length() > 0) {
					File file = new File(
							path + "/KnowWEExtension/tmp/uploads/",
							"decisiontable.xls");
					try {
						fileItem.write(file);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					return "feature under construction";
//					ExceltoTextParser parser = new ExceltoTextParser(file);
//					String data = parser.parse();
//					kopicWriter.appendDecisionTable(data);
				}
			}
			if (fileItem.getFieldName().equals(ImportKnOfficeHandler.KEY_OWL)) {
				String text = fileItem.getString();

				String path = KnowWEEnvironment.getInstance()
						.getKnowWEExtensionPath();
				if (text != null && text.length() > 0) {
					File file = new File(path + File.separatorChar
							+ "owlincludes", fileItem.getName());
					File fpath = new File(path, "owlincludes");

					try {
						if (!fpath.exists())
							fpath.mkdirs();

						if (!file.exists()) {
							file.createNewFile();
						} else {
							file.delete();
							file.createNewFile();
						}
						fileItem.write(file);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					SemanticCore sc = SemanticCore.getInstance();
					sc.loadOwlFile(file);
					return "redirect:Wiki.jsp?page=" + "SemanticSettings";
				}
			}

			if (fileItem.getFieldName().equals(
					OwlUploadHandler.KEY_DELETE_OWL)) {
				String filename = fileItem.getString();
				SemanticCore.getInstance().removeFile(filename);
				return "redirect:Wiki.jsp?page=" + "SemanticSettings";
			}
			if (fileItem.getFieldName().equals(ImportKnOfficeHandler.KEY_RULES)) {
				String data = fileItem.getString();
				kopicWriter.appendRules(data);
			}
			if (fileItem.getFieldName().equals(
					ImportKnOfficeHandler.KEY_SCORETABLE)) {
				String text = fileItem.getString();
				String path = KnowWEEnvironment.getInstance().getContext()
						.getRealPath("");
				if (text != null && text.length() > 0) {
					File file = new File(
							path + "/KnowWEExtension/tmp/uploads/",
							"scoretable.xls");
					try {
						fileItem.write(file);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return "feature under construction";
//					ExceltoTextParser parser = new ExceltoTextParser(file);
//					String data = parser.parse();
//					kopicWriter.appendScoreTable(data);
				}

			}
			if (fileItem.getFieldName().equals(
					ImportKnOfficeHandler.KEY_COVERINGTABLE)) {
				String text = fileItem.getString();
				String path = KnowWEEnvironment.getInstance().getContext()
						.getRealPath("");
				if (text != null && text.length() > 0) {
					File file = new File(
							path + "/KnowWEExtension/tmp/uploads/",
							"coveringtable.xls");
					try {
						fileItem.write(file);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return "feature under construction";
//					ExceltoTextParser parser = new ExceltoTextParser(file);
//					String data = parser.parse();
//					kopicWriter.appendCoveringTable(data);
				}

			}
			if (fileItem.getFieldName().equals(
					ImportKnOfficeHandler.KEY_DIALOG_CSS)
					|| fileItem.getFieldName().equals(
							ImportKnOfficeHandler.KEY_DIALOG_LAYOUT)
					|| fileItem.getFieldName().equals(
							ImportKnOfficeHandler.KEY_DIALOG_SETTINGS)
					|| fileItem.getFieldName().equals(
							ImportKnOfficeHandler.KEY_DIALOG_PIC)) {

				String path = KnowWEEnvironment.getInstance().getContext()
						.getRealPath("");
				String text = fileItem.getString();
				String name = fileItem.getName();
				if (text != null && text.length() > 0) {
					String folderName = pagename + "PP"
							+ KnowWEEnvironment.generateDefaultID(pagename);
					File f = new File(path + "/kbResources" + "/" + folderName
							+ "/multimedia/");
					if (!f.exists()) {
						f.mkdirs();
					}

					File file = new File(f.toString(), name);
					try {
						fileItem.write(file);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}

		String kopicText = kopicWriter.getKopicText();

		boolean exists = KnowWEEnvironment.getInstance().getWikiConnector()
				.doesPageExist(pagename);
		if (exists) {
			KnowWEEnvironment.getInstance().getWikiConnector()
					.appendContentToPage(pagename, kopicText);
		} else {
			KnowWEEnvironment.getInstance().getWikiConnector().createWikiPage(
					pagename, kopicText, "KnOfficeUpload");
		}

		return "redirect:Wiki.jsp?page=" + pagename;
	}

}
