<%--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
--%>
<%--
    KnowWE overrides the Haddock search page with the section based search.

    Deliberately just a mount point: everything else lives in KnowWE-Plugin-Search.js, which talks to
    /action/WikiSearchAction. Keeping this file thin means the search can be changed without touching a JSP.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setLocale value="${prefs.Language}" />
<fmt:setBundle basename="templates.default"/>

<div class="page-content">
	<div id="knowwe-search" data-placeholder="<fmt:message key='find.input'/>"></div>
	<noscript>
		<p class="warning">Die Wiki-Suche braucht JavaScript.</p>
	</noscript>
</div>
