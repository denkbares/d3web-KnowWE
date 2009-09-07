package de.d3web.we.taghandler;

import java.util.Collection;
import java.util.ResourceBundle;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class WikiSolutionsTagHandler extends AbstractTagHandler{
	
	public WikiSolutionsTagHandler() {
		this("WikiSolutions");
	}

	@Override
	public String getDescription() {
		return D3webModule.getInstance().getKwikiBundle_d3web().getString("KnowWE.WikiSolutions.description");
	}
	
	public WikiSolutionsTagHandler(String name) {
		super(name);
	}
	

	@Override
	public String render(String topic, KnowWEUserContext user, String value, String web) {
		Collection<KnowledgeService> services = DPSEnvironmentManager.getInstance().getEnvironments(web).getServices();
		
		String text = "<h1>Solutions</h1>";
		GlobalTerminology solutions = DPSEnvironmentManager.getInstance().getEnvironments(web).getTerminologyServer().getGlobalTerminology(TerminologyType.diagnosis);
		Collection<Term> allTerms = solutions.getAllTerms();
		for (Term term : allTerms) {
			text += term.getInfo(TermInfoType.TERM_NAME)+"<br>";
			
			//TODO refactor using alingnment knowledge
			for (KnowledgeService service : services) {
				if(service instanceof D3webKnowledgeService) {
					Diagnosis d = KnowledgeBaseManagement.createInstance(((D3webKnowledgeService)service).getBase()).findDiagnosis( (String)term.getInfo(TermInfoType.TERM_NAME));
					if(d != null) {
						String topicName = service.getId().substring(0,service.getId().indexOf(".."));
						String link = "<a href=\"Wiki.jsp?page="+ topicName + "\">"
						+ term.getInfo(TermInfoType.TERM_NAME) + "</a>";
						text += link+"<br /> \n"; // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
					}
				}
				
			}
			text+="<br />";
		}
		return text;
	}

}
