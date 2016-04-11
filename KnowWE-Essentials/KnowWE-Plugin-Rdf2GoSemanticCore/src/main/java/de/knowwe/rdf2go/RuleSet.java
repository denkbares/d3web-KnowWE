/*
 * Copyright (C) 2013 denkbares GmbH
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
package de.knowwe.rdf2go;

/**
 * The RuleSet specifies the OWL profile used by OWLIM for OWL reasoning.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created 28.01.14
 */
public enum RuleSet {

	NONE("owlim-rdf.ttl"),
	RDF("owlim-rdf.ttl"),
	RDFS("owlim-rdfs.ttl"),
	//OWL_HORST("owlim-owl-horst.ttl"),
	//OWL_MAX("owlim-owl-max.ttl"),
	//OWL2_RL_CONF("owlim-owl2-rl-conf.ttl"),
	//OWL2_RL_REDUCED("owlim-owl2-rl-reduced.ttl"),
	RDFS_OPTIMIZED("owlim-rdfs-optimized.ttl"),
	OWL_HORST_OPTIMIZED("owlim-owl-horst-optimized.ttl"),
	OWL_MAX_OPTIMIZED("owlim-owl-max-optimized.ttl"),
	OWL2_RL_REDUCED_OPTIMIZED("owlim-owl2-rl-reduced-optimized.ttl");

	private final String configFile;

	private RuleSet(String configFile) {
		this.configFile = configFile;
	}

	public String getConfigFile() {
		return configFile;
	}
}
