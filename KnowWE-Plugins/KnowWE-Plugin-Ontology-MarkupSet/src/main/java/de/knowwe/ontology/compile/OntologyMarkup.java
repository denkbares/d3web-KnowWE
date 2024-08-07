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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.events.EventManager;
import com.denkbares.semanticcore.config.RepositoryConfig;
import com.denkbares.semanticcore.config.RepositoryConfigs;
import com.denkbares.strings.Strings;
import de.knowwe.core.compile.CompilationLocal;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileTypeRenderer;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageSelection;
import de.knowwe.core.compile.terminology.TermCompiler.MultiDefinitionMode;
import de.knowwe.core.compile.terminology.TermCompiler.ReferenceValidationMode;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.core.kdom.basicType.TimeStampType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationNameType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.ontology.kdom.InitTerminologyHandler;
import de.knowwe.ontology.kdom.namespace.AbbreviationDefinition;
import de.knowwe.ontology.kdom.namespace.Namespace;
import de.knowwe.ontology.kdom.namespace.NamespaceAbbreviationDefinition;
import de.knowwe.ontology.kdom.namespace.NamespaceDefinition;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Compiles and provides ontology from the Ontology-MarkupSet.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 17.12.2013
 */
public class OntologyMarkup extends DefaultMarkupPackageCompileType {
	private static final Logger LOGGER = LoggerFactory.getLogger(OntologyMarkup.class);

	public static final String PLUGIN_ID = "KnowWE-Plugin-Ontology-MarkupSet";

	public static final String ANNOTATION_COMPILE = PackageManager.COMPILE_ATTRIBUTE_NAME;
	public static final String ANNOTATION_RULE_SET = "ruleset";
	public static final String ANNOTATION_MULTI_DEF_MODE = "multiDefinitionMode";
	public static final String ANNOTATION_REFERENCE_VALIDATION_MODE = "referenceValidationMode";
	public static final String ANNOTATION_COMMIT = "commit";
	public static final String ANNOTATION_IMPORT = "import";
	public static final String ANNOTATION_EXPORT = "export";
	public static final String ANNOTATION_EXPORT_DELAY = "exportDelay";
	public static final String ANNOTATION_SILENT_IMPORT = "silentImport";
	public static final String ANNOTATION_DEFAULT_NAMESPACE = "defaultNamespace";
	public static final String ANNOTATION_TERM_MATCHING = "termMatching";
	public static final String CASE_SENSITIVE = "case sensitive";
	public static final String CASE_INSENSITIVE = "case insensitive";

	public static final DefaultMarkup MARKUP;
	public static final String COMPILER_PRIORITY = "compilerPriority";

