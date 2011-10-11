/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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
package de.knowwe.core.compile;

import de.knowwe.core.kdom.Type;

/**
 * Instances of this class can be registered to SubtreeHandlers, to determine
 * their compile behavior. Depending on the type of the module and the checked
 * constraints, the SubtreeHandler either compiles or does not compile in the
 * different circumstances.
 * 
 * @author Albrecht Striffler
 * @created 25.01.2011
 * @param <T> is the Type of the Section
 */
public abstract class ConstraintModule<T extends Type> implements IncrementalConstraint<T> {

	/**
	 * Represents the behavior of the SubtreeHandler, depending on whether the
	 * constraints are violated or not.
	 */
	public Operator OPERATOR;

	/**
	 * Represents the purpose of this module.
	 */
	public Purpose PURPOSE;

	/**
	 * Represents the behavior of the SubtreeHandler, depending on whether the
	 * constraints are violated or not.
	 */
	public enum Operator {
		/**
		 * A ConstraintModule with this operator prevents compilation, if the
		 * checked constraints are violated. In other words: The SubtreeHandler
		 * will not compile, if the violatedConstraints method returns true.
		 */
		DONT_COMPILE_IF_VIOLATED,
		/**
		 * A ConstraintModule with this operator forces compilation, if the
		 * checked constraints are violated. In other words: The SubtreeHandler
		 * will compile, if the violatedConstraints method returns true.
		 */
		COMPILE_IF_VIOLATED
	}

	/**
	 * Represents the purpose of the module.
	 */
	public enum Purpose {
		/** The constraints are only tested for the create step. */
		CREATE,
		/** The constraints are only tested for the destroy step. */
		DESTROY,
		/** The constraints are tested for the create and the destroy step. */
		CREATE_AND_DESTROY
	}
	
	/**
	 * Creates a default ConstraintModule with Operator.COMPILE_IF_VIOLATED and
	 * Purpose.CREATE_AND_DESTROY.
	 */
	public ConstraintModule() {
		this(Operator.COMPILE_IF_VIOLATED, Purpose.CREATE_AND_DESTROY);
	}
	
	/**
	 * Creates a ConstraintModules with the given Operator and Purpose.
	 * 
	 * @param o will become the Operator of this module
	 * @param p will become the Purpose of this module
	 */
	public ConstraintModule(Operator o, Purpose p) {
		if (o != null) this.OPERATOR = o;
		if (p != null) this.PURPOSE = p;
	}

}
