package de.d3web.we.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.dialog.Dialog;
import de.d3web.we.core.dialog.DialogControl;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

public class KSSViewHistoryRenderer implements KnowWEAction {

	private String htmlHeader;
	private SimpleDateFormat dateFormat;
	private String dialogLink = "KnowWE.jsp?renderer=KWiki_dialog&action=KWiki_requestDialog&KWikisessionid=%id%&KWikiUser=%user%&KWikiWeb=%web%";
	
	public KSSViewHistoryRenderer() {
		htmlHeader = "<head>"
			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"KnowWEExtension/css/general.css\"/>"
			+ "<link href=\"templates/knowweTmps/jspwiki.css\" type=\"text/css\" media=\"screen, projection, print\" rel=\"stylesheet\">"
			+ "</head>";
		dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern("dd.MM.yyyy HH:mm:ss");
	}

	public String perform(KnowWEParameterMap map){
		ResourceBundle rb = ResourceBundle.getBundle("KnowWE_messages");
		String userString = map.get(KnowWEAttributes.USER);
		
		Broker broker = D3webModule.getBroker(map);
		String web = map.get(KnowWEAttributes.WEB);
		DialogControl dialogControl = broker.getDialogControl();
		List<Dialog> instant = dialogControl.getInstantIndicatedDialogs();
		List<Dialog> indicated = dialogControl.getIndicatedDialogs();
		List<Dialog> old = dialogControl.getHistory();
		List<Dialog> history = new ArrayList<Dialog>();
		history.addAll(old);
		history.addAll(instant);
		history.addAll(indicated);
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/transitional.dtd\">");
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"de\">");

		if (htmlHeader != null) {
			sb.append(htmlHeader);
		}		
		String[] header = {rb.getString("KnowWE.topic"), rb.getString("KnowWE.dialog"), rb.getString("KnowWE.dialog.indicatedBy") };
		
		sb.append("<body onLoad='window.focus()'>");
		sb.append("<div id=\"popup-findings\">");
		sb.append("<h3>");
		sb.append(rb.getString("KnowWE.dialog.history"));
		sb.append("</h3>");
		sb.append("<table>");
		sb.append("<thead><tr>");
		for (String string : header) {
			sb.append("<th>" + string + "</th>");
		}
		sb.append("</tr></thead><tbody>");
		
		boolean even = false;
		for (Dialog each : history) {
			if(even){
				sb.append("<tr class=\"even\">");
			} else {
				sb.append("<tr>");
			}
			sb.append("<td>" + getLinkToTopic(each.getDialog().getNamespace(), web) + "</td>");
			sb.append("<td>" + getLinkToDialog(each.getDialog().getNamespace(), userString, web, true) + "</td>");
			
			if(each.getReason() == null){
				sb.append("<td>" + rb.getString("KnowWE.user") + "</td>");
			} else {
				sb.append("<td>" + getLinkToDialog(each.getReason().getNamespace(), userString, web, false) + "</td>");
			}
			sb.append("</tr>");
			even = !even;
		}
		sb.append("</tbody></table></div></body></html>");
		return sb.toString();
	}
	
	
	private String getLinkToDialog(String string, String userString, String web, boolean onlyKB) {
		String namespace = "unknown";
		String[] strings = string.split("\\.\\.");
		String text = string;
		
		if(strings.length == 2) {
			namespace = strings[1];
			if(onlyKB) {
				text = strings[1];
			}
		} else {
			namespace = string;
		}
		StringBuffer sb = new StringBuffer();
		String l = dialogLink;
		l = l.replaceAll("%id%", namespace);
		l = l.replaceAll("%user%", userString);
		l = l.replaceAll("%web%", web);
		sb.append("<a href='");
		sb.append(l);
		sb.append("' target='KWiki Dialog'>");
		sb.append(text);
		sb.append("</a>");
		return sb.toString();
	}

	private String getLinkToTopic(String string, String web) {
		String topic = "unknown";
		String[] strings = string.split("\\.\\.");
		if(strings.length == 2) {
			topic = strings[0];
		} else {
			
		}
		StringBuffer sb = new StringBuffer();
		sb.append("<a href='");
		sb.append("/bin/view/"+web+"/"+topic);
		sb.append("' target='_parent'>");
		sb.append(topic);
		sb.append("</a>");
		return sb.toString();
	}
// 	commented the next 3 methods out, because they were never used
//	private String getLink(String namespaceID, String username) {
//		StringBuffer sb = new StringBuffer();
//		String l = dialogLink;
//		l = l.replaceAll("%id%", namespaceID);
//		l = l.replaceAll("%user%", username);
//		sb.append("<a href='");
//		sb.append(l);
//		sb.append("' target='KWiki Dialog'>");
//		sb.append(namespaceID);
//		sb.append("</a>");
//		return sb.toString();
//	}
//
//	private String getObjectText(Information info, KnowWEParameterMap map) {
//		StringBuffer result = new StringBuffer();
//		DPSEnvironment env = KnowWEUtils.getDPSE(map);
//		
//		LocalTerminologyAccess terminology = env.getTerminologyServer().getStorage().getTerminology(info.getTerminologyType(), info.getNamespace());
//		
//		IdentifiableInstance iio = info.getIdentifiableObjectInstance();
//			
//		Object ido = terminology.getObject(iio.getObjectId(), null);
//		
//		if(ido instanceof NamedObject) {
//			result.append(((NamedObject)ido).getText());
//		}
//		return result.toString();
//	}
//	
//	private String getValueText(Information info, KnowWEParameterMap map) {
//		StringBuffer result = new StringBuffer();
//		DPSEnvironment env = KnowWEUtils.getDPSE(map);
//		LocalTerminologyAccess terminology = env.getTerminologyServer().getStorage().getTerminology(info.getTerminologyType(), info.getNamespace());
//		IdentifiableInstance iio = info.getIdentifiableObjectInstance();
//		Collection<IdentifiableInstance> iivs = info.getIdentifiableValueInstances();
//		if(iivs.isEmpty()) return result.toString();
//		Iterator<IdentifiableInstance> iter = iivs.iterator();
//		while (iter.hasNext()) {
//			IdentifiableInstance iiv = iter.next();
//			Object value = iiv.getValue();
//			if(value instanceof String) {
//				Object valueObject = terminology.getObject(iio.getObjectId(), (String) value);
//				if(valueObject instanceof AnswerChoice) {
//					result.append(((AnswerChoice)valueObject).getText());
//				} else if(valueObject instanceof AnswerUnknown) {
//					result.append("unkown");
//				}
//			} else {
//				result.append(value.toString());
//			}	
//			if(iter.hasNext()) {
//				result.append(", ");
//			}
//		}
//		return result.toString();
//	}
}
