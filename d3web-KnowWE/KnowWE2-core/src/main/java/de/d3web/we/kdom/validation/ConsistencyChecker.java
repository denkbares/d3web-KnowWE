package de.d3web.we.kdom.validation;

import java.io.File;
import java.io.FileOutputStream;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.visitor.CreateTextVisitor;

public class ConsistencyChecker {

	private static ConsistencyChecker instance;

	public static synchronized  ConsistencyChecker getInstance() {
		if (instance == null) {
			instance = new ConsistencyChecker();
		}

		return instance;
	}
	
	/**
	 * prevent cloning
	 */
	 @Override
	public Object clone()
		throws CloneNotSupportedException
	  {
	    throw new CloneNotSupportedException(); 	   
	  }

	public boolean checkConsistency(String text, KnowWEArticle art) {

		CreateTextVisitor vis = CreateTextVisitor.getInstance();
		vis.visit(art.getSection());
		String treeText = vis.getText();

		boolean matches = treeText.equals(text);

		if (!matches) {
			String path = KnowWEEnvironment.getInstance().getContext()
					.getRealPath("");
			File f = new File(path + "/KnowWEExtension/tmp/consistency/", art
					.getTitle()
					+ ".txt");
			File fTree = new File(path + "/KnowWEExtension/tmp/consistency/",
					art.getTitle() + "_tree.txt");

			try {
				FileOutputStream stream = new FileOutputStream(f);
				FileOutputStream streamTree = new FileOutputStream(fTree);
				stream.write(text.getBytes());
				streamTree.write(treeText.getBytes());
				stream.close();
				streamTree.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return matches;
	}
}
