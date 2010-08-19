/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.hermes.quiz;

import java.util.HashMap;
import java.util.Map;

public class QuizSessionManager {

	private static QuizSessionManager instance;

	public static QuizSessionManager getInstance() {
		if (instance == null) {
			instance = new QuizSessionManager();

		}

		return instance;
	}

	public QuizSession createSession(String user, Integer from, Integer to) {
		sessions.put(user, new QuizSession(user, from, to));
		return sessions.get(user);
	}

	public void stopSession(String user) {
		QuizSession s = sessions.get(user);
		if (s != null) {
			s.setStopped(true);
		}
	}

	public QuizSession getSession(String user) {
		return sessions.get(user);
	}

	private final Map<String, QuizSession> sessions = new HashMap<String, QuizSession>();

	public void setAnswer(String user, int answer) {
		QuizSession session = this.sessions.get(user);
		session.setAnswer(answer);
	}
}
