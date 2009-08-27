package de.d3web.textParser.cocor.decisionTreeParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.BitSet;
import java.util.LinkedList;

class Token {
	public int kind;    // token kind
	public int pos;     // token position in the source text (starting at 0)
	public int col;     // token column (starting at 0)
	public int line;    // token line (starting at 1)
	public String val;  // token value
	public Token next;  // ML 2005-03-11 Peek tokens are kept in linked list
}

class Buffer {
	public static final char EOF = (char) 256;
	static final int MAX_BUFFER_LENGTH = 64 * 1024; // 64KB
	byte[] buf;   // input buffer
	int bufStart; // position of first byte in buffer relative to input stream
	int bufLen;   // length of buffer
	int fileLen;  // length of input stream
	int pos;      // current position in buffer
	RandomAccessFile file; // input stream (seekable)

	public Buffer(InputStream s) {
		try {
			fileLen = bufLen = s.available();
			buf = new byte[bufLen];
			s.read(buf, 0, bufLen);
			pos = 0;
			bufStart = 0;
		} catch (IOException e){
			System.out.println("--- error on filling the buffer ");
			System.exit(1);
		}
	}

	public Buffer(String fileName) {
		try {
			file = new RandomAccessFile(fileName, "r");
			fileLen = bufLen = (int) file.length();
			if (bufLen > MAX_BUFFER_LENGTH) bufLen = MAX_BUFFER_LENGTH;
			buf = new byte[bufLen];
			bufStart = Integer.MAX_VALUE; // nothing in buffer so far
			setPos(0); // setup buffer to position 0 (start)
			if (bufLen == fileLen) Close();
		} catch (IOException e) {
			System.out.println("--- could not open file " + fileName);
			System.exit(1);
		}
	}
	
		public Buffer(Reader s) {

		byte[] bytes = readBytes(s);
		fileLen = bufLen = bytes.length;
		buf = bytes;
		pos = 0;
		bufStart = 0;

	}
	
	private byte[] readBytes(Reader r) {
		int zeichen = 0;
		java.util.List bytes = new LinkedList();
		while (true) {

			try {
				zeichen = r.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (zeichen == -1)
				break;
			Byte b = new Byte((byte) zeichen);
			bytes.add(b);
		}

		Object[] o = bytes.toArray();
		byte[] byteArray = new byte[o.length];

		for (int i = 0; i < o.length; i++) {
			byteArray[i] = ((Byte) o[i]).byteValue();

		}
		return byteArray;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		Close();
	}

	void Close() {
		if (file != null) {
			try {
				file.close();
				file = null;
			} catch (IOException e) {
				e.printStackTrace(); System.exit(1);
			}
		}
	}

	public int Read() {
		if (pos < bufLen) {
			return buf[pos++] & 0xff;  // mask out sign bits
		} else if (getPos() < fileLen) {
			setPos(getPos());         // shift buffer start to pos
			return buf[pos++] & 0xff; // mask out sign bits
		} else {
			return EOF;
		}
	}

	public int Peek() {
		if (pos < bufLen) {
			return buf[pos] & 0xff;  // mask out sign bits
		} else if (getPos() < fileLen) {
			setPos(getPos());       // shift buffer start to pos
			return buf[pos] & 0xff; // mask out sign bits
		} else {
			return EOF;
		}
	}

	public String GetString(int beg, int end) {
	    int len = end - beg;
	    char[] buf = new char[len];
	    int oldPos = getPos();
	    setPos(beg);
	    for (int i = 0; i < len; ++i) buf[i] = (char) Read();
	    setPos(oldPos);
	    return new String(buf);
	}

	public int getPos() {
		return pos + bufStart;
	}

	public void setPos(int value) {
		if (value < 0) value = 0;
		else if (value > fileLen) value = fileLen;
		if (value >= bufStart && value < bufStart + bufLen) { // already in buffer
			pos = value - bufStart;
		} else if (file != null) { // must be swapped in
			try {
				file.seek(value);
				bufLen = file.read(buf);
				bufStart = value; pos = 0;
			} catch(IOException e) {
				e.printStackTrace(); System.exit(1);
			}
		} else {
			pos = fileLen - bufStart; // make getPos() return fileLen
		}
	}

}

public class Scanner {
	static final char EOL = '\n';
	static final int  eofSym = 0;
	static final int charSetSize = 256;
	static final int maxT = 45;
	static final int noSym = 45;
	short[] start = {
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,  8, 16, 16,  8, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	  0, 16,  1, 12, 16, 16, 16, 16,  9, 10, 16, 16, 16, 17, 16, 18,
	  4,  7,  7,  7,  7,  7,  7,  7,  7,  7, 16, 16, 59, 51, 60, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 58, 16, 11, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 25, 16, 26, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16,
	  -1};


