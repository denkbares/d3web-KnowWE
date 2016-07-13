/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.ontology.kdom;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import com.denkbares.semanticcore.config.RepositoryConfigs;
import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.ontology.compile.OntologyType;
import de.knowwe.ontology.kdom.namespace.AbbreviationDefinition;
import de.knowwe.ontology.kdom.objectproperty.Property;
import de.knowwe.ontology.kdom.resource.Resource;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * Adds the all the terms of the statements that are in the repository by default or are added via import to the {@link
 * TerminologyManager}
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 23.04.2015
 */
public class InitTerminologyHandler extends OntologyHandler<PackageCompileType> {

	private final ExecutorService executorService = Executors.newFixedThreadPool(2);

	private static final String[] RESOURCE_TERMS = new String[] {
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#List",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#ObjectProperty",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil",
			"http://www.w3.org/2000/01/rdf-schema#Class",
			"http://www.w3.org/2000/01/rdf-schema#Container",
			"http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty",
			"http://www.w3.org/2000/01/rdf-schema#Datatype",
			"http://www.w3.org/2000/01/rdf-schema#Literal",
			"http://www.w3.org/2000/01/rdf-schema#Resource",
			"http://www.w3.org/2001/XMLSchema#NCName",
			"http://www.w3.org/2001/XMLSchema#NMTOKEN",
			"http://www.w3.org/2001/XMLSchema#Name",
			"http://www.w3.org/2001/XMLSchema#anyURI",
			"http://www.w3.org/2001/XMLSchema#base64Binary",
			"http://www.w3.org/2001/XMLSchema#boolean",
			"http://www.w3.org/2001/XMLSchema#byte",
			"http://www.w3.org/2001/XMLSchema#dateTime",
			"http://www.w3.org/2001/XMLSchema#dateTimeStamp",
			"http://www.w3.org/2001/XMLSchema#decimal",
			"http://www.w3.org/2001/XMLSchema#double",
			"http://www.w3.org/2001/XMLSchema#float",
			"http://www.w3.org/2001/XMLSchema#hexBinary",
			"http://www.w3.org/2001/XMLSchema#int",
			"http://www.w3.org/2001/XMLSchema#integer",
			"http://www.w3.org/2001/XMLSchema#language",
			"http://www.w3.org/2001/XMLSchema#long",
			"http://www.w3.org/2001/XMLSchema#negativeInteger",
			"http://www.w3.org/2001/XMLSchema#nonNegativeInteger",
			"http://www.w3.org/2001/XMLSchema#nonPositiveInteger",
			"http://www.w3.org/2001/XMLSchema#normalizedString",
			"http://www.w3.org/2001/XMLSchema#positiveInteger",
			"http://www.w3.org/2001/XMLSchema#short",
			"http://www.w3.org/2001/XMLSchema#string",
			"http://www.w3.org/2001/XMLSchema#token",
			"http://www.w3.org/2001/XMLSchema#unsignedByte",
			"http://www.w3.org/2001/XMLSchema#unsignedInt",
			"http://www.w3.org/2001/XMLSchema#unsignedLong",
			"http://www.w3.org/2001/XMLSchema#unsignedShort",
			"http://www.w3.org/2002/07/owl#AllDifferent",
			"http://www.w3.org/2002/07/owl#AllDisjointClasses",
			"http://www.w3.org/2002/07/owl#AllDisjointProperties",
			"http://www.w3.org/2002/07/owl#Annotation",
			"http://www.w3.org/2002/07/owl#AnnotationProperty",
			"http://www.w3.org/2002/07/owl#AsymmetricProperty",
			"http://www.w3.org/2002/07/owl#Axiom",
			"http://www.w3.org/2002/07/owl#Class",
			"http://www.w3.org/2002/07/owl#DataRange",
			"http://www.w3.org/2002/07/owl#DatatypeProperty",
			"http://www.w3.org/2002/07/owl#DeprecatedClass",
			"http://www.w3.org/2002/07/owl#DeprecatedProperty",
			"http://www.w3.org/2002/07/owl#FunctionalProperty",
			"http://www.w3.org/2002/07/owl#InverseFunctionalProperty",
			"http://www.w3.org/2002/07/owl#IrreflexiveProperty",
			"http://www.w3.org/2002/07/owl#NamedIndividual",
			"http://www.w3.org/2002/07/owl#NegativePropertyAssertion",
			"http://www.w3.org/2002/07/owl#Nothing",
			"http://www.w3.org/2002/07/owl#ObjectProperty",
			"http://www.w3.org/2002/07/owl#Ontology",
			"http://www.w3.org/2002/07/owl#OntologyProperty",
			"http://www.w3.org/2002/07/owl#ReflexiveProperty",
			"http://www.w3.org/2002/07/owl#Restriction",
			"http://www.w3.org/2002/07/owl#SymmetricProperty",
			"http://www.w3.org/2002/07/owl#Thing",
			"http://www.w3.org/2002/07/owl#TransitiveProperty",
			"http://www.w3.org/2002/07/owl#distinctmembers",
			"http://www.w3.org/2002/07/owl#rational",
			"http://www.w3.org/2002/07/owl#real"
	};

