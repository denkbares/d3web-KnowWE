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

package de.d3web.we.kdom.table.xcl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.kopic.AbstractKopicSection;
import de.d3web.we.kdom.kopic.renderer.KopicTableSectionRenderer;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.table.Table;
import de.d3web.we.kdom.table.TableAttributesProvider;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableColumnHeaderCellContent;
import de.d3web.we.kdom.table.TableLine;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.terminology.D3webSubtreeHandler;

public class CoveringTableSection extends AbstractKopicSection implements TableAttributesProvider {

	public static final String TAG = "SetCoveringTable-section";

	public CoveringTableSection() {
		super(TAG);
	}

	@Override
	protected void init() {
		childrenTypes.add(new CoveringTableContent());
		addSubtreeHandler(new CoveringTableSubTreeHandler());
		setCustomRenderer(new KopicTableSectionRenderer());
		setNotRecyclable(true);
	}

	private class CoveringTableSubTreeHandler extends D3webSubtreeHandler {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {

			// Set the headerline of the table as type SolutionHeaderLine
			Section headerLine = s.findSuccessor(TableLine.class);
			AbstractKnowWEObjectType solutionHeaderType = new SolutionTableHeaderLine();
			headerLine.setType(solutionHeaderType);

			// generate Knowledge by external covering-table-parser
			// KnowledgeBaseManagement kbm = getKBM(s);
			//			
			// if (kbm != null) {
			// TableParser parser = new TableParser2();
			// D3webBuilder builder = new D3webBuilder(s.getId(),
			// new XCLRelationBuilder("xcl"), 0, 0, parser, new
			// SingleKBMIDObjectManager(kbm));
			//	
			// builder.setLazy(true);
			// builder.setLazyDiag(true);
			//	
			// Section content = ((AbstractKopicSection)
			// s.getObjectType()).getContentChild(s);
			// if (content != null) {
			// parser.parse(removeTextIncludeTags(content.getOriginalText()));
			//	
			// List<Message> errors = builder.checkKnowledge();
			//	
			// storeMessages(s,errors);
			// Report ruleRep = new Report();
			// for (Message messageKnOffice : errors) {
			// ruleRep.add(messageKnOffice);
			// }
			// KnowWEParseResult result = new KnowWEParseResult(ruleRep, s
			// .getTitle(), removeTextIncludeTags(s.getOriginalText()));
			// s.getArticle().getReport().addReport(result);
			// }
			// }

			// Set headerColumn: first column is question/answer-column
			List<Section> headCells = new ArrayList<Section>();
			s.findSuccessorsOfType(TableColumnHeaderCellContent.class, headCells);
			for (Section section : headCells) {
				section.setType(new CoveringTableHeaderColumnCellContent());
			}

			// all cells still left are (potential) xclrelation-entry cells
			List<Section> entryCells = new ArrayList<Section>();
			s.findSuccessorsOfType(TableCellContent.class, entryCells);
			for (Section section : entryCells) {
				section.setType(new EntryCellContent());
			}

			return null;
		}
	}

	@Override
	public String[] getAttributeValues(Section<? extends TableCellContent> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestorOfType(AbstractXMLObjectType.class);
		if (xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(Table.ATT_VALUES).split(",");
		}
		return null;
	}

	@Override
	public String getNoEditColumnAttribute(Section<Table> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestorOfType(AbstractXMLObjectType.class);
		if (xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(Table.ATT_NOEDIT_COLUMN);
		}
		return null;
	}

	@Override
	public String getNoEditRowAttribute(Section<Table> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestorOfType(AbstractXMLObjectType.class);
		if (xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(Table.ATT_NOEDIT_ROW);
		}
		return null;
	}

	@Override
	public String getWidthAttribute(Section<Table> s) {
		Section<AbstractXMLObjectType> xml = s.findAncestorOfType(AbstractXMLObjectType.class);
		if (xml != null) {
			return AbstractXMLObjectType.getAttributeMapFor(xml).get(Table.ATT_WIDTH);
		}
		return null;
	}
}
