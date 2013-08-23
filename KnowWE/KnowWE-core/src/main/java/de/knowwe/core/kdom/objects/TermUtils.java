package de.knowwe.core.kdom.objects;

import java.util.regex.Pattern;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;

public class TermUtils {

	private static final String CONTROL_CHARS = "()[]{}<>\"\'#=";
	private static final Pattern CONTROL_PATTERN = Pattern.compile(
			"(^&REF)|([" + Pattern.quote(CONTROL_CHARS) + "])",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Returns if a specified term usually requires quotes. This method can only
	 * make an assumption because if quotes are required depends on the markup
	 * where the term is used. By this method you will get an estimation that
	 * will work for all common existing markup. Especially a term requires
	 * Quotes if it already has some quotes. If you want to avoid this, also use
	 * the method {@link Strings#isQuoted(String)}.
	 * 
	 * @created 23.08.2013
	 * @param termName the term to be checked for quotes to be required
	 * @return if the term usually requires quotes / shall be quoted in common
	 *         markups
	 */
	public static boolean needsQuotes(String termName) {
		return Identifier.needsQuotes(termName) || CONTROL_PATTERN.matcher(termName).find();
	}

	/**
	 * Quotes a specified term if is need quotes. Be careful when using this,
	 * because a already quoted term needs quotes (see
	 * {@link #needsQuotes(String)}) and therefore is quoted again.
	 * 
	 * @created 23.08.2013
	 * @param termName the term name to be checked and quoted
	 * @return the eventually quoted term name
	 */
	public static String quoteIfRequired(String termName) {
		if (needsQuotes(termName)) {
			termName = Strings.quote(termName);
		}
		return termName;
	}

}
