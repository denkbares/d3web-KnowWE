/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.error;

import java.util.HashSet;
import java.util.Set;

import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public abstract class KDOMReportMessage {

	public abstract String getVerbalization(KnowWEUserContext usercontext);

	private static final String ERROR_STORE_KEY = "ERROR-SET";

	private static final String WARNING_STORE_KEY = "WARNING-SET";

	private static final String NOTICE_STORE_KEY = "NOTICE-SET";

	public static void storeMessage(Section s, KDOMReportMessage m) {
		if (m instanceof KDOMError) {
			storeError(s, (KDOMError) m);
		}

		if (m instanceof KDOMNotice) {
			storeNotice(s, (KDOMNotice) m);
		}

		if (m instanceof KDOMWarning) {
			storeWarning(s, (KDOMWarning) m);
		}

	}

	public static void storeWarning(Section s, KDOMWarning e) {
		Set<KDOMWarning> warnings = (Set<KDOMWarning>) KnowWEUtils
				.getStoredObject(s, WARNING_STORE_KEY);
		if (warnings == null) {
			warnings = new HashSet<KDOMWarning>();
			KnowWEUtils.storeSectionInfo(s, WARNING_STORE_KEY, warnings);
		}
		warnings.add(e);

	}

	public static Set<? extends KDOMWarning> getWarnings(Section s) {
		Set<? extends KDOMWarning> warnings = (Set<KDOMWarning>) KnowWEUtils
				.getStoredObject(s, WARNING_STORE_KEY);
		return warnings;

	}

	public static void storeNotice(Section s, KDOMNotice e) {
		Set<KDOMNotice> notices = (Set<KDOMNotice>) KnowWEUtils
				.getStoredObject(s, NOTICE_STORE_KEY);
		if (notices == null) {
			notices = new HashSet<KDOMNotice>();
			KnowWEUtils.storeSectionInfo(s, NOTICE_STORE_KEY, notices);
		}
		notices.add(e);

	}

	public static Set<? extends KDOMNotice> getNotices(Section s) {
		Set<? extends KDOMNotice> notices = (Set<KDOMNotice>) KnowWEUtils
				.getStoredObject(s, NOTICE_STORE_KEY);
		return notices;

	}

	public static void storeError(Section s, KDOMError e) {
		Set<KDOMError> errors = (Set<KDOMError>) KnowWEUtils.getStoredObject(s,
				ERROR_STORE_KEY);
		if (errors == null) {
			errors = new HashSet<KDOMError>();
			KnowWEUtils.storeSectionInfo(s, ERROR_STORE_KEY, errors);
		}
		errors.add(e);

	}

	public static Set<? extends KDOMError> getErrors(Section s) {
		Set<? extends KDOMError> errors = (Set<KDOMError>) KnowWEUtils
				.getStoredObject(s, ERROR_STORE_KEY);
		return errors;

	}
	
	@Override
	public int hashCode() {
		// TODO better implementation possible
		return this.getVerbalization(null).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof KDOMReportMessage) {
			// TODO better implementation possible
			return ((KDOMReportMessage)obj).getVerbalization(null).equals(this.getVerbalization(null));
		}
		return false;
	}

}
