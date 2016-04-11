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

package de.knowwe.core.expression;

/**
 * Created by Stefan Plehn (denkbares GmbH) on 21.10.14.
 */

import org.json.JSONObject;

import de.knowwe.core.ArticleManager;
import de.knowwe.core.action.UserActionContext;

/**
 * This interface handles appropriate expression resolutions. Every
 * ExpressionResolver has to return an empty JSONObject if it can't handle the expression. If it can handle the
 * expression return following JSONObject structure for ease of use in JavaScript.
 * <pre>
 *     response
 *     |
 *     +--info //with info of return type for custom handling
 *     |
 *     +-- values //with your values
 * </pre>
 */
public interface ExpressionResolver {
	/**
	 * @param expression     And expression to be evaluated
	 * @param context
	 * @param articleManager
	 */
	JSONObject getValue(String expression, UserActionContext context, ArticleManager articleManager);

}
