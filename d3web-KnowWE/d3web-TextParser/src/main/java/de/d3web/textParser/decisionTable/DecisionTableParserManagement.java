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

/* Created on 15. December 2004, 16:15 */
package de.d3web.textParser.decisionTable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import de.d3web.persistence.utilities.URLUtils;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.KBTextInterpreter;
import de.d3web.textParser.KWikiToKnofficePreParser;
import de.d3web.textParser.casesTable.TextParserResource;

/**
 * XLSParser liest eine XLS-Datei ein, welche eine oder mehrere gleichartige
 * Tabellen enthalten sollte. Mithilfe von übergebenen Syntax- und
 * Value-Checkern wird eine Syntax- sowie eine Werte-Überprüfung vorgenommen.
 * Ein KnowledgeGenerator kann aus den eingelesenen Tabellen Wissen erzeugen,
 * welches einer Wissensbasis hinzugefügt werden kann.
 * 
 * @author Andreas Klar
 */
public class DecisionTableParserManagement {

	/** enthält alle Tabellen, die in der Datei vorkommen */
	protected List<DecisionTable> tables;

	protected Object xlsFile;

	protected SyntaxChecker sChecker;

	protected ValueChecker vChecker;

	protected KnowledgeGenerator knowGen;

	protected Report report;

	/**
	 * Erzeugt einen neuen XLS Parser. Dabei wird die angegebene XLS-Datei mit
	 * dem übergebenen Syntax- und Value-Checker, sowie mit dem angebenen
	 * RuleMaker geparst.
	 * 
	 * @param xlsFile
	 *            XLS-Datei, welche geparst werden soll
	 * @param sChecker
	 *            der zu benutzende Syntax-Checker
	 * @param vChecker
	 *            der zu benutzende ValueChecker
	 * @param knowGen
	 *            der zu benutzende KnowledgeGenerator, kann auch weggelassen
	 *            werden, dann wird jedoch kein Wissen erzeugt
	 */
	public DecisionTableParserManagement(URL xlsFile, SyntaxChecker sChecker,
			ValueChecker vChecker, KnowledgeGenerator knowGen) {
		this(TextParserResource.makeTextParserResource(xlsFile,true),sChecker,vChecker,knowGen);
	}

	public DecisionTableParserManagement(TextParserResource xlsFile,
			SyntaxChecker sChecker, ValueChecker vChecker,
			KnowledgeGenerator knowGen) {
		this.tables = new ArrayList<DecisionTable>(1);
		this.report = new Report();

		if (xlsFile == null)
			report.error(MessageGenerator.noXLSFile());
		if (sChecker == null)
			report.error(MessageGenerator.missingSyntaxChecker());
		if (vChecker == null)
			report.error(MessageGenerator.missingValueChecker());
		if (knowGen == null)
			report.note(MessageGenerator.missingKnowledgeGenerator());
		HSSFWorkbook wb = readTable(xlsFile);
		if (!(report.getErrorCount() > 0 || wb == null)) {
			this.xlsFile = xlsFile;
			this.sChecker = sChecker;
			this.vChecker = vChecker;
			this.knowGen = knowGen;
			long before, after;
			before = System.currentTimeMillis();
			this.tables = HSSFFunctions.extractTables(wb, 1000);
			after = System.currentTimeMillis();
			// System.out.println("table-extract: "+xlsFile.getFile()+",
			// "+(after-before)+"ms");
			// value 1000 prevents user from putting more than one table on a
			// sheet
		}
	}

	public void checkContent() {
		if (tables.size() == 0) {
			report.warning(MessageGenerator.foundTables(0));
		}
		// Prüfungen durchführen und evtl. Fehler dem Report hinzufügen
		else {
			report.note(MessageGenerator.foundTables(tables.size()));
			Iterator it = tables.iterator();
			for (int count = 1; it.hasNext(); count++) {
				DecisionTable table = (DecisionTable) it.next();
				checkTableContent(table);
			}
		}
	}

	public DecisionTableParserManagement makeDecisionTableParserManagement(
			String filename, SyntaxChecker sChecker, ValueChecker vChecker,
			KnowledgeGenerator knowGen) {
		URL url = null;
		try {
			url = new File(filename).toURL();
		} catch (Exception e) {
			report.error(MessageGenerator.errorReadingXLS(e.getMessage()));

		}

		return new DecisionTableParserManagement(url, sChecker, vChecker,
				knowGen);
	}

	/**
	 * Liefert eine Liste, die alle Tabellen enthält, die vom Parser erkannt
	 * wurden
	 * 
	 * @return Liste mit allen Tabellen
	 */
	public List<DecisionTable> getTables() {
		return this.tables;
	}

	public SyntaxChecker getSyntaxChecker() {
		return this.sChecker;
	}

	public ValueChecker getValueChecker() {
		return this.vChecker;
	}

