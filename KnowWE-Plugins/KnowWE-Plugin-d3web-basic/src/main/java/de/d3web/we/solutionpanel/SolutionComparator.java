package de.d3web.we.solutionpanel;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseUtils;
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
		if (comparison == 0) {
			List<Integer> thisPos = KnowledgeBaseUtils.getPositionInTree(o1);
			Iterator<Integer> thisIter = thisPos.iterator();
			List<Integer> otherPos = KnowledgeBaseUtils.getPositionInTree(o2);
			Iterator<Integer> otherIter = otherPos.iterator();

			while (comparison == 0 && thisIter.hasNext() && otherIter.hasNext()) {
				comparison = thisIter.next().compareTo(otherIter.next());
			}
			if (comparison == 0) {
				comparison = thisPos.size() - otherPos.size();
			}
		}
		return comparison;
	}
}
