package de.d3web.we.solutionpanel;

import java.util.Comparator;

import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.session.Session;

public class SolutionComparator implements Comparator<Solution> {

	private final Session session;

	public SolutionComparator(Session session) {
		this.session = session;
	}

	@Override
	public int compare(Solution o1, Solution o2) {
		Rating rating1 = session.getBlackboard().getRating(o1);
		Rating rating2 = session.getBlackboard().getRating(o2);
		int comparison = rating2.compareTo(rating1);
		if (comparison == 0) return o1.getName().compareTo(o2.getName());
		return comparison;
	}
}
