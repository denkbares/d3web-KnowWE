/*
 * Copyright (C) 2010 denkbares GmbH, Germany
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
package de.d3web.we.object;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.ValueObject;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.session.Session;
import de.d3web.core.session.Value;
import de.d3web.core.session.values.UndefinedValue;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.solutionpanel.SolutionPanelUtils;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.Strings;

/**
 * Renders a D3webTerm section by adding the current value(s) as a tooltip.
 * 
 * @author volker_belli
 * @created 30.11.2010
 */
public class ValueTooltipRenderer implements Renderer {

	private final Renderer decoratedRenderer;

	public ValueTooltipRenderer(Renderer decoratedRenderer) {
		this.decoratedRenderer = decoratedRenderer;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {
		String tooltip = createTooltip(section, user);
		preRenderTooltip(tooltip, string);
		decoratedRenderer.render(section, user, string);
		postRenderTooltip(tooltip, string);
	}

	private String createTooltip(Section<?> section, UserContext user) {
		SessionProvider provider = SessionProvider.getSessionProvider(user);
		if (provider == null) return null;
		if (!(section.get() instanceof D3webTerm<?>)) return null;

		@SuppressWarnings("unchecked")
		Section<D3webTerm<NamedObject>> sec = (Section<D3webTerm<NamedObject>>) section;
		String web = user.getWeb();
		ArticleManager articleManager = Environment.getInstance().getArticleManager(web);
		PackageManager packageManager = Environment.getInstance().getPackageManager(web);
		StringBuilder buffer = new StringBuilder();
		for (String articleName : packageManager.getCompilingArticles(section)) {
			Article article = articleManager.getArticle(articleName);
			NamedObject namedObject = sec.get().getTermObject(article, sec);
			KnowledgeBase knowledgeBase = D3webUtils.getKnowledgeBase(article);
			Session session = provider.getSession(knowledgeBase);
			if (namedObject instanceof ValueObject) {
				Value value = D3webUtils.getValueNonBlocking(session, (ValueObject) namedObject);
				if (value == null) continue;
				if (UndefinedValue.isUndefinedValue(value)) continue;
				if (buffer.length() > 0) buffer.append('\n');
				String name = knowledgeBase.getName();
				if (name == null) name = articleName;
				buffer.append("current value in '").append(name).append("': ");
				buffer.append(SolutionPanelUtils.formatValue(value, 2));
			}
		}
		if (buffer.length() == 0) return null;
		return buffer.toString();
	}

	private void preRenderTooltip(String tooltip, RenderResult string) {
		if (tooltip == null) return;
		tooltip = Strings.maskJSPWikiMarkup(tooltip.replace('\'', '"'));

		string.appendHTML("<span");
		string.append(" title='").append(tooltip).append("'");
		string.appendHTML(">");
	}

	private void postRenderTooltip(String tooltip, RenderResult string) {
		if (tooltip == null) return;
		string.appendHTML("</span>");
	}

}
