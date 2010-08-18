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
import java.util.logging.Logger;

import de.d3web.abstraction.inference.PSMethodAbstraction;
import de.d3web.core.inference.KnowledgeSlice;
import de.d3web.core.inference.MethodKind;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.inference.PSMethodInit;
import de.d3web.core.inference.Rule;
import de.d3web.core.inference.RuleSet;
import de.d3web.core.inference.condition.NoAnswerException;
import de.d3web.core.inference.condition.UnknownAnswerException;
import de.d3web.core.knowledge.TerminologyObject;
import de.d3web.core.knowledge.terminology.DerivationType;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QASet;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.session.Session;
import de.d3web.core.session.blackboard.Fact;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.indication.inference.PSMethodNextQASet;
import de.d3web.indication.inference.PSMethodUserSelected;

/**
 * Multiple Question Dialog Controller <br>
 * Creation date: (21.02.2001 10:30:15)
 * 
 * @author Georg Buscher
 */
public class MQDialogController implements DialogController {

	private List<? extends QASet> initQASets = null; // contains the
	// init-QContainers at
	// the
	// beginning
	private List<? extends QASet> qasetQueue = null; // contains all QContainers
	// which have to be
	// presented
	private List history = null; // contains QContainers in the order in which
	// they have been presented
	private List<QContainer> processedContainers = null; // contains all
	// QContainers that
	// have been (partially) answered
	private List<QASet> userIndicationList = null; // contains QContainers that
	// currently are indicated by the
	// user
	// (they may have indication-rules, so don't have to be user-selected)
	private List userSelectedQASets = null; // contains all currently
	// user-selected QASets
	// (QASets that don't have indication-rules except PSMethodUserSelected)

	protected int historyCursor = -1;

	// for "moveToNextRemaingQASet" only
	private List remainingQASetQueue = null;
	private boolean obtainRemainingQASetStatus = false;

	protected Session session = null;

	/**
	 * MQDialogController constructor comment.
	 */
	public MQDialogController(Session _session) {
		this(_session, new LinkedList());
	}

	/**
	 * new MQDialogController that will be initialized with the given list of
	 * initQASets. (attention: the case might add some more initQASets that are
	 * specified in the knowledge base)
	 */
	public MQDialogController(Session _session, List initQASets) {
		session = _session;
		qasetQueue = new LinkedList();
		this.initQASets = new LinkedList(initQASets);
		userIndicationList = new LinkedList();
		userSelectedQASets = new LinkedList();
		history = new LinkedList();
		processedContainers = new LinkedList();

		QASetManagerManagement.getInstance().setQASetManager(session, this);

		moveToNewestQASet();
	}

	/**
	 * Adds the new QASet considering the priorities of QContainers.
	 * 
	 * Creation date: (08.06.2002 18:42:38)
	 * 
	 * @param q de.d3web.kernel.domainModel.QASet
	 */
	private void addQASet(List qaSetList,
			de.d3web.core.knowledge.terminology.QASet q) {
		QContainer toAdd = null;
		if (q instanceof QContainer) {
			toAdd = (QContainer) q;
		}
		else if (q instanceof Question) {
			toAdd = getFirstLogicalContainerParent((Question) q);
		}
		if (toAdd != null) {
			// insert into qaSetQueue
			addContainer(qaSetList, toAdd, toAdd.getPriority());
			if (obtainRemainingQASetStatus) {
				// insert into remainingQASetQueue, if the remaining qaSets are
				// currently obtained
				addContainer(remainingQASetQueue, toAdd, toAdd.getPriority());
			}
		}
	}

	/**
	 * Adds the new QContainer into the "containerList" considering the
	 * priorities of QContainers (lowest priority on top).
	 * 
	 * Creation date: (11.10.2002 18:42:38)
	 * 
	 * @param q de.d3web.kernel.domainModel.QContainer
	 * @param priority Integer
	 */
	private static void addContainer(List containerList, QContainer q,
			Integer priority) {
		if (!containerList.contains(q)) {

			// bei Frageklassenoberbegriff die children-Frageklassen anhängen
			boolean hasQuestions = false;
			for (TerminologyObject to : q.getChildren()) {
				if (to instanceof QContainer) {
					addContainer(containerList, (QContainer) to,
							((QContainer) to).getPriority());
				}
				else if (to instanceof Question) {
					hasQuestions = true;
				}
			}

			if (hasQuestions) {
				// add the new QContainer

				if (priority != null) {
					QASet tempQA;
					Iterator queueIter = containerList.iterator();
					int i = 0;
					boolean added = false;

					// insert into the containerList considering the priority
					// (lowest priority on top)
					while ((queueIter.hasNext()) && (!added)) {

						tempQA = (QASet) queueIter.next();
						if ((tempQA instanceof QContainer)
								&& ((((QContainer) tempQA).getPriority() == null) || (priority
										.compareTo(((QContainer) tempQA)
												.getPriority()) < 0))) {
							containerList.add(i, q);
							added = true;
						}
						i++;

					}

					if (!added) {
						containerList.add(q);
					}
				}
				else {
					containerList.add(q);
				}
			}

		}
	}

