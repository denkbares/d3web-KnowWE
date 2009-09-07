package de.d3web.we.kdom.typeInformation;


public class XCLRelationInfo  {
	
	private String kbid;
	private String id;
	
	public XCLRelationInfo (String id, String kbid) {
		this.id = id;
		this.kbid = kbid;
	}

	public String getKbid() {
		return kbid;
	}

	public String getId() {
		return id;
	}
}
