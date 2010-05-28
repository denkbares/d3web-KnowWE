package de.knowwe.d3web;

import javax.servlet.ServletContext;

import de.d3web.we.d3webModule.D3webModule;
import de.knowwe.plugin.Instantiation;

public class d3webInstantiation implements Instantiation {

	@Override
	public void init(ServletContext context) {
		D3webModule.initModule(context);
	}

}
