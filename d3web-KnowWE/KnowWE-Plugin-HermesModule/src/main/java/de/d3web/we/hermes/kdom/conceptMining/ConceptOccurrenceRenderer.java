package de.d3web.we.hermes.kdom.conceptMining;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import de.d3web.we.hermes.kdom.TimeEventType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.Context;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.DefaultSubjectContext;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.utils.SPARQLUtil;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public abstract class ConceptOccurrenceRenderer extends KnowWEDomRenderer {

	private static String TITLE_QUERY = "SELECT  ?title WHERE {  <URI> lns:hasTitle ?title }";

	@Override
	public void render(KnowWEArticle article, Section arg0, KnowWEUserContext arg1, StringBuilder arg2) {

		String conceptName = arg0.getOriginalText();

		Context subjectContext = ContextManager.getInstance().getContext(arg0,
				DefaultSubjectContext.CID);

		String subjectString = "error: subject not found!";
		URI subjectURI = null;
		
		if (subjectContext != null) {
			 subjectURI =((DefaultSubjectContext) subjectContext)
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

		// <img src='KnowWEExtension/images/question.gif' width='12' />
		// id='" + arg0.getId() + "'

		String htmlContent1 = "<b>"
				+ arg0.getOriginalText()
				+ "</b>"
				+ "<span>"
				+ "<img rel=\"{type: '"	+ conceptName
				+ "', id: '" + arg0.getId()
				+ "', termName: '" + conceptName
				+ "', user:'" + arg1.getUsername()
				+ "'}\" class=\"conceptLink pointer\" id='"
				+ arg0.getId()
				+ "' src='KnowWEExtension/images/question.gif' width='12' /> "
				+ "</span><span id='" + arg0.getId()
				+ "_popupcontent' style='visibility:hidden;position:fixed' >";

		String popupContent = generatePopupContent(arg0, subjectURI, subjectString);
		
		if(popupContent == null) {
			arg2.append("__"+conceptName+"__"); 
			return;
		}

		String htmlContentTail = "</span>";
		arg2.append(KnowWEUtils.maskHTML(htmlContent1));
		arg2.append(popupContent);
		arg2.append(KnowWEUtils.maskHTML(htmlContentTail));

	}

	protected abstract String[] getPossibleProperties(URI subject,
			String object);

	private String generatePopupContent(Section arg0, URI subject, String subjectTitle) {
		StringBuffer buffy = new StringBuffer();

		buffy.append("<div style='padding:10px' class=\"confirmPanel\" >");

		buffy.append("<span style='font-weight:bold' >" + subjectTitle + "</span>");

		buffy.append("<div style='padding:10px' class=\"options\" >");

		String originalText = arg0.getOriginalText();
		String[] opts = getPossibleProperties(subject, originalText);

		String[] newOpts = filterOpts(subject, originalText, opts);
		
		if(newOpts.length == 0) return null;

		String[] defaultOpts = { "concept missmatch", "dont ask again" };

		Section ancestor = KnowWEObjectTypeUtils.getAncestorOfType( arg0, TimeEventType.class.getName() );
		
		for (String string : newOpts) {
			
			String options =  "kdomid='" + arg0.getId() + "' subject='" + subject 
				+ "' rel='"	+ string + "' object='" + originalText 
				+ "' name='" + string + "' " + "ancestor='" + ancestor.getId() + "'";
			
			
			buffy.append("<li><div class=\"confirmOption pointer\" " + options + ">");
			buffy.append("" + string + "  " + "");
			buffy.append("<span style='font-style:italic' class='confirmobject' "+options+">" + originalText + " </span>");
			buffy.append("<span style='font-style:italic'> ? </span>");
			buffy.append("</div></li>");
		}

		for (String string : defaultOpts) {
			buffy.append("<li><div class=\"confirmOption\" name='" + string
					+ "'>");
			buffy.append("" + string + "  " + "");
			buffy.append("</div></li>");
		}

		buffy.append("</div>");
		buffy.append("</div>");
		return buffy.toString();
	}

	private static final String RELATION_QUERY = "ASK { SUBJECT lns:RELATION lns:OBJECT .}";

	private String[] filterOpts(URI subject, String originalText,
			String[] opts) {
		
		List<String> goodOpts = new ArrayList<String>();
		
		for (String relation : opts) {

			String q = RELATION_QUERY.replaceAll("SUBJECT", "<"+subject.stringValue()+">");
			q = q.replaceAll("RELATION", relation);
			q = q.replaceAll("OBJECT", originalText);
			Boolean result = SPARQLUtil.executeBooleanQuery(q);
			if(result != null && !result.booleanValue()) {
				goodOpts.add(relation);
			}
		}
		return goodOpts.toArray(new String[goodOpts.size()]);
	}

}
