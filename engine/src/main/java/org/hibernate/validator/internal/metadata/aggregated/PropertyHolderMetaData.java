/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.util.List;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Validatable;

/**
 * Interface defining the meta data about the constraints defined in a given property holder.
 *
 * @author Marko Bekhta
 */
public interface PropertyHolderMetaData extends Validatable {

	/**
	 * @return the mapping name for a property holder.
	 */
	String getMappingName();

	/**
	 * @return {@code true} if the mapping has any constraints at all, {@code false} otherwise.
	 */
	boolean hasConstraints();

	/**
	 * Get the composition of the default group sequence.
	 *
	 * @return a list of classes representing the default group sequence.
	 */
	List<Class<?>> getDefaultGroupSequence();

	/**
	 * @return A set of {@code MetaConstraint} instances encapsulating the information of all the constraints
	 * 		defined for the corresponding property holder mapping.
	 */
	Set<MetaConstraint<?>> getMetaConstraints();

}
