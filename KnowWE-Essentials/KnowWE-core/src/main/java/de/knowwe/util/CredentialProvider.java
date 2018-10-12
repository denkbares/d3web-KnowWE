package de.knowwe.util;

/**
 * Interface for credential providers.
 * <p>
 * A provider can provide credentials used for authenticating requests made by the Wiki system, such as attachment
 * updating.
 *
 * @author Alex Legler (denkbares GmbH)
 * @created 2018-10-12
 */
public interface CredentialProvider {
	/**
	 * Retrieves the specified credential stored under the specified key
	 *
	 * @param key        Key of the credential entry, could be a URL
	 * @param credential Credential to return
	 * @return Matching credential, or null if no matching credential can be found
	 */
	String get(String key, Credential credential);

	enum Credential {
		USERNAME,
		PASSWORD
	}
}
