/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.aggregated;

import javax.validation.constraints.NotNull;

/**
 * @author Marko Bekhta
 */
public interface HierarchyWithBadConstraints<T> {

	class WebServiceImpl extends AbstractWebService implements ExtendedWebService {

	}

	abstract class AbstractWebService implements WebService {

		@Override
		public int getEntityVersion(Long id) {
			return id.intValue();
		}
	}

	interface ExtendedWebService extends WebService {

		@Override
		int getEntityVersion(Long id);
	}

	interface WebService {

		int getEntityVersion(@NotNull Long id);
	}
}
