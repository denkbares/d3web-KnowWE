package de.d3web.we.javaEnv;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class KnowWEParameterMap extends HashMap<String, String> {

	private HttpSession session;
	private ServletContext context;
	private HttpServletRequest request;
	private KnowWEUserContext wikiContext;

	public KnowWEParameterMap(KnowWEUserContext wikiContext,
			HttpServletRequest r, ServletContext c, KnowWEEnvironment e) {
		if (r != null) {
			this.session = r.getSession();
		}
		this.wikiContext = wikiContext;
		this.request = r;
		this.context = c;

		Enumeration<String> paramNames = r.getParameterNames();
		if ((paramNames != null)) {
			while ((paramNames.hasMoreElements())) {
				String name = paramNames.nextElement();
				this.put(name, r.getParameter(name));
			}
		}
	}

	public KnowWEParameterMap(String name, String value) {

		this.put(name, value);
	}

	public HttpSession getSession() {
		return session;
	}

	public ServletContext getContext() {
		return context;
	}

	public String getUser() {
		return this.get(KnowWEAttributes.USER);
	}

	public String getWeb() {
		return this.get(KnowWEAttributes.WEB);
	}

	public String getTopic() {
		return this.get(KnowWEAttributes.TOPIC);
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public KnowWEUserContext getWikiContext() {
		return this.wikiContext;
	}
}
