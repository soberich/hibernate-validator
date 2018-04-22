/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.util.List;
import java.util.Set;

import org.hibernate.validator.internal.util.CollectionHelper;

/**
 * Represents the complete constraint related configuration of one property holder
 * originating from one {@link ConfigurationSource}. Contains meta data on
 * property constraints as well as meta data on default group sequences.
 *
 * @author Marko Bekhta
 */
public class PropertyHolderConfiguration {

	private final ConfigurationSource source;

	private final String propertyHolderMappingName;

	private final Set<ConstrainedElement> constrainedElements;

	private final List<Class<?>> defaultGroupSequence;

	/**
	 * Creates a new property holder configuration.
	 *
	 * @param source The source of this configuration.
	 * @param propertyHolderMappingName The mapping name defining this configuration.
	 * @param constrainedElements The constraint elements representing this property holder properties.
	 * @param defaultGroupSequence The default group sequence for the given type as configured by
	 * 		the given configuration source.
	 */
	public PropertyHolderConfiguration(
			ConfigurationSource source,
			String propertyHolderMappingName,
			Set<? extends ConstrainedElement> constrainedElements,
			List<Class<?>> defaultGroupSequence) {

		this.source = source;
		this.propertyHolderMappingName = propertyHolderMappingName;
		this.constrainedElements = CollectionHelper.newHashSet( constrainedElements );
		this.defaultGroupSequence = defaultGroupSequence;
	}

	public ConfigurationSource getSource() {
		return source;
	}

	public String getMappingName() {
		return propertyHolderMappingName;
	}

	public Set<ConstrainedElement> getConstrainedElements() {
		return constrainedElements;
	}

	public List<Class<?>> getDefaultGroupSequence() {
		return defaultGroupSequence;
	}

	@Override public String toString() {
		final StringBuilder sb = new StringBuilder( "PropertyHolderConfiguration{" );
		sb.append( "source=" ).append( source );
		sb.append( ", propertyHolderMappingName='" ).append( propertyHolderMappingName ).append( '\'' );
		sb.append( ", constrainedElements=" ).append( constrainedElements );
		sb.append( ", defaultGroupSequence=" ).append( defaultGroupSequence );
		sb.append( '}' );
		return sb.toString();
	}

	@Override public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		PropertyHolderConfiguration that = (PropertyHolderConfiguration) o;

		if ( source != that.source ) {
			return false;
		}
		return propertyHolderMappingName.equals( that.propertyHolderMappingName );
	}

	@Override public int hashCode() {
		int result = source.hashCode();
		result = 31 * result + propertyHolderMappingName.hashCode();
		return result;
	}
}
