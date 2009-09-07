package de.d3web.we.kdom.table;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>Represents the body of the <code>Table</code> tag.</p>
 * 
 * @author smark
 * @see XMLContent
 */
public class TableContent extends XMLContent 
{

	@Override
	protected void init() 
	{
		childrenTypes.add( new TableLine () );
	}

	@Override
	public KnowWEDomRenderer getRenderer()
	{
		/**
		 * This is a renderer for the TableContent. I wraps the <code>Table</code>
		 * tag into an own DIV and delegates the rendering of each <code>TableCellContent</code> 
		 * to its own renderer.
		 * 
		 * @author smark
		 */
		class TableContentRenderer extends KnowWEDomRenderer
		{
			@Override
			public String render(Section sec, KnowWEUserContext user, String web, String topic) 
			{				
				StringBuilder html = new StringBuilder();
				
				html.append( "<div class=\"table-edit\" id=\"" + sec.getId() + "\">" );
				html.append( generateQuickEdit( topic, sec.getId() ));
				
				html.append( "<table class='wikitable knowwetable' border='1'><tbody>" );
				html.append( SpecialDelegateRenderer.getInstance().render(sec, user, web, topic) );
				html.append( "</tbody></table>" );
				
				if ( sec.hasQuickEditModeSet( user.getUsername() ) )
				{
				    html.append( "<input id=\"" + sec.getId() + "\" type=\"submit\" value=\"save\"/>" );
				}
				
				html.append("</div>");
				
				return KnowWEEnvironment.maskHTML( html.toString() );
			}
			
			/**
			 * Generates a link used to enable or disable the Quick-Edit-Flag.
			 * 
			 * @see UserSetting, UserSettingsManager, NodeFlagSetting
			 * @param topic     name of the current page
			 * @param id        of the section the flag should assigned to
			 * @return
			 */
			private String generateQuickEdit(String topic, String id)
			{
				String icon = " <img src='KnowWEExtension/images/pencil.png' title='Set QuickEdit-Mode'/>";
				return "<a href=\"javascript:QuickEdit.doTable('" + id + "')\">" + icon + "</a>";
			}
			
		}
		return new TableContentRenderer();
	}
}
