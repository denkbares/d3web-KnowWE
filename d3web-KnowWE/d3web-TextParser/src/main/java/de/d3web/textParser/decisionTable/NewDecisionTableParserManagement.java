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

package de.d3web.textParser.decisionTable;

import java.util.HashMap;
import java.util.Iterator;
import de.d3web.report.Message;
import de.d3web.textParser.Utils.QuestionNotInKBError;
import de.d3web.report.Report;
import de.d3web.textParser.casesTable.TextParserResource;

public class NewDecisionTableParserManagement extends
		DecisionTableParserManagement {
	
	private boolean scoreLine = false;
	private boolean conjunctorLine = false;
	private HashMap<String,Integer> questionTypes = new HashMap<String,Integer>();
	
	
	public NewDecisionTableParserManagement(TextParserResource xlsFile, SyntaxChecker sChecker, ValueChecker vChecker, KnowledgeGenerator knowGen) {
		super(xlsFile, sChecker, vChecker, knowGen);
		preprocessData();
		
	}
	
	private void preprocessData() {
		
		for (DecisionTable decisionTable : tables) {
			deleteQTags(decisionTable);
			deleteQuotes(decisionTable);
		}
		
	}
	
	public int getQuestionType(String name) {
		if(questionTypes.containsKey(name)) {
			return questionTypes.get(name).intValue();
		}
		return -1;
	}


	public static  void deleteQuotes(DecisionTable table) {
		for(int i = 0; i < table.columns(); i++) {
			for(int j = 0; j < table.rows(); j++) {
				String data = table.get(j, i).trim();
				table.getTableData()[j][i] = unquote(data);
			}
		}
		
	}
	
	public static String unquote(String data) {
		if(data.startsWith("\"") && data.endsWith("\"")) {
			return data.substring(1, data.length()-1).trim();
		}
		return data.trim();
	}
	
	public void deleteQTags(DecisionTable table) {
		for(int i = 0; i < table.rows(); i++) {
			String data = table.get(i, 0);
			int type = QuestionNotInKBError.getFollowingQTag(data);
			String name = unquote(QuestionNotInKBError.cutFollowingQTag(data).trim()).trim();
			if(type != -1) {
				questionTypes.put(name, new Integer(type));
			}
			table.getTableData()[i][0] = name;
		}
		
	}

	public int getScoreLine() {
		if(!scoreLine) { return -1;} else {
			return 1;
		}
		
	}
	
	public int getConjunctorLine() {
		if(!conjunctorLine) {
			return -1;
		}else {
			if(scoreLine) {
				return 2;
			}else {
				return 1;
			}
		}
	}
	public int getDataStartLine() {
		int line = 1;
		if(scoreLine) {
			line++;
		}
		if(conjunctorLine) {
			line++;
		}
		return line;
	}

	
	@Override
	protected void checkTableContent(DecisionTable table) {
		// perform syntax and value check
		if(sChecker instanceof NewHeuristicDecisionTableSyntaxChecker) {
			((NewHeuristicDecisionTableSyntaxChecker)sChecker).setParserManagement(this);
		}
		if(vChecker instanceof NewHeuristicDecisionTableValueChecker) {
			((NewHeuristicDecisionTableValueChecker)vChecker).setParserManagement(this);
		}
		if(knowGen instanceof NewHeuristicDecisionTableRuleGenerator) {
			((NewHeuristicDecisionTableRuleGenerator)knowGen).setParserManagement(this);
		}
		Report checkReport = new Report();
		if(table.get(1, 0).equalsIgnoreCase("Score")) {
			scoreLine = true;
			if(table.get(2, 0).equalsIgnoreCase("Operator")) {
				conjunctorLine = true;
			}
		}
		if(table.get(1, 0).equalsIgnoreCase("Operator")) {
			conjunctorLine = true;
		}
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
}
