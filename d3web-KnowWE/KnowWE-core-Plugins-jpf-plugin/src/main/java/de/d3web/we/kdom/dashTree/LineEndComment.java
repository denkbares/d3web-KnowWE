package de.d3web.we.kdom.dashTree;

import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.bulletLists.CommentRenderer;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.SplitUtility;

public class LineEndComment extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.setSectionFinder(new LineEndCommentFinder());
		setCustomRenderer(new CommentRenderer());
	}

	/**
	 * @author Jochen
	 *
	 * this LineEndCommentFinder assumes that single text 
	 * lines are given to the sectionfinder 
	 *
	 */
	static class LineEndCommentFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father, KnowWEObjectType type) {

			//looks for an unquoted occurrence of '//' and cuts off from this point 
			int start = SplitUtility.indexOfUnquoted(text, "//");
			if (start != -1) {
				//if found return section from start to the end of the line
				return SectionFinderResult
						.createSingleItemList(new SectionFinderResult(start,
								text.length()));
			}
			return null;
		}

	}
}
