package de.d3web.KnOfficeParser;

import java.io.Reader;
import java.util.Collection;

import de.d3web.report.Message;

/**
 * Interface welche alle Builder implementieren sollten um diese mit ihren Parsern einheitlicha aufrufen zu können
 * @author Markus Friedrich
 *
 */
public interface KnOfficeParser {
	
	Collection<Message> addKnowledge(Reader r, IDObjectManagement idom, KnOfficeParameterSet s);
	
	Collection<Message> checkKnowledge();

}
