/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.d3web.core.records.io.SessionPersistence;
import de.knowwe.dialog.SessionConstants;

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


		KnowledgeBase base = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);
		Session session = SessionProvider.getSession(context, base);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

		// init header to make the browser save the file
		context.setContentType("application/octet-stream");
		String filename = sdf.format(new Date()) + "-" + base.getName() + ".xml";
		context.setHeader("Content-disposition", "attachment; filename=" + filename);

		SessionRecord record = SessionConversionFactory.copyToSessionRecord(session);
		SessionPersistenceManager.getInstance()
				.saveSessions(context.getOutputStream(), Collections.singletonList(record));
	}

}
