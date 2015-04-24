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
package de.knowwe.ontology.compile;

import java.util.List;

import de.d3web.utils.Log;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileTypeRenderer;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.AnnotationNameType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageReferenceRegistrationScript;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupPackageRegistrationScript;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.kdom.InitTerminologyHandler;
import de.knowwe.rdf2go.RuleSet;
import de.knowwe.util.Icon;

/**
 * Compiles and provides ontology from the Ontology-MarkupSet.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 17.12.2013
 */
public class OntologyType extends DefaultMarkupType {

	public static final String ANNOTATION_COMPILE = "uses";
	public static final String ANNOTATION_RULESET = "ruleset";
	public static final String ANNOTATION_COMMIT = "commit";
	public static final String ANNOTATION_IMPORT = "import";

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Ontology");
		MARKUP.addAnnotation(ANNOTATION_COMPILE, false);
		MARKUP.addAnnotationIcon(ANNOTATION_COMPILE, Icon.PACKAGE.addTitle("Uses"));

		MARKUP.addAnnotation(ANNOTATION_IMPORT, false);
		MARKUP.addAnnotationIcon(ANNOTATION_IMPORT, Icon.FILE_XML.addTitle("Import"));

		MARKUP.addAnnotation(ANNOTATION_RULESET, false, RuleSet.values());
		MARKUP.addAnnotationIcon(ANNOTATION_RULESET, Icon.COG.addTitle("Rule Set"));

		MARKUP.addAnnotationContentType(ANNOTATION_IMPORT, new ImportType());
		MARKUP.addAnnotation(ANNOTATION_COMMIT, false, CommitType.values());
		DefaultMarkupPackageCompileType compileType = new DefaultMarkupPackageCompileType();
		compileType.addCompileScript(Priority.INIT, new InitTerminologyHandler());
		compileType.addCompileScript(new OntologyCompilerRegistrationScript());
		MARKUP.addContentType(compileType);

		MARKUP.addAnnotationContentType(PackageManager.COMPILE_ATTRIBUTE_NAME, new PackageTerm());
	}

	public OntologyType() {
		super(MARKUP);

		this.removeCompileScript(PackageRegistrationCompiler.class,
				DefaultMarkupPackageReferenceRegistrationScript.class);
		this.setRenderer(new DefaultMarkupPackageCompileTypeRenderer() {
			@Override
			protected void renderContents(Section<?> section, UserContext user, RenderResult string) {
				List<Section<AnnotationType>> annotations = Sections.successors(section, AnnotationType.class);
				for (Section<AnnotationType> annotation : annotations) {
					Section<AnnotationNameType> annotationName = Sections.successor(annotation, AnnotationNameType.class);
					if (annotationName.getText().startsWith("@" + ANNOTATION_COMPILE)) continue;
					DelegateRenderer.getInstance().render(annotation, user, string);
					string.appendHtml("<br>");
				}
				super.renderContents(section, user, string);
			}
		});

		// don't add this markup section to the packages, instead, add it to the compilation manually
		// if we would add it to a package and that package is compiled in multiple ontology markups,
		// all ontology markup sections in that package would be compiled by the each compiler, although each should
		// only be compiled by one
		removeCompileScript(PackageRegistrationCompiler.class, DefaultMarkupPackageRegistrationScript.class);

	}

	private static class OntologyCompilerRegistrationScript extends PackageRegistrationScript<PackageCompileType> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageCompileType> section) throws CompilerMessage {
			Section<DefaultMarkupType> ontologyType = Sections.ancestor(section, DefaultMarkupType.class);
			String ruleSetValue = DefaultMarkupType.getAnnotation(ontologyType, ANNOTATION_RULESET);
			RuleSet ruleSet = getRuleSet(ruleSetValue);
			OntologyCompiler ontologyCompiler = new OntologyCompiler(
					compiler.getPackageManager(), section, OntologyType.class, ruleSet );
			compiler.getCompilerManager().addCompiler(5, ontologyCompiler);
			if (ruleSetValue != null && ruleSet == null) {
				throw CompilerMessage.warning("The rule set \"" + ruleSetValue + "\" does not exist.");
			}
		}

		private RuleSet getRuleSet(String ruleSetValue) {
			if (ruleSetValue != null) {
				try {
					return RuleSet.valueOf(ruleSetValue);
				}
				catch (IllegalArgumentException e) {
					// no such rule set!
					Log.warning("No owlim ruleset found for: " + ruleSetValue);
				}
			}
			return null;
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageCompileType> section) {
			// we just remove the no longer used compiler... we do not need to destroy the s
			for (PackageCompiler packageCompiler : section.get().getPackageCompilers(section)) {
				if (packageCompiler instanceof OntologyCompiler) {
					compiler.getCompilerManager().removeCompiler(packageCompiler);
				}
			}
		}

	}

}