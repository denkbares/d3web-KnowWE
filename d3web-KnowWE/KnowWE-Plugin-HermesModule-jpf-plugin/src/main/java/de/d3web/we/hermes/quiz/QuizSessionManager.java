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
		if(s != null) {
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
