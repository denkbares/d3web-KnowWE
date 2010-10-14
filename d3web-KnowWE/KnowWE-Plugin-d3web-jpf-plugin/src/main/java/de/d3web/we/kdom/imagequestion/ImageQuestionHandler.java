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
package de.d3web.we.kdom.imagequestion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Choice;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.knowledge.terminology.QuestionMC;
import de.d3web.core.knowledge.terminology.QuestionOC;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.ChoiceValue;
import de.d3web.core.session.values.MultipleChoiceValue;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.multimedia.io.ImageQuestionStore;
import de.d3web.we.action.SetSingleFindingAction;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Handling ImageQuestions with AnswerRegions.
 * 
 * @author Johannes Dienst
 * 
 */
public class ImageQuestionHandler extends AbstractTagHandler {

	public static final String TAGHANDLER_ANNOTATION =
			"question";

	/**
	 * Nearly every method in this class needs: topic, web and the
	 * KnowWEUserContext. So it is easier to set them at the start as fields.
	 */
	private String topic;
	private String web;
	private KnowWEUserContext user;

	// private String width;
	// private String height;

	public ImageQuestionHandler() {
		super("imagequestionhandler");
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).
				getString("KnowWE.ImageQuestionHandler.Description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		return this.render(topic, user, values, web, true);
	}

