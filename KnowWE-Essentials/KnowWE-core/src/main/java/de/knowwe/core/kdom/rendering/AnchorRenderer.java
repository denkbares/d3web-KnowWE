/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.knowwe.core.kdom.rendering;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * The AnchorKDOMRender prefixes a section with an HTML anchor. This anchor can
 * be used to link from other articles to the section.
 * 
 * @author Volker Belli
 * @since 16.08.2013
 */
public class AnchorRenderer implements Renderer {

	private static final AnchorRenderer DELEGATE_INSTANCE = new AnchorRenderer();

	private final Renderer delegate;
	private final String separator;

	public AnchorRenderer() {
		this("");
	}
	public AnchorRenderer(String separator) {
		this(DelegateRenderer.getInstance(), separator);
	}

	public AnchorRenderer(Renderer delegate) {
		this(delegate, "");
	}

	public AnchorRenderer(Renderer delegate, String separator) {
		this.delegate = delegate;
		this.separator = separator;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		KnowWEUtils.renderAnchor(section, result);
		result.append(separator);
		delegate.render(section, user, result);
	}

	/**
	 * Returns a renderer that renders an anchor and used the delegate renderer
	 * to render the contents of the section.
	 * 
	 * @created 16.08.2013
	 * @return the anchor+delegate renderer
	 */
	public static Renderer getDelegateInstance() {
		return DELEGATE_INSTANCE;
	}
}
