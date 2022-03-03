/*
 * Copyright (C) 2018 denkbares GmbH, Germany
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.knowwe.dialog.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import com.denkbares.progress.DummyProgressListener;
import com.denkbares.strings.Locales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.core.records.SessionConversionFactory;
import de.d3web.core.records.SessionRecord;
import de.d3web.core.records.io.SessionPersistenceManager;
import de.d3web.core.session.Session;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;

import static de.knowwe.dialog.action.InitWiki.PARAM_LANGUAGE;

/**
 * Downloads the protocol of the current d3web session.
 *
 * @author Jonas Müller
 */
public class UploadProtocol extends AbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(UploadProtocol.class);

	@Override
	public void execute(UserActionContext context) throws IOException {

		ByteArrayInputStream data = new ByteArrayInputStream(context.getParameter("xmlData").getBytes("UTF-8"));
		Collection<SessionRecord> sessionRecords = SessionPersistenceManager.getInstance().loadSessions(data);
		if (sessionRecords == null || sessionRecords.isEmpty()) return;

		KnowledgeBase kb = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);

		Session session;
		try {
			session = SessionConversionFactory.replayToSession(kb, sessionRecords.iterator().next());
		} catch (NullPointerException e) {
			context.getResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
			context.getResponse().getWriter().write("Dialog could not be loaded. The uploaded file does not seem not match with this interview.");
			context.getResponse().flushBuffer();
			return;
		}
		if (session == null) return;
		SessionProvider.setSession(context, session);

	}

}
