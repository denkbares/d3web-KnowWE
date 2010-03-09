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

package de.d3web.KnOfficeParser.txttable;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.KnOfficeParser.KnOfficeParameterSet;
import de.d3web.KnOfficeParser.KnOfficeParser;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.knowledge.terminology.info.MMInfoSubject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.IDObjectManagement;
import de.d3web.report.Message;


public class TxtAttributeTableBuilder extends TxtTableBuilder implements KnOfficeParser {
	
	@Override
	public List<Message> addKnowledge(Reader r, IDObjectManagement idom,
			KnOfficeParameterSet s) {
		
		List<Message> messages = new ArrayList<Message>();
		
		StringBuilder b = new StringBuilder();
		try {
			for (int c; (c = r.read()) != -1;)
				b.append((char) c);
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<TxtTableParserResult> lines = getLines(b.toString());
		
		int parsedAttributes = 0;
		
		for (int i = 0; i < lines.size(); i++) {
			int line = i + 1;
			String lineText = lines.get(i).toString();
			
			List<TxtTableParserResult> cells = getCells(lines.get(i).getContent());
			
			int requiredCellCount = 4;
			if (cells.size() != requiredCellCount) {
				messages.add(MessageKnOfficeGenerator.createErrorMSG("wrongCellCount", null, line, 
						lineText, new Object[] {requiredCellCount, cells.size()}));
			} else {
				boolean lineOk = true;
				
				// retrieve object
				String objectString = cells.get(0).getContent().trim();
				String questionString;
				String answerString;
				
				// parse question and answer
				List<Integer> dots = new ArrayList<Integer>();
				for (int j = 0; j < objectString.length(); j++) {
					if (objectString.charAt(j) == '.'  && !TxtTableParser.isEscapedSymbol(j, objectString)) 
						dots.add(j);
				}
				if (dots.size() == 1) {
					questionString = TxtTableParser.compile(objectString.substring(0, dots.get(0)));
					answerString = TxtTableParser.compile(objectString.substring(dots.get(0) + 1, objectString.length()));
				} else {
					questionString = TxtTableParser.compile(objectString);
					answerString = null;
					if (dots.size() > 1)
						messages.add(MessageKnOfficeGenerator.createWarningMSG("toManyDots", null, line, 
								lineText, TxtTableParser.ESCAPE_SYMBOL));
				}
				
				// search for question in knowledgeBase
				IDObject name = idom.getKnowledgeBase().searchObjectForName(questionString);
				IDObject id = idom.getKnowledgeBase().search(questionString);
				DCMarkup markup = new DCMarkup();
				IDObject kbObject = null;
				
				if (name != null) {
					if (id != null) {
						kbObject = id;
					} else {
						kbObject = name;
					}
				} else {
					if (id != null) {
						kbObject = id;
					} else {
						messages.add(MessageKnOfficeGenerator.createErrorMSG("objectNotFound", null,
								line, lineText, questionString));
						lineOk = false;
					}
				}
				if (lineOk && kbObject instanceof Question && answerString != null) {
					kbObject = idom.findAnswer((Question) kbObject, answerString);
					if (kbObject == null) {
						messages.add(MessageKnOfficeGenerator.createErrorMSG("objectNotFound", null,
								line, lineText, answerString));
						lineOk = false;
					}
				}
				if (lineOk)
					markup.setContent(DCElement.SOURCE, kbObject.getId());
				
				// retrieve MMInfoSubject
				String subjectString = cells.get(1).toString().trim();
				MMInfoSubject foundSubject = null;
				StringBuilder possibleSubjects = new StringBuilder();
				for (MMInfoSubject subject:MMInfoSubject.getSubjects()) {	
					if (subjectString.compareToIgnoreCase(subject.getName()) == 0) {
						foundSubject = subject;
					}
					possibleSubjects.append(subject.getName() + ", ");
				}
				possibleSubjects.delete(possibleSubjects.length() - 2, possibleSubjects.length());
				if (foundSubject == null)  {
					messages.add(MessageKnOfficeGenerator.createErrorMSG("invalidMMInfoSubject", null, line, 
							lineText, subjectString));
					messages.add(MessageKnOfficeGenerator.createNoteMSG("validMMInfoSubjects", null, line, 
							lineText, possibleSubjects.toString()));
					lineOk = false;
				}
				
				// retrieve title and language of DCMarkup
				String dcElementString =  cells.get(2).toString().trim();
				Pattern dcCell = Pattern.compile("^(?:([^ ]*?(lang::)?[^ ]*?(de|en)?) +)?([^ ]+)$", 
						Pattern.CASE_INSENSITIVE);
				Matcher dcMatcher = dcCell.matcher(dcElementString);
				if (dcMatcher.find()) {
					if (dcMatcher.group(1) != null) {
						if (dcMatcher.group(2) != null && dcMatcher.group(3) != null) {
							markup.setContent(DCElement.LANGUAGE, dcMatcher.group(3).toLowerCase());
						} else {
							messages.add(MessageKnOfficeGenerator.createErrorMSG("invalidLanguageSyntax", null, line, 
									lineText, new Object[] {"lang::(de|en)", dcMatcher.group(1)}));
						}
					}
					if (lineOk) {
						markup.setContent(DCElement.TITLE, dcMatcher.group(4));
						markup.setContent(DCElement.SUBJECT, foundSubject.getName());
					}
				} else {
					messages.add(MessageKnOfficeGenerator.createErrorMSG("invalidDCSyntax", null, line, 
							lineText, new Object[] {"[lang::(de|en)] titleOfElement", dcElementString}));
					lineOk = false;
				}
				
				if (!lineOk) continue;
				
				// retrieve data
				String dataString = cells.get(3).toString().trim();
				
				// retrieve MMInfoStorage from kbObject
				MMInfoStorage storage = null;
				if (kbObject instanceof NamedObject) {
					storage = (MMInfoStorage) ((NamedObject) kbObject).getProperties().getProperty(Property.MMINFO);
					if (storage == null) {
						storage = new MMInfoStorage();
						((NamedObject) kbObject).getProperties().setProperty(Property.MMINFO, storage);
					}
				} else if (kbObject instanceof Answer) {
					storage = (MMInfoStorage) ((Answer) kbObject).getProperties().getProperty(Property.MMINFO);
					if (storage == null) {
						storage = new MMInfoStorage();
						((Answer) kbObject).getProperties().setProperty(Property.MMINFO, storage);
					}
				}
				
				// write MMInfoStorage
				storage.addMMInfo(new MMInfoObject(markup, dataString));
				parsedAttributes++;
			}
		}
		
		if (parsedAttributes > 0) {
			messages.add(0, MessageKnOfficeGenerator.createNoteMSG("attributesparsed", null, 0, 
					null, parsedAttributes));
		}
		return messages;
	}


	@Override
	public List<Message> checkKnowledge() {
		List<Message> messages = new ArrayList<Message>();
		// TODO: Anything?
		return messages;
	}
}
