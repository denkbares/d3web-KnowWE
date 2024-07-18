package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class TestHttpServletResponse implements HttpServletResponse {

	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final ByteArrayServletOutputStream servletOutputStream = new ByteArrayServletOutputStream(out);

	private final Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);

	private final StringWriter stringWriter = new StringWriter();
	private final PrintWriter printWriter = new PrintWriter(stringWriter);

	private int status;

	@Override
	public void addCookie(Cookie cookie) {
	}


	public String getContent() {
		return stringWriter.toString();
	}


	@Override
	public boolean containsHeader(String s) {
		return false;
	}

	@Override
	public String encodeURL(String s) {
		return null;
	}

	@Override
	public String encodeRedirectURL(String s) {
		return null;
	}

	@Override
	public String encodeUrl(String s) {
		return null;
	}

	@Override
	public String encodeRedirectUrl(String s) {
		return null;
	}

	@Override
	public void sendError(int i, String s) throws IOException {

	}

	@Override
	public void sendError(int i) throws IOException {

	}

	@Override
	public void sendRedirect(String s) throws IOException {

	}

	@Override
	public void setDateHeader(String s, long l) {

	}

	@Override
	public void addDateHeader(String s, long l) {

	}

	@Override
	public void setHeader(String s, String s1) {

	}

	@Override
	public void addHeader(String s, String s1) {

	}

	@Override
	public void setIntHeader(String s, int i) {

	}

	@Override
	public void addIntHeader(String s, int i) {

	}

	@Override
	public void setStatus(int statusCode) {
		this.status = statusCode;
	}

	@Override
	public void setStatus(int i, String s) {
		setStatus(i);
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public String getHeader(String s) {
		return null;
	}

	@Override
	public Collection<String> getHeaders(String s) {
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		return null;
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return servletOutputStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return printWriter;
	}

	@Override
	public void setCharacterEncoding(String s) {

	}

	@Override
	public void setContentLength(int i) {

	}

	@Override
	public void setContentLengthLong(long l) {

	}

	@Override
	public void setContentType(String s) {

	}

	@Override
	public void setBufferSize(int i) {

	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		servletOutputStream.flush();
		out.flush();
		writer.flush();
		printWriter.flush();
	}

	@Override
	public void resetBuffer() {

	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {

	}

	@Override
	public void setLocale(Locale locale) {

	}

	@Override
	public Locale getLocale() {
		return null;
	}

	static class ByteArrayServletOutputStream extends ServletOutputStream {
		final ByteArrayOutputStream m_buffer;

		ByteArrayServletOutputStream(final ByteArrayOutputStream byteArrayOutputStream) {
			this.m_buffer = byteArrayOutputStream;
		}

		@Override
		public void write(final int aInt) {
			this.m_buffer.write(aInt);
		}

		@Override
		public boolean isReady() {
			return false;
		}

		@Override
		public void setWriteListener(final WriteListener writeListener) {
		}
	}
}
