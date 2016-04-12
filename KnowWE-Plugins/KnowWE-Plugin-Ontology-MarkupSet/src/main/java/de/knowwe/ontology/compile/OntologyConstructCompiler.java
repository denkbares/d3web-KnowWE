/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.ontology.compile;

import de.knowwe.core.compile.AbstractPackageCompiler;
import de.knowwe.core.compile.ParallelScriptCompiler;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.notification.NotificationManager;
import de.knowwe.notification.StandardNotification;
import de.knowwe.ontology.sparql.SparqlContentType;
import de.knowwe.ontology.sparql.SparqlType;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 30.04.15.
 */
public class OntologyConstructCompiler extends AbstractPackageCompiler implements Rdf2GoCompiler {

    private OntologyCompiler ontologyCompiler;
    private ParallelScriptCompiler<OntologyConstructCompiler> scriptCompiler;
    private ParallelScriptCompiler<OntologyConstructCompiler> destroyScriptCompiler;

    public OntologyConstructCompiler(OntologyCompiler ontologyCompiler) {
        super(ontologyCompiler.getPackageManager(), ontologyCompiler.getCompileSection(), OntologyType.class);
        this.ontologyCompiler = ontologyCompiler;
        destroyScriptCompiler = new ParallelScriptCompiler<>(this);
        scriptCompiler = new ParallelScriptCompiler<>(this);
    }


    @Override
    public void compilePackages(String[] packagesToCompile) {


        Collection<Section<?>> sectionsOfPackage;

        /*
        removed deleted constructs
         */
        sectionsOfPackage = getPackageManager().getRemovedSections(packagesToCompile);
        Collection<Section<?>> constructSectionsRemoved = filterConstruct(sectionsOfPackage);
        for (Section<?> section : constructSectionsRemoved) {
            destroyScriptCompiler.addSubtree(section);
        }

        /*
        remove existing constructs
         */
        sectionsOfPackage = getPackageManager().getSectionsOfPackage(packagesToCompile);
        Collection<Section<?>> constructSections = filterConstruct(sectionsOfPackage);
        for (Section<?> section : constructSections) {
            destroyScriptCompiler.addSubtree(section);
        }
        destroyScriptCompiler.destroy();

        /*
        insert all existing constructs again
         */
        for (Section<?> section : sectionsOfPackage) {
            scriptCompiler.addSubtree(section);
        }
        scriptCompiler.compile();


        if (!sectionsOfPackage.isEmpty() && OntologyCompiler.getCommitType(ontologyCompiler) == CommitType.onDemand) {
            NotificationManager.addGlobalNotification(new StandardNotification("There are changes not yet committed to " +
                    "the ontology repository. Committing may take some time. If you want to commit the changes now, " +
                    "click <a onclick=\"KNOWWE.plugin.ontology.commitOntology('" + getCompileSection().getID() + "')\">here</a>.",
                    Message.Type.INFO, OntologyCompiler.getCommitNotificationId(ontologyCompiler)));
        } else {
            OntologyCompiler.commitOntology(ontologyCompiler);
        }


        destroyScriptCompiler = new ParallelScriptCompiler<>(this);
        scriptCompiler = new ParallelScriptCompiler<>(this);
    }


    private Collection<Section<?>> filterConstruct(Collection<Section<?>> sectionsOfPackage) {
        return sectionsOfPackage.stream().filter(x -> x.get() instanceof SparqlType).filter(x -> SparqlContentType.isConstructQuery(DefaultMarkupType.getContentSection(x))).collect(Collectors.toSet());
    }

    @Override
    public Rdf2GoCore getRdf2GoCore() {
        return ontologyCompiler.getRdf2GoCore();
    }

    @Override
    public String getArticleName() {
        return ontologyCompiler.getArticleName();
    }

    @Override
    public TerminologyManager getTerminologyManager() {
        return ontologyCompiler.getTerminologyManager();
    }

    @Override
    public void destroy() {
        // handled by OntologyCompiler
    }
}