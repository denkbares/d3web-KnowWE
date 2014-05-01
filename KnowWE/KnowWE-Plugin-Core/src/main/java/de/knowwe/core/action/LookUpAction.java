package de.knowwe.core.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import de.d3web.strings.Identifier;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Lukas Brehl
 * @created 11.12.2012
 */
public class LookUpAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String web = context.getParameter(Attributes.WEB);
		String searchterm = context.getParameter("searchstring");

		// gathering all terms
		List<String> allTerms = new ArrayList<String>();
		Iterator<Article> iter = Environment.getInstance()
				.getArticleManager(web).getArticles().iterator();
		Article currentArticle;

		TerminologyManager terminologyManager;
		while (iter.hasNext()) {
			currentArticle = iter.next();
			terminologyManager = KnowWEUtils
					.getTerminologyManager(currentArticle);
			Collection<Identifier> allDefinedTerms = terminologyManager
					.getAllDefinedTerms();
			for (Identifier definition : allDefinedTerms) {
				if (!allTerms.contains(definition.toExternalForm())) {
					allTerms.add(definition.toExternalForm());
				}
			}
		}

		Collections.sort(allTerms);
		Collections.sort(allTerms, new LevenshteinComparator(searchterm));

		JSONObject response = new JSONObject();
		try {
			response.accumulate("allTerms", allTerms);
			response.write(context.getWriter());
		} catch (JSONException e) {
			throw new IOException(e.getMessage());
		}
	}

	public class LevenshteinComparator implements Comparator<String> {

		private final String searchString;

		public LevenshteinComparator(String searchString) {
			this.searchString = searchString;
		}

		@Override
		public int compare(String o1, String o2) {
			int first = StringUtils.getLevenshteinDistance(searchString, o1);
			int second = StringUtils.getLevenshteinDistance(searchString, o2);
			return first - second;
		}

	}
}
