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

package de.d3web.kernel.dialogcontrol.controllers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.MethodKind;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.Session;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.indication.ActionNextQASet;

/**
 * This iterator lists the children of QContainer. It should only be used in
 * MQDialogController <br>
 * Creation date: (20.02.2001 15:53:12)
 * 
 * @author Norman Brümmer
 */
public class QContainerIterator {

	private Session session = null;

	private Iterator<TerminologyObject> childIter = null;

	private Question currentQuestion = null;

	private QContainer container = null;
	private List<QuestionModel> flatList = null;

	private List<Question> tempQuestions = null;

	/**
	 * this class encapsulates a Question with the current case and an
	 * indentation for displaying in HTML/XML
	 */
	public class QuestionModel {

		private Session session = null;
		private Question question = null;
		private int indentation = 0;
		private String nextAnchor = null;
		private String nextAnchorHref = null;

		QuestionModel(Session _session, Question q, int indent) {
			session = _session;
			question = q;
			indentation = indent;
		}

		public Session getSession() {
			return session;
		}

		public Question getQuestion() {
			return question;
		}

		public int getIndentation() {
			return indentation;
		}

		public String getNextAnchor() {
			return nextAnchor;
		}

		public String getNextAnchorHref() {
			return nextAnchorHref;
		}

		@Override
		public String toString() {
			return question.getId() + ", " + nextAnchor + ", " + nextAnchorHref;
		}

		void setNextAnchor(String a) {
			nextAnchor = a;
		}

		void setNextAnchorHref(String h) {
			nextAnchorHref = h;
		}

	}

	/**
	 * creates a new QContainerIterator
	 * 
	 * @param container the Root-Container
	 * @param session current Session
	 */
	public QContainerIterator(Session session, QContainer container) {
		this.session = session;
		List<TerminologyObject> children = Arrays.asList(container.getChildren());
		childIter = children.iterator();
		this.container = container;

	}

	/**
	 * Helper method to determine follow questions of the given Question q
	 * without duplicates. They will be determined by searching in the Actions
	 * of "next qaset rules" and in the terminal objects of its Contition
	 * 
	 * @return List of follow questions of q
	 */
	public static List<QASet> createFollowList(Session session, Question q) {
		// two-dimensional list: elements are terminal-object-lists of the
		// different rules
		List<List<QASet>> follow2d = new LinkedList<List<QASet>>();

		Iterator<? extends PSMethod> psmethod = session.getPSMethods().iterator();
		while (psmethod.hasNext()) {

			PSMethod p = psmethod.next();

			KnowledgeSlice kslices = q.getKnowledge(p.getClass(), MethodKind.FORWARD);
			if (kslices != null && kslices instanceof RuleSet) {
				RuleSet rs = (RuleSet) kslices;
				for (Rule rule : rs.getRules()) {

					if (rule.getAction() instanceof ActionNextQASet) {
						Iterator<? extends NamedObject> termiter = rule.getCondition().getTerminalObjects().iterator();
						while (termiter.hasNext()) {
							if (termiter.next().equals(q)) {
								// follows of the same action have to stay
								// in the given order
								List<QASet> ruleFollows = new LinkedList<QASet>();

								Iterator<QASet> qiter = ((ActionNextQASet) rule.getAction())
										.getQASets().iterator();
								while (qiter.hasNext()) {
									QASet o = qiter.next();
									if ((o instanceof Question)
											&& (!ruleFollows.contains(o))) {
										ruleFollows.add(o);
									}
								}
								if (!ruleFollows.isEmpty()) {
									follow2d.add(ruleFollows);
								}
							}
						}

					}
				}
			}
		}
		// put the follows from different rules in the right order
		return getSortedFollows(follow2d, q);
	}

	/**
	 * Returns a list, that contains all objects which are contained by the
	 * "leaf-lists" of "activeFollows2d". The order of the elements within the
	 * leaf-lists is kept, the order of the first-level-lists (the
	 * rules-follow-lists) could have changed in order to obtain the arrangement
	 * of the children of q.
	 */
	private static List<QASet> getSortedFollows(List<List<QASet>> activeFollows2d, Question q) {
		List<QASet> result = new LinkedList<QASet>();
		List<List<QASet>> follows2d = new LinkedList<List<QASet>>(activeFollows2d);

		// try to keep the order of q.getChildren-list
		// (as measured by the first element of the rule-follow-lists)
		for (TerminologyObject to : q.getChildren()) {
			QASet child = (QASet) to;

			// iterate through rule-follow-lists, until a list is found which
			// starts
			// with the current child
			Iterator<List<QASet>> follow2dIter = follows2d.iterator();
			while (follow2dIter.hasNext()) {
				List<QASet> ruleFollows = follow2dIter.next();
				if (child.equals(ruleFollows.get(0))) {

					// add all follow-qasets to the result unless they are
					// already contained
					Iterator<QASet> ruleFollowIter = ruleFollows.iterator();
					while (ruleFollowIter.hasNext()) {
						QASet follow = ruleFollowIter.next();
						if (!result.contains(follow)) {
							result.add(follow);
						}
					}
					follow2dIter.remove();
					break;
				}
			}
		}

		// if there are some remaining rule-follow-lists, add their qasets in
		// the end
		Iterator<List<QASet>> follow2dIter = follows2d.iterator();
		while (follow2dIter.hasNext()) {
			List<QASet> ruleFollows = follow2dIter.next();
			Iterator<QASet> ruleFollowIter = ruleFollows.iterator();
			while (ruleFollowIter.hasNext()) {
				QASet follow = ruleFollowIter.next();
				if (!result.contains(follow)) {
					result.add(follow);
				}
			}
		}
		return result;
	}

