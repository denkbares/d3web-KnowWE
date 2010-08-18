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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.Rule;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.session.Session;
import de.d3web.core.session.values.UndefinedValue;

/**
 * one-question-controller class for the dialog; based on MQDialogController
 * asks one question at a time
 * 
 * @see MQDialogController
 * @author Georg Buscher
 * @author Norman Brümmer
 */
public class OQDialogController implements DialogController {

	private List<QASet> history = null;
	private int historyCursor = -1;

	private final MQDialogController mqdc;
	private final Session session;

	public OQDialogController(Session session) {
		mqdc = new MQDialogController(session);
		history = new LinkedList<QASet>();
		this.session = session;
		QASetManagerManagement.getInstance().setQASetManager(session, this);
		moveToNewestQASet();
	}

	public MQDialogController getMQDialogController() {
		return mqdc;
	}

	@Override
	public QASet getCurrentQASet() throws InvalidQASetRequestException {
		if (historyCursor < 0) {
			throw new InvalidPreviousQASetRequestException();
		}
		else if (historyCursor >= history.size()) {
			throw new InvalidNextQASetRequestException();
		}
		else {
			return history.get(historyCursor);
		}
	}

	/**
	 * @return the internal history.
	 */
	public List<QASet> getHistory() {
		return history;
	}

	/**
	 * hasPreviousQASet method comment.
	 */
	@Override
	public boolean hasPreviousQASet() {
		int historyC = historyCursor;
		boolean result = findPreviousUnblockedOnHistory() != null;
		historyCursor = historyC;
		return result;
	}

	/**
	 * @return true if there is a next question on history or an unanswered
	 *         question
	 */
	@Override
	public boolean hasNextQASet() {
		int historyC = historyCursor;
		boolean result = (findNextUnblockedOnHistory() != null) || (hasNewestQASet());
		historyCursor = historyC;
		return result;
	}

	/**
	 * checks if there is any unanswered question (looking forwards)
	 */
	@Override
	public boolean hasNewestQASet() {
		return mqdc.hasNextQASet();
	}

	@Override
	public boolean isValidForDC(QASet qaSet) {
		return mqdc.isValidForDC(qaSet);
	}

	@Override
	public QASet moveToNextRemainingQASet() {
		return mqdc.moveToNextRemainingQASet();
	}

	@Override
	public List<?> getProcessedContainers() {
		return mqdc.getProcessedContainers();
	}

	@Override
	public List<? extends QASet> getQASetQueue() {
		return mqdc.getQASetQueue();
	}

	/**
	 * moves to the next unanswered QASet
	 * 
	 * @return the QASet the controller has moved to
	 */
	@Override
	public QASet moveToNewestQASet() {
		QContainer qc = (QContainer) mqdc.moveToNewestQASet();
		if (qc != null) {
			Iterator<?> questionIter =
					mqdc.getAllQuestionsToRender(qc)
							.iterator();
			while (questionIter.hasNext()) {
				Question q = (Question) ((List<?>) questionIter.next()).get(0);
				if (!isDone(session, q)) {
					if ((history.isEmpty()) || (!history.get(history.size() - 1).equals(q))) {
						history.add(q);
					}
					historyCursor = history.size() - 1;
					return q;
				}
			}
		}
		historyCursor = history.size();
		return null;
	}

	/**
	 * moves to the next QASet on history.<br>
	 * ATTENTION: moveToNextQASet and moveToPreviousQASet may not work
	 * correctly, if follow-questions are indicated.
	 * 
	 * @return the QASet the controller moved to
	 */
	@Override
	public QASet moveToNextQASet() {
		QASet next = findNextUnblockedOnHistory();
		if (next != null) {
			return next;
		}
		else {
			return moveToNewestQASet();
		}
	}

	/**
	 * moves to the previous QASet on history.<br>
	 * ATTENTION: moveToNextQASet and moveToPreviousQASet may not work
	 * correctly, if follow-questions are indicated.
	 * 
	 * @return the QASet the controller moved to
	 */
	@Override
	public QASet moveToPreviousQASet() {
		QASet q = findPreviousUnblockedOnHistory();
		if (q != null) {
			mqdc.addUserIndicationQASet(q);
			mqdc.moveToNewestQASet();
		}
		return q;
	}

	@Override
	public QASet moveToQASet(QASet searchQ) {
		return mqdc.moveToQASet(searchQ);
	}

	@Override
	public QASet moveToQuestion(QASet searchQ) {
		return mqdc.moveToQASet(searchQ);
	}

	private synchronized Question findNextUnblockedOnHistory() {
		if (history.size() > 0) {
			while (historyCursor < history.size() - 1) {
				historyCursor++;
				Question q = checkUnblocked();
				if (q != null) {
					return q;
				}
			}
			historyCursor = history.size();
		}
		return null;
	}

	private Question findPreviousUnblockedOnHistory() {
		if (history.size() > 0) {
			while (historyCursor > 0) {
				historyCursor--;
				Question q = checkUnblocked();
				if (q != null) {
					return q;
				}
			}
			historyCursor = -1;
		}
		return null;
	}

	/**
	 * @return a question, if it is valid.
	 */
	private Question checkUnblocked() {
		Question tempQ = (Question) history.get(historyCursor);

		// List<Reason> proList = getProReasonsOfParent(session, tempQ);

		if (session.getInterview().isActive(tempQ)) {
			return tempQ;
			// // question valid but answered (of course).
			// // check if there are proper pro-reasons:
			// // - ActionNextQASet
			// // - InitQASets
			// // - User-Activated
			//
			// Iterator<Reason> proIter = proList.iterator();
			// while (proIter.hasNext()) {
			// Reason pro = proIter.next();
			// if (isQASetRule(pro)
			// || PSMethodInit.class.equals(pro.getProblemSolverContext())
			// ||
			// PSMethodUserSelected.class.equals(pro.getProblemSolverContext()))
			// {
			// return tempQ;
			// }
			// }
		}
		return null;
	}

	// private List<Reason> getProReasonsOfParent(Session session, Question q) {
	// List<Reason> proReasons = new LinkedList<Reason>();
	// for (TerminologyObject to: q.getParents()) {
	// QASet qaSet = (QASet) to;
	// proReasons.addAll(qaSet.getProReasons(session));
	// }
	// return proReasons;
	// }

	// /**
	// * @return true, if the specified rule contains the action ActionNextQASet
	// */
	// private boolean isQASetRule(QASet.Reason reason) {
	// if (reason.getRule() != null) {
	// PSAction action = reason.getRule().getAction();
	// return (action instanceof de.d3web.indication.ActionNextQASet);
	// } else
	// return false;
	// }

	@Override
	public void propagate(NamedObject no, Rule rule, PSMethod psm) {
		mqdc.propagate(no, rule, psm);
	}

	@Override
	public MQDialogController getMQDialogcontroller() {
		return mqdc;
	}

	@Override
	public OQDialogController getOQDialogcontroller() {
		return this;
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
