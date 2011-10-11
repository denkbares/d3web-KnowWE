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

import java.util.LinkedList;
import java.util.List;

import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageReference;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.KnowWEDomRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
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
		this.setCustomRenderer(new CompileFlagRenderer());
	}

	static class CompileFlagRenderer extends KnowWEDomRenderer<CompileFlag> {

		@Override
		public void render(KnowWEArticle article,
				Section<CompileFlag> sec,
				UserContext user,
				StringBuilder string) {

			List<Section<SinglePackageReference>> packageReferences = new LinkedList<Section<SinglePackageReference>>();
			Sections.findSuccessorsOfType(sec, SinglePackageReference.class, packageReferences);
			if (packageReferences.isEmpty()) {
				DelegateRenderer.getInstance().render(article, sec, user, string);
				return;
			}
			string.append(KnowWEUtils.maskHTML("<div id=\"knowledge-panel\" class=\"panel\">"));
			string.append(KnowWEUtils.maskHTML("<h3>" + "Compile: " + sec.getOriginalText() +
					"</h3><div>"));
			for (Section<?> child : packageReferences) {
				if (child.get() instanceof SinglePackageReference) {
					((SinglePackageReferenceRenderer) child.get().getRenderer()).render(article,
							child, user, string);
				}
			}
			string.append(KnowWEUtils.maskHTML("</div></div>"));
		}

	}

	private static class CompileFlagContentType extends PackageCompileType {

		public CompileFlagContentType() {
			this.childrenTypes.add(new SinglePackageReference());
		}

		@Override
		public List<String> getPackagesToCompile(Section<? extends PackageReference> s) {
			List<String> includes = new LinkedList<String>();
			for (Section<?> child : s.getChildren()) {
				if (child.get() instanceof SinglePackageReference) {
					includes.add(child.getOriginalText());
				}
			}
			return includes;
		}
	}


}
