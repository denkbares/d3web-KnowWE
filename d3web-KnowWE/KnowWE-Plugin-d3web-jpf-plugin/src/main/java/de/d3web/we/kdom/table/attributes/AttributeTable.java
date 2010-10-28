/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.table.attributes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.CreateRelationFailed;
import de.d3web.we.kdom.table.Table;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableLine;
import de.d3web.we.object.QuestionReference;
import de.d3web.we.object.QuestionnaireReference;
import de.d3web.we.object.SolutionReference;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;

/**
 * AttributeTable which allows to specifiy additional information for
 * NamedObjects. Therefore the data is added to the MMInfoStorage.
 * 
 * It is not necessary to specify the header of the table, because it is
 * generated automatically.
 * 
 * The NamedObject is specified in the first column. The MMInfoSubject in the
 * second column. The DCElement[Lang] Title in the third. and the data/content
 * in the fourth;
 * 
 * @author Sebastian Furth
 * @created 28/10/2010
 */
public class AttributeTable extends Table {

	public AttributeTable() {
		super(new AttributeTableAttributesProvider());
		childrenTypes.add(0, new AttributeTableLine());
		this.setCustomRenderer(new AttributeTableRenderer());
		this.addSubtreeHandler(Priority.LOWEST, new AttributeTableSubTreeHandler());
	}

	public class AttributeTableSubTreeHandler extends D3webSubtreeHandler<AttributeTable> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<AttributeTable> s) {
			
			Collection<KDOMReportMessage> msg = new LinkedList<KDOMReportMessage>();

			List<Section<AttributeTableTempType>> tempTypes = new LinkedList<Section<AttributeTableTempType>>();
			s.findSuccessorsOfType(AttributeTableTempType.class, tempTypes);
			KnowledgeBaseManagement kbm = getKBM(article);
			
			for (Section<AttributeTableTempType> tempType : tempTypes) {

				NamedObject namedObject = findNamedObject(kbm, tempType.getOriginalText());

				// Create MMInfo
				if (namedObject != null) {
					createMMInfo(namedObject, tempType);
				}

				// Set correct TermReference Type
				if (namedObject instanceof Solution) {
					tempType.setType(new SolutionReference());
				}
				else if (namedObject instanceof Question) {
					tempType.setType(new QuestionReference());
				}
				else if (namedObject instanceof QContainer) {
					tempType.setType(new QuestionnaireReference());
				}
				else {
					msg.add(new CreateRelationFailed("Unable to find a terminology object named \""
							+ tempType.getOriginalText() + "\""));
				}

			}

			return msg;
		}

		private void createMMInfo(NamedObject namedObject, Section<AttributeTableTempType> tempType) {
			Section<TableLine> line = tempType.findAncestorOfType(TableLine.class);
			List<Section<TableCellContent>> cells = new LinkedList<Section<TableCellContent>>();
			line.findSuccessorsOfType(TableCellContent.class, cells);
			if (cells.size() == 4) {
				String subject = cells.get(1).getOriginalText().trim();
				String title = cells.get(2).getOriginalText().trim();
				String data = cells.get(3).getOriginalText().trim();
				addMMInfo(namedObject, title, subject, data);
			}
			else {
				Logger.getLogger(this.getClass().getName()).warning(
						"Failed to add MMInfo to \"" + namedObject);
			}

		}

		private NamedObject findNamedObject(KnowledgeBaseManagement kbm, String name) {
			// Is there a Question with this name?
			NamedObject namedObject = kbm.findQuestion(name);
			if (namedObject == null) {
				// Or a Solution?
				namedObject = kbm.findSolution(name);
				if (namedObject == null) {
					// Or a QContainer?
					namedObject = kbm.findQContainer(name);
				}
			}
			return namedObject;
		}

		private void addMMInfo(NamedObject o, String title, String subject, String content) {
			MMInfoStorage mmis;
			DCMarkup dcm = new DCMarkup();
			dcm.setContent(DCElement.TITLE, title);
			dcm.setContent(DCElement.SUBJECT, subject);
			dcm.setContent(DCElement.SOURCE, o.getId());
			MMInfoObject mmi = new MMInfoObject(dcm, content);
			mmis = new MMInfoStorage();
			o.getInfoStore().addValue(BasicProperties.MMINFO, mmis);
			mmis.addMMInfo(mmi);
		}

	}

}
