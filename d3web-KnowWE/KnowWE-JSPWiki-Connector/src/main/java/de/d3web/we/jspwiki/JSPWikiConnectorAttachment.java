package de.d3web.we.jspwiki;

import java.util.Date;

import com.ecyrd.jspwiki.attachment.Attachment;

import de.d3web.we.wikiConnector.ConnectorAttachment;

public class JSPWikiConnectorAttachment implements ConnectorAttachment{
	
	private Attachment attachment;
	
	public JSPWikiConnectorAttachment(Attachment attachment) {
		this.attachment = attachment;
	}
	
	@Override
	public String getFileName() {
		return attachment.getFileName();
	}

	@Override
	public String getParentName() {
		return attachment.getParentName();
	}
	
	@Override
	public Date getDate() {
		return attachment.getLastModified();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof JSPWikiConnectorAttachment)) return false;
		JSPWikiConnectorAttachment other = (JSPWikiConnectorAttachment) o;
		if (other.attachment==attachment) {
			return true;
		} else {
			return false;
		}
	}

}
