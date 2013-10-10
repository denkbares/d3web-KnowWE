/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.timeline;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import de.d3web.core.knowledge.terminology.Question;
import de.knowwe.timeline.parser.ParseException;
import de.knowwe.timeline.parser.QueryLang;
import de.knowwe.timeline.parser.SimpleNode;
import de.knowwe.timeline.tree.QuestionVisitor;
import de.knowwe.timeline.tree.TimesetVisitor;

/**
 * 
 * @author Tobias Bleifuss, Steffen Hoefner
 */
public class Query {
	/**
	 * The query text.
	 */
	private final String query;
	/**
	 * The root node of the parsed query's AST.
	 */
	private final SimpleNode root;

	/**
	 * 
	 * 
	 * @param query
	 * 	The query text.
	 * @throws ParseException
	 * 	if the query text could not be parsed.
	 */
	public Query(String query) throws ParseException {
		this.query = query;
		this.root = parseQuery();
	}

	public String getQuery() {
		return query;
	}

	public Timeset execute(IDataProvider dataProvider) {
		TimesetVisitor visitor = new TimesetVisitor(dataProvider);
		return root.jjtAccept(visitor, null);
	}

	public Set<Question> getQuestions(IDataProvider dataProvider) {
		QuestionVisitor visitor = new QuestionVisitor(dataProvider);
		root.jjtAccept(visitor, null);
		return visitor.getQuestions();
	}

	private SimpleNode parseQuery() throws ParseException {
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(query.getBytes());
			QueryLang parser = new QueryLang(is);
			return parser.query();
		}
		finally {
			closeQuietly(is);
		}
	}

	private void closeQuietly(InputStream is) {
		if (is == null) return;
		try {
			is.close();
		}
		catch (IOException ignored) {
		}
	}
}
