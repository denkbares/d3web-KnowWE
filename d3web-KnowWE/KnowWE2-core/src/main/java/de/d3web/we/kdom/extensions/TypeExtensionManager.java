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
package de.d3web.we.kdom.extensions;

import java.util.Collection;
import java.util.HashSet;

import de.d3web.we.kdom.KnowWEObjectType;

/**
 * @author kazamatzuri
 * 
 */
public class TypeExtensionManager {

    private static TypeExtensionManager me;
    private Collection<KnowWEObjectType> typeextensions;

    private TypeExtensionManager() {
	typeextensions=new HashSet<KnowWEObjectType>();
    }

    /**
     * register a new type within the typesystem
     * it is attached at the root article type 
     * @param newtype
     */
    public void registerType(KnowWEObjectType newtype){
	typeextensions.add(newtype);
    }
    
    public static TypeExtensionManager getInstance() {
	if (me == null)
	    me = new TypeExtensionManager();
	return me;
    }

    /**
     * prevent cloning
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }

    /**
     * @return
     */
    public Collection<KnowWEObjectType> getTypes() {
	return typeextensions;
    }

}
