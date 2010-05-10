package de.d3web.we.ci4ke.groovy;

import groovy.lang.Script;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.ci4ke.handling.CIConfig;
import de.d3web.we.ci4ke.handling.CITest;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.xcl.XCLRelation;
import de.d3web.we.kdom.xcl.XCList;

public abstract class GroovyCITestScript extends Script implements CITest {

	public GroovyCITestScript() {
		super();
	}
	
	protected CIConfig config;
	
	@Override
	public void init(CIConfig config) {
		this.config = config;
	}	
	
	//TODO: Reactivate! ;-)
//	public Collection<KnowWEArticle> getAllArticles(){
//		return KnowWEEnvironment.getInstance().
//				getArticleManager(config.get(DeprecatedCIConfiguration.WEB_KEY)).getArticles();
//	}
//	
	//just for documentation purposes
	public KnowWEArticle getArticle(){
		return KnowWEEnvironment.getInstance().getArticle(KnowWEEnvironment.DEFAULT_WEB, 
				this.config.getMonitoredArticleTitle());
	}
	
	public static List<String> findXCListsWithLessThenXRelations(KnowWEArticle article, int limitRelations){
		
		List<String> sectionIDs = new ArrayList<String>();
		
		List<Section<XCList>> found = new ArrayList<Section<XCList>>();
		article.getSection().findSuccessorsOfType(XCList.class, found);
		
		for(Section<XCList> xclSection : found){
			List<Section<XCLRelation>> relations = new ArrayList<Section<XCLRelation>>();
			xclSection.findSuccessorsOfType(XCLRelation.class, relations);
			if(relations.size() < limitRelations)
				sectionIDs.add(xclSection.getId());
		}		
		return sectionIDs;
	}		
	
}
