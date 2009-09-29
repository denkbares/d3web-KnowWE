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

/**
 * 
 */
package de.d3web.we.module.semantic.owl;

import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


/**
 * @author kazamatzuri
 * 
 */
public class PropertyManager {

    static PropertyManager instance;
    private UpperOntology uo;

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
	uo = UpperOntology.getInstance();
    }

    public boolean isValid(String property) {
	URI prop = UpperOntology.getInstance().getHelper().createlocalURI(property);
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
	//TODO evil hack, to get going
	if (property.getLocalName().contains("subClassOf")||property.getLocalName().contains("type")||property.getLocalName().contains("subPropertyOf")||property.getLocalName().contains("hasTag"))
	    return true;
	String querystring="PREFIX ns: <"+uo.getBaseNS()+"> \n";
	querystring+="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
	querystring+="PREFIX lns: <"+uo.getLocaleNS()+"> \n";
	querystring+="PREFIX owl:<http://www.w3.org/2002/07/owl#> \n";
	querystring+="PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> \n";
	String objectpropquery=querystring+"ASK WHERE { <"+property.toString()+"> rdf:type owl:ObjectProperty }";
	String datatypepropquery=querystring+"ASK WHERE { <"+property.toString()+"> rdf:type owl:DatatypeProperty }";
	querystring+= "ASK WHERE { <"+property.toString()+"> rdfs:subClassOf ns:NaryProperty }";
	RepositoryConnection con = UpperOntology.getInstance().getConnection();
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
    /**
     * @param prop
     * @return
     */
    public boolean isRDF(URI property) {
	return (property.getLocalName().contains("type"));
	    
    }


}
