/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import de.d3web.utilities.ISetMap;
import de.d3web.utilities.SetMap;
import de.d3web.we.alignment.GlobalAlignment;
import de.d3web.we.alignment.LocalAlignment;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.broker.BrokerImpl;
import de.d3web.we.core.dialog.DistributedDialogControl;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.persistence.AlignmentPersistenceHandler;
import de.d3web.we.persistence.FriendlyServiceClusterPersistenceHandler;
import de.d3web.we.persistence.TerminologyPersistenceHandler;
import de.d3web.we.terminology.TerminologyBroker;
import de.d3web.we.terminology.TerminologyServer;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.local.LocalTerminologyAccess;
import de.d3web.we.terminology.term.Term;

public class DPSEnvironment {

	public static final String symptomTerminology = "SymptomTerminology.xml";
	public static final String diagnosisTerminology = "DiagnosisTerminology.xml";
	public static final String localAlignments = "LocalAlignments.xml";
	public static final String globalAlignments = "GlobalAlignments.xml";
	public static final String clustersLocation = "Clusters.xml";
	public static final String metaInfoLocation = "META";
	
	private URL environmentLocation;
	private TerminologyServer terminologyServer;
	private Map<String, de.d3web.we.core.knowledgeService.KnowledgeService> services;
	private ISetMap<String, String> clusters;
	private Map<String, Broker> brokers;
	
	public DPSEnvironment(URL url) {
		super();
		setEnvironmentLocation(url);
		terminologyServer = new TerminologyServer();
		services = new HashMap<String, KnowledgeService>();
		clusters = new SetMap<String, String>();
		brokers = new HashMap<String, Broker>();
		initialize();
	}
	
	public void setEnvironmentLocation(URL url) {
		environmentLocation = url;
	}
	
	private void clear() {
		terminologyServer = new TerminologyServer();
		services = new HashMap<String, KnowledgeService>();
		clusters = new SetMap<String, String>();
		brokers = new HashMap<String, Broker>();
	}
	
	synchronized public void reInitialize() {
		deleteXMLFiles();
		clear();
		initialize();
	}
	
