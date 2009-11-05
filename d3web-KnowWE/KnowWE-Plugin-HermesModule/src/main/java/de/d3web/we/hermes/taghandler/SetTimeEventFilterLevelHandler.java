package de.d3web.we.hermes.taghandler;

import java.util.Map;

import de.d3web.we.hermes.HermesUserManagement;
import de.d3web.we.taghandler.AbstractTagHandler;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class SetTimeEventFilterLevelHandler extends AbstractTagHandler {

	public SetTimeEventFilterLevelHandler() {
		super("filterEventLevel");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		Integer currentLevel = HermesUserManagement.getInstance().getEventFilterLevelForUser(user.getUsername());
		int currInt = -1;
		if(currentLevel != null) {
			currInt = currentLevel.intValue();
		}
		
		StringBuffer buffy = new StringBuffer();
		
		buffy.append("Ereignisse anzeigen ab Stufe:");
		
		buffy.append("<br>");
		
		
		//quick & dirty		
		wrappBoldIf("<a href=\"\" onclick=\"sendFilterLevel('0','"+user.getUsername()+"')\">  0  </a>", buffy, currInt, 0);
		
		

		
		wrappBoldIf("<a href=\"\" onclick=\"sendFilterLevel('1','"+user.getUsername()+"')\"> 1  </a>", buffy, currInt, 1);
		
		
		wrappBoldIf("<a href=\"\" onclick=\"sendFilterLevel('2','"+user.getUsername()+"')\"> 2  </a>", buffy, currInt, 2);
		
		
		
		wrappBoldIf("<a href=\"\" onclick=\"sendFilterLevel('3','"+user.getUsername()+"')\"> 3  </a>", buffy, currInt, 3);
		
		
		
		return buffy.toString();
	}

	private void wrappBoldIf(String string, StringBuffer buffy, int currInt,
			int i) {
		if(currInt == i) {
			buffy.append("<b>");
		}
		buffy.append(string);
		if(currInt == i) {
			buffy.append("</b>");
		}
		
	}

}
