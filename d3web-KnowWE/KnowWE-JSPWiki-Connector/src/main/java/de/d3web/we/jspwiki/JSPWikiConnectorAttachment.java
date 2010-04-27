package de.d3web.we.jspwiki;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import com.ecyrd.jspwiki.attachment.Attachment;
import com.ecyrd.jspwiki.attachment.AttachmentManager;
import com.ecyrd.jspwiki.providers.ProviderException;

import de.d3web.we.wikiConnector.ConnectorAttachment;

public class JSPWikiConnectorAttachment implements ConnectorAttachment {

	private Attachment attachment;
	private AttachmentManager attachmentManager;

	public JSPWikiConnectorAttachment(Attachment attachment, AttachmentManager attachmentManager) {
		this.attachment = attachment;
		this.attachmentManager = attachmentManager;
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
		if (other.attachment == attachment) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			return attachmentManager.getAttachmentStream(attachment);
		}
		catch (ProviderException e) {
			throw new IOException(e);
		}
	}

}
