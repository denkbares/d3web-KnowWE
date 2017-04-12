package cc.knowwe.dialog.repository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import cc.knowwe.dialog.repository.ArchiveStorage.PatchFile;
import cc.knowwe.dialog.repository.ArchiveStorage.StorageFile;

import com.denkbares.utils.Log;

/**
 * ArchiveRepository manages a versioned repository of archive files.
 * 
 * A repository have a set of defined versions (VersionSet) and a set of alias
 * names, identify some exposed versions (e.g. current productive version,
 * current tested version, etc.). A VersionSet having a file list of contained
 * files of the repository and the date of creating the version.
 * 
 * A file config.xml is located in the archive's root folder. It contains all
 * the defined version sets of the repository and the available alias names for
 * some versions. It has the following structure:
 * 
 * <pre>
 * 	<repository>
 * 
 * 		<alias name='productive' version='1.0.1'/>
 * 		<alias name='test' version='1.1'/>
 * 
 * 		<versionset version='1.0.1' date='' millis=''>
 * 			<file name='knowledgebase.jar' checksum='abc'>
 * 			<file name='ui.zip' checksum='def'>
 * 			<file name='mobile.jar' checksum='ghi'>
 * 		</versionset>
 * 
 * 		<versionset version='1.1' date='' millis='' comment='some comment'>
 * 			<file name='knowledgebase.jar' checksum='abc'>
 * 			<file name='ui.zip' checksum='def'>
 * 			<file name='mobile.jar' checksum='jkl'>
 * 		</versionset>
 * 
 * 	</repository>
 * </pre>
 * 
 * @author Volker Belli
 * 
 */
public class ArchiveRepository {

	public static final String REPOSITORY_CONFIGURATION_FILE = "config.xml";

	// xml constants
	private static final String TAG_REPOSITORY = "repository";
	private static final String TAG_ALIAS = "alias";
	private static final String TAG_VERSIONSET = "versionset";
	private static final String TAG_FILE = "file";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_COMMENT = "comment";
	private static final String ATTR_CREATEDATE = "date";
	private static final String ATTR_CREATEMILLIS = "millis";
	private static final String ATTR_VERSION = "version";
	private static final String ATTR_CHECKSUM = "checksum";
	private static final DateFormat XML_DATE_FORMAT = DateFormat.getDateTimeInstance(
			DateFormat.LONG, DateFormat.LONG, Locale.US);

	private final File rootFolder;
	private final Map<String, String> aliasToVersion = new HashMap<>();
	private final Map<String, VersionSet> versionSets = new HashMap<>();

	private final ArchiveStorage storage;

	public ArchiveRepository(File rootFolder) throws IOException {
		this.rootFolder = rootFolder;
		this.storage = new ArchiveStorage(rootFolder);
		load();
	}

	public StorageFile addArchive(File archiveFile) throws IOException {
		StorageFile file = this.storage.addArchive(archiveFile);
		this.save();
		return file;
	}

	public void addVersionSet(VersionSet versionSet) throws IOException {
		this.versionSets.put(versionSet.getVersion(), versionSet);
		this.save();
	}

	public void removeVersionSet(VersionSet versionSet) throws IOException {
		this.versionSets.remove(versionSet.getVersion());
		this.save();
	}

	public void setAlias(String alias, VersionSet versionSet) throws IOException {
		this.aliasToVersion.put(alias, versionSet.getVersion());
		this.save();
	}

	public void removeAlias(String alias) throws IOException {
		this.aliasToVersion.remove(alias);
		this.save();
	}

	public String getVersion(String versionAlias) {
		return this.aliasToVersion.get(versionAlias);
	}

	public VersionSet getVersionSet(String version) {
		return this.versionSets.get(version);
	}

	public PatchFile getPatch(String archiveName, String originalChecksum, VersionSet versionSet) throws IOException {
		StorageFile file = versionSet.getFile(archiveName);
		if (file == null) return null;
		String targetChecksum = file.getChecksum();
		return this.storage.getPatch(archiveName, originalChecksum, targetChecksum);
	}

