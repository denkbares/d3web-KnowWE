/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.d3webModule;

import java.net.URLEncoder;
import java.util.List;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.QASet;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerNum;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;

public class HTMLDialogRenderer {

	public static final String SPAN = "span";
	public static final String TR_TAG = "<tr>";
	public static final String TR_TAG_CLOSE = "</tr>";
	public static final String TD_TAG = "<td>";
	public static final String TD_TAG_CLOSE = "</td>";

	public static String renderDialog(XPSCase c, String web) {

		KnowledgeBase b = c.getKnowledgeBase();
		java.util.List<de.d3web.kernel.domainModel.qasets.QContainer> containers = b
				.getQContainers();
		StringBuffer buffi = new StringBuffer();

		// for formatting the specific dialog panel
		buffi
				.append("<div class='dialogstyle''>"
						+ "<b class='top'><b class='b1'></b><b class='b2'></b><b class='b3'>"
						+ "</b><b class='b4'></b></b>"
						+ "<div class='boxcontent'>");

		buffi.append(getDialogPluginHeader());
		buffi.append("<div id='dialog' class='dialogstyle' style='display:inline'>");

		boolean first = true;
		for (de.d3web.kernel.domainModel.qasets.QContainer container : containers) {
			if (container.getText().endsWith("Q000"))
				continue;

			
			if(first){
				buffi.append("<div class='qcontainer' id='" + container.getId() + "'>");
				buffi.append("<h4 class='qcontainerName'><a href='javascript:showDialogElement(\""
							+ container.getId() + "\")'>");
				buffi.append("<img src='KnowWEExtension/images/arrow_down.png' border='0'/>");
				buffi.append(container.getText() + ": ");
				buffi.append("</a></h4>");
				buffi.append("<table id='tbl" + container.getId() + "' class='visible'><tbody>");
				first = false;
			} else {
				
				buffi.append("<div class='qcontainer' id='" + container.getId() + "'>");
				buffi.append("<h4 class='qcontainerName'><a href='javascript:showDialogElement(\""
							+ container.getId() + "\")'>");
				buffi.append("<img src='KnowWEExtension/images/arrow_right.png' border='0'/>");
				buffi.append(container.getText() + ": ");
				buffi.append("</a></h4>");
				buffi.append("<table id='tbl" + container.getId() + "' class='hidden'><tbody>");
			}
			
			java.util.List<? extends NamedObject> questions = container
					.getChildren();

			// to be able to format the table lines alternatingly
			int i = -1;
			for (NamedObject namedObject : questions) {
				Question q = null;
				if (namedObject instanceof Question) {
					q = (Question) namedObject;

				} else {
					continue;
				}
				//System.out.println("HALLO" + q.getKnowledge(PSMethodNextQASet.getInstance().getClass()));
				
				i += 1;
				// assigns different css classes according to whether nr/line is
				// odd or even
				
				if(i==0){
					buffi.append("<tr class='trEven first'>");
					buffi.append(render(c, q, web, b.getId(), true));
					buffi.append("</tr> \n");
					
				} else if (i % 2 == 0) {
					buffi.append("<tr class='trEven'>");
					buffi.append(render(c, q, web, b.getId(), true));
					buffi.append("</tr> \n");
					
				} else {
					buffi.append("<tr class='trOdd'>");
					buffi.append(render(c, q, web, b.getId(), false));
					buffi.append("</tr> \n");
				}
				
				/*
				if(q.getChildren()!=null && !(q.getChildren().isEmpty())){
					buffi.append("<tr id ='trf" + q.getId() + "' class='hidden'>");
					buffi.append(renderFollowUpQuestions(c, q, web, b.getId()));
					buffi.append("</tr \n>");
					
				}*/
			}
			buffi.append("</tbody></table>");
			buffi.append("</div>"); // qcontainer div
		}

		buffi.append("</div>"); // dialog div
		buffi.append("</div>"); // box content div
		buffi.append("<b class='bottom'><b class='b4b'></b><b class='b3b'></b>"
				+ "<b class='b2b'></b><b class='b1b'></b></b>" + "</div>"); // inset
																			// div

		return buffi.toString();
	}

	/**
	 * <p>
	 * Creates the header of the dialog extension.
	 * </p>
	 * 
	 * @return HTML string
	 */
	private static String getDialogPluginHeader() {
		StringBuffer html = new StringBuffer();
		html.append("<h3>");
		html.append("Dialog");
		html.append("</h3>");
		return html.toString();
	}

	private static String render(XPSCase c, Question q, String web,
			String namespace, boolean even) {
		StringBuffer html = new StringBuffer();

		if (even) {
			html.append("<td class='labelcellEven'><label for='" + q.getId()
					+ "'>" + q.getText() + "</label></td>");
		} else {
			html.append("<td class='labelcellOdd'><label for='" + q.getId()
					+ "'>" + q.getText() + "</label></td>");
		}

		html.append("<td class='fieldcell'><div id='" + q.getId() + "'>");
		if (q instanceof QuestionChoice) {
			List<AnswerChoice> list = ((QuestionChoice) q).getAllAlternatives();
			renderChoiceAnswers(c, html, q, list, web, namespace);

			// to remove the last comma after answers in built in dialog
			html.delete(html.length() - 2, html.length());

		} else {
			renderNumAnswers(c, html, q, web, namespace);
		}
		html.append("</div></td>");
		
		return html.toString();
	}
	
	
	private static String renderFollowUpQuestions(XPSCase c, Question trigger, 
			String web, String namespace){
		List follows = trigger.getChildren();
		StringBuffer html = new StringBuffer();
		
		QASet first = (QASet)follows.get(0);
		
		if(first instanceof Question){
			
			html.append("<td class='labelcellFollow'><label for='" + first.getId() + "'>"
					 + first.getText() + " </label></td>");
		
			html.append("<td class='fieldcell'><div id='" + first.getId() + "'>");
			
			if (first instanceof QuestionChoice) {
				List<AnswerChoice> list = ((QuestionChoice) first).getAllAlternatives();
				renderChoiceAnswers(c, html, (Question)first, list, web, namespace);

				// to remove the last comma after answers in built in dialog
				html.delete(html.length() - 2, html.length());

			} else {
				renderNumAnswers(c, html, (Question)first, web, namespace);
			}
		}
		
		html.append("</div></td>");
		
		return html.toString();
	}
	

