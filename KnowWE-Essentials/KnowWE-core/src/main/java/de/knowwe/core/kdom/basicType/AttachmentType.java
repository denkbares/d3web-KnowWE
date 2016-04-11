/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.kdom.basicType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import de.d3web.strings.Strings;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.event.Event;
import de.knowwe.core.event.EventListener;
import de.knowwe.core.event.EventManager;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.Messages;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.event.AttachmentDeletedEvent;
import de.knowwe.event.AttachmentEvent;
import de.knowwe.event.AttachmentStoredEvent;

/**
 * A type that refers to a wiki attachment and checks for its existence.
 *
 * @author Reinhard Hatko
 * @created 06.11.2012
 */
public class AttachmentType extends AbstractType {

	private static final String LISTENER_KEY = "listener_key";

	public AttachmentType() {
		setSectionFinder(AllTextFinder.getInstance());
		addCompileScript(Priority.HIGHER, new DefaultGlobalScript<AttachmentType>() {

			@Override
			public void compile(DefaultGlobalCompiler compiler, Section<AttachmentType> section) {

				// check attachment validity
				String path = getPath(section);
				if (path.isEmpty()) {
					Messages.storeMessage(section, getClass(),
							Messages.syntaxError("No file specified."));
					return;
				}

				WikiAttachment attachment;
				try {
					attachment = getAttachment(section);
				}
				catch (IOException e) {
					Messages.storeMessage(section, getClass(),
							Messages.internalError("Could not access attachment '"
									+ path + "'.", e));
					return;
				}

				if (attachment == null) {
					Messages.storeMessage(section, getClass(),
							Messages.noSuchObjectError("Attachment", path));
					return;
				}

				// attach listener
				section.storeObject(compiler, LISTENER_KEY, new AttachmentChangedListener(section));

				Messages.clearMessages(section, getClass());
			}

			@Override
			public void destroy(DefaultGlobalCompiler compiler, Section<AttachmentType> section) {

				// clean up listener
				AttachmentChangedListener listener = (AttachmentChangedListener) section.removeObject(compiler, LISTENER_KEY);
				if (listener != null) listener.destroy();
			}

		});
	}

	public static WikiAttachment getAttachment(Section<AttachmentType> section) throws IOException {
		String path = getPath(section);
		return Environment.getInstance().getWikiConnector().getAttachment(path);

	}

	public static String getPath(Section<AttachmentType> section) {
		String path = Strings.trim(section.getText());
		if (!path.contains("/")) {
			path = section.getTitle() + "/" + path;
		}
		return path;
	}

	private static class AttachmentChangedListener implements EventListener {

		private Section<AttachmentType> section;

		public AttachmentChangedListener(Section<AttachmentType> section) {
			this.section = section;
			EventManager.getInstance().registerListener(this);
		}

		public void destroy() {
			EventManager.getInstance().unregister(this);
		}

		@Override
		public Collection<Class<? extends Event>> getEvents() {
			return Arrays.asList(AttachmentStoredEvent.class, AttachmentDeletedEvent.class);
		}

		@Override
		public void notify(Event event) {
			AttachmentEvent attachmentEvent = (AttachmentEvent) event;
			String web = section.getWeb();
			if (!web.equals(attachmentEvent.getWeb())) return;

			String thisAttachmentPath = getPath(section);
			String eventAttachmentPath = attachmentEvent.getPath();

			if (thisAttachmentPath.startsWith(eventAttachmentPath)) {

				// basically, do a full parse...
				ArticleManager articleManager = KnowWEUtils.getArticleManager(web);
				Article article = section.getArticle();
				Article newArticle = Article.createArticle(article.getText(), article.getTitle(), web);
				articleManager.registerArticle(newArticle);
			}
		}
	}
}
