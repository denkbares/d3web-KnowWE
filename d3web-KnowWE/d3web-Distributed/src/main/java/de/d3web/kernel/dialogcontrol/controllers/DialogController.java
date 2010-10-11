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

import de.d3web.core.knowledge.terminology.QASet;

/**
 * The dialogs view on the dialogcontrolling-part of the case! <br>
 * Creation date: (21.02.2001 12:58:46)
 * 
 * @author Norman Brümmer
 */
public interface DialogController extends QASetManager {

	/**
	 * @return if the QASet is a valid one for current DC.
	 **/
	public boolean isValidForDC(QASet q);

	/**
	 * @return current QASet - the QASet the controller points on
	 */
	public QASet getCurrentQASet() throws InvalidQASetRequestException;

	/**
	 * @return true, if there is any QASet that can be answered in the current
	 *         case
	 */
	public boolean hasNewestQASet();

	/**
	 * @return true, if there is a QASet the controller can go back to
	 */
	public boolean hasPreviousQASet();

	/**
	 * moves to the next new QASet (unanswered, activated)
	 * 
	 * @return newest QASet
	 */
	public QASet moveToNewestQASet();

	/**
	 * moves to the next QASet (e.g. on history) even if it has already been
	 * answered
	 * 
	 * @return next QASet
	 */
	public QASet moveToNextQASet();

	/**
	 * moves to the previous QASet (e.g. on history)
	 * 
	 * @return previous QASet
	 */
	public QASet moveToPreviousQASet();

	/**
	 * Returns the next remaining QASet (e.g. on qaSetQueue) even if the current
	 * QASet hasn't been answered completely. The currentQASet, newestQASet and
	 * nextQASet will not be changed by this method. Multiple method-calls are
	 * iterating the nextQASet's, until "moveToNewestQASet" is called (then, the
	 * nextRemainingQASet is the QASet that comes after the currentQASet).
	 * 
	 * @return QASet
	 * @deprecated Das ist natürlich Quatsch mit Soße. Besser wäre es doch wohl,
	 *             dafür einen DialogController zu verwenden, dem es egal ist,
	 *             ob ein QContainer komplett beantwortet ist.
	 */
	@Deprecated
	public QASet moveToNextRemainingQASet();

	/**
	 * jumps to QASet q and activates it
	 * 
	 * @return the QASet the controller has jumped to
	 */
	public QASet moveToQASet(QASet q);

	/**
	 * jumps to Question q or to QContainer that is parent of q (depending on
	 * DialogController type)
	 * 
	 * @return the QASet the controller has jumped to
	 */
	public QASet moveToQuestion(QASet q);
}