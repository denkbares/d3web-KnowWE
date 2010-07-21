package de.d3web.we.kdom.questionTreeNew;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashSubtree;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.D3webTermDefinition;
import de.d3web.we.kdom.objects.QuestionDefinition;
import de.d3web.we.kdom.objects.QuestionnaireDefinition;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;


public abstract class QuestionTreeElementDefinition<TermObject> extends D3webTermDefinition<TermObject> {

	protected final String parentQASetSectionKey = "PARENT_QASETSECTION_" + key;

	public QuestionTreeElementDefinition(String key) {
		super(key);
	}

	public int getPosition(Section<? extends QuestionTreeElementDefinition<TermObject>> s) {
		Section<DashSubtree> subTreeRoot = DashSubtree.getNextDashSubtreeFor(s);

		if (subTreeRoot != null) {
			Section<?> fatherSubTree = subTreeRoot.getFather();
			if (fatherSubTree.get() instanceof DashSubtree
					|| fatherSubTree.get() instanceof QuestionDashTree) {
				int pos = 0;
				for (Section<?> sec : fatherSubTree.getChildren()) {
					if (sec.get() instanceof DashSubtree) {
						if (sec == subTreeRoot) {
							return pos;
						}
						pos++;
					}
				}
			}
		}
		return 0;
	}

	/**
	 * retrieves AND stores the corresponding parent Question or Questionnaire
	 * to the given QuesitonTreeElement.
	 */
	@SuppressWarnings("unchecked")
	public Section<? extends QuestionTreeElementDefinition<?>> retrieveAndStoreParentQASetSection(
			Section<? extends QuestionTreeElementDefinition<?>> s) {
		
		Section<DashSubtree> subTree = DashSubtree.getNextDashSubtreeFor(s);
		Section<?> subtreeFather = subTree.getFather();

		while (subtreeFather.get() instanceof DashSubtree) {

			Section<? extends DashTreeElement> dashTreeElement =
					subtreeFather.findChildOfType(DashTreeElement.class);

			if (dashTreeElement != null) {
				Section<? extends QuestionTreeElementDefinition> qasetSection =
						dashTreeElement.findSuccessor(QuestionTreeElementDefinition.class);
				if (qasetSection != null && (qasetSection.get() instanceof QuestionDefinition
							|| qasetSection.get() instanceof QuestionnaireDefinition)) {

					KnowWEUtils.storeSectionInfo(s.getArticle(), s, parentQASetSectionKey,
							qasetSection);
					return (Section<? extends QuestionTreeElementDefinition<?>>) qasetSection;
				}
				subtreeFather = subtreeFather.getFather();
			}
		}
		return null;
	}

	/**
	 * returns the the corresponding parent Question or Questionnaire to the
	 * given QuesitonTreeElement.
	 */
	@SuppressWarnings("unchecked")
	public final Section<? extends QuestionTreeElementDefinition<?>> getStoredParentQASetSection(
			Section<? extends QuestionTreeElementDefinition<TermObject>> s) {

		Section<? extends QuestionTreeElementDefinition<?>> parentQASetSection =
				(Section<? extends QuestionTreeElementDefinition<?>>)
				KnowWEUtils.getStoredObject(s.getArticle(), s, parentQASetSectionKey);

		if (parentQASetSection == null) {
			parentQASetSection = retrieveAndStoreParentQASetSection(s);
		}

		return parentQASetSection;
	}

	/**
	 * If the answer section is reused and was hooked in somewhere else in the
	 * last version of article, this method returns the section of the
	 * corresponding last question-reference
	 * 
	 * @created 21.06.2010
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final Section<? extends QuestionTreeElementDefinition<?>> getLastStoredQASetSection(
			Section<? extends QuestionTreeElementDefinition<?>> s) {

		return (Section<? extends QuestionTreeElementDefinition<?>>) KnowWEUtils.getObjectFromLastVersion(
				s.getArticle(), s, parentQASetSectionKey);
	}
	
	public static abstract class QuestionTreeElementDefSubtreeHandler<T extends QuestionTreeElementDefinition<?>>
			extends D3webSubtreeHandler<T> {


		@Override
		public boolean needsToCreate(KnowWEArticle article, Section<T> s) {
			return super.needsToCreate(article, s)
					|| DashSubtree.subtreeAncestorHasNotReusedObjectDefs(article, s);
		}

		@Override
		public boolean needsToDestroy(KnowWEArticle article, Section<T> s) {
			return super.needsToDestroy(article, s)
					|| DashSubtree.subtreeAncestorHasNotReusedObjectDefs(article, s);
		}

	}

}
