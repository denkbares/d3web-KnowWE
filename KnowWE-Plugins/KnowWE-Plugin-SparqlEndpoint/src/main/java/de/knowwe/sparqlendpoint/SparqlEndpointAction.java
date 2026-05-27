package de.knowwe.sparqlendpoint;
/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wiki.auth.WikiSecurityException;
import org.openrdfdes.http.server.repository.RepositoryController;
import org.springframework.web.servlet.ModelAndView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.jspwiki.JSPAuthenticationManager;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 13.01.15.
 * <p>
 * This action provides a Sparql Endpoint for KnowWE.<br>
 * You can use it like this:<br>
 * http://localhost:8080/KnowWE/action/SparqlEndpointAction?query=$$SPARQL_QUERY$$<br>
 * following parameters for the request are optional:<br>
 * - package: the package of the ontology in KnowWE (if you don't provide a package parameter the first ontology
 * in KnowWE will be
 * used<br>
 * - user: your KnowWE user name<br>
 * - password: your KnowWE password<br>
 * <b>Important:</b><br>
 * You have to provide user name and password if your client is not already logged in into KnowWE.<br>
 * Additionally you have to be a member of the KnowWE group "SparqlEndpoint" to have access.<br>
 * <br>
 * Example for a full URL:<br>
 * http://localhost:8080/KnowWE/action/SparqlEndpointAction?query=SELECT%20?actorLabel%20?movieLabel%20WHERE%20{%20?actor%20lns:playedIn%20?movie.%20?actor%20rdfs:label%20?actorLabel.%20?movie%20rdfs:label%20?movieLabel.%20FILTER%20langMatches(%20lang(?movieLabel),%20%22en%22).%20}%20ORDER%20BY%20?actorLabel&package=jamesDeanMovies&user=StefanPlehn&password=test<br>
 * <br>For further information: http://www.w3.org/TR/sparql11-protocol/<br>
 */
public class SparqlEndpointAction extends AbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(SparqlEndpointAction.class);

	public static final String PACKAGE = "package";
	public static final String GROUPNAME = "SparqlEndpoint";
	public static final String USER = "user";
	public static final String PASSWORD = "password";

	@Override
	public void execute(UserActionContext context) throws IOException {

		WikiConnector wikiConnector = Environment.getInstance().getWikiConnector();

		JSPAuthenticationManager manager = (JSPAuthenticationManager) context.getManager();
		HttpServletRequest request = context.getRequest();

		if (!manager.userIsAuthenticated()) {
			if (hasLoginDataInParameters(request)) {
				try {
					manager.login(request, request.getParameter(USER), request.getParameter(PASSWORD));
				}
				catch (WikiSecurityException e) {
					context.sendError(HttpServletResponse.SC_FORBIDDEN,
							"Your user name or password is not correct.");
				}
			}
		}
		if (wikiConnector.userIsMemberOfGroup(GROUPNAME, context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You are not allowed to use this sparql endpoint. Please talk to your administrator.");
			return;
		}

		RepositoryController repositoryController = new RepositoryController();

		try {
			ModelAndView modelAndView = repositoryController.handleRequest(context.getRequest(), context.getResponse());
			modelAndView.getView().render(modelAndView.getModel(), request, context.getResponse());
		}
		catch (Exception e) {
			LOGGER.error("Exception while executing query on SPARQL endpoint.", e);
		}

	}

	private boolean hasLoginDataInParameters(HttpServletRequest request) {
		String username = request.getParameter(USER);
		String password = request.getParameter(PASSWORD);
		return !(username == null || password == null);
	}

}
