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

package de.knowwe.rightpanel.watches;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.denkbares.collections.PriorityList;
import com.denkbares.plugin.Extension;
import com.denkbares.plugin.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.expression.ExpressionResolver;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 07.10.14.
 * <p>
 * This action handles the resolution of expressions. If you want to write your own ExpressionResolver you have to plug
 * it as an ExtensionPoint with point-id "ExpressionResolver" in your plugin.xml (a priority > 5 is recommended).
 * <p>
 */
public class GetExpressionValueAction extends de.knowwe.core.action.AbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(GetExpressionValueAction.class);

	private final static String EXPRESSION = "expressions";
	public static final String ID = "id";

	@Override
	public void execute(UserActionContext context) {
		String data = context.getParameter("data");
		JSONObject responseObject = new JSONObject();
		JSONArray responseArray = new JSONArray();
		try {
			JSONObject requestObject = new JSONObject(data);

			JSONArray expressionArray = requestObject.getJSONArray(EXPRESSION);
			ArticleManager articleManager = KnowWEUtils.getArticleManager(context.getWeb());
			for (int k = 0; k < expressionArray.length(); k++) {
				String expression = expressionArray.get(k)
						.toString().replaceAll("\u200b", "").replaceAll("\00a0", " ");
				JSONObject jsonValuesObject = getExpressionValue(expression, context, articleManager);
				responseArray.put(jsonValuesObject);
			}
			responseObject.put("values", responseArray);
			if (requestObject.has(ID)) {
				String id = requestObject.getString(ID);
				responseObject.put("id", id);
			}
			context.setHeader("Content-Type", "application/json; charset=UTF-8");
			responseObject.write(context.getWriter());
		}
		catch (JSONException | IOException e) {
			LOGGER.error("Exception while resolving watch expression: " + data + "\n", e);
		}

	}

	public static JSONObject getExpressionValue(String expr, UserActionContext context, ArticleManager articleManager) {
		JSONObject resultJsonObject = new JSONObject();
		Extension[] extensions = PluginManager.getInstance().getExtensions(
				"KnowWEExtensionPoints",
				"ExpressionResolver");

		PriorityList<Double, Extension> prioList = new PriorityList<>(100.0);
		for (Extension extension : extensions) {
			prioList.add(extension.getPriority(), extension);
		}

		for (Extension extension : prioList) {
			Object object = extension.getSingleton();
			if (object instanceof ExpressionResolver) {
				ExpressionResolver er = (ExpressionResolver) object;
				resultJsonObject = er.getValue(expr, context, articleManager);
				if (resultJsonObject.length() > 0) {
					return resultJsonObject;
				}

			}
		}
		return resultJsonObject;
	}
}
