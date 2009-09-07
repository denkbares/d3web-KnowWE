package de.d3web.we.action;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ResourceBundle;

import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.utils.KnowWEUtils;

public class SessionChooserRenderer implements KnowWEAction {

	public FilenameFilter filter;
	
	public SessionChooserRenderer(String newId) {
		filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		};
	}

	public String perform(KnowWEParameterMap map) {
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_messages");
		StringBuffer sb = new StringBuffer();
		String sessionPath = KnowWEUtils.getSessionPath(map);
		File sessionPathFile = new File(sessionPath);
		String linkAction = map.get(KnowWEAttributes.LINK_ACTION);
		String user = map.get(KnowWEAttributes.USER);
		String web = map.get(KnowWEAttributes.WEB);
		sb.append("<table>");
		File[] files = sessionPathFile.listFiles(filter);
		if(files == null || files.length == 0) {
			sb.append("<tr>");
			sb.append("<div class='patternButton' onmouseover=\"replanToHide(this,event)\" onmouseout=\"planToHide(this,event)\" style='clear:left; text-align:right; width:100%;'><a href='#' onclick='hideSessionChooser();' rel='nofollow' title='"+rb.getString("KnowWE.session.noSessionsAvailable")+"'>"+rb.getString("KnowWE.session.noSessionsAvailable")+"</a></div>");		
			sb.append("</tr>");
		} else {
			for (File file : sessionPathFile.listFiles(filter)) {
				String link = "kwiki_call(\"/KnowWE.jsp?action="+linkAction+"&KWikiUser="+user+"&KWikiWeb="+web+"&"+KnowWEAttributes.SESSION_FILE+"="+file.getName()+"\");hideSessionChooser();";
				sb.append("<tr>");
				sb.append("<div class='patternButton' style='clear:left; text-align:right; width:100%;'><a href='#' onclick='"+link+ "' rel='nofollow' title='Choose session'>"+file.getName()+"</a></div>");		
				sb.append("</tr>");
			}
		}
		sb.append("</table>");
		sb.append("</div>");
		
		return sb.toString();
	}

}
