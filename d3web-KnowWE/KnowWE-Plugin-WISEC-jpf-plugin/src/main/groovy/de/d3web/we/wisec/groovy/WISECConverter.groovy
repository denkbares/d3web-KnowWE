#!/bin/groovy
// groovy -classpath jxl.jar WISECConverter.groovy 

package de.d3web.we.wisec.groovy

import jxl.*;

///////////////////// MAIN
class WISECConverter {
	def printer       = new WikiPrinter()
	def workspace     = '/Users/sebastian/Projekte/Temp/KnowWE/WISEC/'
	def filename  = 'WISEC.xls'
	public static wikiworkspace = '/Users/sebastian/Projekte/Temp/KnowWE/wiki/wiseccontent/'
	def numberOfLists = 0
	def stats = new ConversionStatistics()

	// some settings for the generation
	public static FILE_PRAEFIX = "WI_"
	public static MAX_NUMBER_OF_LISTS_TO_CONVERT = 1000
	public static MAX_NO_SUBSTANCES = 100
	public static ASKS_ANNOTATION = false

	
	public static void main(String[] args) {
		def converter = new WISECConverter()
		converter.convertAll()
	}

	def convertAll() {
		Workbook workbook = Workbook.getWorkbook(new File(workspace+filename)); 
		stats = new ConversionStatistics()
		convertUpperLists(workbook);
		convertLists(workbook);
		printStatitistics()
	}
	
	def printStatitistics() {
		new StatisticsPrinter().print(wikiworkspace+FILE_PRAEFIX+"WISEC.txt", stats)
	}

	def convertUpperLists(workbook) {
		def sheet = workbook.getSheet("ListenListe")
		
		// get attribute names
		def attributes = [ : ]
		for (col in 1..sheet.getColumns()-1) {
			attributes[col] = sheet.getCell(col, 1).getContents()
		}
		
		def ulp = new UpperListPrinter()
		for (row in 2..sheet.getRows()-1) {
			def institution = sheet.getCell(2,row).getContents()
			if (institution != 'automatisch generierte Liste' && 
				institution != 'UBA-intern: nutzerdefinierte Liste') {
				ulp.print(attributes, toStrArray(sheet.getRow(row)), stats)
			}
		}
	}
	
	def convertLists(workbook) {
		numberOfLists = 0
		def listen = initializeLists(workbook)
		listen = updateListsWithSubstances(listen, workbook)
		
		println "No of list with not printed substances: ${printer.NON_PRINTED_SUBSTANCES}"
		listen
	}

	//////////////////////////////////////////////////
	def initializeLists(workbook) {
		def listen = [:]
		def sheet = workbook.getSheet("Datentabellen")
	
		for (row in 2..sheet.getRows()-1) {
			def tablename = tidy(sheet.getCell(4,row).getContents().toString())
		
			if (isADataList(tablename) && (numberOfLists < MAX_NUMBER_OF_LISTS_TO_CONVERT)) {
				numberOfLists++;
				def list = new WISECList()

				def lfdNr = removeDecimals(sheet.getCell(0,row).getContents())
				list.upperList = stats.upperLists[lfdNr]

				
				list.name = tablename
				for (col in 7..20) {
					def value = sheet.getCell(col,row).getContents()
					def attri = sheet.getCell(col,1).getContents()
					list.listData[attri] = value
				}
				listen[tablename] = list
			}
		}
	
		return listen
	}

	def updateListsWithSubstances(listen, workbook) {
		workbook.getSheets().each { sheet ->
			def name = tidy(sheet.getCell(0,0).getContents()) //sheet.getName())
			if (isADataList(name)) {
				def list = listen[name]
				if (list) {
					list = convertSubstanceList(sheet, list)
					listen[name] = list
					stats.convertedLists[name] = name
					printer.print(wikiworkspace+FILE_PRAEFIX+name+".txt", list, stats)	
				} else {
					println "Error: Did not find ${name}."
				}
			}
		}
		listen
	}

	def tidy(name) {
		return name.replaceAll("&","_AND_")
			.replaceAll("ä","ae")
			.replaceAll("ö","oe")
			.replaceAll("ü","ue")
			.replaceAll("ß","ss")
			.replaceAll("\n", " ")
	}

	def isADataList(name) {
		(name =~ /^[0-9]+.*/)
	}


	def convertSubstanceList(sheet, list) {
		// 1) NOT NEEDED ANYMORE-Determine list name
		// list.name = sheet.getCell(0,0).getContents()
	
		// 2) determine header names
		def headerNames = []
		def maxCols = sheet.getColumns()-1
		for (col in 0..maxCols)
			headerNames << sheet.getCell(col,1).getContents()
		list.attributes = headerNames
	
		// 3) Create a new WISEC list
		for (row in 2..sheet.getRows()-1) {
			def substance = new Substance()
			for (col in 0..maxCols) {
				def val = sheet.getCell(col,row).getContents()
				def att = list.attributes[col]
				substance.attributes[att] = tidy(val)
			}
			list.substances << substance
		}
		return list
	}
	
	def toStrArray(cellArray) {
		def result = []
		cellArray.each { cell ->
			result << cell.getContents()
		}
		result
	}

	public static removeDecimals(string) {
		if (string.endsWith('.0'))
			string = string.substring(0,string.size()-2)
		return string
	}

	
}


