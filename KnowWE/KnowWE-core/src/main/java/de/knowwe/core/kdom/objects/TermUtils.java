package de.knowwe.core.kdom.objects;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;

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

	private static class DefaultTermInfo implements TermInfo {

		private final Identifier identifier;
		private final boolean caseSensitive;
		private final String key;
		private final Collection<TerminologyManager> managers = new LinkedList<TerminologyManager>();

		public DefaultTermInfo(Identifier identifier, boolean caseSensitive) {
			this.identifier = identifier;
			this.caseSensitive = caseSensitive;
			this.key = getKey(identifier, caseSensitive);
		}

		/**
		 * Returns the key the to identify a {@link TermInfo} object for a
		 * specific identifier and a case sensitivity flag.
		 * 
		 * @created 26.08.2013
		 * @param identifier the term identifier
		 * @param caseSensitive whether the term info shall be case sensitive
		 * @return the key to be used by that term info
		 * @see #getKey()
		 */
		public static String getKey(Identifier identifier, boolean caseSensitive) {
			return caseSensitive
					? identifier.toExternalForm()
					: identifier.toExternalForm().toLowerCase();
		}

		@Override
		public Identifier getIdentifier() {
			return identifier;
		}

		@Override
		public boolean isCaseSensitive() {
			return caseSensitive;
		}

		/**
		 * Returns if the specified identifier will match this {@link TermInfo},
		 * according to the {@link Identifier} of this set and whether this set
		 * is case sensitive or not.
		 * 
		 * @created 25.08.2013
		 * @param otherIdentifier the identifier to be checked if it matches
		 *        this set
		 * @return if the identifier matches this set
		 */
		@Override
		public boolean matches(Identifier otherIdentifier) {
			return key.equals(getKey(otherIdentifier, caseSensitive));
		}

		/**
		 * Returns the key the to identify this {@link TermInfo} object. Is
		 * considers the term's identifier and the case sensitivity flag.
		 * 
		 * @created 26.08.2013
		 * @return the key to be used by that term info
		 */
		@Override
		public String getKey() {
			return key;
		}

		public void addManager(TerminologyManager manager) {
			managers.add(manager);
		}

		@Override
		public Collection<TerminologyManager> getManagers() {
			return Collections.unmodifiableCollection(managers);
		}

		@Override
		public Iterator<TerminologyManager> iterator() {
			return managers.iterator();
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			TermInfo other = (TermInfo) obj;
			return (key.equals(other.getKey()));
		}
	}

	/**
	 * Returns a Collection of all defining {@link TerminologyManager}
	 * definitions for all {@link Identifier}. You may specify if the managers
	 * are collected case insensitive of case sensitive.
	 * 
	 * @created 25.08.2013
	 * @param web the web to collect the definitions for
	 * @param caseSensitive whether the managers are collected case insensitive
	 *        of case sensitive
	 * @param allowedTermClasses a set of classes the matched definitions shall
	 *        be type of. The method only returns term managers matching at
	 *        least one of these classes.
	 * @return the collection of all term managers for all terms
	 */
	public static Collection<TermInfo> getAllTermInfos(String web, boolean caseSensitive, Class<?>... allowedTermClasses) {
		Map<String, DefaultTermInfo> result = new HashMap<String, DefaultTermInfo>();
		if (allowedTermClasses.length == 0) {
			allowedTermClasses = new Class[] { Object.class };
		}

		// add and enhance term infos for all articles' term manager
		ArticleManager articleManager = Environment.getInstance().getArticleManager(web);
		for (Article article : articleManager.getArticles()) {
			TerminologyManager termManager = KnowWEUtils.getTerminologyManager(article);
			addAllMatchingTermInfos(result, termManager, caseSensitive, allowedTermClasses);
		}
		// add and enhance term infos also for global term manager
		addAllMatchingTermInfos(result,
				Environment.getInstance().getTerminologyManager(web, null),
				caseSensitive, allowedTermClasses);

		return Collections.<TermInfo> unmodifiableCollection(result.values());
	}

	private static void addAllMatchingTermInfos(Map<String, DefaultTermInfo> result, TerminologyManager termManager, boolean caseSensitive, Class<?>[] allowedTermClasses) {
		for (Identifier identifier : termManager.getAllDefinedTerms()) {
			// check if class is matched
			boolean classMatched = false;
			for (Class<?> clazz : allowedTermClasses) {
				if (termManager.hasTermOfClass(identifier, clazz)) {
					classMatched = true;
					break;
				}
			}
			if (!classMatched) continue;

			// add term manager
			String key = DefaultTermInfo.getKey(identifier, caseSensitive);
			DefaultTermInfo managers = result.get(key);
			if (managers == null) {
				managers = new DefaultTermInfo(identifier, caseSensitive);
				result.put(key, managers);
			}
			managers.addManager(termManager);
		}
	}

	/**
	 * Returns a Collection of all defining {@link TerminologyManager}
	 * definitions for a specified term {@link Identifier}. You may specify if
	 * the managers are collected case insensitive of case sensitive. The method
	 * returns an TermManagerSet with an empty list of
	 * {@link TerminologyManager}s if the identifier is defined in no article.
	 * 
	 * @created 25.08.2013
	 * @param web the web to collect the definitions for
	 * @param identifier the identifier to get the term managers for
	 * @param caseSensitive whether the managers are collected case insensitive
	 *        of case sensitive
	 * @param allowedTermClasses a set of classes the matched definitions shall
	 *        be type of. The method only returns term managers matching at
	 *        least one of these classes.
	 * @return the collection of all term managers for the specified term
	 */
	public static TermInfo getTermInfo(String web, Identifier termIdentifier, boolean caseSensitive, Class<?>... allowedTermClasses) {
		DefaultTermInfo result = new DefaultTermInfo(termIdentifier, caseSensitive);
		if (allowedTermClasses.length == 0) {
			allowedTermClasses = new Class[] { Object.class };
		}

		// check all articles' term manager to be added
		ArticleManager articleManager = Environment.getInstance().getArticleManager(web);
		for (Article article : articleManager.getArticles()) {
			TerminologyManager termManager = KnowWEUtils.getTerminologyManager(article);
			addTermManagerIfMatches(result, termManager, allowedTermClasses);
		}
		// check also global term manager to be added
		addTermManagerIfMatches(result,
				Environment.getInstance().getTerminologyManager(web, null), allowedTermClasses);
		return result;
	}

	private static void addTermManagerIfMatches(DefaultTermInfo result, TerminologyManager termManager, Class<?>[] allowedTermClasses) {
		for (Identifier identifier : termManager.getAllDefinedTerms()) {
			// check if term is matched
			if (!result.matches(identifier)) continue;

			// check if class is matched
			boolean classMatched = false;
			for (Class<?> clazz : allowedTermClasses) {
				if (termManager.hasTermOfClass(identifier, clazz)) {
					classMatched = true;
					break;
				}
			}
			if (!classMatched) continue;

			// add term manager
			result.addManager(termManager);
		}
	}

}
