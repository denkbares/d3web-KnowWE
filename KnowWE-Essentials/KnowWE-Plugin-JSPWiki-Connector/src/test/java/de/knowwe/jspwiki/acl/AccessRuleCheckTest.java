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

package de.knowwe.jspwiki.acl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.wiki.TestEngine;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.authorize.XMLGroupDatabase;
import org.apache.wiki.auth.user.UserDatabase;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.auth.user.XMLUserDatabase;
import org.junit.BeforeClass;
import org.junit.Test;

import de.knowwe.core.report.Messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Checks {@link AccessRuleCheck} against a real JSPWiki engine with the users and groups of the JSPWiki test
 * databases, e.g. loginName 'janne', fullName 'Janne Jalkanen', wikiName 'JanneJalkanen', and the group 'TV'.
 */
public class AccessRuleCheckTest {

	private static final ResourceBundle GERMAN = Messages.getMessageBundle(Locale.GERMAN);
	private static final ResourceBundle ENGLISH = Messages.getMessageBundle(Locale.ENGLISH);

	private static AccessRuleCheck check;

	@BeforeClass
	public static void setUp() throws Exception {
		TestEngine engine = createEngine(Path.of("target/accessRuleCheckTest"), new Properties());
		UserDatabase userDatabase = engine.getManager(UserManager.class).getUserDatabase();
		// the login name of this one is Fred's wiki name
		addUser(userDatabase, "FredFlintstone", "Fred Second");
		// the login name of this one is the name of a group
		addUser(userDatabase, "TV", "Tv Fan");
		check = checkOf(engine);
	}

	private static TestEngine createEngine(Path dir, Properties extra) throws Exception {
		Files.createDirectories(dir);
		Properties properties = TestEngine.getTestProperties();
		properties.setProperty(XMLUserDatabase.PROP_USERDATABASE, copyResource("/userdatabase.xml", dir).toString());
		properties.setProperty(XMLGroupDatabase.PROP_DATABASE, copyResource("/groupdatabase.xml", dir).toString());
		properties.putAll(extra);
		return new TestEngine(properties);
	}

	private static Path copyResource(String resource, Path dir) throws IOException {
		Path target = dir.resolve(resource.substring(1)).toAbsolutePath();
		try (InputStream in = AccessRuleCheckTest.class.getResourceAsStream(resource)) {
			Files.copy(Objects.requireNonNull(in, "missing classpath resource " + resource), target,
					StandardCopyOption.REPLACE_EXISTING);
		}
		// jspwiki 3 refuses to start if a database no longer matches the integrity hash it wrote next to it
		Files.deleteIfExists(target.resolveSibling(target.getFileName() + ".check"));
		return target;
	}

	private static void addUser(UserDatabase userDatabase, String loginName, String fullName) throws Exception {
		UserProfile profile = userDatabase.newProfile();
		profile.setLoginName(loginName);
		profile.setFullname(fullName);
		profile.setEmail(loginName + "@example.com");
		profile.setPassword("secret");
		userDatabase.save(profile);
	}

	private static AccessRuleCheck checkOf(Engine engine) {
		return new AccessRuleCheck(engine.getManager(AuthorizationManager.class),
				engine.getManager(UserManager.class).getUserDatabase(), GERMAN);
	}

	@Test
	public void entriesThatTakeEffectAreNotReported() {
		// wiki name, login name, both at once, group, built-in roles
		for (String name : List.of("JanneJalkanen", "janne", "Alice", "Literature", "Authenticated", "All")) {
			assertNull(name, check.checkEntry(name));
		}
		assertTrue(check.checkAccessRule("[{ALLOW edit JanneJalkanen, janne, Authenticated}]").isEmpty());
	}

	@Test
	public void aFullNameIsIneffectiveAndTheWikiNameIsSuggested() {
		AccessRuleCheck.Problem problem = check.checkEntry("Janne Jalkanen");
		assertEquals("ACL-Eintrag „Janne Jalkanen“ ist unwirksam: Seit JSPWiki 3 begründen volle Namen keine "
				+ "Berechtigung mehr. Bitte stattdessen den Wiki-Namen „JanneJalkanen“ verwenden.", problem.verbalize(GERMAN));
		assertEquals("ACL entry \"Janne Jalkanen\" is ineffective: since JSPWiki 3, full names no longer grant any "
				+ "permission. Please use the wiki name \"JanneJalkanen\" instead.", problem.verbalize(ENGLISH));
	}

	@Test
	public void aNameOfNobodyIsIneffective() {
		assertEquals("ACL-Eintrag „Redaktoin“ ist unwirksam: Es existiert weder ein Benutzer noch eine Gruppe oder "
				+ "Rolle dieses Namens.", check.checkEntry("Redaktoin").verbalize(GERMAN));
	}

	@Test
	public void theLoginNameOfOneAndTheWikiNameOfAnotherUserIsAmbiguous() {
		assertEquals("ACL-Eintrag „FredFlintstone“ ist mehrdeutig. Er gilt für: Benutzer „Fred Second“ (Login-Name), "
				+ "Benutzer „Fred Flintstone“ (Wiki-Name).", check.checkEntry("FredFlintstone").verbalize(GERMAN));
	}

	@Test
	public void aGroupHidesAUserOfTheSameName() {
		assertEquals("ACL-Eintrag „TV“ ist mehrdeutig. Er gilt für die Gruppe „TV“, nicht jedoch für "
				+ "Benutzer „Tv Fan“ (Login-Name).", check.checkEntry("TV").verbalize(GERMAN));
	}

	@Test
	public void everyEntryOfTheMarkupIsChecked() {
		List<AccessRuleCheck.Problem> problems =
				check.checkAccessRule("[{ALLOW edit Janne Jalkanen,JanneJalkanen , Redaktoin}]");
		assertEquals(List.of(AccessRuleCheck.FULL_NAME, AccessRuleCheck.UNKNOWN),
				problems.stream().map(AccessRuleCheck.Problem::key).toList());
		assertTrue("not an ACL", check.checkAccessRule("[{TableOfContents}]").isEmpty());
	}

	@Test
	public void withTheOldAccessControlLogicFullNamesStillTakeEffect() throws Exception {
		Properties old = new Properties();
		old.setProperty(Engine.PROP_USE_2_X_ACL_LOGIC, "true");
		TestEngine engine = createEngine(Path.of("target/accessRuleCheckTest-old"), old);
		assertNull(checkOf(engine).checkEntry("Janne Jalkanen"));
	}
}
