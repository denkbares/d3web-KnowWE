package de.knowwe.core.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.terminology.TermIdentifier;
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

		// gathering all terms
		List<String> allTerms = new ArrayList<String>();
		Iterator<Article> iter = Environment.getInstance()
				.getArticleManager(web).getArticleIterator();
		Article currentArticle;

		TerminologyManager terminologyManager;
		while (iter.hasNext()) {
			currentArticle = iter.next();
			terminologyManager = KnowWEUtils
					.getTerminologyManager(currentArticle);
			Collection<TermIdentifier> allDefinedTerms = terminologyManager
					.getAllDefinedTerms();
			for (TermIdentifier definition : allDefinedTerms) {
				if (!allTerms.contains(definition.toExternalForm())) {
					allTerms.add(definition.toExternalForm());
				}
			}
		}

		// sort the terms and write the response
		Collections.sort(allTerms);
		JSONObject response = new JSONObject();
		try {
			response.accumulate("allTerms", allTerms);
			response.write(context.getWriter());
		} catch (JSONException e) {
			throw new IOException(e.getMessage());
		}
	}
}
