package de.d3web.we.kdom.condition.helper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.d3web.we.kdom.renderer.CommentRenderer;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.util.SplitUtility;

/**
 * 
 * A line comment within the condition-markup. Line end comments introduced by
 * '//' are always allowed after closing brackets: ')'
 * 
 * @author Jochen
 * @created 16.10.2010
 */
public class CompCondLineEndComment extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.setSectionFinder(new LineEndCommentFinder());
		setCustomRenderer(new CommentRenderer());
	}

	/**
	 * @author Jochen
	 * 
	 * 
	 */
	class LineEndCommentFinder implements ISectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
					Section father, KnowWEObjectType type) {

			// looks for an unquoted occurrence of '//'
			int start = SplitUtility.lastIndexOfUnquoted(text, "//");
			if (start != -1) {
				// needs to check, whether the comment is in the last line of
				// the content
				Pattern pattern = Pattern.compile("\\r?\\n");
				Matcher matcher = pattern.matcher(text);
				int lineBreak = -1;
				while (matcher.find()) {
					// attempts to find the last line break
					lineBreak = matcher.start();
				}

				// if no linebreak, then everthing from '//'
				if (lineBreak == -1) {

					return SectionFinderResult
							.createSingleItemList(new SectionFinderResult(start,
									text.length()));
				} // linebreak check whether it is before or after the
				// endline-comment
				else {
					if (CompositeCondition.hasLineBreakAfterComment(text)) {
						// not comment for current expression
						return null;
					}
					else {
						return SectionFinderResult
								.createSingleItemList(new SectionFinderResult(start,
										lineBreak));
					}

				}
			}
			return null;
		}

	}
}
