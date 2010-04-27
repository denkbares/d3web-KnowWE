package de.d3web.we.wikiConnector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public interface ConnectorAttachment {

	public String getFileName();

	public String getParentName();

	public Date getDate();

	public InputStream getInputStream() throws IOException;
}
