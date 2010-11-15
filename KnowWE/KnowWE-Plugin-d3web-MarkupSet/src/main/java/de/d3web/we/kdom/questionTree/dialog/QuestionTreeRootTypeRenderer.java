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

package de.d3web.we.kdom.questionTree.dialog;

import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.defaultMarkup.AnnotationType;
import de.d3web.we.kdom.rendering.CustomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.RenderingMode;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * QuestionTreeRenderer. Renders the collapsible dialog of the question tree.
 * 
 * @author smark
 * @since 2010/03/09
 */
public class QuestionTreeRootTypeRenderer extends CustomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec,
			KnowWEUserContext user, StringBuilder string) {

		string.append(KnowWEUtils.maskHTML("<div id=\"" + sec.getID()
				+ "\" class=\"collapsible-questiontree\">\n"));

		string.append(KnowWEUtils.maskHTML("<div id=\"\" style=\"float:right;\">\n"));

		string.append(KnowWEUtils.maskHTML("<span class=\"dt-ajax pointer\" rel=\"{dt : '', KdomNodeId : '"
				+ sec.getID()
				+ "'}\"><img src='KnowWEExtension/images/dt_icon_explanation2.png' alt='Default decision tree view' title='Default decision tree view'/></span> "
				+ "<span class=\"dt-ajax pointer\" rel=\"{dt : 'question', KdomNodeId : '"
				+ sec.getID()
				+ "'}\"><img src='KnowWEExtension/images/icon_question_small.gif' alt='Show decision tree in question mode' title='Show decision tree in question mode'/></span> "
				+ "<span class=\"dt-ajax pointer\" rel=\"{dt : 'answer', KdomNodeId : '"
				+ sec.getID()
				+ "'}\"><img src='KnowWEExtension/images/icon_diagnosis.gif' alt='Show decision tree in anwser mode' title='Show decision tree in anwser mode'/></span>"
				));
		string.append(KnowWEUtils.maskHTML("</div>\n"));
		string.append(KnowWEUtils.maskHTML("<div id=\"collapsible-questiontree-info\" style=\"display:none\"></div>\n"));

		// add an anchor to enable direct link to the section
		String anchorName = KnowWEUtils.getAnchor(sec);
		string.append(KnowWEUtils.maskHTML("<a name='" + anchorName + "'></a>\n"));

		// render messages and content
		// renderMessages(article, sec, string);

		List<Section<? extends KnowWEObjectType>> sections = sec.getChildren();
		for (Section<? extends KnowWEObjectType> section : sections) {
			if (!(section.getObjectType() instanceof PlainText || section.getObjectType() instanceof AnnotationType)) {
				DelegateRenderer.getInstance().render(article, section, user, string);
			}
		}
		// and close the box
		string.append(KnowWEUtils.maskHTML("</div>\n"));
	}

	@Override
	public boolean doesApply(String user, String topic, RenderingMode type) {
		return true;
	}
}
