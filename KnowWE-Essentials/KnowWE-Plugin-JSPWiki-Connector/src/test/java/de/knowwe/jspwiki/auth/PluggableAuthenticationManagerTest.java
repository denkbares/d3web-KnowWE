package de.knowwe.jspwiki.auth;

import java.security.Principal;
import java.util.Properties;

import org.apache.wiki.WikiSession;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Session;
import org.apache.wiki.auth.AuthenticationManager;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.WikiPrincipal;
import org.apache.wiki.auth.authorize.GroupManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.event.WikiSecurityEvent;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluggableAuthenticationManagerTest {

	private Engine engine;
	private PluggableAuthenticationManager manager;

	@Before
	public void setUp() throws Exception {
		engine = mock(Engine.class);
		manager = new PluggableAuthenticationManager();
		manager.initialize(engine, new Properties());

		GroupManager groups = mock(GroupManager.class);
		when(groups.getRoles()).thenReturn(new Principal[0]);

		UserProfile profile = mock(UserProfile.class);
		when(profile.getLoginName()).thenReturn("alice");
		UserDatabase database = mock(UserDatabase.class);
		when(database.find("alice")).thenReturn(profile);
		when(database.getPrincipals("alice")).thenReturn(
				new Principal[] { new WikiPrincipal("alice", WikiPrincipal.LOGIN_NAME) });
		UserManager users = mock(UserManager.class);
		when(users.getUserDatabase()).thenReturn(database);

		when(engine.getManager(AuthenticationManager.class)).thenReturn(manager);
		when(engine.getManager(GroupManager.class)).thenReturn(groups);
		when(engine.getManager(UserManager.class)).thenReturn(users);
	}

	@Test
	public void firedEventReachesSessionsRegisteredThroughTheWrapper() {
		Session session = WikiSession.guestSession(engine);
		assertFalse(session.isAuthenticated());

		manager.fireEvent(WikiSecurityEvent.LOGIN_AUTHENTICATED,
				new WikiPrincipal("alice", WikiPrincipal.LOGIN_NAME), session);

		assertTrue(session.isAuthenticated());
		assertEquals("alice", session.getLoginPrincipal().getName());
	}

	@Test
	public void eventForAnotherSessionLeavesThisOneUntouched() {
		Session session = WikiSession.guestSession(engine);
		Session other = WikiSession.guestSession(engine);

		manager.fireEvent(WikiSecurityEvent.LOGIN_AUTHENTICATED,
				new WikiPrincipal("alice", WikiPrincipal.LOGIN_NAME), other);

		assertFalse(session.isAuthenticated());
		assertTrue(other.isAuthenticated());
	}

	@Test
	public void fireBeforeInitializeIsIgnored() {
		new PluggableAuthenticationManager().fireEvent(WikiSecurityEvent.LOGIN_AUTHENTICATED,
				new WikiPrincipal("alice", WikiPrincipal.LOGIN_NAME), mock(Session.class));
	}
}
