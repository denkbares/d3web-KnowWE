package org.apache.wiki.providers.gitCache;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.providers.AbstractFileProvider;

public class DelayedFileProvider extends AbstractFileProvider {
	public DelayedFileProvider() {

	}

	@Override
	public void movePage(Page from, String to) throws ProviderException {
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void putPageText(final Page page, final String text) throws ProviderException {
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		super.putPageText(page, text);
	}

	@Override
	public boolean pageExists(final String page, final int version) {
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return super.pageExists(page, version);
	}

	@Override
	public Page getPageInfo(final String pageName, final int version) throws ProviderException {
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return super.getPageInfo(pageName, version);
	}

	@Override
	public Collection<Page> getAllPages() {
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		try {
			return super.getAllPages();
		}
		catch (ProviderException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void initialize(final Engine engine, final Properties properties) throws NoRequiredPropertyException, IOException {

		int a=0;
	}

	@Override
	public String getPageText(final String pageName, final int version) throws ProviderException {
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return super.getPageText(pageName, version);
	}

	@Override
	public String getProviderInfo() {
		return DelayedFileProvider.class.getSimpleName();
	}
}