	// COCO begin add
	private String filename = new String();
	// COCO end add

	public Buffer buffer; // scanner buffer

	Token t;         // current token
	char ch;         // current input character
	int pos;         // column number of current character
	int line;        // line number of current character
	int lineStart;   // start position of current line
	int oldEols;     // EOLs that appeared in a comment;
	BitSet ignore;   // set of characters to be ignored by the scanner

	Token tokens;    // list of tokens already peeked (first token is a dummy)
	Token pt;        // current peek token
	
	char[] tokenText = new char[16]; // token text used in NextToken(), dynamically enlarged
	
	public Scanner (String fileName) {
		
		// COCO begin add
		this.filename = fileName;
		// COCO end add

		buffer = new Buffer(fileName);
		Init();
	}
	
	// COCO begin add
	public String getFilename() {
		return this.filename;
	}
	// COCO end add
	
	public Scanner(InputStream s) {
		buffer = new Buffer(s);
		Init();
	}
	
	public Scanner(Reader r) {
		buffer = new Buffer(r);
		Init();
	}
	
	void Init () {
		pos = -1; line = 1; lineStart = 0;
		oldEols = 0;
		NextCh();
		ignore = new BitSet(charSetSize+1);
		ignore.set(' '); // blanks are always white space
		ignore.set(9); 
		pt = tokens = new Token();  // first token is a dummy
	}
	
	void NextCh() {
		if (oldEols > 0) { ch = EOL; oldEols--; } 
		else {
			ch = (char)buffer.Read(); pos++;
			// replace isolated '\r' by '\n' in order to make
			// eol handling uniform across Windows, Unix and Mac
			if (ch == '\r' && buffer.Peek() != '\n') ch = EOL;
			if (ch == EOL) { line++; lineStart = pos + 1; }
		}

	}
	

	
	void CheckLiteral() {
		String lit = t.val;
		if (lit.compareTo("N1") == 0) t.kind = 24;
		else if (lit.compareTo("N2") == 0) t.kind = 25;
		else if (lit.compareTo("N3") == 0) t.kind = 26;
		else if (lit.compareTo("N4") == 0) t.kind = 27;
		else if (lit.compareTo("N5") == 0) t.kind = 28;
		else if (lit.compareTo("N5+") == 0) t.kind = 29;
		else if (lit.compareTo("N6") == 0) t.kind = 30;
		else if (lit.compareTo("N7") == 0) t.kind = 31;
		else if (lit.compareTo("P1") == 0) t.kind = 32;
		else if (lit.compareTo("P2") == 0) t.kind = 33;
		else if (lit.compareTo("P3") == 0) t.kind = 34;
		else if (lit.compareTo("P4") == 0) t.kind = 35;
		else if (lit.compareTo("P5") == 0) t.kind = 36;
		else if (lit.compareTo("P5+") == 0) t.kind = 37;
		else if (lit.compareTo("P6") == 0) t.kind = 38;
		else if (lit.compareTo("P7") == 0) t.kind = 39;
		else if (lit.compareTo("+") == 0) t.kind = 40;
		else if (lit.compareTo("++") == 0) t.kind = 41;
		else if (lit.compareTo("+++") == 0) t.kind = 42;
	}

