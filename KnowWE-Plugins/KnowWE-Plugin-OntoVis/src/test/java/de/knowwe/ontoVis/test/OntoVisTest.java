/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.ontoVis.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.Node;

import com.denkbares.semanticcore.Reasoning;
import de.d3web.plugin.test.InitPluginManager;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.RuleSet;
import de.knowwe.rdf2go.modelfactory.OWLIMLiteModelFactory;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdfs.vis.OntoGraphDataBuilder;
import de.knowwe.rdfs.vis.util.Utils;
import de.knowwe.visualization.ConceptNode;
import de.knowwe.visualization.Config;
import de.knowwe.visualization.Edge;
import de.knowwe.visualization.GraphVisualizationRenderer;
import de.knowwe.visualization.SubGraphData;
import de.knowwe.visualization.dot.DOTVisualizationRenderer;

import static org.junit.Assert.assertEquals;

/**
 * @author Johanna Latt
 * @created 19.07.2014.
 */
public class OntoVisTest {

	static Rdf2GoCore rdfRepository = null;

	@BeforeClass
	public static void setUp() throws IOException {
		InitPluginManager.init();

		RDF2Go.register(new OWLIMLiteModelFactory(RuleSet.OWL2_RL_OPTIMIZED));
		Model model = RDF2Go.getModelFactory().createModel();
		model.open();
		rdfRepository = new Rdf2GoCore("http://localhost:8080/KnowWE/Wiki.jsp?page=",
				"http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#", model, Reasoning.OWL2_RL_OPTIMIZED);
		// rdfRepository.addNamespace("ns", bns);
		// rdfRepository.addNamespace(LNS_ABBREVIATION, lns);
		rdfRepository.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		rdfRepository.addNamespace("owl", "http://www.w3.org/2002/07/owl#");
		rdfRepository.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		rdfRepository.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
		rdfRepository.addNamespace("si", "http://www.example.org/ontology#");

		File ontologyFile = new File("src/test/resources/simpsons-ontology.xml");
		rdfRepository.readFrom(ontologyFile);
	}

