package de.d3web.we.action;

import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;

/**
 * <p>UpdateTableKDOMNodes class.</p>
 * 
 * This class handles the changes in the in-view editable tables.
 * 
 * @author smark
 * @see KnowWEAction
 */
public class UpdateTableKDOMNodes implements KnowWEAction
{

	@Override
	public String perform(KnowWEParameterMap parameterMap) 
	{
		String nodeDelim = "::";
		String tokenDelim = "-";
		
		String web = parameterMap.getWeb();
		String nodes = parameterMap.get(KnowWEAttributes.TARGET);
		String name = parameterMap.getTopic();
		
		
		String newSourceText = "";
		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);

		if( nodes != "" )
		{
			if( nodes.contains( nodeDelim )) 
			{
				String[] tokens = nodes.split( nodeDelim );
				for (String string : tokens) 
				{
					String[] node = string.split( tokenDelim );
					try{
					    newSourceText = mgr.replaceKDOMNodeWithoutSave(parameterMap, name, node[0], node[2]);
					} catch ( ArrayIndexOutOfBoundsException e ){
					    newSourceText = mgr.replaceKDOMNodeWithoutSave(parameterMap, name, node[0], " ");
					}
				}
			}
			else
			{
				String[] node = nodes.split( tokenDelim );
				newSourceText = mgr.replaceKDOMNodeWithoutSave(parameterMap, name, node[0], node[2]);
			}				
			KnowWEEnvironment.getInstance().saveArticle(web, name, newSourceText, parameterMap);
		}
		
		return "done";
	}
}
