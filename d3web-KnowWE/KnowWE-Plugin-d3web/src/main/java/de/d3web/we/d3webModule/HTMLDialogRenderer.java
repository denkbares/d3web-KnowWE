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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.Answer;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.QuestionChoice;
import de.d3web.core.session.XPSCase;
import de.d3web.core.session.values.AnswerChoice;
import de.d3web.core.session.values.AnswerNum;

/**
 * Class for rendering the HTML table-based interview
 */
public class HTMLDialogRenderer {

	/**
	 * Renders the HTML table-based interview.
	 * @param c the given XPS case
	 * @param web 	
	 * @return the String that represents the interview in HTML encoding
	 */
	public static String renderDialog(XPSCase c, String web) {

		// get the current knowledge base
		KnowledgeBase b = c.getKnowledgeBase();
		
		// get all qcontainers of kb into a list
		java.util.List<de.d3web.core.knowledge.terminology.QContainer> containers = b
				.getQContainers();
		
		StringBuffer buffi = new StringBuffer();
		
		// for formatting the specific dialog panel
		buffi.append("<div class='dialogstyle''>"
						+ "<b class='top'><b class='b1'></b><b class='b2'></b><b class='b3'>"
						+ "</b><b class='b4'></b></b><div class='boxcontent'>");

		// add plugin header
		buffi.append(getDialogPluginHeader());
		
		// markup the whole dialog with own id and class
		buffi.append("<div id='dialog' class='dialogstyle' style='display:inline'>");
		boolean first = true;
		
		// go through all qcontainers of the knowledge base
		for (de.d3web.core.knowledge.terminology.QContainer container : containers) {
			
			// skipt the rootiest root element "Q000"
			if (container.getName().endsWith("Q000"))
				continue;

			// the first element of the dialog should be extended so the user sees where
			// he has to do something
			if(first){
				buffi.append("<div class='qcontainer' id='" + container.getId() + "'>");
				buffi.append("<h4 class='qcontainerName pointer extend-htmlpanel-down'>");
				buffi.append("  " + container.getName() + ": ");
				buffi.append("</h4>");
				buffi.append("<table id='tbl" + container.getId() + "' class='visible'><tbody>");
				first = false;
			} else {
				buffi.append("<div class='qcontainer' id='" + container.getId() + "'>");
				buffi.append("<h4 class='qcontainerName pointer extend-htmlpanel-right'>");
				buffi.append("  " + container.getName() + ": ");
				buffi.append("</h4>");
				buffi.append("<table id='tbl" + container.getId() + "' class='hidden'><tbody>");
			}
			
			// get all question objects for the given qcontainer = questionnaire
			java.util.List<? extends NamedObject> questions = container.getChildren();
			
			// counter, to be able to format the table lines alternatingly
			int i = -1;
			// go through all question objects
			for (NamedObject namedObject : questions) {
	
				Question q = null;
				if (namedObject instanceof Question) {
					q = (Question) namedObject;		// cast element to Question of possible
				} else {	
					continue;						// otherwise jump to next element in list								
				}
				
				// increase counter
				i += 1;
				
				// if there are no follow-up questions
				if(q.getChildren().isEmpty()){	
					
					// assigns different css classes according to whether nr/line is
					// odd or even
					if(i==0){
						buffi.append(getTableRowString(c, q, web, b.getId(), false, "class='trEven first'"));
					} else if (i % 2 == 0) {
						buffi.append(getTableRowString(c, q, web, b.getId(), false, "class='trEven'"));
					} else {
						buffi.append(getTableRowString(c, q, web, b.getId(), false, "class='trOdd'"));
					}
				
				// if there are follow-up questions
				} else {
				
					// first render their initiating root element 
					if (i % 2 == 0) {
						buffi.append(getTableRowString(c, q, web, b.getId(), false, 
								"id='" + q.getId() + "' class='follow pointer extend-htmlpanel-right-s'"));	
					} else {
						buffi.append(getTableRowString(c, q, web, b.getId(), false, 
								"id='" + q.getId() + "' class='follow pointer extend-htmlpanel-right-s'"));
					}
					
					// then assemble the StringBuffer that contains all follow up questions
					StringBuffer ch = new StringBuffer();
					for(NamedObject cset : q.getChildren()){
						getFollowUpChildrenRekur(ch, (Question)cset, c, web, b.getId(), true, 35, q);
					}
					buffi.append(ch.toString());
				}
			}
			buffi.append("</tbody></table>");
			buffi.append("</div>"); // qcontainer div
		}
		buffi.append("</div>"); // dialog div
		buffi.append("</div>"); // box content div
		buffi.append("<b class='bottom'><b class='b4b'></b><b class='b3b'></b>" // inset
				+ "<b class='b2b'></b><b class='b1b'></b></b>" + "</div>"); 	// div
		return buffi.toString();
	}
	
	
	/**
	 * assembles a StringBuffer, that represents one table row, that is a question and
	 * its answer alternatives
	 */
	 private static String getTableRowString(XPSCase c, Question q, String web,
			  String namespace, boolean even, String classDecl){
		 StringBuffer buffi = new StringBuffer();
		 buffi.append("<tr " + classDecl + ">");
		 buffi.append(render(c, q, web, namespace, even));
		 buffi.append("</tr> \n");
		 return buffi.toString();
	  }
	
	 
	/**
	 * gets all follow-up questions of the given Question "set" recursively and assembles
	 * them into a StringBuffer decorated with HTML tags
	 */
	private static StringBuffer getFollowUpChildrenRekur(StringBuffer children, Question set,
			XPSCase c, String web, String namespace, boolean bool, int indent, Question parent){
		//increase the indentation with every hierarchical descendance = each recursion level
		indent +=15;
		
		// add the table-row HTML, assign the id of the clicked root element that initiates the
		// follow-up questions and set each element hidden for the first rendering
		children.append("<tr id='" + parent.getId() + "' class='trf hidden'");
		children.append(renderFollowUpQuestion(c, set, web, namespace, indent));
		children.append("</tr> \n");
		
		// as long as the follow-up question has further child elements, call the method
		// recursively for each of them
		if(!set.getChildren().isEmpty()){
			for(NamedObject cset : set.getChildren()){
				getFollowUpChildrenRekur(children, (Question)cset, c, web, namespace, !bool, indent, parent);
			}
		} 
		return children;
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
		html.append("Interview");
		html.append("</h3>");
		return html.toString();
	}

