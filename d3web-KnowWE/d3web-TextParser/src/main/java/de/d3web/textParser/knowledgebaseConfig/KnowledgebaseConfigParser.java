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

package de.d3web.textParser.knowledgebaseConfig;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.supportknowledge.DCElement;
import de.d3web.kernel.supportknowledge.DCMarkup;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.textParser.KBTextInterpreter;
import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.casesTable.TextParserResource;

public class KnowledgebaseConfigParser {

	private static final ResourceBundle rb = ResourceBundle
			.getBundle("properties.KBconfigParser");

	private TextParserResource file;

	private HashMap<String, String> configMap = new HashMap<String, String>();

	private HashMap<String, ConfigItem> allowedValues = new HashMap<String, ConfigItem>();

	private Report report = new Report();

	private int attributCnt = 0;

	// private boolean indicationRulesComplex = true;

	public static final String KEY_AUTHOR = "configItem.author";

	public static final String KEY_TITLE = "configItem.title";

	public static final String KEY_VERBALISATION_YES = "configItem.verbalization.yesAnswer";

	public static final String KEY_VERBALISATION_NO = "configItem.verbalization.noAnswer";

	public static final String KEY_VERBALISATION_UNKNOWN = "configItem.verbalization.unknown";

	public static final String KEY_UNKNOWN_VISIBLE = "configItem.presentation.unknownVisible";

	public static final String KEY_INDICATION_RULES = "configItem.parser.questionTree.indicationRules";

	public static final String KEY_UNIQUE_QUESTION_NAMES = "configItem.parser.questionTree.uniqueQuestionName";
	
	public static final String KEY_ESTABLISH_REFINE = "configItem.PSMethod.heuristic.establishRefine";
	
	public static final String KEY_EXCLUDE_DISCARD = "configItem.PSMethod.heuristic.excludeDiscard";

	public static final String KEY_RULEBASED_EXCLUSION = "configItem.PSMethod.scm.rulebasedExclusion";
	
	public static final String KEY_SCLIST_INHERITANCE = "configItem.parser.sclist.inheritance";
	
	public static final String KEY_LANGUAGE_EN = "configItem.language.english";
	
	public static final String KEY_SCPS_SIMPLE = "configItem.PSMethod.scm.simple";
	
	public static final String KEY_NEW_DCPARSER = "configItem.parser.questionTree.new";
	
	
	public static final String TRUE = "true";

	public static final String FALSE = "false";

	public KnowledgebaseConfigParser(TextParserResource res) {
		this.file = res;
		init();
	}

	public String getValue(String key) {
		if (key == null)
			return null;
		return configMap.get(key);
	}

	private void init() {
		ResourceBundle values = ResourceBundle
				.getBundle("properties/KBconfigValues");

		for (Enumeration<String> keys = values.getKeys(); keys
				.hasMoreElements();) {
			String key = keys.nextElement();
			if (key.startsWith("//"))
				continue;
			String value = values.getString(key);
			String[] pair = value.split("\\|");
			String[] answers = pair[1].split(",");

			allowedValues.put(key, new ConfigItem(pair[0], answers));
		}

	}

	public Report parse(KnowledgeBase kb) {
		List<String> data = file.getDataLines();

		int cnt = 0;
		for (String string : data) {
			if (string.trim().length() == 0 || string.trim().startsWith("//"))
				continue;
			String[] parts = string.split("=");
			
			if (parts.length != 2) {
				report.add(new Message(Message.ERROR, rb
						.getString("message.errorAt")		
						+ ": \""+ data.get(cnt)+"\"",
						KBTextInterpreter.KNOWLEDGEBASE_CONFIG, cnt, 0, null));
				
			}
			
			String myKey = checkIfAllowed(parts[0], parts[1]);
			
			if (myKey != null) {
				configMap.put(myKey, parts[1].trim());
			} else {
				
				report.add(new Message(Message.ERROR, rb
						.getString("message.wrongValue")
						+ ": \"" + parts[0] + " = " + parts[1] + "\"",
						KBTextInterpreter.KNOWLEDGEBASE_CONFIG, cnt, 0, null));
			}
			cnt++;


		}
		insertAttributes(kb);
		report.note(new Message(this.attributCnt + " "
				+ rb.getString("message.itemsParsed")));
		return report;
	}

