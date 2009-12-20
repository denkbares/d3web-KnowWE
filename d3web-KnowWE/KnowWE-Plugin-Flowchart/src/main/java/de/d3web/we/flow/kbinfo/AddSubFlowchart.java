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
		String[] splitInfos = infos.split(",");
		String pageName = splitInfos[0];
		String name = splitInfos[1];
		String[] exits = new String[splitInfos.length - 2];
		for (int i = 2; i < splitInfos.length; i++) {
			exits[i -2] = splitInfos[i];
			Logging.getInstance().info(exits[i-2]);
		}

		
		// get everything to update the article
		KnowWEArticleManager artManager = KnowWEEnvironment.getInstance().getArticleManager(web);
		KnowWEArticle article = artManager.getArticle(pageName);
		KnowWEEnvironment instance = KnowWEEnvironment.getInstance();
		Section sec = article.getSection();
		KnowWEParameterMap map =  new KnowWEParameterMap(KnowWEAttributes.WEB, sec.getWeb());
		String oldText = article.getSection().getOriginalText();

		
		String id = createID(oldText);
		String before = getSurrounding(oldText)[0];
		int numberOfNodes = exits.length;
		
		String flowchart = "";
		List<String> exitsFlowHtml = new ArrayList<String>();
		
		
		// the html representation of the nodes
		
		String firstLine = "<flowchart id=\"" + id + "\" name=\"" + name + "\" icon=\"sanduhr.gif\" width=\"750\" height=\"500\" idCounter=\"" + numberOfNodes + "\">" + LINE_SEPARATOR + LINE_SEPARATOR;
		flowchart += firstLine;
		
		String startNode = "<!-- nodes of the flowchart -->" + LINE_SEPARATOR
				+ "<node id=\"#node_0\">" + LINE_SEPARATOR
				+ "<position left=\"320\" top=\"45\"></position>"
				+ LINE_SEPARATOR + "<start>Start</start>" + LINE_SEPARATOR
				+ "</node>" + LINE_SEPARATOR + LINE_SEPARATOR;
		
		flowchart += startNode;
		
		
		int currentNode = 0;
		int x = 750 / (exits.length + 2);
		int y = 400; 
		
		for (String s : exits) {
			currentNode++;
			
			// for some not yet fixed bug
			if (s.startsWith("undefinded")) {
				s = s.substring(10);
			}
			
			// to get the id
			String tempid = "#node_" + currentNode;
			int left = (x + 1) * currentNode;
			int top = y;
			
			// html of the exit node
			String exit = "<node id=\"" + tempid + "\">" + LINE_SEPARATOR
					+ "<position left=\"" + left + "\" top=\"" + top
					+ "\"></position>" + LINE_SEPARATOR + "<exit>Exit</exit>"
					+ LINE_SEPARATOR + "</node>" + LINE_SEPARATOR + LINE_SEPARATOR;
			
			flowchart += exit;
			
			
			String flowExit = "<DIV id=\"" + tempid + "\" class=\"Node\" style=\"left: " + left + "px; top: " + top + "px; width: 74px; height: 20px;\">"
			+ "<DIV class=\"exit\" style=\"width: 60px; height: 20px;\">"
			+ "<DIV class=\"decorator\" style=\"width: 25px; height: 25px;\"/>"
			+ "<DIV class=\"title\" style=\"width: 60px; height: 16px;\">Exit</DIV>"
			+ "</DIV></DIV>";
			
			exitsFlowHtml.add(flowExit);
		}

		// the flowchart div part
		String start = "<preview mimetype=\"text/html\">" + LINE_SEPARATOR
		+ "<![CDATA[" + LINE_SEPARATOR
		+ "<DIV class=\"Flowchart\" style=\" width: 751px; height: 501px;\">"
		+ "<DIV id=\"#node_0\" class=\"Node\" style=\"left: 340px; top: 45px; width: 72px; height: 20px;\">"
		+ "<DIV class=\"start\" style=\"width: 58px; height: 20px;\">"
		+ "<DIV class=\"decorator\" style=\"width: 25px; height: 25px;\"/>"
		+ "<DIV class=\"title\" style=\"width: 60px; height: 16px;\">Start</DIV>"
		+ "</DIV></DIV>";
		
		flowchart += start;
		
		for (String s : exitsFlowHtml) {
			Logging.getInstance().info("exitsFlowHtml: " + s);
			flowchart += s;
		}
		
		flowchart += "]]></preview></flowchart>";
		

		String text = getSurrounding(oldText)[0] + flowchart
				+ getSurrounding(oldText)[1];
		
		Logging.getInstance().info(text);
		instance.saveArticle(sec.getWeb(), sec.getTitle(), text, map);

		return "";
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
