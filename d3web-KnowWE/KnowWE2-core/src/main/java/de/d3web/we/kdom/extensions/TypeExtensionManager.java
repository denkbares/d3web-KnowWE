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
