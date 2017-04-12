package cc.knowwe.dialog.action;

import java.io.IOException;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cc.knowwe.dialog.SessionConstants;

import com.denkbares.progress.DummyProgressListener;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.records.SessionConversionFactory;
import de.d3web.core.records.SessionRecord;
import de.d3web.core.records.io.SessionPersistenceManager;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Downloads the protocol of the current d3web session.
 * 
 * @author Volker Belli
 */
public class DownloadProtocol extends AbstractAction {

	public static final String FILENAME_SESSION_PROTOCOL_XML = "session-protocol.xml";

	@Override
	public void execute(UserActionContext context) throws IOException {
		// init header to make the browser save the file
		context.setContentType("application/octet-stream");
		context.setHeader("Content-disposition", "attachment; filename=session-protocol.zip");

		KnowledgeBase base = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);
		Session session = SessionProvider.getSession(context, base);
		SessionRecord record = SessionConversionFactory.copyToSessionRecord(session);

		// we create a zip file instead of a xml stream,
		// otherwise some browsers will show the file
		// instead of saving it to disk
		// (e.g. SWT internal browser)
		ZipOutputStream zipOut = new ZipOutputStream(context.getOutputStream());
		zipOut.putNextEntry(new ZipEntry(FILENAME_SESSION_PROTOCOL_XML));
		SessionPersistenceManager.getInstance().saveSessions(
				zipOut,
				Collections.singletonList(record),
				new DummyProgressListener());
		zipOut.closeEntry();
		zipOut.flush();
		zipOut.close();
	}

}
