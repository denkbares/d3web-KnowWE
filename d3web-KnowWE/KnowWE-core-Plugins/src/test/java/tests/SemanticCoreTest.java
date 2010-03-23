package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.SemanticCore;
import de.d3web.we.core.TaggingMangler;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import dummies.KnowWETestWikiConnector;

public class SemanticCoreTest {

	private KnowWEEnvironment ke;
	private KnowWEArticleManager am;
	private KnowWEObjectType type;
	private KnowWEParameterMap params;
	private SemanticCore sc;

	@Before
	public void setUp() throws Exception {
		InitPluginManager.init();
		/*
		 * Initialise KnowWEEnvironment
		 */
		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		ke = KnowWEEnvironment.getInstance();
		am = KnowWEEnvironment.getInstance().getArticleManager("default_web");
		type = ke.getRootType();

		/*
		 * Init first Article
		 */
		KnowWEArticle article1 = new KnowWEArticle("", "TagTest", type,
				"default_web");

		am.saveUpdatedArticle(article1);
		params = new KnowWEParameterMap("", "");
		sc = SemanticCore.getInstance();
	}

	@Test
	public void testNamespaceStuff() {
		// check if we have sc
		assertNotNull(sc);
		assertNotNull(SemanticCore.getInstance());
		// set a new namespace
		sc.addNamespace("swrc", "http://swrc.ontoware.org/ontology#");
		// get all namespaces
		HashMap<String, String> nspaces = sc.getNameSpaces();
		// check if is in the namespaces map
		assertEquals("http://swrc.ontoware.org/ontology#", nspaces.get("swrc"));
		// check if is correctly expanded
		assertEquals("http://swrc.ontoware.org/ontology#", sc
				.expandNamespace("swrc"));
		// check out defaultnamespaces
		nspaces = sc.getDefaultNameSpaces();
		// check for all the default ns
		assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#", nspaces
				.get("rdf"));
		assertEquals(
				"http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#",
				nspaces.get("ns"));
		assertEquals("http://www.w3.org/2002/07/owl#", nspaces.get("owl"));
		assertEquals("http://www.w3.org/2000/01/rdf-schema#", nspaces
				.get("rdfs"));
		assertEquals("http://www.w3.org/2001/XMLSchema#", nspaces.get("xsd"));
		// check that those are only the default ones
		assertFalse(nspaces.containsKey("src"));
		// this should now be a string of the length 386
		assertEquals(436, sc.getSparqlNamespaceShorts().length());
		// check if a namespace is correctly reduced
		assertEquals("swrc:", sc
				.reduceNamespace("http://swrc.ontoware.org/ontology#"));
	}

	@Test(expected = CloneNotSupportedException.class)
	public void testClone() throws CloneNotSupportedException {
		sc.clone();
	}

	@Test
	public void testGetUpper() {
		assertNotNull(sc.getUpper());
	}

	@Test
	public void testAddStatements() {
		KnowWEArticle article1 = new KnowWEArticle("", "Tag1", type,
				"default_web");
		am.saveUpdatedArticle(article1);
		TaggingMangler tm = TaggingMangler.getInstance();
		tm.addTag("Tag1", "tag", params);
		am.saveUpdatedArticle(article1);
	}

}
