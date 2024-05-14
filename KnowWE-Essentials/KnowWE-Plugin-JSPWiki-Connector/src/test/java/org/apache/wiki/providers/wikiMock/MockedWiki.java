package org.apache.wiki.providers.wikiMock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.WikiPage;
import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.api.exceptions.WikiException;
import org.apache.wiki.api.providers.PageProvider;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.gitBridge.JSPUtils;
import org.apache.wiki.pages.DefaultPageManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.providers.GitVersioningAttachmentProvider;
import org.apache.wiki.providers.GitVersioningFileProvider;
import org.mockito.Mockito;

import com.denkbares.utils.Files;

import static org.apache.wiki.api.providers.AttachmentProvider.PROP_STORAGEDIR;
import static org.apache.wiki.providers.GitVersioningFileProvider.JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockedWiki {

	public final Engine engine;
	private final GitVersioningFileProvider fileProvider;

	private final GitVersioningAttachmentProvider attachmentProvider;

	public MockedWiki(Engine engine) {
		this.engine = engine;
		this.fileProvider = (GitVersioningFileProvider) this.engine.getManager(PageManager.class).getProvider();
		attachmentProvider = new GitVersioningAttachmentProvider();
		try {
			attachmentProvider.initialize(engine, engine.getWikiProperties());
		}
		catch (NoRequiredPropertyException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void createPages(int numPages) {
		String pageName = "page_";
		for (int i = 0; i < numPages; i++) {
			WikiPage dummyPage = new WikiPage(this.engine, pageName + i);
			dummyPage.setAuthor("author");

			try {
				this.fileProvider.putPageText(dummyPage, "");
			}
			catch (ProviderException e) {
				throw new RuntimeException(e);
			}
		}
	}

	//returns the content of each version of that attachment
	public List<String> createVersionsForAttachment(Attachment attachment, int numVersions) {
		List<String> attachmentContent = new ArrayList<>();
		Attachment att = new org.apache.wiki.attachment.Attachment(engine, attachment.getParentName(), attachment.getFileName());
		//we need the context
		String fileContent = null;
		try {
			File attachmentFile = JSPUtils.findAttachmentFile(attachment.getParentName(), attachment.getFileName(), this.fileProvider.getFilesystemPath());
			fileContent = Files.readFile(attachmentFile);
		}
		catch (ProviderException | IOException e) {
			throw new RuntimeException(e);
		}

		attachmentContent.add(fileContent);

		att.setAuthor("author");
		for (int i = 0; i < numVersions; i++) {
			//we just repeat the content and put it again!

			byte[] bytes = fileContent.repeat(i + 2).getBytes(StandardCharsets.UTF_8);
			attachmentContent.add(new String(bytes));
			att.setSize(bytes.length);
			att.setVersion(i + 2);
			try {
				this.attachmentProvider.putAttachmentData(att, new ByteArrayInputStream(bytes));
			}
			catch (ProviderException | IOException e) {
				throw new RuntimeException(e);
			}
		}
		return attachmentContent;
	}

	public List<Attachment> createAttachments(String pageName, int numAttachments) {
		List<Attachment> attachments = new ArrayList<>();
		for (int i = 0; i < numAttachments; i++) {
			Attachment att = new org.apache.wiki.attachment.Attachment(engine, pageName, "Attachment_" + i + ".txt");
			att.setAuthor("author");
			byte[] bytes = ("text file contents_" + i).getBytes(StandardCharsets.UTF_8);
			InputStream in = new ByteArrayInputStream(bytes);

			att.setSize(bytes.length);
			attachments.add(att);

			try {
				this.attachmentProvider.putAttachmentData(att, in);
			}
			catch (ProviderException e) {
				throw new RuntimeException(e);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return attachments;
	}

	public int numPages() {
		return this.fileProvider.getPageCount();
	}

	public List<Page> getAllPages() {
		return this.fileProvider.getAllPages().stream().toList();
	}

	public Page getPageFor(String name) {
		//assume latest
		return getPageFor(name, -1);
	}

	public Page getPageFor(String name, int version) {
		try {
			return this.fileProvider.getPageInfo(name, version);
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	public Page getPageFor(int id, int version) {
		try {
			return this.fileProvider.getPageInfo("page_" + id, version);
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	public String getTextForPage(String pageName, int version) {
		try {
			return this.fileProvider.getPageText(pageName, version);
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Page> getVersionHistory(String pageName) {
		try {
			return this.fileProvider.getVersionHistory(pageName);
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the version history of the attachment. The returning list is sorted so that the lastest version is at entry
	 * 0
	 *
	 * @param att
	 * @return
	 */
	public List<Attachment> getVersionHistory(Attachment att) {
		return this.attachmentProvider.getVersionHistory(att);
	}

	public void movePage(Page page, String to) {
		try {
			this.fileProvider.movePage(page, to);
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	public String getTextForPage(String pageName) {
		return getTextForPage(pageName, -1);
	}

	public void editPage(Page page, String text) {
		try {
			this.fileProvider.putPageText(page, text);
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Page> getAllVersionForPage(Page page) {
		try {
			return this.fileProvider.getVersionHistory(page.getName());
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Attachment> getAttachmentsForPage(Page page) {
		try {
			return this.attachmentProvider.listAttachments(page);
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	public Attachment getAttachmentVersionForPage(Page page, String attName, int version) {
		try {
			return this.attachmentProvider.getAttachmentInfo(page, attName, version);
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	//====== FACTORY=======

	public static MockedWiki fromProperties(Properties properties) throws WikiException, IOException {
		WikiEngine engine = Mockito.mock(WikiEngine.class);
		when(engine.getWikiProperties()).thenReturn(properties);

		PageManager pageManager = new DefaultPageManager(engine, properties);

		when(engine.getManager(PageManager.class)).thenReturn(pageManager);
		UserManager um = Mockito.mock(UserManager.class);
		when(engine.getManager(UserManager.class)).thenReturn(um);

		final UserDatabase udb = mock(UserDatabase.class);
		when(um.getUserDatabase()).thenReturn(udb);
		final UserProfile up = mock(UserProfile.class);
		when(udb.findByFullName("author")).thenReturn(up);
		when(up.getFullname()).thenReturn("author");
		when(up.getEmail()).thenReturn("author@nowhere.com");

		PageProvider provider = pageManager.getProvider();
		provider.initialize(engine, properties);

		return new MockedWiki(engine);
	}

	public static MockedWiki defaultWiki(String gitDir) {
		File file = new File(gitDir);
		if (file.exists() && file.isDirectory()) {
			Files.recursiveDelete(file);
		}
		Properties properties = new Properties();
		properties.setProperty(GitVersioningFileProvider.JSPWIKI_GIT_DEFAULT_BRANCH, "maintenance");
		properties.setProperty("jspwiki.pageProvider", "GitVersioningFileProvider");
		properties.setProperty(JSPWIKI_FILESYSTEMPROVIDER_PAGEDIR, gitDir);
		properties.setProperty(PROP_STORAGEDIR, gitDir);

		try {
			return fromProperties(properties);
		}
		catch (WikiException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void deletePage(Page page) {
		try {
			this.fileProvider.deletePage(page);
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isClean() {
		return this.fileProvider.isClean();
	}

	public String getAttachmentData(Attachment att) {
		try {
			InputStream attachmentData = attachmentProvider.getAttachmentData(att);
			byte[] buffer = new byte[(int) att.getSize()];
			IOUtils.read(attachmentData, buffer);
			return new String(buffer);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}
}
