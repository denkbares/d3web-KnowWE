package de.d3web.we.flow.kbinfo;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.action.KnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.logging.Logging;

/**
 * @author Florian Ziegler
 */
public class AddSubFlowchart implements KnowWEAction{

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Override
	public boolean isAdminAction() {
		return false;
	}

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		
		Logging.getInstance().addHandlerToLogger(Logging.getInstance().getLogger(), "AddSub.txt");
		// get everything important from the parameter map
		String web = parameterMap.getWeb();
		String infos = parameterMap.get("infos");
//		String[] splitInfos = infos.split(",");
//		String pageName = splitInfos[0];
//		String name = splitInfos[1];
//		String[] exits = new String[splitInfos.length - 2];
//		for (int i = 2; i < splitInfos.length; i++) {
//			exits[i -2] = splitInfos[i];
//			Logging.getInstance().info(exits[i-2]);
//		}
		
		String pageName = infos.substring(infos.indexOf("[Pagename]") + 10, infos.indexOf("[Name]"));
		String name = infos.substring(infos.indexOf("[Name]") + 6, infos.indexOf("[Nodes]"));
		String nodesToLine = infos.substring(infos.indexOf("[Nodes]") + 7).replace("[next]", ":next:");
		String[] exits = nodesToLine.split(":next:");
		
		
		// get everything to update the article
		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(pageName);
		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		Section sec = article.getSection();
		String oldText = article.getSection().getOriginalText();

		
		String id = createID(oldText);
		int numberOfNodes = exits.length;
		
		String flowchart = "";
		
		
		String preview = "<preview mimetype=\"text/html\">" + LINE_SEPARATOR
		+ "<![CDATA[" + LINE_SEPARATOR
		+ "<DIV class=\"Flowchart\" style=\" width: 751px; height: 501px;\">" + LINE_SEPARATOR 
		+ "<DIV id=\"#node_0\" class=\"Node\" style=\"left: 340px; top: 45px; width: 72px; height: 20px;\">" + LINE_SEPARATOR 
		+ "<DIV class=\"start\" style=\"width: 58px; height: 20px;\">" + LINE_SEPARATOR 
		+ "<DIV class=\"decorator\" style=\"width: 25px; height: 25px;\">" + LINE_SEPARATOR 
		+ "</DIV>" + LINE_SEPARATOR 
		+ "<DIV class=\"title\" style=\"width: 60px; height: 16px;\">Start</DIV>"+ LINE_SEPARATOR 
		+ "</DIV>"+ LINE_SEPARATOR 
		;
		
		// the html representation of the nodes
		
		flowchart += "<flowchart id=\"" + id + "\" name=\"" + name + "\" icon=\"sanduhr.gif\" width=\"750\" height=\"500\" idCounter=\"" + numberOfNodes + "\">" + LINE_SEPARATOR + LINE_SEPARATOR;
		
		String startNode = "<!-- nodes of the flowchart -->" + LINE_SEPARATOR
				+ "<node id=\"#node_0\">" + LINE_SEPARATOR
				+ "<position left=\"320\" top=\"45\"></position>"
				+ LINE_SEPARATOR + "<start>Start</start>" + LINE_SEPARATOR
				+ "</node>" + LINE_SEPARATOR + LINE_SEPARATOR;
		
		flowchart += startNode;
		
		
		int currentNode = 0;
		int x = 450 / (exits.length + 2);
		int y = 400; 
		
		for (String s : exits) {
			currentNode++;
			
			// to get the id
			String tempid = "#node_" + currentNode;
			int left = (x + 1) * currentNode;
			int top = y;
			
			// html of the exit node
			String exit = "<node id=\"" + tempid + "\">" + LINE_SEPARATOR
					+ "<position left=\"" + left + "\" top=\"" + top
					+ "\"></position>" + LINE_SEPARATOR + "<exit>"+ s + "</exit>"
					+ LINE_SEPARATOR + "</node>" + LINE_SEPARATOR + LINE_SEPARATOR;
			
			flowchart += exit;
			
			
			preview += "<DIV id=\"" + tempid + "\" class=\"Node\" style=\"left: " + left + "px; top: " + top + "px; width: 74px; height: 20px;\">" + LINE_SEPARATOR 
			+ "<DIV class=\"exit\" style=\"width: 60px; height: 20px;\">" + LINE_SEPARATOR 
			+ "<DIV class=\"decorator\" style=\"width: 25px; height: 25px;\"> </DIV>" + LINE_SEPARATOR 
			+ "<DIV class=\"title\" style=\"width: 60px; height: 16px;\">" + s +"</DIV>" + LINE_SEPARATOR 
			+ "</DIV></DIV>" + LINE_SEPARATOR ;
			
		}

		// the flowchart div part
	
		
		preview += "</DIV> ]]>" + LINE_SEPARATOR + 
				"</preview>" + LINE_SEPARATOR;
				
				
		flowchart += preview;
		
		flowchart += "</flowchart>" + LINE_SEPARATOR;

		String text = getSurrounding(oldText)[0] + flowchart
				+ getSurrounding(oldText)[1];
		
		Logging.getInstance().info(text);
		instance.saveArticle(sec.getWeb(), sec.getTitle(), text, parameterMap);

		return "success";
	}
	
	private String createID(String text) {
		
		if (text.contains("<flowchart")) {
			String[] flowcharts = text.split("<flowchart id=\"");
			
			String tempid = flowcharts[flowcharts.length - 1].substring(2, flowcharts[flowcharts.length - 1].indexOf("\""));
			int number = Integer.valueOf(tempid) + 1;
			
			String leadingZeros = "";
			for (char c : tempid.toCharArray()) {
				if (c == '0') {
					leadingZeros += "0";
				} else {
					break;
				}
			}	

			return "sh" + leadingZeros + number;
			
		} else {
			return "sh001";
		}
	}
	
	private String[] getSurrounding (String text) {
		String before = "";
		String after = "";
		String[] surrounding = new String[2];
		if (text.contains("<Kopic>")) {
			before = text.substring(0, text.indexOf("<Kopic>"));
			after = text.substring(text.indexOf("<Kopic>"));
		} else if (text.contains("<Questions-section>")){
			before = text.substring(0, text.indexOf("<Questions-section>"));
			after = text.substring(text.indexOf("<Questions-section>"));
		} else if (text.contains("<Solutions-section>")) {
			before = text.substring(0, text.indexOf("<Solutions-section>"));
			after = text.substring(text.indexOf("<Solutions-section>"));
		} else {
			before = text;
		}
		surrounding[0] = before;
		surrounding[1] = after;
		
		return surrounding;
	}

}
