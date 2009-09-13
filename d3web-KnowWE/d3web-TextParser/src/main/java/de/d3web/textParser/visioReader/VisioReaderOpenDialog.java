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

package de.d3web.textParser.visioReader;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultFormatter;

import de.d3web.utilities.swing.LineSeparator;

//import de.d3web.ka.plugin.report.ReportPlugin;

public class VisioReaderOpenDialog extends JPanel implements ActionListener {
	
	private JButton importButton;
	private JLabel label;
	private JCheckBox checkBoxCreateKnowmeProject;
	private JFileChooser fileChooser;
	private JButton searchButton;
	private JFormattedTextField textField;
	
	private double size[][] = { {10, 100, 5, 300, 5, TableLayoutConstants.PREFERRED, 10},  // Columns
		{10, TableLayoutConstants.PREFERRED, 5, TableLayoutConstants.PREFERRED, 5, TableLayoutConstants.PREFERRED, 10}};
	
	public VisioReaderOpenDialog()
	{
//		super(new BorderLayout());
//		
//		setLayout(new BoxLayout(this,
//                BoxLayout.Y_AXIS));
//		
//		fileChooser = new JFileChooser();
//		FileNameExtensionFilter filter = new FileNameExtensionFilter(
//		        "Visio XML Files (.vdx)", "vdx");
//		fileChooser.setFileFilter(filter);		
//		
//		openButton = new JButton("Open a Visio File...");
//		openButton.addActionListener(this);
//		
//		createKnowmeProject = new JCheckBox("Create KnowME Project after Visio Import");
//		createKnowmeProject.addActionListener(this);
//		
//		JPanel buttonPanel = new JPanel(); //use FlowLayout
//        buttonPanel.add(openButton, BorderLayout.CENTER);
//        buttonPanel.add(createKnowmeProject, BorderLayout.PAGE_END);
//        
//        //Add the buttons and the log to this panel.
//        add(buttonPanel, BorderLayout.CENTER );
		
		this.label = new JLabel("Visio .VDX File:");
		initialize();
		
	}
	
	private void initialize()
	{
		this.setLayout(new TableLayout(size));

		// text field
		DefaultFormatter formatter = new DefaultFormatter();
		formatter.setOverwriteMode(false);
		formatter.setValueClass(File.class);
		textField = new JFormattedTextField(formatter);
		textField.setFocusLostBehavior(JFormattedTextField.COMMIT);
		
		// label	
		label.setLabelFor(textField);
		this.add(label, "1,1,r,c");
		
		
//		textField.addPropertyChangeListener("value", new PropertyChangeListener() {
//			public void propertyChange(PropertyChangeEvent evt) {
//				owner.refreshImportButtonStatus();					
//			}
//		});
		this.add(textField, "3,1");	
		
		// search button
		searchButton = new JButton("Durchsuchen...");
		searchButton.setActionCommand("search");
		this.add(searchButton, "5,1");
		searchButton.addActionListener(this);		
		
		this.add(new LineSeparator(), "1, 3, 5, 3");
		
		checkBoxCreateKnowmeProject = new JCheckBox("Create KnowME Project after Visio Import");
		checkBoxCreateKnowmeProject.addActionListener(this);
		this.add(checkBoxCreateKnowmeProject, "1, 5, 3, 5");	
		
		// search button
		importButton = new JButton("Importieren!");
		importButton.setActionCommand("import");
		this.add(importButton, "5,5");
		importButton.addActionListener(this);		
		
//		int height = this.getPreferredSize().height + 380;
//		this.setSize(600, height);		
		
		
	}
	
    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == searchButton) {
        	
    		fileChooser = new JFileChooser();
    		FileNameExtensionFilter filter = new FileNameExtensionFilter(
    		        "Visio XML Files (.vdx)", "vdx");
    		fileChooser.setFileFilter(filter);        	
    		
            int returnVal = fileChooser.showOpenDialog(VisioReaderOpenDialog.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File theFile = fileChooser.getSelectedFile();
                textField.setValue(theFile);
                //This is where a real application would open the file.
                //VisioXMLReader vr = new VisioXMLReader());
            } 
        }
        else if (e.getSource() == importButton){
        	
        	File impFile = new File(textField.getText());
        	
        	if (!impFile.isFile()){
        		System.err.println("No valid File selected!");
        		//textField.setText("");
        		return;
        	}
        	VisioXMLReader vr = new VisioXMLReader(impFile);
        	vr.parse();

        }
    }
    
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("VisioReaderOpenDialog");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JPanel newContentPane = new VisioReaderOpenDialog();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

		frame.setLocation(300,300);        
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	createAndShowGUI();
                
            }
        });
    }    

}
