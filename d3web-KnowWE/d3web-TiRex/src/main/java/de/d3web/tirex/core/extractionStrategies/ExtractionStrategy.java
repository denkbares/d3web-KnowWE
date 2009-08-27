package de.d3web.tirex.core.extractionStrategies;

import de.d3web.kernel.domainModel.IDObject;
import de.d3web.tirex.core.OriginalMatchAndStrategy;

/**
 * The interface for all kinds of Strategies to extract certain portions of text
 * from wiki-files.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public interface ExtractionStrategy {
	/**
	 * @param toMatch
	 *            The IDObject (Question or Answer) which is to be searched for.
	 * @param knowledge
	 *            The String, which is to be parsed and may contain the textual
	 *            representation of the IDObject "toMatch".
	 * @return An OriginalMatchAndStrategy object is created and returned if the
	 *         extraction was successful.
	 */
	public abstract OriginalMatchAndStrategy extract(IDObject toMatch, String knowledge);

	/**
	 * @return The name of the strategy.
	 */
	public abstract String getName();

	/**
	 * @return It might be useful to mark a match found by a strategy. The
	 *         Annotation provided by this method can be used to do that.
	 */
	public abstract Annotation getAnnotation();

	/**
	 * return Returns a custom "rating" for the strategy, which indicates how
	 * good it supposedly is. For example a direct match is better than a
	 * synonym match, etc.. The value returned should be chosen between 0 and 1.
	 */
	public abstract double getRating();
}
