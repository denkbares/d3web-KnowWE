/**
 * 
 */
package de.d3web.we.module.semantic.owl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;

/**
 * @author kazamatzuri
 * 
 */
public class IntermediateOwlObject {
    public static final int LIST = 1;
    public static final int LITERAL = 0;
    private ArrayList<URI> literals;
    ArrayList<Statement> statementslist;
    private ArrayList<URI> removedliterals;
    private HashMap<URI, String> textorigin;
    private boolean validprop;
    private String bad;

    public IntermediateOwlObject() {
	validprop=true;
	literals = new ArrayList<URI>();
	removedliterals = new ArrayList<URI>();
	statementslist = new ArrayList<Statement>();
	textorigin = new HashMap<URI, String>();
	bad="";
    }

    /**
     * @param myLiteral
     *            the myLiteral to set
     */
    public void addLiteral(URI literal) {
	literals.add(literal);
    }

    public void addStatement(Statement statement) {
	statementslist.add(statement);
    }

    /**
     * @return
     */
    public List<Statement> getAllStatements() {
	return statementslist;
    }

    public ArrayList<URI> getLiterals() {
	return literals;
    }

    /**
     * @param owl
     */
    public void merge(IntermediateOwlObject owl) {
	if (owl != null) {
	    owl.commitremove();
	    literals.addAll(owl.getLiterals());
	    statementslist.addAll(owl.getAllStatements());
	    textorigin.putAll(owl.getOrigins());
	    if (validprop){
		setValidPropFlag(owl.getValidPropFlag());
		setBadAttribute(owl.getBadAttribute());
	    }
	}
    }

    public HashMap<URI, String> getOrigins() {
	return textorigin;
    }

    /**
     * 
     */
    private void commitremove() {
	for (URI current : removedliterals) {
	    literals.remove(current);
	}
    }

    /**
     * @param statementslist
     *            the statementslist to set
     */
    public void setStatementslist(ArrayList<Statement> statementslist) {
	this.statementslist = statementslist;
    }

    /**
     * @param allStatements
     */
    public void addAllStatements(List<Statement> allStatements) {
	statementslist.addAll(allStatements);
    }

    /**
     * @param curi
     */
    public void removeLiteral(URI curi) {
	removedliterals.add(curi);
    }

    /**
     * @param curi
     * @return
     */
    public String getOrigin(URI curi) {
	return textorigin.get(curi);
    }

    /**
     * @param literalinstance
     * @param id
     */
    public void setOrigin(URI literalinstance, String id) {
	textorigin.put(literalinstance, id);
    }

    /**
     * @param validprop
     */
    public void setValidPropFlag(boolean validprop) {
	this.validprop=validprop;	
    }
    
    public void setBadAttribute(String bad){
	this.bad=bad;
    }
    
    public String getBadAttribute(){
	return bad;
    }
    
    public boolean getValidPropFlag(){
	return validprop;
    }


}
