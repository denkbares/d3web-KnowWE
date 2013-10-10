package de.knowwe.timeline;

import java.util.Collection;
import java.util.Date;
import java.util.SortedMap;

import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.Value;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public interface IDataProvider {

	public abstract SortedMap<Date, Value> getValues(Question question);

	/**
	 * Tries to find a {@link Question} instance with the specified unique
	 * identifier.
	 * 
	 * @param questionName
	 *            the unique identifier of the search {@link Question}
	 * @return the searched question; <code>null</code> if none found
	 */
	public abstract Question searchQuestion(String questionName);

	/**
	 * Returns all dates where findings exist.
	 * 
	 * @return
	 */
	public abstract Collection<Date> getAllDates();
}