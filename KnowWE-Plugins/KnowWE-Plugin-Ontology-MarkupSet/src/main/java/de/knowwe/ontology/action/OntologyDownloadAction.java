/*
 * Copyright (C) 2013 denkbares GmbH, Germany
 */
package de.knowwe.ontology.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import com.denkbares.utils.Log;
import com.denkbares.utils.Streams;
import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.ontology.compile.OntologyType;
import de.knowwe.ontology.tools.OntologyDownloadProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 19.04.2013
 */
public class OntologyDownloadAction extends AbstractAction {

	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_SYNTAX = "syntax";

	@Override
	public void execute(UserActionContext context) throws IOException {

		String filename = context.getParameter(PARAM_FILENAME);
		String secID = context.getParameter(Attributes.SECTION_ID);
		String title = context.getParameter(OntologyDownloadProvider.TITLE);

		Section<?> section;
		if (secID != null) {
			section = $(Sections.get(secID)).successor(PackageCompileType.class).getFirst();
		}
		else {
			Article article = (title == null) ? context.getArticle()
					: KnowWEUtils.getArticleManager(context.getWeb()).getArticle(title);
			section = $(article).successor(OntologyType.class).successor(PackageCompileType.class).getFirst();
		}

		Rdf2GoCompiler compiler = Compilers.getCompiler(context, section, Rdf2GoCompiler.class);
		if (compiler == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "No compiler found");
			return;
		}

		Rdf2GoCore rdf2GoCore = compiler.getRdf2GoCore();

		RDFFormat syntax = Rio.getParserFormatForMIMEType(context.getParameter(PARAM_SYNTAX)).orElse(RDFFormat.RDFXML);
		String mimeType = syntax.getDefaultMIMEType() + "; charset=UTF-8";
		context.setContentType(mimeType);
		context.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		try (OutputStream outputStream = context.getOutputStream()) {
			if (syntax == RDFFormat.TURTLE) {
				// pretty formatted turtle doesn't always work, we try first and do fallback in case it does not work
				// since we can't fallback if we write to the response directly, we have to write to a temp file first
				final File tempFile = Files.createTempFile(secID, filename).toFile();
				tempFile.deleteOnExit();
				try {
					try (FileOutputStream out = new FileOutputStream(tempFile)) {
						rdf2GoCore.writeModel(out, syntax);
					}
					try (FileInputStream inputStream = new FileInputStream(tempFile)) {
						Streams.stream(inputStream, outputStream);
					}
				}
				catch (Exception e) {
					Log.warning("Formatted turtle export failed, very likely due to setting inline_blank_nodes, trying again without...");
					// formatted writing didn't work, just write to response directly, we don't expect failure
					rdf2GoCore.writeModel(Rio.createWriter(syntax, outputStream));
				}
				finally {
					tempFile.delete();
				}
			}
			else {
				rdf2GoCore.writeModel(outputStream, syntax);
			}
		}
	}
}
