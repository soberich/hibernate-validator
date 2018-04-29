/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.ElementKind;
import javax.validation.groups.Default;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedProperty;
import org.hibernate.validator.internal.metadata.raw.PropertyHolderConfiguration;
import org.hibernate.validator.internal.properties.propertyholder.PropertyHolder;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Marko Bekhta
 */
public final class PropertyHolderMetaDataImpl implements PropertyHolderMetaData {

	/**
	 * Represents the "sequence" of just Default.class.
	 */
	private static final List<Class<?>> DEFAULT_GROUP_SEQUENCE = Collections.singletonList( Default.class );

	/**
	 * Whether there are any constraints or cascades at all.
	 */
	private final boolean hasConstraints;

	/**
	 * The mapping name for this meta data.
	 */
	private final String mappingName;

	/**
	 * Set of all constraints for this mapping.
	 */
	@Immutable
	private final Set<MetaConstraint<?>> metaConstraints;

	/**
	 * The cascaded properties of this bean.
	 */
	@Immutable
	private final Set<Cascadable> cascadedProperties;

	/**
	 * The default groups sequence for this bean class.
	 */
	@Immutable
	private final List<Class<?>> defaultGroupSequence;

	private PropertyHolderMetaDataImpl(String mappingName,
			List<Class<?>> defaultGroupSequence,
			Set<ConstraintMetaData> constraintMetaDataSet) {

		this.mappingName = mappingName;

		Set<PropertyMetaData> propertyMetaDataSet = newHashSet();

		boolean hasConstraints = false;

		for ( ConstraintMetaData constraintMetaData : constraintMetaDataSet ) {
			boolean elementHasConstraints = constraintMetaData.isCascading() || constraintMetaData.isConstrained();
			hasConstraints |= elementHasConstraints;

			if ( constraintMetaData.getKind() == ElementKind.PROPERTY ) {
				propertyMetaDataSet.add( (PropertyMetaData) constraintMetaData );
			}
		}

		Set<Cascadable> cascadedProperties = newHashSet();
		Set<MetaConstraint<?>> allMetaConstraints = newHashSet();

		for ( PropertyMetaData propertyMetaData : propertyMetaDataSet ) {
			cascadedProperties.addAll( propertyMetaData.getCascadables() );
			allMetaConstraints.addAll( propertyMetaData.getAllConstraints() );
		}

		this.hasConstraints = hasConstraints;
		this.cascadedProperties = CollectionHelper.toImmutableSet( cascadedProperties );
		this.metaConstraints = CollectionHelper.toImmutableSet( allMetaConstraints );

		this.defaultGroupSequence = CollectionHelper.toImmutableList(
				defaultGroupSequence != null && !defaultGroupSequence.isEmpty() ? defaultGroupSequence : DEFAULT_GROUP_SEQUENCE
		);

	}

	@Override
	public String getMappingName() {
		return mappingName;
	}

	@Override
	public boolean hasConstraints() {
		return hasConstraints;
	}

	@Override
	public Set<Cascadable> getCascadables() {
		return cascadedProperties;
	}

	@Override
	public boolean hasCascadables() {
		return !cascadedProperties.isEmpty();
	}

	@Override
	public Set<MetaConstraint<?>> getMetaConstraints() {
		return metaConstraints;
	}

	@Override
	public List<Class<?>> getDefaultGroupSequence() {
		return defaultGroupSequence;
	}

	public static class PropertyHolderMetaDataBuilder {

		private final ConstraintHelper constraintHelper;
		private final TypeResolutionHelper typeResolutionHelper;
		private final ValueExtractorManager valueExtractorManager;

		private PropertyHolderMetaDataBuilder(
				ConstraintHelper constraintHelper,
				TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager) {
			this.constraintHelper = constraintHelper;
			this.typeResolutionHelper = typeResolutionHelper;
			this.valueExtractorManager = valueExtractorManager;
		}

		public static PropertyHolderMetaDataBuilder getInstance(
				ConstraintHelper constraintHelper,
				TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager) {
			return new PropertyHolderMetaDataBuilder( constraintHelper, typeResolutionHelper, valueExtractorManager );
		}

		public PropertyHolderMetaDataImpl build(PropertyHolderConfiguration configuration) {

			Set<ConstraintMetaData> aggregatedElements = newHashSet();

			for ( ConstrainedElement constrainedElement : configuration.getConstrainedElements() ) {
				if ( ConstrainedElementKind.PROPERTY.equals( constrainedElement.getKind() ) ) {
					aggregatedElements.add(
							new PropertyMetaData.Builder(
									PropertyHolder.class,
									(ConstrainedProperty) constrainedElement,
									constraintHelper,
									typeResolutionHelper,
									valueExtractorManager
							).build()
					);
				}
//				else {
//					// if it's not a property than it should be another property holder ?
//				}
			}

			return new PropertyHolderMetaDataImpl(
					configuration.getMappingName(),
					configuration.getDefaultGroupSequence(),
					aggregatedElements
			);
		}
	}
}
