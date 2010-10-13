/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.d3web.we.sectionfinder;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.tcas.Annotation;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.uimaconnector.UIMAConnector;
import de.d3web.we.utils.AnnotationStore;
import de.d3web.we.utils.DefaultIntervalCalculator;
import de.d3web.we.utils.IIntervalCalculator;

/**
 * To Find Nested Types that are Annotations, that are Features of their father
 * types. If you only want to find Features see {@link FeatureSectionFinder}
 * 
 * @author Johannes Dienst
 * 
 */
public class NestedTypeSectionFinder implements ISectionFinder {

	private String clazzName = "";
	private IIntervalCalculator intervalCalculator = DefaultIntervalCalculator.getInstance();

	public NestedTypeSectionFinder(Class c) {
		clazzName = c.getName().replaceAll("knowwetypes", "typesystem");
	}

	/**
	 * @param clazzName2
	 * @param replace
	 */
	public NestedTypeSectionFinder(String clazzName, boolean replace) {
		this.clazzName = clazzName;
		if (replace) this.clazzName = clazzName.replaceAll("knowwetypes", "typesystem");
	}

	@Override
	public List<SectionFinderResult> lookForSections(
			String text, Section<?> father, KnowWEObjectType type) {
		ArrayList<SectionFinderResult> results = new ArrayList<SectionFinderResult>();
		AnnotationStore store = AnnotationStore.getInstance();

		int pos = 0;
		if (store.contains(clazzName)) pos = store.getAccessCount(clazzName);
		else store.initKey(clazzName);

		List<FeatureStructure> annos = UIMAConnector.getInstance().findFeatureAnnotations(clazzName);
		// HOTFIX for annos containing null
		if (pos == annos.size() || annos.contains(null)) {
			store.removeKey(clazzName);
			return results;
		}

		// Calculate Absolute starting position of father
		IIntervalCalculator calc = this.intervalCalculator.reInit(father);

		Annotation a;
		while (pos < annos.size()) {
			a = (Annotation) annos.get(pos);
			int begin = a.getBegin();
			int end = a.getEnd();

			if (calc.isResultValid(begin, end, text, clazzName)) {
				Integer[] result = calc.getRelativePositions(begin, end, text);
				store.incrementAccessCount(clazzName);
				((DefaultIntervalCalculator) calc).updateTakenSpace(begin, end);
				results.add(new SectionFinderResult(result[0], result[1]));
			}
			pos++;
		}

		return results;
	}

	/**
	 * Sets the IntervalCalculator
	 * 
	 * @param ic
	 */
	public void setIntervalCalculator(IIntervalCalculator ic) {
		this.intervalCalculator = ic;
	}
}
