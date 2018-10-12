package de.knowwe.util;

import java.util.Iterator;

import com.denkbares.collections.PriorityList;
import de.knowwe.plugin.Plugins;

/**
 * @author Alex Legler (denkbares GmbH)
 * @created 2018-10-12
 */
public class CredentialProviders {
	private static PriorityList<Double, CredentialProvider> providers = Plugins.getCredentialProviders();

	public static String get(String key, CredentialProvider.Credential credential) {
		final Iterator<PriorityList.Group<Double, CredentialProvider>> iterator = providers.groupIterator();

		while (iterator.hasNext()) {
			final PriorityList.Group<Double, CredentialProvider> group = iterator.next();

			for (CredentialProvider provider : group) {
				final String cred = provider.get(key, credential);

				if (cred != null) return cred;
			}
		}

		return null;
	}
}
