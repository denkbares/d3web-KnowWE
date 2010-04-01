package de.d3web.we.wisec.groovy
class UpperListPrinter {
	
	def categories = ["P", "B", "T", "R", "E", "C", "X", "M"]
	
	def print(attributes, row, stats) {
		def lfdNr = removeDecimals(row[0])
		 
		def filename = "${WISECConverter.FILE_PRAEFIX}UPPERLIST${lfdNr}"
		def stream = WikiPrinter.openFile(WISECConverter.wikiworkspace+filename+".txt")
		def upperlistname = row[1]
		out stream, "!!! ${upperlistname}\n\n"
		
		def uplistShortName = generateShortName(lfdNr, upperlistname)
		stats.upperLists[lfdNr] = uplistShortName
		// [1 <=> 001_WRRL w:Risk_related:: 1]
		// out stream, "Shortname: [${uplistShortName} <=> ${uplistShortName} type:: w:UpperList]\n"
	
		// First print the non-category attributes
		for (col in 2..row.size()-1) {
			def attr = attributes[col]
			if (!categories.contains(attr)) {
				def val  = WikiPrinter.wikitidy(row[col])
				out stream, "| ${attr} | ${val}"
				//out stream, "[${val} <=> ${uplistShortName} w:${attr}:: ${val}]\n"
			}
		}
		
		// Second: Print the categories
		out stream, "\n\n!! Categorization\n\n"
		out stream, "%%UpperListCategorization\n\n"
		for (col in 2..row.size()-1) {
			def attr = attributes[col]
			if (categories.contains(attr)) {
				def val  = WikiPrinter.wikitidy(row[col])
				if (val) {
					out stream, "| ${attr} | ${val}"
					//[${val} <=> ${uplistShortName} w:${attr}:: ${val}]"					
				} else {
					out stream, "| ${attr} | ${val} "
				}
				out stream, "\n"
			}
		}
		
		out stream, "-\n"

		out stream, "@ListID: ${uplistShortName}\n"
		out stream, "%\n"
		
		
		stream.close()
		stats.convertedUpperLists[upperlistname] = filename;
	}
	
	def generateShortName(lfdNr, upperlistname) {
		def name = removeEmptyStrings(upperlistname)
		return "${lfdNr.padLeft(3,'0')}${name}"
	}
	
	def removeEmptyStrings(str) {
		return str.replaceAll("[^0-9a-zA-z]","")
	}
	
	def out (stream, string) {
		stream.write string
	}
	
	def removeDecimals(string) {
		if (string.endsWith('.0'))
			string = string.substring(0,string.size()-2)
		return string
	}
	
}