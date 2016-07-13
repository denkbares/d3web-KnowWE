/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.d3web.we.watches;

import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.knowledge.terminology.QuestionDate;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.ValueUtils;
import de.d3web.core.session.values.DateValue;
import com.denkbares.strings.Identifier;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.expression.ExpressionResolver;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 21.10.14.
 * <p/>
 * This class resolves simple expressions. Only basic d3web conditions can be resolved.
 */
public class GetValueObject implements ExpressionResolver {

	public static final String INFO = "info";
	public static final String KBSENTRY = "kbsEntries";

	@Override
	public JSONObject getValue(String expression, UserActionContext context, ArticleManager articleManager) {
		JSONObject resultObject = new JSONObject();
		if (isValueObject(expression, articleManager)) {
			Identifier exprIdentifier = Identifier.fromExternalForm(expression);
			Collection<D3webCompiler> compilers = Compilers.getCompilers(articleManager, D3webCompiler.class);
			resultObject = getValueObject(exprIdentifier, context, compilers);
			return resultObject;
		}
		return resultObject;
	}

	private static JSONObject getValueObject(Identifier identifier, UserActionContext context, Collection<D3webCompiler> compilers) {
		JSONObject resultJsonObject = new JSONObject();
		for (D3webCompiler compiler : compilers) {
			JSONObject kbvalue = new JSONObject();
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
			List<ValueObject> objects = kb.getManager().getObjects(ValueObject.class);
			ValueObject valueObject = getValueObjectFromBlackboard(objects, identifier);
			if (valueObject == null) {
				continue;
			}
			Session session = SessionProvider.getSession(context, kb);
			Value value = session.getBlackboard().getValue(valueObject);
			try {
				resultJsonObject.put(INFO, "simpleValue");
				kbvalue.put("kbname", kb.getName());
				String valueString;
				if (value instanceof DateValue) {
					valueString = ValueUtils.getDateOrDurationVerbalization((QuestionDate) valueObject,
							((DateValue) value).getDate());
				}
				else {
					valueString = value.toString();
				}
				kbvalue.put("value", valueString);
				resultJsonObject.append(KBSENTRY, kbvalue);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return resultJsonObject;
	}

	@SuppressWarnings("all")
	private static boolean isValueObject(String expr, ArticleManager articleManager) {
		Identifier exprIdentifier = Identifier.fromExternalForm(expr);
		Collection<TerminologyManager> terminologyManagers = KnowWEUtils.getTerminologyManagers(articleManager);
		boolean sameTerm = false;
		for (TerminologyManager terminologyManager : terminologyManagers) {
			sameTerm = findIdentifier(exprIdentifier, terminologyManager);
			if (sameTerm == true) return sameTerm;
		}
		return sameTerm;
	}

	@SuppressWarnings("all")
	private static boolean findIdentifier(Identifier exprIdentifier, TerminologyManager terminologyManager) {
		boolean sameTerm = false;
		Collection<Identifier> allDefinedTerms = terminologyManager.getAllDefinedTerms();
		for (Identifier allDefinedTerm : allDefinedTerms) {
			sameTerm = exprIdentifier.toExternalForm().toLowerCase().equals(
					allDefinedTerm.toExternalForm().toLowerCase());
			if (sameTerm) return sameTerm;
		}
		return sameTerm;
	}

	public static ValueObject getValueObjectFromBlackboard(List<ValueObject> objects, Identifier identifier) {
		for (ValueObject valueObject : objects) {
			if (identifier.equals(Identifier.fromExternalForm(valueObject.toString()))) {
				return valueObject;
			}
		}
		return null;
	}

}