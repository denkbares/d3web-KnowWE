package de.knowwe.core.kdom.objects;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.denkbares.strings.Identifier;
import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.CompilationLocal;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.user.UserContext;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class TermUtils {

	private static final Pattern CONTROL_PATTERN = Pattern.compile("(?:^&REF)");

	/**
	 * Returns if a specified term usually requires quotes. This method can only
	 * make an assumption because if quotes are required depends on the markup
	 * where the term is used. By this method you will get an estimation that
	 * will work for all common existing markup. Especially a term requires
	 * Quotes if it already has some quotes. If you want to avoid this, also use
	 * the method {@link Strings#isQuoted(String)}.
	 *
	 * @param termName the term to be checked for quotes to be required
	 * @return if the term usually requires quotes / shall be quoted in common
	 * markups
	 * @created 23.08.2013
	 */
	public static boolean needsQuotes(String termName) {
		return Identifier.needsQuotes(termName) || CONTROL_PATTERN.matcher(termName).find();
	}

	/**
	 * Quotes a specified term if is need quotes. Be careful when using this,
	 * because a already quoted term needs quotes (see
	 * {@link #needsQuotes(String)}) and therefore is quoted again.
	 *
	 * @param termName the term name to be checked and quoted
	 * @return the eventually quoted term name
	 * @created 23.08.2013
	 */
	public static String quoteIfRequired(String termName) {
		if (needsQuotes(termName)) {
			termName = Strings.quote(termName);
		}
		return termName;
	}

	/**
	 * Returns a Collection of all defining {@link TerminologyManager}
	 * definitions for all {@link Identifier}. You may specify if the managers
	 * are collected case insensitive of case sensitive.
	 *
	 * @param web                the web to collect the definitions for
	 * @param caseSensitive      whether the managers are collected case insensitive
	 *                           of case sensitive
	 * @param allowedTermClasses a set of classes the matched definitions shall
	 *                           be type of. The method only returns term managers matching at
	 *                           least one of these classes.
	 * @return the collection of all term managers for all terms
	 * @created 25.08.2013
	 * @deprecated please use the faster, cached version of this call: {@link #getAllTermInfos(UserContext, boolean, Class[])}
	 */
	@Deprecated
	public static TermInfoSet getAllTermInfos(String web, boolean caseSensitive, Class<?>... allowedTermClasses) {
		return getAllTermInfosInternal(web, caseSensitive, allowedTermClasses);
	}

	@NotNull
	private static TermInfoSet getAllTermInfosInternal(String web, boolean caseSensitive, Class<?>[] allowedTermClasses) {
		TermInfoSet result = new TermInfoSet(caseSensitive, allowedTermClasses);
		result.initAllTerms(web);
		return result;
	}

	/**
	 * Returns a cached Collection of all defining {@link TerminologyManager}
	 * definitions for all {@link Identifier}. You may specify if the managers
	 * are collected case insensitive of case sensitive.
	 *
	 * @param userContext        the user context to fetch the term infos for
	 * @param caseSensitive      whether the managers are collected case insensitive
	 *                           of case sensitive
	 * @param allowedTermClasses a set of classes the matched definitions shall
	 *                           be type of. The method only returns term managers matching at
	 *                           least one of these classes.
	 * @return the collection of all term managers for all terms
	 * @created 25.08.2013
	 */
	public static TermInfoSet getAllTermInfos(UserContext userContext, boolean caseSensitive, Class<?>... allowedTermClasses) {
		final String sessionKey = "termInfos" + Stream.of(allowedTermClasses).map(Class::getName).collect(joining());
		//noinspection unchecked
		CompilationLocal<TermInfoSet> container = (CompilationLocal<TermInfoSet>)
				userContext.getSession().getAttribute(sessionKey);
		if (container == null) {
			synchronized (userContext.getSession()) {
				//noinspection unchecked
				container = (CompilationLocal<TermInfoSet>) userContext.getSession().getAttribute(sessionKey);
				if (container == null) {
					container = new CompilationLocal<>(userContext.getArticleManager().getCompilerManager(),
							() -> getAllTermInfosInternal(userContext.getWeb(), caseSensitive, allowedTermClasses));
					userContext.getSession().setAttribute(sessionKey, container);
				}
			}
		}
		return container.get();
	}

	/**
	 * Returns a Collection of all defining {@link TerminologyManager}
	 * definitions for a specified term {@link Identifier}. You may specify if
	 * the managers are collected case insensitive of case sensitive. The method
	 * returns an TermManagerSet with an empty list of
	 * {@link TerminologyManager}s if the identifier is defined in no article.
	 *
	 * @param web                the web to collect the definitions for
	 * @param termIdentifier     the identifier to get the term managers for
	 * @param caseSensitive      whether the managers are collected case insensitive
	 *                           of case sensitive
	 * @param allowedTermClasses a set of classes the matched definitions shall
	 *                           be type of. The method only returns term managers matching at
	 *                           least one of these classes.
	 * @return the collection of all term managers for the specified term
	 * @created 25.08.2013
	 */
	public static TermInfo getTermInfo(String web, Identifier termIdentifier, boolean caseSensitive, Class<?>... allowedTermClasses) {
		TermInfoSet set = new TermInfoSet(caseSensitive, allowedTermClasses);
		set.initTerm(web, termIdentifier);
		return set.getTermInfo(termIdentifier);
	}

	/**
	 * Simple method returning a set of all term identifiers of a given web.
	 *
	 * @param web the web for which the identifiers should be returned
	 * @return a set of all term identifiers
	 */
	public static Set<Identifier> getTermIdentifiers(String web) {
		// gathering all terms
		return Compilers.getCompilers(Environment.getInstance()
				.getArticleManager(web), TermCompiler.class)
				.stream()
				.map(compiler -> compiler.getTerminologyManager().getAllDefinedTerms())
				.flatMap(Collection::stream)
				.collect(toSet());
	}
}