	private static void renderNumAnswers(XPSCase c, StringBuffer buffi,
			Question q, String web, String namespace) {
		String value = "";
		if (q.hasValue(c)) {
			List l = q.getValue(c);
			if (l.size() > 0) {
				Object o = l.get(0);
				if (o instanceof AnswerNum) {
					value = Double.toString((Double) ((AnswerNum) o)
							.getValue(c));
				}
			}
		}
		String id = "numInput_" + q.getId();
		String jscall = "numInputEntered(event,'" + web + "','" + namespace
				+ "','" + q.getId() + "','" + URLEncoder.encode(q.getText())
				+ "','" + id + "');";
		buffi.append("<input id='" + id + "' type='text' value='" + value
				+ "' class='numInput' size='7' onkeydown=\"" + jscall + "\">");
		String jscall2 = "numOkClicked('" + web + "','" + namespace + "','"
				+ q.getId() + "','" + URLEncoder.encode(q.getText()) + "','"
				+ id + "');";
		buffi.append("<input type='button' value='ok' onclick=\"" + jscall2
				+ "\">");

	}

	private static void renderChoiceAnswers(XPSCase c, StringBuffer buffi,
			Question q, List<AnswerChoice> list, String web, String namespace) {

		// changed cssclass
		//String cssclass = "fieldcell";
		
		int i=0;
		// to space before and after commas evenly
		//buffi.delete(buffi.length() - 1, buffi.length());
		for (AnswerChoice answerChoice : list) {

			String cssclass="fieldcell";
			// OLD: String jscall = "answerClicked('" + answerChoice.getId() +
			// "','"
			// + web + "','" + namespace + "','" + q.getId() + "','"
			// + URLEncoder.encode(q.getText()) + "')";

			// For BIOLOG2
			String jscall = "answerClicked('" + answerChoice.getId() + "','"
					+ web + "','" + namespace + "','" + q.getId() + "')";
			
			if (q.getValue(c).contains(answerChoice)) {
				cssclass = "answerTextActive";

				
				jscall = "answerActiveClicked('" + answerChoice.getId() + "','"
						+ web + "','" + namespace + "','" + q.getId() + "')";

				// jscall = "answerActiveClicked('" + answerChoice.getId() +
				// "','"
				// + web + "','" + namespace + "','" + q.getId() + "','"
				// + URLEncoder.encode(q.getText()) + "')";
			}
			String spanid = "span_" + q.getId() + "_" + answerChoice.getId();
			buffi.append(getEnclosingTagOnClick(SPAN, ""
					+ answerChoice.getText() + " ", cssclass, jscall, "",
					spanid));
			// "setHandCursor("+"'"+spanid+"')", spanid));
			
			
			if(i<list.size()){
				buffi.append(" , ");
			}
			i++;
		}
	}

	
	
	private static String getEnclosingTagOnClick(String tag, String text,
			String cssclass, String onclick, String onmouseover, String id) {
		StringBuffer sub = new StringBuffer();
		sub.append("<" + tag);
		if (id != null && id.length() > 0) {
			sub.append(" id='" + id + "' ");
		}
		if (cssclass != null && cssclass.length() > 0) {
			sub.append(" class='" + cssclass + "'");
		}
		if (onclick != null && onclick.length() > 0) {
			sub.append(" onclick=" + onclick + "; ");
		}
		if (onmouseover != null && onmouseover.length() > 0) {
			sub.append(" onmouseover=" + onmouseover + "; ");
		}
		sub.append(">");
		sub.append(text);
		sub.append("</" + tag + ">");
		return sub.toString();

	}

	/*
	 * private static String renderQuestion(XPSCase c, Question q, String web,
	 * String namespace) { StringBuffer buffi = new StringBuffer();
	 * buffi.append(getEnclosingTag(SPAN, q.getText() + ": ",
	 * "questionText",null)); if (q instanceof QuestionChoice) {
	 * List<AnswerChoice> list = ((QuestionChoice) q).getAllAlternatives();
	 * renderChoiceAnswers(c, buffi, q, list, web, namespace); } else { //
	 * render NumInput renderNumAnswers(c, buffi, q, web, namespace); } return
	 * buffi.toString(); }
	 */

	
	/*
	 * private static String getSaveAsXCLHTMLElement(){ return "<p>" +
	 * rb.getString("KnowWE.solution") +
	 * ": <input name='xcl-solution' id='xcl-solution' value='' type='text' size='50'><input type='button' value='Save as' id='xcl-save-as'></p>"
	 * ; }
	 */

	
	/*
	 * private static String getEnclosingTag(String tag, String text, String
	 * cssclass, String id) { return getEnclosingTagOnClick(tag, text, cssclass,
	 * null, null, id); }
	 */

}