	Token NextToken() {
		while (ignore.get(ch)) NextCh();

		t = new Token();
		t.pos = pos; t.col = pos - lineStart + 1; t.line = line; 
		int state = start[ch];
		char[] tval = tokenText; // local variables are more efficient
		int tlen = 0;
		tval[tlen++] = ch; NextCh();
		
		boolean done = false;
		while (!done) {
			if (tlen >= tval.length) {
				char[] newBuf = new char[2 * tval.length];
				System.arraycopy(tval, 0, newBuf, 0, tval.length);
				tokenText = tval = newBuf;
			}
			switch (state) {
				case -1: { t.kind = eofSym; done = true; break; } // NextCh already done 
				case 0: { t.kind = noSym; done = true; break; }   // NextCh already done
				case 1:
					if (!(ch == '"') && ch != Buffer.EOF) {tval[tlen++] = ch; NextCh(); state = 2; break;}
					else {t.kind = noSym; done = true; break;}
				case 2:
					if (!(ch == '"') && ch != Buffer.EOF) {tval[tlen++] = ch; NextCh(); state = 2; break;}
					else if (ch == '"') {tval[tlen++] = ch; NextCh(); state = 3; break;}
					else {t.kind = noSym; done = true; break;}
				case 3:
					{t.kind = 1; t.val = new String(tval, 0, tlen); CheckLiteral(); return t;}
				case 4:
					if ((ch == ',' || ch == '.')) {tval[tlen++] = ch; NextCh(); state = 5; break;}
					else {t.kind = 2; done = true; break;}
				case 5:
					if ((ch >= '0' && ch <= '9')) {tval[tlen++] = ch; NextCh(); state = 6; break;}
					else {t.kind = noSym; done = true; break;}
				case 6:
					if ((ch >= '0' && ch <= '9')) {tval[tlen++] = ch; NextCh(); state = 6; break;}
					else {t.kind = 2; done = true; break;}
				case 7:
					if ((ch >= '0' && ch <= '9')) {tval[tlen++] = ch; NextCh(); state = 7; break;}
					else if ((ch == ',' || ch == '.')) {tval[tlen++] = ch; NextCh(); state = 5; break;}
					else {t.kind = 2; done = true; break;}
				case 8:
					{t.kind = 3; done = true; break;}
				case 9:
					{t.kind = 5; done = true; break;}
				case 10:
					{t.kind = 6; done = true; break;}
				case 11:
					{t.kind = 8; done = true; break;}
				case 12:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= '!' || ch >= '$' && ch <= 39 || ch >= '*' && ch <= ',' || ch >= '.' && ch <= ';' || ch >= '?' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 'z' || ch == '|' || ch >= '~' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 13; break;}
					else {t.kind = noSym; done = true; break;}
				case 13:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= '!' || ch >= '$' && ch <= 39 || ch >= '*' && ch <= ',' || ch >= '.' && ch <= ';' || ch >= '?' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 'z' || ch == '|' || ch >= '~' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 13; break;}
					else {t.kind = 9; done = true; break;}
				case 14:
					if ((ch == 10 || ch == 13)) {tval[tlen++] = ch; NextCh(); state = 15; break;}
					else if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 14; break;}
					else {t.kind = noSym; done = true; break;}
				case 15:
					if ((ch == 10 || ch == 13)) {tval[tlen++] = ch; NextCh(); state = 15; break;}
					else {t.kind = 46; done = true; break;}
				case 16:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= '!' || ch >= '$' && ch <= 39 || ch >= '*' && ch <= ';' || ch >= '?' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 'z' || ch == '|' || ch >= '~' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 19; break;}
					else {t.kind = 1; t.val = new String(tval, 0, tlen); CheckLiteral(); return t;}
				case 17:
					if ((ch == '0')) {tval[tlen++] = ch; NextCh(); state = 4; break;}
					else if ((ch >= '1' && ch <= '9')) {tval[tlen++] = ch; NextCh(); state = 7; break;}
					else {t.kind = 4; done = true; break;}
				case 18:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= '!' || ch >= '$' && ch <= 39 || ch >= '*' && ch <= '.' || ch >= '0' && ch <= ';' || ch >= '?' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 'z' || ch == '|' || ch >= '~' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 19; break;}
					else if (ch == '/') {tval[tlen++] = ch; NextCh(); state = 20; break;}
					else {t.kind = 1; t.val = new String(tval, 0, tlen); CheckLiteral(); return t;}
				case 19:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= '!' || ch >= '$' && ch <= 39 || ch >= '*' && ch <= ';' || ch >= '?' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 'z' || ch == '|' || ch >= '~' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 19; break;}
					else {t.kind = 1; t.val = new String(tval, 0, tlen); CheckLiteral(); return t;}
				case 20:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= '!' || ch >= '$' && ch <= 39 || ch >= '*' && ch <= ';' || ch >= '?' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 'z' || ch == '|' || ch >= '~' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 20; break;}
					else if ((ch == 10 || ch == 13)) {tval[tlen++] = ch; NextCh(); state = 15; break;}
					else if ((ch >= '"' && ch <= '#' || ch >= '(' && ch <= ')' || ch >= '<' && ch <= '>' || ch == '[' || ch == ']' || ch == '{' || ch == '}')) {tval[tlen++] = ch; NextCh(); state = 14; break;}
					else {t.kind = 1; t.val = new String(tval, 0, tlen); CheckLiteral(); return t;}
				case 21:
					if (ch == 'u') {tval[tlen++] = ch; NextCh(); state = 22; break;}
					else {t.kind = noSym; done = true; break;}
				case 22:
					if (ch == 'm') {tval[tlen++] = ch; NextCh(); state = 23; break;}
					else {t.kind = noSym; done = true; break;}
				case 23:
					if (ch == ']') {tval[tlen++] = ch; NextCh(); state = 24; break;}
					else {t.kind = noSym; done = true; break;}
				case 24:
					{t.kind = 10; done = true; break;}
				case 25:
					{t.kind = 11; done = true; break;}
				case 26:
					{t.kind = 12; done = true; break;}
				case 27:
					if (ch == 'c') {tval[tlen++] = ch; NextCh(); state = 28; break;}
					else {t.kind = noSym; done = true; break;}
				case 28:
					if (ch == ']') {tval[tlen++] = ch; NextCh(); state = 29; break;}
					else {t.kind = noSym; done = true; break;}
				case 29:
					{t.kind = 13; done = true; break;}
				case 30:
					if (ch == 'c') {tval[tlen++] = ch; NextCh(); state = 31; break;}
					else {t.kind = noSym; done = true; break;}
				case 31:
					if (ch == ']') {tval[tlen++] = ch; NextCh(); state = 32; break;}
					else {t.kind = noSym; done = true; break;}
				case 32:
					{t.kind = 14; done = true; break;}
				case 33:
					if (ch == 'a') {tval[tlen++] = ch; NextCh(); state = 34; break;}
					else {t.kind = noSym; done = true; break;}
				case 34:
					if (ch == 't') {tval[tlen++] = ch; NextCh(); state = 35; break;}
					else {t.kind = noSym; done = true; break;}
				case 35:
					if (ch == 'e') {tval[tlen++] = ch; NextCh(); state = 36; break;}
					else {t.kind = noSym; done = true; break;}
				case 36:
					if (ch == ']') {tval[tlen++] = ch; NextCh(); state = 37; break;}
					else {t.kind = noSym; done = true; break;}
				case 37:
					{t.kind = 15; done = true; break;}
				case 38:
					if (ch == 'e') {tval[tlen++] = ch; NextCh(); state = 39; break;}
					else {t.kind = noSym; done = true; break;}
				case 39:
					if (ch == 'x') {tval[tlen++] = ch; NextCh(); state = 40; break;}
					else {t.kind = noSym; done = true; break;}
				case 40:
					if (ch == 't') {tval[tlen++] = ch; NextCh(); state = 41; break;}
					else {t.kind = noSym; done = true; break;}
				case 41:
					if (ch == ']') {tval[tlen++] = ch; NextCh(); state = 42; break;}
					else {t.kind = noSym; done = true; break;}
				case 42:
					{t.kind = 16; done = true; break;}
				case 43:
					if (ch == 'n') {tval[tlen++] = ch; NextCh(); state = 44; break;}
					else {t.kind = noSym; done = true; break;}
				case 44:
					if (ch == ']') {tval[tlen++] = ch; NextCh(); state = 45; break;}
					else {t.kind = noSym; done = true; break;}
				case 45:
					{t.kind = 17; done = true; break;}
				case 46:
					if (ch == 'n') {tval[tlen++] = ch; NextCh(); state = 47; break;}
					else {t.kind = noSym; done = true; break;}
				case 47:
					if (ch == ']') {tval[tlen++] = ch; NextCh(); state = 48; break;}
					else {t.kind = noSym; done = true; break;}
				case 48:
					{t.kind = 18; done = true; break;}
				case 49:
					{t.kind = 20; done = true; break;}
				case 50:
					{t.kind = 22; done = true; break;}
				case 51:
					{t.kind = 23; done = true; break;}
				case 52:
					if (ch == 't') {tval[tlen++] = ch; NextCh(); state = 53; break;}
					else {t.kind = noSym; done = true; break;}
				case 53:
					if (ch == '>') {tval[tlen++] = ch; NextCh(); state = 54; break;}
					else {t.kind = noSym; done = true; break;}
				case 54:
					{t.kind = 43; done = true; break;}
				case 55:
					if (ch == 't') {tval[tlen++] = ch; NextCh(); state = 56; break;}
					else {t.kind = noSym; done = true; break;}
				case 56:
					if (ch == '>') {tval[tlen++] = ch; NextCh(); state = 57; break;}
					else {t.kind = noSym; done = true; break;}
				case 57:
					{t.kind = 44; done = true; break;}
				case 58:
					if (ch == 'n') {tval[tlen++] = ch; NextCh(); state = 21; break;}
					else if (ch == 'o') {tval[tlen++] = ch; NextCh(); state = 27; break;}
					else if (ch == 'm') {tval[tlen++] = ch; NextCh(); state = 30; break;}
					else if (ch == 'd') {tval[tlen++] = ch; NextCh(); state = 33; break;}
					else if (ch == 't') {tval[tlen++] = ch; NextCh(); state = 38; break;}
					else if (ch == 'j') {tval[tlen++] = ch; NextCh(); state = 43; break;}
					else if (ch == 'y') {tval[tlen++] = ch; NextCh(); state = 46; break;}
					else {t.kind = 7; done = true; break;}
				case 59:
					if (ch == '=') {tval[tlen++] = ch; NextCh(); state = 49; break;}
					else if (ch == 'a') {tval[tlen++] = ch; NextCh(); state = 61; break;}
					else {t.kind = 19; done = true; break;}
				case 60:
					if (ch == '=') {tval[tlen++] = ch; NextCh(); state = 50; break;}
					else {t.kind = 21; done = true; break;}
				case 61:
					if (ch == 'b') {tval[tlen++] = ch; NextCh(); state = 62; break;}
					else {t.kind = noSym; done = true; break;}
				case 62:
					if (ch == 's') {tval[tlen++] = ch; NextCh(); state = 63; break;}
					else {t.kind = noSym; done = true; break;}
				case 63:
					if (ch == 't') {tval[tlen++] = ch; NextCh(); state = 64; break;}
					else {t.kind = noSym; done = true; break;}
				case 64:
					if (ch == 'r') {tval[tlen++] = ch; NextCh(); state = 65; break;}
					else {t.kind = noSym; done = true; break;}
				case 65:
					if (ch == 'a') {tval[tlen++] = ch; NextCh(); state = 66; break;}
					else {t.kind = noSym; done = true; break;}
				case 66:
					if (ch == 'k') {tval[tlen++] = ch; NextCh(); state = 52; break;}
					else if (ch == 'c') {tval[tlen++] = ch; NextCh(); state = 55; break;}
					else {t.kind = noSym; done = true; break;}

			}
		}
		t.val = new String(tval, 0, tlen);
		return t;
	}
	
	// get the next token (possibly a token already seen during peeking)
	public Token Scan () {
		if (tokens.next == null) {
			return NextToken();
		} else {
			pt = tokens = tokens.next;
			return tokens;
		}
	}

	// get the next token, ignore pragmas
	public Token Peek () {
		if (pt.next == null) {
			do {
				pt = pt.next = NextToken();
			} while (pt.kind > maxT); // skip pragmas
		} else {
			do {
				pt = pt.next;
			} while (pt.kind > maxT);
		}
		return pt;
	}

	// make sure that peeking starts at current scan position
	public void ResetPeek () { pt = tokens; }

} // end Scanner

