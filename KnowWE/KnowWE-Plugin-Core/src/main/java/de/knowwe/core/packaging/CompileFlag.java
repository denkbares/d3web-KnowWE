/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.core.packaging;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageCompiler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class CompileFlag extends DefaultMarkupType {

	private static DefaultMarkup m = null;

	public static final String MARKUP_NAME = "Compile";

	static {
		m = new DefaultMarkup("Compile");
		m.addContentType(new CompileFlagContentType());

	}

	public CompileFlag() {
		super(m);
		this.setRenderer(new CompileFlagRenderer());
	}

	static class CompileFlagRenderer implements Renderer {

		@Override
		public void render(Section<?> sec,
				UserContext user,
				RenderResult string) {

			List<Section<SinglePackageReference>> packageReferences = new LinkedList<Section<SinglePackageReference>>();
			Sections.findSuccessorsOfType(sec, SinglePackageReference.class, packageReferences);
			if (packageReferences.isEmpty()) {
				DelegateRenderer.getInstance().render(sec, user, string);
				return;
			}
			string.appendHTML("<div id=\"knowledge-panel\" class=\"panel\">");
			string.appendHTML("<h3>" + "Compile: " + sec.getText() +
					"</h3><div>");
			for (Section<?> child : packageReferences) {
				if (child.get() instanceof SinglePackageReference) {
					((SinglePackageReferenceRenderer) child.get().getRenderer()).render(child,
							user, string);
				}
			}
			string.appendHTML("</div></div>");
		}

	}

	private static class CompileFlagContentType extends PackageCompileType {

		public CompileFlagContentType() {
			this.childrenTypes.add(new SinglePackageReference());
		}

		@Override
		public Set<String> getPackagesToCompile(Section<? extends PackageCompiler> section) {
			Set<String> packagesToCompile = new HashSet<String>();
			for (Section<?> child : section.getChildren()) {
				if (child.get() instanceof SinglePackageReference) {
					packagesToCompile.add(child.getText());
				}
			}
			return packagesToCompile;
		}
	}

}
