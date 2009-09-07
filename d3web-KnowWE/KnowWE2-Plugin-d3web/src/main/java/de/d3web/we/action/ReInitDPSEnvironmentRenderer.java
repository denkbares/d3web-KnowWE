package de.d3web.we.action;

import java.util.ResourceBundle;

import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;


public class ReInitDPSEnvironmentRenderer implements KnowWEAction{

	private static ResourceBundle kwikiBundle = ResourceBundle.getBundle("KnowWE_messages");

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		long time1 = System.currentTimeMillis();
		String web = parameterMap.get(KnowWEAttributes.WEB);
		DPSEnvironment dpse = DPSEnvironmentManager.getInstance().getEnvironments(web);
		dpse.reInitialize();
		//((GlobalTerminologyRenderer) model.getWebApp().getRenderer("KWiki_globalTerminology")).reInitialize(model, web);
		
		long time2 = System.currentTimeMillis();
		long diff = time2 - time1;
		
		StringBuffer html = new StringBuffer();
		
		html.append("<p class=\"box info\">");
		html.append("<a href=\"#\" onclick=\"clearInnerHTML('reInit');\">" + kwikiBundle.getString("KnowWE.buttons.close") + "</a><br />");
		html.append(kwikiBundle.getString("dpsenv.status") + "<br />");
		html.append(kwikiBundle.getString("dpsenv.duration") + (((float)diff)/1000) + kwikiBundle.getString("dpsenv.seconds") + " <br />");
		html.append("</p>");
		return html.toString();
	}
}