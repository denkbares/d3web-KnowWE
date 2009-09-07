package de.d3web.we.codeCompletion;

public class DefaultCompletionFinder extends AbstractCompletionFinder {

	@Override
	public CompletionFinding find(String name, String data) {
		//if(name.contains(data)) {
			//int pos = name.indexOf(data);
			//return new CompletionFinding(name,name.substring(pos+data.length()),data.length());
			//if(this.startsWithIgnoreCase(name,data)) 
			if(name.startsWith(data)) 
			return new CompletionFinding(name,name.substring(data.length()),data.length());
		//}
		return null;
	}
	
	
}
