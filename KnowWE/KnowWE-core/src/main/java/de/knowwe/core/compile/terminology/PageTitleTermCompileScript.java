/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.core.compile.terminology;

import de.d3web.strings.Identifier;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.RootType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;

import java.util.Collection;

/**
 * Created by jochenreutelshofer on 21.08.14.
 */
public class PageTitleTermCompileScript extends DefaultGlobalCompiler.DefaultGlobalScript<RootType> {

    @Override
    public void compile(DefaultGlobalCompiler compiler, Section<RootType> section) throws CompilerMessage {
        final TerminologyManager terminologyManager = compiler.getTerminologyManager();
        final Identifier termIdentifier = new Identifier(section.getTitle());
        terminologyManager.registerTermDefinition(compiler, section, Article.class, termIdentifier);
        Collection<Section<?>> termReferenceSections = terminologyManager.getTermReferenceSections(termIdentifier);
        Compilers.addSectionsToCompile(compiler, termReferenceSections);
    }

    @Override
    public void destroy(DefaultGlobalCompiler compiler, Section<RootType> section) {
        final TerminologyManager terminologyManager = compiler.getTerminologyManager();
        final Identifier termIdentifier = new Identifier(section.getTitle());
        terminologyManager.unregisterTermDefinition(compiler, section, Article.class, termIdentifier);
        Collection<Section<?>> termReferenceSections = terminologyManager.getTermReferenceSections(termIdentifier);
        Compilers.addSectionsToDestroyAndCompile(compiler, termReferenceSections);
    }

  }