	/**
	 * Adds "q" to the current history position and shifts the last "current
	 * QASet" (if any) to the right. Creation date: (27.02.2001 12:58:41)
	 * 
	 * @param q de.d3web.kernel.domainModel.QASet
	 */
	protected void addToCurrentHistoryPos(QASet q) {
		if (historyCursor < 0) historyCursor = 0;
		if (historyCursor > history.size()) historyCursor = history.size();
		history.add(historyCursor, q);
		if ((q instanceof QContainer) && (!processedContainers.contains(q))) {
			processedContainers.add((QContainer) q);
		}
		// move the container that was current before to the qaSetQueue
		if (history.size() > historyCursor + 1) {
			addQASet(qasetQueue, (QASet) history.get(historyCursor + 1));
			history.remove(historyCursor + 1);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (20.02.2001
	 * 15:55:25)
	 * 
	 * @return de.d3web.kernel.domainModel.QContainer
	 */
	@Override
	public QASet getCurrentQASet() {

		if ((historyCursor < 0) || (historyCursor >= history.size())) return null;
		else return (QASet) history.get(historyCursor);

	}

	/**
	 * Insert the method's description here. Creation date: (11.01.2001
	 * 14:47:47)
	 * 
	 * @return java.util.List
	 */
	@Override
	public List<? extends QASet> getQASetQueue() {
		return qasetQueue;
	}

	public List getHistory() {
		return history;
	}

	protected List getInitQASets() {
		return initQASets;
	}

	protected List getUserIndicationList() {
		return userIndicationList;
	}

	/**
	 * @return List of all QContainers, that have been (partially) processed
	 *         during answering the case (system-indicated and user-selected
	 *         ones)
	 */
	@Override
	public List getProcessedContainers() {
		if ((getCurrentQASet() != null)
				&& (!processedContainers.contains(getCurrentQASet()))) {
			processedContainers.add((QContainer) getCurrentQASet());
		}
		Iterator iter = processedContainers.iterator();
		while (iter.hasNext()) {
			QContainer c = (QContainer) iter.next();
			if (nothingIsDoneInContainer(c) || (!isIndicated(c))) {
				iter.remove();
			}
		}
		return processedContainers;
	}

	/**
	 * hasNewestQASet method comment.
	 */
	@Override
	public boolean hasNewestQASet() {
		return (hasNewestContainer());
	}

	/**
	 * hasNextQASet method comment.
	 */
	@Override
	public boolean hasNextQASet() {
		return (hasNewestContainer());
	}

	/**
	 * Returns, if there is a container to show (with valid questions).
	 */
	private boolean hasNewestContainer() {

		// first look at user-selected containers
		if (userIndicationList.size() > 0) {
			return true;
		}

		// if there isn't any user-selected container:

		// is the case aborted due to single fault assumption?
		Boolean abortCaseSFA = (Boolean) session.getProperties().getProperty(
				Property.HDT_ABORT_CASE_SFA);
		if ((abortCaseSFA != null) && (abortCaseSFA.booleanValue())) {
			return false;
		}

		// second, look at the current container
		if (isValidForDC(getCurrentQASet())) {
			return true;
		}

		// third, look at the initQASets
		Iterator initIter = initQASets.iterator();
		while (initIter.hasNext()) {
			QASet qaSet = (QASet) initIter.next();
			if (isValidForDC(qaSet)) {
				return true;
			}
		}

		// fourth, look at the containers in the qaSetQueue
		Iterator qaSetIter = getQASetQueue().iterator();
		while (qaSetIter.hasNext()) {
			QASet qaSet = (QASet) qaSetIter.next();
			if (isValidForDC(qaSet)) {
				return true;
			}
		}

		// else, there is no container to show
		return false;
	}

	/**
	 * hasPreviousQASet method comment.
	 */
	@Override
	public boolean hasPreviousQASet() {
		return historyCursor > 0;
	}

	/**
	 * Insert the method's description here. Creation date: (22.02.2001
	 * 13:12:22)
	 * 
	 * @return boolean
	 * @param q de.d3web.kernel.domainModel.QASet
	 */
	@Override
	public boolean isValidForDC(QASet q) {
		return (isValidForDC(q, new LinkedList()));
	}

	private boolean isValidForDC(QASet q, List processedQASets) {
		if ((q == null) || (processedQASets.contains(q))) {
			return false;
		}

		// List pros = q.getProReasons(session);
		// List cons = q.getContraReasons(session);

		boolean active = (session.getInterview().isActive(q));

		// user-selection outweight any contrareason!
		if (active) {
			if (!isDone(session, q)) {
				// if there are pro-reasons and and (some) question(s) are not
				// answered
				return true;
			}
			else {
				if (q instanceof Question) {
					// go through all follow-questions
					Iterator iter = QContainerIterator.createFollowList(
							session, (Question) q).iterator();
					// to avoid cycles:
					processedQASets.add(q);
					while (iter.hasNext()) {
						QASet follow = (QASet) iter.next();
						if (follow instanceof Question) {
							Question followQ = (Question) follow;
							if (isValidForDC(followQ, processedQASets)) {
								QASet logicalParent = getFirstLogicalParent(followQ);
								if ((logicalParent == null)
										|| (logicalParent.equals(q))) {
									// a follow-question is valid
									processedQASets.remove(processedQASets
											.size() - 1);
									return (true);
								}
							}
						}
						else {
							if (isValidForDC(follow, processedQASets)) {
								// a follow-container is valid
								Logger
										.getLogger(this.getClass().getName())
										.warning(
												"how can that be? : container "
														+ follow.getId()
														+ " is follow of question "
														+ q.getId());
								processedQASets
										.remove(processedQASets.size() - 1);
								return (true);
							}
						}
					}
					// no valid follow-question
					processedQASets.remove(processedQASets.size() - 1);
					return false;
				}
				else {
					// to avoid cycles:
					processedQASets.add(q);
					// go through all children
					for (TerminologyObject to : q.getChildren()) {
						if (isValidForDC((QASet) to, processedQASets)) {
							// a child is valid
							processedQASets.remove(processedQASets.size() - 1);
							return true;
						}
					}
					if ((session.getKnowledgeBase().getInitQuestions()
							.contains(q))
							&& (!isDone(session, q))) {
						return true;
					}
					// container is not indicated or no child is valid
					return (false);
				}
			}
		}
		else {
			// there are contra-reasons against this QASet
			return (false);
		}

	}

	/**
	 * Returns true, if the given container or any child of it is indicated.
	 * 
	 * @param considerQuestionsAsChildren determines, if the quesiton-children
	 *        of a container shall also be considered (if false, only
	 *        children-containers will be considered).
	 */
	public boolean isIndicatedOrHasIndicatedChild(QContainer container,
			boolean considerQuestionsAsChildren) {
		return isIndicatedOrHasIndicatedChild(container, new LinkedList(),
				considerQuestionsAsChildren);
	}

	/**
	 * Returns true, if the given QASet is indicated.
	 */
	private boolean isIndicated(QASet q) {
		return session.getInterview().isActive(q);

		// List pros = q.getProReasons(session);
		// List cons = q.getContraReasons(session);
		//
		// // user-selection outweight any contrareason!
		// if (((cons.isEmpty()) && (!pros.isEmpty()))
		// || (pros.contains(new QASet.Reason(null,
		// PSMethodUserSelected.class)))) {
		// return true;
		// }
		// return false;
	}

	/**
	 * Returns true, if the given container or any child of it is indicated.
	 * 
	 * @param c (QContainer to test)
	 * @param processedQASets (to avoid cycles)
	 * @param considerQuestionsAsChildren determines, if the quesiton-children
	 *        of a container shall also be considered (if false, only
	 *        children-containers will be considered).
	 * @return boolean
	 */
	private boolean isIndicatedOrHasIndicatedChild(QContainer c,
			List processedQASets, boolean considerQuestionsAsChildren) {
		if ((c == null) || (processedQASets.contains(c))) {
			return false;
		}
		if (isIndicated(c)) {
			return true;
		}
		else if (c.getChildren().length != 0) {
			if (c.getChildren()[0] instanceof Question) {
				if (considerQuestionsAsChildren
						&& somethingIsDoneInContainer(c)) {
					return true;
				}
			}
			else {
				processedQASets.add(c);
				// go through all container-children
				for (TerminologyObject to : c.getChildren()) {
					if (to instanceof QContainer) {
						if (isIndicatedOrHasIndicatedChild((QContainer) to,
								processedQASets, considerQuestionsAsChildren)) {
							return true;
						}
					}
					else {
						if (to instanceof Question) {
							if (considerQuestionsAsChildren
									&& somethingIsDoneInContainer(c)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns true, if the given container has any child, that is valid.
	 * (children of all levels will be considered, not only the next level)
	 * 
	 * @return boolean
	 */
	public boolean isAnyChildValid(QContainer c) {
		for (TerminologyObject q : c.getChildren()) {
			if (q instanceof QContainer) {
				QContainer childC = (QContainer) q;
				if ((isValidForDC(childC)) || (isAnyChildValid(childC))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * returns a QASet, which is logical parent of the given "followQuestion"
	 * (this is in general the first TerminalObject of the first active
	 * derivation-rule)
	 */
	public QASet getFirstLogicalParent(Question followQuestion) {
		KnowledgeSlice knowledge = followQuestion.getKnowledge(
				PSMethodNextQASet.class, MethodKind.BACKWARD);
		if (knowledge == null) {
			knowledge = followQuestion.getKnowledge(PSMethodAbstraction.class,
					MethodKind.BACKWARD);
		}
		if (knowledge != null) {
			RuleSet rs = (RuleSet) knowledge;
			for (Rule rule : rs.getRules()) {
				try {
					// TODO: Hotfix should be removed
					if (rule == null) continue;
					if (rule.getCondition().eval(session)) {
						return getSuitableTerminalObjectForLogicalParent(rule
								.getCondition().getTerminalObjects());
					}
				}
				catch (NoAnswerException ex) {
				}
				catch (UnknownAnswerException ex) {
				}
				catch (IndexOutOfBoundsException ex) {
					Logger.getLogger(this.getClass().getName()).warning(
							"Rule " + rule.getId()
									+ " is corrupted! See Question "
									+ followQuestion.getId());
				}
			}
		}

		// container-parents have a higher priority than question-parents for
		// being logical parents
		List<Question> remainingQuestions = new LinkedList<Question>();
		for (TerminologyObject parent : followQuestion.getParents()) {
			if (parent instanceof Question) {
				remainingQuestions.add((Question) parent);
			}
			if (parent instanceof QContainer) {
				if ((getQASetQueue().contains(parent))
						|| (history.contains(parent))) {
					return (QContainer) parent;
				}
			}
		}

		if (!remainingQuestions.isEmpty()) {
			return remainingQuestions.get(0);
		}

		return null;
	}

	/**
	 * Returns a QASet out of the terminalObjects, which should be taken as
	 * logical parent. (in general, it is the first terminal object, that is
	 * valid; but if this is an SI-question, then its first logical parent will
	 * be determined and returned instead)
	 */
	private QASet getSuitableTerminalObjectForLogicalParent(List terminalObjects) {
		Iterator iter = terminalObjects.iterator();
		QASet qaset = null;
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof Question) {
				Question qaSet = (Question) obj;
				if (session.getInterview().isActive(qaSet)) {
					if (isSiQuestion(qaSet)) {
						return getFirstLogicalParent(qaSet);
					}
					return qaSet;
				}
			}
			else if (qaset == null && obj instanceof QContainer) {
				qaset = (QContainer) obj;
			}

		}
		return qaset;
	}

	/**
	 * Returns true, if the given question is an "SI"-question.
	 */
	private boolean isSiQuestion(Question q) {
		return q.getDerivationType().equals(DerivationType.DERIVED);
	}

	/**
	 * Returns a List, that contains all valid (to ask) questions, which are
	 * below "qc" in hierarchy. If "qc" is not a "leaf"-container, the first
	 * "leaf"-container, that is hierarchically under "qc" will be considered.
	 */
	public List<Question> getAllValidQuestionsOf(QContainer qc) {
		if (qc == null) {
			return (new LinkedList<Question>());
		}

		List<Question> validQuestions = new LinkedList<Question>();
		for (TerminologyObject to : qc.getChildren()) {
			QASet qaSet = (QASet) to;
			if (qaSet instanceof Question) {
				if (!isDone(session, qaSet)) {
					validQuestions.add((Question) qaSet);
				}
				validQuestions
						.addAll(getAllValidFollowQuestionsOf((Question) qaSet));
			}
			else {
				if (isValidForDC(qaSet)) {
					validQuestions
							.addAll(getAllValidQuestionsOf((QContainer) qaSet));
				}
			}
		}
		return validQuestions;
	}

	/**
	 * Returns a List, that contains all valid (to ask) follow-questions and
	 * follow-follow-questions ... of the given question "q".
	 */
	private List<Question> getAllValidFollowQuestionsOf(Question q) {
		List<Question> validQuestions = new LinkedList<Question>();
		Iterator<QASet> iter = QContainerIterator.createFollowList(session, q)
				.iterator();
		while (iter.hasNext()) {
			Question follow = (Question) iter.next();
			if (isValidForDC(follow)) {
				validQuestions.add(follow);
				validQuestions.addAll(getAllValidFollowQuestionsOf(follow));
			}
		}
		return (validQuestions);
	}

	/**
	 * Returns all active questions of the container "qc" in a List of List's;
	 * the List's contain first the Question-object and second an Integer-object
	 * describing the level of the question The order of the questions in the
	 * returned list is exactly the order in which they shall be presented on
	 * the screen.
	 * 
	 * @param qC
	 * @return List
	 */
	public List getAllQuestionsToRender(QContainer qC) {
		return getAllQuestionsToRender(qC, false, false);
	}

	/**
	 * Returns all visible questions of the container "qc" in a List of List's;
	 * the List's contain first the Question-object and second an Integer-object
	 * describing the level of the question The order of the questions in the
	 * returned list is exactly the order in which they shall be presented on
	 * the screen.
	 * 
	 * @param qC
	 * @param includingInactiveOnes boolean (include questions that are not
	 *        valid?)
	 * @param inactiveHierarchicalChildrenOnly boolean (If
	 *        "inactiveHierarchicalChildrenOnly" is set, then the inactive
	 *        follow-questions, that are not hierarchical children, will not be
	 *        added.)
	 * @return List
	 */
	public List getAllQuestionsToRender(QContainer qC,
			boolean includingInactiveOnes,
			boolean inactiveHierarchicalChildrenOnly) {

		List questionList = new LinkedList();
		if ((qC.getChildren().length > 0)
				&& (qC.getChildren()[0] instanceof Question)) {
			QContainerIterator containerIterator = new QContainerIterator(
					session, qC);

			while (containerIterator.hasNextChild()) {
				QASet child = containerIterator.getNextChild();
				if (child instanceof Question) {
					LinkedList oneQuestion = new LinkedList();
					oneQuestion.add(child);
					oneQuestion.add(new Integer(0));
					questionList.add(oneQuestion);
					addAllFollowQuestionsToRender((Question) child, 1,
							questionList, includingInactiveOnes,
							inactiveHierarchicalChildrenOnly);
				}
			}
		}
		return (questionList);
	}

	/**
	 * Adds all or only visible follow-questions of the question "q" to the list
	 * "prevQuestions". If "inactiveHierarchicalChildrenOnly" is set, then the
	 * inactive follow-questions, that are not hierarchical children, will not
	 * be added. The list "prevQuestions" is a List of List's; the List's
	 * contain first the Question-object and second an Integer-object describing
	 * the level of the question. The order of the questions in the
	 * "prevQuestions"-list ist exactly the order in which they shall be
	 * presented on the screen.
	 */
	private void addAllFollowQuestionsToRender(Question q, int level,
			List prevQuestions, boolean includingInactiveOnes,
			boolean inactiveHierarchicalChildrenOnly) {

		// children (logical follow-questions, NOT hierarchical)
		Iterator followiter = QContainerIterator.createFollowList(session, q)
				.iterator();
		while (followiter.hasNext()) {
			QASet follow = (QASet) followiter.next();
			if ((follow instanceof Question)
					&& ((!isInListOfQuestionLists((Question) follow,
							prevQuestions)))) {
				Question followQ = (Question) follow;
				if (session.getInterview().isActive(followQ)
						|| hasToShowInactiveQuestion(q, followQ,
								includingInactiveOnes,
								inactiveHierarchicalChildrenOnly)) {

					QASet logicalParent = getFirstLogicalParent((Question) follow);

					// logical parent has to be original question
					if ((logicalParent == null)
							|| (logicalParent.equals(q))
							|| (!session.getInterview().isActive(logicalParent))) {

						List oneQuestion = new LinkedList();
						oneQuestion.add(followQ);
						oneQuestion.add(new Integer(level));
						prevQuestions.add(oneQuestion);
						addAllFollowQuestionsToRender(followQ, level + 1,
								prevQuestions, includingInactiveOnes,
								inactiveHierarchicalChildrenOnly);
					}
				}
			}
		}
	}

	/**
	 * Returns true, if the follow-question "follow" has to be displayed on the
	 * screen, independent from its state of indication.
	 * 
	 * @see addAllFollowQuestionsToRender
	 */
	private boolean hasToShowInactiveQuestion(Question parent, Question follow,
			boolean includingInactiveQuestions,
			boolean inactiveHierarchicalChildrenOnly) {
		Boolean showAlways = (Boolean) follow.getProperties().getProperty(
				Property.DIALOG_MQDIALOGS_SHOW_FOLLOWQ_ALWAYS);
		if ((showAlways != null)
				&& (showAlways.booleanValue())
				&& (arrayContains(parent.getChildren(), follow) || !inactiveHierarchicalChildrenOnly)) {
			return true;
		}

		if ((includingInactiveQuestions)
				// (intersect with hierarchical children)
				&& (arrayContains(parent.getChildren(), follow) || !inactiveHierarchicalChildrenOnly)) {
			return true;
		}

		return false;
	}

	private boolean arrayContains(TerminologyObject[] array,
			TerminologyObject object) {
		for (TerminologyObject to : array) {
			if (to == object) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param q Question (the question, which is to search)
	 * @param toSearch List (a List of List's; only if the question is contained
	 *        in one of the List's, this method returns true)
	 * @return boolean
	 */
	private static boolean isInListOfQuestionLists(Question q, List toSearch) {
		Iterator iter = toSearch.iterator();
		while (iter.hasNext()) {
			List oneQuestion = (List) iter.next();
			if (oneQuestion.get(0).equals(q)) return (true);
		}
		return (false);
	}

	/**
	 * @return true, if all questions of the given container are not done and do
	 *         not have any real valid (except user-seletion!)
	 *         parent-containers. (if a container hasn't any proreasons but has
	 *         some questions, that are done, the container has to be
	 *         user-selected)
	 */
	public boolean nothingIsDoneInContainer(QContainer c) {
		for (TerminologyObject qaSet : c.getChildren()) {
			if (qaSet instanceof Question) {
				Question q = (Question) qaSet;

				QASet logicalParent = getFirstLogicalParent(q);
				if (c.equals(logicalParent)) {
					return false;
				}
				else if (logicalParent == null) {

					boolean aParentIsValid = false;
					for (TerminologyObject to : q.getParents()) {
						QASet parent = (QASet) to;
						if (parent != c) {
							// determine, if the parent is user-selected

							// QASet.Reason userSelectionReason = new
							// QASet.Reason(
							// null, PSMethodUserSelected.class);
							Fact fact = session.getBlackboard().getInterviewFact(parent);
							boolean wasUserSelected = false;
							if (fact.getPSMethod().equals(PSMethodUserSelected.getInstance())) {
								wasUserSelected = true;
							}

							// check, if the parent is real valid (valid WITHOUT
							// the
							// user-selection)
							aParentIsValid = aParentIsValid
									|| ((parent instanceof QContainer) && (session
											.getInterview().isActive(parent)));

						}
					}
					// if there is an answered question, which has no other real
					// valid
					// container-parent...
					// (then the container has to be user-selected)
					if ((!aParentIsValid) && (isDone(session, q))) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Returns true, if at least one of the children-questions is done.
	 */
	public boolean somethingIsDoneInContainer(QContainer c) {
		for (TerminologyObject qaSet : c.getChildren()) {
			if (qaSet instanceof Question) {
				Question q = (Question) qaSet;
				if (UndefinedValue.isNotUndefinedValue(session.getBlackboard()
						.getValue(q))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * getNewestQASet method comment.
	 */
	@Override
	public QASet moveToNewestQASet() {
		return (moveToNewestContainer());
	}

	/**
	 * getNextQASet method comment.
	 */
	@Override
	public QASet moveToNextQASet() {
		return (moveToNewestContainer());
	}

	private QASet moveToNewestContainer() {

		// now, the process of obtaining the remaining QASets (if started) is
		// interrupted
		obtainRemainingQASetStatus = false;

		// first look at user-selected containers
		if (!userIndicationList.isEmpty()) {
			tryToRemoveUserSelection((QContainer) getCurrentQASet());

			QASet current = userIndicationList.get(0);
			// insert it at the current history-position
			addToCurrentHistoryPos(current);
			userIndicationList.remove(0);
			return (current);
		}

		// if there isn't any user-selected container:

		// is the case aborted due to single fault assumption?
		Boolean abortCaseSFA = (Boolean) session.getProperties().getProperty(
				Property.HDT_ABORT_CASE_SFA);
		if ((abortCaseSFA != null) && (abortCaseSFA.booleanValue())) {
			return null;
		}

		// second, look at the current container
		if (isValidForDC(getCurrentQASet())) {
			return (getCurrentQASet());
		}
		else {
			historyCursor++;
		}

		// if the current container is not valid:

		// init-containers
		Iterator initIter = initQASets.iterator();
		while (initIter.hasNext()) {
			QASet qaSet = (QASet) initIter.next();
			if (qaSet instanceof QContainer) {
				tryToRemoveUserSelection((QContainer) qaSet);
			}
			initIter.remove();
			addToCurrentHistoryPos(qaSet);
			if (isValidForDC(qaSet)) {
				return (qaSet);
			}
			else {
				historyCursor++;
			}
		}

		// determine the first valid container in qaSetQueue
		// (that's a container, which was indicated by a rule)
		Iterator queueIter = getQASetQueue().iterator();
		while (queueIter.hasNext()) {
			QContainer container = (QContainer) queueIter.next();
			queueIter.remove();

			// check, if the container has to be user-selected (if
			// user-selected)
			tryToRemoveUserSelection(container);

			addToCurrentHistoryPos(container);
			if (isValidForDC(container)) {
				return (container);
			}
			else {
				historyCursor++;
			}
		}

		// else, there is no container to show
		return (null);
	}

	/**
	 * Trys to remove the user-selection (if existing).
	 * 
	 * @param container (QContainer)
	 */
	public void tryToRemoveUserSelection(QContainer container) {
		if (container == null) {
			return;
		}
		if ((userSelectedQASets.contains(container))
				&& ((nothingIsDoneInContainer(container)))) { // &&
			// (container.getContraReasons(session).isEmpty()))))
			// {
			// if nothing is answered in the container or if the container has
			// proreasons
			// and no contrareason, remove the user-selection
			userSelectedQASets.remove(container);
			// container.removeProReason(new QASet.Reason(null,
			// PSMethodUserSelected.class),
			// session);
		}
	}

	/**
	 * Returns all remaining QASets step by step.
	 * 
	 * @see de.d3web.kernel.dialogcontrol.controllers.DialogController#moveToNextRemainingQASet()
	 */
	@Override
	public QASet moveToNextRemainingQASet() {
		if ((!obtainRemainingQASetStatus) || (remainingQASetQueue == null)) {
			obtainRemainingQASetStatus = true;
			remainingQASetQueue = new LinkedList();
			// at the beginning of the obtaining process the remaining QASets
			// are exactly
			// the QASets in initQASets and qaSetQueue
			remainingQASetQueue.addAll(initQASets);
			remainingQASetQueue.addAll(getQASetQueue());
			remainingQASetQueue.addAll(userIndicationList);
		}
		Iterator iter = remainingQASetQueue.iterator();
		while (iter.hasNext()) {
			QASet next = (QASet) iter.next();
			iter.remove();
			if (isValidForDC(next)) {
				if ((next instanceof QContainer)
						&& (!processedContainers.contains(next))) {
					processedContainers.add((QContainer) next);
				}
				return next;
			}
		}
		return null;
	}

	/**
	 * getPreviousQASet method comment.
	 */
	@Override
	public QASet moveToPreviousQASet() {
		if (hasPreviousQASet()) {
			historyCursor--;
			if (historyCursor >= history.size()) {
				historyCursor = history.size() - 1;
			}
			QASet q = (QASet) history.get(historyCursor);
			return q;
		}
		return null;
	}

	/**
	 * Insert the method's description here. Creation date: (27.02.2001
	 * 11:33:05)
	 * 
	 * @param id java.lang.String
	 */
	@Override
	public QASet moveToQASet(QASet searchQASet) {
		addUserIndicationQASet(searchQASet);
		return (searchQASet);
	}

	/**
	 * Insert the method's description here. Creation date: (27.02.2001
	 * 11:33:05)
	 * 
	 * @param id java.lang.String
	 */
	@Override
	public QASet moveToQuestion(QASet searchQuestion) {

		if (searchQuestion == null) {
			return (moveToNewestContainer());
		}
		else if (searchQuestion instanceof QContainer) {
			return (moveToQASet(searchQuestion));
		}

		addUserIndicationQASet(searchQuestion);
		// determine the parent-container and move to that parent
		QContainer c = getFirstContainerParent((Question) searchQuestion);
		return (c);
	}

	/**
	 * Returns a "leaf"-QContainer, that is parent of the question
	 */
	private static QContainer getFirstContainerParent(Question q) {
		if (q == null) return (null);
		for (TerminologyObject qaSet : q.getParents()) {
			if (qaSet instanceof Question) return (getFirstContainerParent((Question) qaSet));
			else return ((QContainer) qaSet);
		}
		return (null);
	}

	/**
	 * Returns a "leaf"-QContainer, that is directly parent of the question "q"
	 * or that is parent of a question, whose follow-(follow-...-)question is
	 * "q".
	 * 
	 * @param q
	 * @return
	 */
	public QContainer getFirstLogicalContainerParent(Question q) {
		QASet parent = null;
		QASet firstLogicalParent = getFirstLogicalParent(q);
		while (firstLogicalParent instanceof Question) {
			parent = firstLogicalParent;
			firstLogicalParent = getFirstLogicalParent((Question) firstLogicalParent);
		}
		if (parent instanceof Question) {
			return getFirstContainerParent((Question) parent);
		}
		else {
			return getFirstContainerParent(q);
		}
	}

	/**
	 * addUserIndicationQASet method comment.
	 */
	public void addUserIndicationQASet(
			de.d3web.core.knowledge.terminology.QASet q) {
		QContainer qContainer = null;
		if (q instanceof QContainer) {
			// only "leaf"-containers should be added
			if (q.getChildren().length != 0) {
				qContainer = getFirstIndicatedLeafContainer((QContainer) q,
						true);
				if (qContainer == null) {
					qContainer = getFirstIndicatedLeafContainer((QContainer) q,
							false);
				}
				if (qContainer == null) {
					qContainer = getFirstLeafContainer((QContainer) q);
				}
			}
			else {
				qContainer = (QContainer) q;
			}
		}
		else {
			// questions shouldn't be added, so determine a parent-container
			qContainer = getFirstContainerParent((Question) q);
		}
		if ((qContainer != null) && (!userIndicationList.contains(qContainer))) {
			// // if the container hasn't any proreasons or if it has some
			// // contrareasons,
			// // mark it as user-selected
			// List pros = qContainer.getProReasons(session);
			// List cons = qContainer.getContraReasons(session);
			// if ((pros.isEmpty()) || (!cons.isEmpty())) {
			if (!userSelectedQASets.contains(qContainer)) {
				userSelectedQASets.add(qContainer);
			}
			// qContainer
			// .addProReason(new QASet.Reason(null, PSMethodUserSelected.class),
			// session);
			// }
			userIndicationList.add(0, qContainer);

		}
	}

	/**
	 * Returns the first container (below startC in hierarchy) which has
	 * questions as children.
	 */
	private QContainer getFirstLeafContainer(QContainer startC) {
		if (startC == null) return null;

		while ((startC.getChildren())[0] instanceof QContainer) {
			startC = (QContainer) startC.getChildren()[0];
		}
		return startC;
	}

	/**
	 * Returns the first container (below startC) which is indicated (and
	 * valid?) and which has questions as children.
	 */
	private QContainer getFirstIndicatedLeafContainer(QContainer startC,
			boolean mustBeValid) {
		if (startC == null) {
			return null;
		}
		for (TerminologyObject to : startC.getChildren()) {
			QASet qaSet = (QASet) to;
			if (qaSet instanceof Question) {
				if ((isIndicated(startC))
						&& ((!mustBeValid) || (isValidForDC(startC)))) {
					return startC;
				}
				else {
					return null;
				}
			}
			else {
				if (isIndicated(qaSet)) {
					QContainer ret = getFirstIndicatedLeafContainer(
							(QContainer) qaSet, mustBeValid);
					if (ret != null) {
						return ret;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void propagate(NamedObject no, Rule rule, PSMethod psm) {
		if (no instanceof QASet) {
			QASet qaSet = (QASet) no;
			if (psm instanceof PSMethodUserSelected) {
				addUserIndicationQASet(qaSet);
			}
			else if (psm instanceof PSMethodInit) {
				addQASet(initQASets, qaSet);
			}
			else {
				addQASet(qasetQueue, qaSet);
			}
		}
	}

	@Override
	public MQDialogController getMQDialogcontroller() {
		return this;
	}

	@Override
	public OQDialogController getOQDialogcontroller() {
		return null;
	}

	/**
	 * HOTFIX: A Plugin need to know, what QASet will be displayed next
	 * 
	 * @return newest QAset
	 */
	public QASet getNewestQASet() {
		// now, the process of obtaining the remaining QASets (if started) is
		// interrupted
		obtainRemainingQASetStatus = false;

		// first look at user-selected containers
		if (!userIndicationList.isEmpty()) {
			return userIndicationList.get(0);
		}

		// if there isn't any user-selected container:

		// is the case aborted due to single fault assumption?
		Boolean abortCaseSFA = (Boolean) session.getProperties().getProperty(
				Property.HDT_ABORT_CASE_SFA);
		if ((abortCaseSFA != null) && (abortCaseSFA.booleanValue())) {
			return null;
		}

		// second, look at the current container
		if (isValidForDC(getCurrentQASet())) {
			return (getCurrentQASet());
		}

		// if the current container is not valid:

		// init-containers
		for (QASet each : initQASets) {
			if (isValidForDC(each)) {
				return (each);
			}
		}

		// determine the first valid container in qaSetQueue
		// (that's a container, which was indicated by a rule)
		for (QASet each : getQASetQueue()) {
			if (isValidForDC(each)) {
				return (each);
			}
		}

		// else, there is no container to show
		return (null);
	}

	private boolean isDone(Session session, QASet object) {
		if (object instanceof Question) {
			return UndefinedValue.isNotUndefinedValue(session.getBlackboard()
					.getValue((Question) object));
		}
		else if (object instanceof QContainer) {
			return session.getInterview().isActive(object);
		}
		else return false;
	}

}