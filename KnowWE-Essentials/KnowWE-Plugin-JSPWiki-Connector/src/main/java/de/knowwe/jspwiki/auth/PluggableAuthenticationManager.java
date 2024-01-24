package de.knowwe.jspwiki.auth;

import java.security.Principal;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;
import javax.servlet.http.HttpServletRequest;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Session;
import org.apache.wiki.api.exceptions.WikiException;
import org.apache.wiki.auth.AuthenticationManager;
import org.apache.wiki.auth.DefaultAuthenticationManager;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.event.WikiEventListener;
import org.apache.wiki.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegates authentication management to a class specified in the KnowWE configuration.
 * Should no configuration exist, the default upstream authentication manager is employed.
 *
 * @author Alex Legler (denkbares GmbH)
 * @created 2024-01-22
 */
public class PluggableAuthenticationManager implements AuthenticationManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluggableAuthenticationManager.class);

	private AuthenticationManager delegate = null;

	@Override
	public void initialize(Engine engine, Properties props) throws WikiException {
		final var className = props.getProperty(KnowWEAuth.PROP_AUTHN_MANAGER);

		if (className != null && !"".equals(className)) {
			// Use the upstream-provided mapping and instantiation foo
			try {
				delegate = ClassUtil.getMappedObject(className);
				delegate.initialize(engine, props);
				LOGGER.info("Authentication management delegated to " + className);
			}
			catch (ReflectiveOperationException | ClassCastException e) {
				LOGGER.warn("Instantiating requested authentication manager has failed.", e);
				throw new WikiException("Instantiating authentication manager has failed.", e);
			}
		}

		if (delegate == null) {
			LOGGER.info("Initializing JSPWiki default authentication manager.");
			delegate = new DefaultAuthenticationManager();
			delegate.initialize(engine, props);
		}
	}

	@Override
	public boolean isContainerAuthenticated() {
		return delegate.isContainerAuthenticated();
	}

	@Override
	public boolean login(HttpServletRequest request) throws WikiSecurityException {
		return delegate.login(request);
	}

	@Override
	public boolean login(Session session, HttpServletRequest request, String username, String password) throws WikiSecurityException {
		return delegate.login(session, request, username, password);
	}

	@Override
	public void logout(HttpServletRequest request) {
		delegate.logout(request);
	}

	@Override
	public boolean allowsCookieAssertions() {
		return delegate.allowsCookieAssertions();
	}

	@Override
	public boolean allowsCookieAuthentication() {
		return delegate.allowsCookieAuthentication();
	}

	@Override
	public Set<Principal> doJAASLogin(Class<? extends LoginModule> clazz, CallbackHandler handler, Map<String, String> options) throws WikiSecurityException {
		return delegate.doJAASLogin(clazz, handler, options);
	}

	@Override
	public void addWikiEventListener(WikiEventListener listener) {
		delegate.addWikiEventListener(listener);
	}

	@Override
	public void removeWikiEventListener(WikiEventListener listener) {
		delegate.removeWikiEventListener(listener);
	}
}
