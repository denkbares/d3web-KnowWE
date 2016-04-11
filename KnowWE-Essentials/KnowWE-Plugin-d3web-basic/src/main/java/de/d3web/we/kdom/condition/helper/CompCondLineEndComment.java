package de.d3web.we.kdom.condition.helper;

import de.d3web.strings.Strings;
import de.d3web.we.kdom.condition.CompositeCondition;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.renderer.StyleRenderer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * A line comment within the condition-markup. Line end comments introduced by
 * '//' are always allowed after closing brackets: ')'
 * 
 * @author Jochen
 * @created 16.10.2010
 */
public class CompCondLineEndComment extends AbstractType {

	public CompCondLineEndComment() {
		this.setSectionFinder(new LineEndCommentFinder());
		setRenderer(StyleRenderer.COMMENT);
	}

	/**
	 * @author Jochen
	 * 
	 * 
	 */
	class LineEndCommentFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
					Section<?> father, Type type) {

			// looks for an unquoted occurrence of '//'
			int start = Strings.lastIndexOfUnquoted(text, "//");
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

                // if no linebreak, then everything from '//'
                if (lineBreak == -1) {

					return SectionFinderResult
							.singleItemList(new SectionFinderResult(start,
									text.length()));
				} // linebreak check whether it is before or after the
                // end-line-comment
                else {
					if (CompositeCondition.hasLineBreakAfterComment(text)) {
						// not comment for current expression
						return null;
					}
					else {
						return SectionFinderResult
								.singleItemList(new SectionFinderResult(start,
										lineBreak));
					}

				}
			}
			return null;
		}

	}
}
