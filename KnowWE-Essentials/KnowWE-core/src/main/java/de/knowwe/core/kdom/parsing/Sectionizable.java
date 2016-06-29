package de.knowwe.core.kdom.parsing;

import de.knowwe.core.kdom.sectionFinder.SectionFinder;


public interface Sectionizable {

	/**
	 * @param sectionFinder is the SectionFinder that is later returned by
	 *        {@link Sectionizable#getSectionFinder()};
	 */
	void setSectionFinder(SectionFinder sectionFinder);

	/**
	 * @return if a SectionFinder was set using
	 *         {@link Sectionizable#setSectionFinder(SectionFinder)}, that
	 *         SectionFinder should be returned. Else return a default
	 *         SectionFinder.
	 */
	SectionFinder getSectionFinder();

}
