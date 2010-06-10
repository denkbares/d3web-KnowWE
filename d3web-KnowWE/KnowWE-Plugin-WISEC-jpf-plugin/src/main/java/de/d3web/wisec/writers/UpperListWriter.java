package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SubstanceList;
import de.d3web.wisec.model.UpperList;
import de.d3web.wisec.model.WISECModel;

public class UpperListWriter extends WISECWriter {

	private static String filePraefix = WISECExcelConverter.FILE_PRAEFIX+"UL_";
	private static Map<String, String> listName2fileName = new HashMap<String, String>();

	public UpperListWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		int counter = 0;
		for (UpperList list : model.getUpperLists()) {
			counter++;
			list.filename = filePraefix +counter;
			listName2fileName.put(list.getName(), list.filename);
			Writer writer = ConverterUtils.createWriter(this.outputDirectory+list.filename+".txt");
			write(list,writer);
			writer.close();
		}
	}

	private void write(UpperList list, Writer w) throws IOException {
		w.write("!!! " + list.getName() + "\n\n");
		
		w.write("!! Attributes \n\n");
		for (String attribute: list.getAttributes()) {
			String value = list.get(attribute);
			if (value != null && value.length() >0) { 
				w.write(" __" + attribute + "__ : " + value + " \n\n");
			}
		}
		
		if (!list.getChildren().isEmpty()) {
			w.write("!! Connected substance lists\n\n");
			for (SubstanceList substancelist : list.getChildren()) {
				w.write("* "+SubstanceListWriter.asWikiMarkup(substancelist)+"\n");
			}
		}
	}


	public static String getWikiFilename(String realListName) {
		return listName2fileName.get(realListName);
	}
}