	private static final String[] PROPERTY_TERMS = new String[] {
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#_1",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#first",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#langRange",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#object",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#rest",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#subject",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#value",
			"http://www.w3.org/2000/01/rdf-schema#comment",
			"http://www.w3.org/2000/01/rdf-schema#domain",
			"http://www.w3.org/2000/01/rdf-schema#isDefinedBy",
			"http://www.w3.org/2000/01/rdf-schema#label",
			"http://www.w3.org/2000/01/rdf-schema#member",
			"http://www.w3.org/2000/01/rdf-schema#range",
			"http://www.w3.org/2000/01/rdf-schema#seeAlso",
			"http://www.w3.org/2000/01/rdf-schema#subClassOf",
			"http://www.w3.org/2000/01/rdf-schema#subPropertyOf",
			"http://www.w3.org/2001/XMLSchema#length",
			"http://www.w3.org/2001/XMLSchema#maxExclusive",
			"http://www.w3.org/2001/XMLSchema#maxInclusive",
			"http://www.w3.org/2001/XMLSchema#maxLength",
			"http://www.w3.org/2001/XMLSchema#minExclusive",
			"http://www.w3.org/2001/XMLSchema#minInclusive",
			"http://www.w3.org/2001/XMLSchema#minLength",
			"http://www.w3.org/2001/XMLSchema#pattern",
			"http://www.w3.org/2002/07/owl#allValuesFrom",
			"http://www.w3.org/2002/07/owl#annotatedProperty",
			"http://www.w3.org/2002/07/owl#annotatedSource",
			"http://www.w3.org/2002/07/owl#annotatedTarget",
			"http://www.w3.org/2002/07/owl#assertionProperty",
			"http://www.w3.org/2002/07/owl#backwardCompatibleWith",
			"http://www.w3.org/2002/07/owl#bottomDataProperty",
			"http://www.w3.org/2002/07/owl#bottomObjectProperty",
			"http://www.w3.org/2002/07/owl#cardinality",
			"http://www.w3.org/2002/07/owl#complementOf",
			"http://www.w3.org/2002/07/owl#datatypeComplementOf",
			"http://www.w3.org/2002/07/owl#deprecated",
			"http://www.w3.org/2002/07/owl#differentFrom",
			"http://www.w3.org/2002/07/owl#disjointUnionOf",
			"http://www.w3.org/2002/07/owl#disjointWith",
			"http://www.w3.org/2002/07/owl#distinctMembers",
			"http://www.w3.org/2002/07/owl#equivalentClass",
			"http://www.w3.org/2002/07/owl#equivalentProperty",
			"http://www.w3.org/2002/07/owl#hasKey",
			"http://www.w3.org/2002/07/owl#hasSelf",
			"http://www.w3.org/2002/07/owl#hasValue",
			"http://www.w3.org/2002/07/owl#imports",
			"http://www.w3.org/2002/07/owl#incompatibleWith",
			"http://www.w3.org/2002/07/owl#intersectionOf",
			"http://www.w3.org/2002/07/owl#inverseOf",
			"http://www.w3.org/2002/07/owl#maxCardinality",
			"http://www.w3.org/2002/07/owl#maxQualifiedCardinality",
			"http://www.w3.org/2002/07/owl#members",
			"http://www.w3.org/2002/07/owl#minCardinality",
			"http://www.w3.org/2002/07/owl#minQualifiedCardinality",
			"http://www.w3.org/2002/07/owl#onClass",
			"http://www.w3.org/2002/07/owl#onDataRange",
			"http://www.w3.org/2002/07/owl#onDatatype",
			"http://www.w3.org/2002/07/owl#onProperties",
			"http://www.w3.org/2002/07/owl#onProperty",
			"http://www.w3.org/2002/07/owl#oneOf",
			"http://www.w3.org/2002/07/owl#priorVersion",
			"http://www.w3.org/2002/07/owl#propertyChainAxiom",
			"http://www.w3.org/2002/07/owl#propertyDisjointWith",
			"http://www.w3.org/2002/07/owl#qualifiedCardinality",
			"http://www.w3.org/2002/07/owl#sameAs",
			"http://www.w3.org/2002/07/owl#someValuesFrom",
			"http://www.w3.org/2002/07/owl#sourceIndividual",
			"http://www.w3.org/2002/07/owl#targetIndividual",
			"http://www.w3.org/2002/07/owl#targetValue",
			"http://www.w3.org/2002/07/owl#topDataProperty",
			"http://www.w3.org/2002/07/owl#topObjectProperty",
			"http://www.w3.org/2002/07/owl#unionOf",
			"http://www.w3.org/2002/07/owl#versionIRI",
			"http://www.w3.org/2002/07/owl#versionInfo",
			"http://www.w3.org/2002/07/owl#withRestrictions"
	};

