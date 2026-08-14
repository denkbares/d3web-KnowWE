/*
 * Copyright (C) 2026 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.renderer;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;

/**
 * Implemented by markup types that display their content as a {@link GroupedFilterListSectionsRenderer}. Separating the
 * construction of the list from rendering it allows to rebuild the very same list definition in a subsequent request,
 * which is required to offer per-column filters: {@link ListSectionsFilterProviderAction} needs the value accessors of
 * the columns to collect the values available for filtering.
 * <p>
 * Implementations should build the complete list, without applying any user-specific filtering or pagination. Both is
 * applied by {@link GroupedFilterListSectionsRenderer#render(de.knowwe.core.kdom.rendering.RenderResult)}, based on the
 * settings of the current user.
 * <p>
 * Note that per-column filters are only offered for markups implementing this interface.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 2026-08-14
 */
public interface ListSectionsProvider {

	/**
	 * Builds the list definition of the specified section, without rendering it. If there is nothing to display, an
	 * empty list has to be returned, which renders its placeholder text. Note that this method is also called outside
	 * of any rendering, so it must not depend on anything the markup's renderer does beforehand.
	 *
	 * @param self    the markup section to build the list for
	 * @param context the user context of the current request
	 * @return the list definition, never null
	 */
	@NotNull
	GroupedFilterListSectionsRenderer<?> buildList(Section<?> self, UserContext context);
}
