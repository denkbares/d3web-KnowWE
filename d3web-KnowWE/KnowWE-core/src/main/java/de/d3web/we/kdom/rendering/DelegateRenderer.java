/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.rendering;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMNotice;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.KDOMWarning;
import de.d3web.we.kdom.report.MessageRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DelegateRenderer extends KnowWEDomRenderer {

	private static DelegateRenderer instance;

	static {
		instance = new DelegateRenderer();
	}

	public static DelegateRenderer getInstance() {
		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// @Override
	// public String render(Section sec, KnowWEUserContext user) {
	//
	// // old hack - TODO: remove (not referring to ObjectType-Renderer
	// // causes loop with ErrorRenderer!!! -> deactivated
	// // if (sec.getRenderer() != null) {
	// // return sec.getRenderer().render(sec, user);
	// // }
	//
	// StringBuilder result = new StringBuilder();
	// List<Section> subsecs = sec.getChildren();
	// if (subsecs.size() == 0) {
	// return sec.getOriginalText();
	// }
	//
	// for (Section section : subsecs) {
	// try {
	// KnowWEObjectType objectType = section.getObjectType();
	// KnowWEDomRenderer renderer = RendererManager.getInstance()
	// .getRenderer(objectType, user.getUsername(),
	// sec.getTitle());
	// if (renderer == null) {
	// renderer = objectType.getRenderer();
	// }
	//
	// /* Once we have completely switched to new render-method
	// * deprecated call will be removed (also from the interface)
	// */
	// try {
	// renderer.render(section, user, result);
	// } catch (NotImplementedException e) {
	//					
	// result.append(renderer.render(section, user));
	// }
	//
	// } catch (Exception e) {
	// System.out.println(section.getObjectType());
	// e.printStackTrace();
	// }
	//
	// }
	// 
	// return result.toString();
	// }

	@Override
	public void render(KnowWEArticle article, Section section,
			KnowWEUserContext user, StringBuilder builder) {

		boolean renderTypes = isRenderTypes(user.getUrlParameterMap());
		if (renderTypes)
			renderType(section, true, builder);

		try {
			List<Section<?>> subSections = section.getChildren();
			if (subSections.size() == 0) {
				// KnowWEDomRenderer renderer = getRenderer(section, user);
				// if (renderer == this) {
				// // avoid endless recursion
				// builder.append(section.getOriginalText());
				// }
				// else {
				// // otherwise use section's renderer instead
				// renderer.render(article, section, user, builder);
				// }
				renderAnchor(section, builder);
				builder.append(section.getOriginalText());
			} else {
				for (Section<?> subSection : subSections) {
					renderSubSection(article, subSection, user, builder);
				}
			}
		} catch (Throwable e) {
			// wow, that was evil!
			// System.out.println(section.getObjectType());
			// e.printStackTrace();
			// now we log instead AND report the error to the user
			Logger.getLogger(getClass()).warn(
					"internal error while rendering section", e);
			builder.append(KnowWEUtils.maskHTML("<span class='warning'>"));
			builder.append("internal error while rendering section: " + e);
			builder.append(KnowWEUtils.maskHTML("</span>"));
		}

		if (renderTypes)
			renderType(section, false, builder);
	}

	protected void renderSubSection(KnowWEArticle article, Section<?> subSection, KnowWEUserContext user, StringBuilder builder) {
		renderAnchor(subSection, builder);

		// use subSection's renderer
		KnowWEDomRenderer renderer = getRenderer(subSection, user);
		renderer.render(article, subSection, user, builder);

		// Render notices
		Set<? extends KDOMNotice> notices = KDOMReportMessage
				.getNotices(subSection);
		if (notices != null && notices.size() > 0) {
			for (KDOMNotice kdomNotice : notices) {
				MessageRenderer noticeRenderer = subSection.get()
						.getNoticeRenderer();
				if (noticeRenderer != null) {
					builder.append(noticeRenderer.renderMessage(kdomNotice, user));
				}
			}
		}

		// Render warnings
		Set<? extends KDOMWarning> warnings = KDOMReportMessage
				.getWarnings(subSection);
		if (warnings != null && warnings.size() > 0) {
			for (KDOMWarning kdomWarning : warnings) {
				MessageRenderer warningRenderer = subSection.get()
						.getWarningRenderer();
				if (warningRenderer != null) {
					builder.append(warningRenderer
							.renderMessage(kdomWarning, user));
				}
			}
		}
	}

	private void renderAnchor(Section<?> subSection, StringBuilder builder) {
//		String anchor = subSection.getId();
//		builder.append(KnowWEUtils.maskHTML("<a name='kdomID-"+anchor+"'></a>"));
	}

	private void renderType(Section<?> section, boolean openIt,
			StringBuilder builder) {
		builder.append(KnowWEUtils.maskHTML("<sub>&lt;"));
		if (!openIt)
			builder.append('/');
		builder.append(section.getObjectType().getName());
		builder.append(KnowWEUtils.maskHTML("&gt;</sub>"));
	}

	private KnowWEDomRenderer getRenderer(Section<?> section,
			KnowWEUserContext user) {
		KnowWEDomRenderer renderer = null;
		if (KDOMReportMessage.getErrors(section) != null) {
			renderer = section.getObjectType().getErrorRenderer();
		}

		KnowWEObjectType objectType = section.getObjectType();
		if (renderer == null) {
			renderer = RendererManager.getInstance().getRenderer(objectType,
					user.getUsername(), section.getTitle());
		}
		if (renderer == null) {
			renderer = objectType.getRenderer();
		}
		return renderer;
	}

	private boolean isRenderTypes(Map<String, String> urlParameterMap) {
		String debug = urlParameterMap.get("renderTypes");
		return debug != null && debug.equals("true");
	}
}
