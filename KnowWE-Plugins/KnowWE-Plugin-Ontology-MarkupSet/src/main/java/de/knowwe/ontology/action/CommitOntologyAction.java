/*
 * Copyright (C) 2013 denkbares GmbH, Germany
 */
package de.knowwe.ontology.action;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.ontology.compile.OntologyCompiler;

/**
 * Commits changes in the OntologyCompiler.
 */
public class CommitOntologyAction extends AbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommitOntologyAction.class);

	@Override
	public void execute(UserActionContext context) throws IOException {

		String compileSectionId = context.getParameter(Attributes.SECTION_ID);
		Section<?> section = Sections.get(compileSectionId);
		Section<PackageCompileType> compileSection = Sections.cast(section, PackageCompileType.class);
		Collection<PackageCompiler> packageCompilers = compileSection.get().getPackageCompilers(compileSection);
		Optional<PackageCompiler> optionalCompiler = packageCompilers.stream()
				.filter(compiler -> compiler instanceof OntologyCompiler)
				.findFirst();
		LOGGER.info("Committing ontology changes for Ontology on article '" + compileSection.getTitle() + "'");
		optionalCompiler.ifPresent(compiler -> OntologyCompiler.commitOntology(((OntologyCompiler) compiler)));
	}

}