	@Test
	public void testInstances() {
		Config config = new Config();
		config.setConcept("si:bart");
		config.setFormat("svg");
		config.addFilterRelations("si:sibling", "si:child");
		config.setSize("750");
		config.setRankDir(Config.RankDir.TB);
		config.setShowOutgoingEdges(true);
		config.setShowLabels(true);
		config.setCacheFileID("instances");
		config.setCacheDirectoryPath("target");

		String colorCodes = "";
		colorCodes += "si:sibling: #511F7A;";
		colorCodes += "si:child: #398743;";

		config.setRelationColors(colorCodes);

		OntoGraphDataBuilder ontoGraphDataBuilder = new OntoGraphDataBuilder(null,
				config,
				new DummyLinkToTermDefinitionProvider(), rdfRepository);

		ontoGraphDataBuilder.createData();

		String generatedSource = ontoGraphDataBuilder.getSource().trim();
		String expectedSource = null;
		try {
			expectedSource = Strings.readFile(new File("src/test/resources/graph-Bart.dot")).trim();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// the expressions do not have constant order within the dot-code
		// therefore we need some fuzzy-compare

		assertEquals(
				"Length of generated dot-source does not match length of expected dot-source.",
				String.valueOf(expectedSource).length(),
				String.valueOf(generatedSource).length());
		List<Byte> expectedBytes = asSortedByteList(expectedSource);
		List<Byte> generatedBytes = asSortedByteList(generatedSource);

		assertEquals(
				"Generated dot-source does not match (sorted-bytes) expected dot-source.",
				expectedBytes, generatedBytes);
	}

	@Test
	public void testClasses() {
		Config config = new Config();
		config.setConcept("si:Human");
		config.setFormat("png");
		config.addExcludeNodes("owl:Nothing");
		config.addExcludeRelations("rdf:first", "owl:equivalentClass", "rdf:type", "owl:assertionProperty", "owl:sameAs");
		config.setShowOutgoingEdges(false);
		config.setShowLabels(true);
		config.setLanguage("en");
		config.setCacheFileID("classes");
		String colorCodes = "";
		colorCodes += "rdfs:subClassOf: #19F193;";
		config.setRelationColors(colorCodes);
		config.setPredecessors(3);
		config.setCacheDirectoryPath("target");

		OntoGraphDataBuilder ontoGraphDataBuilder = new OntoGraphDataBuilder(null,
				config, new DummyLinkToTermDefinitionProvider(), rdfRepository);

		ontoGraphDataBuilder.createData();

		String generatedSource = ontoGraphDataBuilder.getSource().trim();
		String expectedSource = null;
		try {
			expectedSource = Strings.readFile(new File("src/test/resources/graph-Human.dot")).trim();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// the expressions do not have constant order within the dot-code
		// therefore we need some fuzzy-compare

		assertEquals(
				"Length of generated dot-source does not match length of expected dot-source.",
				String.valueOf(expectedSource).length(),
				String.valueOf(generatedSource).length());
		List<Byte> expectedBytes = asSortedByteList(expectedSource);
		List<Byte> generatedBytes = asSortedByteList(generatedSource);

		assertEquals(
				"Generated dot-source does not match (sorted-bytes) expected dot-source.",
				expectedBytes, generatedBytes);
	}

	@Test
	public void testProperties() {
		Config config = new Config();
		config.addConcept("si:child");
		config.setFormat("svg");
		config.addExcludeRelations("rdf:type");
		config.setShowOutgoingEdges(false);
		config.setShowLabels(true);
		config.setCacheFileID("properties");

		String colorCodes = "";
		colorCodes += "si:child: #398743;";
		colorCodes += "si:parent si:color #123A56;";
		colorCodes += "si:relatedWith si:color #987F65;";

		config.setRelationColors(colorCodes);
		config.setPredecessors(3);
		config.setCacheDirectoryPath("target");

		OntoGraphDataBuilder ontoGraphDataBuilder = new OntoGraphDataBuilder(null,
				config,
				new DummyLinkToTermDefinitionProvider(), rdfRepository);

		ontoGraphDataBuilder.createData();

		String generatedSource = ontoGraphDataBuilder.getSource().trim();
		String expectedSource = null;
		try {
			expectedSource = Strings.readFile(new File("src/test/resources/graph-Child.dot")).trim();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// the expressions do not have constant order within the dot-code
		// therefore we need some fuzzy-compare

		assertEquals(
				"Length of generated dot-source does not match length of expected dot-source.",
				String.valueOf(expectedSource).length(),
				String.valueOf(generatedSource).length());
		List<Byte> expectedBytes = asSortedByteList(expectedSource);
		List<Byte> generatedBytes = asSortedByteList(generatedSource);

		assertEquals(
				"Generated dot-source does not match (sorted-bytes) expected dot-source.",
				expectedBytes, generatedBytes);
	}

	@Test
	public void testTable() {
		Config config = new Config();
		config.setConcept("si:lisa");
		config.addFilterRelations("si:age", "rdfs:label", "si:child");
		config.setShowOutgoingEdges(false);
		config.setShowLabels(false);
		config.setCacheFileID("table");
		config.setCacheDirectoryPath("target");

		OntoGraphDataBuilder ontoGraphDataBuilder = new OntoGraphDataBuilder(null,
				config,
				new DummyLinkToTermDefinitionProvider(), rdfRepository);

		ontoGraphDataBuilder.createData();

		String generatedSource = ontoGraphDataBuilder.getSource().trim();
		String expectedSource = null;
		try {
			expectedSource = Strings.readFile(new File("src/test/resources/graph-Table.dot")).trim();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// the expressions do not have constant order within the dot-code
		// therefore we need some fuzzy-compare

		assertEquals(
				"Length of generated dot-source does not match length of expected dot-source.",
				String.valueOf(expectedSource).length(),
				String.valueOf(generatedSource).length());
		List<Byte> expectedBytes = asSortedByteList(expectedSource);
		List<Byte> generatedBytes = asSortedByteList(generatedSource);

		assertEquals(
				"Generated dot-source does not match (sorted-bytes) expected dot-source.",
				expectedBytes, generatedBytes);
	}

	@Test
	public void testTwoConcepts() {
		Config config = new Config();
		config.setConcept("si:abraham", "si:maggie");
		config.addFilterRelations("si:child");
		config.setShowInverse(false);
		config.addFilterRelations("si:child");
		config.setShowOutgoingEdges(true);
		config.setShowLabels(true);
		config.setSuccessors(0);
		config.setCacheFileID("twoconcepts");
		config.setCacheDirectoryPath("target");

		OntoGraphDataBuilder ontoGraphDataBuilder = new OntoGraphDataBuilder(null,
				config, new DummyLinkToTermDefinitionProvider(), rdfRepository);

		ontoGraphDataBuilder.createData();

		String generatedSource = ontoGraphDataBuilder.getSource().trim();
		String expectedSource = null;
		try {
			expectedSource = Strings.readFile(new File("src/test/resources/graph-TwoConcepts.dot")).trim();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// the expressions do not have constant order within the dot-code
		// therefore we need some fuzzy-compare

		assertEquals(
				"Length of generated dot-source does not match length of expected dot-source.",
				String.valueOf(expectedSource).length(),
				String.valueOf(generatedSource).length());
		List<Byte> expectedBytes = asSortedByteList(expectedSource);
		List<Byte> generatedBytes = asSortedByteList(generatedSource);

		assertEquals(
				"Generated dot-source does not match (sorted-bytes) expected dot-source.",
				expectedBytes, generatedBytes);
	}

	@Test
	public void testSparql() {
		Config config = new Config();
		config.setCacheDirectoryPath("target");
		config.setCacheFileID("sparql");
		config.setShowLabels(false);

		String sparql = "SELECT ?x ?y ?z\nWHERE {\n?x ?y ?z . ?x rdf:type si:Human .\n}";

		LinkToTermDefinitionProvider uriProvider = new DummyLinkToTermDefinitionProvider();

		String sparqlString = Rdf2GoUtils.createSparqlString(rdfRepository, sparql);
		QueryResultTable resultSet = rdfRepository.sparqlSelect(sparqlString);

		SubGraphData data = new SubGraphData();
		List<String> variables = resultSet.getVariables();

		for (QueryRow row : resultSet) {

			Node fromURI = row.getValue(variables.get(0));

			Node relationURI = row.getValue(variables.get(1));

			Node toURI = row.getValue(variables.get(2));

			if (fromURI == null || toURI == null || relationURI == null) {
				Log.warning("incomplete query result row: " + row.toString());
				continue;
			}

			ConceptNode fromNode = Utils.createNode(config, rdfRepository, uriProvider,
					null, data, fromURI, true);
			String relation = Utils.getConceptName(relationURI, rdfRepository);

			ConceptNode toNode = Utils.createNode(config, rdfRepository, uriProvider, null,
					data, toURI, true);

			String relationLabel = Utils.createRelationLabel(config, rdfRepository, relationURI,
					relation);

			Edge newLineRelationsKey = new Edge(fromNode, relationLabel, toNode);

			data.addEdge(newLineRelationsKey);

		}

		String conceptName = data.getConceptDeclarations().iterator().next().getName();
		config.setConcept(conceptName);

		GraphVisualizationRenderer graphRenderer = new DOTVisualizationRenderer(data,
				config);
		graphRenderer.generateSource();

		String generatedSource = graphRenderer.getSource().trim();
		String expectedSource = null;
		try {
			expectedSource = Strings.readFile(new File("src/test/resources/graph-Sparql.dot")).trim();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// the expressions do not have constant order within the dot-code
		// therefore we need some fuzzy-compare

		assertEquals(
				"Length of generated dot-source does not match length of expected dot-source.",
				String.valueOf(expectedSource).length(),
				String.valueOf(generatedSource).length());
		List<Byte> expectedBytes = asSortedByteList(expectedSource);
		List<Byte> generatedBytes = asSortedByteList(generatedSource);

		assertEquals(
				"Generated dot-source does not match (sorted-bytes) expected dot-source.",
				expectedBytes, generatedBytes);
	}

	@Test
	public void testInverse() {
		Config config = new Config();
		config.setConcept("si:marge");
		config.addExcludeRelations("rdf:type", "onto:_checkChain3", "owl:sameAs", "si:father", "si:mother", "si:gender", "si:livesIn");
		config.addExcludeNodes("owl:Thing", "owl:Nothing");
		config.setShowOutgoingEdges(false);
		config.setShowLabels(true);
		config.setShowInverse(false);
		config.setCacheFileID("testInverse");
		config.setCacheDirectoryPath("target");

		OntoGraphDataBuilder ontoGraphDataBuilder = new OntoGraphDataBuilder(null,
				config,
				new DummyLinkToTermDefinitionProvider(), rdfRepository);

		ontoGraphDataBuilder.createData();

		String generatedSource = ontoGraphDataBuilder.getSource().trim();
		String expectedSource = null;
		try {
			expectedSource = Strings.readFile(new File("src/test/resources/graph-Marge.dot")).trim();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// the expressions do not have constant order within the dot-code
		// therefore we need some fuzzy-compare

		assertEquals(
				"Length of generated dot-source does not match length of expected dot-source.",
				String.valueOf(expectedSource).length(),
				String.valueOf(generatedSource).length());
		List<Byte> expectedBytes = asSortedByteList(expectedSource);
		List<Byte> generatedBytes = asSortedByteList(generatedSource);

		assertEquals(
				"Generated dot-source does not match (sorted-bytes) expected dot-source.",
				expectedBytes, generatedBytes);
	}

	private List<Byte> asSortedByteList(String expectedSource) {
		byte[] bytes = expectedSource.getBytes();
		Byte[] Bytes = new Byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			Bytes[i] = bytes[i];
		}
		List<Byte> list = Arrays.asList(Bytes);
		Collections.sort(list);
		return list;
	}

}
