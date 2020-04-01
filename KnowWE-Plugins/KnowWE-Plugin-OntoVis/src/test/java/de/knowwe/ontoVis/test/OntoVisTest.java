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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import connector.DummyConnector;
import org.junit.BeforeClass;
import org.junit.Test;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFParseException;

import com.denkbares.collections.DefaultMultiMap;
import com.denkbares.collections.MultiMap;
import com.denkbares.plugin.test.InitPluginManager;
import com.denkbares.semanticcore.CachedTupleQueryResult;
import com.denkbares.semanticcore.config.RepositoryConfigs;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.rdf2go.Rdf2GoCore;
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
	static Rdf2GoCore rdfRepository2 = null;

	@BeforeClass
	public static void setUp() throws IOException, RDFParseException, RepositoryException {
		InitPluginManager.init();
		Environment.initInstance(new DummyConnector());
		rdfRepository = new Rdf2GoCore("http://localhost:8080/KnowWE/Wiki.jsp?page=", RepositoryConfigs.find("OWL2_RL_OPTIMIZED"));
		rdfRepository.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		rdfRepository.addNamespace("owl", "http://www.w3.org/2002/07/owl#");
		rdfRepository.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		rdfRepository.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
		rdfRepository.addNamespace("si", "http://www.example.org/ontology#");

		rdfRepository2 = new Rdf2GoCore("http://localhost:8080/KnowWE/Wiki.jsp?page=", RepositoryConfigs.find("OWL2_RL_OPTIMIZED"));
		rdfRepository2.addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		rdfRepository2.addNamespace("owl", "http://www.w3.org/2002/07/owl#");
		rdfRepository2.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		rdfRepository2.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
		rdfRepository2.addNamespace("sis", "http://www.example.org/newOntology#");

		File ontologyFile = new File("src/test/resources/simpsons-ontology.xml");
		File ontologyFile2 = new File("src/test/resources/simpsonsSmall-ontology.xml");

		rdfRepository.readFrom(ontologyFile);
		rdfRepository2.readFrom(ontologyFile2);

	}

	@Test
	public void testInstances() throws IOException {
		Config config = new Config();
		config.setConcept("si:bart");
		config.setFormat("svg");
		config.addFilterRelations("si:sibling", "si:child");
		config.setSize("750");
		config.setRankDir(Config.RankDir.TB);
		config.setShowOutgoingEdges(true);
		config.setShowLabels("true");
		config.setLiteralMode(Config.LiteralMode.OFF);
		config.setCacheFileID("instances");
		config.setCacheDirectoryPath("target");

		Map<String, String> colorMap = new HashMap<>();
		colorMap.put("si:sibling", "#511F7A");
		colorMap.put("si:child", "#398743");
		config.setRelationColors(colorMap);

		generateAndCompare(config, "src/test/resources/graph-Bart.dot");
	}

	@Test
	public void testClasses() throws IOException {
		Config config = new Config();
		config.setConcept("si:Human");
		config.setFormat("png");
		config.addExcludeNodes("owl:Nothing", "owl:Thing");
		config.addExcludeRelations("rdf:first", "owl:equivalentClass", "rdf:type", "owl:assertionProperty", "owl:sameAs");
		config.setShowOutgoingEdges(false);
		config.setShowLabels("true");
		config.setLiteralMode(Config.LiteralMode.OFF);
		config.setLanguages(Locale.forLanguageTag("en"));
		config.setCacheFileID("classes");

		Map<String, String> colorMap = new HashMap<>();
		colorMap.put("rdfs:subClassOf", "#19F193");
		config.setRelationColors(colorMap);
		config.setPredecessors(3);
		config.setCacheDirectoryPath("target");

		generateAndCompare(config, "src/test/resources/graph-Human.dot");
	}

	@Test
	public void testProperties() throws IOException {
		Config config = new Config();
		config.addConcept("si:child");
		config.setFormat("svg");
		config.addExcludeRelations("rdf:type");
		config.setShowOutgoingEdges(false);
		config.setShowLabels("true");
		config.setCacheFileID("properties");

		Map<String, String> colorMap = new HashMap<>();
		colorMap.put("si:child", "#398743");
		colorMap.put("si:parent", "#123A56");
		colorMap.put("si:relatedWith", "#987F65");
		config.setRelationColors(colorMap);
		config.setPredecessors(3);
		config.setCacheDirectoryPath("target");

		generateAndCompare(config, "src/test/resources/graph-Child.dot");
	}

	@Test
	public void testTable() throws IOException {
		Config config = new Config();
		config.setConcept("si:lisa");
		config.addFilterRelations("si:age", "rdfs:label", "si:child");
		config.setShowOutgoingEdges(false);
		config.setShowLabels("false");
		config.setCacheFileID("table");
		config.setCacheDirectoryPath("target");

		generateAndCompare(config, "src/test/resources/graph-Table.dot");
	}

	@Test
	public void testTwoConcepts() throws IOException {
		Config config = new Config();
		config.setConcept("si:abraham", "si:maggie");
		config.addFilterRelations("si:child");
		//Old implementation
		//config.setShowInverse(false);
		config.setShowInverse(false);
		config.addFilterRelations("si:child");
		config.setShowOutgoingEdges(true);
		config.setShowLabels("true");
		config.setLiteralMode(Config.LiteralMode.OFF);
		config.setSuccessors(0);
		config.setCacheFileID("twoconcepts");
		config.setCacheDirectoryPath("target");

		generateAndCompare(config, "src/test/resources/graph-TwoConcepts.dot");
	}

	private void generateAndCompare(Config config, String pathname) throws IOException {
		OntoGraphDataBuilder ontoGraphDataBuilder = new OntoGraphDataBuilder(null,
				config, new DummyLinkToTermDefinitionProvider(), rdfRepository);

		ontoGraphDataBuilder.createData(Long.MAX_VALUE);

		String generatedSource = ontoGraphDataBuilder.getSource();
		String expectedSource = Strings.readFile(new File(pathname));
		compare(generatedSource, expectedSource);

	}

	private void compare(String generatedSource, String expectedSource) {
		// the expressions do not have constant order within the dot-code
		// therefore we need some fuzzy-compare

		generatedSource = generatedSource.trim().replace("\r", "");
		expectedSource = expectedSource.trim().replace("\r", "");
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
	public void testSparql() throws IOException {
		Config config = new Config();
		config.setCacheDirectoryPath("target");
		config.setCacheFileID("sparql");
		config.setShowLabels("false");

		String sparql = "SELECT ?x ?y ?z\nWHERE {\n?x ?y ?z . ?x rdf:type si:Human .\n}";

		LinkToTermDefinitionProvider uriProvider = new DummyLinkToTermDefinitionProvider();

		String sparqlString = Rdf2GoUtils.createSparqlString(rdfRepository, sparql);
		CachedTupleQueryResult resultSet = rdfRepository.sparqlSelect(sparqlString);

		SubGraphData data = new SubGraphData();
		List<String> variables = resultSet.getBindingNames();

		for (BindingSet row : resultSet) {

			Value fromURI = row.getValue(variables.get(0));

			Value relationURI = row.getValue(variables.get(1));

			Value toURI = row.getValue(variables.get(2));

			if (fromURI == null || toURI == null || relationURI == null) {
				Log.warning("incomplete query result row: " + row);
				continue;
			}

			ConceptNode fromNode = Utils.createValue(config, rdfRepository, uriProvider,
					null, data, fromURI, true);
			String relation = Utils.getConceptName(relationURI, rdfRepository);

			ConceptNode toNode = Utils.createValue(config, rdfRepository, uriProvider, null,
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

		String generatedSource = graphRenderer.getSource();
		String expectedSource = Strings.readFile(new File("src/test/resources/graph-Sparql.dot"));

		// the expressions do not have constant order within the dot-code
		// therefore we need some fuzzy-compare

		compare(generatedSource, expectedSource);
	}

	@SuppressWarnings("Duplicates") // Was reused in the newer headless Test implementation aswell
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

	@SuppressWarnings("Duplicates") // Took testSparql as Template
	@Test
	public void testSubProperties() throws IOException {
		Config config = new Config();
		config.setCacheDirectoryPath("target");
		config.setCacheFileID("subProperties");
		config.setShowLabels("false");

		MultiMap<String, String> subPropertiesMap = new DefaultMultiMap<>();

		// Get all  SubProperties and add all non-recursive to a ArrayList
		String subPropertyQuery = "SELECT ?Property ?SubProperty WHERE {\n" +
				"\t  ?SubProperty rdfs:subPropertyOf ?Property\n" +
				"  }\n";
		CachedTupleQueryResult propertyRelations = rdfRepository2.sparqlSelect(subPropertyQuery);
		for (BindingSet propertyRelation : propertyRelations) {
			String subProperty = propertyRelation.getValue("SubProperty").stringValue();
			String property = propertyRelation.getValue("Property").stringValue();

			// if SubProperty is not same as Property
			if (!property.equals(subProperty)) {
				subPropertiesMap.put(property, subProperty);
			}
		}

		String sparql = "SELECT ?xLabel ?y ?zLabel WHERE {\n?x ?y ?z .\n?x rdf:type sis:Human.\n?z rdf:type sis:Human.\n?x rdfs:label ?xLabel.\n?z rdfs:label ?zLabel.\n}";

		LinkToTermDefinitionProvider uriProvider = new DummyLinkToTermDefinitionProvider();

		String sparqlString = Rdf2GoUtils.createSparqlString(rdfRepository2, sparql);
		CachedTupleQueryResult resultSet = rdfRepository2.sparqlSelect(sparqlString);

		//SubGraphData data = new SubGraphData();
		SubGraphData data = new SubGraphData(subPropertiesMap);
		List<String> variables = resultSet.getBindingNames();

		for (BindingSet row : resultSet) {

			Value fromURI = row.getValue(variables.get(0));

			Value relationURI = row.getValue(variables.get(1));

			Value toURI = row.getValue(variables.get(2));

			if (fromURI == null || toURI == null || relationURI == null) {
				Log.warning("incomplete query result row: " + row);
				continue;
			}

			ConceptNode fromNode = Utils.createValue(config, rdfRepository2, uriProvider,
					null, data, fromURI, true);
			String relation = Utils.getConceptName(relationURI, rdfRepository2);

			ConceptNode toNode = Utils.createValue(config, rdfRepository2, uriProvider, null,
					data, toURI, true);

			String relationLabel = Utils.createRelationLabel(config, rdfRepository2, relationURI,
					relation);

			//Edge newLineRelationsKey = new Edge(fromNode, relationLabel, toNode);
			Edge newLineRelationsKey = new Edge(fromNode, relationLabel, relationURI.stringValue(), toNode);

			data.addEdge(newLineRelationsKey);
		}

		String conceptName = data.getConceptDeclarations().iterator().next().getName();
		config.setConcept(conceptName);

		GraphVisualizationRenderer graphRenderer = new DOTVisualizationRenderer(data,
				config);
		graphRenderer.generateSource();

		String generatedSource = graphRenderer.getSource();
		String expectedSource = Strings.readFile(new File("src/test/resources/graph-SubProperties.dot"));

		// the expressions do not have constant order within the dot-code
		// therefore we need some fuzzy-compare

		compare(generatedSource, expectedSource);
	}
}