	private void insertAttributes(KnowledgeBase kb) {
		Set<String> keys = configMap.keySet();
		for (String key : keys) {
			if (key.equals(KnowledgebaseConfigParser.KEY_AUTHOR)) {
				setAuthor(configMap.get(key), kb);
				report.note(new Message(rb.getString("message.author") + ": "
						+ configMap.get(key)));
				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_TITLE)) {
				setTitle(configMap.get(key), kb);
				report.note(new Message(rb.getString("message.title") + ": "
						+ configMap.get(key)));
				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_VERBALISATION_NO)) {
				setVerbalizationNo(configMap.get(key), kb);
				report.note(new Message(rb
						.getString("message.verbalization.no")
						+ ": " + configMap.get(key)));
				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_VERBALISATION_YES)) {
				setVerbalizationYes(configMap.get(key), kb);
				report.note(new Message(rb
						.getString("message.verbalization.yes")
						+ ": " + configMap.get(key)));

				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_VERBALISATION_UNKNOWN)) {
				setVerbalizationUnknown(configMap.get(key), kb);
				report.note(new Message(rb
						.getString("message.verbalization.unknown")
						+ ": " + configMap.get(key)));

				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_LANGUAGE_EN)) {
				report.note(new Message(rb
						.getString("message.language.english")
						+ ": " + configMap.get(key)));

				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_UNKNOWN_VISIBLE)) {
				//handled by ParserManagement
				report.note(new Message(rb.getString("message.unknownVisible")
						+ ": " + configMap.get(key)));
				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_NEW_DCPARSER)) {
				//handled by ParserManagement
				report.note(new Message(rb.getString("message.newDCParser")
						+ ": " + configMap.get(key)));
				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_ESTABLISH_REFINE)) {
				setEstablishRefine(configMap.get(key), kb);
				report.note(new Message(rb.getString("message.establishRefine")
						+ ": " + configMap.get(key)));
				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_EXCLUDE_DISCARD)) {
				setExcludeDiscard(configMap.get(key), kb);
				report.note(new Message(rb.getString("message.excludeDiscard")
						+ ": " + configMap.get(key)));
				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_RULEBASED_EXCLUSION)) {
				setRuleBasedExclusion(configMap.get(key), kb);
				report.note(new Message(rb.getString("message.ruleBasedSCMExclusion")
						+ ": " + configMap.get(key)));
				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_SCPS_SIMPLE)) {
				setSCProblemSolverSimple(configMap.get(key), kb);
				report.note(new Message(rb.getString("message.scProblemSolverSimple")
						+ ": " + configMap.get(key)));
				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_INDICATION_RULES)) {

				if (configMap.get(key).trim().equals(
//						handled by ParserManagement
						allowedValues.get(key).getAnswers()[0].trim())) {
					configMap.put(
							KnowledgebaseConfigParser.KEY_INDICATION_RULES,
							KnowledgebaseConfigParser.TRUE);
					report.note(new Message(rb
							.getString("message.fullPathIndicationCondition")
							+ ": " + configMap.get(key)));
					this.attributCnt++;
				}
				if (configMap.get(key).trim().equals(
						allowedValues.get(key).getAnswers()[1].trim())) {
//					handled by ParserManagement
					configMap.put(
							KnowledgebaseConfigParser.KEY_INDICATION_RULES,
							KnowledgebaseConfigParser.FALSE);
					report.note(new Message(rb
							.getString("message.fullPathIndicationCondition")
							+ ": " + configMap.get(key)));
					this.attributCnt++;
				}
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_UNIQUE_QUESTION_NAMES)) {
//				handled by ParserManagement
				report.note(new Message(rb
						.getString("message.uniqueQuestionName")
						+ ": " + configMap.get(key)));
				this.attributCnt++;
			}
			if (key.equals(KnowledgebaseConfigParser.KEY_SCLIST_INHERITANCE)) {
//				handled by ParserManagement
				report.note(new Message(rb
						.getString("message.scListInheritance")
						+ ": " + configMap.get(key)));
				this.attributCnt++;
			}

		}
		
		setDefaultValues(kb);
		
	}


	private void setDefaultValues(KnowledgeBase kb) {
		Set keySet = configMap.keySet();
		if(!keySet.contains(KEY_SCPS_SIMPLE)) {
			setSCProblemSolverSimple("true", kb);
			report.note(new Message(rb.getString("message.scProblemSolverSimple")
					+ ": " + "DEFAULT: true"));
		}
		if(!keySet.contains(KEY_RULEBASED_EXCLUSION)) {
			setRuleBasedExclusion("true", kb);
			report.note(new Message(rb.getString("message.ruleBasedSCMExclusion")
					+ ": " + "DEFAULT: true"));
		}
		
	}

	private void setSCProblemSolverSimple(String string, KnowledgeBase kb) {
		kb.getProperties().setProperty(Property.SC_PROBLEMSOLVER_SIMPLE,
				new Boolean(string));
	}

	private void setVerbalizationUnknown(String string, KnowledgeBase kb) {
		kb.getProperties().setProperty(Property.UNKNOWN_VERBALISATION, string);
		
	}

	private void setEstablishRefine(String string, KnowledgeBase kb) {
		kb.getProperties().setProperty(Property.ESTABLISH_REFINE_STRATEGY,
				new Boolean(string));
		
	}
	
	private void setExcludeDiscard(String string, KnowledgeBase kb) {
		kb.getProperties().setProperty(Property.EXCLUDE_DISCARD_STRATEGY,
				new Boolean(string));
	}

	private void setRuleBasedExclusion(String string, KnowledgeBase kb) {
		kb.getProperties().setProperty(Property.RULEBASED_EXCLUSION,
				new Boolean(string));
	}


	private void setVerbalizationNo(String text, KnowledgeBase kb) {
		kb.getProperties().setProperty(Property.NO_VERBALISATION, text);
	}

	private void setVerbalizationYes(String text, KnowledgeBase kb) {
		kb.getProperties().setProperty(Property.YES_VERBALISATION, text);
	}

	private void setTitle(String line, KnowledgeBase kb) {
		DCMarkup dc = kb.getDCMarkup();
		dc.setContent(DCElement.TITLE, line);
		kb.setDCDMarkup(dc);
	}

	private void setAuthor(String text, KnowledgeBase kb) {
		DCMarkup dc = kb.getDCMarkup();
		dc.setContent(DCElement.CREATOR, text);
		kb.setDCDMarkup(dc);
	}

	private String checkIfAllowed(String key, String answer) {

		for (String string : allowedValues.keySet()) {
			ConfigItem it = allowedValues.get(string);
			if (it.getName().trim().equalsIgnoreCase(key.trim())) {
				if (it.containsAnswer("text") || it.containsAnswer(answer)) {
					return string;
				}
			}
		}
		return null;
	}

	public HashMap<String, String> getConfigMap() {
		return configMap;
	}
}
