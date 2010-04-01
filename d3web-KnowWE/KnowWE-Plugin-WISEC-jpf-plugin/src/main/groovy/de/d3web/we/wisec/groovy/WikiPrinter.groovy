package de.d3web.we.wisec.groovy

//////////////////////////////////////////////////
class WikiPrinter {
	def NON_PRINTED_SUBSTANCES = 0
	
	
	def print(name, wisecList, stats) {
		def outfile = openFile(name)
		print_pr(name, wisecList, outfile, stats)
		outfile.close()
	}
	
	def out(outfile, string) {
		outfile.write string
	}
	
	def print_pr(name, wisecList, outfile, stats) {
		out(outfile, "!!! ${wisecList.name} \n\n")
		
		// PRINT GENERAL LIST DATA
		out outfile, "!! Characteristics\n"
		out outfile, "\n%%ListCriteria\n\n"
		wisecList.listData.each { attribute, value ->
			out(outfile, "| ${attribute} | ")
			if (value) {
				out outfile, "${value} "
				//out outfile, "[${value} <=> ${wisecList.name} w:${attribute}:: ${value}] "
			}
			// [1 <=> 001_WRRL w:Risk_related:: 1]
			out(outfile, "\n")
		}
		
		out outfile, "\n-\n"
		out outfile, "@ListID: ${wisecList.name}\n"

		def str = "-"
		if (wisecList.upperList)
			out outfile, "@UpperlistID: ${wisecList.upperList}\n\n"
			//str = "[${wisecList.upperList} <=> ${wisecList.name} w:onUpperList:: ${wisecList.upperList}]"
		out outfile, "%\n\n"
		
		
		out(outfile, "!! Substances\n")
		
		if (wisecList.substances.size() > WISECConverter.MAX_NO_SUBSTANCES) {
			println "To many substances: ${wisecList.substances.size()}"
			out(outfile, "To many substances: ${wisecList.substances.size()}\n")
			NON_PRINTED_SUBSTANCES++;
		} else {
			// PRINT SUBSTANCE DATA
			def sgncol = -1
			out outfile, "\n%%ListSubstances\n"
			if (wisecList.attributes) {
				for (j in 0..wisecList.attributes.size()-1) {
					out outfile, "|| ${wisecList.attributes[j]} "
					if (wisecList.attributes[j] == "SGN")
						sgncol = j
				}
				out(outfile, "\n")
				for (i in 0..wisecList.substances.size()-1) {
					def substance = wisecList.substances[i]
					for (j in 0..wisecList.attributes.size()-1) {
						def attribute = wisecList.attributes[j]
						def value     = substance.attributes[attribute]
						if (j != sgncol) {
							outfile.write "| ${wikitidy(value)} "
						} 
						// ok, print the SGN name as string into to cell 
						else {
							value = wikitidy(value)
							out outfile, "| "
							if (WISECConverter.ASKS_ANNOTATION) {
								// {{word phrase <=> asks:: QuestionText}}
								out outfile, "{{ ${value} <=> asks:: ${value} }}"
							}
							else
								out outfile, "${value} "
							stats.definedSubstances << value
						}
					}
					out outfile, "\n"
				}
			}
		}
		out outfile, "-\n"

		out outfile, "@ListID: ${wisecList.name}\n"
		out outfile, "%\n"
		out outfile, "\n"
	}
	
	static public def wikitidy(string) {
		string = string.replaceAll('\\[','(')
		string = string.replaceAll('\n', '\\\\') 
		return string
	}
	
	def printOverview(listen, name) {
		def outfile = openFile(name)
		out(outfile, "!!! Overview \n\n")
		
		def sortenListNames = listen.keySet().sort()
		
		out(outfile, "|| Name || Substances || Classes \n")
		sortenListNames.each { listname ->
			def subSize = listen[listname].substances.size()
			def verbalizedClasses = verbalizeClasses(listen[listname])
			out(outfile, "| [${listname}|${WISECConverter.FILE_PRAEFIX}${listname}] | ${subSize} | ${verbalizedClasses} \n")
		}
		outfile.close()
	}
	
	def verbalizeClasses(list) {
		def verb = ""
		list.listData.each{ att, val ->
			if (val) {
				verb += "${att}=${val} "
			}
		}
		return verb
	}
	
	static public def openFile(name) {
		def f = new File(name);
		if (f.exists())
			f.delete()
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f), "UTF8");
		BufferedWriter bw = new BufferedWriter(osw);
		return bw
	}
	
}
