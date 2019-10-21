/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.denkbares.utils.Log;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.records.SessionConversionFactory;
import de.d3web.core.records.SessionRecord;
import de.d3web.core.records.io.SessionPersistenceManager;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.dialog.SessionConstants;

/**
 * Downloads the protocol of the current d3web session.
 *
 * @author Volker Belli
 */
public class DownloadProtocol extends AbstractAction {

	public static final String FILENAME_SESSION_PROTOCOL_XML = "session-protocol.xml";
	public static final String PARAMETER_SAVE_AS = "saveAs";

	@Override
	public void execute(UserActionContext context) throws IOException {

		KnowledgeBase base = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);

		Session session = SessionProvider.getSession(context, base);
		if (session == null) {
			failUnexpected(context, "No session found");
		}

		boolean saveAs = context.getParameter(PARAMETER_SAVE_AS, "true").equals("true");
		File file = null;
		if (!saveAs) {
			file = session.getSessionObject(LoadedSessionContext.getInstance()).getFile();
			if (file == null) {
				failUnexpected(context, "Trying to save to file the session was loaded from, but file attribute could not be found in session.");
			}
			if (!file.canWrite()) {
				Log.severe("Trying to save to file the session was loaded from, but cannot write file: " + file.getPath());
				file = null;
			}
		}
		List<SessionRecord> record = Collections.singletonList(SessionConversionFactory.copyToSessionRecord(session));

		if (saveAs || file == null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			String filename = sdf.format(new Date()) + "-" + base.getName() + ".xml";

			// init header to make the browser save the file
			context.setContentType("application/octet-stream");
			context.setHeader("Content-disposition", "attachment; filename=" + filename);

			SessionPersistenceManager.getInstance().saveSessions(context.getOutputStream(), record);
		}
		else {
			SessionPersistenceManager.getInstance().saveSessions(file, record);
		}
	}
}
