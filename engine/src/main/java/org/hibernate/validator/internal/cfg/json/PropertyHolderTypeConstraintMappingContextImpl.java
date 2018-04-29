/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.json;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.cfg.propertyholder.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.TypeConstraintMappingContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.PropertyHolderConfiguration;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.propertyholder.ComplexPropertyHolderProperty;
import org.hibernate.validator.internal.properties.propertyholder.SimpleTypePropertyHolderProperty;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Constraint mapping creational context which allows to configure the class-level constraints for one bean.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Marko Bekhta
 */
public final class PropertyHolderTypeConstraintMappingContextImpl extends PropertyHolderConstraintMappingContextImplBase
		implements TypeConstraintMappingContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final String propertyHolderMappingName;

	private final Set<PropertyConstraintMappingContextImpl> propertyContexts = newHashSet();
	private final Set<PropertyConstraintMappingContextImpl> propertyHolderContexts = newHashSet();
	private final Set<Constrainable> configuredMembers = newHashSet();

	private List<Class<?>> defaultGroupSequence;

	PropertyHolderTypeConstraintMappingContextImpl(PropertyHolderConstraintMappingImpl mapping, String propertyHolderMappingName) {
		super( mapping );
		this.propertyHolderMappingName = propertyHolderMappingName;
	}

	@Override
	public TypeConstraintMappingContext defaultGroupSequence(Class<?>... defaultGroupSequence) {
		this.defaultGroupSequence = Arrays.asList( defaultGroupSequence );
		return this;
	}

	@Override
	public PropertyConstraintMappingContext property(String property, Class<?> propertyType) {
		Contracts.assertNotNull( property, "The property name must not be null." );
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		Property constrainable = new SimpleTypePropertyHolderProperty( property, propertyType );
		if ( configuredMembers.contains( constrainable ) ) {
			throw LOG.getPropertyHolderMappingPropertyHasAlreadyBeenConfiguredViaProgrammaticApiException( propertyHolderMappingName, property );
		}

		PropertyConstraintMappingContextImpl context = new PropertyConstraintMappingContextImpl(
				this,
				constrainable
		);

		configuredMembers.add( constrainable );
		propertyContexts.add( context );
		return context;
	}

	@Override
	public PropertyConstraintMappingContext propertyHolder(String property, String mappingName) {
		Contracts.assertNotNull( property, "The property name must not be null." );
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		Property constrainable = new ComplexPropertyHolderProperty( property, mappingName );
		if ( configuredMembers.contains( constrainable ) ) {
			throw LOG.getPropertyHolderMappingPropertyHasAlreadyBeenConfiguredViaProgrammaticApiException( propertyHolderMappingName, property );
		}

		PropertyConstraintMappingContextImpl context = new PropertyConstraintMappingContextImpl(
				this,
				constrainable
		);

		configuredMembers.add( constrainable );
		propertyHolderContexts.add( context );
		return context;
	}

	PropertyHolderConfiguration build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		return new PropertyHolderConfiguration(
				ConfigurationSource.API,
				propertyHolderMappingName,
				buildConstraintElements( constraintHelper, typeResolutionHelper, valueExtractorManager ),
				defaultGroupSequence
		);
	}

	private Set<ConstrainedElement> buildConstraintElements(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		Set<ConstrainedElement> elements = newHashSet();

		//properties
		for ( PropertyConstraintMappingContextImpl propertyContext : propertyContexts ) {
			elements.add( propertyContext.build( constraintHelper, typeResolutionHelper, valueExtractorManager ) );
		}

		return elements;
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}

}
