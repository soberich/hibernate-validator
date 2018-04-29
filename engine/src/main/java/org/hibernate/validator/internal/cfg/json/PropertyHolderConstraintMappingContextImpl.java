/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.json;

import java.util.Collections;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.propertyholder.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.propertyholder.PropertyHolderConstraintMappingContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedProperty;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * Constraint mapping creational context which allows to configure the constraints for one bean property.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
final class PropertyHolderConstraintMappingContextImpl
		extends CascadablePropertyHolderConstraintMappingContextImplBase<PropertyHolderConstraintMappingContext>
		implements PropertyHolderConstraintMappingContext {

	private final PropertyHolderTypeConstraintMappingContextImpl typeContext;

	private final Property property;

	PropertyHolderConstraintMappingContextImpl(PropertyHolderTypeConstraintMappingContextImpl typeContext, Property property) {
		super( typeContext.getConstraintMapping(), property.getType() );
		this.typeContext = typeContext;
		this.property = property;
	}

	@Override
	protected PropertyHolderConstraintMappingContextImpl getThis() {
		return this;
	}

	@Override
	public PropertyHolderConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		super.addConstraint(
				ConfiguredConstraint.forProperty(
						definition, property
				)
		);
		return this;
	}

	@Override
	public PropertyConstraintMappingContext propertyHolder(String property, String mappingName) {
		return typeContext.propertyHolder( property, mappingName );
	}

	ConstrainedElement build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager) {
		return ConstrainedProperty.forField(
				ConfigurationSource.API,
				property,
				getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
				Collections.emptySet(),
				getCascadingMetaDataBuilder()
		);
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
