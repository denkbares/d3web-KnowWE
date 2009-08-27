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
