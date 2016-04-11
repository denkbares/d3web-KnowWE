package de.knowwe.core.action;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.d3web.strings.Identifier;
import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.objects.TermUtils;

import static java.util.stream.Collectors.toList;

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
		List<String> allTerms = TermUtils.getTermIdentifiers(web)
				.stream()
				.map(Identifier::toExternalForm)
				.collect(toList());

		JSONObject response = new JSONObject();
		try {
			response.accumulate("allTerms", allTerms);
			response.write(context.getWriter());
		}
		catch (JSONException e) {
			throw new IOException(e.getMessage());
		}
	}
}