	public synchronized String getConfigXML() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (FileInputStream in = new FileInputStream(getConfigFile())) {
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
		}
		String result = new String(out.toByteArray());
		return result;
	}

	private void load() throws IOException {
		Document doc = loadXML();
		for (Node node = doc.getFirstChild().getFirstChild(); node != null; node = node.getNextSibling()) {
			String nodeName = node.getNodeName();
			if (nodeName.equals("#text")) { // NOSONAR
				// ingore
			}
			else if (nodeName.equals(TAG_ALIAS)) {
				// add alias entry
				loadAlias(node);
			}
			else if (nodeName.equals(TAG_VERSIONSET)) {
				// add version set
				loadVersionSet(node);
			}
			else {
				Log.warning("ignoring unexpected tag '" + nodeName
								+ "' in repository configuration file.");
			}
		}
	}

	private void loadVersionSet(Node versionSetNode) throws IOException {
		Node versionAttribute = versionSetNode.getAttributes().getNamedItem(ATTR_VERSION);
		Node dateAttribute = versionSetNode.getAttributes().getNamedItem(ATTR_CREATEDATE);
		Node commentAttribute = versionSetNode.getAttributes().getNamedItem(ATTR_COMMENT);

		String version = (versionAttribute == null) ? null : versionAttribute.getTextContent();
		String comment = (commentAttribute == null) ? "" : commentAttribute.getTextContent();
		Date date = null;
		try {
			if (dateAttribute != null) {
				date = XML_DATE_FORMAT.parse(dateAttribute.getTextContent());
			}
		}
		catch (ParseException e) {
			Log.warning("invalid date format in repository configuration in versionset '" + version
							+ "'. Using base date (1/1/1970) instead.");
			date = new Date(0);
		}

		if (date == null || version == null) {
			throw new IOException("incomplete versionset tag: version='" + version + "', date='"
					+ date + "'");
		}

		VersionSet versionSet = new VersionSet(version, comment, date);
		for (Node node = versionSetNode.getFirstChild(); node != null; node = node.getNextSibling()) {
			String nodeName = node.getNodeName();
			if (nodeName.equals("#text")) { // NOSONAR
				// ingore
			}
			else if (nodeName.equals(TAG_FILE)) {
				Node nameAttribute = node.getAttributes().getNamedItem(ATTR_NAME);
				Node checksumAttribute = node.getAttributes().getNamedItem(ATTR_CHECKSUM);

				String name = (nameAttribute == null) ? null : nameAttribute.getTextContent();
				String checksum = (checksumAttribute == null)
						? null
						: checksumAttribute.getTextContent();

				if (name == null || checksum == null) {
					throw new IOException("incomplete file tag: name='" + name + "', checksum='"
							+ checksum + "'");
				}

				versionSet.addFile(this.storage.getFile(name, checksum));
			}
			else {
				Log.warning("ignoring unexpected tag '" + nodeName
								+ "' in versionset in repository configuration file.");
			}
		}
		this.versionSets.put(version, versionSet);
	}

	private void loadAlias(Node aliasNode) throws IOException {
		Node nameAttribute = aliasNode.getAttributes().getNamedItem(ATTR_NAME);
		Node versionAttribute = aliasNode.getAttributes().getNamedItem(ATTR_VERSION);

		String name = (nameAttribute == null) ? null : nameAttribute.getTextContent();
		String version = (versionAttribute == null) ? null : versionAttribute.getTextContent();

		if (name == null || version == null) {
			throw new IOException("incomplete alias tag: name='" + name + "', version='" + version
					+ "'");
		}

		this.aliasToVersion.put(name, version);
	}

	private synchronized Document loadXML() throws IOException {
		File file = getConfigFile();
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			// lazy create basic configuration with no files but two aliases
			VersionSet versionSet = new VersionSet("0", "initial empty configuration", new Date());
			addVersionSet(versionSet);
			setAlias("productive", versionSet);
			setAlias("test", versionSet);
			save();
		}
		try (FileInputStream in = new FileInputStream(file)) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(in);
			return doc;
		}
		catch (ParserConfigurationException e) {
			throw new IllegalStateException("internal configuration error", e);
		}
		catch (SAXException e) {
			throw new IOException("internal error, bad xml format of repository configuration", e);
		}
	}

	private File getConfigFile() {
		return new File(this.rootFolder, REPOSITORY_CONFIGURATION_FILE);
	}

	private synchronized void save() throws IOException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.newDocument();

			Element root = doc.createElement(TAG_REPOSITORY);
			doc.appendChild(root);

			// add aliases
			for (String alias : aliasToVersion.keySet()) {
				String version = aliasToVersion.get(alias);
				// alias
				Element aliasNode = doc.createElement(TAG_ALIAS);
				aliasNode.setAttribute(ATTR_NAME, alias);
				aliasNode.setAttribute(ATTR_VERSION, version);
				root.appendChild(aliasNode);
			}

			// add versionsets
			for (VersionSet versionset : versionSets.values()) {
				// versionset
				Element versionSetNode = doc.createElement(TAG_VERSIONSET);
				versionSetNode.setAttribute(ATTR_VERSION, versionset.getVersion());
				versionSetNode.setAttribute(ATTR_CREATEDATE,
						XML_DATE_FORMAT.format(versionset.getCreateDate()));
				versionSetNode.setAttribute(ATTR_CREATEMILLIS,
						String.valueOf(versionset.getCreateDate().getTime()));
				versionSetNode.setAttribute(ATTR_COMMENT, versionset.getComment());
				root.appendChild(versionSetNode);

				// add files of versionset
				for (StorageFile file : versionset.getFiles()) {
					// file
					Element fileNode = doc.createElement(TAG_FILE);
					fileNode.setAttribute(ATTR_NAME, file.getArchiveName());
					fileNode.setAttribute(ATTR_CHECKSUM, file.getChecksum());
					fileNode.setAttribute(ATTR_CREATEDATE,
							XML_DATE_FORMAT.format(new Date(file.getFile().lastModified())));
					fileNode.setAttribute(ATTR_CREATEMILLIS,
							String.valueOf(file.getFile().lastModified()));
					versionSetNode.appendChild(fileNode);
				}
			}

			try (FileOutputStream out = new FileOutputStream(getConfigFile())) {
				Source source = new DOMSource(doc);
				Result result = new StreamResult(out);

				Transformer xformer =
						TransformerFactory.newInstance().newTransformer();
				xformer.setOutputProperty("method", "xml");
				xformer.setOutputProperty("encoding", "UTF-8");
				xformer.setOutputProperty("omit-xml-declaration", "no");
				xformer.setOutputProperty("indent", "yes");

				xformer.transform(source, result);
			}
		}
		catch (ParserConfigurationException e) {
			throw new IllegalStateException("internal error: bad parser configuration", e);
		}
		catch (TransformerException e) {
			throw new IllegalStateException("internal error: bad transformer configuration", e);
		}
	}
}
