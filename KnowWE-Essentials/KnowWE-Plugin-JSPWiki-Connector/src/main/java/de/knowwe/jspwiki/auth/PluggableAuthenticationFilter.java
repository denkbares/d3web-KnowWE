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
 * A filter for ALL URLs served by the wiki that can process any given credentials.
 *
 * @author Alex Legler (denkbares GmbH)
 * @created 2025-12-09
 */
public class PluggableAuthenticationFilter extends WikiServletFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluggableAuthenticationFilter.class);
	private WikiServletFilter delegate = null;

	@Override
	public void init(FilterConfig config) throws ServletException {
		super.init(config);

		final var className = m_engine.getWikiProperties().getProperty(KnowWEAuth.PROP_AUTH_FILTER);
		if (className != null && !className.isEmpty()) {
			try {
				delegate = ClassUtil.getMappedObject(className);
				delegate.init(config);
			}
			catch (ReflectiveOperationException e) {
				LOGGER.warn("Authentication filter instantiation has failed.", e);
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
