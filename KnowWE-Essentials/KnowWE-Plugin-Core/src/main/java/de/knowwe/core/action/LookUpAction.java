package de.knowwe.core.action;

import de.knowwe.core.utils.KnowWEUtils;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.denkbares.strings.Identifier;
import de.knowwe.core.kdom.objects.TermUtils;

import static java.util.stream.Collectors.toList;

/**
 * @author Lukas Brehl
 * @created 11.12.2012
 */
public class LookUpAction extends AbstractAction {

	/**
	 * Returns the collected term identifiers of the web without targeting a specific article, so any authenticated
	 * user may call it.
	 */
	@Override
	public Action.Access requiredAccess() {
		return Action.Access.AUTH;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		KnowWEUtils.assertUserAuth(context);

		// gathering all terms
		List<String> allTerms = TermUtils.getTermIdentifiers(context)
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
