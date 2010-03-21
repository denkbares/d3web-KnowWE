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

package de.d3web.we.wisec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.SemanticCore;
import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableLine;
import de.d3web.we.logging.Logging;

/**
 * Content type for the ListCriteria section.
 * 
 * @author Sebastian Furth
 */
public class ListCriteriaType extends DefaultAbstractKnowWEObjectType {

	public ListCriteriaType() {
		setSectionFinder(new AllTextSectionFinder());
		addReviseSubtreeHandler(new ListCriteriaSubtreeHandler());		
		addChildType(new WISECTable());
	}
		
	static class ListCriteriaSubtreeHandler implements ReviseSubTreeHandler {
		
		@Override
		public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {

			// Just to have fewer warnings :-)
			Section<ListCriteriaType> section = s;
			
			// Get the necessary Annotations
			Section<ListCriteriaRootType> root = section.findAncestor(ListCriteriaRootType.class);
			String listID = DefaultMarkupType.getAnnotation(root, "ListID");
			String upperlistID = DefaultMarkupType.getAnnotation(root, "UpperlistID");
					
			// Get the WISEC Namespace and create OwlObject
			String ns = SemanticCore.getInstance().expandNamespace("w");
			IntermediateOwlObject ioo = new IntermediateOwlObject();
			
			// Create OnUpperList statement
			createOnUpperListStatement(ioo, ns, listID, upperlistID);
			
			// Check if we want to use the KDOM
			boolean useKDom = s.get().getAllowedChildrenTypes().size() > 0 ? true : false;
			
			// Process the Table Content
			if (useKDom)
				createOWLUsingKDom(section, ioo, ns, listID);
			else {
				createOWL(section.getOriginalText().trim(), ioo, ns, listID);
			}

			// Add the created statements to KnowWE's SemanticCore
			SemanticCore.getInstance().addStatements(ioo, s);  
			return null;
		}

		/**
		 * Creates OWL Statements by traversing the KDOM and extracting
		 * the necessary information from it.
		 * @param section the current section
		 * @param ioo the already created IntermediateOwlObject which stores the statements
		 * @param ns the WISEC Namespace
		 * @param listID the ID of the list
		 */
		private void createOWLUsingKDom(Section<ListCriteriaType> section,
				IntermediateOwlObject ioo, String ns, String listID) {

			// Check if the table was recognized
			if (section.findSuccessor(WISECTable.class) != null) {
				
				// Get all lines
				List<Section<TableLine>> tableLines = new ArrayList<Section<TableLine>>();
				section.findSuccessorsOfType(TableLine.class, tableLines);
				
				for (Section<TableLine> line : tableLines) {
					
					// Get the content of all cells
					ArrayList<Section<TableCellContent>> contents = new ArrayList<Section<TableCellContent>>();
					line.findSuccessorsOfType(TableCellContent.class, contents);
					
					// Create OWL from cell content
					if (contents.size() == 2 && !contents.get(1).getOriginalText().matches("\\s*"))
						createCharacteristicStatement(ioo, 
													  ns, 
													  listID, 
													  contents.get(0).getOriginalText().trim(), 
													  contents.get(1).getOriginalText().trim()
													  );
				}
			} else {
				Logging.getInstance().warning("Processing via KDOM failed, trying it without KDOM");
				createOWL(section.getOriginalText().trim(), ioo, ns, listID);
			}
			
		}

		/**
		 * Creates OWL Statements <b>without</b> traversing the KDOM
		 * @param tableContent the Content of the Table
		 * @param ioo the already created IntermediateOwlObject which stores the statements
		 * @param ns the WISEC Namespace
		 * @param listID the ID of the list
		 */
		private void createOWL(String tableContent, IntermediateOwlObject ioo,
				String ns, String listID) {
			
			// Remove the trailing dashes
			StringBuilder bob = new StringBuilder(tableContent);
			while (bob.charAt(bob.length() - 1) == '-')
				bob.delete(bob.length() - 1, bob.length());
			tableContent = bob.toString();
			
			Pattern cellPattern = Pattern.compile("\\s*\\|+\\s*");
			String[] cells = cellPattern.split(tableContent);
			for (int i = 1; i < cells.length - 1; i += 2) {
				if (!cells[i+1].equals(""))
					createCharacteristicStatement(ioo, ns, listID, cells[i].trim(), cells[i+1].trim());
			}
		}

		private void createCharacteristicStatement(IntermediateOwlObject ioo,
				String ns, String listID, String characteristic, String value) {
			URI source = SemanticCore.getInstance().getUpper().getHelper().createURI(listID);
			URI prop = SemanticCore.getInstance().getUpper().getHelper().createURI(ns, characteristic);
			Literal object = SemanticCore.getInstance().getUpper().getHelper().createLiteral(value);
			try {
				Statement stmt = SemanticCore.getInstance().getUpper().getHelper().createStatement(source, prop, object);
				ioo.addStatement(stmt);
				System.out.println(stmt.toString());
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}

		private void createOnUpperListStatement(IntermediateOwlObject ioo,
				String ns, String listID, String upperlistID) {
			URI source = SemanticCore.getInstance().getUpper().getHelper().createURI(listID);
			URI prop = SemanticCore.getInstance().getUpper().getHelper().createURI(ns, "onUpperList");
			URI object = SemanticCore.getInstance().getUpper().getHelper().createURI(upperlistID);
			try {
				Statement stmt = SemanticCore.getInstance().getUpper().getHelper().createStatement(source, prop, object);
				ioo.addStatement(stmt);
				System.out.println(stmt.toString());
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}

	}
}
