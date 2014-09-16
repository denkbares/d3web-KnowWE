/*
 * Copyright (C) 2013 denkbares GmbH
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

package de.knowwe.kdom.renderer;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Class for decorating a renderer with some tooltip. If the decorating renderer is null,
 * delegate-rendering of the sub-sections is used.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 01.03.14.
 */
public abstract class TooltipRenderer implements Renderer {

	private final Renderer delegate;

	public TooltipRenderer() {
		this(null);
	}

	public TooltipRenderer(Renderer delegate) {
		this.delegate = delegate;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		String tooltip = getTooltip(section, user);
		preRenderTooltip(tooltip, getTooltipDelay(section, user), result);
		if (delegate != null) {
			delegate.render(section, user, result);
		}
		else {
			DelegateRenderer.getInstance().render(section, user, result);
		}
		postRenderTooltip(tooltip, result);
	}

	protected abstract String getTooltip(Section<?> section, UserContext user);

	/**
	 * Set the delay in milliseconds after which the tooltip will be shown. Do not overwrite if you want default delay.
	 */
	protected int getTooltipDelay(Section<?> section, UserContext user) {
		return -1;
	}

	private void preRenderTooltip(String tooltip, int delay, RenderResult string) {
		if (Strings.isBlank(tooltip)) return;
		tooltip = KnowWEUtils.maskJSPWikiMarkup(tooltip.replace('\'', '"'));

		string.appendHtml("<span class='tooltipster'");
		if (delay >= 0) {
			string.append(" delay='").append(delay).append("'");
		}
		string.append(" title='").append(tooltip.replace("&", "&amp;").replace("'", "&#39;")).append("'");
		string.appendHtml(">");
	}

	private void postRenderTooltip(String tooltip, RenderResult string) {
		if (Strings.isBlank(tooltip)) return;
		string.appendHtml("</span>");
	}
}
