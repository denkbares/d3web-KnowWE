package de.d3web.we.taghandler;

import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ImportKnOfficeHandler extends AbstractTagHandler {
	
	
	public static final String KEY_SOLUTIONS = "solutions";
	public static final String KEY_WIKIPAGE = "wikipage";
	public static final String KEY_QUESTIONNAIRES = "questionnaires";
	public static final String KEY_DECISIONTREE = "decisiontree";
	public static final String KEY_RULES = "rules";
	public static final String KEY_COVERINGLISTS = "coveringlists";
	public static final String KEY_COVERINGTABLE = "coveringtable";
	public static final String KEY_SCORETABLE = "scoretable";
	public static final String KEY_DECISIONTABLE = "decisiontable";
	public static final String KEY_CONFIG = "config";
	public static final String KEY_DIALOG_SETTINGS = "dialogsettings";
	public static final String KEY_DIALOG_LAYOUT = "dialoglayout";
	public static final String KEY_DIALOG_CSS = "dialogcss";
	public static final String KEY_DIALOG_PIC = "dialogpic";
	public static final String KEY_OWL="owlfile";
	
	
	public ImportKnOfficeHandler() {
		super("KnOfficeImport");
	}
	@Override
	public String getDescription() {
		 return KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.ImportKnOfficehandler.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
	
		Object[][] fields = {{"KnowWE.knoffice.fields.wikipage", KEY_WIKIPAGE, "text", 50, ""},
				{"KnowWE.knoffice.fields.diagnosishierachy", KEY_SOLUTIONS, "file", 50, "txt"},
			    {"KnowWE.knoffice.fields.questionclasshierachy", KEY_QUESTIONNAIRES, "file", 50, "txt"},
			    {"KnowWE.knoffice.fields.decisiontree", KEY_DECISIONTREE, "file", 50, "txt"},
			    {"KnowWE.knoffice.fields.rules", KEY_RULES, "file", 50, "txt"},
			    {"KnowWE.knoffice.fields.setCoveringList", KEY_COVERINGLISTS, "file", 50, "txt"},
			    {"KnowWE.knoffice.fields.setCoveringTable", KEY_COVERINGTABLE, "file", 50, "xls"},
			    {"KnowWE.knoffice.fields.diagnosticScores", KEY_SCORETABLE, "file", 50, "xls"},
			    {"KnowWE.knoffice.fields.decisionTable", KEY_DECISIONTABLE, "file", 50, "xls"},
			    {"KnowWE.knoffice.fields.config", KEY_CONFIG, "file", 50, "txt"},
			    {"KnowWE.knoffice.fields.owlfile", KEY_OWL, "file", 50, "xml"}};
			    
		Object[][] extFields = {{"KnowWE.knoffice.fields.dialog.settings", KEY_DIALOG_SETTINGS, "file", 50, "xml", "dialogsettings.xml"},
			    {"KnowWE.knoffice.fields.dialog.layout", KEY_DIALOG_LAYOUT, "file", 50, "xml", "dialoglayout.xml"},
			    {"KnowWE.knoffice.fields.dialog.sheet", KEY_DIALOG_CSS, "file", 50, "css", "dialog.css"},
			    {"KnowWE.knoffice.fields.dialog.image", KEY_DIALOG_PIC, "file", 50, "img", ""}};

		StringBuffer html = new StringBuffer();
		//enctype='multipart/form-data'
		//accept-charset=\"UTF-8\" 
		html.append("<div id='knoffice-panel' class='panel'>");
		html.append("<h3>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.knoffice.name") + "</h3>");	
		
		html.append("<form onsubmit='sendForm(this);' enctype=\"multipart/form-data\"  method=\"POST\" action=\"KnowWEUpload\">");
		//normal fields
		for (int i = 0; i < fields.length; i++) {
				html.append("<label for='" + 	fields[i][1] + "' class='" + fields[i][4] + "'>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString(fields[i][0].toString()) + "</label>");
				html.append("<input id='" + fields[i][1] + "' name='" + fields[i][1] 
				                + "' type='" + fields[i][2] + "' size='" + fields[i][3] + "'/>");
				html.append("\n <br />"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
		}
		
		html.append("<p id='knoffice-show-extend' class='pointer extend-panel-down'>" 
				+ KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.renamingtool.settings") + "</p>");
		html.append("<div id='knoffice-extend-panel' class='hidden'>");
		//extended fields
		for (int i = 0; i < extFields.length; i++) {			
			html.append("<label for='" + extFields[i][1] + "' class='" + extFields[i][4] + "'>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString(extFields[i][0].toString()) + "</label>");
			html.append("<input id='" + extFields[i][1] + "' name='" + extFields[i][1] 
			                + "' type='" + extFields[i][2] + "' size='" + extFields[i][3] + "'/>");
			if(extFields[i][5] != "")
			    html.append("<span class='hint'>" + KnowWEEnvironment.getInstance().getKwikiBundle().getString("KnowWE.knoffice.hint").replace("{0}", extFields[i][5].toString() ) + "</span>");
			html.append("\n <br />"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
	    }
		
		html.append("</div>");
		html.append("<br /><p><input id='upload' type='submit' value='Hochladen' name='submit' class='button'/></p>");
		html.append("</form><br /></div>");
		return html.toString();
	}
}