	static {
		MARKUP = new DefaultMarkup("Ontology");
		MARKUP.setDocumentation("Markup to create a new ontology with specified content. " +
								"Necessary for other ontology markups (like %%TSM) to be compiled.");
		MARKUP.setTemplate("""
				%%Ontology
				«Ontology-Name»

				@uses: «package-a»
				@uses: «package-b»
				%
				"""
		);
		MARKUP.addContentType(new OntologyDefinition());

		MARKUP.addAnnotation(ANNOTATION_COMPILE, false)
				.addIcon(Icon.PACKAGE.addTitle("Uses"))
				.addContentType(new PackageSelection())
				.setDocumentation("A package that should be added to this ontology");

		MARKUP.addAnnotation(ANNOTATION_IMPORT, false)
				.addIcon(Icon.FILE_XML.addTitle("Import"))
				.addContentType(new AttachmentType(false))
				.setDocumentation("Import the ontology contained in the given attachment");

		MARKUP.addAnnotation(ANNOTATION_EXPORT, false)
				.addIcon(Icon.ATTACHMENT.addTitle("Export"))
				.setDocumentation("Specify an attachment to export the ontology to after every change");

		String delayDocu = "Time to wait for additional changes to the ontology before starting a new export";
		MARKUP.addAnnotation(ANNOTATION_EXPORT_DELAY, false, Pattern.compile("\\d+(\\.\\d+)?|" + TimeStampType.DURATION))
				.addIcon(Icon.CLOCK.addTitle(delayDocu))
				.setDocumentation(delayDocu);

		MARKUP.addAnnotation(ANNOTATION_SILENT_IMPORT, false)
				.addIcon(Icon.FILE.addTitle("Import silently (faster, but without term support)"))
				.addContentType(new AttachmentType(false))
				.setDocumentation("Import the ontology contained in the given attachment, but without being able to " +
								  "reference it in wiki markup later, to make compilation a bit faster (especially with big imports)");

		MARKUP.addAnnotation(ANNOTATION_TERM_MATCHING, false, CASE_SENSITIVE, CASE_INSENSITIVE)
				.setDocumentation("Decide whether matching between IRIs should be case-sensitive or case-insensitive");

		String[] configNames = RepositoryConfigs.values()
				.stream()
				.map(RepositoryConfig::getName)
				.sorted()
				.toArray(String[]::new);
		MARKUP.addAnnotation(ANNOTATION_RULE_SET, false, configNames)
				.addIcon(Icon.COG.addTitle("Rule Set"))
				.setDocumentation("The reasoning rule set to be used for this ontology.<br>" +
								  "Choose one of the following: <ul>"
								  + Arrays.stream(configNames).map(n -> "<li>" + n + "</li>").collect(Collectors.joining("\n")) + "</ul>");

		MARKUP.addAnnotation(ANNOTATION_MULTI_DEF_MODE, false, MultiDefinitionMode.class)
				.addIcon(Icon.ORDERED_LIST.addTitle("Multi-definition-mode"))
				.setDocumentation("""
						Specifies how to handle multiple definitions for the sem term/IRI.<br>
						There are the following options:
						<ul>
							<li>ignore: Multiple definitions are ignored (they are considered ok, no warning/error is shown)</li>
							<li>warn: If there are multiple definitions of the same term, a warning message is shown.</li>
							<li>error: If there are multiple definitions of the same term, an error message is show</li>
						</ul>
						""");

		MARKUP.addAnnotation(ANNOTATION_REFERENCE_VALIDATION_MODE, false, ReferenceValidationMode.class)
				.addIcon(Icon.ORDERED_LIST.addTitle("Reference-validation-mode"))
				.setDocumentation("""
						Specifies how references should be validated for this ontology.<br>
						There are the following options:
						<ul>
							<li>ignore: References not matching a definition are ignored (they are considered ok, no warning/error is shown).</li>
							<li>warn: If a term reference does not match to a definition, a warning is shown.</li>
							<li>error: If a term reference does not match to a definition, an error is shown.</li>
						</ul>
						""");

		MARKUP.addAnnotation(ANNOTATION_DEFAULT_NAMESPACE, false)
				.addIcon(Icon.GLOBE.addTitle("Default Namespace"))
				.addContentType(new NamespaceAbbreviationDefinition())
				.setDocumentation("Allows to define a default namespace that will be used " +
								  "by other markups, if no specific namespace is given.");

		MARKUP.addAnnotation(ANNOTATION_COMMIT, false, CommitType.class)
				.setDocumentation("Specifies whether changes to a page containing statements for the ontology should be " +
								  "committed to the ontology database immediately after saving, or on demand (button will be shown in header after changes).");
	}

	public OntologyMarkup() {
		super(MARKUP);
		this.addCompileScript(Priority.INIT, new InitTerminologyHandler());
		this.addCompileScript(Priority.HIGHEST, new OntologyCompilerRegistrationScript());

		this.setRenderer(new DefaultMarkupPackageCompileTypeRenderer() {
			@Override
			public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult string) {
				Section<?> title = $(section).successor(OntologyDefinition.class).getFirst();
				string.appendHtml("<div><b>").append(title, user).appendHtml("</b></div>\n");

				List<Section<AnnotationType>> annotations = Sections.successors(section, AnnotationType.class);
				for (Section<AnnotationType> annotation : annotations) {
					Section<AnnotationNameType> annotationName = Sections.successor(annotation, AnnotationNameType.class);
					assert annotationName != null;
					if (annotationName.getText().startsWith("@" + ANNOTATION_COMPILE)) continue;
					DelegateRenderer.getInstance().render(annotation, user, string);
					string.appendHtml("<br>");
				}
				super.renderContentsAndAnnotations(section, user, string);
			}
		});

