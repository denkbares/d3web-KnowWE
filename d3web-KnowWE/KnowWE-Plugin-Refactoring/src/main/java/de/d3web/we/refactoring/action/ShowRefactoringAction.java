package de.d3web.we.refactoring.action;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.action.AbstractKnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.KnowWEScriptLoader;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.decisionTree.SolutionID;
import de.d3web.we.kdom.xcl.XCList;

/**
 * @author Franz Schwab
 */
public class ShowRefactoringAction extends AbstractKnowWEAction {
	KnowWEParameterMap pm;

	String id;
	String topic;
	String web;
	KnowWEArticleManager am;
	KnowWEArticle a;
	Section<?> as;
	

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		pm = parameterMap;
		initAttributes();
		return perform();
	}

	private void initAttributes() {
		id = pm.get("refactoringElement");
		topic = pm.getTopic();
		web = pm.getWeb();
		am = KnowWEEnvironment.getInstance().getArticleManager(web);
		a = am.getArticle(topic);
		as = a.getSection();
	}
	

	public String perform() {
		KnowWEArticle article = KnowWEEnvironment.getInstance()
						.getArticleManager(web).getArticle(topic);
				Section articleSection = article.getSection();
		List<Section> sections = new ArrayList<Section>();
		articleSection.findSuccessorsOfType(XCList.class, sections);
		StringBuilder html = new StringBuilder();
		KnowWEScriptLoader.getInstance().add("RefactoringPlugin.js", false);
		// oder veraltete Möglichkeit: html.append("<script type=text/javascript src=KnowWEExtension/scripts/RefactoringPlugin.js></script>\n");
		//html.append("<div id='refactoring-result'></div>");
		html.append("<fieldset><div class='left'>");
		html.append("<p>Es wurden <strong>x</strong> Refactorings gefunden. Bitte wählen Sie das gewünschte Refactoring aus.</p></div>");
		html.append("<div style='clear:both'></div><form name='refactoringform'><div class='left'><label for='article'>Refactoring</label>");
		html.append("<select name='refactoringselect'>");
		for(Section s:sections) {
			html.append("<option value='");
			html.append(s.getId());
			html.append("'>");
			List<Section> ls = new ArrayList<Section>();
			s.findSuccessorsOfType(SolutionID.class, ls);
			html.append(ls.get(0).getOriginalText());
			html.append("</option>");
		}                
		html.append("</select></div><div><input type='button' value='» Ausführen' name='submit' class='button' onclick='refactoring(\"" + id + "\");'/></div></fieldset>");
		
//		StringBuilder html = new StringBuilder("[{Groovy debug='false'" +
//				as.findChild(id).getOriginalText() +
//				"}]");

		return html.toString();
	}
}
