/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.rdfs.vis.markup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;

import com.denkbares.utils.Streams;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.rdfs.vis.util.Utils;
import de.knowwe.visualization.Config;
import de.knowwe.visualization.dot.DOTRenderer;

/**
 * Downloads a generated visualization file.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.05.15
 */
public abstract class OntoVisDownload extends AbstractAction {

	protected abstract String getExtension();

	@Override
	public void execute(UserActionContext context) throws IOException {

		ServletContext servletContext = context.getServletContext();
		if (servletContext == null) return; // at wiki startup only

		// find graph name
		Section<?> section = Sections.get(context.getParameter("SectionID"));
		Config config = new Config(Sections.cast(section, DefaultMarkupType.class), context);
		config.setCacheFileID(Utils.getFileID(section, context));
		File svg = new File(DOTRenderer.getFilePath(config) + "." + getExtension());
		String name = svg.getName();

		context.setContentType(BINARY);
		context.setHeader("Content-Disposition", "attachment;filename=\"" + name + "\"");

		InputStream fis = new FileInputStream(svg);
		OutputStream ous = context.getOutputStream();

		Streams.stream(fis, ous);

		// close the Stream
		fis.close();
		ous.close();
	}
}
