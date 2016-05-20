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

import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import com.denkbares.semanticcore.config.RepositoryConfigs;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.d3web.utils.Log;
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

	private ExecutorService executorService = Executors.newFixedThreadPool(2);

	@Override
	public Collection<Message> create(OntologyCompiler compiler, Section<PackageCompileType> section) {
		registerTerminology(compiler, compiler.getRdf2GoCore(), section);

		Section<OntologyType> ontologyMarkup = Sections.ancestor(section, OntologyType.class);
		handleImports(compiler, DefaultMarkupType.getAnnotationContentSections(ontologyMarkup, OntologyType.ANNOTATION_IMPORT), false);
		handleImports(compiler, DefaultMarkupType.getAnnotationContentSections(ontologyMarkup, OntologyType.ANNOTATION_SILENT_IMPORT), true);

		registerTerm(compiler, compiler.getRdf2GoCore(), section, "http://www.w3.org/2002/07/owl#Thing", Resource.class);
		registerTerm(compiler, compiler.getRdf2GoCore(), section, "http://www.w3.org/2002/07/owl#Nothing", Resource.class);
		registerTerm(compiler, compiler.getRdf2GoCore(), section, "http://www.w3.org/2005/xpath-functions#string-length", Resource.class);
		registerTerm(compiler, compiler.getRdf2GoCore(), section, "http://www.w3.org/2001/XMLSchema#decimal", Resource.class);

		return Messages.noMessage();
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
					extension = location.substring(Math.max(0, location.lastIndexOf(".")));
				}
				// if content-location is not defined, we probably access a file directly...
				else {
					String fileName = new File(importString).getName();
					extension = fileName.substring(fileName.lastIndexOf("."));
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
