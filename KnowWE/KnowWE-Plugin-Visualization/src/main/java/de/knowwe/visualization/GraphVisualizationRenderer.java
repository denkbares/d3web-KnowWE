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
package de.knowwe.visualization;

/**
 * Describes the interface of a visualization component to be used with the
 * RenderingCore. It can be used to implement different visualizations, e.g.,
 * DOT, D3, etc.
 * 
 * @author Jochen Reutelshoefer
 * @created 27.05.2013
 */
public interface GraphVisualizationRenderer {

	/**
	 * Generates the respective presentation of the subgraph.
	 * 
	 * @created 27.05.2013
	 * @return
	 */
	String generateSource();

	/**
	 * Returns the respective presentation of the subgraph.
	 * 
	 * @created 27.05.2013
	 * @return
	 */
	String getSource();

	/**
	 * Returns an html snipplet to be included within the website showing the
	 * final result of the graph visualization.
	 * 
	 * @created 27.05.2013
	 * @return
	 */
	String getHTMLIncludeSnipplet();

	/**
	 * Returns the full path of the files that are to be created (without the file-extension but
	 * including the file name and file ID). This is used to check if the files already exist and thus doesn't
	 * have to be re-rendered.
	 * @created 27.07.2014
	 * @return
	 */
	String getGraphFilePath();

	/**
	 * Returns the full path to the files that are to be created (without the file name!).
	 * @created 27.07.2014
	 * @return
	 */
	String getFilePath();

}
