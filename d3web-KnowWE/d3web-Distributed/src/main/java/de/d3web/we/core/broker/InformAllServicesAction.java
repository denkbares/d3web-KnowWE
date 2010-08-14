/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.core.broker;

import java.util.ArrayList;
import java.util.Collection;

import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.terminology.term.Term;

public class InformAllServicesAction implements ServiceAction {

	private final Information info;
	private final DPSEnvironment environment;
	private final DPSSession session;

	public InformAllServicesAction(Information info, DPSSession session, DPSEnvironment environment) {
		super();
		this.environment = environment;
		this.info = info;
		this.session = session;
	}

	public void run() {
		if (info.getInformationType().equals(InformationType.ClusterInformation)) {
			Collection<Information> alignedInfos = new ArrayList<Information>();
			Term term = environment.getTerminologyServer().getGlobalTerminology(
					info.getTerminologyType()).getTerm(info.getObjectID(), null);
			for (IdentifiableInstance each : environment.getTerminologyServer().getBroker().getAlignedIdentifiableInstances(
					term)) {
				alignedInfos.add(new Information(each.getNamespace(), each.getObjectId(),
						info.getValues(), info.getTerminologyType(),
						InformationType.SolutionInformation));
			}
			informAll(alignedInfos);
		}
		else {
			Collection<Information> alignedInfos = environment.getAlignedInformation(info);
			informAll(alignedInfos);
		}
	}

	private void informAll(Collection<Information> alignedInfos) {
		for (Information information : alignedInfos) {
			if (!information.getNamespace().equals(info.getNamespace())
					&& checkInfo(info, information, environment)) {
				new InformServiceAction(information, session).run();
			}
		}
	}

	private boolean checkInfo(Information origin, Information target, DPSEnvironment environment) {
		if (origin.getInformationType().equals(InformationType.HeuristicInferenceInformation)
				|| origin.getInformationType().equals(
						InformationType.SetCoveringInferenceInformation)
				|| origin.getInformationType().equals(InformationType.XCLInferenceInformation)
				|| origin.getInformationType().equals(InformationType.CaseBasedInferenceInformation)) {
			if (origin.getTerminologyType().equals(TerminologyType.diagnosis)) {
				return environment.getFriendlyServices(origin.getNamespace()).contains(
						target.getNamespace());
			}
		}
		return true;
	}

	public final Information getInfo() {
		return info;
	}

}
