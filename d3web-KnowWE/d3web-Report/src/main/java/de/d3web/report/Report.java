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

package de.d3web.report;

import java.util.*;

/**
 * This class administers the messages from the parsers.
 *
 * @author Christian Braun
 * @version 1.021
 * @since JDK 1.5
 */
public class Report {
	
	protected List<Message> allMessages;
	protected List<Message> errors;
	protected List<Message> warnings;
	protected List<Message> notes;


	/**
	 * Constructor.
	 */
	public Report() {
		init();
	}

	private void init() {
		allMessages = new ArrayList<Message>();
		errors = new ArrayList<Message>();
		warnings = new ArrayList<Message>();
		notes = new ArrayList<Message>();
	}
	
	public Report(List<Message> c) {
		init();
		this.addAll(c);
	}


	/**
	 * Adds the given message to the current report.
	 * 
	 * @param message 
	 * @return message count of the current report
	 */
	public int add(Message message) {
		if (message!=null) {
			allMessages.add(message);
			if (message.getMessageType()==Message.ERROR)
				errors.add(message);
			else if (message.getMessageType()==Message.WARNING)
				warnings.add(message);
			else if (message.getMessageType()==Message.NOTE)
				notes.add(message);
		}
		return allMessages.size();
	}

	public int addAll(List<Message> messages) {
		for (Iterator<Message> it = messages.iterator(); it.hasNext(); ) {
			Message nextMessage = it.next();
			allMessages.add(nextMessage);
			if (nextMessage.getMessageType()==Message.ERROR)
				errors.add(nextMessage);
			else if (nextMessage.getMessageType()==Message.WARNING)
				warnings.add(nextMessage);
			else if (nextMessage.getMessageType()==Message.NOTE)
				notes.add(nextMessage);
		}
		return allMessages.size();
	}
	
	public int addAll(Report report) {
		return addAll(report.getAllMessages());
	}
	
	public void removeMessage(Message m) {
		while(allMessages.remove(m));
		while(errors.remove(m));
		while(warnings.remove(m));
		while(notes.remove(m));
	}
	
	/**
	 * Adds the given message as an error message to the report.
	 *
	 * @param message 
	 * @return number of errors in the report
	 */
	public int error(Message message) {
		if (message!=null) {
			message.setMessageType(Message.ERROR);
			allMessages.add(message);
			errors.add(message);
		}
		return errors.size();
	}


	/**
	 * Adds the given message as a warning message.
	 *
	 * @param message
	 * @return number of warnings in the report
	 */
	public int warning(Message message) {
		if (message!=null) {
			message.setMessageType(Message.WARNING);
			allMessages.add(message);
			warnings.add(message);
		}
		return warnings.size();
	}

	/**
	 * Adds the given message as a note.
	 *
	 * @param message
	 * @return number of notes in the report
	 */
	public int note(Message message) {
		if (message!=null) {
			message.setMessageType(Message.NOTE);
			allMessages.add(message);
			notes.add(message);
		}
		return notes.size();
	}
	
	
	/**
	 * Returns a string containing the number of errors, notes and warnings.
	 * @return String
	 */
	public String getMessageCount() {
		String s = new String();
		if (errors.size() > 0) {
			s += errors.size() + " errors";
			if (warnings.size() > 0 || notes.size() > 0) s += ", ";
		}
		if (warnings.size() > 0) {
			s += warnings.size() + (warnings.size() == 1 ? " warning" : " warnings");
			if (notes.size() > 0) s += ", ";
		}
		if (notes.size() > 0) {
			s += notes.size() + (notes.size() == 1 ? " note" : " notes");
		}
		if (!s.equals("")) s += "\n";
		return s;
	}

	/**
	 * Returns the number of messages of the report.
	 * @return Integer
	 */
	public int size() {
		return allMessages.size();
	}

	public boolean isEmpty() {
		return allMessages.isEmpty();
	}
	
	public int getErrorCount() {
		return errors.size();
	}
	
	public boolean hasErrors() {
		return (getErrorCount() > 0);
	}

	public int getWarningCount() {
		return warnings.size();
	}

	public int getNoteCount() {
		return notes.size();
	}

	/**
	 * Returns the message at index i.
	 * @param i 
	 * @return message
	 */
	public Message getMessage(int i) {
		return (i >= 0 && i < allMessages.size() ? allMessages.get(i) : null);
	}

	public List<Message> getAllMessages() {
		return allMessages;
	}
	
	public List<Message> getErrors() {
		return errors;
	}
	
	public List<Message> getWarnings() {
		return warnings;
	}
	
	public List<Message> getNotes() {
		return notes;
	}
	
	
	/**
	 * Returns all messages as string.
	 * @return String
	 */
	public String getAllMessagesAsString() {
		String s = new String();
		Iterator<Message> it = allMessages.iterator();
		while (it.hasNext()) s += it.next().toString() + "\n";
		return s;
	}
		
	/**
	 * Returns all error messages as string.
	 * @return String
	 */
	public String getErrorMsg() {
		String s = new String();
		Iterator<Message> it = errors.iterator();
		while (it.hasNext()) s += it.next().toString() + "\n";
		return s;
	}

	/**
	 * Returns all warnings as string.
	 * @return String
	 */
	public String getWarningMsg() {
		String s = new String();
		Iterator<Message> it = warnings.iterator();
		while (it.hasNext()) s += it.next().toString() + "\n";
		return s;
	}

	/**
	 * Returns all notes as string.
	 * @return String
	 */
	public String getNoteMsg() {
		String s = new String();
		Iterator<Message> it = notes.iterator();
		while (it.hasNext()) s += it.next().toString() + "\n";
		return s;
	}


	/**
	 * Returns the whole report as string.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		String s = getAllMessagesAsString();
		if (!s.equals("")) s += "\n";
		return s + getMessageCount();
	}

}
