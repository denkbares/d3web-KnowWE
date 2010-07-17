/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.questionTreeNew;

import de.d3web.core.knowledge.terminology.info.NumericalInterval;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.kdom.sectionFinder.ConditionalAllTextFinder;

public class NumericCondLine extends DefaultAbstractKnowWEObjectType {


	@Override
	protected void init() {
		this.sectionFinder = new ConditionalAllTextFinder() {

			@Override
			protected boolean condition(String text, Section father) {
				return (text.startsWith("[") && text.endsWith("]")) || text.startsWith("<") || text.startsWith(">") || text.startsWith("=") ;
			}
		};

		this.setCustomRenderer(new FontColorRenderer(FontColorRenderer.COLOR7));

	}

	public static Double getValue(Section<NumericCondLine> sec) {
		String content = sec.getOriginalText();
		if(content.startsWith("\"") && content.endsWith("\"")) {
			content = content.substring(1, content.length()-1);
		}

			String value = content.substring(getComparator(sec).length()).trim();
			Double d = null;
			try {
			d = Double.parseDouble(value);
			} catch (NumberFormatException e) {
				//FIXME
			}
			return d;
	}

	public static String getComparator(Section<NumericCondLine> sec) {
		String content = sec.getOriginalText();
		if(content.startsWith("\"") && content.endsWith("\"")) {
			content = content.substring(1, content.length()-1).trim();
		}

		String [] comps = {"<=", ">=", "<", ">", "="};
		for (String string : comps) {
			if(content.startsWith(string)) {
				return string;
			}
		}
		return null;
	}
	
	public static NumericalInterval getNumericalInterval(Section<NumericCondLine> sec) {
		if (isIntervall(sec)) {
			String[] doubles = sec.getOriginalText().substring(1,
					sec.getOriginalText().length() - 1).split(" ");
			if (doubles.length == 2) {
				try {
					return new NumericalInterval(Double.parseDouble(doubles[0]),
							Double.parseDouble(doubles[1]));
				}
				catch (NumberFormatException e) {
				}
			}
		}
		return null;
	}

	public static boolean isIntervall(Section<NumericCondLine> sec) {
		if(sec.getOriginalText().startsWith("[") && sec.getOriginalText().endsWith("]")) {
			return true;
		}
		return false;
	}

}
