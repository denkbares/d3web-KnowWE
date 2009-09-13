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

package de.d3web.tirex.gui;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TiRexLogger extends JFrame {
	private static TiRexLogger instance;

	private static JTextArea contentArea = null;

	private static StringBuffer content = new StringBuffer();

	private TiRexLogger() {
		super("TiRexLogger");
		setSize(640, 480);
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		Container c = this.getContentPane();

		c.add(new JScrollPane(getContentArea()));
	}

	public static TiRexLogger getInstance() {
		if (instance == null) {
			instance = new TiRexLogger();
		}

		return instance;
	}
	
	public void updateContent(String s) {
		content.append(s);
		updateContentArea();
	}

	private void updateContentArea() {
		contentArea.setText(content.toString());
	}

	private Component getContentArea() {
		if (contentArea == null) {
			contentArea = new JTextArea();
		}

		return contentArea;
	}
}
