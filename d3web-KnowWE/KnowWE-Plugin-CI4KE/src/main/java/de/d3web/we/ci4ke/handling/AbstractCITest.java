package de.d3web.we.ci4ke.handling;

public abstract class AbstractCITest implements CITest {

	protected CIConfig config;
	
	public AbstractCITest(){}
	
	@Override
	public void init(CIConfig config) {
		this.config = config;
	}
}
