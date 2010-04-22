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

package de.d3web.we.kdom.report;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 *
 * Abstract class for a KnowWEMessage
 *
 * Also contains the management of the messages with resprect to the
 * corresponding sections
 *
 *
 * @author Jochen
 *
 */
public abstract class KDOMReportMessage {

	/**
	 * return the verbalization of this message. Will be rendered into the wiki
	 * page by the given MessageRenderer of the KnowWEObjectType of the section
	 *
	 * @param usercontext
	 * @return
	 */
	public abstract String getVerbalization(KnowWEUserContext usercontext);

	private static final String ERROR_STORE_KEY = "ERROR-SET";

	private static final String WARNING_STORE_KEY = "WARNING-SET";

	private static final String NOTICE_STORE_KEY = "NOTICE-SET";

	/**
	 * Cleans all KDOMReportMessages from the given ReviseSubTreeHandler <tt>h</tt>
	 * for the Section <tt>s</tt>.
	 */
	public static void cleanMessages(Section s, Class<?> source) {
		cleanErrors(s, source);
		cleanNotices(s, source);
		cleanWarnings(s, source);
	}

	public static void cleanErrors(Section s, Class<?> source) {
		Map<String, Set<KDOMError>> errors = getErrorsMap(s);
		if (errors != null) {
			errors.remove(source.getName());
		}
	}

	public static void cleanNotices(Section s, Class<?> source) {
		Map<String, Set<KDOMNotice>> notices = getNoticesMap(s);
		if (notices != null) {
			notices.remove(source.getName());
		}
	}

	public static void cleanWarnings(Section s, Class<?> source) {
		Map<String, Set<KDOMWarning>> warnings = getWarningsMap(s);
		if (warnings != null) {
			warnings.remove(source.getName());
		}
	}

	/**
	 * Stores the given KDOMReportMessage <tt>m</tt> from the ReviseSubTreeHandler <tt>h</tt>
	 * for Section <tt>s</tt>.
	 */
	public static void storeMessage(Section s, Class<?> source, KDOMReportMessage m) {
		if (m instanceof KDOMError) {
			storeError(s, source, (KDOMError) m);
		} else if (m instanceof KDOMNotice) {
			storeNotice(s, source, (KDOMNotice) m);
		} else if (m instanceof KDOMWarning) {
			storeWarning(s, source, (KDOMWarning) m);
		}
	}

	public static void storeWarning(Section s, Class<?> source, KDOMWarning e) {
		Map<String, Set<KDOMWarning>> warnings = getWarningsMap(s);
		if (warnings == null) {
			warnings = new HashMap<String, Set<KDOMWarning>>();
			KnowWEUtils.storeSectionInfo(s, WARNING_STORE_KEY, warnings);
		}
		Set<KDOMWarning> ws = warnings.get(source.getName());
		if (ws == null) {
			ws = new HashSet<KDOMWarning>();
			warnings.put(source.getName(), ws);
		}
		ws.add(e);
	}

	public static void storeNotice(Section s, Class<?> source, KDOMNotice e) {
		Map<String, Set<KDOMNotice>> notices = getNoticesMap(s);
		if (notices == null) {
			notices = new HashMap<String, Set<KDOMNotice>>();
			KnowWEUtils.storeSectionInfo(s, NOTICE_STORE_KEY, notices);
		}
		Set<KDOMNotice> ns = notices.get(source.getName());
		if (ns == null) {
			ns = new HashSet<KDOMNotice>();
			notices.put(source.getName(), ns);
		}
		ns.add(e);
	}

	public static void storeError(Section s, Class<?> source, KDOMError e) {
		Map<String, Set<KDOMError>> errors = getErrorsMap(s);
		if (errors == null) {
			errors = new HashMap<String, Set<KDOMError>>();
			KnowWEUtils.storeSectionInfo(s, ERROR_STORE_KEY, errors);
		}
		Set<KDOMError> es = errors.get(source.getName());
		if (es == null) {
			es = new HashSet<KDOMError>();
			errors.put(source.getName(), es);
		}
		es.add(e);
	}

	public static Map<String, Set<KDOMWarning>> getWarningsMap(Section s) {
		return (Map<String, Set<KDOMWarning>>) KnowWEUtils
				.getStoredObject(s, WARNING_STORE_KEY);
	}

	public static Map<String, Set<KDOMNotice>> getNoticesMap(Section s) {
		return (Map<String, Set<KDOMNotice>>) KnowWEUtils
				.getStoredObject(s, NOTICE_STORE_KEY);
	}

	public static Map<String, Set<KDOMError>> getErrorsMap(Section s) {
		return (Map<String, Set<KDOMError>>) KnowWEUtils
			.getStoredObject(s,ERROR_STORE_KEY);
	}

	public static Set<KDOMWarning> getWarnings(Section s) {
		Map<String, Set<KDOMWarning>> warnings = getWarningsMap(s);
		if (warnings == null) {
			return null;
		}
		Set<KDOMWarning> allSets = new HashSet<KDOMWarning>();
		for (Set<KDOMWarning> revSet:warnings.values()) {
			allSets.addAll(revSet);
		}
		return allSets;
	}

	public static Set<KDOMNotice> getNotices(Section s) {
		Map<String, Set<KDOMNotice>> notices = getNoticesMap(s);
		if (notices == null) {
			return null;
		}
		Set<KDOMNotice> allSets = new HashSet<KDOMNotice>();
		for (Set<KDOMNotice> revSet:notices.values()) {
			allSets.addAll(revSet);
		}
		return allSets;
	}

	public static Set<KDOMError> getErrors(Section s) {
		Map<String, Set<KDOMError>> errors = getErrorsMap(s);
		if (errors == null) {
			return null;
		}
		Set<KDOMError> allSets = new HashSet<KDOMError>();
		for (Set<KDOMError> revSet:errors.values()) {
			allSets.addAll(revSet);
		}
		return allSets;
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
