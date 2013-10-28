package de.knowwe.core.kdom;

public class Types {

	/**
	 * Returns the last element of the type path that matches the specified
	 * class. If there is no such type in the specified path, null is returned.
	 * 
	 * @created 28.10.2013
	 * @param path the path to be searched
	 * @param typeClass the class to be searched
	 * @return the matched type
	 */
	public static <T> T getLastOfType(Type[] path, Class<T> typeClass) {
		for (int i = path.length - 1; i >= 0; i--) {
			Type type = path[i];
			if (typeClass.isInstance(type)) {
				return typeClass.cast(type);
			}
		}
		return null;
	}

	/**
	 * Returns the first element of the type path that matches the specified
	 * class. If there is no such type in the specified path, null is returned.
	 * 
	 * @created 28.10.2013
	 * @param path the path to be searched
	 * @param typeClass the class to be searched
	 * @return the matched type
	 */
	public static <T> T getFirstOfType(Type[] path, Class<T> typeClass) {
		for (int i = 0; i < path.length; i++) {
			Type type = path[i];
			if (typeClass.isInstance(type)) {
				return typeClass.cast(type);
			}
		}
		return null;
	}
}
