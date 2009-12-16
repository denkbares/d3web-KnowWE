package de.d3web.we.kdom.rendering;

import java.util.Map;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>The <code>DefaultEditSectionRender</code> renders the content of a section 
 * depending on the edit flag state. If the edit flag is set for the current section,
 * the section will be wrapped into an HTML textarea which allows the user to 
 * edit the content of the section. The text of the HTML textarea is the original
 * text of the section. So be carefully when editing the lines.</p>
 * 
 * <p>If the edit flag is not set the content is rendered through the <code>
 * renderContent</code> method. This method has to be implemented by each child
 * of the <code>DefaultEditSectionRender</code></p>
 * 
 * @author smark
 * @since 2009/10/18
 */
public abstract class DefaultEditSectionRender extends KnowWEDomRenderer {

	@Override
	public final void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		
		
		boolean isEditable = sec.hasQuickEditModeSet( user.getUsername() ); 
		boolean highlight = false;
		Map<String, String> urlParameterMap = user.getUrlParameterMap();
		
		
		String highlightKDOMID = urlParameterMap.get("highlight");
		if(highlightKDOMID != null) {
			if(highlightKDOMID.equals(sec.getId())) {
				highlight = true;
			}
		}
		
		String editKDOMID = urlParameterMap.get("edit");
		if(editKDOMID != null) {
			if(editKDOMID.equals(sec.getId())) {
				isEditable = true;
			}
		}
	
		if(highlight && !isEditable) {
			string.append(KnowWEUtils.maskHTML("<div class=\"searchword\">"));
		}
		string.append( KnowWEUtils.maskHTML( "<a name=\""+sec.getId()+"\"></a><div id=\"" + sec.getId() + "\">" ));
		
		if( sec.getArticle().equals( article ) ) {
			string.append( KnowWEUtils.maskHTML( this.generateQuickEdit( sec.getId(), isEditable) ));
		}
		
		if ( isEditable ) {
			String str = sec.getOriginalText();
			if(!user.getUrlParameterMap().containsKey("action")) {  // is not ajax action add verbatim for jspwiki render pipeline
				string.append(KnowWEUtils.maskHTML("{{{"));
			}
			string.append( KnowWEUtils.maskHTML( "<textarea name=\"default-edit-area\" id=\"default-edit-area\" style=\"width:95%; height:"+this.getHeight(str)+"px;\">" ));
			string.append( str );
			string.append( KnowWEUtils.maskHTML( "</textarea>" ));
			if(!user.getUrlParameterMap().containsKey("action")) {// is not ajax action add verbatim for jspwiki render pipeline
				string.append(KnowWEUtils.maskHTML("}}}"));
			}
			string.append( KnowWEUtils.maskHTML( "<div class=\"default-edit-handle\"></div>" ));
		} else {
			renderContent(article, sec, user, string );
		}
		string.append( KnowWEUtils.maskHTML( "</div>" ));
		if(highlight && !isEditable) {
			string.append(KnowWEUtils.maskHTML("</div>"));
		}
	}
		
	/**
	 * 
	 * @param sec
	 * @param user
	 * @param string
	 */
	public abstract void renderContent(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string);
	
	
	/**
	 * Generates a link used to enable or disable the Quick-Edit-Flag.
	 * 
	 * @param 
	 *     topic - name of the current page.
	 * @param 
	 *     id - of the section the flag should assigned to.
	 * @return
	 *     The quick edit menu panel.
	 */
	private String generateQuickEdit(String id, boolean isEditable) {
		StringBuilder b = new StringBuilder();
		b.append( "<div class=\"right\">" );
		b.append( "<img src=\"KnowWEExtension/images/pencil.png\" width=\"10\" title=\"Set QuickEdit-Mode\" class=\"quickedit default pointer\" rel=\"{id : '" + id + "'}\"/><br />");
		if( isEditable ){
		    b.append( "<input rel=\"{id : '" + id + "'}\" type=\"submit\" value=\"save\"/>" );
		}
		b.append( "</div>" );		
		return b.toString();
	}
	
	/**
	 * Calculates the height of the HTML textarea.
	 * 
	 * @param 
	 *      str - The string used to calculated the height.
	 * @return
	 *      The height of the HTML textarea element.
	 */
	private Integer getHeight( String str ){
		int linebreaks = str.split("\n|\f").length;
		int lineHeight = 18; //px
		return (linebreaks + 5) * lineHeight;
	}
	
}
