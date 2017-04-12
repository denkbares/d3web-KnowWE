package cc.knowwe.dialog.action.sync.server;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import cc.knowwe.dialog.Utils;
import cc.knowwe.dialog.repository.ArchiveStorage;
import cc.knowwe.dialog.repository.ArchiveStorage.PatchFile;
import cc.knowwe.dialog.repository.VersionSet;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class GetUpdateInfo extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String requestXML = context.getParameter("xml");
		List<PatchFile> files = new LinkedList<>();
		String versionAlias = null;
		String version = null;

		try {
			SyncServerContext serverContext = SyncServerContext.getInstance();
			// if the sync server has not been initialized,
			// deliver an empty set of files
			if (serverContext != null) {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new ByteArrayInputStream(requestXML.getBytes()));

				Node root = doc.getFirstChild();
				versionAlias = root.getAttributes().getNamedItem("alias").getTextContent();
				version = serverContext.getRepository().getVersion(versionAlias);

				if (version != null) {
					VersionSet versionSet = serverContext.getRepository().getVersionSet(version);
					for (Node fileNode = root.getFirstChild(); fileNode != null; fileNode = fileNode.getNextSibling()) {
						if (fileNode.getNodeName().equals("#text")) continue;
						String name = fileNode.getTextContent();
						String hash = fileNode.getAttributes().getNamedItem("checksum").getTextContent();

						try {
							PatchFile file = serverContext.getRepository().getPatch(name, hash,
									versionSet);
							if (file != null) files.add(file);
						}
						catch (FileNotFoundException e) {
							// client requests file that we do not have on the
							// server at all
							// so we do not want to provide an update for that
							// file
							// (this is an allowed situation)
						}
					}
				}
			}
		}
		catch (ParserConfigurationException e) {
			throw new IllegalStateException("internal configuration error", e);
		}
		catch (SAXException e) {
			throw new IOException("internal error, bad xml format from sync client", e);
		}

		Writer writer = context.getWriter();
		writer.append("<updates version='").append(
				Utils.encodeXML(version != null ? version : "none")).append("'>\n");
		for (ArchiveStorage.PatchFile file : files) {
			writer.append("\t<file size='").append(String.valueOf(file.getFile().length())).append(
					"'");
			writer.append(" time='").append(String.valueOf(file.getFile().lastModified())).append(
					"'");
			writer.append(" patch='").append(String.valueOf(file.isIncrementalPatch())).append("'>");
			writer.append(Utils.encodeXML(file.getArchiveName()));
			writer.append("</file>\n");
		}
		writer.append("</updates>");
		context.setContentType("text/xml");
	}

}
