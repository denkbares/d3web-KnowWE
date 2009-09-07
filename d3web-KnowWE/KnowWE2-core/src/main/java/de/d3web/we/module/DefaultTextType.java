package de.d3web.we.module;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.owlextension.Extension;
import de.d3web.we.kdom.owlextension.OwlProperties;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.kdom.sectionFinder.AllTextFinder;
import de.d3web.we.kdom.semanticAnnotation.SemanticAnnotation;
import de.d3web.we.kdom.semanticFactSheet.Info;
import de.d3web.we.kdom.sparql.Sparql;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class DefaultTextType extends DefaultAbstractKnowWEObjectType {

	public static final String BRACKET_OPEN = "KNOWWE_BRACKET_OPEN";
	public static final String BRACKET_CLOSED = "KNOWWE_BRACKET_CLOSED";
	public static final String SPLITTER = "KNOWWE_SPLITTER";
	public static final String BLOB = "%-DONE-%";

	public static DefaultTextType instance = null;

	boolean initialzed = false;

	public static DefaultTextType getInstance() {
		if (instance == null) {
			instance = new DefaultTextType();

		}

		return instance;
	}

	public void addActionRenderer(
			Map<Class<? extends KnowWEAction>, KnowWEAction> map) {

	}


	// TODO factor me OUT!
	public static final String KnowWE_JSP_PATH = "KnowWE.jsp";

	public static String getRenderedInput(String questionid, String question,
			String namespace, String userName, String title, String text,
			String type) {
		question = URLEncoder.encode(question);
		// text=URLEncoder.encode(text);
		String func = KnowWE_JSP_PATH + "?renderer=semAno&namespace="
				+ namespace + "&" + KnowWEAttributes.SEMANO_OBJECT_ID + "="
				+ questionid + "&TermName=" + question
				+ "&KWikiWeb=default_web" + "&TermType=symptom&KWikiUser="
				+ userName;
		String rendering = "<span class=\"semLink\"><a href=\"javascript:void(0);\" title=\""
				+ title
				+ " TYPE: "
				+ type
				+ "\" onclick=\"return olgetajax('"
				+ func
				+ "',kajaxwrapper,300, 'ovfl1');\" onmouseout=\"return nd();\">"
				+ text + "</a></span>";
		return rendering;

	}

	public static String getErrorQ404(String question, String text) {
		String rendering = "<span class=\"semLink\"><a href=\"javascript:void(0);\" title=\""
				+ "Question not found:"
				+ question
				+ "\" >"
				+ text
				+ "</a></span>";
		return rendering;

	}

	public static String getRenderedInputWithoutTitle(String questionid,
			String question, String namespace, String userName, String text) {
		try {
			question = URLEncoder.encode(question,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String func = KnowWE_JSP_PATH + "?renderer=semAno&namespace="
				+ namespace + "&" + KnowWEAttributes.SEMANO_OBJECT_ID + "="
				+ questionid + "&TermName=" + question
				+ "&KWikiWeb=default_web" + "&TermType=symptom&KWikiUser="
				+ userName;
		String rendering = "<span class=\"semLink\"><a href=\"javascript:void(0);\" onclick=\"return olgetajax('"
				+ func
				+ "',kajaxwrapper,300, 'ovfl1');\" onmouseout=\"return nd();\">"
				+ text + "</a></span>";
		return rendering;

	}

	@Override
	public void init() {
		if (!initialzed) {		        		
			childrenTypes.add(new Sparql());
			childrenTypes.add(new Extension());
			childrenTypes.add(new OwlProperties());
			childrenTypes.add(new SemanticAnnotation());
			childrenTypes.add(new Info());			
			sectionFinder = new AllTextFinder(this);
			initialzed = true;
		}
	}

	public static String getErrorUnknownConcept(String op, String text) {
		String rendering = "<span class=\"semLink\"><a href=\"javascript:void(0);\" title=\""
				+ "Concept not found:" + op + "\" >" + text + "</a></span>";
		return rendering;

	}

	public String performAction(String action, KnowWEParameterMap parameterMap) {
		return "action not found";
	}

	@Override
	public Collection<Section> getAllSectionsOfType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "TextModule";
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return SpecialDelegateRenderer.getInstance();
	}


}
