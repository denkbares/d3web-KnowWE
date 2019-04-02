package de.d3web.we.knowledgebase;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.object.D3webTerm;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.SimpleDefinitionRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.renderer.StyleRenderer;

import static de.knowwe.core.kdom.parsing.Sections.$;

public class KnowledgeBaseDefinition extends AbstractType implements TermDefinition, D3webTerm<KnowledgeBase>, RenamableTerm {

	public KnowledgeBaseDefinition() {
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		setRenderer(StyleRenderer.CONSTANT.withToolMenu());
		addCompileScript(Priority.HIGHER, new SimpleDefinitionRegistrationScript<>(DefaultGlobalCompiler.class));
		addCompileScript(Priority.HIGHER, (D3webCompileScript<KnowledgeBaseDefinition>) (compiler, section) -> {

			// get required information
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
			InfoStore infoStore = kb.getInfoStore();
			Section<KnowledgeBaseDefinition> titleSection =
					Sections.successor(section, KnowledgeBaseDefinition.class);
			if (titleSection != null) {
				String title = titleSection.get().getTermName(titleSection);
				infoStore.addValue(MMInfo.PROMPT, title);
				TerminologyManager terminologyManager = compiler.getTerminologyManager();
				terminologyManager.registerTermDefinition(
						compiler, titleSection, KnowledgeBase.class, new Identifier(title));
			}
		});
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
				$(self).ancestor(KnowledgeBaseType.class).mapFirst(KnowledgeBaseType::getCompiler));
	}
}
