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
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.utils.Stopwatch;
import com.denkbares.utils.Streams;
import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.RecompileAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyMarkup;
import de.knowwe.ontology.tools.OntologyDownloadProvider;
import de.knowwe.rdf2go.Rdf2GoCore;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Sebastian Furth (denkbares GmbH)
 * @created 19.04.2013
 */
public class OntologyDownloadAction extends AbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyDownloadAction.class);

	public static final String PARAM_FILENAME = "filename";
	public static final String PARAM_SYNTAX = "syntax";
	private static final String PARAM_ONTOLOGY_NAME = "ontology";
	public static final String PARAM_FULL_COMPILE = "requireFullCompile";

	@Override
	public void execute(UserActionContext context) throws IOException {

		OntologyCompiler compiler = getCompiler(context);

		if (compiler == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND, "No compiler found");
			return;
		}

		if (!KnowWEUtils.canView(compiler.getCompileSection().getArticle(), context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You are not allowed to download this knowledge base");
			return;
		}

		Compilers.awaitTermination(context.getArticleManager().getCompilerManager());
		if (Boolean.parseBoolean(context.getParameter(PARAM_FULL_COMPILE, "false"))) {
			Compilers.awaitTermination(context.getArticleManager().getCompilerManager());
			if (compiler.isIncrementalBuild()) {
				RecompileAction.recompileVariant(context, "Ontology download");
				Compilers.awaitTermination(context.getArticleManager().getCompilerManager());
				compiler = getCompiler(context);
				if (compiler == null) failUnexpected(context, "Compile no longer available after recompile");
			}
		}

		Rdf2GoCore rdf2GoCore = compiler.getRdf2GoCore();

		RDFFormat syntax = Rio.getParserFormatForMIMEType(context.getParameter(PARAM_SYNTAX)).orElse(RDFFormat.RDFXML);
		String mimeType = syntax.getDefaultMIMEType() + "; charset=UTF-8";

		String filename = context.getParameter(PARAM_FILENAME);
		if (filename == null) {
			filename = Compilers.getCompilerName(compiler) + "." + syntax.getFileExtensions()
					.stream()
					.findFirst()
					.orElse("ontology");
		}

		context.setContentType(mimeType);
		context.getResponse().addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		context.getResponse()
				.addHeader("Last-Modified", org.apache.http.client.utils.DateUtils.formatDate(compiler.getLastModified()));
		Stopwatch stopwatch = new Stopwatch();
		try (OutputStream outputStream = context.getOutputStream()) {
			if (syntax == RDFFormat.TURTLE) {
				// pretty formatted turtle doesn't always work, we try first and do fallback in case it does not work
				// since we can't fallback if we write to the response directly, we have to write to a temp file first
				final File tempFile = Files.createTempFile(rdf2GoCore.getName(), filename).toFile();
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
					LOGGER.warn("Formatted turtle export failed, very likely due to setting inline_blank_nodes, trying again without...");
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
		stopwatch.log("Exported " + filename);
	}

	@Nullable
	private OntologyCompiler getCompiler(UserActionContext context) {
		String compilerName = context.getParameter(PARAM_ONTOLOGY_NAME);
		if (compilerName != null) {
			Optional<OntologyCompiler> compilerByName = Compilers.getCompilers(context, context.getArticleManager(), OntologyCompiler.class)
					.stream()
					.filter(c -> c.getName().equals(compilerName))
					.findFirst();
			if (compilerByName.isPresent()) {
				return compilerByName.get();
			}
		}

		String secID = context.getParameter(Attributes.SECTION_ID);
		String title = context.getParameter(OntologyDownloadProvider.TITLE);

		Section<?> section;
		if (secID != null) {
			section = $(Sections.get(secID)).successor(PackageCompileType.class).getFirst();
		}
		else {
			Article article = (title == null) ? context.getArticle()
					: KnowWEUtils.getArticleManager(context.getWeb()).getArticle(title);
			section = $(article).successor(OntologyMarkup.class).successor(PackageCompileType.class).getFirst();
		}

		return Compilers.getCompiler(context, section, OntologyCompiler.class);
	}
}
