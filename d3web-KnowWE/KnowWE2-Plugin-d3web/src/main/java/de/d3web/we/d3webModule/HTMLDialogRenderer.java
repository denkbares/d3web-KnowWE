package de.d3web.we.d3webModule;

import java.net.URLEncoder;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerNum;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;

public class HTMLDialogRenderer {

	private static ResourceBundle kwikiBundle = ResourceBundle.getBundle("KnowWE_messages");
	
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
		buffi.append(getDialogPluginHeader());
		buffi.append("<div id='dialog' class='hidden'>");

		for (de.d3web.kernel.domainModel.qasets.QContainer container : containers) {
			if (container.getText().endsWith("Q000"))
				continue;

			buffi.append("<div class='qcontainer' id='" + container.getId() + "'>");
			buffi.append("<h4 class='qcontainerName'><a href='javascript:showDialogElement(\"" + container.getId() + "\")'>");
			buffi.append("<img src='KnowWEExtension/images/arrow_down.png' border='0'/>");
			buffi.append(container.getText() + ": ");
			buffi.append("</a></h4>");
//			buffi.append(getEnclosingTag(SPAN, container.getText() + ": ",
//					"qcontainerName",null));
			
			
			buffi.append("<table class='hidden'><tbody>");
			java.util.List<? extends NamedObject> questions = container
					.getChildren();
			for (NamedObject namedObject : questions) {
				Question q = null;
				if (namedObject instanceof Question) {
					q = (Question) namedObject;

				} else {
					continue;
				}
				

				buffi.append(TR_TAG);
//				buffi.append(getEnclosingTag(SPAN, renderQuestion(c, q, web, b
//						.getId()), "question",null));
				buffi.append(render(c, q, web, b.getId()));
				buffi.append(TR_TAG_CLOSE +  "\n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
			}
			buffi.append("</tbody></table>");
			buffi.append("</div>");
		}
		buffi.append("</div>");
		return buffi.toString();
	}
	
	/**
	 * <p>Creates the header of the dialog extension.</p>
	 * @return HTML string
	 */
	private static String getDialogPluginHeader(){
		StringBuffer html = new StringBuffer();
		html.append("<h3>Dialog</h3>");
		html.append("<h5><a href='javascript:showDialog()' alt='show dialog' title='show dialog'>");
		html.append("<img border='0' src='KnowWEExtension/images/arrow_down.png' alt='arrow down' title='arrow down'/>");
		html.append(kwikiBundle.getString("KnowWE.dialog.show") + "</a></h5>");
		
		return html.toString();
	}
	
	private static String render(XPSCase c, Question q, String web, 
			String namespace){
		StringBuffer html = new StringBuffer();
		html.append("<td class='labelcell'><label for='" +  q.getId() + "'>" 
				+ q.getText() + ": </label></td>");
		
		html.append("<td class='fieldcell'><div id='" +  q.getId() + "'>");
		if(q instanceof QuestionChoice){
			List<AnswerChoice> list = ((QuestionChoice) q).getAllAlternatives();
			renderChoiceAnswers(c, html, q, list, web, namespace);
		} else {
			renderNumAnswers(c, html, q, web, namespace);
		}
		html.append("</div></td>");
		return html.toString();
	}

	private static String renderQuestion(XPSCase c, Question q, String web,
			String namespace) {
		StringBuffer buffi = new StringBuffer();
		buffi.append(getEnclosingTag(SPAN, q.getText() + ": ", "questionText",null));
		if (q instanceof QuestionChoice) {
			List<AnswerChoice> list = ((QuestionChoice) q).getAllAlternatives();
			renderChoiceAnswers(c, buffi, q, list, web, namespace);
		} else {
			// render NumInput
			renderNumAnswers(c, buffi, q, web, namespace);
		}
		return buffi.toString();
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
		for (AnswerChoice answerChoice : list) {
			String cssclass = "answerText";
//			String jscall = "answerClicked('" + answerChoice.getId() + "','"
//					+ web + "','" + namespace + "','" + q.getId() + "','"
//					+ URLEncoder.encode(q.getText()) + "')";
					
            //For BIOLOG2
			String jscall = "answerClicked('" + answerChoice.getId() + "','"
			+ web + "','" + namespace + "','" + q.getId() + "')";					
					
			if (q.getValue(c).contains(answerChoice)) {
				cssclass = "answerTextActive";

				jscall = "answerActiveClicked('" + answerChoice.getId() + "','"
						+ web + "','" + namespace + "','" + q.getId() +  "')";
				
//				jscall = "answerActiveClicked('" + answerChoice.getId() + "','"
//						+ web + "','" + namespace + "','" + q.getId() + "','"
//						+ URLEncoder.encode(q.getText()) + "')";
			}
			String spanid = "span_" + q.getId() + "_" + answerChoice.getId();
			buffi.append(getEnclosingTagOnClick(SPAN, "&nbsp;"
					+ answerChoice.getText() + " ", cssclass, jscall,
					"", spanid));
//			        "setHandCursor("+"'"+spanid+"')", spanid));
			buffi.append(", ");

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

	private static String getEnclosingTag(String tag, String text,
			String cssclass, String id) {
		return getEnclosingTagOnClick(tag, text, cssclass, null, null, id);
	}

}
