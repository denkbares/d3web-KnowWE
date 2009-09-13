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

/* Created on 15. Januar 2005, 20:44 */
package de.d3web.textParser.decisionTable;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Speichert eine Tabelle als zweidimensionales Array ab und gibt die
 * Mï¿½glichkeit Fragen, Antworten und Diagnosen direkt abzufragen.
 * Die Abfrage der Fragen, Antworten und Diagnosen erfolgt nur anhand
 * der jeweiligen Positionen in der Tabelle.
 * Auch wenn ein Wert beispielsweise keine Frage ist, aber an entsprechender
 * Stelle in der Tabelle steht, wird er als Frage erkannt und ausgegeben.
 * @author  Andreas Klar
 */
public class DecisionTable {
    
    private String[][] tableData;
    private Map<Integer, Integer> alternatives = new Hashtable<Integer, Integer>();
    private int rows;
    private int columns;
    private String sheetName;
    private int tableNum;
    
    /**
     * Erzeugt eine Tabelle fï¿½r den XLSParser unter Angabe der Inhalte der Datenzellen
     * @param data Inhalt der Tabelle als zweidimensionales Array
     */
    public DecisionTable(String[][] data) {
        this.tableData = data;
        this.rows = tableData.length;
        this.columns = tableData[0].length;
        
        if (this.columns>1) {
            // Ordnet die Symptom-Ausprï¿½gungen dem entsprechenden Symptom zu
            int lastSym=0;
            for (int i=0; i<this.rows; i++) {
                if (!this.tableData[i][0].equals(""))
                    lastSym=i;
            	//                if (!this.tableData[i][1].equals(""))
                this.alternatives.put(new Integer(i), new Integer(lastSym));
            }
        }
    }
    
    public String[][] getTableData() {
        return this.tableData;
    }
    
    public String get(int row, int column) {
    	if (row>=rows)
    		return "";
    	if (column>=columns)
    		return "";
    	return this.tableData[row][column];
    }
   
    public boolean isEmptyCell(int row, int column) {
    	return this.tableData[row][column].equals("") ? true : false;
    }
    
    public boolean isEmptyRow(int row) {
    	return isEmptyRow(row, 0);
    }
    
    public boolean isEmptyRow(int row, int startColumnIndex) {
    	for (int col=startColumnIndex; col<columns; col++) {
    		if (!tableData[row][col].trim().equals(""))
    			return false;
    	}
    	return true;
    }
    
    public int rows() {
        return this.rows;
    }
    
    public int columns() {
        return this.columns;
    }
    
    public void setSheetName(String sheetName) {
    	this.sheetName = sheetName;
    }
    
    public String getSheetName() {
    	return this.sheetName;
    }

    public void setTableNumber(int number) {
    	this.tableNum = number;
    }
    
    public int getTableNumber() {
    	return this.tableNum;
    }
    
    /**
     * Liefert den Index der Zeile in welcher die Frage steht, zu welcher die
     * Antwort in der angebenen Zeile gehï¿½rt
     * @param answerRowIndex Zeile der Antwortalternative
     * @return Zeile der Frage, zu welcher die angegebene Antwort gehï¿½rt
     */
    public int getQuestionRowIndex(int answerRowIndex) {
        if (this.alternatives.containsKey(new Integer(answerRowIndex))) {
            Integer i = this.alternatives.get(new Integer(answerRowIndex));
            return i.intValue();
        }
        else
            return answerRowIndex;
    }
    
    /**
     * Liefert den Text der Frage, zu welcher die Antwort in der angegeben Zeile gehï¿½rt,
     * als String.
     * @param answerRowIndex Zeile der Antwortalternative
     * @return Text der Frage zur Antwortalternative in der angegebenen Zeile
     */    
    public String getQuestionText(int answerRowIndex) {
        return this.tableData[getQuestionRowIndex(answerRowIndex)][0];
    }
    
    /**
     * Liefert alle Fragen, die in der Tabelle auftauchen
     * @return alle gefundenen Fragen
     */ 
    public List<String> getQuestions() {
        List<String> questions = new ArrayList<String>(1);
        for (int i=0; i<this.rows; i++) {
            if (!this.tableData[i][0].equals(""))
                questions.add(this.tableData[i][0]);
        }
        return questions;
    }
    
    /**
     * Liefert alle Antwortalternativen, die in der Tabelle auftauchen
     * @return alle gefundenen Antwortalternativen
     */ 
    public List<String> getAllAnswers() {
        List<String> answers = new ArrayList<String>(1);
        for (int i=0; i<this.rows; i++) {
            if (!this.tableData[i][1].equals(""))
                answers.add(this.tableData[i][1]);
        }
        return answers;
    }
    
    /**
     * Liefert alle Diagnosen, die in der Tabelle auftauchen
     * @return alle gefundenen Diagnosen
     */ 
    public List<String> getDiagnoses() {
        List<String> diagnoses = new ArrayList<String>(1);
        for (int j=0; j<this.columns; j++) {
            if (!this.tableData[0][j].equals(""))
                diagnoses.add(this.tableData[0][j]);
        }
        return diagnoses;
    }
    
    /**
     * Liefert eine String-Reprï¿½sentation der Tabelle
     * @return String-Reprï¿½sentation der Tabelle
     */
    @Override
	public String toString() {
        String[][] c = this.tableData;
        int[] columnsizes = new int[this.columns()];
        for (int j=0; j<columnsizes.length; j++) {
            int maxLength = 0;
            for (int i=0; i<this.rows(); i++) {
                if (c[i][j].length()>maxLength)
                    maxLength = c[i][j].length();
            }
            columnsizes[j] = maxLength + 4;
        }
        String ret = new String();
        for (int i=-2; i<c.length; i++) {
            if (i>=0) {
                StringBuffer lineNumber = new StringBuffer(Integer.toString(i+1));
                while (lineNumber.length()<4)
                    lineNumber.append(" ");
                ret += lineNumber + "| ";
            }
            for (int j=0; j<c[0].length; j++) {
                StringBuffer output;
                if (i==-2) {
                    if (j==0)
                        ret += "    | ";
                    output = new StringBuffer(Integer.toString(j+1));
                    while (output.length()<columnsizes[j])
                        output.append(" ");
                }
                else if (i==-1) {
                    output = new StringBuffer();
                    if (j==0)
                        ret += "----|";
                    
                    while (output.length()<columnsizes[j])
                        output.append("-");
                }
                else {
                    output = new StringBuffer(c[i][j]);
                    while (output.length()<columnsizes[j])
                        output.append(" ");
                }

                ret += output;
            }
            ret += "\n\r";
        }
        return ret;
    }
    
    public static final String columnDescriptor(int colNo) {
    	int digit2 = colNo/26;
    	int digit1 = colNo%26;
    	String[] digits = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I",
    			"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W",
    			"X", "Y", "Z" };
    	String colDesc = "";
    	if (digit2>0)
    		colDesc += digits[digit2-1];
    	colDesc += digits[digit1];
    	return colDesc;
    }
    
}
