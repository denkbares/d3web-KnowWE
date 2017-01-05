/*
 * Copyright (C) 2013 denkbares GmbH, Germany
 */
package de.knowwe.ontology.action;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.ontology.tools.OntologyDownloadProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

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
		Rdf2GoCore rdf2GoCore = null;
		Section<?> section;
		if (title == null) {
			section = Sections.successor(Sections.get(secID), PackageCompileType.class);
		}
		else {
			Article article = KnowWEUtils.getArticleManager(context.getWeb()).getArticle(title);
			section = Sections.successor(article, PackageCompileType.class);
		}
		Collection<Rdf2GoCompiler> compilers = Compilers.getCompilers(section, Rdf2GoCompiler.class);
		for (Rdf2GoCompiler compiler : compilers) {
			if (compiler instanceof PackageCompiler) {
				PackageCompiler packageCompiler = (PackageCompiler) compiler;
				if (packageCompiler.getCompileSection() == section) {
					rdf2GoCore = compiler.getRdf2GoCore();
					break;
				}
			}
		}

		RDFFormat syntax = Rio.getWriterFormatForMIMEType(context.getParameter(PARAM_SYNTAX)).orElse(RDFFormat.TURTLE);
		String mimeType = syntax.getDefaultMIMEType() + "; charset=UTF-8";
		context.setContentType(mimeType);
		context.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		StringWriter writer = new StringWriter();
		rdf2GoCore.writeModel(writer, syntax);
		String content = writer.toString();
		byte[] contentBytes = content.getBytes("UTF-8");
		context.setContentLength(contentBytes.length);

		context.getWriter().write(new String(contentBytes, "UTF-8"));
	}

}
