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

import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class DelegateRenderer extends KnowWEDomRenderer {

	private static DelegateRenderer instance;

	public static synchronized DelegateRenderer getInstance() {
		if (instance == null)
			instance = new DelegateRenderer();
		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

//	@Override
//	public String render(Section sec, KnowWEUserContext user) {
//
//		// old hack - TODO: remove (not referring to ObjectType-Renderer
//		// causes loop with ErrorRenderer!!! -> deactivated
////		if (sec.getRenderer() != null) {
////			return sec.getRenderer().render(sec, user);
////		}
//
//		StringBuilder result = new StringBuilder();
//		List<Section> subsecs = sec.getChildren();
//		if (subsecs.size() == 0) {
//			return sec.getOriginalText();
//		}
//
//		for (Section section : subsecs) {
//			try {
//				KnowWEObjectType objectType = section.getObjectType();
//				KnowWEDomRenderer renderer = RendererManager.getInstance()
//						.getRenderer(objectType, user.getUsername(),
//								sec.getTitle());
//				if (renderer == null) {
//					renderer = objectType.getRenderer();
//				}
//
//				/* Once we have completely switched to new render-method
//				 * deprecated call will be removed (also from the interface)
//				 */		
//				try {
//					renderer.render(section, user, result);
//				} catch (NotImplementedException e) {
//					
//					result.append(renderer.render(section, user));
//				}
//
//			} catch (Exception e) {
//				System.out.println(section.getObjectType());
//				e.printStackTrace();
//			}
//
//		}
//
//		return result.toString();
//	}

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder builder) {

		List<Section> subsecs = sec.getChildren();
		if (subsecs.size() == 0) {
			builder.append(sec.getOriginalText());
		}

		for (Section section : subsecs) {
			try {
				KnowWEObjectType objectType = section.getObjectType();
				KnowWEDomRenderer renderer = RendererManager.getInstance()
						.getRenderer(objectType, user.getUsername(),
								sec.getTitle());
				if (renderer == null) {
					renderer = objectType.getRenderer();
				}
			
				// TODO is section right here: Was sec befor
				// Johannes
				renderer.render(section, user, builder);
//				/* Once we have completely switched to new render-method
//				 * deprecated call will be removed (also from the interface)
//				 */	
//				try {
//					renderer.render(section, user, builder);
//				} catch (NotImplementedException e) {
//					//System.out.println("old rendering: "+section.getObjectType().toString() );
//					builder.append(renderer.render(section, user));
//				}

			} catch (Exception e) {
				System.out.println(section.getObjectType());
				e.printStackTrace();
			}
		}
	}
}
