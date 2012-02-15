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

package de.knowwe.kdom.dashtree;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * Type to detect Too-much-dashes-Erros in DashTree-Markup This error is first
 * ignored by the dash-tree parser (leaves it as plaintext/comment) This type
 * catches it to render it highlighted.
 * 
 * 
 * @author Jochen
 * 
 * 
 * 
 */
public class OverdashedElement extends AbstractType {

	public OverdashedElement() {

		this.addSubtreeHandler(new OverDashedErrorHandler());

		this.sectionFinder = new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
				// when there is no father, one dash is too much
				int level = 1;

				// IMPORTANT: +2
				if (father.get() instanceof DashSubtree) {
					level = DashTreeUtils.getDashLevel(father) + 2;
				}

				Matcher m = Pattern.compile("^\\s*" + "(-{" + level + "})",
						Pattern.MULTILINE).matcher(text);
				if (m.find()) {

					return SectionFinderResult.createSingleItemList(new SectionFinderResult(
							m.start(1), m.start(1) + level));
				}
				return null;
			}
		};
	}

	class OverDashedErrorHandler extends SubtreeHandler<OverdashedElement> {

		@Override
		public Collection<Message> create(KnowWEArticle article, Section<OverdashedElement> s) {
			return Messages.asList(Messages.syntaxError("to many dashes; remove \"-\""));
		}
	}

}
