/* Created on 25. Januar 2005, 17:49 */
package de.d3web.textParser.decisionTable;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Erweitert die Funktionen des HSSF-POI packages um Funktionen zur
 * Überprüfung, ob eine Reihe tatsächlich leer ist. Dies wird benötigt,
 * da manche Reihen nicht immer als leer erkannt werden, wenn ein zuvor
 * vorhandener Wert aus einer Zelle gelöscht wurde.
 * @author  Andreas Klar
 */
public class HSSFFunctions {
        
    /**
     * Liefert die letzte Zeile eines Excel-Sheets, welche Daten enthält,
     * unter der Verwendung der Funktion isEmptyRow().
     * @param sheet Excel-Sheet, dessen letzte Zeile gefunden werden soll
     * @return Index der letzten Zeile, welche Daten enthält
     */    
    public static int getLastRowNum(HSSFSheet sheet) {
        int lastRowNum = sheet.getLastRowNum();
        for (int i=lastRowNum; i>0; i--) {
            HSSFRow lastRow = sheet.getRow(i);
            if (isEmptyRow(lastRow))
                lastRowNum--;
            else
                return lastRowNum;
        }
        return lastRowNum;
    }
    
    /**
     * Überprüft, ob eine Zeile tatsächlich leer ist.
     * @param row Die Reihe welche überprüft werden soll
     * @return liefert <CODE>true</CODE>, wenn keine Zelle der Zeile einen Wert enthält,
     * ansonsten <CODE>false</CODE>
     */    
    public static boolean isEmptyRow(HSSFRow row) {
        if (row==null)
            return true;
        boolean isEmpty = true;
        for (int j=0; isEmpty && j<row.getLastCellNum(); j++) {
            HSSFCell cell = row.getCell((short)j);
            String value = "";
            try {
                value = cell.getStringCellValue().trim();
            }
            catch (Exception e) {
                try {
                    value = Double.toString(cell.getNumericCellValue());
                }
                catch (Exception f) {}
            }
            if (!value.equals(""))
                isEmpty = false;
        }
        return isEmpty;
    }

    /**
     * Extrahiert alle Tabellen aus allen Worksheets
     * Auf einem Sheet beginnt eine neue Tabelle, sobald <CODE>emptyRowBoundary<CODE> leere Zeilen auftauchen
     * @param workbook Workbook, dessen Tabellen extrahiert werden sollen
     * @param emptyRows Anzahl der leeren Zeilen, die maximal in einer Tabelle enthalten
     * sein dürfen bis eine neue Tabelle extrahiert wird
     * @return Liste der Tabellen
     */
    public static List<DecisionTable> extractTables(HSSFWorkbook workbook, int emptyRowBoundary) {
        if (emptyRowBoundary<0) emptyRowBoundary = 0;
    	List<DecisionTable> tables = new ArrayList<DecisionTable>(1);
    	int size = workbook.getNumberOfSheets();
    	for (int i=0; i< size; i++) {
            HSSFSheet sheet = workbook.getSheetAt(i);
            
            int sheetLastRow = HSSFFunctions.getLastRowNum(sheet);
            String sheetName = workbook.getSheetName(i);
            int tableFirstRow = 0;
            int tableLastRow = -1;
            int emptyRows = 0;
            int tableCount = 1;
            for (int j=0; j<=sheetLastRow+1; j++) {
            	HSSFRow row = sheet.getRow(j);
                if (HSSFFunctions.isEmptyRow(row) && j<sheetLastRow+1)
                    emptyRows++;
                else {
	                if (emptyRows>emptyRowBoundary || j==sheetLastRow+1) {
	                    if (tableFirstRow<tableLastRow) {
	                        String[][] newTableData = convertTable(workbook, i, tableFirstRow, tableLastRow);
	                        DecisionTable newTable = new DecisionTable(newTableData);
	                        newTable.setSheetName(sheetName);
	                        newTable.setTableNumber(tableCount);
	                        tables.add(newTable);
	                        tableCount++;
	                    }
	                    tableFirstRow = j;
	                    tableLastRow = j;
	                }
	                else
	                    tableLastRow = tableLastRow+emptyRows+1;
                    emptyRows = 0;
                }
            }
        }
        return tables;
    }
    
   /**
    * Wandelt eine vorgegebene Tabelle in ein zweidimensonales Array um
    * @param sheetNo Index des Excel-Sheets auf welchem sich die Tabelle befindet
    * @param firstRow Nummer der ersten Tabellen-Zeile (beginnnend mit 0)
    * @param lastRow Nummer der letzten Tabellen-Zeile
    * @return Zweidimensionales Array, welches die alle Zellen der Tabelle enthält
    */
   private static String[][] convertTable(HSSFWorkbook workbook, int sheetNo, int firstRow, int lastRow) {
       HSSFSheet sheet = workbook.getSheetAt(sheetNo);
       int height = (lastRow-firstRow)+1;
	   int maxWidth = 0;
       for (int i=firstRow; i<=lastRow; i++) {
   		   HSSFRow row = sheet.getRow(i);
   		   if (row!=null) {
   			   int width = row.getLastCellNum();
   		       if (width>maxWidth)
    			   maxWidth=width;
   		   }
       }
       String[][] tableData = new String[height][maxWidth+1]; 
       for (int i=0; i<height; i++) {
           for (int j=0; j<maxWidth+1; j++) {
               tableData[i][j] = new String();
           }
       }
       for (int i=firstRow; i<=lastRow; i++) {
           HSSFRow row = sheet.getRow(i);
           for (int j=0; row!=null && j<=row.getLastCellNum(); j++) {
               HSSFCell cell = row.getCell((short)j);
               String value = "";
               try {
                   value = cell.getStringCellValue();
               }
               catch (Exception e) {
                   try {
                       value = Double.toString(cell.getNumericCellValue()).trim();
                   }
                   catch (Exception f) {}
               }
               value = value.trim();
               tableData[i-firstRow][j] = value;
           }
       }
       tableData = cutEmptyRows(tableData);
       return tableData;
   }

	/**
	 * Cuts empty rows at the end of a table
	 * @param tableData data of the table to be cut
	 * @return new table data that doesn't contain empty rows at end of table
	 */
	private static String[][] cutEmptyRows(String[][] tableData) {
		// Abschneiden leerer Spalten.
		   // Dies ist nötig, weil manchmal auch leere Zellen als nichtleer erkannt werden
		   boolean lastColumnEmpty = true;
		   while (lastColumnEmpty) {
		       for (int i=0; i<tableData.length; i++) {
		           if (!tableData[i][tableData[i].length-1].equals("")) {
		               lastColumnEmpty = false;
		               continue;
		           }

		       }
		       if (lastColumnEmpty) {
		           String[][] newContent = new String[tableData.length][tableData[0].length-1]; 
		           for (int i=0; i<tableData.length; i++) {
		               System.arraycopy(tableData[i], 0, newContent[i], 0, tableData[i].length-1);
		           }
		           tableData = newContent;
		       }
		   }
		return tableData;
	}
}
