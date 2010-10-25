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

package de.d3web.we.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.DCElement;
import de.d3web.core.knowledge.terminology.info.DCMarkup;
import de.d3web.core.knowledge.terminology.info.MMInfoObject;
import de.d3web.core.knowledge.terminology.info.MMInfoStorage;
import de.d3web.core.session.Session;
import de.d3web.scoring.Score;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.basic.SessionBroker;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class D3webUtils {

	static ArrayList<String> possibleScorePoints;

	public static List<String> getPossibleScores() {
		if (possibleScorePoints == null) {

			possibleScorePoints = new ArrayList<String>();

			String n = "N";
			String p = "P";
			for (int i = 1; i <= 7; i++) {
				possibleScorePoints.add(n + i);
				possibleScorePoints.add(p + i);
			}
			possibleScorePoints.add("P5x");
			possibleScorePoints.add("N5x");
			possibleScorePoints.add("!");
			possibleScorePoints.add("?");
			possibleScorePoints.add("excluded");
			possibleScorePoints.add("established");
			possibleScorePoints.add("suggested");
		}
		return possibleScorePoints;
	}

	/**
	 * 
	 * Adds a MMInfo DCMarkup to the NamedObject
	 * 
	 * @param o
	 * @param title
	 * @param subject
	 * @param content
	 * @param language
	 */
	public static void addMMInfo(NamedObject o, String title, String subject,
			String content, String language) {
		if (o == null) return;
		if (content == null) return;

		if (content.startsWith("\"") && content.endsWith("\"")
				&& content.length() > 1) {
			content = content.substring(1, content.length() - 1);
		}

		DCMarkup dcm = new DCMarkup();
		dcm.setContent(DCElement.TITLE, title);
		dcm.setContent(DCElement.SUBJECT, subject);
		dcm.setContent(DCElement.SOURCE, o.getId());
		if (language != null) dcm.setContent(DCElement.LANGUAGE, language);
		MMInfoObject mmi = new MMInfoObject(dcm, content);
		MMInfoStorage mmis = (MMInfoStorage) o.getInfoStore().getValue(BasicProperties.MMINFO);
		if (mmis == null) {
			mmis = new MMInfoStorage();
			o.getInfoStore().addValue(BasicProperties.MMINFO, mmis);
		}
		mmis.addMMInfo(mmi);
	}

	/**
	 * Gets the Session Object.
	 */
	public static Session getSession(String topic, KnowWEUserContext user, String web) {

		String sessionId = topic + ".." + KnowWEEnvironment.generateDefaultID(topic);
		SessionBroker broker = D3webModule.getBroker(user.getUserName(), web);
		return broker.getServiceSession(sessionId);
	}

	/**
	 * Gets the Session Object.
	 */
	public static Session getSession(String topic, String user, String web) {

		String sessionId = topic + ".." + KnowWEEnvironment.generateDefaultID(topic);
		SessionBroker broker = D3webModule.getBroker(user, web);
		return broker.getServiceSession(sessionId);
	}

	public static Collection<Session> getSessions(String user, String web) {
		SessionBroker broker = D3webModule.getBroker(user, web);
		return broker.getServiceSessions();
	}

	public static Score getScoreForString(String argument) {
		Score score = null;
		List<Score> allScores = Score.getAllScores();
		for (Score sc : allScores) {
			if (sc.getSymbol().equals(argument)) {
				score = sc;
				break;
			}
		}
		if (argument.equals("!")) {
			score = Score.P7;
		}
		if (argument.equals("?")) {
			score = Score.P5;
		}
		if (argument.equals("excluded")) {
			score = Score.N7;
		}
		if (argument.equals("established")) {
			score = Score.P7;
		}
		if (argument.equals("suggested")) {
			score = Score.P5;
		}

		return score;
	}

	/**
	 * Deletes a terminology object and all potential children from the
	 * knowledge base. Before the deletion the corresponding knowledge instances
	 * (KnowledgeSlices) are also removed.
	 * 
	 * @param object the object to be removed
	 */
	public static void removeRecursively(NamedObject object) {
		for (TerminologyObject to : object.getChildren()) {
			removeRecursively((NamedObject) to);
		}
		try {
			object.getKnowledgeBase().remove(object);
		}
		catch (IllegalAccessException e) {
			// shouldn't happen...
			e.printStackTrace();
		}
	}

}
