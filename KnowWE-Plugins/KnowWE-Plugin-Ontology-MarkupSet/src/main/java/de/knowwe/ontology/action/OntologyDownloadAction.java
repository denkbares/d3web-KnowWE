/*
 * Copyright (C) 2013 denkbares GmbH, Germany
 */
package de.knowwe.ontology.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.semanticcore.utils.RDFUtils;
import com.denkbares.strings.Strings;
import com.denkbares.utils.Stopwatch;
import com.denkbares.utils.Streams;
import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.Action.Access;
import de.knowwe.core.action.RecompileAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.CompilerManager.CompilationBlock;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.GroupingCompiler;
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

	@Override
	public Access requiredAccess() {
		return Access.READ;
	}
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

		Compilers.awaitTermination(compiler.getCompilerManager());
		if (Boolean.parseBoolean(context.getParameter(PARAM_FULL_COMPILE, "false"))) {
			Compilers.awaitTermination(compiler.getCompilerManager());
			if (compiler.isIncrementalBuild()) {
				RecompileAction.recompileVariant(context, "Ontology download", compiler);
				Compilers.awaitTermination(compiler.getCompilerManager());
				compiler = getCompiler(context);
				if (compiler == null) failUnexpected(context, "Compile no longer available after recompile");
			}
		}

		String syntaxValue = context.getParameter(PARAM_SYNTAX);
		RDFFormat syntax;
		if (RDFUtils.TURTLE_PRETTY.getDefaultMIMEType().equals(syntaxValue)) {
			syntax = RDFUtils.TURTLE_PRETTY;
		}
		else {
			syntax = Rio.getParserFormatForMIMEType(syntaxValue).orElse(RDFFormat.RDFXML);
		}
		String mimeType = syntax.getDefaultMIMEType() + "; charset=UTF-8";

		// sanitize, the name is used as part of the response headers
		String filename = Strings.encodeFileName(context.getParameter(PARAM_FILENAME));
		if (filename == null) {
			filename = Compilers.getCompilerName(compiler) + "." + syntax.getFileExtensions()
					.stream()
					.findFirst()
					.orElse("ontology");
		}

		context.setContentType(mimeType);
		context.setContentDisposition("attachment", filename);
		context.getResponse()
				.addHeader("Last-Modified", org.apache.http.client.utils.DateUtils.formatDate(compiler.getLastModified()));
		Stopwatch stopwatch = new Stopwatch();
		Rdf2GoCore rdf2GoCore = compiler.getRdf2GoCore();
		// the name of the temp file is irrelevant, use a fixed one, as the core and compiler names are not sanitized
		File tempFile = Files.createTempFile("ontology-download-", "." + syntax.getDefaultFileExtension()).toFile();
		try {
			// Only the export blocks the compilation, streaming it to the client must not: every page save waits for
			// the compilation to be unblocked, so a slow client would keep the whole wiki from saving.
			try (CompilationBlock ignore = compiler.getCompilerManager().blockCompilation()) {
				exportModel(rdf2GoCore, syntax, tempFile);
			}
			try (InputStream inputStream = new FileInputStream(tempFile);
				 OutputStream outputStream = context.getOutputStream()) {
				Streams.stream(inputStream, outputStream);
			}
		}
		finally {
			//noinspection ResultOfMethodCallIgnored
			tempFile.delete();
		}
		stopwatch.log("Exported " + filename);
	}

	/**
	 * Writes the model of the specified core in the specified syntax into the specified file, replacing its content.
	 */
	private static void exportModel(Rdf2GoCore rdf2GoCore, RDFFormat syntax, File target) throws IOException {
		if (syntax == RDFFormat.TURTLE) {
			// pretty formatted turtle doesn't always work, we try first and do fallback in case it does not work
			try (OutputStream out = new FileOutputStream(target)) {
				rdf2GoCore.writeModel(out, syntax);
				return;
			}
			catch (Exception e) {
				LOGGER.warn("Formatted turtle export failed, very likely due to setting inline_blank_nodes, trying again without...");
			}
			// formatted writing didn't work, write again without formatting, we don't expect failure
			try (OutputStream out = new FileOutputStream(target)) {
				rdf2GoCore.writeModel(Rio.createWriter(syntax, out));
			}
		}
		else {
			try (OutputStream out = new FileOutputStream(target)) {
				rdf2GoCore.writeModel(out, syntax);
			}
		}
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
			section = $(Sections.get(context, secID)).successor(PackageCompileType.class).getFirst();
		}
		else {
			Article article = (title == null) ? context.getArticle()
					: context.getArticleManager().getArticle(title);
			section = $(article).successor(OntologyMarkup.class).successor(PackageCompileType.class).getFirst();
		}

		return Compilers.getCompiler(context, section, OntologyCompiler.class);
	}
}
