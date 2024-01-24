package de.knowwe.jspwiki.auth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.wiki.ui.WikiServletFilter;
import org.apache.wiki.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A filter for the JSPWiki Login.jsp page that can redirect users to the appropriate SSO provider,
 * or just show the JSP contents.
 *
 * @author Alex Legler (denkbares GmbH)
 * @created 2024-01-22
 */
public class PluggableLoginPageFilter extends WikiServletFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluggableLoginPageFilter.class);
	private WikiServletFilter delegate = null;

	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(config);

		final var className = m_engine.getWikiProperties().getProperty(KnowWEAuth.PROP_LOGIN_PAGE_FILTER);
		if (className != null && !"".equals(className)) {
			try {
				delegate = ClassUtil.getMappedObject(className);
				delegate.init(config);
			}
			catch (ReflectiveOperationException e) {
				LOGGER.warn("Login page filter instantiation has failed.", e);
			}
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (delegate != null) {
			delegate.doFilter(request, response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}
}