	private void flatten(int depth, Question q, Iterator<TerminologyObject> children) {

		currentQuestion = q;
		QASetManager m =
				QASetManagerManagement.getInstance().getQASetManager(session);

		if ((((DialogController) m).isValidForDC(q) || isDone(session, q))
				&& !tempQuestions.contains(q)) {

			QuestionModel qmod = new QuestionModel(session, q, depth);
			flatList.add(qmod);
			tempQuestions.add(q);
		}

		if (hasNextFollow()) {
			Iterator<TerminologyObject> follows = new LinkedList<TerminologyObject>(
					createFollowList(session, q)).iterator();
			// must be a Question here, has been checked earlier...
			Question newQ = (Question) follows.next();
			flatten(depth + 1, newQ, follows);
		}
		else {
			// System.out.println(q.getId() + " has no more follow");
		}

		if (children.hasNext()) {
			Question newQ = (Question) children.next();
			flatten(depth, newQ, children);
		}

	}

	/**
	 * @return a list of all questions
	 */
	public List<Question> getAllQuestions() {
		getQuestionModels();
		return tempQuestions;
	}

	/**
	 * Creation date: (20.02.2001 16:00:11)
	 * 
	 * @return the next child of the current QContainer
	 */
	public Question getNextChild() {
		if (hasNextChild()) {
			QASet qaSet = (QASet) childIter.next();
			if (qaSet instanceof Question) {
				currentQuestion = (Question) qaSet;
			}
			else {
				/**
				 * [HOTFIX]: georg: This is only, to avoid an exception, if a
				 * container has questions _and_ containers as children. Now,
				 * this method may return null even after "hasNextChild"
				 * returned true.
				 */
				return getNextChild();
			}
			return currentQuestion;
		}

		return null;

	}

	/**
	 * Creation date: (21.02.2001 16:09:11)
	 * 
	 * @return the next follow QAset of the current QASet
	 */
	public Question getNextFollow() {
		if (hasNextFollow()) {
			List<TerminologyObject> followList = new LinkedList<TerminologyObject>(
					createFollowList(session, currentQuestion));
			childIter = followList.iterator();
			currentQuestion = (Question) childIter.next();
			return currentQuestion;
		}
		return null;
	}

	/**
	 * @return a flat List of all determined Questions encapsulated in
	 *         QuestionModel objects
	 */
	public List<QuestionModel> getQuestionModels() {

		if (flatList == null) {
			flatList = new LinkedList<QuestionModel>();

			tempQuestions = new LinkedList<Question>();

			Iterator<TerminologyObject> childIterSave = childIter;
			Question currentQuestionSave = currentQuestion;

			childIter = Arrays.asList(container.getChildren()).iterator();

			Question topQuestion = (Question) childIter.next();
			flatten(0, topQuestion, childIter);

			childIter = childIterSave;
			currentQuestion = currentQuestionSave;
		}

		// building nextAnchors:

		Iterator<QuestionModel> iter = flatList.iterator();

		QuestionModel lastSet = null;
		int i = 0;

		while (iter.hasNext()) {
			QuestionModel model = iter.next();
			if (!isDone(session, model.getQuestion())) {
				if (i == 0) {
					model.setNextAnchor("next");
					i++;
				}
				else {
					model.setNextAnchor("next" + i++);
				}
				model.setNextAnchorHref("#next" + i);
				lastSet = model;
			}
		}

		// now model is the last unanswered question (if not null)

		if (lastSet != null) {
			lastSet.setNextAnchorHref(null);
		}

		return flatList;
	}

	/**
	 * @return true, if the current QASet has a child that has not been
	 *         encountered yet
	 */
	public boolean hasNextChild() {

		return childIter.hasNext();
	}

	/**
	 * @return true, if the current Question has any following question
	 */
	public boolean hasNextFollow() {
		if (currentQuestion == null) return false;
		else return !createFollowList(session, currentQuestion).isEmpty();
	}

	private boolean isDone(Session session, QASet object) {
		if (object instanceof Question) {
			return UndefinedValue.isNotUndefinedValue(session.getBlackboard().getValue(
					(Question) object));
		}
		else if (object instanceof QContainer) {
			return session.getInterview().isActive(object);
		}
		else return false;
	}
}