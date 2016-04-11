package de.knowwe.kdom.sectionFinder;

import java.util.regex.Pattern;

import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.kdom.constraint.AtMostOneFindingConstraint;
import de.knowwe.kdom.constraint.ConstraintSectionFinder;
import de.knowwe.kdom.constraint.SingleChildConstraint;

/**
 * A regex-sectionfinder that always creates at most one result (first match)
 * and also only creates one single child of this type w.r.t to father section
 * 
 * @author Jochen
 * @created 09.06.2011
 */
public class RegexSectionFinderSingle extends ConstraintSectionFinder {

	public RegexSectionFinderSingle(String p) {
		this(p, 0);
	}

	public RegexSectionFinderSingle(String p, int patternmod) {
		this(p, patternmod, 0);
	}

	public RegexSectionFinderSingle(String p, int patternmod, int group) {
		this(Pattern.compile(p, patternmod), group);
	}

	public RegexSectionFinderSingle(Pattern pattern) {
		this(pattern, 0);
	}

	public RegexSectionFinderSingle(Pattern pattern, int group) {
		super(new RegexSectionFinder(pattern, group));
		this.addConstraint(SingleChildConstraint.getInstance());
		this.addConstraint(AtMostOneFindingConstraint.getInstance());
	}
}