	public void initialize() {
		File dir = null;
		try {
			dir = getMetaDataLocation();
		} catch (URISyntaxException e) {
			Logger.getLogger(getClass().getName()).warning("Error: initialization failed: " + environmentLocation + "\n" + e.getMessage());
		}
		if(dir == null || !dir.isDirectory()) {
			Logger.getLogger(getClass().getName()).warning("Error: initialization failed: " + environmentLocation);
			return;
		}
		try {
			initTerminologies(dir);
			initAlignment(dir);
			initClusters(dir);
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).warning("Error: Cannot initialize: " + dir + " :\n " + e.getMessage());
			e.printStackTrace();
		}
	}

	
	private void initTerminologies(File dir) {
		File symptomGTFile = new File(dir, symptomTerminology);		
		File diagnosisGTFile = new File(dir, diagnosisTerminology);	
		if(!symptomGTFile.exists()) {
			GlobalTerminology gt = createNewTerminology(TerminologyType.symptom);
			Logger.getLogger(getClass().getName()).info("Created global symptom terminology: "+gt.getAllTerms().size()+ " terms");
			
			TerminologyPersistenceHandler.getInstance().saveSymptomTerminology(gt, symptomGTFile);
		} else {
			GlobalTerminology gt= TerminologyPersistenceHandler.getInstance().loadSymptomTerminology(symptomGTFile);
			terminologyServer.setGlobalTerminology(TerminologyType.symptom, gt);
		}
		
		if(!diagnosisGTFile.exists()) {
			GlobalTerminology gt = createNewTerminology(TerminologyType.diagnosis);
			Logger.getLogger(getClass().getName()).info("Created global diagnosis terminology: "+gt.getAllTerms().size()+ " terms");
			
			TerminologyPersistenceHandler.getInstance().saveSolutionTerminology(gt, diagnosisGTFile);
		} else {
			GlobalTerminology gt= TerminologyPersistenceHandler.getInstance().loadSolutionTerminology(diagnosisGTFile);
			terminologyServer.setGlobalTerminology(TerminologyType.diagnosis, gt);
		}
	}

	private GlobalTerminology createNewTerminology(TerminologyType type) {
		for (KnowledgeService each : services.values()) {
			Map<TerminologyType, LocalTerminologyAccess> map = each.getTerminologies();
			terminologyServer.addTerminology(each.getId(), type, map.get(type));
		}
		GlobalTerminology result = terminologyServer.getGlobalTerminology(type);
		if(result == null) {
			result = new GlobalTerminology(type);
		}
		return result;
	}

	private void initAlignment(File dir) throws Exception {
		File localAlignmentFile = new File(dir, localAlignments);
		File globalAlignmentFile = new File(dir, globalAlignments);	
		
		if(!localAlignmentFile.exists()) {
			Collection<LocalAlignment> local = terminologyServer.createLocalAlignments();
			terminologyServer.setLocalAlignments(local);
			AlignmentPersistenceHandler.getInstance().saveLocalAlignments(terminologyServer.getLocalAlignments(), localAlignmentFile.toURI().toURL());
		} else {
			Collection<LocalAlignment> local = null;
			try {
				local = AlignmentPersistenceHandler.getInstance().loadLocalAlignment(localAlignmentFile.toURI().toURL(), terminologyServer);
			} catch (MalformedURLException e) {
				Logger.getLogger(getClass().getName()).warning("Error: Cannot parse: " + localAlignmentFile + " :\n " + e.getMessage());
			}
			if(local != null) {
				terminologyServer.setLocalAlignments(local);
			}
		}
		
		if(!globalAlignmentFile.exists()) {
			Collection<GlobalAlignment> global = terminologyServer.createGlobalAlignments();
			terminologyServer.setGlobalAlignments(global);
			AlignmentPersistenceHandler.getInstance().saveGlobalAlignments(terminologyServer.getGlobalAlignments(), globalAlignmentFile.toURI().toURL());
		} else {
			Collection<GlobalAlignment> global = null;
			try {
				global = AlignmentPersistenceHandler.getInstance().loadGlobalAlignment(globalAlignmentFile.toURI().toURL(), terminologyServer);
			} catch (MalformedURLException e) {
				Logger.getLogger(getClass().getName()).warning("Error: Cannot parse: " + globalAlignmentFile + " :\n " + e.getMessage());
			}
			if(global != null) {
				terminologyServer.setGlobalAlignments(global);
			}
		}
	}

	private void initClusters(File dir) throws Exception {
		File clustersFile = new File(dir, clustersLocation);
		if(clustersFile.exists()) {
			clusters = FriendlyServiceClusterPersistenceHandler.getInstance().loadClusterInformation(clustersFile.toURI().toURL());
		}
	}
	
	public synchronized void saveAll(boolean threaded) {
		if(threaded) {
			new Thread(new Runnable() {
				public void run() {
					saveAll();
				}
			}).start();
		} else {
			saveAll();
		}
	}
	
	
	private void saveAll() {
		try {
			File dir = getMetaDataLocation();
			File symptomGTFile = new File(dir, symptomTerminology);		
			File diagnosisGTFile = new File(dir, diagnosisTerminology);	
			File localAlignmentFile = new File(dir, localAlignments);
			File globalAlignmentFile = new File(dir, globalAlignments);	
			File clustersFile = new File(dir, clustersLocation);
			TerminologyPersistenceHandler.getInstance().saveSymptomTerminology(terminologyServer.getGlobalTerminology(TerminologyType.symptom), symptomGTFile);
			TerminologyPersistenceHandler.getInstance().saveSolutionTerminology(terminologyServer.getGlobalTerminology(TerminologyType.diagnosis), diagnosisGTFile);
			AlignmentPersistenceHandler.getInstance().saveLocalAlignments(terminologyServer.getLocalAlignments(), localAlignmentFile.toURI().toURL());
			AlignmentPersistenceHandler.getInstance().saveGlobalAlignments(terminologyServer.getGlobalAlignments(), globalAlignmentFile.toURI().toURL());
			FriendlyServiceClusterPersistenceHandler.getInstance().saveClusterInformation(clusters, clustersFile.toURI().toURL());
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).warning("Error: Cannot save: " + e.getMessage());
		}
	}

	private File getMetaDataLocation() throws URISyntaxException {
		File dir = new File(new File(environmentLocation.toURI()), metaInfoLocation);
		dir.mkdirs();
		return dir;
	}
	
	private void deleteXMLFiles() {
		File dir = null;
		try {
			dir = getMetaDataLocation();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(dir != null) {
			Set<File> files = new HashSet<File>();
			files.add(new File(dir, symptomTerminology));		
			files.add(new File(dir, diagnosisTerminology));	
			files.add(new File(dir, localAlignments));
			files.add(new File(dir, globalAlignments));
			for (File file : files) {
				if(file != null) {
					if(file.delete()) {
					Logger.getLogger(getClass().getName()).warning("Note: Deleting XML-file: " + file.toString());
					} else {
						Logger.getLogger(getClass().getName()).warning("ERROR: Cannot delete XML-file: " + file.toString());
						
					}
				}
			}
			
			//File clustersFile = new File(dir, clustersLocation);
		}
	}
	
	public void addService(KnowledgeService service, String clusterID, boolean initialize, boolean useDPS) {
		KnowledgeService oldService = getService(service.getId());
		if(oldService != null) {
			removeService(oldService);
		}
		
		if(clusterID != null && !clusterID.trim().equals("")) {
			clusters.removeAll(clusters.keySet(), service.getId());
			clusters.add(clusterID, service.getId());
			File dir;
			try {
				dir = getMetaDataLocation();
				File clustersFile = new File(dir, clustersLocation);
				FriendlyServiceClusterPersistenceHandler.getInstance().saveClusterInformation(clusters, clustersFile.toURI().toURL());
			} catch (Exception e) {
				
			}
		}
		services.put(service.getId(), service);

		for (TerminologyType eachType : service.getTerminologies().keySet()) {
			LocalTerminologyAccess eachAccess = service.getTerminologies().get(eachType);
			terminologyServer.getStorage().register(service.getId(), eachType, eachAccess);
			if (initialize) {
				TerminologyBroker tb = terminologyServer.getBroker();
				ISetMap<Object, Term> map = tb.addTerminology(eachType, eachAccess,
						service.getId(), terminologyServer.getStorage());

				Collection<LocalAlignment> las = tb.alignLocal(eachAccess, service.getId(),
						terminologyServer.getStorage());

				tb.addLocalAlignments(las);

				if (useDPS) {
					Collection<GlobalAlignment> gas = tb.alignGlobal(eachAccess, service.getId(),
							terminologyServer.getStorage());
					tb.addGlobalAlignments(gas);
					saveAll();
				}

			}
		}
	}
	
	public void removeService(KnowledgeService service) {
		Map<TerminologyType, LocalTerminologyAccess> map = service.getTerminologies();
		for (TerminologyType type : new ArrayList<TerminologyType>(map.keySet())) {
			terminologyServer.removeTerminology(service.getId(), type);
		}
		services.remove(service.getId());
	}
	
	public Broker createBroker(String userID) {
		Broker result = new BrokerImpl(this, userID, new DistributedDialogControl());
		for (KnowledgeService each : services.values()) {
			result.register(each);
		}
		return result;
	}
	
	public KnowledgeService getService(String id) {
		return services.get(id);
	}
	
	public Collection<KnowledgeService> getServices() {
		return services.values();
	}
	
	public String getCluster(String serviceId) {
		for (String eachCluster : clusters.keySet()) {
			if(clusters.get(eachCluster).contains(serviceId)) {
				return eachCluster;
			}
		}
		return null;
	}
	
	public Collection<String> getClusters() {
		return clusters.keySet();
	}
	
	public Collection<String> getFriendlyServices(String serviceId) {
		String cluster = getCluster(serviceId);
		if(cluster == null) return new ArrayList<String>();
		return clusters.get(cluster);
	}
	
	public Collection<KnowledgeServiceSession> createServiceSessions(Broker broker) {
		Collection<KnowledgeServiceSession> result = new ArrayList<KnowledgeServiceSession>();
		for (String id : services.keySet()) {
			result.add(createServiceSession(id, broker));
		}
		return result;
	}
	
	public KnowledgeServiceSession createServiceSession(String id, Broker broker) {
		KnowledgeService service = services.get(id);
		if(service != null) {
			return service.createSession(broker);
		}
		return null;
	}

	public Collection<Information> getAlignedInformation(Information info) {
		return terminologyServer.getAlignedInformation(info);
	}

	public TerminologyServer getTerminologyServer() {
		return terminologyServer;
	}

	public Broker getBroker(String userID) {
		Broker result = brokers.get(userID);
		if(result == null) {
			Broker broker = createBroker(userID);
			brokers.put(userID, broker);
			result = broker;
		}
		return result;
	}
	
	public Collection<Broker> getBrokers() {
		return brokers.values();
	}
	
	public void remove(String userID) {
		brokers.remove(userID);
	}

	public URL getEnvironmentLocation() {
		return environmentLocation;
	}
	
	
}
