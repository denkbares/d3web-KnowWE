package de.d3web.we.knowledgebase;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.object.D3webTerm;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageCompilerNameDefinition;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.SimpleDefinitionRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.renderer.StyleRenderer;

import static de.knowwe.core.kdom.parsing.Sections.$;

public class KnowledgeBaseDefinition extends AbstractType implements PackageCompilerNameDefinition, D3webTerm<KnowledgeBase> {

	public KnowledgeBaseDefinition() {
		setSectionFinder(new AllTextFinderTrimmed(AllTextFinderTrimmed.TrimType.SPACES, true));
		setRenderer(StyleRenderer.CONSTANT.withToolMenu());
		addCompileScript(Priority.HIGHER, new SimpleDefinitionRegistrationScript<>(PackageRegistrationCompiler.class));
		addCompileScript(Priority.HIGHER, new KnowledgeBaseNameScript());
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return KnowledgeBase.class;
	}

	@Override
	public KnowledgeBase getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<KnowledgeBase>> section) {
		return D3webUtils.getKnowledgeBase(compiler);
	}

	@NotNull
	public D3webCompiler getCompiler(Section<? extends KnowledgeBaseDefinition> self) {
		return Objects.requireNonNull(
				$(self).ancestor(KnowledgeBaseMarkup.class).mapFirst(KnowledgeBaseMarkup::getCompiler));
	}

	@Override
	public Identifier getTermIdentifier(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return new Identifier(getTermObjectClass(compiler, section).getSimpleName(), getTermName(section));
	}

	private static class KnowledgeBaseNameScript implements D3webCompileScript<KnowledgeBaseDefinition> {
		@Override
		public void compile(D3webCompiler compiler, Section<KnowledgeBaseDefinition> section) throws CompilerMessage {

			// get required information
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
			Section<KnowledgeBaseDefinition> titleSection = Sections.successor(section, KnowledgeBaseDefinition.class);
			if (titleSection != null) {
				String title = titleSection.get().getTermName(titleSection);
				compiler.getTerminologyManager()
						.registerTermDefinition(compiler, titleSection, KnowledgeBase.class, new Identifier(title));
			}
		}

		@Override
		public void destroy(D3webCompiler compiler, Section<KnowledgeBaseDefinition> section) {
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
			Section<KnowledgeBaseDefinition> titleSection = Sections.successor(section, KnowledgeBaseDefinition.class);
			if (titleSection != null) {
				String title = titleSection.get().getTermName(titleSection);
				compiler.getTerminologyManager()
						.unregisterTermDefinition(compiler, titleSection, KnowledgeBase.class, new Identifier(title));
			}
		}
	}
}
