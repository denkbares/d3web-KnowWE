package de.knowwe.rdf2go.modelfactory;
//package de.d3web.we.core.semantic.rdf2go.modelfactory;
//
//import java.io.File;
//import java.util.Properties;
//
//import org.ontoware.rdf2go.RDF2Go;
//import org.ontoware.rdf2go.exception.ModelRuntimeException;
//import org.ontoware.rdf2go.impl.AbstractModelFactory;
//import org.ontoware.rdf2go.model.Model;
//import org.ontoware.rdf2go.model.ModelSet;
//import org.ontoware.rdf2go.model.Statement;
//import org.ontoware.rdf2go.model.node.URI;
//import org.openrdf.model.Graph;
//import org.openrdf.model.Resource;
//import org.openrdf.model.ValueFactory;
//import org.openrdf.model.impl.GraphImpl;
//import org.openrdf.sail.Sail;
//import org.openrdf.sail.config.SailConfigException;
//import org.openrdf.sail.config.SailFactory;
//import org.openrdf.sail.config.SailImplConfig;
//import org.openrdf.sail.config.SailRegistry;
//
//import com.ontotext.trree.config.OWLIMSailSchema;
//import com.ontotext.trree.rdf2go.OwlimModel;
//import com.ontotext.trree.rdf2go.OwlimModelSet;
//import com.ontotext.trree.rdf2go.OwlimRepository;
//import com.ontotext.trree.rdf2go.api.Subscriber;
//
//public class BigOwlimInMemoryModelFactory extends AbstractModelFactory {
//	public Model createModel(Properties properties)
//		throws ModelRuntimeException
//	{
//		return new OwlimModel(null, createRepository(properties));
//	}
//	
//	public Model createModel(URI contextURI)
//		throws ModelRuntimeException
//	{
//		return new OwlimModel(contextURI, createRepository(null));
//	}
//	
//	public ModelSet createModelSet(Properties properties)
//		throws ModelRuntimeException
//	{
//		return new OwlimModelSet(createRepository(properties));
//	}
//	
//	private OwlimRepository createRepository(Properties properties)
//		throws ModelRuntimeException
//	{
//		if (properties == null) properties = new Properties();
//		Properties owlimProps = new Properties();
//		
//		// locate the storage folder
//		String storageProperty = properties.getProperty(STORAGE);
//		if (storageProperty == null) {
//			storageProperty = System.getProperty("user.dir") + File.separator + "owlim-storage";
//		}
//		File storageDir = new File(storageProperty);
//		if (! storageDir.exists() && ! storageDir.mkdirs() || ! storageDir.isDirectory())
//			throw new ModelRuntimeException("Invalid storage folder: " + storageProperty);
//		
//		// find out if we need reasoning
//		String ruleset = "empty";
//		String reasoningProperty = properties.getProperty(REASONING);
//		if (reasoningProperty != null) {
//			if (reasoningProperty.toLowerCase().equals("rdfs"))
//				ruleset = "rdfs";
//			else if (reasoningProperty.toLowerCase().equals("owl"))
//				ruleset = "owl-max";
//		}
//		
//		// initialize some reasonable properties
//		owlimProps.setProperty("storage-folder", storageDir.getName());
//		owlimProps.setProperty("ruleset", ruleset);
//		owlimProps.setProperty("console-thread", "false");
//		owlimProps.setProperty("useShutdownHooks", "false");
//		owlimProps.setProperty("repository-type", "in-memory-repository");
//		owlimProps.setProperty("partialRDFS", "true");
//		owlimProps.setProperty("noPersist", "true");
//		owlimProps.setProperty("jobsize", "200");
//
//		boolean isAutoRemove = false;
//		if ("true".equals(properties.get("auto-remove"))) {
//			isAutoRemove = true;
//		}
//		
//		// create a Sail stack
//		SailFactory sailFactory = SailRegistry.getInstance().get("owlim:Sail");
//		SailImplConfig sailConfig = sailFactory.getConfig();
//		
//		// translate properties into OWLIM configuration
//		Graph configGraph = new GraphImpl();
//		ValueFactory vf = configGraph.getValueFactory();
//		Resource configRoot = vf.createBNode();
//		for (Object key : owlimProps.keySet()) {
//			Object value = owlimProps.get(key);
//			if (key instanceof String && value instanceof String) {
//				configGraph.add(configRoot, 
//						vf.createURI(OWLIMSailSchema.NAMESPACE + (String) key), 
//						vf.createLiteral((String) value));
//			}
//		}
//		
//		Sail owlimSail = null;
//
//		// initialize OWLIM configuration with translated props
//		try {
//			sailConfig.parse(configGraph, configRoot);
//			owlimSail = sailFactory.getSail(sailConfig);
//			owlimSail.setDataDir(storageDir.getAbsoluteFile().getParentFile());
//		} catch (SailConfigException e) {
//			throw new ModelRuntimeException(e);
//		}
//		
//		OwlimRepository repository = new OwlimRepository(owlimSail);
//		repository.setAutoRemove(isAutoRemove);
//		return repository;
//	}
//	
//	
//	public static void main(String[] args) {
//		Properties props = new Properties();
//		props.setProperty(REASONING, "OWL");
//		
//		RDF2Go.register("com.ontotext.trree.rdf2go.OwlimModelFactory");
//		
//		Model m = RDF2Go.getModelFactory().createModel(props);
//
//		m.open();
//		
//		URI subs = ((OwlimModel)m).subscribe("SELECT * WHERE {?s ?p ?o}", new Subscriber() {
//			public void matchingStatement(URI subscription, Statement statement) {
//				System.out.println("*** " + subscription + "   " + statement);
//			}
//		});
//		System.out.println(subs);
//		
//		m.addStatement(
//				m.createURI("http://example.com/a"),
//				m.createURI("http://example.com/b"),
//				m.createURI("http://example.com/c")
//		);
//		
//		((OwlimModel)m).unsubscribe(subs);
//		
//		m.addStatement(
//				m.createURI("http://example.com/a1"),
//				m.createURI("http://example.com/b1"),
//				m.createURI("http://example.com/c1")
//		);
//		
//		m.close();
//		m.open();		
//		m.close();
//	}
//}
