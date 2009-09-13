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

package de.d3web.we.core.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.SolutionState;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.broker.BrokerActionListener;
import de.d3web.we.core.broker.BrokerImpl;
import de.d3web.we.core.broker.InformAllServicesAction;
import de.d3web.we.core.broker.ServiceAction;
import de.d3web.we.persistence.SessionPersistenceHandler;
import de.d3web.we.terminology.term.Term;

public class BatchRun implements BrokerActionListener {

	public static void main(String[] args) {
		File batchFile1 = null;
		File batchFile2 = null;
		File batchFile3 = null;
		File batchFile4 = null;
		try {
			batchFile1 = new File(BatchRun.class.getClassLoader().getResource("batch/pubs/pubs.batch").toURI());
			batchFile2 = new File(BatchRun.class.getClassLoader().getResource("batch/fitness/fitness.batch").toURI());
			batchFile3 = new File(BatchRun.class.getClassLoader().getResource("batch/bad_align_tests/bad_align_tests.batch").toURI());
			batchFile4 = new File(BatchRun.class.getClassLoader().getResource("batch/cluster/cluster.batch").toURI());
		} catch (Exception e) {
		}
		try {
			//new BatchRun().run(batchFile1);
			//new BatchRun().run(batchFile2);
			//new BatchRun().run(batchFile3);
			new BatchRun().run(batchFile4);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private void run(File batchFile) throws Exception {
		BrokerImpl broker = null;
		List<Information> infos = new LinkedList<Information>(); 
		File sessionFile = new File(batchFile.getParentFile(), "session.xml");
		infos = extractInfomation(batchFile);
		
		System.out.println("### try with processInit():");
		broker = setup(batchFile);
		broker.getSession().getBlackboard().setAllInformation(infos);
		broker.processInit();
		System.out.println("############################################");
		for (Term term : broker.getSession().getBlackboard().getGlobalSolutions().keySet()) {
			SolutionState state = broker.getSession().getBlackboard().getGlobalSolutions().get(term);
			System.out.println(term + " : " + state);
		}
		System.out.println("############################################");
		System.out.println();
		System.out.println();
		SessionPersistenceHandler.getInstance().saveSession(broker, sessionFile.toURI().toURL());
		
		/*
		System.out.println("### Save and try again with processInit():");
		broker = setup(batchFile);
		infos = SessionPersistenceHandler.getInstance().loadSession(broker, sessionFile.toURI().toURL());
		broker.getSession().getBlackboard().setAllInformation(infos);
		broker.processInit();
		System.out.println("############################################");
		for (Term term : broker.getSession().getBlackboard().getGlobalSolutions().keySet()) {
			SolutionState state = broker.getSession().getBlackboard().getGlobalSolutions().get(term);
			System.out.println(term + " : " + state);
		}
		System.out.println("############################################");
		System.out.println();
		System.out.println();
		System.out.println("### try again with same broker (cleared) with processInit():");
		broker.clearDPSSession();
		infos = SessionPersistenceHandler.getInstance().loadSession(broker, sessionFile.toURI().toURL());
		broker.getSession().getBlackboard().setAllInformation(infos);
		broker.processInit();
		System.out.println("############################################");
		for (Term term : broker.getSession().getBlackboard().getGlobalSolutions().keySet()) {
			SolutionState state = broker.getSession().getBlackboard().getGlobalSolutions().get(term);
			System.out.println(term + " : " + state);
		}
		System.out.println("############################################");
		
		
		System.out.println("### Save and try again with update():");
		SessionPersistenceHandler.getInstance().saveSession(broker, sessionFile.toURI().toURL());
		broker = setup(batchFile);
		infos = SessionPersistenceHandler.getInstance().loadSession(broker, sessionFile.toURI().toURL());
		for (Information info : infos) {
			broker.update(info);
		}
		System.out.println("############################################");
		for (Term term : broker.getSession().getBlackboard().getGlobalSolutions().keySet()) {
			SolutionState state = broker.getSession().getBlackboard().getGlobalSolutions().get(term);
			System.out.println(term + " : " + state);
		}
		System.out.println("############################################");
		*/
	}


	public void actionPerformed(ServiceAction action) {
		if(action instanceof InformAllServicesAction) {
			InformAllServicesAction iasa = (InformAllServicesAction) action;
			System.out.println(iasa.getInfo());
		}
	}

	private BrokerImpl setup(File batchFile) throws MalformedURLException {
		DPSEnvironment dpse = new DPSEnvironment(batchFile.getParentFile().toURI().toURL());
		BrokerImpl broker = (BrokerImpl) dpse.createBroker("Batch");
		broker.addActionListener(this);
		return broker;
	}
	
	private List<Information> extractInfomation(File batchFile) throws IOException {
		List<Information> result = new LinkedList<Information>();
		
		BufferedReader br = new BufferedReader(new FileReader(batchFile));
		String line;
		int lineNumer = 0;
		while((line = br.readLine()) != null) {
			lineNumer++;
			if(!line.startsWith("#")) {
				String[] parts = line.split("\\s");
				if(parts.length == 3) {
					List values = getValues(parts[2]);
					result.add(new Information(parts[0], parts[1], values, TerminologyType.symptom, InformationType.ExternalInformation));
				} else { 
					System.out.println("Line : " + lineNumer + " is not parsable");
				}
			}
		}
		return result;
	}


	private List getValues(String input) {
		List result = new LinkedList();
		for (String each : input.split(", ")) {
			Object value = null;
			try {
				value = Double.parseDouble(each);
			} catch (Exception e) {
				value = each;
			}
			if(value != null) {
				result.add(value);
			}
		}
		return result;
	}
	
}