		EventManager.getInstance().registerListener(OntologyExporter.getInstance());
	}

	@NotNull
	public OntologyCompiler getCompiler(Section<? extends OntologyMarkup> self) {
		return Objects.requireNonNull(
				Compilers.getCompiler(Sections.successor(self, PackageCompileType.class), OntologyCompiler.class),
				"unexpected internal error: no compiler created");
	}

	/**
	 * Returns the default namespace of the ontology of the given ontology compiler. If there are multiple default
	 * namespaces, a random default namespace will be used.
	 *
	 * @param compiler the compiler to get the default namespace from
	 * @return the default namespace or null, if there is no default namespace
	 */
	@Nullable
	public static Namespace getDefaultNamespace(OntologyCompiler compiler) {
		if (compiler == null) return null;
		return CompilationLocal.getCached(compiler, "defaultNamespace", () -> {
			Section<OntologyMarkup> ontologyTypeSection = compiler.getCompileSection();
			Section<? extends AnnotationContentType> annotationContentSection = getAnnotationContentSection(ontologyTypeSection, ANNOTATION_DEFAULT_NAMESPACE);
			if (annotationContentSection == null) return null;

			String abbreviation = $(annotationContentSection).successor(AbbreviationDefinition.class)
					.mapFirst(s -> s.get().getTermName(s));
			String uri = $(annotationContentSection).successor(NamespaceDefinition.class).mapFirst(Section::getText);
			if (abbreviation == null || uri == null) return null;
			return new Namespace(abbreviation, uri);
		});
	}

	public static int getCompilerPriority(Section<? extends PackageCompileType> compileTypeSection) {
		final Object priority = compileTypeSection.getObject(COMPILER_PRIORITY);
		return priority == null ? 5 : (int) priority;
	}

	public static void setCompilerPriority(Section<? extends PackageCompileType> compileTypeSection, int priority) {
		compileTypeSection.storeObject(COMPILER_PRIORITY, priority);
	}

	public static void resetCompilerPriority(Section<? extends PackageCompileType> compileTypeSection) {
		compileTypeSection.removeObject(COMPILER_PRIORITY);
	}

	private static class OntologyCompilerRegistrationScript implements PackageRegistrationScript<OntologyMarkup> {
		private static final Logger LOGGER = LoggerFactory.getLogger(OntologyCompilerRegistrationScript.class);

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<OntologyMarkup> section) throws CompilerMessage {
			String ruleSetValue = DefaultMarkupType.getAnnotation(section, ANNOTATION_RULE_SET);
			RepositoryConfig ruleSet = getRuleSet(ruleSetValue);
			String multiDefModeValue = DefaultMarkupType.getAnnotation(section, ANNOTATION_MULTI_DEF_MODE);
			MultiDefinitionMode multiDefMode = getMultiDefinitionMode(multiDefModeValue);
			String referenceValidationModeValue = DefaultMarkupType.getAnnotation(section, ANNOTATION_REFERENCE_VALIDATION_MODE);
			ReferenceValidationMode referenceValidationMode = getReferenceValidationMode(referenceValidationModeValue);
			String termMatchingAnnotation = DefaultMarkupType.getAnnotation(section, ANNOTATION_TERM_MATCHING);
			// for backward compatibility, ask for explicit insensitivity
			boolean caseSensitive = !CASE_INSENSITIVE.equalsIgnoreCase(termMatchingAnnotation);
			OntologyCompiler ontologyCompiler = new OntologyCompiler(
					compiler.getPackageManager(), section, OntologyMarkup.class, ruleSet, multiDefMode, referenceValidationMode, caseSensitive);
			compiler.getCompilerManager().addCompiler(getCompilerPriority(section), ontologyCompiler);

			if (ruleSetValue != null && ruleSet == null) {
				throw CompilerMessage.warning("The rule set \"" + ruleSetValue + "\" does not exist.");
			}
		}

		private ReferenceValidationMode getReferenceValidationMode(String referenceValidationMode) {
			return parseEnum(ReferenceValidationMode.class, referenceValidationMode, "reference-validation-mode", ReferenceValidationMode.error);
		}

		private MultiDefinitionMode getMultiDefinitionMode(String multiDefModeValue) {
			return parseEnum(MultiDefinitionMode.class, multiDefModeValue, "multi-definition-mode", MultiDefinitionMode.ignore);
		}

		private RepositoryConfig getRuleSet(String ruleSetValue) {
			return RepositoryConfigs.get(ruleSetValue);
		}

		private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value, String enumName, T defaultValue) {
			if (value != null) {
				try {
					return Enum.valueOf(enumClass, value);
				}
				catch (IllegalArgumentException e) {
					LOGGER.warn("'" + value + "' is not a " + enumName + ", please choose one of the following: "
								+ Strings.concat(", ", enumClass.getEnumConstants()));
				}
			}
			return defaultValue;
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<OntologyMarkup> section) {
			// we just remove the no longer used compiler... we do not need to destroy the s
			for (PackageCompiler packageCompiler : section.get().getPackageCompilers(section)) {
				if (packageCompiler instanceof OntologyCompiler) {
					compiler.getCompilerManager().removeCompiler(packageCompiler);
				}
			}
		}
	}
}