	private static String render(XPSCase c, Question q, String web,
			String namespace, boolean even) {
		StringBuffer html = new StringBuffer();

		if (even) {
			html.append("<td class='labelcellEven'>" + q.getName() + "</td>");
		} else {
			html.append("<td class='labelcellOdd'>" + q.getName() + "</td>");
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
	
	
	private static String renderFollowUpQuestion(XPSCase c, Question q, 
			String web, String namespace, int indent){
		
		StringBuffer html = new StringBuffer();
			
			html.append("<td class='labelcellFollow' style='margin: 0px 0px 0px " + indent+"px;'>" 
					+ q.getName() + " </td>");
			html.append("<td class='fieldcellFollow'><div id='" + q.getId() + "'>");
			
			if (q instanceof QuestionChoice) {
				List<AnswerChoice> list = ((QuestionChoice) q).getAllAlternatives();
				renderChoiceAnswers(c, html, (Question)q, list, web, namespace);

				// to remove the last comma after answers in built in dialog
				html.delete(html.length() - 2, html.length());

			} else {
				renderNumAnswers(c, html, q, web, namespace);
			}
		
		
		html.append("</div></td>");
		
		return html.toString();
	}
	

	private static void renderNumAnswers(XPSCase c, StringBuffer buffi,
			Question q, String web, String namespace) {
		String value = "";
		if (q.hasValue(c)) {
			Answer answer = q.getValue(c);
			if (answer != null && answer instanceof AnswerNum) {
				value = Double.toString((Double) ((AnswerNum) answer).getValue(c));
			}
		}
		String id = "numInput_" + q.getId();
		String jscall = " rel=\"{oid: '"+q.getId()+"'," 
			    + "web: '"+web+"',"
			    + "ns: '"+namespace+"',"
			    + "qtext: '"+URLEncoder.encode(q.getName())+"',"
			    + "inputid: '"+id+"'"
				+ "}\" ";
		buffi.append("<input id='" + id + "' type='text' " 
				+ "value='" + value + "' "
				+ "class='numInput num-cell-down' " 
				+ "size='7' " 
				+ jscall + ">");
		buffi.append("<input type='button' value='ok' class=\"num-cell-ok\">");
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
			
			// For BIOLOG2
			String jscall = " rel=\"{oid: '"+answerChoice.getId()+"'," 
				    + "web: '"+web+"',"
				    + "ns: '"+namespace+"',"
				    + "qid: '"+q.getId()+"'"
					+ "}\" ";
				
			if (q.getValue(c)!=null && q.getValue(c).equals(answerChoice)) {
				cssclass = "fieldcell answerTextActive";
			}
			String spanid = "span_" + q.getId() + "_" + answerChoice.getId();
			buffi.append(getEnclosingTagOnClick("span", ""
					+ answerChoice.getText() + " ", cssclass, jscall, null,
					spanid));
			
			
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
			sub.append(" " + onclick + " ");
		}
		if (onmouseover != null && onmouseover.length() > 0) {
			sub.append(" " + onmouseover + " ");
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
