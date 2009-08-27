package de.d3web.textParser.xclPatternParser;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.apache.tools.ant.util.ReaderInputStream;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.report.Report;
import de.d3web.report.Message;

/**
 * @author kazamatzuri
 *	kapselt den von ANTLR generierten Parser zu etwas benutzbarem zusammen.
 */
public class XCLParserHelper {
	

/**
 * @param kbase Knowledgebase auf der das XCLModel gebaut werden soll
 * @param input Inputstream mit der zu parsenden Eingabe
 * @return das resultierende XCLModel
 */
private static Report getXCLModel(KnowledgeBase kbase, InputStream input){	
	
	
	Report report=null;
	ANTLRInputStream istream;
	try {
		istream = new ANTLRInputStream(input);
		xclLexer lexer=new xclLexer(istream);
		CommonTokenStream tokens=new CommonTokenStream(lexer);
		xclParser parser=new xclParser(tokens);
		report = parser.xclrule(kbase);

	} catch (Exception e) {
		if (report==null){report=new Report();}
		report.error(new Message(e.toString()));
	} 

	return report;
}

public static String[] splitToSingleLists(String multipleLists) {
    String splitSign = "}";
    int end = multipleLists.lastIndexOf(splitSign) + splitSign.length();
    multipleLists = multipleLists.substring(0, end);
    String [] lists = multipleLists.split(splitSign);
    for (int i = 0; i < lists.length; i++) {
        lists[i] = cleanUp(lists[i]);
        lists[i] += splitSign;
    }
    return lists;
       }


	public static char NEWLINE = '\n';
	public static char NEWLINE_R = '\r';
	public static char SPACE = ' ';

	private static String cleanUp(String xclist) {
		if (xclist != null && xclist.length() > 0) {
			while ((xclist.charAt(0) == NEWLINE || xclist.charAt(0) == SPACE || xclist.charAt(0) == NEWLINE_R)) {
				xclist = xclist.substring(1);
				if (xclist.length() == 0)
					return "";
			}
			while (xclist.charAt(xclist.length() - 1) == NEWLINE
					|| xclist.charAt(xclist.length() - 1) == SPACE || xclist.charAt(xclist.length() - 1) == NEWLINE_R) {
				xclist = xclist.substring(0, xclist.length() - 1);
			}
			if (xclist.length() == 0)
				return "";
		}
		return xclist;
}

public static Report getXCLModel(KnowledgeBase kbase,Reader reader){
	ReaderInputStream is=new ReaderInputStream(reader);
    return getXCLModel(kbase,is);
}
/** 
 * wrapper um Stringbuffer als eingabe verwenden zu koennen
 * @param kbase Knowledgebase auf der das XCLModel gebaut werden soll
 * @param buffy StringBuffer mit der zu parsenden Eingabe
 * @return das resultierende XCLModel
 * 
 * @delete comment: braucht doch kein Mensch...  ;-)
 *   comment: doch, ... mein junit test dafuer -- kaza 
 */
public static Report getXCLModel(KnowledgeBase kbase,StringBuffer buffy){
    //StringBufferInputStreamb=new StringBufferInputStream(buffy.toString());
	// this is deprecated ... you're welcome to find a nicer alternative. 
	// especially having the potential encoding-woes in mind

	
	//TODO find better solution

	byte[] bArray = buffy.toString().getBytes();
    //System.out.println(buffy.toString());
	ByteArrayInputStream bais = new ByteArrayInputStream(bArray);

    return getXCLModel(kbase,bais);
}

}
