package de.knowwe.core.kdom.objects;

import java.util.Collection;
import java.util.Iterator;

import de.d3web.strings.Identifier;
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
public interface TermInfo extends Iterable<TerminologyManager> {

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
	public abstract Identifier getIdentifier();

	/**
	 * Returns if the contents of this term info are collected for case
	 * sensitive or case insensitive for the specific term identifier.
	 * 
	 * @created 26.08.2013
	 * @return the case sensitivity flag
	 */
	public abstract boolean isCaseSensitive();

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
	public abstract boolean matches(Identifier otherIdentifier);

	/**
	 * Returns the key the to identify this {@link TermInfo} object. Is
	 * considers the term's identifier and the case sensitivity flag.
	 * 
	 * @created 26.08.2013
	 * @return the key to be used by that term info
	 */
	public abstract String getKey();

	/**
	 * Returns a (unmodifiable) collection of all the terminology managers that
	 * are defining (and maybe referencing) the specific term identifier of this
	 * {@link TermInfo}. Depending on the flag {@link #isCaseSensitive()} the
	 * included terms are all identical or may be in different cases.
	 * 
	 * @created 26.08.2013
	 * @return the {@link TerminologyManager} defining the specific term
	 * @see #getIdentifier()
	 * @see #isCaseSensitive()
	 */
	public abstract Collection<TerminologyManager> getManagers();

	/**
	 * Implementation to iterate through the {@link TermInfo}'s terminology
	 * managers.
	 * 
	 * @created 26.08.2013
	 * @return an iterator to iterate through the terminology managers
	 */
	public abstract Iterator<TerminologyManager> iterator();

}