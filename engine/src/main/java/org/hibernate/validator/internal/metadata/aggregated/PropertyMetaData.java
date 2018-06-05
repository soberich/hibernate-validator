/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ElementKind;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.PropertyDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.metadata.location.PropertyConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedFieldProperty;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Represents the constraint related meta data for a JavaBeans property.
 * Abstracts from the concrete physical type of the underlying Java element(s)
 * (fields or getter methods).
 * <p>
 * In order to provide a unified access to all JavaBeans constraints also
 * class-level constraints are represented by this meta data type.
 * </p>
 * <p>
 * Identity is solely based on the property name, hence sets and similar
 * collections of this type may only be created in the scope of one Java type.
 * </p>
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class PropertyMetaData extends AbstractConstraintMetaData {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Immutable
	private final Set<Cascadable> cascadables;

	private final ConstraintLocationKind constraintLocationKind;

	private PropertyMetaData(ConstraintLocationKind constraintLocationKind,
			String propertyName,
			Type type,
			Set<MetaConstraint<?>> constraints,
			Set<MetaConstraint<?>> containerElementsConstraints,
			Set<Cascadable> cascadables) {
		super(
				propertyName,
				type,
				constraints,
				containerElementsConstraints,
				!cascadables.isEmpty(),
				!cascadables.isEmpty() || !constraints.isEmpty() || !containerElementsConstraints.isEmpty()
		);

		this.cascadables = CollectionHelper.toImmutableSet( cascadables );
		this.constraintLocationKind = constraintLocationKind;
	}

	@Override
	public PropertyDescriptorImpl asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		// TODO we have one CascadingMetaData per Cascadable but we need only one to provide a view to the
		// Bean Validation metadata API so we pick the first one...
		CascadingMetaData firstCascadingMetaData = cascadables.isEmpty() ? null : cascadables.iterator().next().getCascadingMetaData();

		return new PropertyDescriptorImpl(
				getType(),
				getName(),
				asDescriptors( getDirectConstraints() ),
				asContainerElementTypeDescriptors( getContainerElementsConstraints(), firstCascadingMetaData, defaultGroupSequenceRedefined, defaultGroupSequence ),
				firstCascadingMetaData != null ? firstCascadingMetaData.isCascading() : false,
				defaultGroupSequenceRedefined,
				defaultGroupSequence,
				firstCascadingMetaData != null ? firstCascadingMetaData.getGroupConversionDescriptors() : Collections.emptySet()
		);
	}

	/**
	 * Returns the cascadables of this property, if any. Often, there will be just a single element returned. Several
	 * elements may be returned in the following cases:
	 * <ul>
	 * <li>a property's field has been marked with {@code @Valid} but type-level constraints have been given on the
	 * getter</li>
	 * <li>one type parameter of a property has been marked with {@code @Valid} on the field (e.g. a map's key) but
	 * another type parameter has been marked with {@code @Valid} on the property (e.g. the map's value)</li>
	 * <li>a (shaded) private field in a super-type and another field of the same name in a sub-type are both marked
	 * with {@code @Valid}</li>
	 * </ul>
	 */
	public Set<Cascadable> getCascadables() {
		return cascadables;
	}

	@Override
	public String toString() {
		return "PropertyMetaData [type=" + getType() + ", propertyName=" + getName() + "]]";
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.PROPERTY;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !super.equals( obj ) ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		return this.constraintLocationKind == ( (PropertyMetaData) obj ).constraintLocationKind;
	}

	public static abstract class Builder extends MetaDataBuilder {

		private final String propertyName;
		private final Type propertyType;
		protected final Map<Property, Cascadable.Builder> cascadableBuilders = new HashMap<>();

		public static Builder builder(Class<?> beanClass, ConstrainedElement constrainedElement, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager) {
			if ( constrainedElement instanceof ConstrainedExecutable ) {
				return new GetterBuilder( beanClass, ( (ConstrainedExecutable) constrainedElement ), constraintHelper, typeResolutionHelper, valueExtractorManager );
			}
			else if ( constrainedElement instanceof ConstrainedFieldProperty ) {
				return new FieldBuilder( beanClass, ( (ConstrainedFieldProperty) constrainedElement ), constraintHelper, typeResolutionHelper, valueExtractorManager );
			}
			else {
				throw LOG.getUnexpectedConstraintElementType( constrainedElement.getClass(), ConstrainedExecutable.class, ConstrainedFieldProperty.class );
			}
		}

		protected Builder(Class<?> beanClass, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager, String propertyName, Type propertyType) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractorManager );
			this.propertyName = propertyName;
			this.propertyType = propertyType;
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			if ( !elementAcceptanceTest( constrainedElement ) ) {
				return false;
			}
			return Objects.equals( getPropertyName( constrainedElement ), propertyName );
		}

		protected abstract boolean elementAcceptanceTest(ConstrainedElement constrainedElement);

		@Override
		public void add(ConstrainedElement constrainedElement) {
			super.add( constrainedElement );

			if ( constrainedElement.getCascadingMetaDataBuilder().isMarkedForCascadingOnAnnotatedObjectOrContainerElements() ||
					constrainedElement.getCascadingMetaDataBuilder().hasGroupConversionsOnAnnotatedObjectOrContainerElements() ) {
				addCascadingInformation( constrainedElement );
			}
		}

		protected abstract String getPropertyName(ConstrainedElement constrainedElement);

		@Override
		public PropertyMetaData build() {
			Set<Cascadable> cascadables = cascadableBuilders.values()
					.stream()
					.map( b -> b.build() )
					.collect( Collectors.toSet() );

			return new PropertyMetaData(
					getConstraintLocationKind(),
					propertyName,
					propertyType,
					adaptOriginsAndImplicitGroups( getDirectConstraints() ),
					adaptOriginsAndImplicitGroups( getContainerElementConstraints() ),
					cascadables
			);
		}

		protected abstract ConstraintLocationKind getConstraintLocationKind();

		public abstract void addCascadingInformation(ConstrainedElement constrainedElement);
	}

	private static class FieldBuilder extends Builder {

		public FieldBuilder(Class<?> beanClass, ConstrainedFieldProperty constrainedFieldProperty, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractorManager,
					constrainedFieldProperty.getProperty().getName(), constrainedFieldProperty.getProperty().getType() );
			add( constrainedFieldProperty );
		}

		@Override
		protected boolean elementAcceptanceTest(ConstrainedElement constrainedElement) {
			if ( constrainedElement.getKind() == ConstrainedElementKind.PROPERTY ) {
				if ( !( constrainedElement instanceof ConstrainedFieldProperty ) ) {
					throw LOG.getUnexpectedConstraintElementType( constrainedElement.getClass(), ConstrainedFieldProperty.class );
				}
				return true;
			}

			return false;
		}

		@Override
		protected String getPropertyName(ConstrainedElement constrainedElement) {
			return ( (ConstrainedFieldProperty) constrainedElement ).getProperty().getPropertyName();
		}

		@Override
		protected ConstraintLocationKind getConstraintLocationKind() {
			return ConstraintLocationKind.PROPERTY;
		}

		@Override
		public final void addCascadingInformation(ConstrainedElement constrainedElement) {
			Property property = ( (ConstrainedFieldProperty) constrainedElement ).getProperty();
			Cascadable.Builder builder = cascadableBuilders.get( property );

			if ( builder == null ) {
				builder = new PropertyCascadable.Builder( valueExtractorManager, property, constrainedElement.getCascadingMetaDataBuilder() );
				cascadableBuilders.put( property, builder );
			}
			else {
				builder.mergeCascadingMetaData( constrainedElement.getCascadingMetaDataBuilder() );
			}
		}
	}

	private static class GetterBuilder extends Builder {

		public GetterBuilder(Class<?> beanClass, ConstrainedExecutable constrainedMethod, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractorManager,
					constrainedMethod.getCallable().as( Property.class ).getPropertyName(), constrainedMethod.getCallable().getType() );
			add( constrainedMethod );
		}

		@Override
		protected boolean elementAcceptanceTest(ConstrainedElement constrainedElement) {
			if ( constrainedElement.getKind() == ConstrainedElementKind.METHOD ) {
				if ( !( constrainedElement instanceof ConstrainedExecutable ) ) {
					throw LOG.getUnexpectedConstraintElementType( constrainedElement.getClass(), ConstrainedExecutable.class );
				}
				return ( (ConstrainedExecutable) constrainedElement ).isGetterMethod();
			}

			return false;
		}

		@Override
		protected String getPropertyName(ConstrainedElement constrainedElement) {
			return ( (ConstrainedExecutable) constrainedElement ).getCallable().as( Property.class ).getPropertyName();
		}

		@Override
		protected ConstraintLocationKind getConstraintLocationKind() {
			return ConstraintLocationKind.METHOD;
		}

		@Override
		public final void addCascadingInformation(ConstrainedElement constrainedElement) {
			Property property = ( (ConstrainedExecutable) constrainedElement ).getCallable().as( Property.class );
			Cascadable.Builder builder = cascadableBuilders.get( property );

			if ( builder == null ) {
				builder = new PropertyCascadable.Builder( valueExtractorManager, property, constrainedElement.getCascadingMetaDataBuilder() );
				cascadableBuilders.put( property, builder );
			}
			else {
				builder.mergeCascadingMetaData( constrainedElement.getCascadingMetaDataBuilder() );
			}
		}

		@Override
		protected Set<MetaConstraint<?>> adaptConstraints(ConstrainedElement constrainedElement, Set<MetaConstraint<?>> constraints) {
			if ( constraints.isEmpty() ) {
				return constraints;
			}

			ConstraintLocation getterConstraintLocation = ConstraintLocation.forProperty( ( (ConstrainedExecutable) constrainedElement ).getCallable().as( Property.class ) );

			// convert return value locations into getter locations for usage within this meta-data
			return constraints.stream()
					.map( c -> withGetterLocation( getterConstraintLocation, c ) )
					.collect( Collectors.toSet() );
		}

		private MetaConstraint<?> withGetterLocation(ConstraintLocation getterConstraintLocation, MetaConstraint<?> constraint) {
			ConstraintLocation converted = null;

			// fast track if it's a regular constraint
			if ( !( constraint.getLocation() instanceof TypeArgumentConstraintLocation ) ) {
				// Change the constraint location to a GetterConstraintLocation if it is not already one
				if ( constraint.getLocation() instanceof PropertyConstraintLocation ) {
					converted = constraint.getLocation();
				}
				else {
					converted = getterConstraintLocation;
				}
			}
			else {
				Deque<ConstraintLocation> locationStack = new ArrayDeque<>();

				// 1. collect the hierarchy of delegates up to the root return value location
				ConstraintLocation current = constraint.getLocation();
				do {
					locationStack.addFirst( current );
					if ( current instanceof TypeArgumentConstraintLocation ) {
						current = ( (TypeArgumentConstraintLocation) current ).getDelegate();
					}
					else {
						current = null;
					}
				}
				while ( current != null );

				// 2. beginning at the root, transform each location so it references the transformed delegate
				for ( ConstraintLocation location : locationStack ) {
					if ( !( location instanceof TypeArgumentConstraintLocation ) ) {
						// Change the constraint location to a GetterConstraintLocation if it is not already one
						if ( location instanceof PropertyConstraintLocation ) {
							converted = location;
						}
						else {
							converted = getterConstraintLocation;
						}
					}
					else {
						converted = ConstraintLocation.forTypeArgument(
								converted,
								( (TypeArgumentConstraintLocation) location ).getTypeParameter(),
								location.getTypeForValidatorResolution()
						);
					}
				}
			}

			return MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraint.getDescriptor(), converted );
		}
	}
}
