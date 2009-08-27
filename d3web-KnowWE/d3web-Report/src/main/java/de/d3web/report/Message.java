package de.d3web.report;

/**
 * Stores messages from the parsers.
 * Used to indicate errors during parsing. Therefore a message stores the actual
 * message, the filename, line number and the erroneous  line with error 
 * the error highlighted.
 *
 * @author Christian Braun
 * 
 * @version 1.021
 * @since JDK 1.5
 */
public class Message {

    /**
     * Message for an unknown error.
     */
    private static final String UNKNOWN_ERROR = "An unknow error occurred.";

    public static final String ERROR = "error";
    public static final String WARNING = "warning";
    public static final String NOTE = "note";
       
    /** type of the message. */
    private String messageType;
    
    /** The message of the message. */
    private String messageText;

    /** The filename in which the error occurred. */
    private String filename;

    /** Line number in which the message occurred. */
    private int lineNo;
    private String location;
    
    /** Column number in which the message occurred. */
    private int columnNo;
    
    /** Row in which the error occurred. */
    private String line;

    /**
     * Creates a message with unknown error.
     */
    public Message() {
        this(UNKNOWN_ERROR, "", "", 0, "");
    }

    /**
     * Constructor. Creates a message with given message text.
     *
     * @param messageText 
     */
    public Message(String messageText) {
        this(UNKNOWN_ERROR, messageText, "", 0, "");
    }

    /**
     * Constructor. Creates a message with given message text and name of the file
     * in which the message occurred.
     *
     * @param messageText 
     * @param file 
     */
    public Message(String messageText, String file) {
        this(UNKNOWN_ERROR, messageText, file, 0, "");
    }

    /**
     * Constructor. Creates a message with message text, filename and line number
     * of the message.
     *
     * @param messageText
     * @param file 
     * @param lineNo
     */
    public Message(String messageText, String file, int lineNo) {
        this(UNKNOWN_ERROR, messageText, file, lineNo, "");
    }

    /**
     * Constructor. Creates a message with message text, filename and line of the
     * message.
     *
     * @param messageText
     * @param file
     * @param line
     */
    public Message(String messageText, String file, String line) {
        this(UNKNOWN_ERROR, messageText, file, 0, line);
    }

    /**
     * Constructor. Creates a message with message text, filename, line number and
     * line of the message.
     *
     * @param messageText
     * @param file
     * @param lineNo
     * @param line
     */
    public Message(String messageText, String file, int lineNo, String line) {
        this(UNKNOWN_ERROR, messageText, file, lineNo, line);
    }

    public Message(String messageType, String messageText, String file, int lineNo, String line) {
        this(messageType, messageText, file, lineNo, 0 , line);
    }
    
    public static Message createError(String messageText, String file, int lineNo, String line) {
    	return new Message(ERROR, messageText, file, lineNo, line);
    }
    
    public static Message createError(String messageText, String file, int lineNo, int column, String line) {
    	return new Message(ERROR, messageText, file, lineNo, column, line);
    }
    
    public static Message createWarning(String messageText, String file, int lineNo, String line) {
    	return new Message(WARNING, messageText, file, lineNo, line);
    }
    
    public static Message createWarning(String messageText, String file, int lineNo, int column, String line) {
    	return new Message(WARNING, messageText, file, lineNo, column, line);
    }
    
    public static Message createNote(String messageText, String file, int lineNo, String line) {
    	return new Message(NOTE, messageText, file, lineNo, line);
    }
    
    public static Message createNote(String messageText, String file, int lineNo, int column, String line) {
    	return new Message(NOTE, messageText, file, lineNo, column, line);
    }
      
    /**
     * Constructor.
     * @param messageType
     * @param messageText
     * @param file
     * @param lineNo
     * @param columnNo
     * @param line
     */
    public Message(String messageType, String messageText, String file, int lineNo, int columnNo, String line) {
        setMessageType(messageType);
        if (messageText == null) messageText = "";
        if (file == null) file = "";
        if (line == null) line = "";
        if (lineNo < 0) lineNo = 0;
        if (columnNo < 0) columnNo = 0;
        this.messageText = messageText;
        this.filename = file;
        this.lineNo = lineNo;
        this.columnNo = columnNo;
        this.line = line;
        this.location = "";
    }
    
    /**
     * Sets the message type, e.g. error, warning, note.
     * 
     * @param messageType
     */
    public void setMessageType(String messageType) {
        if ((messageType==null) ||
            !(messageType==ERROR || messageType==WARNING || messageType==NOTE))
            this.messageType = UNKNOWN_ERROR;
        else
            this.messageType = messageType;
    }
    
    /**
     * Sets the message text of the message.
     * 
     * @param messageText
     */
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    /**
     * Sets the filename the message belongs to.
     * 
     * @param filename
     */
    public void setFilename(String filename) {
        if (filename == null)
            this.filename = "";
        else
            this.filename = filename;
    }
    
    /**
     * Sets the location of the message.
     * 
     * @param location
     */
    public void setLocation(String location) {
        if (location==null) this.location = "";
        else this.location = location;
    }
    
    /**
     * Returns the type of the message, e.g. warning, note, error.
     * 
     * @return String
     */
    public String getMessageType() {
        return messageType;
    }
    
    /**
     * Returns the message text of the message.
     *
     * @return String
     */
    public String getMessageText() {
        return messageText;
    }


    /**
     * Returns the filename in which the message occurred.
     *
     * @return String
     */
    public String getFilename() {
        return filename;
    }


    /**
     * Returns the row number in which the message occurred.
     *
     * @return Integer
     */
    public int getLineNo() {
        return lineNo;
    }
    
    /**
     * Returns the column number in which the message occurred.
     *
     * @return Integer
     */
    public int getColumnNo() {
        return columnNo;
    }

    public String getLocation() {
        return location;
    }


    /**
     * Returns the line in which the message occurred. If the row is unknown an 
     * empty string is returned.
     *
     * @return String
     */
    public String getLine() {
        return line;
    }

    /**
     * Returns the message as a string.
     *
     * @return String
     */
    @Override
	public String toString() {
        String s = new String();
        if (!filename.equals("")) s += filename + ":";
        if (lineNo != 0) {
            if (filename.equals("")) s += "Zeile";
            s += " " + lineNo + ":";
        }
        if (columnNo != 0) {
            if (filename.equals("")) s += "Spalte";
            s += " " + columnNo + ":";
        }
        if (!filename.equals("") || (lineNo != 0)) s += "\n";
        if (messageType==ERROR || messageType==WARNING || messageType==NOTE)
            s+= messageType + ": ";
        s += messageText + "\n";
        if (!line.equals("")) s += line + "\n";
        return s;
    }

    /**
     * Sets the line number of the message.
     * @param lineNo
     */
	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

}