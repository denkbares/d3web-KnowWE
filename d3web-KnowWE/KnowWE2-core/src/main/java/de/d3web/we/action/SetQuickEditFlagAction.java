package de.d3web.we.action;

import java.util.List;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DefaultDelegateRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.user.UserSettingsManager;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * 
 */
public class SetQuickEditFlagAction implements KnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String web = parameterMap.getWeb();
		String nodeID = parameterMap.get(KnowWEAttributes.TARGET);
		nodeID = nodeID.replace("_content", "");
		
		String topic = parameterMap.getTopic();
		String user = parameterMap.getUser();
		
		
		
		UserSettingsManager.getInstance().setQuickEditFlag(nodeID, user, topic);
		
		String result = KnowWEEnvironment.unmaskHTML( refreshKDOMElement(web, topic, new KnowWEUserContextImpl(user,parameterMap), nodeID) );
		return result;
	}

	/**
	 * Searches the element the user set the QuickEditFlag to and renders it.
	 * The result is returned for refreshing the view.
	 * 
	 * @param web
	 * @param topic
	 * @param user
	 * @param nodeID
	 * @return
	 */
	private String refreshKDOMElement(String web, String topic, KnowWEUserContext user, String nodeID)
	{
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager( web );
		KnowWEArticle article = mgr.getArticle( topic );
		
		if( article == null)
			return "<p class=\"error box\"> Article not found! </p>";
		
		Section root = article.getSection();
		Section secWithNodeID = getSectionFromCurrentID( nodeID, root );
		
		if( secWithNodeID != null )
		{
			String result = SpecialDelegateRenderer.getInstance().render(secWithNodeID, user, web, topic);
			return result;
		}
		return "<p class=\"error box\"> Something bad happend! </p>";
	}
	
	/**
	 * Searches for a section with the node id from the <code>SetQuickEditFlagAction</code>.
	 * The resulting section will be re-rendered and updated in the view.
	 * 
	 * @param nodeID
	 * @param root
	 * @param found
	 */
	private Section getSectionFromCurrentID( String nodeID, Section root )
	{		
	    if( root.getId().equals( nodeID ))
	    	return root;
	 
		Section found = null;
		List<Section> children = root.getChildren();
		for (Section section : children) 
		{
			found = getSectionFromCurrentID( nodeID, section );
			if( found != null) return found;
		}
		return found;
	}	

}
