package de.uniwue.d3web.gitConnector;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uniwue.d3web.gitConnector.impl.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.CachingGitConnector;

import static org.junit.Assert.assertEquals;

public class TestJGit {

	private String TMP_NEW_REPO = "/Users/mkrug/temp/konap_test/testGit";
//	private String TMP_NEW_REPO = "/Users/mkrug/temp/konap_test/wiki";

	private GitConnector gitConnector;

	@Before
	public void init() {
//		jGitConnector = JGitConnector.fromPath(TMP_NEW_REPO);
		gitConnector = new CachingGitConnector(BareGitConnector.fromPath(TMP_NEW_REPO));
	}

	@After
	public void tearDown() {
//		try {
//			FileUtils.deleteDirectory(new File(TMP_NEW_REPO));
//		}
//		catch (IOException e) {
//			throw new RuntimeException(e);
//		}
	}

	@Test
	public void testNumberOfCommitsForFile() {
		for (int i = 1; i <= 7; i++) {
			String fileName = i + ".txt";

			int expected = i;

			if (i == 7) {
				expected = 0;
			}
			int number = this.gitConnector.numberOfCommitsForFile(fileName);

			assertEquals(expected, number);
		}
	}

	@Test
	public void testObtainCommitHashForFileAndVersion() {
		String[] fileNames = { "1.txt", "2.txt", "3.txt", "4.txt", "5.txt", "6.txt", "6.txt" };
		int[] versions = { 2, 2, 3, 4, 4, 9, -1 };
		String[] expectedHashes = {
				null,
				"f2b07350bea8106f95210dbbc19fe335d78e57de",
				"ce3cdfaae0d83b7f501d9893cf27707f4aa08ec1",
				"5070f5004f914c312a63839bf9459f19f6b34723",
				"8fe09b47f5c749f443f1e8bfeea88da3e968b0e5",
				null,
				"1026436c88ecce689eebe3171084959185cf4203"
		};
		for (int i = 0; i < fileNames.length; i++) {
			String fileName = fileNames[i];
			int version = versions[i];
			String expectedHash = expectedHashes[i];

			String accordingHash = gitConnector.commitHashForFileAndVersion(fileName, version);

			assertEquals("Filename: " + fileName + " version: " + version, expectedHash, accordingHash);
		}
	}

	@Test
	public void testGetCommitsSince() {

		//assumes to be no commit
		List<String> commitsSince = this.gitConnector.getCommitsSince(new Date());

		assertEquals(0, commitsSince.size());

		//all commits
		commitsSince = this.gitConnector.getCommitsSince(new Date(0));

		assertEquals(21, commitsSince.size());
	}

	@Test
	public void testGetBytesForPath() {

		//assumes to be no commit
		byte[] bytesForPath = this.gitConnector.getBytesForPath("2.txt", 2);
		assertEquals("Hier ist 2.2!",new String(bytesForPath));


		bytesForPath = this.gitConnector.getBytesForPath("4.txt", -1);
		assertEquals("Hier ist 4.4!",new String(bytesForPath));

		bytesForPath = this.gitConnector.getBytesForPath("6.txt", 5);
		assertEquals("Hier ist 6.5!",new String(bytesForPath));

		bytesForPath = this.gitConnector.getBytesForPath("6.txt", 55);
		assertEquals(null,bytesForPath);
	}

	@Test
	public void testGetTextForPath() {

		//assumes to be no commit
		String text = this.gitConnector.getTextForPath("2.txt", 2);
		assertEquals("Hier ist 2.2!",text);


		text = this.gitConnector.getTextForPath("4.txt", -1);
		assertEquals("Hier ist 4.4!",text);

		text = this.gitConnector.getTextForPath("6.txt", 5);
		assertEquals("Hier ist 6.5!",text);
	}
}
