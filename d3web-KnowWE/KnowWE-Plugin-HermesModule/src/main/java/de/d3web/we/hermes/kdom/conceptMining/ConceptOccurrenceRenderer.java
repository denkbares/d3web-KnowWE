package de.d3web.we.hermes.kdom.conceptMining;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.Context;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.SPARQLUtil;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ConceptOccurrenceRenderer extends KnowWEDomRenderer {


	private static String TITLE_QUERY = "SELECT  ?title WHERE {  <URI> lns:hasTitle ?title }";


	@Override
	public void render(Section arg0, KnowWEUserContext arg1, StringBuilder arg2) {

		String conceptName = arg0.getOriginalText();

		Context subjectContext = ContextManager.getInstance().getContext(arg0,
				DefaultSubjectContext.CID);

		String subjectString = "error: subject not found!";

		if (subjectContext != null) {
			URI subjectURI = ((DefaultSubjectContext) subjectContext)
					.getSolutionURI();
			subjectString = subjectURI.getLocalName();
			TupleQueryResult result = SPARQLUtil.executeTupleQuery(TITLE_QUERY
					.replaceAll("URI", subjectURI.toString()), arg0.getTitle());
			if (result != null) {
				try {
					if (result.hasNext()) {
						BindingSet set = result.next();
						String title = set.getBinding("title").getValue()
								.stringValue();
						try {
							title = URLDecoder.decode(title, "UTF-8");
							subjectString = title;
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (QueryEvaluationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		//<img src='KnowWEExtension/images/question.gif' width='12' />
// id='" + arg0.getId() + "'
		
		String htmlContent1 = "<b>" + arg0.getOriginalText() + "</b>"
				+ "<span   "
				+ ">" + "<img rel=\"{type: '" + conceptName + "', id: '" + arg0.getId()
				+ "', termName: '" + conceptName + "', user:'"
				+ arg1.getUsername() + "'}\" class=\"conceptLink\" id='" + arg0.getId() + "' src='KnowWEExtension/images/question.gif' width='12' ></img> " + "</span><span id='"
				+ arg0.getId()
				+ "_popupcontent' style='visibility:hidden;position:fixed' >";

		String popupContent = generatePopupContent(arg0, subjectString);

		String htmlContentTail = "</span>";
		arg2.append(KnowWEEnvironment.maskHTML(htmlContent1));
		arg2.append(popupContent);
		arg2.append(KnowWEEnvironment.maskHTML(htmlContentTail));

	}

	private String generatePopupContent(Section arg0, String subject) {
		StringBuffer buffy = new StringBuffer();

		buffy.append("<div style='padding:10px' class=\"confirmPanel\" >");

		buffy.append("<span style='font-weight:bold' >"
				+ subject + "</span><br>");

		buffy.append("<div style='padding:10px' class=\"options\" >");

		String[] opts = { "involves"};
		
		String[] defaultOpts = { "concept missmatch", "dont ask again" };

		for (String string : opts) {
			buffy.append("<li><div class=\"confirmOption\" kdomid='"+arg0.getId()+"' subject='"+subject+"' rel='"+string+"' object='"+arg0.getOriginalText()+"' name='" + string + "'>");
			buffy.append("" + string +"  "+ "");
			buffy.append("<span style='font-style:italic'  class='confirmobject'>"+arg0.getOriginalText()+" </span>");
			buffy.append("<span style='font-style:italic'> ? </span>");
			buffy.append("</div></li>");
		}

		buffy.append("<br>");
		
		for (String string : defaultOpts) {
			buffy.append("<li><div class=\"confirmOption\" name='" + string + "'>");
			buffy.append("" + string +"  "+ "");
			buffy.append("</div></li>");
		}
		
		buffy.append("</div>");
		buffy.append("</div>");
		return buffy.toString();
	}

}
