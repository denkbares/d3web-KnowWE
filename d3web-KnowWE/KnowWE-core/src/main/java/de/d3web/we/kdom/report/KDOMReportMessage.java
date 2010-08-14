/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.d3web.we.kdom.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEUtils;

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
	 * @return
	 */
	public abstract String getVerbalization();

	/**
	 * @see KnowWEUtils#clearMessages(KnowWEArticle, Section, Class, Class)
	 */
	public static void clearMessages(KnowWEArticle article, Section<? extends KnowWEObjectType> s, Class<?> source) {
		storeMessages(article, s, source, new ArrayList<KDOMReportMessage>(0));
	}

	/**
	 * @see KnowWEUtils#storeSingleMessage(KnowWEArticle, Section, Class, Class,
	 *      Object)
	 */
	public static void storeSingleError(KnowWEArticle article,
			Section<? extends KnowWEObjectType> s, Class<?> source, KDOMError msg) {
		if (msg != null) storeMessages(article, s, source, Arrays.asList((KDOMReportMessage) msg));
	}

	/**
	 * @see KnowWEUtils#storeSingleMessage(KnowWEArticle, Section, Class, Class,
	 *      Object)
	 */
	public static void storeSingleWarning(KnowWEArticle article,
			Section<? extends KnowWEObjectType> s, Class<?> source, KDOMWarning msg) {
		if (msg != null) storeMessages(article, s, source, Arrays.asList((KDOMReportMessage) msg));
	}

	/**
	 * @see KnowWEUtils#storeSingleMessage(KnowWEArticle, Section, Class, Class,
	 *      Object)
	 */
	public static void storeSingleNotice(KnowWEArticle article,
			Section<? extends KnowWEObjectType> s, Class<?> source, KDOMNotice msg) {
		if (msg != null) storeMessages(article, s, source, Arrays.asList((KDOMReportMessage) msg));
	}

	/**
	 * @see KnowWEUtils#storeMessages(KnowWEArticle, Section, Class, Collection)
	 */
	public static void storeMessages(KnowWEArticle article, Section<? extends KnowWEObjectType> s,
			Class<?> source, Collection<KDOMReportMessage> msgs) {
		if (msgs == null) return;
		Collection<KDOMError> errors = new ArrayList<KDOMError>(msgs.size());
		Collection<KDOMWarning> warnings = new ArrayList<KDOMWarning>(msgs.size());
		Collection<KDOMNotice> notices = new ArrayList<KDOMNotice>(msgs.size());

		for (KDOMReportMessage msg : msgs) {
			if (msg instanceof KDOMError) {
				errors.add((KDOMError) msg);
			}
			else if (msg instanceof KDOMWarning) {
				warnings.add((KDOMWarning) msg);
			}
			else if (msg instanceof KDOMNotice) {
				notices.add((KDOMNotice) msg);
			}
		}

		storeErrors(article, s, source, errors);
		storeWarnings(article, s, source, warnings);
		storeNotices(article, s, source, notices);
	}

	/**
	 * @see KnowWEUtils#storeMessages(KnowWEArticle, Section, Class, Collection)
	 */
	public static void storeErrors(KnowWEArticle article, Section<? extends KnowWEObjectType> s,
			Class<?> source, Collection<KDOMError> msgs) {
		KnowWEUtils.storeMessages(article, s, source, KDOMError.class, msgs);
	}

	/**
	 * @see KnowWEUtils#storeMessages(KnowWEArticle, Section, Class, Collection)
	 */
	public static void storeNotices(KnowWEArticle article, Section<? extends KnowWEObjectType> s,
			Class<?> source, Collection<KDOMNotice> msgs) {
		KnowWEUtils.storeMessages(article, s, source, KDOMNotice.class, msgs);
	}

	/**
	 * @see KnowWEUtils#storeMessages(KnowWEArticle, Section, Class, Collection)
	 */
	public static void storeWarnings(KnowWEArticle article, Section<? extends KnowWEObjectType> s,
			Class<?> source, Collection<KDOMWarning> msgs) {
		KnowWEUtils.storeMessages(article, s, source, KDOMWarning.class, msgs);
	}

	public static Collection<KDOMReportMessage> getMessages(Section<? extends KnowWEObjectType> s, KnowWEArticle article) {
		Collection<KDOMReportMessage> msgs = new ArrayList<KDOMReportMessage>();
		msgs.addAll(KnowWEUtils.getMessages(article, s, KDOMError.class));
		msgs.addAll(KnowWEUtils.getMessages(article, s, KDOMWarning.class));
		msgs.addAll(KnowWEUtils.getMessages(article, s, KDOMNotice.class));
		return Collections.unmodifiableCollection(msgs);
	}

	public static Collection<KDOMError> getErrors(KnowWEArticle article, Section<? extends KnowWEObjectType> s) {
		return KnowWEUtils.getMessages(article, s, KDOMError.class);
	}

	public static Collection<KDOMNotice> getNotices(KnowWEArticle article, Section<? extends KnowWEObjectType> s) {
		return KnowWEUtils.getMessages(article, s, KDOMNotice.class);
	}

	public static Collection<KDOMWarning> getWarnings(KnowWEArticle article, Section<? extends KnowWEObjectType> s) {
		return KnowWEUtils.getMessages(article, s, KDOMWarning.class);
	}

	@Override
	public int hashCode() {
		// TODO better implementation possible
		return this.getVerbalization().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KDOMReportMessage) {
			// TODO better implementation possible
			return ((KDOMReportMessage) obj).getVerbalization().equals(
					this.getVerbalization());
		}
		return false;
	}

}
