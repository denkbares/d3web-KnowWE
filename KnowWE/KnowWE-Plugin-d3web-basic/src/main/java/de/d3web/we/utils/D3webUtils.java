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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.session.Session;
import de.d3web.scoring.Score;
import de.d3web.we.basic.D3webKnowledgeHandler;
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
	public static void removeRecursively(TerminologyObject object) {
		for (TerminologyObject to : object.getChildren()) {
			removeRecursively(to);
		}
		object.destroy();
	}

	/**
	 * Utility method to get a {@link KnowledgeBase} from an article
	 * 
	 * @created 15.12.2010
	 * @param web
	 * @param topic
	 * @return
	 */
	public static KnowledgeBase getKB(String web, String topic) {
		if (web == null || topic == null) {
			throw new IllegalArgumentException("Argument 'web' and/or 'topic' was null!");
		}
		D3webKnowledgeHandler knowledgeHandler =
				D3webModule.getKnowledgeRepresentationHandler(web);
		if (knowledgeHandler != null) {
			KnowledgeBaseUtils kbm = knowledgeHandler.getKBM(topic);
			if (kbm != null) {
				return kbm.getKnowledgeBase();
			}
		}
		return null;
	}
}
