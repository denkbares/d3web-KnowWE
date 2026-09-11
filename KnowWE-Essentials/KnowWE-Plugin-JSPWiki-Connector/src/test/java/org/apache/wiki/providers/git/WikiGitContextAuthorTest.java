/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package org.apache.wiki.providers.git;

import java.util.HashMap;
import java.util.Map;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.providers.commentStrategy.GitCommentStrategy;
import org.junit.Before;
import org.junit.Test;

import de.knowwe.jspwiki.auth.AuthenticatedIdentities;
import de.uniwue.d3web.gitConnector.CommitUserData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Who a commit is attributed to: the wiki's user database where it has a profile, else what the identity provider
 * stated at sign-in, else the name as it is.
 */
public class WikiGitContextAuthorTest {

	private Engine engine;
	private UserDatabase users;
	private WikiGitContext context;

	@Before
	public void setUp() throws Exception {
		engine = mock(Engine.class);
		// the engine's attributes, which is where the identities learned at sign-in live
		Map<String, Object> attributes = new HashMap<>();
		when(engine.getAttribute(anyString())).thenAnswer(call -> attributes.get(call.<String>getArgument(0)));
		doAnswer(call -> attributes.put(call.getArgument(0), call.getArgument(1)))
				.when(engine).setAttribute(anyString(), any());
		users = mock(UserDatabase.class);
		when(users.findByFullName(anyString())).thenThrow(new NoSuchPrincipalException("nobody"));
		UserManager userManager = mock(UserManager.class);
		when(userManager.getUserDatabase()).thenReturn(users);
		when(engine.getManager(UserManager.class)).thenReturn(userManager);
		context = new WikiGitContext(engine, mock(GitCommentStrategy.class));
	}

	@Test
	public void aProfileInTheUserDatabaseWins() throws Exception {
		UserProfile profile = mock(UserProfile.class);
		when(profile.getFullname()).thenReturn("Hedwig Owl");
		when(profile.getEmail()).thenReturn("hedwig@hogwarts.example");
		doReturn(profile).when(users).findByFullName("hedwig");
		AuthenticatedIdentities.remember(engine,
				new AuthenticatedIdentities.Identity("hedwig", "Someone Else", "else@example.com"));

		CommitUserData author = context.userData("hedwig", "saved");

		assertEquals("Hedwig Owl", author.user);
		assertEquals("hedwig@hogwarts.example", author.email);
		assertEquals("saved", author.message);
	}

	@Test
	public void aUserKnownFromSignInIsAttributedAsTheProviderStatedThem() {
		AuthenticatedIdentities.remember(engine,
				new AuthenticatedIdentities.Identity("hedwig", "Hedwig Owl", "hedwig@hogwarts.example"));

		// by login name, which is what the wiki passes as the author
		CommitUserData author = context.userData("hedwig", "saved");
		assertEquals("Hedwig Owl", author.user);
		assertEquals("hedwig@hogwarts.example", author.email);
	}

	@Test
	public void aChosenDisplayNameCannotTakeOverAnotherUsersEntry() {
		AuthenticatedIdentities.remember(engine,
				new AuthenticatedIdentities.Identity("hedwig", "Hedwig Owl", "hedwig@hogwarts.example"));
		// somebody else picks hedwig's login name as their display name
		AuthenticatedIdentities.remember(engine,
				new AuthenticatedIdentities.Identity("impostor", "hedwig", "impostor@example.com"));

		CommitUserData author = context.userData("hedwig", "saved");
		assertEquals("Hedwig Owl", author.user);
		assertEquals("hedwig@hogwarts.example", author.email);
	}

	@Test
	public void anUnknownUserKeepsTheirNameAndGetsNoMadeUpAddress() {
		CommitUserData author = context.userData("root", "saved");

		assertEquals("root", author.user);
		assertEquals("", author.email);
	}

	@Test
	public void anAuthorThatIsAnAddressIsUsedAsTheEmail() {
		CommitUserData author = context.userData("jane.doe@example.com", "saved");

		assertEquals("jane.doe@example.com", author.user);
		assertEquals("jane.doe@example.com", author.email);
	}

	@Test
	public void nobodyIsKnownBeforeAnyoneSignedIn() {
		assertNull(AuthenticatedIdentities.of(engine, "hedwig"));
		assertNull(AuthenticatedIdentities.of(engine, null));
	}
}
