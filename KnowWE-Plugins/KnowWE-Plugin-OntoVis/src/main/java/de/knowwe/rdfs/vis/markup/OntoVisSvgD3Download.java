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
package de.knowwe.rdfs.vis.markup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletContext;

import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * 
 * @author Max Matthe (denkbares GmbH)
 * @created 20.11.2013
 */
public class OntoVisSvgD3Download extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		ServletContext servletContext = context.getServletContext();

		String realPath = servletContext.getRealPath("");
		String separator = System.getProperty("file.separator");
		String tmpPath = separator + "KnowWEExtension" + separator + "tmp" + separator;
		String path = realPath + separator + tmpPath + "svgSource.svg";

		String svgSource = context.getParameters().get("data");

		File svgFile = new File(path);

		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(svgFile));
			writer.write(svgSource);

		}
		catch (IOException e)
		{
		}
		finally
		{
			try
			{
				if (writer != null) writer.close();
			}
			catch (IOException e)
			{
			}
		}

		String url = Environment.getInstance().getWikiConnector().getBaseUrl() + tmpPath
				+ "svgSource.svg";

		OutputStream ous = context.getOutputStream();
		PrintStream printStream = new PrintStream(ous);
		printStream.print(url);
		printStream.close();
		ous.close();

	}

}
