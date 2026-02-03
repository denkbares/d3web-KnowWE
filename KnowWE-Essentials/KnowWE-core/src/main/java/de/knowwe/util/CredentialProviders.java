package de.knowwe.util;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.denkbares.collections.PriorityList;
import de.knowwe.plugin.Plugins;

/**
 * @author Alex Legler (denkbares GmbH)
 * @created 2018-10-12
 */
public class CredentialProviders {
	// Einmalig beim Klassenladen initialisierte, unveränderliche, flache Provider-Liste
	private static final List<CredentialProvider> providers;

	static {
		PriorityList<Double, CredentialProvider> pl = Plugins.getCredentialProviders();
		List<CredentialProvider> flat = new ArrayList<>();
		Iterator<PriorityList.Group<Double, CredentialProvider>> it = pl.groupIterator();
		while (it.hasNext()) {
			PriorityList.Group<Double, CredentialProvider> group = it.next();
			for (CredentialProvider p : group) {
				flat.add(p);
			}
		}
		providers = Collections.unmodifiableList(flat);
	}

	public static String get(String key, CredentialProvider.Credential credential) {
		for (CredentialProvider provider : providers) {
			final String cred = provider.get(key, credential);
			if (cred != null) return cred;
		}
		return null;
	}

	public static String match(String key, CredentialProvider.Credential credential) {
		for (CredentialProvider provider : providers) {
			final String cred = provider.match(key, credential);
			if (cred != null) return cred;
		}
		return null;
	}
}
