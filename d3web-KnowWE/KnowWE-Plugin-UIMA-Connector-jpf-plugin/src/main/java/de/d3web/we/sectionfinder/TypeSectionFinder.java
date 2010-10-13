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
import java.util.Collections;
import java.util.List;

import org.apache.uima.jcas.tcas.Annotation;

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.uimaconnector.UIMAConnector;
import de.d3web.we.utils.AnnotationComparator;
import de.d3web.we.utils.AnnotationStore;
import de.d3web.we.utils.DefaultIntervalCalculator;
import de.d3web.we.utils.IIntervalCalculator;

public class TypeSectionFinder implements ISectionFinder {

	/**
	 * Types in CAS are found by their name.
	 */
	private final String clazzName;
	private IIntervalCalculator intervalCalculator =
			DefaultIntervalCalculator.getInstance();

	/**
	 * @param c
	 * @param replace
	 */
	public TypeSectionFinder(Class<?> c, boolean replace) {
		this(c.getName(), replace);
	}

	/**
	 * @param clazzName2
	 * @param replace
	 */
	public TypeSectionFinder(String clazzName, boolean replace) {

		if (replace) this.clazzName =
				clazzName.replaceAll("knowwetypes", "typesystem");
		else this.clazzName = clazzName;
	}

	@Override
	public List<SectionFinderResult> lookForSections(
			String text, Section<?> father, KnowWEObjectType type) {
		ArrayList<SectionFinderResult> results = new ArrayList<SectionFinderResult>();
		AnnotationStore store = AnnotationStore.getInstance();

		int pos = 0;
		if (store.contains(clazzName)) pos = store.getAccessCount(clazzName);
		else store.initKey(clazzName);

		List<Annotation> annos = UIMAConnector.getInstance().findAllTypes(clazzName);
		// Check if every annotation has been used.
		if (pos == annos.size()) {
			store.removeKey(clazzName);
			return results;
		}

		annos = this.sortAnnotationBySize(annos);

		// Calculate absolute starting position of father including offsets
		IIntervalCalculator calc = this.intervalCalculator.reInit(father);

		Annotation a;
		for (int p = 0; p < annos.size(); p++) {

			a = annos.get(p);
			int begin = a.getBegin();
			int end = a.getEnd();

			if (calc.isResultValid(begin, end, text, clazzName)) {
				Integer[] result = calc.getRelativePositions(begin, end, text);
				store.incrementAccessCount(clazzName);
				((DefaultIntervalCalculator) calc).updateTakenSpace(begin, end);
				results.add(new SectionFinderResult(result[0], result[1]));
			}
		}

		return results;
	}

	/**
	 * Annotations can be nested. So when you sort them by textlength this
	 * problem is shifted a bit, so that you have larger annotations first. This
	 * does NOT solve the problem!
	 * 
	 * @param annos
	 * @return
	 */
	private List<Annotation> sortAnnotationBySize(List<Annotation> annos) {
		Collections.sort(annos, new AnnotationComparator());
		return annos;
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
