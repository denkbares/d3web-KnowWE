package de.d3web.we.kdom;

import de.d3web.we.kdom.sectionFinder.ISectionFinder;


public interface Sectionizable {

	/**
	 * @param sectionFinder is the SectionFinder that is later returned by
	 *        {@link Sectionizable#getSectioFinder()};
	 */
	public void setSectionFinder(ISectionFinder sectionFinder);

	/**
	 * @return if a SectionFinder was set using
	 *         {@link Sectionizable#setSectionFinder(ISectionFinder)}, that
	 *         SectionFinder should be returned. Else return a default
	 *         SectionFinder.
	 */
	public abstract ISectionFinder getSectioFinder();

}
