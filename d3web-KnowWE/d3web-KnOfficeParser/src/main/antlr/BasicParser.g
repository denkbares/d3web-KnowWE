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

parser grammar BasicParser; 

options {
	language = Java;
} 
@members{

  private ParserErrorHandler eh;
  
  public void setEH(ParserErrorHandler eh) {
    this.eh=eh;
  }
  
  private String delQuotes(String s) {
    s=s.substring(1, s.length()-1);
    s=s.replace("\\\"", "\"");
    return s;
  }
  
  private Double parseDouble(String s) {
    if (s==null||s.equals("")) s="0";
    s=s.replace(',', '.');
    Double d=0.0;
    try {
      d = Double.parseDouble(s);
    } catch (NumberFormatException e) {
      
    }
      return d;
  }
  
  @Override
  public void reportError(RecognitionException re) {
    if (eh!=null) {
      eh.parsererror(re);
    } else {
      super.reportError(re);
    }
  }
}

name returns [String value]
: String* (ID|INT) (ID|INT|String)* {$value=$text;}
| String {$value=delQuotes($String.text);};

type returns [String value]
: SBO ID SBC {$value=$ID.text;};

eq  : EQ|LE|L|GE|G;
eqncalc : eq|PLUS EQ|MINUS EQ;

d3double returns [Double value]
: MINUS? INT ((COMMA|DOT) INT)? {$value=parseDouble($text);};

nameOrDouble returns [String value]
:(MINUS INT | INT DOT | INT COMMA)=> d3double {$value=$d3double.value.toString();}| name {$value=$name.value;} | EX {$value=$text;} ;
