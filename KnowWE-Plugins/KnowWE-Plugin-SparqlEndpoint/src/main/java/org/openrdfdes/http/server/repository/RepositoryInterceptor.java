/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package org.openrdfdes.http.server.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.http.protocol.Protocol;
import org.jetbrains.annotations.NotNull;
import org.openrdfdes.http.server.ClientHTTPException;
import org.openrdfdes.http.server.ProtocolUtil;
import org.openrdfdes.http.server.ServerHTTPException;
import org.openrdfdes.http.server.ServerInterceptor;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.sparqlendpoint.SparqlEndpointAction;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

/**
 * Interceptor for repository requests. Handles the opening and closing of
 * connections to the repository specified in the request. Should not be a
 * singleton bean! Configure as inner bean in openrdf-servlet.xml
 *
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author Stefan Plehn (modified)
 */
public class RepositoryInterceptor extends ServerInterceptor {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String REPOSITORY_ID_KEY = "repositoryID";

	private static final String REPOSITORY_KEY = "repository";

	private static final String REPOSITORY_CONNECTION_KEY = "repositoryConnection";

	/*-----------*
	 * Variables *
	 *-----------*/

	private RepositoryManager repositoryManager;

	private String repositoryID;

	private RepositoryConnection repositoryCon;

	/*---------*
	 * Methods *
	 *---------*/

	public void setRepositoryManager(RepositoryManager repMan) {
		repositoryManager = repMan;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse respons, Object handler)
			throws Exception {
		String pathInfoStr = request.getPathInfo();
		logger.debug("path info: {}", pathInfoStr);

		repositoryID = null;

		if (pathInfoStr != null && !pathInfoStr.equals("/")) {
			String[] pathInfo = pathInfoStr.substring(1).split("/");
			if (pathInfo.length > 0) {
				repositoryID = pathInfo[0];
				logger.debug("repositoryID is '{}'", repositoryID);
			}
		}

		ProtocolUtil.logRequestParameters(request);

		return super.preHandle(request, respons, handler);
	}

	@Override
	protected String getThreadName() {
		String threadName = Protocol.REPOSITORIES;

		if (repositoryID != null) {
			threadName += "/" + repositoryID;
		}

		return threadName;
	}

	@Override
	protected void setRequestAttributes(HttpServletRequest request)
			throws ClientHTTPException, ServerHTTPException {
		if (repositoryID != null) {
			try {
				Repository repository = repositoryManager.getRepository(repositoryID);

				if (repository == null) {
					throw new ClientHTTPException(SC_NOT_FOUND, "Unknown repository: " + repositoryID);
				}

				repositoryCon = repository.getConnection();

				// SES-1834 by default, the Sesame server should not treat datatype or language value verification errors
				// as fatal. This is to be graceful, by default, about accepting "dirty" data.
				// FIXME SES-1833 this should be configurable by the user.
				repositoryCon.getParserConfig().addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
				repositoryCon.getParserConfig().addNonFatalError(BasicParserSettings.VERIFY_LANGUAGE_TAGS);

				// FIXME: hack for repositories that return connections that are not
				// in auto-commit mode by default
				if (!repositoryCon.isAutoCommit()) {
					repositoryCon.setAutoCommit(true);
				}

				request.setAttribute(REPOSITORY_ID_KEY, repositoryID);
				request.setAttribute(REPOSITORY_KEY, repository);
				request.setAttribute(REPOSITORY_CONNECTION_KEY, repositoryCon);
			}
			catch (RepositoryConfigException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
			catch (RepositoryException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
		}
	}

	@Override
	protected void cleanUpResources()
			throws ServerHTTPException {
		if (repositoryCon != null) {
			try {
				repositoryCon.close();
			}
			catch (RepositoryException e) {
				throw new ServerHTTPException(e.getMessage(), e);
			}
		}
	}

	public static String getRepositoryID(HttpServletRequest request) {
		return (String) request.getAttribute(REPOSITORY_ID_KEY);
	}

	public static Repository getRepository(HttpServletRequest request) throws RepositoryException {
		return getRepositoryConnection(request).getRepository();
	}

	@NotNull
	public static Rdf2GoCore getRdf2GoCore(HttpServletRequest request) throws RepositoryException {
		String pack = request.getParameter(SparqlEndpointAction.PACKAGE);
		ArticleManager articleManager = KnowWEUtils.getArticleManager(Environment.DEFAULT_WEB);
		PackageManager packageManager = KnowWEUtils.getPackageManager(Environment.DEFAULT_WEB);
		Set<Section<? extends PackageCompileType>> compileSections = packageManager.getCompileSections(pack);
		Class<Rdf2GoCompiler> compilerClass = Rdf2GoCompiler.class;
		if (compileSections.isEmpty()) {
			List<Rdf2GoCompiler> compilers = new ArrayList<>(Compilers.getCompilers(articleManager, compilerClass));
			if (compilers.isEmpty()) {
				throw new RepositoryException("No repository found!");
			}
			compilers.sort(Comparator.comparing(c -> c.getRdf2GoCore().getStatementCacheSize()));
			return Rdf2GoCore.getInstance(compilers.get(compilers.size() - 1));
		}
		else {
			for (Section<? extends PackageCompileType> compileSection : compileSections) {
				Rdf2GoCompiler compiler = Compilers.getCompiler(compileSection, compilerClass);
				if (compiler != null) return compiler.getRdf2GoCore();
			}
		}
		throw new RepositoryException("No repository found for package " + pack);
	}

	public static RepositoryConnection getRepositoryConnection(HttpServletRequest request) throws RepositoryException {
		return getRdf2GoCore(request).getRepositoryConnection();
	}
}