	@Override
	public Collection<Message> create(OntologyCompiler compiler, Section<PackageCompileType> section) {
		registerBaseTerminology(compiler, section);

		Section<OntologyType> ontologyMarkup = Sections.ancestor(section, OntologyType.class);
		handleImports(compiler, DefaultMarkupType.getAnnotationContentSections(ontologyMarkup, OntologyType.ANNOTATION_IMPORT), false);
		handleImports(compiler, DefaultMarkupType.getAnnotationContentSections(ontologyMarkup, OntologyType.ANNOTATION_SILENT_IMPORT), true);

		return Messages.noMessage();
	}

	private void registerBaseTerminology(OntologyCompiler compiler, Section<PackageCompileType> section) {
		compiler.getRdf2GoCore().addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		compiler.getRdf2GoCore().addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		compiler.getRdf2GoCore().addNamespace("xsd", "http://www.w3.org/2001/XMLSchema#");
		compiler.getRdf2GoCore().addNamespace("owl", "http://www.w3.org/2002/07/owl#");
		for (String resourceTerm : RESOURCE_TERMS) {
			registerTerm(compiler, compiler.getRdf2GoCore(), section, resourceTerm, Resource.class);
		}

		for (String resourceTerm : PROPERTY_TERMS) {
			registerTerm(compiler, compiler.getRdf2GoCore(), section, resourceTerm, Property.class);
		}
	}

	private void handleImports(OntologyCompiler compiler, List<Section<? extends AnnotationContentType>> annotationContentSections, boolean silent) {
		for (Section<? extends AnnotationContentType> annotationContentSection : annotationContentSections) {
			String importString = Strings.trimQuotes(annotationContentSection.getText());
			URL url = null;
			try {
				url = new URL(importString);
			}
			catch (Exception ignore) {
				// we will know when url == null...
			}
			if (url == null) {
				importAttachment(compiler, annotationContentSection, importString, silent);
			}
			else {
				String attachmentName = cacheOntology(compiler, annotationContentSection, url, importString);
				importAttachment(compiler, annotationContentSection, attachmentName, silent);
			}
		}
	}

