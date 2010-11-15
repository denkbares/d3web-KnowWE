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

package de.d3web.we.kdom.report;

import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Default renderer for Notice messages
 * 
 * To have your own customized NoticeRenderer overwrite getNoticeRenderer in
 * your KnowWEObjectType and return a (custom) MessageRenderer of your choice
 * 
 * @author Jochen
 * 
 */
public class DefaultNoticeRenderer implements MessageRenderer {

	private static DefaultNoticeRenderer instance = null;

	public static DefaultNoticeRenderer getInstance() {
		if (instance == null) {
			instance = new DefaultNoticeRenderer();

		}

		return instance;
	}

	@Override
	public String preRenderMessage(KDOMReportMessage notice, KnowWEUserContext user) {
		// does do nothing --> happens post
		return "";
	}

	@Override
	public String postRenderMessage(KDOMReportMessage notice, KnowWEUserContext user) {

		StringBuffer buffy = new StringBuffer();

		buffy.append(" <img height='6' src='KnowWEExtension/images/green_bulb.gif'");

		buffy.append(" title='" + notice.getVerbalization() + "'>");

		return KnowWEUtils.maskHTML(buffy.toString());

	}

}
