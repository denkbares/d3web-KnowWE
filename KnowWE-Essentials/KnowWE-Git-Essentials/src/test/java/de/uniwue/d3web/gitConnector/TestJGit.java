package de.uniwue.d3web.gitConnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.denkbares.utils.Files;
import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;
import de.uniwue.d3web.gitConnector.impl.cached.CachingGitConnector;

import static org.junit.Assert.assertEquals;

public class TestJGit {

	private File TMP_NEW_REPO = null;
	private String TEST_GIT = "testGit.zip";
//	private String TMP_NEW_REPO = "/Users/mkrug/temp/konap_test/wiki";

	private GitConnector gitConnector;

	@Before
	public void init() {

		try {
			TMP_NEW_REPO = Files.createTempDir();
			InputStream resourceAsStream = TestJGit.class.getClassLoader().getResourceAsStream(TEST_GIT);
			File zipFile = Paths.get(TMP_NEW_REPO.getAbsolutePath(), TEST_GIT).toFile();
			FileUtils.copyInputStreamToFile(resourceAsStream,zipFile);

			//unzip
			File destDir = Paths.get(TMP_NEW_REPO.getAbsolutePath(), TEST_GIT.replaceAll(".zip","")).toFile();

			byte[] buffer = new byte[1024];
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				while (zipEntry != null) {
					File newFile = newFile(destDir, zipEntry);
					if (zipEntry.isDirectory()) {
						if (!newFile.isDirectory() && !newFile.mkdirs()) {
							throw new IOException("Failed to create directory " + newFile);
						}
					} else {
						// fix for Windows-created archives
						File parent = newFile.getParentFile();
						if (!parent.isDirectory() && !parent.mkdirs()) {
							throw new IOException("Failed to create directory " + parent);
						}

						// write file content
						FileOutputStream fos = new FileOutputStream(newFile);
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						fos.close();
					}
					zipEntry = zis.getNextEntry();
				}
			}

			zis.closeEntry();
			zis.close();

		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		gitConnector = new CachingGitConnector(BareGitConnector.fromPath(Paths.get(TMP_NEW_REPO.getAbsolutePath(), TEST_GIT.replaceAll(".zip",""),"testGit").toFile()
				.getAbsolutePath()));
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
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
