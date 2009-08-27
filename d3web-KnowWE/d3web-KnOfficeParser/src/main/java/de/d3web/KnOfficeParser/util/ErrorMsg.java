package de.d3web.KnOfficeParser.util;

import org.antlr.runtime.Token;

public class ErrorMsg {
	public static String getCharString(int i) {
		String s;
		if (i==Token.EOF) {
			s="<EndOfFile>";
		} else if (i=='\n') {
			s="<Newline>";
		} else if (i=='\r') {
			s="<Return>";
		} else if (i=='\t') {
			s="<Tab>";
		} else {
			s=""+(char)i;
		}
		return s;
	}
	
	public static String getTokenString(Token t) {
		String s=t.getText();
		if (s==null) {
			if (t.getType()==Token.EOF) {
				s="<Dateiende>";
			}
			else {
				s="<Typ: "+t.getType()+">";
			}
		}
		s = s.replaceAll("\n","<Newline>");
		s = s.replaceAll("\r","<Return>");
		s = s.replaceAll("\t","<Tab>");
		return s;
	}
}
