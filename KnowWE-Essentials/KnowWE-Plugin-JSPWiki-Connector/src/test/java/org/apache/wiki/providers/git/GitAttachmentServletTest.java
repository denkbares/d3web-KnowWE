/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package org.apache.wiki.providers.git;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.wiki.api.core.Attachment;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.attachment.AttachmentServlet;
import org.apache.wiki.http.filter.CsrfProtectionFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import de.uniwue.d3web.gitConnector.impl.bare.BareGitConnector;

import static org.apache.wiki.providers.git.GitTestWiki.JANNE;
import static org.apache.wiki.providers.git.GitTestWiki.JANNE_EMAIL;
import static org.apache.wiki.providers.git.GitTestWiki.JANNE_FULL_NAME;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Uploads and downloads through the JSPWiki {@link AttachmentServlet}, with multipart requests parsed by
 * commons-fileupload, onto the git attachment provider of a real engine.
 */
public class GitAttachmentServletTest {

	private static final String BOUNDARY = "----GitAttachmentServletTestBoundary";

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private GitTestWiki wiki;
	private AttachmentServlet servlet;
	private HttpServletRequest janne;

	@Before
	public void setUp() throws Exception {
		File probe = folder.newFolder("git-probe");
		assumeTrue(BareGitConnector.fromPath(probe.getAbsolutePath()).gitInstalledAndReady());
		wiki = GitTestWiki.start(folder.newFolder("wiki"), null);

		ServletConfig config = Mockito.mock(ServletConfig.class);
		Mockito.doReturn(wiki.engine.getServletContext()).when(config).getServletContext();
		servlet = new AttachmentServlet();
		servlet.init(config);

		janne = wiki.login(JANNE);
		wiki.save(janne, "Topic", "text");
	}

	@After
	public void tearDown() {
		if (wiki != null) {
			wiki.close();
		}
	}

	@Test
	public void anUploadIsCommittedWithTheUploadingUser() throws Exception {
		HttpServletResponse response = upload("data.txt", "uploaded content", "the change note");

		assertRedirectedTo(response, "Wiki.jsp?page=Topic");
		Attachment stored = wiki.attachments().getAttachmentInfo("Topic/data.txt");
		assertNotNull("the upload must be stored", stored);
		assertEquals(1, stored.getVersion());
		assertEquals(1, wiki.commitCount("Topic-att/data.txt"));
		assertEquals(JANNE_FULL_NAME + " <" + JANNE_EMAIL + ">", wiki.lastAuthor("Topic-att/data.txt"));
		assertEquals("the change note", wiki.git("log", "-1", "--format=%s", "--", "Topic-att/data.txt"));
	}

	@Test
	public void aSecondUploadIsANewVersion() throws Exception {
		upload("data.txt", "first", null);
		upload("data.txt", "second", null);

		assertEquals(2, wiki.commitCount("Topic-att/data.txt"));
		assertEquals(2, wiki.attachments().getAttachmentInfo("Topic/data.txt").getVersion());
		assertEquals("second", download("Topic/data.txt", null));
		assertEquals("first", download("Topic/data.txt", "1"));
	}

	@Test
	public void anUploadWithoutTheCsrfTokenIsRejected() throws Exception {
		HttpServletResponse response = post(multipart("Topic", "data.txt", "content", null, "wrong token"));

		ArgumentCaptor<String> location = ArgumentCaptor.forClass(String.class);
		verify(response).sendRedirect(location.capture());
		assertTrue(location.getValue(), location.getValue().contains("Error.jsp"));
		assertNull(wiki.attachments().getAttachmentInfo("Topic/data.txt"));
		assertEquals(0, wiki.commitCount("Topic-att/data.txt"));
	}

	@Test
	public void aDownloadServesTheStoredBytes() throws Exception {
		upload("data.txt", "uploaded content", null);

		assertEquals("uploaded content", download("Topic/data.txt", null));
	}

	private HttpServletResponse upload(String fileName, String content, String changeNote) throws Exception {
		String token = Wiki.session().find(wiki.engine, janne).antiCsrfToken();
		return post(multipart("Topic", fileName, content, changeNote, token));
	}

	private HttpServletResponse post(byte[] body) throws IOException {
		HttpServletRequest request = wiki.request(janne, "/attach");
		Mockito.doReturn("POST").when(request).getMethod();
		Mockito.doReturn("multipart/form-data; boundary=" + BOUNDARY).when(request).getContentType();
		Mockito.doReturn(body.length).when(request).getContentLength();
		Mockito.doReturn((long) body.length).when(request).getContentLengthLong();
		Mockito.doReturn(String.valueOf(body.length)).when(request).getHeader("Content-Length");
		Mockito.doReturn(StandardCharsets.UTF_8.name()).when(request).getCharacterEncoding();
		Mockito.doReturn("/Topic").when(request).getPathInfo();
		Mockito.doReturn(inputStream(body)).when(request).getInputStream();
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		servlet.doPost(request, response);
		return response;
	}

	private String download(String attachmentName, String version) throws IOException {
		HttpServletRequest request = wiki.request(janne, "/attach");
		Mockito.doReturn("GET").when(request).getMethod();
		Mockito.doReturn("/" + attachmentName).when(request).getPathInfo();
		Mockito.doReturn(version).when(request).getParameter("version");
		Mockito.doReturn(-1L).when(request).getDateHeader(Mockito.anyString());
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.doReturn(outputStream(body)).when(response).getOutputStream();

		servlet.doGet(request, response);

		verify(response, never()).sendError(Mockito.anyInt());
		verify(response, never()).sendError(Mockito.anyInt(), Mockito.anyString());
		return body.toString(StandardCharsets.UTF_8);
	}

	private static void assertRedirectedTo(HttpServletResponse response, String nextPage) throws IOException {
		verify(response).sendRedirect(nextPage);
	}

	/**
	 * The form the attachment upload page posts: the parent page, the next page, the change note, the CSRF token and
	 * the file.
	 */
	private static byte[] multipart(String page, String fileName, String content, String changeNote, String token) {
		StringBuilder body = new StringBuilder();
		field(body, "page", page);
		field(body, "nextpage", "Wiki.jsp?page=" + page);
		if (changeNote != null) {
			field(body, "changenote", changeNote);
		}
		field(body, CsrfProtectionFilter.ANTICSRF_PARAM, token);
		body.append("--").append(BOUNDARY).append("\r\n")
				.append("Content-Disposition: form-data; name=\"content\"; filename=\"").append(fileName).append("\"\r\n")
				.append("Content-Type: text/plain\r\n\r\n")
				.append(content).append("\r\n")
				.append("--").append(BOUNDARY).append("--\r\n");
		return body.toString().getBytes(StandardCharsets.UTF_8);
	}

	private static void field(StringBuilder body, String name, String value) {
		body.append("--").append(BOUNDARY).append("\r\n")
				.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n")
				.append(value).append("\r\n");
	}

	private static ServletInputStream inputStream(byte[] bytes) {
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		return new ServletInputStream() {
			@Override
			public int read() {
				return in.read();
			}

			@Override
			public int read(byte[] buffer, int offset, int length) {
				return in.read(buffer, offset, length);
			}

			@Override
			public boolean isFinished() {
				return in.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener listener) {
				throw new UnsupportedOperationException();
			}
		};
	}

	private static ServletOutputStream outputStream(ByteArrayOutputStream out) {
		return new ServletOutputStream() {
			@Override
			public void write(int b) {
				out.write(b);
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setWriteListener(WriteListener listener) {
				throw new UnsupportedOperationException();
			}
		};
	}
}
