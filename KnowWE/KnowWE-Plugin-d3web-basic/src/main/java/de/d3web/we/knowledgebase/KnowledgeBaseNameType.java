package de.d3web.we.knowledgebase;

import java.util.Collection;

import de.d3web.core.knowledge.InfoStore;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.strings.Identifier;
import de.d3web.we.object.D3webTerm;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.core.utils.KnowWEUtils;

public class KnowledgeBaseNameType extends AbstractType implements D3webTerm<KnowledgeBase>, RenamableTerm {

	public KnowledgeBaseNameType() {
		this.setIgnorePackageCompile(true);
		setSectionFinder(new AllTextFinderTrimmed());
		addSubtreeHandler(Priority.HIGHER, new D3webSubtreeHandler<KnowledgeBaseNameType>() {

			@Override
			public Collection<Message> create(Article article, Section<KnowledgeBaseNameType> section) {
				// get required information
				KnowledgeBase kb = getKB(article);
				InfoStore infoStore = kb.getInfoStore();
				Section<KnowledgeBaseNameType> titleSection =
						Sections.findSuccessor(section, KnowledgeBaseNameType.class);
				if (titleSection != null) {
					String title = titleSection.get().getTermName(titleSection);
					infoStore.addValue(MMInfo.PROMPT, title);
					TerminologyManager terminologyManager = KnowWEUtils.getTerminologyManager(article);
					terminologyManager.registerTermDefinition(
							titleSection, KnowledgeBase.class, new Identifier(title));
				}
				return null;
			}
		});
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		return newIdentifier.getLastPathElement();
	}

	@Override
	public Class<?> getTermObjectClass(Section<? extends Term> section) {
		return KnowledgeBase.class;
	}

	@Override
	public Identifier getTermIdentifier(Section<? extends Term> section) {
		return new Identifier(section.getText());
	}

	@Override
	public String getTermName(Section<? extends Term> section) {
		return section.getText();
	}

	@Override
	public KnowledgeBase getTermObject(Article article, Section<? extends D3webTerm<KnowledgeBase>> section) {
		return D3webUtils.getKnowledgeBase(article);
	}

}
