/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.d3web.core.io.PersistenceManager;
import de.d3web.core.io.PersistenceManager.KnowledgeBaseInfo;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.Resource;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;
import de.knowwe.dialog.action.StartCase.KnowledgeBaseProvider;
import de.knowwe.dialog.action.StartCase.StartInfo;

import static de.knowwe.dialog.SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS;

/**
 * Class to provide action for initializing the denkbares dialog web application. This Action is specialized to
 * incorporate with the Mobile Application.
 *
 * @author volker_belli
 * @created 18.04.2011
 */
public class Init extends AbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(Init.class);

	private static String knowledgeBaseRoot = SessionConstants.DEFAULT_KNOWLEDGE_FOLDER;

	/**
	 * Provides the knowledge base out of a specified file. It reloads the knowledge base only if the file has changed,
	 * otherwise the session-cached instance id reused.
	 *
	 * @author volker_belli
	 * @created 22.09.2010
	 */
	public static class FileProvider implements KnowledgeBaseProvider {
		private static final Logger LOGGER = LoggerFactory.getLogger(FileProvider.class);

		private final HttpSession httpSession;
		private final File knowledgeBaseFile;
		private final KnowledgeBaseInfo kbInfo;

		public FileProvider(HttpSession httpSession, File knowledgeBaseFile) throws IOException {
			this.httpSession = httpSession;
			this.knowledgeBaseFile = knowledgeBaseFile;
			PersistenceManager mgr = PersistenceManager.getInstance();
			this.kbInfo = mgr.loadKnowledgeBaseInfo(knowledgeBaseFile);
		}

		@Override
		public synchronized KnowledgeBase getKnowledgeBase(UserActionContext context) throws IOException {
			// check if file exists
			if (!knowledgeBaseFile.exists()) {
				throw new FileNotFoundException("cannot find knowledge base file '"
						+ knowledgeBaseFile.getCanonicalPath() + "'");
			}
			Date lastChanged = new Date(knowledgeBaseFile.lastModified());

			// if we already have a base, check if it is the same source file
			// and if the source file has not been changed since last access
			KnowledgeBase base = (KnowledgeBase) httpSession.getAttribute(SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);
			if (base != null) {
				File loadedFile = (File) httpSession.getAttribute(SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE_FILE);
				Date loadedDate = (Date) httpSession.getAttribute(SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE_DATE);
				if (loadedFile.equals(knowledgeBaseFile) && loadedDate.equals(lastChanged)) {
					LOGGER.info("reuse knowledge base '" + knowledgeBaseFile.getCanonicalPath() + "'");
					return base;
				}
			}

			// otherwise load the base and remember it for later use
			LOGGER.info("load knowledge base '" + knowledgeBaseFile.getCanonicalPath() + "'");
			PersistenceManager mgr = PersistenceManager.getInstance();
			base = mgr.load(knowledgeBaseFile);
			httpSession.setAttribute(
					SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE_FILE, knowledgeBaseFile);
			httpSession.setAttribute(SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE_DATE, lastChanged);
			httpSession.setAttribute(SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE, base);

			return base;
		}

		public File getKnowledgeBaseFile() {
			return knowledgeBaseFile;
		}

		@Override
		public String getName(UserActionContext context) {
			return kbInfo.getName();
		}

		@Override
		public String getDescription(UserActionContext context) {
			return kbInfo.getDescription();
		}

		@Override
		public Resource getFavIcon(UserActionContext context) {
			return kbInfo.getFavIcon();
		}
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		try {
			// start a case for the knowledge base (taken out of default folder)
			KnowledgeBaseProvider[] providers = createKnowledgeBaseProviders(context);
			HttpSession httpSession = context.getSession();
			httpSession.setAttribute(ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS, providers);
			if (providers.length == 1) {
				// make sure that a new session will be started
				// when initializing (for mobile usage only)
				StartCase cmd = (StartCase) Utils.getAction(StartCase.class.getSimpleName());
				assert cmd != null : "Invalid installation: command 'StartCase' is missing";
				cmd.startCase(context, providers[0], new StartInfo(true));
			}
			else {
				String language = context.getParameter(StartCase.PARAM_LANGUAGE);
				context.sendRedirect("Resource/ui.zip/html/selectBase.html?" +
						StartCase.PARAM_LANGUAGE + "=" + language);
			}
		}
		catch (IOException e) {
			Utils.redirectToErrorPage(context, e);
		}
	}

	private KnowledgeBaseProvider[] createKnowledgeBaseProviders(UserActionContext context) throws IOException {
		List<KnowledgeBaseProvider> result = new LinkedList<>();
		File[] bases = getKnowledgeBaseFiles(Utils.getRootDirectory(context));
		for (File file : bases) {
			result.add(new FileProvider(context.getSession(), file));
		}

		return result.toArray(new KnowledgeBaseProvider[0]);
	}

	/**
	 * Returns the knowledge base files stored in the specified path (folder or file). If the path itself is a file, it
	 * is returned. If the path is a folder, the list of knowledge bases are directly in that folder are returned.
	 *
	 * @param applicationRootPath the path to search at
	 * @return the knowledge base files found under that path
	 * @throws IOException if the path cannot be searched correctly
	 * @created 17.06.2011
	 */
	public static File[] getKnowledgeBaseFiles(File applicationRootPath) throws IOException {
		// check if our path is relative or absolute
		File path = new File(getKnowledgeBaseRoot());
		if (!path.isAbsolute()) {
			// if relative make it absolute to the server working directory
			path = new File(applicationRootPath, getKnowledgeBaseRoot());
		}

		// the check if path denotes a file, this should be loaded directly
		if (path.isFile()) {
			return new File[] { path };
		}
		// otherwise (if path denotes a folder), scan for knowledge bases there
		File[] bases = path.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File file, String name) {
				name = name.toLowerCase();
				return (name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith(".d3web"));
			}
		});
		if (bases == null) bases = new File[0];
		return bases;
	}

	/**
	 * Sets a knowledge base file or folder name (absolute or relative to the servers working directory). If a file is
	 * specified, this is the only knowledge base being available to be used. If a folder is specified, all d3web
	 * knowledge bases of that folder are available to be used.
	 *
	 * @param fileOrFolder the knowledge base file or a folder containing these files
	 * @created 18.04.2011
	 */
	public static void setKnowledgeBaseRoot(String fileOrFolder) {
		knowledgeBaseRoot = fileOrFolder;
	}

	/**
	 * Returns the knowledge base file or folder name previously set by {@link #setKnowledgeBaseRoot(String)}.
	 *
	 * @return the previously set path
	 * @created 17.06.2011
	 */
	public static String getKnowledgeBaseRoot() {
		return knowledgeBaseRoot;
	}
}
