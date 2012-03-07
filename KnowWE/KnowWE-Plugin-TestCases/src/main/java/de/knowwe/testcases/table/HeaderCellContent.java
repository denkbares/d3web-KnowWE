/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.testcases.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.kdom.subtreeHandler.SubtreeHandler;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableCellContentRenderer;
import de.knowwe.kdom.table.TableLine;

/**
 * 
 * 
 * @author Reinhard Hatko
 * @created 21.01.2011
 */
public class HeaderCellContent extends TableCellContent {

	public HeaderCellContent() {
		setRenderer(new TableCellContentRenderer(false));

		QuestionReference qref = new QuestionReference();
		qref.setRenderer(DefaultTextRenderer.getInstance());
		qref.setSectionFinder(new SectionFinder() {

			@Override
			public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
				Section<TableLine> line = Sections.findAncestorOfType(father, TableLine.class);

				// first two columns are no QRefs, but name and time
				if (line.getChildren().size() < 3) {
					return null;
				}
				else {
					String trim = text.trim();
					int start = text.indexOf(trim);
					if (trim.length() > 0) {
						return SectionFinderResult.createSingleItemList(new SectionFinderResult(
								start,
								start + trim.length()));
					}
					else {
						return null;
					}
				}

			}
		});

		qref.addSubtreeHandler(new SubtreeHandler<QuestionReference>() {

			@Override
			public Collection<Message> create(KnowWEArticle article, Section<QuestionReference> s) {

				KnowledgeBase kb = D3webUtils.getKnowledgeBase(article.getWeb(), article.getTitle());

				Question question = kb.getManager().searchQuestion(s.getText());

				if (question == null) {
					List<Message> messages = new ArrayList<Message>();
					// TODO message is not shown
					messages.add(Messages.noSuchObjectError(s.getText()));
					return messages;
				}
				else

				return null;
			}

		});

		childrenTypes.add(qref);

	}

}