	private String cacheOntology(OntologyCompiler compiler, Section<? extends AnnotationContentType> section, URL url, String importString) {
		String attachmentName = importString.replaceAll("^https?://", "").replaceAll("\\W", "-").replaceAll("-$", "");

		WikiAttachment attachment = null;
		try {
			Collection<WikiAttachment> attachments = KnowWEUtils.getAttachments(section.getTitle(), attachmentName + ".*");
			if (!attachments.isEmpty()) {
				attachment = attachments.iterator().next();
				attachmentName = attachment.getFileName();
			}
		}
		catch (IOException e) {
			Messages.storeMessage(compiler, section, this.getClass(), Messages.error("Error while retrieving attachment '"
					+ importString + "': " + e.getMessage()));
			return null;
		}
		if (attachment == null) {
			WikiConnector wc = Environment.getInstance().getWikiConnector();
			try {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				String extension;
				// first try to use the Content-Location field, i.e. support content negotiation
				connection.setRequestMethod("HEAD");
				connection.connect();
				List<String> locations = connection.getHeaderFields().get("Content-Location");
				if (locations != null && !locations.isEmpty()) {
					String location = locations.iterator().next();
					extension = getExtension(location);
				}
				// if content-location is not defined, we probably access a file directly...
				else {
					String fileName = new File(importString).getName();
					extension = getExtension(fileName);
				}
				attachmentName = attachmentName + extension;
				connection.disconnect();
				wc.storeAttachment(section.getTitle(), attachmentName, "SYSTEM", url.openStream());
			}
			catch (Exception e) {
				Messages.storeMessage(compiler, section, this.getClass(), Messages.error("Unable to cache ontology file from '"
						+ importString + "': " + e.getMessage()));
				return null;
			}
		}
		return attachmentName;
	}

	@NotNull
	private String getExtension(String fileName) {
		return fileName.substring(Math.max(0, fileName.lastIndexOf(".")));
	}

	private void importAttachment(OntologyCompiler compiler, Section<? extends AnnotationContentType> section, String attachmentFile, boolean silent) {
		long start = System.currentTimeMillis();
		Section<AttachmentType> importSection = Sections.successor(section, AttachmentType.class);
		String path = createPath(section, attachmentFile);
		WikiAttachment attachment;
		try {
			attachment = Environment.getInstance().getWikiConnector().getAttachment(path);
		}
		catch (IOException e) {
			Messages.storeMessage(compiler, section, this.getClass(), Messages.error("Error while retrieving attachment '"
					+ attachmentFile + "': " + e.getMessage()));
			return;
		}
		Rdf2GoCore core = compiler.getRdf2GoCore();
		if (core == null) {
			Messages.storeMessage(compiler, section, this.getClass(), Messages.error("No ontology repository found '"
					+ section.getText() + "'"));
			return;
		}
		if (attachment == null) {
			Messages.storeMessage(compiler, section, this.getClass(), Messages.error("Attachment '"
					+ section.getText().trim() + "' not found"));
			return;
		}

		String fileName = attachment.getFileName();
		RDFFormat syntax = Rdf2GoUtils.syntaxForFileName(fileName);
		Future<?> mainReadFuture = executorService.submit(() -> readFrom(compiler, section, core, attachment, syntax));
		if (!silent) {
			// we need rdfs reasoning for the SPARQLs to work
			Rdf2GoCore dummy = new Rdf2GoCore(RepositoryConfigs.find("RDFS"));
			readFrom(compiler, section, dummy, attachment, syntax);
			// register the terminology imported in the empty dummy repository
			registerTerminology(compiler, dummy, importSection);
			dummy.destroy();
		}
		try {
			mainReadFuture.get();
		}
		catch (InterruptedException | ExecutionException e) {
			handleException(compiler, section, attachment, e);
		}
		long duration = System.currentTimeMillis() - start;
		if (duration > TimeUnit.SECONDS.toMillis(1)) {
			Log.info("Loaded ontology from attachment " + path + " in " + duration + "ms");
		}
	}

