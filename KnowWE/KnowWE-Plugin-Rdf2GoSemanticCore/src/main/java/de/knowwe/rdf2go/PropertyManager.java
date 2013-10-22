/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

/**
 * 
 */
package de.knowwe.rdf2go;

import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.node.URI;

import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * This class manages semantic properties, n-ary as well as simple ones
 * 
 * @author kazamatzuri
 * 
 */
public class PropertyManager {

	static PropertyManager instance;

	/**
	 * 
	 * @param defaultModulesTxtPath
	 * @return an instance
	 */
	public static synchronized PropertyManager getInstance() {
		if (instance == null) instance = new PropertyManager();
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
		// uo = UpperOntology.getInstance();
	}

	public boolean isValid(Rdf2GoCore core, String property) {
		URI prop = Rdf2GoCore.getInstance().createlocalURI(
				property);
		return isValid(core, prop);
	}

	/**
	 * checks if a given property is valid. To be valid is has to be either
	 * defined as an nary-property via the <properties> tag. Or it has to be a
	 * 'normal' property (imported or via the extensions...)
	 * 
	 * @param property
	 * @return
	 */
	public boolean isValid(Rdf2GoCore core, URI property) {
		boolean result = false;
		// TODO evil hack, to get going
		if (Rdf2GoUtils.getLocalName(property).contains("subClassOf")
				|| Rdf2GoUtils.getLocalName(property).contains("type")
				|| Rdf2GoUtils.getLocalName(property).contains("subPropertyOf")) return true;
		String querystring = Rdf2GoUtils.getSparqlNamespaceShorts(core);
		String objectpropquery = querystring + "ASK WHERE { <"
				+ property.toString() + "> rdf:type owl:ObjectProperty }";
		String datatypepropquery = querystring + "ASK WHERE { <"
				+ property.toString() + "> rdf:type owl:DatatypeProperty }";
		querystring += "ASK WHERE { <" + property.toString()
				+ "> rdfs:subClassOf ns:NaryProperty }";

		try {
			result = Rdf2GoCore.getInstance().sparqlAsk(querystring);
			if (!result) {
				result = Rdf2GoCore.getInstance().sparqlAsk(objectpropquery);
			}
			if (!result) {
				result = Rdf2GoCore.getInstance().sparqlAsk(datatypepropquery);
			}
		}
		catch (ModelRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		return (Rdf2GoUtils.getLocalName(property).contains("subClassOf")
				|| Rdf2GoUtils.getLocalName(property).contains("type") || Rdf2GoUtils.getLocalName(
				property).contains("subPropertyOf"));

	}

	/**
	 * @param prop
	 * @return
	 */
	public boolean isRDF(URI property) {
		return (Rdf2GoUtils.getLocalName(property).contains("type"));

	}

	public boolean isNary(URI prop) {
		boolean result = false;
		String querystring = "PREFIX ns: <" + Rdf2GoCore.getInstance().getLocalNamespace() + "> \n";
		querystring += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
		querystring += "PREFIX lns: <" + Rdf2GoCore.getInstance().getLocalNamespace() + "> \n";
		querystring += "PREFIX owl:<http://www.w3.org/2002/07/owl#> \n";
		querystring += "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> \n";
		querystring += "ASK WHERE { <" + prop.toString()
				+ "> rdfs:subClassOf ns:NaryProperty }";

		try {
			result = Rdf2GoCore.getInstance().sparqlAsk(querystring);
		}
		catch (ModelRuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
