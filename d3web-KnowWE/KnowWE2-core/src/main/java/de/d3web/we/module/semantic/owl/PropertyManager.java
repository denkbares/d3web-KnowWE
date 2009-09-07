/**
 * 
 */
package de.d3web.we.module.semantic.owl;

import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import de.d3web.we.kdom.Section;

/**
 * @author kazamatzuri
 * 
 */
public class PropertyManager {

    static PropertyManager instance;
    private UpperOntology2 uo;

    /**
     * 
     * @param defaultModulesTxtPath
     * @return an instance
     */
    public static synchronized PropertyManager getInstance() {
	if (instance == null)
	    instance = new PropertyManager();
	return instance;
    }

    /**
     * prevent cloning
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }

    /**
     * 
     */
    private PropertyManager() {
	uo = UpperOntology2.getInstance();
    }

    public IntermediateOwlObject createProperty(String subject,
	    String property, String object, Section source) {

	
	URI suri=uo.createlocalURI(subject);
	URI puri=uo.createlocalURI(property);
	URI ouri=uo.createlocalURI(object);
	
	return createProperty(suri,puri,ouri,source);
    }
    /**
     * @param soluri
     * @param prop
     * @param stringa
     * @param id
     * @return
     */
    public IntermediateOwlObject createProperty(URI suri, URI puri,
	    URI ouri, Section source) {
	IntermediateOwlObject io = new IntermediateOwlObject();
	try {
	    BNode to = uo.getVf().createBNode();
	    BNode nary = uo.getVf().createBNode();
	    io.addStatement(uo.createStatement(to,RDF.TYPE, uo.createURI("TextOrigin")));
	    io.addStatement(uo.createStatement(to,uo.createURI("hasNode"), uo.createLiteral(source.getId())));
	    io.addStatement(uo.createStatement(to,uo.createURI("hasTopic"), uo.createLiteral(source.getTopic())));
	    io.addStatement(uo.createStatement(nary,RDFS.ISDEFINEDBY, to));
	    io.addStatement(uo.createStatement(nary,RDF.TYPE,RDF.STATEMENT));
	    io.addStatement(uo.createStatement(nary,RDF.PREDICATE,puri));
	    io.addStatement(uo.createStatement(nary,RDF.OBJECT,ouri));
	    io.addStatement(uo.createStatement(nary,RDF.SUBJECT,suri));
	    
	} catch (RepositoryException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	return io;
    }

    
    public boolean isValid(String property) {
	URI prop = UpperOntology2.getInstance().createlocalURI(property);
	return isValid(prop);
    }

    
    /**
     * checks if a given property is valid. To be valid is has to be 
     * either defined as an nary-property via the <properties> tag. Or it has
     * to be a  'normal' property (imported or via the extensions...)
     * 
     * @param property
     * @return
     */   
    public boolean isValid(URI property) {
	boolean result=false;
	if (property.getLocalName().contains("subClassOf")||property.getLocalName().contains("type")||property.getLocalName().contains("subPropertyOf"))
	    return true;
	String querystring="PREFIX ns: <"+uo.getBaseNS()+"> \n";
	querystring+="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
	querystring+="PREFIX lns: <"+uo.getLocaleNS()+"> \n";
	querystring+="PREFIX owl:<http://www.w3.org/2002/07/owl#> \n";
	querystring+="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> \n";
	String objectpropquery=querystring+"ASK WHERE { <"+property.toString()+"> rdf:type owl:ObjectProperty }";
	String datatypepropquery=querystring+"ASK WHERE { <"+property.toString()+"> rdf:type owl:DatatypeProperty }";
	querystring+= "ASK WHERE { <"+property.toString()+"> rdfs:subClassOf ns:NaryProperty }";
	RepositoryConnection con = UpperOntology2.getInstance().getConnection();
	Query query = null;
	try {
	    query = con.prepareQuery(QueryLanguage.SPARQL, querystring);
	} catch (RepositoryException e) {
	    Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, e.getMessage());	   
	} catch (MalformedQueryException e) {
	    Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, e.getMessage());
	}
	try {
	    result= ((BooleanQuery) query).evaluate();	    
	} catch (QueryEvaluationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	if (!result){
	    try {
		    query = con.prepareQuery(QueryLanguage.SPARQL, objectpropquery);
		} catch (RepositoryException e) {
		    Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, e.getMessage());	   
		} catch (MalformedQueryException e) {
		    Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, e.getMessage());
		}
		try {
		    result= ((BooleanQuery) query).evaluate();	    
		} catch (QueryEvaluationException e) {
		    e.printStackTrace();
		}
	}
	if (!result){
	    try {
		    query = con.prepareQuery(QueryLanguage.SPARQL, datatypepropquery);
		} catch (RepositoryException e) {
		    Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, e.getMessage());	   
		} catch (MalformedQueryException e) {
		    Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ERROR, e.getMessage());
		}
		try {
		    result= ((BooleanQuery) query).evaluate();	    
		} catch (QueryEvaluationException e) {
		    e.printStackTrace();
		}
	}

	
	return result;
    }

    /**
     * @param cur
     */
    public IntermediateOwlObject createProperty(String cur) {
	URI prop = uo.createlocalURI(cur);
	URI naryprop = uo.createURI("NaryProperty");
	IntermediateOwlObject io = new IntermediateOwlObject();
	if (!isValid(prop)) {
	    try {
		io.addStatement(uo.createStatement(prop, RDFS.SUBCLASSOF, naryprop));
	    } catch (RepositoryException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return io;

    }

    /**
     * 
     * 
     * @param string
     */
    public void registerSimpleProperty(String string) {
	// TODO Auto-generated method stub
	
    }

    /**
     * @param prop
     * @return
     */
    public boolean isRDFS(URI property) {
	// TODO Auto-generated method stub
	return (property.getLocalName().contains("subClassOf")||property.getLocalName().contains("type")||property.getLocalName().contains("subPropertyOf"));
	    
    }


}
