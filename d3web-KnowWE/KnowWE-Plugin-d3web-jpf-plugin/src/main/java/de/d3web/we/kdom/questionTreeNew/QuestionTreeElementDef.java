package de.d3web.we.kdom.questionTreeNew;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.dashTree.DashSubtree;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.objects.D3webObjectDef;
import de.d3web.we.kdom.objects.QuestionDef;
import de.d3web.we.kdom.objects.QuestionnaireDef;
import de.d3web.we.terminology.D3webSubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;


public abstract class QuestionTreeElementDef<TermObject> extends D3webObjectDef<TermObject> {

	protected final String parentQASetSectionKey = "PARENT_QASETSECTION_" + key;

	public QuestionTreeElementDef(String key) {
		super(key);
	}

	public int getPosition(Section<? extends QuestionTreeElementDef<TermObject>> s) {
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
	 * <p/>
	 * Be sure to use KnowWEUtils.storeSectionInfo(s, parentQASetSectionKey,
	 * retrievedParent); to store the retrieved Section in the SectionStore!
	 * @param article TODO
	 * @param s
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Section<? extends QuestionTreeElementDef<?>> retrieveAndStoreParentQASetSection(
			Section<? extends QuestionTreeElementDef<?>> s) {
		
		Section<DashSubtree> subTree = DashSubtree.getNextDashSubtreeFor(s);
		Section<?> subtreeFather = subTree.getFather();

		while (subtreeFather.get() instanceof DashSubtree) {

			Section<? extends DashTreeElement> dashTreeElement =
					subtreeFather.findChildOfType(DashTreeElement.class);

			if (dashTreeElement != null) {
				Section<? extends QuestionTreeElementDef> qasetSection =
						dashTreeElement.findSuccessor(QuestionTreeElementDef.class);
				if (qasetSection != null && (qasetSection.get() instanceof QuestionDef
							|| qasetSection.get() instanceof QuestionnaireDef)) {

					KnowWEUtils.storeSectionInfo(s.getArticle(), s, parentQASetSectionKey,
							qasetSection);
					return (Section<? extends QuestionTreeElementDef<?>>) qasetSection;
				}
				subtreeFather = subtreeFather.getFather();
			}
		}
		return null;
	}


	/**
	 * returns the section of the corresponding question-reference for this
	 * answer.
	 * @param article TODO
	 * @param s
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final Section<? extends QuestionTreeElementDef<?>> getStoredParentQASetSection(
			Section<? extends QuestionTreeElementDef<TermObject>> s) {

		Section<? extends QuestionTreeElementDef<?>> parentQASetSection =
				(Section<? extends QuestionTreeElementDef<?>>)
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
	public final Section<? extends QuestionTreeElementDef<?>> getLastStoredQASetSection(
			Section<? extends QuestionTreeElementDef<?>> s) {

		return (Section<? extends QuestionTreeElementDef<?>>) KnowWEUtils.getObjectFromLastVersion(
				s.getArticle(), s, parentQASetSectionKey);
	}
	
	public static abstract class QuestionTreeElementDefSubtreeHandler<T extends QuestionTreeElementDef<?>>
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
