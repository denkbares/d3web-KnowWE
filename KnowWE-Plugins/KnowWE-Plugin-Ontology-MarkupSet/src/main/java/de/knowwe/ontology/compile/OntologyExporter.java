/*
 * Copyright (C) 2016 denkbares GmbH, Germany
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

package de.knowwe.ontology.compile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import com.denkbares.events.Event;
import com.denkbares.events.EventListener;
import com.denkbares.utils.Log;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdf2go.Rdf2GoCore;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Exports the ontology of a type to a attachment specified by the @export annotation in the %%Ontology markup.
 * <p>
 * Created by Albrecht on 05.02.2016.
 */
public class OntologyExporter implements EventListener {

	private static OntologyExporter instance = null;

	public static OntologyExporter getInstance() {
		if (instance == null) instance = new OntologyExporter();
		return instance;
	}

	private OntologyExporter() {
	}

	@Override
	public Collection<Class<? extends Event>> getEvents() {
		return Collections.singletonList(OntologyCompilerFinishedEvent.class);
	}

	@Override
	public void notify(Event event) {
		OntologyCompilerFinishedEvent finishedEvent = (OntologyCompilerFinishedEvent) event;
		if (!finishedEvent.isOntologyChanged()) return;
		final OntologyCompiler compiler = finishedEvent.getCompiler();
		final Rdf2GoCore rdf2GoCore = compiler.getRdf2GoCore();
		Section<OntologyType> ontologySection = $(finishedEvent.getCompiler()
				.getCompileSection()).ancestor(OntologyType.class)
				.getFirst();
		if (ontologySection == null) {
			Log.severe("Unable to find ontology section of OntologyCompiler, something is very wrong...");
			return;
		}
		Section<?> exportAnnotation = DefaultMarkupType.getAnnotationContentSection(ontologySection, OntologyType.ANNOTATION_EXPORT);
		if (exportAnnotation == null) return; // no export specified, we are finished here
		String export = exportAnnotation.getText();
		String[] split = export.split("/");
		final String title;
		final String annotationName;
		if (split.length == 1) {
			title = ontologySection.getTitle();
			annotationName = split[0];
		}
		else if (split.length == 2) {
			title = split[0];
			annotationName = split[1];
		}
		else {
			Messages.storeMessage(exportAnnotation, this.getClass(), Messages.error("'" + export + "' is not"
					+ " a valid annotation to export to. Use article-name/annotation-name.syntax or"
					+ " only annotation-name.syntax instead."));
			return;
		}
		if (KnowWEUtils.getArticle(ontologySection.getWeb(), title) == null) {
			Messages.storeMessage(exportAnnotation, this.getClass(), Messages.error("Article '" + title + "' does not"
					+ " exist, export has to point to an existing article."));
			return;
		}

		// if not failed yet, clean up messages
		Messages.clearMessages(exportAnnotation, this.getClass());

		RDFFormat syntax = Rio.getWriterFormatForFileName(annotationName).orElse(RDFFormat.TURTLE);

		Thread exportTread = new Thread(() -> {
			Stopwatch stopwatch = new Stopwatch();
			WikiConnector connector = Environment.getInstance().getWikiConnector();
			ByteArrayInputStream stream;
			try {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				// cleanup so the versions don't stack to bad...
				rdf2GoCore.writeModel(outputStream, syntax);
				stream = new ByteArrayInputStream(outputStream.toByteArray());
			}
			catch (Exception e) {
				if (e.getCause() instanceof ClosedChannelException) {
					Log.warning("Export of ontology from '" + compiler.getCompileSection()
							.getTitle() + "' aborted due to repository shutdown.");
				}
				else {
					Log.severe("Unable to export ontology from '" + compiler.getCompileSection()
							.getTitle() + "'", e);
				}
				return;
			}
			try {
				connector.deleteAttachment(title, annotationName, "SYSTEM");
				connector.storeAttachment(title, annotationName, "SYSTEM", stream);
			}
			catch (IOException e) {
				Log.severe("Unable to save exported ontology as an attachment in '" + title + "/" + annotationName + "'", e);
				return;
			}
			Log.info("Exported ontology to attachment '" + title + "/" + annotationName + "' in " + stopwatch.getDisplay());
		});
		exportTread.start();

	}
}