	private void readFrom(OntologyCompiler compiler, Section<? extends AnnotationContentType> section, Rdf2GoCore core, WikiAttachment attachment, RDFFormat syntax) {
		try {
			core.readFrom(attachment.getInputStream(), syntax);
		}
		catch (IOException | RepositoryException | RDFParseException e) {
			handleException(compiler, section, attachment, e);
		}
	}

	private void handleException(OntologyCompiler compiler, Section<? extends AnnotationContentType> section, WikiAttachment attachment, Exception e) {
		Log.severe("Exception while importing ontology " + attachment.getPath(), e);
		Messages.storeMessage(compiler, section, this.getClass(), Messages.error("Error while importing ontology from '"
				+ attachment.getPath() + "': " + e.getMessage()));
	}

	private String createPath(Section<?> section, String attachment) {
		String fileName = attachment.trim();
		if (!fileName.contains("/")) {
			return section.getTitle() + "/" + fileName;
		}
		return fileName;
	}

	private void registerTerminology(OntologyCompiler compiler, Rdf2GoCore rdf2GoCore, Section<?> section) {
		String query = "SELECT ?resource \n" +
				"WHERE {\n" +
				"\t{ ?resource rdf:type rdfs:Resource } \n" +
				"\t\tUNION {\t?resource rdf:type rdfs:Class }\n" +
				"\t\tUNION { ?resource rdf:type owl:Class }\n" +
				"\tMINUS {\t?resource rdf:type rdf:Property } .\n" +
				"\tFILTER (!isBlank(?resource)) .\n" +
				"}";
		registerQueryResult(compiler, rdf2GoCore, section, query, Resource.class);

		query = "SELECT ?resource  WHERE {\n" +
				"\t{ ?resource rdf:type rdf:Property } \n" +
				"\t\tUNION {\t?resource rdf:type owl:ObjectProperty }\n" +
				"\t\tUNION {\t?resource rdf:type rdfs:subPropertyOf } .\n" +
				"\tFILTER (!isBlank(?resource)) .\n" +
				"}";
		query = Rdf2GoUtils.createSparqlString(rdf2GoCore, query);
		registerQueryResult(compiler, rdf2GoCore, section, query, Property.class);
	}

	public void registerQueryResult(OntologyCompiler compiler, Rdf2GoCore core, Section<?> section, String query, Class<? extends Resource> termClass) {
		Iterator<BindingSet> iterator = core.sparqlSelectIt(query);
		while (iterator.hasNext()) {
			BindingSet row = iterator.next();
			String value = row.getValue("resource").stringValue();
			registerTerm(compiler, core, section, value, termClass);
		}
	}

	public void registerTerm(OntologyCompiler compiler, Rdf2GoCore core, Section<?> section, String uri, Class<?> termClass) {
		Map<String, String> namespaces = core.getNamespaces();
		String abbreviation = null;
		String resource = null;
		for (Map.Entry<String, String> entry : namespaces.entrySet()) {
			if (Strings.isBlank(entry.getKey())) continue;
			if (uri.startsWith(entry.getValue())) {
				abbreviation = entry.getKey();
				resource = uri.substring(entry.getValue().length());
				break;
			}
		}
		if (abbreviation == null) {
			Log.warning("No matching namespace found for URI '" + uri + "', skipping term registration.");
			return;
		}
		TerminologyManager terminologyManager = compiler.getTerminologyManager();
		Identifier abbrResourceIdentifier = new Identifier(abbreviation, resource);
		Identifier abbrIdentifier = new Identifier(abbreviation);
		// rdfs identifiers will be part in every import (because we use RDFS reasoning in the model)
		// so we skip the stuff that is already defined...
		if (!terminologyManager.isDefinedTerm(abbrIdentifier)) {
			terminologyManager.registerTermDefinition(compiler, section, AbbreviationDefinition.class, abbrIdentifier);
		}
		if (!terminologyManager.isDefinedTerm(abbrResourceIdentifier)) {
			terminologyManager.registerTermDefinition(compiler, section, termClass, abbrResourceIdentifier);
		}
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<PackageCompileType> section) {
		// no need to remove something, we get a new TerminologyManager
		// anyway...
	}

}
