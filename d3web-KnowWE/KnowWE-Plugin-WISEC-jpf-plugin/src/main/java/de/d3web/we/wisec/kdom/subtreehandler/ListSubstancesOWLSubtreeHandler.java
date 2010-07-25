package de.d3web.we.wisec.kdom.subtreehandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.core.semantic.IntermediateOwlObject;
import de.d3web.we.core.semantic.SemanticCore;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.OwlSubtreeHandler;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableLine;
import de.d3web.we.logging.Logging;
import de.d3web.we.wisec.kdom.ListSubstancesRootType;
import de.d3web.we.wisec.kdom.ListSubstancesType;
import de.d3web.we.wisec.kdom.WISECTable;

public class ListSubstancesOWLSubtreeHandler extends OwlSubtreeHandler<ListSubstancesType> {
	
	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<ListSubstancesType> s) {

		// Get the ListID
		Section<ListSubstancesRootType> root = s.findAncestorOfType(ListSubstancesRootType.class);
		String listID = DefaultMarkupType.getAnnotation(root, "ListID");
		
		// Get the WISEC Namespace and create OwlObject
		String ns = SemanticCore.getInstance().expandNamespace("w");
		IntermediateOwlObject ioo = new IntermediateOwlObject();
		
		// Check if we want to use the KDOM
		boolean useKDom = s.get().getAllowedChildrenTypes().size() > 0 ? true : false;
		
		// Process the Table Content
		if (useKDom)
			createOWLUsingKDom(s, ioo, ns, listID);
		else {
			createOWL(s.getOriginalText().trim(), ioo, ns, listID);
		}
		
		// Add the created statements to KnowWE's SemanticCore
		SemanticCore.getInstance().addStatements(ioo, s);  
		return null;
	}

	private void createOWLUsingKDom(Section<ListSubstancesType> section,
			IntermediateOwlObject ioo, String ns, String listID) {
		
		boolean failed = false;
		
		// Check if the table was recognized
		if (section.findSuccessor(WISECTable.class) == null) {
			failed = true;
		} else {				
			// Get all lines
			List<Section<TableLine>> tableLines = new ArrayList<Section<TableLine>>();
			section.findSuccessorsOfType(TableLine.class, tableLines);
			
			// Find the SGN row
			int sgnIndex = -1; 
			if (tableLines.size() > 1)
				sgnIndex = findSGNIndexKDOM(tableLines.get(0));
			
			// Process all tableLines if SGN was found
			if (sgnIndex == -1) {
				failed = true;
			} else {
				for (int i = 1; i < tableLines.size(); i++) {
					ArrayList<Section<TableCellContent>> contents = new ArrayList<Section<TableCellContent>>();
					tableLines.get(i).findSuccessorsOfType(TableCellContent.class, contents);
					
					// Create OWL statements from cell content
					if (contents.size() >= sgnIndex) {
						addTypeStatement(ioo, ns, contents.get(sgnIndex).getOriginalText().trim());
						addOnListStatement(ioo, ns, contents.get(sgnIndex).getOriginalText().trim(), listID);
						addHasSubstanceStatement(ioo, ns, contents.get(sgnIndex).getOriginalText().trim(), listID);
					} else {
						failed = true;
					}
				}
			}
		}

		if (failed) { // Try to process the content without KDOM
			Logging.getInstance().warning("Processing via KDOM failed, trying it without KDOM");
			createOWL(section.getOriginalText().trim(), ioo, ns, listID);
		}
	}


	private void createOWL(String tableContent, IntermediateOwlObject ioo,
			String ns, String listID) {
		
		// Remove the trailing dashes
		StringBuilder bob = new StringBuilder(tableContent);
		while (bob.charAt(bob.length() - 1) == '-')
			bob.delete(bob.length() - 1, bob.length());
		tableContent = bob.toString();
		
		// Get the lines
		String[] lines = tableContent.split("\n");
		int sgnIndex = -1;
		
		 // We need at least a head and one content line
		if (lines.length > 1)
			sgnIndex = findSGNIndex(lines[0]);
		
		// if "SGN"-row was not found further processing is not possible
		if (sgnIndex > -1) {
			Pattern cellPattern = Pattern.compile("\\s*\\|\\s*");
			String[] cells;
			// lines[0] was the headline and is already processed
			for (int i = 1; i < lines.length; i++) {
				cells = cellPattern.split(lines[i]);
				addTypeStatement(ioo, ns, cells[sgnIndex]);
				addOnListStatement(ioo, ns, cells[sgnIndex], listID);
				addHasSubstanceStatement(ioo, ns, cells[sgnIndex], listID);
			}
		}	
	}

	private void addTypeStatement(IntermediateOwlObject ioo, String ns,
			String sgn) {
		URI source = SemanticCore.getInstance().getUpper().getHelper().createURI(sgn);
		URI object = SemanticCore.getInstance().getUpper().getHelper().createURI(ns, "Substance");
		try {
			Statement stmt = SemanticCore.getInstance().getUpper().getHelper().createStatement(source, RDF.TYPE, object);
			ioo.addStatement(stmt);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	private void addOnListStatement(IntermediateOwlObject ioo,
			String ns, String sgn, String listID) {
		URI source = SemanticCore.getInstance().getUpper().getHelper().createURI(sgn);
		URI prop = SemanticCore.getInstance().getUpper().getHelper().createURI(ns, "onListRelation");
		URI object = SemanticCore.getInstance().getUpper().getHelper().createURI(sgn + "-" + listID);
		try {
			Statement stmt = SemanticCore.getInstance().getUpper().getHelper().createStatement(source, prop, object);
			ioo.addStatement(stmt);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	private void addHasSubstanceStatement(IntermediateOwlObject ioo,
			String ns, String sgn, String listID) {
		URI source = SemanticCore.getInstance().getUpper().getHelper().createURI(listID);
		URI prop = SemanticCore.getInstance().getUpper().getHelper().createURI(ns, "hasSubstanceRelation");
		URI object = SemanticCore.getInstance().getUpper().getHelper().createURI(sgn + "-" + listID);
		try {
			Statement stmt = SemanticCore.getInstance().getUpper().getHelper().createStatement(source, prop, object);
			ioo.addStatement(stmt);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	private int findSGNIndex(String tablehead) {
		Pattern cellPattern = Pattern.compile("\\s*\\|{2}\\s*");
		String[] cells = cellPattern.split(tablehead);
		for (int i = 0; i < cells.length; i++) {
			if (cells[i].trim().equalsIgnoreCase("CAS_No"))
				return i;
		}
		return -1;
	}
	
	private int findSGNIndexKDOM(Section<TableLine> section) {
		ArrayList<Section<TableCellContent>> contents = new ArrayList<Section<TableCellContent>>();
		section.findSuccessorsOfType(TableCellContent.class, contents);
		for (int i = 0; i < contents.size(); i++) {
			if (contents.get(i).getOriginalText().trim().equalsIgnoreCase("CAS_No"))
					return i;
		}
		Logging.getInstance().warning("CAS_No row was not found!");
		return -1;
	}


}
