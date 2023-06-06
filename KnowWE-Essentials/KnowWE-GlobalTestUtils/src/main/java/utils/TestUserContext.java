/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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
package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.Action;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.user.AuthenticationManager;
import de.knowwe.core.user.UserContext;
import de.knowwe.jspwiki.KnowWEPlugin;

/**
 * 
 * @author volker_belli
 * @created 04.08.2011
 */
public class TestUserContext implements UserContext, UserActionContext {

	private static int counter = 0;

	private final boolean isAdmin;
	private final boolean isAsserted;
	private final String genericUserName = "Testuser #" + (counter++);
	private String username = null;
	private final Article article;
	private final Map<String, String> parameterMap = new HashMap<>();
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	private final Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
	private final TestHttpSession session = new TestHttpSession();
	private final TestHttpServletRequest request = new TestHttpServletRequest(session);

	public TestUserContext(Article article) {
		this(article, false, false);
	}

	/**
	 * Creates a test user for an article that does not really exist as an object.
	 * If possible, prefer to use the constructor #TestUserContext(Article), because it
	 * will provide much more capabilities to the created object.
	 *
	 * @param articleName the name of the article to be created.
	 */
	public TestUserContext(String articleName) {
		this(Article.createTemporaryArticle("", articleName, Environment.DEFAULT_WEB));
	}

	public TestUserContext(String articleName, String username) {
		this(Article.createTemporaryArticle("", articleName, Environment.DEFAULT_WEB));
		this.username = username;
	}

	public TestUserContext(Article article, boolean isAdmin, boolean isAsserted) {
		this.article = article;
		this.isAdmin = isAdmin;
		this.isAsserted = isAsserted;
		addParameter(Attributes.WEB, getWeb());
		addParameter(Attributes.TITLE, getTitle());
		addParameter(Attributes.USER, getUserName());
	}

	@Override
	public boolean userIsAdmin() {
		return isAdmin;
	}

	@Override
	public boolean userIsAsserted() {
		return isAsserted;
	}

	@Override
	public String getUserName() {
		return this.username != null ? username : genericUserName;
	}

	@Override
	public String getTitle() {
		return article.getTitle();
	}

	@Override
	public String getWeb() {
		return article.getWeb();
	}

	@Override
	public Map<String, String> getParameters() {
		return this.parameterMap;
	}

	@Override
	public String getParameter(String key) {
		return this.getParameters().get(key);
	}

	@Override
	public String getParameter(String key, String defaultValue) {
		return this.getParameters().get(key) != null
				? this.getParameters().get(key)
				: defaultValue;
	}

	public void addParameter(String key, String value) {
		this.parameterMap.put(key, value);
	}

	@Override
	public HttpServletRequest getRequest() {
		return this.request;
	}

	@Override
	public HttpSession getSession() {
		return this.session;
	}

	@Override
	public ServletContext getServletContext() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public Action getAction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpServletResponse getResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getActionName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuthenticationManager getManager() {
		return null;
	}

	@Override
	public Writer getWriter() throws IOException {
		return writer;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public void setContentType(String mimetype) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentLength(int length) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRedirect(String location)  {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendNavigate(String location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHeader(String name, String value) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		// we write errors to the output stream, replacing the content
		writer.flush();
		out.reset();
		writer.append(String.valueOf(sc)).append(": ").append(msg);
	}

	@Override
	public ArticleManager getArticleManager() {
		return article.getArticleManager();
	}

	@Override
	public boolean isRenderingPreview() {
		return KnowWEPlugin.PREVIEW.equals(getRequest().getAttribute(KnowWEPlugin.RENDER_MODE));
	}

	@Override
	public Article getArticle() {
		return article;
	}

	public String getResponseText() {
		try {
			writer.flush();
			return out.toString(StandardCharsets.UTF_8);
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("platform must support utf-8");
		}
		catch (IOException e) {
			// must not happen
			throw new IllegalStateException("write error writing byte[] only?");
		}
	}
}
