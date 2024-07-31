/*
 * Copyright (C) ${year} denkbares GmbH, Germany
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

package de.d3web.we.knowledgebase;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.PackageCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler.PackageRegistrationScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.DefaultMarkupPackageCompileType;
import de.knowwe.core.compile.packaging.PackageAnnotationNameType;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.compile.packaging.PackageSelection;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.util.Icon;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * This class defines the knowledge base markup. With this, you can specify a knowledge base that will be compiled from
 * names package definitions, found on of all wiki articles.
 * <p>
 * As the content of the markup you must specify the knowledge base name. The markup also supports the following
 * annotations.
 * <ul>
 * <li><b>id:</b> a unique textual id of the knowledge base.
 * <li><b>version:</b> the current version of the knowledge base.
 * <li><b>author:</b> the responsible person/authority/company owning the
 * copyright of the knowledge base.
 * <li><b>comment:</b> some additional textual description on the knowledge
 * base.
 * <li><b>uses:</b> a package name that is searched for compiling.
 * </ul>
 * Please note that you must have at least one package defined. If you want to compile your knowledge base from several
 * packages, use multiple "@uses: ..." annotations. You may specify "this" or "default" as special package names. The
 * package name "default" may be used to compile all wiki content that have no explicitly defined package. The package
 * name "this" may be used to compile the contents of this article, ignoring their package declaration.
 *
 * @author volker_belli
 * @created 13.10.2010
 */
public class KnowledgeBaseMarkup extends DefaultMarkupPackageCompileType {

	public static final String ANNOTATION_ID = "id";
	public static final String ANNOTATION_VERSION = "version";
	public static final String ANNOTATION_AUTHOR = "author";
	public static final String ANNOTATION_COMMENT = "comment";
	public static final String ANNOTATION_FILENAME = "filename";
	public static final String ANNOTATION_STATUS = "status";
	public static final String ANNOTATION_AFFILIATION = "affiliation";
	public static final String ANNOTATION_TERM_MATCHING = "termMatching";

	public static final String DEFAULT_FILENAME = "kb.d3web";

	public static final String CASE_SENSITIVE = "case-sensitive";
	public static final String CASE_INSENSITIVE = "case-insensitive";

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("KnowledgeBase");
		MARKUP.setDocumentation("Markup to create a new knowledge base with specified content. Necessary for other d3web markups (like %%Question and %%Rule and so forth) to be compiled.");
		MARKUP.setTemplate("""
				%%Knowledgebase
				«KB-Name»

				@uses: «package-a»
				@uses: «package-b»
				%
				"""
		);
		MARKUP.addAnnotation(PackageManager.COMPILE_ATTRIBUTE_NAME, false)
				.addIcon(Icon.PACKAGE.addTitle("Uses"))
				.setDocumentation("A package that should be added to this knowledge base");
		MARKUP.addAnnotation(ANNOTATION_AUTHOR, false)
				.setDocumentation("The author of this knowledge base");
		MARKUP.addAnnotation(ANNOTATION_COMMENT, false)
				.setDocumentation("Some optional comment regarding this knowledge base");
		MARKUP.addAnnotation(ANNOTATION_ID, false)
				.setDocumentation("Set the ID of this knowledge base");
		MARKUP.addAnnotation(ANNOTATION_VERSION, false)
				.setDocumentation("Set the version of this knowledge base");
		MARKUP.addAnnotation(ANNOTATION_FILENAME, false)
				.setDocumentation("Set the file name of this knowledge base, will be the placeholder when downloading the knowledge base");
		MARKUP.addAnnotation(ANNOTATION_STATUS, false)
				.setDocumentation("Set the status of this knowledge base, e.g. alpha, beta, release");
		MARKUP.addAnnotation(ANNOTATION_AFFILIATION, false)
				.setDocumentation("Set the affiliation of this knowledge base, e.g. University of Würzburg");
		MARKUP.addAnnotation(ANNOTATION_TERM_MATCHING, false, CASE_SENSITIVE, CASE_INSENSITIVE)
				.setDocumentation("Decide whether matching between objects (questions, solutions, choice...) should be case-sensitive or case-insensitive");

