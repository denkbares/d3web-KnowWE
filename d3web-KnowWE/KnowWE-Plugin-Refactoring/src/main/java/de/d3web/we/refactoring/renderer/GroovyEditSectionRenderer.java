package de.d3web.we.refactoring.renderer;

import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>The <code>EditSectionRenderer</code> renders the content of a section 
 * depending on the edit flag state. If the edit flag is set for the current section,
 * the section will be wrapped into an HTML textarea which allows the user to 
 * edit the content of the section. The text of the HTML textarea is the original
 * text of the section. So be carefully when editing the lines.</p>
 * 
 * @author smark
 * @since 2009/10/18
 */
@Deprecated
public class GroovyEditSectionRenderer extends KnowWEDomRenderer {
	
	KnowWEDomRenderer renderer;
	
	public GroovyEditSectionRenderer() {
		this(DelegateRenderer.getInstance());
	}
	
	public GroovyEditSectionRenderer(KnowWEDomRenderer renderer) {
		this.renderer = renderer;
	}

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
			string.append(KnowWEUtils.maskHTML( this.generateQuickEdit
					("Quickedit " + sec.getObjectType().getName() + " Section", sec.getId(), 
							isEditable, user)));
		}
		
		if ( isEditable ) {
			String str = sec.getOriginalText();
			if(!user.getUrlParameterMap().containsKey("action")) {  // is not ajax action add verbatim for jspwiki render pipeline
				//string.append(KnowWEUtils.maskHTML("{{{"));
				string.append("{{{");
			}
			string.append( KnowWEUtils.maskHTML( "<textarea name=\"default-edit-area\" id=\"" + sec.getId() + "/default-edit-area\" style=\"width:92%; height:"+this.getHeight(str)+"px;\">" ));
			//string.append(KnowWEUtils.maskNewline(str));
			string.append( str );
			string.append( KnowWEUtils.maskHTML( "</textarea>" ));
			if(!user.getUrlParameterMap().containsKey("action")) {// is not ajax action add verbatim for jspwiki render pipeline
				//string.append(KnowWEUtils.maskHTML("}}}"));
				string.append("}}}");
			}
			string.append( KnowWEUtils.maskHTML( "<div class=\"default-edit-handle\"></div>" ));
		} else {
			renderer.render(article, sec, user, string );
		}
		string.append( KnowWEUtils.maskHTML( "</div>" ));
		if(highlight && !isEditable) {
			string.append(KnowWEUtils.maskHTML("</div>"));
		}
		if(isEditable){
			string.append(KnowWEUtils.maskHTML("<script type='text/javascript' src='KnowWEExtension/scripts/CodeMirror-0.65/js/codemirror.js'></script>"));
			string.append(KnowWEUtils.maskHTML("<script type='text/javascript' src='KnowWEExtension/scripts/CodeMirror.js'></script>"));
		}
	}
		
	
	/**
	 * Generates a link used to enable or disable the Quick-Edit-Flag.
	 * @param 
	 *     id - of the section the flag should assigned to.
	 * @param
	 *     user - to get language preferences.
	 * @param 
	 *     topic - name of the current page.
	 * 
	 * @return
	 *     The quick edit menu panel.
	 */
	protected String generateQuickEdit(String tooltip, String id, boolean isEditable, KnowWEUserContext user) {
		StringBuilder b = new StringBuilder();
		final ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);
		b.append( "<div " + getQuickEditDivAttributes() + ">" );
		if (!isEditable) {
			b.append("<img src=\"KnowWEExtension/images/pencil.png\" width=\"10\" title=\"" + tooltip + "\" class=\"quickedit default pointer\" rel=\"{id : '" + id + "'}\" name=\"" + id + "_pencil\"/><br />");			
		}
		if( isEditable ){
			b.append("<input class=\"pointer\" rel=\"{id : '" + id + "'}\" style=\"padding:0 0 0 0; width: 25px; height: 25px; background: #FFF url(KnowWEExtension/images/msg_checkmark.png) no-repeat; border: none; vertical-align:top;\" name=\"" + id + "_accept\" type=\"submit\" value=\"\" title=\"" + rb.getString("KnowWE.TableContentRenderer.accept") + "\"><br/>" );
			b.append("<img class=\"quickedit default pointer\" rel=\"{id : '" + id + "'}\" width=\"25\" title=\"" + rb.getString("KnowWE.TableContentRenderer.cancel") + "\" src=\"KnowWEExtension/images/msg_cross.png\" name=\"" + id + "_cancel\"/>");
		}
		b.append( "</div>" );		
		return b.toString();
	}
	
	protected String getQuickEditDivAttributes() {
		return "class=\"right\"";
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
