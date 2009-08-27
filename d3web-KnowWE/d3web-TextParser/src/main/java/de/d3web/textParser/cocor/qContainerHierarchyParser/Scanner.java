package de.d3web.textParser.cocor.qContainerHierarchyParser;

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
	static final int maxT = 7;
	static final int noSym = 7;
	short[] start = {
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 13,  1,  1, 14,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  0,  1,  2,  4,  1,  1,  1,  1,  1,  1,  1,  1,  1, 15,  1, 11,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  6,  1,  0,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
	  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,
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
		
		// COCO begin change
		// ignore.set(' '); // COCO: blanks are always white space -> not in knowme! by schwab 20.12.05
		// COCO end change

		ignore.set(9); ignore.set(32); 
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
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= '!' || ch >= '$' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 1; break;}
					else {t.kind = 1; done = true; break;}
				case 2:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= '!' || ch >= '#' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 2; break;}
					else if (ch == '"') {tval[tlen++] = ch; NextCh(); state = 3; break;}
					else {t.kind = noSym; done = true; break;}
				case 3:
					{t.kind = 1; done = true; break;}
				case 4:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= 31 || ch == '!' || ch >= '$' && ch <= ',' || ch >= '.' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 5; break;}
					else {t.kind = noSym; done = true; break;}
				case 5:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= 31 || ch == '!' || ch >= '$' && ch <= ',' || ch >= '.' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 5; break;}
					else {t.kind = 2; done = true; break;}
				case 6:
					if ((ch >= '0' && ch <= '9')) {tval[tlen++] = ch; NextCh(); state = 7; break;}
					else {t.kind = noSym; done = true; break;}
				case 7:
					if ((ch >= '0' && ch <= '9')) {tval[tlen++] = ch; NextCh(); state = 7; break;}
					else if (ch == ']') {tval[tlen++] = ch; NextCh(); state = 8; break;}
					else {t.kind = noSym; done = true; break;}
				case 8:
					{t.kind = 3; done = true; break;}
				case 9:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 9; break;}
					else if ((ch == 10 || ch == 13)) {tval[tlen++] = ch; NextCh(); state = 10; break;}
					else {t.kind = noSym; done = true; break;}
				case 10:
					if ((ch == 10 || ch == 13)) {tval[tlen++] = ch; NextCh(); state = 10; break;}
					else {t.kind = 8; done = true; break;}
				case 11:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= '!' || ch >= '$' && ch <= '.' || ch >= '0' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 1; break;}
					else if (ch == '/') {tval[tlen++] = ch; NextCh(); state = 12; break;}
					else {t.kind = 1; done = true; break;}
				case 12:
					if ((ch <= 9 || ch >= 11 && ch <= 12 || ch >= 14 && ch <= '!' || ch >= '$' && ch <= 'Z' || ch == 92 || ch >= '^' && ch <= 255)) {tval[tlen++] = ch; NextCh(); state = 12; break;}
					else if ((ch >= '"' && ch <= '#' || ch == '[' || ch == ']')) {tval[tlen++] = ch; NextCh(); state = 9; break;}
					else if ((ch == 10 || ch == 13)) {tval[tlen++] = ch; NextCh(); state = 10; break;}
					else {t.kind = 1; done = true; break;}
				case 13:
					{t.kind = 4; done = true; break;}
				case 14:
					{t.kind = 5; done = true; break;}
				case 15:
					{t.kind = 6; done = true; break;}

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

