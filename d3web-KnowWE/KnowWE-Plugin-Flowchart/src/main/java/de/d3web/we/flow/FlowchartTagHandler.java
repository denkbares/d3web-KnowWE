package de.d3web.we.flow;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import util.ResStream;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.logging.Logging;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;


/**
 * [{KnowWEPlugin Flowchart}]
 * @author Florian Ziegler
 */
public class FlowchartTagHandler extends AbstractTagHandler {
	
//	private String topic;
//	private KnowWEUserContext user;
//	private ResourceBundle rb;

	public FlowchartTagHandler() {
		super("Flowchart");
		Logging.getInstance().addHandlerToLogger(
				Logging.getInstance().getLogger(), "flowchartTagHandler.txt");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

//		this.topic = topic;
//		this.user = user;
//		this.rb = D3webModule.getKwikiBundle_d3web(user);
		
		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(topic);
//		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
//		Section sec = article.getSection();
//		KnowWEParameterMap map =  new KnowWEParameterMap(KnowWEAttributes.WEB, sec.getWeb());
		String text = article.getSection().getOriginalText();
		
		return createPreview(text);
	}

	private String createPreview(String text) {
		int startPos = text.lastIndexOf("<preview mimetype=\"text/html\">");
		int endPos = text.lastIndexOf("</preview>");
		if (startPos >= 0 && endPos >= 0) {
			return "<div style='zoom: 50%; cursor: pointer;'>"
					+ "<link rel='stylesheet' type='text/css' href='cc/kbinfo/dropdownlist.css'></link>"
					+ "<link rel='stylesheet' type='text/css' href='cc/kbinfo/objectselect.css'></link>"
					+ "<link rel='stylesheet' type='text/css' href='cc/kbinfo/objecttree.css'></link>"
					+ "<link rel='stylesheet' type='text/css' href='cc/flow/flowchart.css'></link>"
					+ "<link rel='stylesheet' type='text/css' href='cc/flow/floweditor.css'></link>"
					+ "<link rel='stylesheet' type='text/css' href='cc/flow/guard.css'></link>"
					+ "<link rel='stylesheet' type='text/css' href='cc/flow/node.css'></link>"
					+ "<link rel='stylesheet' type='text/css' href='cc/flow/nodeeditor.css'></link>"
					+ "<link rel='stylesheet' type='text/css' href='cc/flow/rule.css'></link>"
					+ "<style type='text/css'>div, span, a { cursor: pointer !important; }</style>"
					+ text.substring(startPos + 43, endPos - 8) + "</div>";
		}
		return "you shouldn't read this :(";
	}
}
