package de.knowwe.core.kdom.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Compiler;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;

/**
 * Class representing some information about a specific term from all over the
 * wiki, e.g. a complete set of all term managers that defines the specified
 * term.
 * <p>
 * The TermInfos are created by and accessed through the TermUtils.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 25.08.2013
 */
public interface TermInfo extends Iterable<TermCompiler> {

	/**
	 * Returns the identifier of this term info. According to the case
	 * sensitivity flag {@link #isCaseSensitive()} the identifiers collected by
	 * this term info may vary in the case of the actual identifier. If you
	 * require to match {@link TermInfo} objects under consideration of the case
	 * sensitivity, use {@link #matches(Identifier)} or {@link #getKey()}.
	 * 
	 * @created 26.08.2013
	 * @return the/a term identifier representing the term of this TermInfo
	 */
	Identifier getIdentifier();

	/**
	 * Returns if the contents of this term info are collected for case
	 * sensitive or case insensitive for the specific term identifier.
	 * 
	 * @created 26.08.2013
	 * @return the case sensitivity flag
	 */
	boolean isCaseSensitive();

	/**
	 * Returns if the specified identifier will match this {@link TermInfo},
	 * according to the {@link Identifier} of this set and whether this set is
	 * case sensitive or not.
	 * 
	 * @created 25.08.2013
	 * @param otherIdentifier the identifier to be checked if it matches this
	 *        set
	 * @return if the identifier matches this set
	 */
	boolean matches(Identifier otherIdentifier);

	/**
	 * Returns the key the to identify this {@link TermInfo} object. Is
	 * considers the term's identifier and the case sensitivity flag.
	 * 
	 * @created 26.08.2013
	 * @return the key to be used by that term info
	 */
	String getKey();

	/**
	 * Returns a (unmodifiable) collection of all the TermCompilers that
	 * are defining (and maybe referencing) the specific term identifier of this
	 * {@link TermInfo}. Depending on the flag {@link #isCaseSensitive()} the
	 * included terms are all identical or may be in different cases.
	 * 
	 * @created 26.08.2013
	 * @return the {@link TermCompiler} defining the specific term
	 * @see #getIdentifier()
	 * @see #isCaseSensitive()
	 */
	@NotNull
	Collection<TermCompiler> getTermCompilers();

	/**
	 * Returns a (unmodifiable) collection of all the TermCompilers that
	 * are defining (and maybe referencing) the specific term identifier of this
	 * {@link TermInfo}. Depending on the flag {@link #isCaseSensitive()} the
	 * included terms are all identical or may be in different cases.
	 * This method allows to only return the compilers of a given type, if any are available.
	 *
	 * @created 26.08.2013
	 * @param clazz the type of term compiler we want
	 * @return the {@link TerminologyManager} defining the specific term
	 * @see #getIdentifier()
	 * @see #isCaseSensitive()
	 */
	@NotNull
	default <T extends TermCompiler> Collection<T> getTermCompilers(Class<T> clazz) {
		Collection<T> compilers = new ArrayList<>();
		for (Compiler compiler : getTermCompilers()) {
			if (clazz.isAssignableFrom(compiler.getClass())) {
				compilers.add(clazz.cast(compiler));
			}
		}
		return Collections.unmodifiableCollection(compilers);
	}

	/**
	 * Returns a TermCompiler of the specified class that
	 * is defining (and maybe referencing) the specific term identifier of this
	 * {@link TermInfo}. Depending on the flag {@link #isCaseSensitive()} the
	 * included terms are all identical or may be in different cases.
	 * This method allows to only return the compilers of a given type, if any are available.
	 *
	 * @created 26.08.2013
	 * @param clazz the type of term compiler we want
	 * @return the {@link TerminologyManager} defining the specific term
	 * @see #getIdentifier()
	 * @see #isCaseSensitive()
	 */
	@Nullable
	default <T extends TermCompiler> T getTermCompiler(Class<T> clazz) {
		for (Compiler compiler : getTermCompilers()) {
			if (clazz.isAssignableFrom(compiler.getClass())) {
				return clazz.cast(compiler);
			}
		}
		return null;
	}

	/**
	 * Implementation to iterate through the {@link TermInfo}'s terminology
	 * managers.
	 * 
	 * @created 26.08.2013
	 * @return an iterator to iterate through the terminology managers
	 */
	@Override
	Iterator<TermCompiler> iterator();

}
