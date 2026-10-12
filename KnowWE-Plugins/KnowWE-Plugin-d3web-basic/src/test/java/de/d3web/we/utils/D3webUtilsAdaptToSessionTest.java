/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.utils;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.denkbares.plugin.test.InitPluginManager;
import de.d3web.core.inference.condition.CondEqual;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.QuestionText;
import de.d3web.core.knowledge.terminology.Rating;
import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.manage.RuleFactory;
import de.d3web.core.session.Session;
import de.d3web.core.session.SessionFactory;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.blackboard.FactFactory;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.scoring.Score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Tests setting facts into a session, that are created for objects of another knowledge base than the session's one
 * (e.g. of the compiler's current knowledge base, while a pinned session still uses a previous one).
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 12.10.2026
 */
public class D3webUtilsAdaptToSessionTest {

	@BeforeClass
	public static void initPlugins() throws IOException {
		InitPluginManager.init();
	}

	@Test
	public void keepsFactOfSessionKnowledgeBase() {
		KnowledgeBase base = createKnowledgeBase(false);
		Session session = SessionFactory.createSession(base);
		Fact fact = createYesFact(base);
		assertSame(fact, D3webUtils.adaptToSession(fact, session));
	}

	@Test
	public void adaptsFactToObjectOfSessionKnowledgeBase() {
		KnowledgeBase sessionBase = createKnowledgeBase(false);
		Session session = SessionFactory.createSession(sessionBase);
		Fact fact = createYesFact(createKnowledgeBase(false));

		Fact adapted = D3webUtils.adaptToSession(fact, session);
		assertSame(sessionBase.getManager().searchQuestion("Question"), adapted.getTerminologyObject());
		assertEquals(fact.getValue(), adapted.getValue());
		assertSame(fact.getPSMethod(), adapted.getPSMethod());
		assertSame(fact.getSource(), adapted.getSource());
	}

	@Test
	public void returnsNullIfObjectIsMissingInSessionKnowledgeBase() {
		KnowledgeBase sessionBase = KnowledgeBaseUtils.createKnowledgeBase();
		// equally named object of another class
		new QuestionText(sessionBase.getRootQASet(), "Question");
		Session session = SessionFactory.createSession(sessionBase);
		assertNull(D3webUtils.adaptToSession(createYesFact(createKnowledgeBase(false)), session));

		Session emptySession = SessionFactory.createSession(KnowledgeBaseUtils.createKnowledgeBase());
		assertNull(D3webUtils.adaptToSession(createYesFact(createKnowledgeBase(false)), emptySession));
	}

	@Test
	public void setsFindingForObjectOfSessionKnowledgeBase() {
		KnowledgeBase sessionBase = createKnowledgeBase(false);
		Session session = SessionFactory.createSession(sessionBase);
		// the other knowledge base has a rule establishing the solution, the session's one has not
		Fact fact = createYesFact(createKnowledgeBase(true));

		D3webUtils.setFindingSynchronized(fact, session, null);

		// the rules of the other knowledge base must not be applied to the session
		Solution solution = sessionBase.getManager().searchSolution("Solution");
		assertEquals(Rating.State.UNCLEAR, session.getBlackboard().getRating(solution).getState());
		QuestionOC question = (QuestionOC) sessionBase.getManager().searchQuestion("Question");
		assertSame(question, session.getBlackboard().getValueFact(question).getTerminologyObject());
		assertEquals(fact.getValue(), session.getBlackboard().getValue(question));
	}

	private static KnowledgeBase createKnowledgeBase(boolean withRule) {
		KnowledgeBase base = KnowledgeBaseUtils.createKnowledgeBase();
		QuestionOC question = new QuestionOC(base.getRootQASet(), "Question", "yes", "no");
		Solution solution = new Solution(base.getRootSolution(), "Solution");
		if (withRule) {
			RuleFactory.createHeuristicPSRule(solution, Score.P7,
					new CondEqual(question, new ChoiceValue(question.getAllAlternatives().get(0))));
		}
		return base;
	}

	private static Fact createYesFact(KnowledgeBase base) {
		QuestionOC question = (QuestionOC) base.getManager().searchQuestion("Question");
		return FactFactory.createUserEnteredFact(question, new ChoiceValue(question.getAllAlternatives().get(0)));
	}
}
