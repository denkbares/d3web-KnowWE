package de.d3web.we.taghandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class QuestionnaireValuesViewHandler extends AbstractTagHandler {

	private List<String> questionnaires = new ArrayList<String>();
	
	public QuestionnaireValuesViewHandler() {
		super("questionnaireValues");
	}
	
	@Override
	public String getExampleString() {
		return "[{KnowWEPlugin questionnaireValues = &lt;questionnaireName&gt;" + "}]";
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.Questionnaire.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {
		
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);
		String questionnaireName = values.get("questionnaireValues");
		if (!questionnaires.contains(questionnaireName))
			questionnaires.add(questionnaireName);
		
		int i = questionnaires.indexOf(questionnaireName);
		
		return "<div class='panel'>"
				+ "<h3>"
				+ 	questionnaireName
				+ "</h3><div id='qcvalues-panel" + i + "'>"
				+ "<input type='hidden' class='qcname' value='"
				+ 	questionnaireName
				+ "'/>"
				+ "<div id='qcvalues-result" + i + "'>" 
			    + "</div>"
			  + "</div></div>";
	}
	
}
