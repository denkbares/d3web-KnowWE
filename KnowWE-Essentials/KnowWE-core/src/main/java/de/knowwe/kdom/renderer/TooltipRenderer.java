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

import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.denkbares.strings.Strings;
import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Class for decorating a renderer with some tooltip. If the decorating renderer is null, delegate-rendering of the
 * sub-sections is used.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 01.03.2014
 */
public abstract class TooltipRenderer implements Renderer {

	private final Renderer delegate;

	/**
	 * Creates a new renderer that (potentially) displays a tooltip for the rendered sections.
	 */
	public TooltipRenderer() {
		this(null);
	}

	/**
	 * Creates a new renderer that (potentially) displays a tooltip for the rendered sections. It used the specified
	 * delegate renderer, to render the section's content that will be fitted with a tooltip.
	 */
	public TooltipRenderer(Renderer delegate) {
		this.delegate = delegate;
	}

	/**
	 * Utility method to create a tooltip renderer without subclassing this class. Instead, you can provide a supplier
	 * function that returns the tooltip, or null if no tooltip should be provided for the section.
	 *
	 * @param delegate the delegate renderer to be decorated by a tooltip
	 * @param supplier the tooltip supplier function
	 * @return the tooltip as HTML text, or null
	 */
	public static TooltipRenderer create(Renderer delegate, Function<Section<?>, String> supplier) {
		return create(delegate, (section, user) -> supplier.apply(section));
	}

	/**
	 * Utility method to create a tooltip renderer without subclassing this class. Instead, you can provide a supplier
	 * function that returns the tooltip, or null if no tooltip should be provided for the section.
	 *
	 * @param delegate the delegate renderer to be decorated by a tooltip
	 * @param supplier the tooltip supplier function
	 * @return the tooltip as HTML text, or null
	 */
	public static TooltipRenderer create(Renderer delegate, BiFunction<Section<?>, UserContext, String> supplier) {
		return new TooltipRenderer(delegate) {
			@Override
			public String getTooltip(Section<?> section, UserContext user) {
				return supplier.apply(section, user);
			}
		};
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		boolean hasTooltip = hasTooltip(section, user);
		if (hasTooltip) preRenderTooltip(section, user, result);
		if (delegate != null) {
			delegate.render(section, user, result);
		}
		else {
			DelegateRenderer.getInstance().render(section, user, result);
		}
		if (hasTooltip) postRenderTooltip(result);
	}

	/**
	 * Returns true if there is a tooltip to be potentially displayed for the specified section and the specified user
	 * context. You may overwrite this method to perform a fast check as it is called every time the section is
	 * rendered. If not, it defaults to true, if a non-blank tooltip could be created, see {@link #getTooltip(Section,
	 * UserContext)}.
	 *
	 * @param section the section to create the tooltip for
	 * @param user    the user context to create the tooltip for
	 * @return true if a tooltip is available
	 */
	public boolean hasTooltip(Section<?> section, UserContext user) {
		return Strings.nonBlank(getTooltip(section, user));
	}

	/**
	 * Returns the tooltip to be displayed for the specified section and the specified user context. The method may
	 * return null (or an empty or blank string), if there is no tooltip to be displayed. Otherwise the returned text
	 * should be HTML syntax.
	 * <p>
	 * If this method is complex to compute, you may consider to overwrite {@link #hasTooltip(Section, UserContext)} to
	 * perform a fast check, to avoid that this method is called frequently.
	 *
	 * @param section the section to create the tooltip for
	 * @param user    the user context to create the tooltip for
	 * @return the tooltip as HTML text, or null
	 */
	public abstract String getTooltip(Section<?> section, UserContext user);

	/**
	 * Set the delay in milliseconds after which the tooltip will be shown. Do not overwrite if you want default delay.
	 */
	protected int getTooltipDelay(Section<?> section, UserContext user) {
		return -1;
	}

	private void preRenderTooltip(Section<?> section, UserContext user, RenderResult string) {

		String toolTipAction = "action/RenderTooltipAction"
				+ "?" + Attributes.SECTION_ID + "=" + section.getID();

		ArrayList<String> attributes = new ArrayList<>();
		attributes.add("class");
		attributes.add("tooltipster");
		int tooltipDelay = getTooltipDelay(section, user);
		if (tooltipDelay >= 0) {
			attributes.add("delay");
			attributes.add(String.valueOf(tooltipDelay));
		}
		attributes.add("data-tooltip-src");
		attributes.add(toolTipAction);
		string.appendHtmlTag("span", attributes.toArray(new String[0]));
	}

	private void postRenderTooltip(RenderResult string) {
		string.appendHtml("</span>");
	}
}
