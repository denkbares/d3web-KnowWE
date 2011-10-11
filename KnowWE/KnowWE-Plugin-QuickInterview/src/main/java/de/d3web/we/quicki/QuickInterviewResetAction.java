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

package de.d3web.we.quicki;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import de.d3web.we.basic.D3webModule;
import de.d3web.we.basic.SessionBroker;
import de.knowwe.core.KnowWEAttributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * For resetting the QuickInterview
 *
 * @author Martina Freiberg
 * @created 28.08.2010
 */
public class QuickInterviewResetAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String result = resetQuickInterview(context);
		if (result != null && context.getWriter() != null) {
			context.getWriter().write(result);
		}

	}

	public String resetQuickInterview(UserActionContext context) {

		String topic = context.getTopic();
		String user = context.getParameter(KnowWEAttributes.USER);
		String web = context.getParameter(KnowWEAttributes.WEB);
		HttpServletRequest request = context.getRequest();

		SessionBroker broker = D3webModule.getBroker(context.getParameters());
		broker.clear();
		return QuickInterviewAction.callQuickInterviewRenderer(topic, user, request, web,
				context);
	}
}
