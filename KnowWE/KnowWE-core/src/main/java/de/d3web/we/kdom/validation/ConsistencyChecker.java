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

package de.d3web.we.kdom.validation;

import java.io.File;
import java.io.FileOutputStream;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.visitor.CreateTextVisitor;

public class ConsistencyChecker {

	private static ConsistencyChecker instance;

	public static synchronized ConsistencyChecker getInstance() {
		if (instance == null) {
			instance = new ConsistencyChecker();
		}

		return instance;
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone()
			throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public boolean checkConsistency(String text, KnowWEArticle art) {

		CreateTextVisitor vis = CreateTextVisitor.getInstance();
		vis.visit(art.getSection());
		String treeText = vis.getText();

		boolean matches = treeText.equals(text);

		if (!matches) {
			String path = KnowWEEnvironment.getInstance().getContext()
					.getRealPath("");
			File f = new File(path + "/KnowWEExtension/tmp/consistency/", art
					.getTitle()
					+ ".txt");
			File fTree = new File(path + "/KnowWEExtension/tmp/consistency/",
					art.getTitle() + "_tree.txt");

			try {
				FileOutputStream stream = new FileOutputStream(f);
				FileOutputStream streamTree = new FileOutputStream(fTree);
				stream.write(text.getBytes());
				streamTree.write(treeText.getBytes());
				stream.close();
				streamTree.close();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return matches;
	}
}
