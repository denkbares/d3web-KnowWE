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
package de.d3web.we.kdom.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.InvalidNumberError;
import de.d3web.we.kdom.sectionFinder.AllTextFinderTrimmed;
import de.d3web.we.kdom.sectionFinder.ISectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public class Number extends DefaultAbstractKnowWEObjectType {

	public Number() {
		this.setSectionFinder(new NumberFinder());
		// NumberChecker only makes sense if NumberFinder is overwritten..
		this.addSubtreeHandler(new NumberChecker());
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR7));
	}

	public static Double getNumber(Section<Number> s) {
		try {
			return Double.parseDouble(s.getOriginalText().trim());
		}
		catch (Exception e) {

		}

		return null;
	}

	// only one of them NumberFinder/NumberChecker makes sense to have for one
	// Number-type

	class NumberFinder implements ISectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, KnowWEObjectType type) {
			String trim = text.trim();
			try {
				Double.parseDouble(trim);
				return new AllTextFinderTrimmed().lookForSections(text, father, type);
			}
			catch (Exception e) {
				return null;
			}
		}

	}

	class NumberChecker extends SubtreeHandler<Number> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {
			List<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();
			String trim = s.getOriginalText().trim();
			try {
				Double.parseDouble(trim);
			}
			catch (Exception e) {
				msgs.add(new InvalidNumberError(trim));
			}
			return msgs;
		}

	}

}
