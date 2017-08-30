package de.d3web.we.kdom.rules;

import java.util.ArrayList;
import java.util.List;

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
			Pair<Integer, Integer> trimmed = Strings.trim(text, start, end);
			results.add(new SectionFinderResult(trimmed.getA(), trimmed.getB()));
			start = Strings.indexOf(text, end, flags, startTokens);
		}
		return results;
	}

}