		MARKUP.addContentType(new KnowledgeBaseDefinition());

		MARKUP.addAnnotationNameType(PackageManager.COMPILE_ATTRIBUTE_NAME, new PackageAnnotationNameType());
		MARKUP.addAnnotationContentType(PackageManager.COMPILE_ATTRIBUTE_NAME, new PackageSelection());
	}

	public KnowledgeBaseMarkup() {
		super(MARKUP);
		this.setRenderer(new KnowledgeBaseTypeRenderer());
		this.addCompileScript(Priority.HIGHEST, new D3webCompilerRegistrationScript());
		this.addCompileScript(Priority.HIGHEST, new KnowledgeBasePropertiesScript());
	}

	public String getFilename(Section<? extends KnowledgeBaseMarkup> self) {
		String filename = getAnnotation(self, ANNOTATION_FILENAME);
		return Strings.nonBlank(filename) ? filename : DEFAULT_FILENAME;
	}

	@NotNull
	public D3webCompiler getCompiler(Section<? extends KnowledgeBaseMarkup> self) {
		return Objects.requireNonNull(
				Compilers.getCompiler(Sections.successor(self, PackageCompileType.class), D3webCompiler.class),
				"unexpected internal error: no compiler created");
	}

	private static class D3webCompilerRegistrationScript implements PackageRegistrationScript<PackageCompileType> {

		@Override
		public void compile(PackageRegistrationCompiler compiler, Section<PackageCompileType> section) {
			String annotation = DefaultMarkupType.getAnnotation(section, ANNOTATION_TERM_MATCHING);
			boolean caseSensitive = CASE_SENSITIVE.equalsIgnoreCase(annotation);
			compiler.getCompilerManager()
					.addCompiler(5, new D3webCompiler(compiler.getPackageManager(), section, KnowledgeBaseMarkup.class, caseSensitive));
		}

		@Override
		public void destroy(PackageRegistrationCompiler compiler, Section<PackageCompileType> section) {
			for (PackageCompiler packageCompiler : section.get().getPackageCompilers(section)) {
				if (packageCompiler instanceof D3webCompiler) {
					compiler.getCompilerManager().removeCompiler(packageCompiler);
				}
			}
		}
	}

	private static class KnowledgeBasePropertiesScript implements D3webCompileScript<KnowledgeBaseMarkup> {
		@Override
		public void compile(D3webCompiler compiler, Section<KnowledgeBaseMarkup> section) throws CompilerMessage {
			// get required information
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);

			// prepare the items to be set into the knowledge base
			String id = getAnnotation(section, ANNOTATION_ID);
			String author = getAnnotation(section, ANNOTATION_AUTHOR);
			String comment = getAnnotation(section, ANNOTATION_COMMENT);
			String version = getAnnotation(section, ANNOTATION_VERSION);
			String filename = getAnnotation(section, ANNOTATION_FILENAME);
			String status = getAnnotation(section, ANNOTATION_STATUS);
			String affiliation = getAnnotation(section, ANNOTATION_AFFILIATION);
			String prompt = $(section).successor(KnowledgeBaseDefinition.class).mapFirst(s -> s.get().getTermName(s));

			// register package definition
			TerminologyManager terminologyManager = compiler.getTerminologyManager();
			terminologyManager.registerTermDefinition(compiler, section,
					KnowledgeBase.class, new Identifier("KNOWLEDGEBASE"));

			// and write it to the knowledge base
			if (id != null) kb.setId(id);
			InfoStore infoStore = kb.getInfoStore();

			if (prompt != null) infoStore.addValue(MMInfo.PROMPT, prompt);
			if (author != null) infoStore.addValue(BasicProperties.AUTHOR, author);
			if (comment != null) infoStore.addValue(MMInfo.DESCRIPTION, comment);
			if (version != null) infoStore.addValue(BasicProperties.VERSION, version);
			if (filename != null) infoStore.addValue(BasicProperties.FILENAME, filename);
			if (status != null) infoStore.addValue(BasicProperties.STATUS, status);
			if (affiliation != null) infoStore.addValue(BasicProperties.AFFILIATION, affiliation);
		}
	}
}
