/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.d3web.ontology.bridge;

import java.util.UUID;

import connector.DummyConnector;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.TestUtils;

import com.denkbares.plugin.test.InitPluginManager;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.ontology.compile.OntologyCompiler;

import static org.junit.Assert.*;

/** Real package registration and dependency recompilation, with minimal public markup and no private knowledge base. */
public class OntologyBridgeLifecycleTest {

	@BeforeClass
	public static void initialize() throws Exception {
		InitPluginManager.init();
		if (!Environment.isInitialized()) {
			DummyConnector connector = new DummyConnector();
			connector.setKnowWEExtensionPath(TestUtils.createKnowWEExtensionPath());
			Environment.initInstance(connector);
		}
	}

	@Test
	public void bridgeFollowsOneSidedAndCombinedArticleRebuilds() throws Exception {
		ArticleManager manager = Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB);
		String suffix = UUID.randomUUID().toString();
		String ontologyTitle = "Ontology-" + suffix;
		String kbTitle = "KB-" + suffix;
		String ontologyText = "%%Ontology\n@name " + ontologyTitle + "\n%";
		String kbText = "%%KnowledgeBase\n@name " + kbTitle + "\n@importOntology " + ontologyTitle + "\n%";
		try {
			manager.open();
			try {
				manager.registerArticle(ontologyTitle, ontologyText);
				manager.registerArticle(kbTitle, kbText);
			}
			finally { manager.commit(); }
			assertBridge(manager, kbTitle, ontologyTitle);
			Section<ImportOntologyAnnotationType> originalImport = Sections.$(manager.getArticle(kbTitle))
					.successor(ImportOntologyAnnotationType.class).getFirst();
			assertNotNull(originalImport);
			String oldOntologyId = ontology(manager, ontologyTitle).getCompileSection().getID();
			manager.registerArticle(ontologyTitle, ontologyText);
			assertBridge(manager, kbTitle, ontologyTitle);
			assertEquals(oldOntologyId, ontology(manager, ontologyTitle).getCompileSection().getID());
			manager.registerArticle(ontologyTitle, ontologyText + "\nChanged surrounding text\n");
			assertBridge(manager, kbTitle, ontologyTitle);
			assertNotEquals(oldOntologyId, ontology(manager, ontologyTitle).getCompileSection().getID());
			manager.registerArticle(kbTitle, kbText);
			assertBridge(manager, kbTitle, ontologyTitle);
			// The old registration's stored ownership has already been consumed by normal destruction.
			new ImportOntologyAnnotationType.ImportOntologyCompileScript().destroy(
					Compilers.getPackageRegistrationCompiler(manager), originalImport);
			assertBridge(manager, kbTitle, ontologyTitle);
			manager.registerArticle(kbTitle, kbText + "\nChanged surrounding text\n");
			assertBridge(manager, kbTitle, ontologyTitle);
			manager.open();
			try {
				manager.registerArticle(ontologyTitle, ontologyText);
				manager.registerArticle(kbTitle, kbText);
			}
			finally { manager.commit(); }
			assertBridge(manager, kbTitle, ontologyTitle);
			manager.deleteArticle(ontologyTitle);
			assertTrue(manager.getCompilerManager().awaitTermination(10000));
			assertFalse(OntologyBridge.hasOntology(Compilers.getCompiler(manager.getArticle(kbTitle), D3webCompiler.class)));
			manager.registerArticle(ontologyTitle, ontologyText);
			assertBridge(manager, kbTitle, ontologyTitle);
			String finalKbId = Compilers.getCompiler(manager.getArticle(kbTitle), D3webCompiler.class).getCompileSection().getID();
			String finalOntologyId = ontology(manager, ontologyTitle).getCompileSection().getID();
			manager.deleteArticle(kbTitle);
			assertTrue(manager.getCompilerManager().awaitTermination(10000));
			assertNull(OntologyBridge.getMappedOntologySectionId(finalKbId));
			assertNull(OntologyBridge.getMappedD3webSectionId(finalOntologyId));
		}
		finally {
			if (manager.getArticle(kbTitle) != null) manager.deleteArticle(kbTitle);
			manager.deleteArticle(ontologyTitle);
			assertTrue(manager.getCompilerManager().awaitTermination(10000));
		}
	}

	private static OntologyCompiler ontology(ArticleManager manager, String title) {
		return Compilers.getCompiler(manager.getArticle(title), OntologyCompiler.class);
	}

	private static void assertBridge(ArticleManager manager, String kbTitle, String ontologyTitle) throws Exception {
		assertTrue("Compilation timed out", manager.getCompilerManager().awaitTermination(10000));
		D3webCompiler kb = Compilers.getCompiler(manager.getArticle(kbTitle), D3webCompiler.class);
		OntologyCompiler ontology = ontology(manager, ontologyTitle);
		assertNotNull("Knowledge-base compiler missing", kb);
		assertNotNull("Ontology compiler missing", ontology);
		assertTrue(OntologyBridge.hasOntology(kb));
		assertSame(ontology, OntologyBridge.getOntology(kb));
		assertSame(kb, OntologyBridge.getCompiler(ontology));
	}
}
