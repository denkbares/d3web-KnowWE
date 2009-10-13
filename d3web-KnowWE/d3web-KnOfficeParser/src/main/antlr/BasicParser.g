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
