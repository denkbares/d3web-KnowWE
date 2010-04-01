package de.d3web.we.wisec.groovy
class StatisticsPrinter {
	def print(filename, stats) {
		def praefix = WISECConverter.FILE_PRAEFIX
		def w = WikiPrinter.openFile(filename)
		
		w.write "!!! WISEC Overview\n\n"
		
		w.write "||Number of Substances | ${stats.definedSubstances.size()}\n"
		w.write "||Number of Lists | ${stats.convertedLists.keySet().size()}\n"
		w.write "||Number of Upper Lists | ${stats.convertedUpperLists.keySet().size()}\n"
		
		w.write "\n"
		w.write "!! Upper Lists \n"
		
		stats.convertedUpperLists.each { key, value -> 
			w.write "* [${key}|${value}]\n"
		}
		
		w.write "\n"
		w.write "!! Substance Lists \n"
		stats.convertedLists.each { key, value -> 
			w.write "* [${key}|${praefix}${value}]\n"
		}
		w.close()
	}
}