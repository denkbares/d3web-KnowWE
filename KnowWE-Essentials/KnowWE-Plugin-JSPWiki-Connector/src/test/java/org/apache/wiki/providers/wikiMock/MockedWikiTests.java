package org.apache.wiki.providers.wikiMock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.denkbares.utils.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MockedWikiTests {

	String gitPath = "/Users/mkrug/temp/KONAP/mockWiki";

	@Test
	public void testCreatePages() {
		MockedWiki wiki = MockedWiki.defaultWiki(gitPath);

		int numPages = 100;
		wiki.createPages(numPages);
		assertEquals(numPages, wiki.numPages());
	}

	@Before
	public void setUp() throws Exception {
		gitPath = Files.createTempDir().getAbsolutePath();
	}

	@Test
	@Ignore
	public void testGetAttachmentsSincePage() {
		MockedWiki wiki = MockedWiki.defaultWiki(gitPath);

		int numPages = 5;
		wiki.createPages(numPages);

		Random random = new Random(13374211);
		//create attachments (and versions thereof)
		//store number of random revisions
		Map<String, Integer> versionsMap = new HashMap<>();
		Map<String, List<String>> versionContentMap = new HashMap<>();

		long time = -1;
		int pageCnt = 0;
		int expectednumAtts = 0;
		for (Page page : wiki.getAllPages()) {
			if (pageCnt == 2 && time == -1) {
				//we use this timestamp

				try {
					Thread.sleep(1000);
					time = System.currentTimeMillis();
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			List<Attachment> attachments = wiki.createAttachments(page.getName(), 4);
			if (time != -1) {
				expectednumAtts += attachments.size();
			}
			for (Attachment att : attachments) {
				int numVersions = random.nextInt(3);
				List<String> versionContent = wiki.createVersionsForAttachment(att, numVersions);

				versionsMap.put(att.getName(), numVersions);
				versionContentMap.put(att.getName(), versionContent);
			}

			pageCnt++;
		}

		try {
			List<Attachment> attachmentsSince = wiki.getAttachmentsSince(time);

			assertEquals(expectednumAtts, attachmentsSince.size());
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testCreateAndEditPages() {
		MockedWiki wiki = MockedWiki.defaultWiki(gitPath);

		int numPages = 10;
		wiki.createPages(numPages);

		List<Page> allPages = wiki.getAllPages();

		//verify number
		assertEquals(numPages, allPages.size());

		//we edit all pages a random number of times
		Map<String, Integer> randomEdits = new HashMap<>();

		Random random = new Random(13374211);
		for (int i = 0; i < numPages; i++) {
			Page pageToEdit = allPages.get(i);
			int numeditsForPage = random.nextInt(100);
			randomEdits.put(pageToEdit.getName(), numeditsForPage);

			for (int k = 0; k < numeditsForPage; k++) {
				wiki.editPage(pageToEdit, "Edit: " + k);
			}
		}

		//then verify the number of edits
		allPages = wiki.getAllPages();
		for (Page page : allPages) {
			Integer numEdits = randomEdits.get(page.getName());
			//assert num versions
			if (page.getVersion() != -1) {
				assertEquals((int) numEdits + 1, page.getVersion());
			}
			List<Page> versionHistory = wiki.getVersionHistory(page.getName());
			//also assert the version via the history
			assertEquals((int) numEdits + 1, versionHistory.size());

			//and assert any single version number
			for (int i = 1; i <= numEdits + 1; i++) {
				Page page1 = versionHistory.get(versionHistory.size() - i);
				assertEquals(i, page1.getVersion());
			}

			//assert text of latest version
			assertEquals("Edit: " + (numEdits - 1), wiki.getTextForPage(page.getName()));

			//and of all versions
			for (int v = 0; v < numEdits; v++) {
				String expectedText = "Edit: " + (v - 1);
				if (v == 0) {
					expectedText = "";
				}

				assertEquals(expectedText, wiki.getTextForPage(page.getName(), v + 1));
			}
		}
	}

	@Test
	public void testCreateAndDeletePages() {
		MockedWiki wiki = MockedWiki.defaultWiki(gitPath);

		int numPages = 100;
		wiki.createPages(numPages);

		List<Page> allPages = wiki.getAllPages();

		//verify number
		assertEquals(numPages, allPages.size());

		//then verify the number of edits
		allPages = wiki.getAllPages();

		Set<Page> remainingPages = new HashSet<>();
		Random random = new Random(13374211);
		for (Page page : allPages) {
			if (random.nextBoolean()) {
				wiki.deletePage(page);
			}
			else {
				remainingPages.add(page);
			}
		}

		List<Page> allRemainingPages = wiki.getAllPages();

		assertTrue(remainingPages.containsAll(allRemainingPages) && allRemainingPages.containsAll(remainingPages));
	}

	@Test
	public void testCreateAndMovePages() {
		MockedWiki wiki = MockedWiki.defaultWiki(gitPath);

		int numPages = 100;
		wiki.createPages(numPages);

		List<Page> allPages = wiki.getAllPages();

		//verify number
		assertEquals(numPages, allPages.size());

		//then verify the number of edits
		allPages = wiki.getAllPages();

		Random random = new Random(13374211);
		for (Page page : allPages) {
			wiki.movePage(page, "1_" + page.getName());
		}

		assertTrue(wiki.isClean());
	}

	@Test
	@Ignore
	public void testPutAttachments() {
		MockedWiki wiki = MockedWiki.defaultWiki(gitPath);

		int numPages = 20;
		wiki.createPages(numPages);

		Random random = new Random(13374211);
		//create attachments (and versions thereof)
		//store number of random revisions
		Map<String, Integer> versionsMap = new HashMap<>();
		Map<String, List<String>> versionContentMap = new HashMap<>();
		for (Page page : wiki.getAllPages()) {
			List<Attachment> attachments = wiki.createAttachments(page.getName(), 10);
			for (Attachment att : attachments) {
				int numVersions = random.nextInt(10);
				List<String> versionContent = wiki.createVersionsForAttachment(att, numVersions);

				versionsMap.put(att.getName(), numVersions);
				versionContentMap.put(att.getName(), versionContent);
			}
		}

		//now retrieve the attachments

		for (Page page : wiki.getAllPages()) {
			List<Attachment> attachmentsForPage = wiki.getAttachmentsForPage(page);
			//the number of attachments must be ok
			assertEquals(10, attachmentsForPage.size());

			for (Attachment att : attachmentsForPage) {
				Integer numVersions = versionsMap.get(att.getName());
				List<Attachment> versionHistory = wiki.getVersionHistory(att);

				//check number of versions!
				assertEquals(numVersions + 1, versionHistory.size());
				//check content of each individual version
				List<String> versionContents = versionContentMap.get(att.getName());
				for (int v = 0; v <= numVersions; v++) {
					Attachment attachmentVersionForPage = wiki.getAttachmentVersionForPage(page, att.getFileName(), v + 1);
					String attachmentData = wiki.getAttachmentData(attachmentVersionForPage);
					String expectedData = versionContents.get(v);

					assertEquals(expectedData, attachmentData);
				}
			}
		}
	}

	@Test
	@Ignore
	public void testListAttachmentVersions() {
		MockedWiki wiki = MockedWiki.defaultWiki(gitPath);

		int numPages = 10;
		wiki.createPages(numPages);

		Random random = new Random(13374211);
		//create attachments (and versions thereof)
		//store number of random revisions
		Map<String, Integer> versionsMap = new HashMap<>();
		Map<String, List<String>> versionContentMap = new HashMap<>();
		for (Page page : wiki.getAllPages()) {
			List<Attachment> attachments = wiki.createAttachments(page.getName(), 2);
			for (Attachment att : attachments) {
				int numVersions = random.nextInt(100);
				List<String> versionContent = wiki.createVersionsForAttachment(att, numVersions);

				versionsMap.put(att.getName(), numVersions);
				versionContentMap.put(att.getName(), versionContent);
			}
		}

		//now retrieve the attachments

		for (Page page : wiki.getAllPages()) {
			List<Attachment> attachmentsForPage = wiki.getAttachmentsForPage(page);

			for (Attachment att : attachmentsForPage) {
				Integer numVersions = versionsMap.get(att.getName());
				long time = System.currentTimeMillis();
				List<Attachment> versionHistory = wiki.getVersionHistory(att);

				System.out.println("Time to retrieve " + numVersions + " " + (System.currentTimeMillis() - time));
			}
		}
	}

/*	public static void main(String[] args) throws IOException, WikiException {
		String propertiesFile = "/Users/mkrug/git/denkbares/krone-refactoring/KnowWE/KnowWE-App/src/resources/local/WEB-INF/classes/jspwiki-custom.properties";

		Properties properties = new Properties();
		properties.load(new FileInputStream(propertiesFile));

//		MockedWiki wiki = MockedWiki.fromProperties(properties);
		MockedWiki wiki = MockedWiki.defaultWiki("/Users/mkrug/Konap/Wiki_VM_release Kopie");

		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();

		Page pageFor = wiki.getPageFor("KONAP vm_xx_wd2a1 VM_UP_URLHP");
		System.out.println(pageFor);

		List<Page> allPages = wiki.getAllPages();

		System.out.println("Time to get all pages: " + stopwatch.getDisplay());
	}*/
}