	/**
	 * Used to Rerender the ImageQuestion. So Checkboxes can be set, when a
	 * AnswerRegion was clicked.
	 * 
	 * @param topic
	 * @param user
	 * @param values
	 * @param web
	 * @return
	 */
	public String renderForRerenderAction(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		return this.render(topic, user, values, web, false);
	}

	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web, boolean renderDIV) {

		// set the fields.
		this.topic = topic;
		this.web = web;
		this.user = user;

		D3webKnowledgeService service = D3webModule.getAD3webKnowledgeServiceInTopic(web, topic);
		KnowledgeBase kb = service.getBase();

		// Read out the properties
		String questionID = values.get(ImageQuestionHandler.TAGHANDLER_ANNOTATION);
		KnowledgeBaseManagement kbm = KnowledgeBaseManagement.createInstance(kb);
		Question q = kbm.findQuestion(questionID);
		ImageQuestionStore store = (ImageQuestionStore) q.getInfoStore().getValue(
				BasicProperties.IMAGE_QUESTION_INFO);

		// Layout is: Picture | Checkboxes with labels
		StringBuffer renderedImage = new StringBuffer();
		StringBuffer renderedCheckBoxes = new StringBuffer();
		try {
			this.renderQuestionImage(renderedImage, store, q, kb);
			this.renderQuestionChoiceColumns(
					renderedCheckBoxes, D3webUtils.getSession(topic, user, web),
					q);
		}
		catch (IOException e) {
			Logger.getLogger(ImageQuestionHandler.class.getName())
					.warning(
							"Error in rendering method of ImageQuestionHandler : "
									+ e.getMessage());
		}

		// Render DIV only if it is not RerenderingAction
		StringBuffer buffi = new StringBuffer();
		if (renderDIV) {
			buffi.append("<div id='imagequestion_"
					+ q.getId()
					+ "' class='panel'>"
					+ "<h3> ImageQuestion </h3>");
		}
		buffi.append(
				"<table id=\"imagetable_" + q.getId() + "\"><tr><td>"
						+ renderedImage.toString()
						+ "</td>"
						+ "<td>"
						+ renderedCheckBoxes.toString()
						+ "</td>"
						+ "</tr></table>");
		if (renderDIV) buffi.append("</div>");

		return buffi.toString();
	}

	/**
	 * Builds a list with AnswerRegions from the Attributes.
	 * 
	 * @param answerRegions
	 * @return
	 */
	private List<AnswerRegion> buildAnswerRegions(List<List<String>> answerRegions) {
		List<AnswerRegion> buildAR = new ArrayList<AnswerRegion>();
		List<String> attributes = null;
		for (int i = 0; i < answerRegions.size(); i++) {
			attributes = answerRegions.get(i);
			String answerID = attributes.get(0);
			int xStart = Integer.parseInt(attributes.get(1));
			int xEnd = Integer.parseInt(attributes.get(2));
			int yStart = Integer.parseInt(attributes.get(3));
			int yEnd = Integer.parseInt(attributes.get(4));
			AnswerRegion reg = new AnswerRegion(answerID, xStart, xEnd, yStart, yEnd);
			buildAR.add(reg);
		}
		return buildAR;
	}

	/**
	 * Renders a DIV with the question image of a given ImageQuestion. It also
	 * includes the AnswerRegions.
	 * 
	 * @param buffi
	 * @param store
	 * @param q
	 * @param kb
	 * @throws IOException
	 */
	private void renderQuestionImage(StringBuffer buffi,
			ImageQuestionStore store, Question q,
			KnowledgeBase kb)
			throws IOException {

		buffi.append("<div id=\"" + q.getId() + "\" class=\"questionImage\"");

		StringBuffer buffer = new StringBuffer();
		buffer.append("position: relative;");
		buffer.append("width: " + store.getWidth() + ";");
		buffer.append("height: " + store.getHeight() + ";");

		buffer.append("margin-left: auto;");
		buffer.append("margin-right: auto;");

		String relImagePath =
				"../KnowWE/action/StreamImageToResourceAction/"
						+ store.getFile() +
						"?topic=" + topic + "&web=" + web + "&imagename=" + store.getFile();
		buffer.append("background-image:url(" + relImagePath + ");");
		buffi.append(" style=\"" + buffer.toString() + "\">");

		// render AnswerRegions
		List<AnswerRegion> answerRegs =
				this.buildAnswerRegions(store.getAnswerRegions());
		for (AnswerRegion region : answerRegs) {
			String t = renderAnswerRegion(region, kb, q);
			buffi.append(t);
		}
		buffi.append("</div>");
	}

	/**
	 * Renders the Answer Regions for a picture. The Answer Regions are
	 * clickable and visible if the checkbox for the answerRegion is checked.
	 * 
	 * @param answerRegion
	 * @param kb
	 * @param q
	 * @return
	 * @throws IOException
	 */
	private String renderAnswerRegion(AnswerRegion answerRegion,
			KnowledgeBase kb, Question q) {

		// Is the Answer in the KnowledgeBase
		Choice answer = kb.searchAnswerChoice(answerRegion.getAnswerID());

		if (answer == null) {
			Logger.getLogger(ImageQuestionHandler.class.getName())
					.warning(
							"The Answer ID was not found in the knowledge base : ");
			return "";
		}

		String answerID = answerRegion.getAnswerID();

		// Render the Region
		StringBuffer buffi = new StringBuffer();

		buffi.append("<a id=\"region_" + answerID + "\"");

		buffi.append(" " + this.buildRelAttributeString(answerID, q.getId()));

		buffi.append(" class=\"answerRegion");
		// if region is aleady answered --> insert another styleclass
		if (currentAnswerIsSet(answer.getId(), q)) {
			buffi.append(" answerSet");
		}
		buffi.append("\"");

		// write style-Attribute
		StringBuffer styleString = new StringBuffer();
		styleString.append(" top: " + answerRegion.getYStart() + "px;");
		styleString.append(" left: " + answerRegion.getXStart() + "px;");
		styleString.append(" width: " + answerRegion.getWidth() + "px; height: "
				+ answerRegion.getHeight() + "px;");
		buffi.append(" style=\"" + styleString.toString() + "\"");
		buffi.append(">");

		// insert a transparent gif (so that Internet Explorer can be supported)
		String imageName = answer.getId();
		buffi.append("<img");
		buffi.append(" class=\"qImageHover\" alt=\"\"");
		buffi.append(" src=\"" + KnowWEEnvironment.getInstance().getKnowWEExtensionPath()
				+ "/d3web/icon/spacer.gif\"");
		buffi.append(" width=\"" + answerRegion.getWidth() + "\"");
		buffi.append(" height=\"" + answerRegion.getHeight() + "\"");
		buffi.append(" title=\"" + imageName + "\" ");
		buffi.append(" id=\"img_" + answerID + "\" ");

		// Add a Semantic Annotation,
		// so the SetSingleFindingAction can be used
		buffi.append(this.buildRelAttributeString(answerID, q.getId()));

		buffi.append(">");
		buffi.append("</a> \n"); // avoid pagebreak by jspwiki

		return buffi.toString();
	}

	/**
	 * Builds a rel-Attribute. Used by {@link SetSingleFindingAction} to set
	 * AnswerChoices.
	 * 
	 * @param answerID
	 * @param id
	 * @return
	 */
	private String buildRelAttributeString(String answerID, String id) {
		StringBuffer relBuffi = new StringBuffer();
		relBuffi.append("rel=\"{oid: '" + answerID + "',");
		relBuffi.append(" web:'" + this.web + "',");
		relBuffi.append(" ns:'" + this.topic + ".."
				+ KnowWEEnvironment.generateDefaultID(this.topic) + "',");
		relBuffi.append(" qid:'" + id + "' }\"");
		return relBuffi.toString();
	}

	/**
	 * Checks if this AnswerChoice is currently set. Works for QuestionOC and
	 * QuestionMC which are used in ImageQuestions.
	 * 
	 * @param answerID
	 * @param q
	 * @return
	 */
	private boolean currentAnswerIsSet(String answerID, Question q) {

		Value value = null;
		if (q instanceof QuestionChoice) {
			for (Choice choice : ((QuestionChoice) q).getAllAlternatives()) {
				if (choice.getId().equals(answerID)) {
					value = new ChoiceValue(choice);
					break;
				}
			}
		}

		Session session = D3webUtils.getSession(this.topic, this.user,
				this.web);
		Value answer = session.getBlackboard().getValue(q);
		boolean contains = false;
		if (!(answer instanceof UndefinedValue)) {
			@SuppressWarnings("unchecked")
			Set<Value> values = (Set<Value>) answer.getValue();
			for (Value val : values) {
				if (val.equals(value)) {
					contains = true;
				}
			}
		}

		return contains;
	}

	/**
	 * Renders the the columns with checkboxes and labels.
	 * 
	 * @param buffi
	 * @param session
	 * @param q
	 */
	private void renderQuestionChoiceColumns(StringBuffer buffi, Session session,
			Question q) {

		// new Table for Checkboxes an labels
		buffi.append("<table><tr>");

		// TODO: Not everytime 2 columns
		List<Choice> ans = ((QuestionChoice) q).getAllAlternatives();
		List<String> checkBoxes = null;

		if (q instanceof QuestionMC) checkBoxes = this.renderCheckBoxes(ans, q, true, session);

		if (q instanceof QuestionOC) checkBoxes = this.renderCheckBoxes(ans, q, false, session);

		// Render column 1
		int i = 0;
		buffi.append("<td>");
		buffi.append("<table>");
		for (; i < ((checkBoxes.size() / 2) + 1); i++) {
			buffi.append(checkBoxes.get(i));
		}
		buffi.append("</table>");
		buffi.append("</td>");

		// Render column 2
		buffi.append("<td>");
		buffi.append("<table>");
		for (; i < ans.size(); i++) {
			buffi.append(checkBoxes.get(i));
		}
		buffi.append("</table>");
		buffi.append("</td>");

		// End the table from beginning
		buffi.append("</tr></table>");

	}

	/**
	 * Renders the HTML for a Checkbox. It is easier to build the table
	 * afterwards.
	 * 
	 * @param ans
	 * @param q
	 * @param isCheckBox
	 * @param session
	 * @return
	 */
	private List<String> renderCheckBoxes(List<Choice> ans,
			Question q, boolean isCheckBox, Session session) {

		StringBuffer buffi = new StringBuffer();
		ArrayList<String> rendered = new ArrayList<String>();
		String answerID = "";
		String answerName = "";
		for (int i = 0; i < ans.size(); i++) {
			answerID = ans.get(i).getId();
			buffi.append("<tr>");
			buffi.append("<td>");
			buffi.append("<input id=\"box_" + answerID + "\"" +
					this.buildRelAttributeString(answerID, q.getId()));
			if (isCheckBox) buffi.append("type=\"checkbox\"");
			else buffi.append("type=\"radio\"");
			buffi.append(" class=\"answerRegion2\" ");
			buffi.append(this.buildRelAttributeString(
					answerID, q.getId()));

			Value value = session.getBlackboard().getValue(q);
			if (isAnsweredinCase(value, new ChoiceValue(ans.get(i)))) buffi.append(" checked");

			buffi.append(">");
			buffi.append("</td>");

			// Render Label for Button
			answerName = ans.get(i).getName();
			buffi.append("<td>");
			buffi.append("<label for=\"" + answerID + "\">" +
					answerName + "</label>");
			buffi.append("</td>");
			buffi.append("</tr>");
			rendered.add(buffi.toString());
			buffi = new StringBuffer();
		}

		return rendered;
	}

	/**
	 * Evaluates a value in a given session.
	 * 
	 * @param sessionValue
	 * @param value
	 * @return
	 */
	private boolean isAnsweredinCase(Value sessionValue, Value value) {
		if (sessionValue instanceof MultipleChoiceValue) {
			return ((MultipleChoiceValue) sessionValue).contains(value);
		}
		else {
			return sessionValue.equals(value);
		}
	}
}
