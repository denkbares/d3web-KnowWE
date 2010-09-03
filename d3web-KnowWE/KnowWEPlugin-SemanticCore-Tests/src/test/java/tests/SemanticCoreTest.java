/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import de.d3web.plugin.test.InitPluginManager;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.semantic.ISemanticCore;
import de.d3web.we.core.semantic.SemanticCoreDelegator;
import de.d3web.we.core.semantic.tagging.TaggingMangler;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.RootType;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotation;
import dummies.KnowWETestWikiConnector;

public class SemanticCoreTest {

	private KnowWEEnvironment ke;
	private KnowWEArticleManager am;
	private KnowWEObjectType type;
	private KnowWEParameterMap params;
	private ISemanticCore sc;

	@Before
	public void setUp() throws Exception {
		InitPluginManager.init();

		RootType.getInstance().addChildType(new SemanticAnnotation());

		KnowWEEnvironment.initKnowWE(new KnowWETestWikiConnector());
		ke = KnowWEEnvironment.getInstance();
		type = ke.getRootType();
		am = ke.getArticleManager(KnowWEEnvironment.DEFAULT_WEB);

		params = new KnowWEParameterMap("", "");
		sc = SemanticCoreDelegator.getInstance();
	}

	@Test
	public void testStatementRemoval() {
		String hades = "[ hades <=> hades type:: god ]";
		String joe = "[ joe <=> joe type:: person ]";
		String testtopic = "TestPage";
		ke.processAndUpdateArticleJunit(null, hades, testtopic,
				KnowWEEnvironment.DEFAULT_WEB, type);

		String hadesquery = "ask { lns:hades rdf:type lns:god }";
		String joequery = "ask { lns:joe rdf:type lns:person }";

		// check that the article was parsed and the statements are in the
		// core
		assertTrue(SemanticCoreDelegator.getInstance().booleanQuery(hadesquery));
		// shouldn't know anything about joe
		assertFalse(SemanticCoreDelegator.getInstance().booleanQuery(joequery));
		// now change the article to just contain joe and no hades and make
		// sure
		// this is updated in the core accordingly
		ke.processAndUpdateArticleJunit(null, joe, testtopic,
				KnowWEEnvironment.DEFAULT_WEB, type);
		assertFalse(SemanticCoreDelegator.getInstance().booleanQuery(hadesquery));
		assertTrue(SemanticCoreDelegator.getInstance().booleanQuery(joequery));

		// now add hades and joe
		ke.processAndUpdateArticleJunit(null, hades + "\n " + joe, testtopic,
				KnowWEEnvironment.DEFAULT_WEB, type);
		assertTrue(SemanticCoreDelegator.getInstance().booleanQuery(hadesquery));
		assertTrue(SemanticCoreDelegator.getInstance().booleanQuery(joequery));

		// kill hades ;)
		ke.processAndUpdateArticleJunit(null, joe, testtopic,
				KnowWEEnvironment.DEFAULT_WEB, type);
		assertFalse(SemanticCoreDelegator.getInstance().booleanQuery(hadesquery));
		assertTrue(SemanticCoreDelegator.getInstance().booleanQuery(joequery));

		// kill the article and make sure all statements are gone
		am.deleteArticle(am.getArticle(testtopic));
		assertFalse(SemanticCoreDelegator.getInstance().booleanQuery(hadesquery));
		assertFalse(SemanticCoreDelegator.getInstance().booleanQuery(joequery));

	}

	@Test
	public void testNamespaceStuff() {
		// check if we have sc
		assertNotNull(sc);
		assertNotNull(SemanticCoreDelegator.getInstance());
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
		KnowWEArticle article1 = KnowWEArticle.createArticle("", "Tag1", type,
				"default_web");
		am.saveUpdatedArticle(article1);
		TaggingMangler tm = TaggingMangler.getInstance();
		tm.addTag("Tag1", "tag", params);
		am.saveUpdatedArticle(article1);
		am.deleteArticle(am.getArticle("Tag1"));
	}

	@Test
	public void testInferencing() {
		String hades = "[ hades type:: god ]";
		hades += " [ god subClassOf:: entity ]";
		String testtopic = "TestPage";
		ke.processAndUpdateArticleJunit(null, hades, testtopic,
				KnowWEEnvironment.DEFAULT_WEB, type);

		String hadesquery = "ask { lns:hades rdf:type lns:god }";
		String hadesentityquery = "ask { lns:hades rdf:type lns:entity }";

		// check that the article was parsed and the statements are in the
		// core
		assertTrue(SemanticCoreDelegator.getInstance().booleanQuery(hadesquery));
		// check that inferencing works
		assertTrue(SemanticCoreDelegator.getInstance().booleanQuery(hadesentityquery));

		ArrayList<String> erg = SemanticCoreDelegator.getInstance().simpleQueryToList(
				"SELECT ?x WHERE { ?x rdf:type lns:entity .}", "x");
		// make sure that hades is returned on query for entities
		assertTrue(erg.contains("hades"));

	}
}
