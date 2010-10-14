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
package de.d3web.we.action;

import java.io.IOException;
import java.util.HashMap;

import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.imagequestion.ImageQuestionHandler;

/**
 * Handles the rerendering of an Image Question.
 * 
 * @author Johannes Dienst
 * @created 01.08.2010
 */
public class ImageQuestionAction extends AbstractAction {

	private final AbstractAction action = new ImageQuestionSetAction();

	@Override
	public void execute(ActionContext context) throws IOException {
		KnowWEParameterMap map = context.getKnowWEParameterMap();
		String questionId = map.get("QuestionID");
		HashMap<String, String> values = new HashMap<String, String>();
		values.put(ImageQuestionHandler.TAGHANDLER_ANNOTATION, questionId);
		context.setContentType("text/html; charset=UTF-8");
		action.execute(context);
		context.getWriter().write(new ImageQuestionHandler().renderForRerenderAction(
				map.getTopic(), context.getWikiContext(), values, map.getWeb()));
	}

}
