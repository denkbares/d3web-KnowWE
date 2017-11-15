package de.d3web.we.kdom.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Pair;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.sectionFinder.AllBeforeStringFinder;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.03.2014
 */
public class RuleContainerFinder implements SectionFinder {

	private final String[] startTokens;
	private final String[] endTokens;

	public RuleContainerFinder(String[] startTokens, String[] endTokens) {
		this.startTokens = startTokens;
		this.endTokens = endTokens;
	}

	@Override
	public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
		int flags = Strings.UNQUOTED | Strings.SKIP_COMMENTS;
		// if we have rules with multiple lines, tokens are only searched for at the start of the line
		if (father.getText().contains("\n")) {
			flags |= Strings.FIRST_IN_LINE;
		}
		int start = Strings.indexOf(text, flags, startTokens);
		List<SectionFinderResult> results = new ArrayList<>();
		int end = start;
		while (start != -1 && end != text.length()) {
			int startOffset = AllBeforeStringFinder.addTokenLengthToOffset(text, start, startTokens);
			end = Strings.indexOf(text, startOffset, flags, endTokens);
			if (end == -1) end = text.length();

			end = separateRules(text, start, end);

			Pair<Integer, Integer> trimmed = Strings.trim(text, start, end);
			results.add(new SectionFinderResult(trimmed.getA(), trimmed.getB()));
			start = Strings.indexOf(text, end, flags, startTokens);
		}
		return results;
	}

	/**
	 * @param text  String containing the Rules as text, as well as comments
	 * @param start initial start index of the first rule
	 * @param end   current end index of the rule
	 * @return an unaltered index for {@code end} if no comment has been found between the rules, or a new index for
	 * end marking the end of a rule before the next comment
	 */
	private int separateRules(String text, int start, int end) {
		String toSplit = text.subSequence(start, end).toString();
		List<String> splits = Arrays.asList(toSplit.split("\n"));
		ListIterator<String> iterator = splits.listIterator(splits.size());
		int nEnd = end;
		while (iterator.hasPrevious()) {
			String testForComment = iterator.previous();
			if ("\r".equals(testForComment) || testForComment.startsWith("//")) {
				// .split removes "\n" from each line, thus making them 1 char shorter than they should be
				nEnd -= (testForComment.length() + 1);
			}
			else {
				if (ruleLineContainsComment(testForComment)) {
					nEnd -= (testForComment.length() - testForComment.indexOf("//")) + 1;
				}
				break;
			}
		}
		return nEnd;
	}

	private boolean ruleLineContainsComment(String line) {
		return (!line.startsWith("//") && line.contains("//"));
	}
}
