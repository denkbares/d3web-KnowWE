package de.d3web.we.knowledgebase;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.object.D3webTerm;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;

public class KnowledgeBaseNameType extends AbstractType implements D3webTerm<KnowledgeBase>, RenamableTerm {

	public KnowledgeBaseNameType() {
		setSectionFinder(new AllTextFinderTrimmed());
		addCompileScript(Priority.HIGHER, (D3webCompileScript<KnowledgeBaseNameType>) (compiler, section) -> {

			// get required information
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
			InfoStore infoStore = kb.getInfoStore();
			Section<KnowledgeBaseNameType> titleSection =
					Sections.successor(section, KnowledgeBaseNameType.class);
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
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		return newIdentifier.getLastPathElement();
	}

	@Override
	public Class<?> getTermObjectClass(TermCompiler compiler, Section<? extends Term> section) {
		return KnowledgeBase.class;
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		return section.getText();
	}

	@Override
	public KnowledgeBase getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<KnowledgeBase>> section) {
		return D3webUtils.getKnowledgeBase(compiler);
	}

}