	public KnowledgeGenerator getKnowledgeGenerator() {
		return this.knowGen;
	}

	/**
	 * reads the Excel-File into a HSSFWorkbook. Adds error to report if an
	 * error occurs.
	 */
	private HSSFWorkbook readXLSFile(URL XLSFile) {
		try {

			InputStream in = null;
			in = URLUtils.openStream(XLSFile);
			// FileInputStream fileIn = new FileInputStream(XLSFile);
			HSSFWorkbook wb = new HSSFWorkbook(in);
			return wb;
		} catch (Exception e) {
			report.error(MessageGenerator.errorReadingXLS(e.getMessage()));
			return null;
		}
	}

	private HSSFWorkbook readTableXLS(Reader XLSFile) {
		InputStream stream = new ByteArrayInputStream(KWikiToKnofficePreParser
				.readBytes(XLSFile));
		HSSFWorkbook wb = null;

		try {
			wb = new HSSFWorkbook(stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			report.error(MessageGenerator.errorReadingXLS(e.getMessage()));
		}
		return wb;
	}

	private HSSFWorkbook readTable(TextParserResource tableData) {
		HSSFWorkbook wb = null;
		if (tableData.isExcelSource()) {
			// URL url = tableData.getUrl();

			InputStream stream = tableData.getStream();
			if (stream == null) {
				report.error(new Message(
						"no InputStream ready at TextParserResource:"
								+ tableData.toString()));
			} else {
				try {
					// InputStream stream = url.openStream();
					wb = new HSSFWorkbook(stream);
				} catch (IOException e) {
					report.error(MessageGenerator.errorReadingXLS(e
							.getMessage()));
					e.printStackTrace();
				}
			}
		} else {

			try {
				//System.out.println(tableData);
				if (tableData.getTableType().equals(
						KBTextInterpreter.ATTR_TABLE)) {
					wb = KWikiToKnofficePreParser
							.parseToAttributeTableWorkbook(tableData
									.getReader());
				} else if (tableData.getTableType().equals(
						KBTextInterpreter.QU_DIA_TABLE)
						|| tableData.getTableType().equals(
								KBTextInterpreter.SET_COVERING_TABLE)) {
					wb = KWikiToKnofficePreParser
							.parseToDiagnosisScoreWorkbook(tableData
									.getReader());
				} else if (tableData.getTableType().equals(
						KBTextInterpreter.QU_RULE_DIA_TABLE)) {
					wb = KWikiToKnofficePreParser
							.parseToDecisionTableWorkbook(tableData.getReader());
				}else {
					wb = KWikiToKnofficePreParser.buildHSSFWorkbook(KWikiToKnofficePreParser.parseToTable(tableData.getReader()));
				}
			} catch (DataFormatException e) {
				// TODO Auto-generated catch block
				report.error(MessageGenerator.errorReadingXLS(e.getMessage()));
			}
		}
		return wb;
	}

	/**
	 * performs syntax and value check for a decision table
	 * 
	 * @param table
	 *            the decision table to check
	 */
	protected void checkTableContent(DecisionTable table) {
		// perform syntax and value check
		Report checkReport = new Report();

		long before, after;
		before = System.currentTimeMillis();
		checkReport.addAll(sChecker.checkSyntax(table));
		after = System.currentTimeMillis();
		// System.out.println("Syntax-Check:
		// "+xlsFile.getFile()+":"+table.getSheetName()+",
		// "+(after-before)+"ms");

		before = System.currentTimeMillis();
		checkReport.addAll(vChecker.checkValues(table));
		after = System.currentTimeMillis();
		// System.out.println("Value-Check:
		// "+xlsFile.getFile()+":"+table.getSheetName()+",
		// "+(after-before)+"ms");

		for (Iterator<Message> it = checkReport.getAllMessages().iterator(); it
				.hasNext();) {
			Message next = it.next();
			next.setFilename(xlsFile.toString());
			next.setLocation(table.getSheetName() + ": " + next.getLocation());
		}

		// write errors to report
		if (!(checkReport.isEmpty())) {
			report.addAll(checkReport);
		}
	}

	/**
	 * Inserts the knowledge of the found tables into the knowledgeBase which is
	 * used by the KnowledgeGenerator
	 */
	public void insertKnowledge() {
		if (knowGen != null && report.getErrorCount() == 0) {
			for (Iterator it = tables.iterator(); it.hasNext();) {
				DecisionTable nextTable = (DecisionTable) it.next();
				long before, after;
				before = System.currentTimeMillis();
				report.addAll(knowGen.generateKnowledge(nextTable));
				after = System.currentTimeMillis();
				// System.out.println("Knowledge-Generation:
				// "+xlsFile.getFile()+":"+nextTable.getSheetName()+",
				// "+(after-before)+"ms");
			}
		}
		return;
	}

	/**
	 * Returns the report which contains informations generated by all tests
	 */
	public Report getReport() {
		return this.report;
	}
}
