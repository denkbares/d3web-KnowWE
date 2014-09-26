package de.knowwe.kdom.sectionFinder;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Allocates all text before any of the given tokens is first found in the text.
 * <p/>
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 01.03.2014
 */
public class AllBeforeStringFinder extends AbstractSingleResultFinder {

	private final String[] tokens;
	private final boolean trimmed;
	private final boolean noComment;
	private final boolean unquoted;
	private final boolean includeString;

	/**
	 * Allocates all text before any of the given strings is first found in the text. Tokens inside of quotes or a
	 * comment (double slash til line end) are ignored. The result is trimmed.
	 */
	public AllBeforeStringFinder(String... strings) {
		this(true, true, true, false, strings);
	}

	/**
	 * Allocates all text before any of the given strings is first found in the text. You can specify whether strings
	 * inside quotes or comments (double slash til line end) should be ignored or not. Also the result can be trimmed.
	 *
	 * @param unquoted  if true, strings in between quotes will be ignored
	 * @param noComment if true, strings in comments will be ignored.
	 * @param trimmed   if true, the result will be trimmed
	 * @param includeString if true, the found string will be included in the result
	 * @param strings   the strings for which we want the text before
	 */
	public AllBeforeStringFinder(boolean unquoted, boolean noComment, boolean trimmed, boolean includeString, String... strings) {
		this.unquoted = unquoted;
		this.noComment = noComment;
		this.trimmed = trimmed;
		this.includeString = includeString;
		this.tokens = strings;
	}

	@Override
	public SectionFinderResult lookForSection(String text, Section<?> father, Type type) {
		int start = 0;
		int flags = 0;
		if (unquoted) flags += Strings.UNQUOTED;
		if (noComment) flags += Strings.SKIP_COMMENTS;
		int end = Strings.indexOf(text, flags, tokens);
		if (trimmed) {
			start = Strings.trimLeft(text, start, end);
			end = Strings.trimRight(text, start, end);
			if (includeString) end = addTokenLengthToOffset(text, end, tokens);
		}
		if (start <= end) {
			return new SectionFinderResult(start, end);
		}
		else {
			return null;
		}
	}

	public static int addTokenLengthToOffset(String text, int offset, String[] tokens) {
		for (String token : tokens) {
			if (text.startsWith(token, offset)) return offset + token.length();
		}
		return offset;
	}
}
