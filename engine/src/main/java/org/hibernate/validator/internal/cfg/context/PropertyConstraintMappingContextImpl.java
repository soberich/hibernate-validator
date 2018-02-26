/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.ContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocationReflectionInformation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedProperty;
import org.hibernate.validator.internal.properties.java.beans.JavaBeansField;
import org.hibernate.validator.internal.properties.java.beans.JavaBeansGetter;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * Constraint mapping creational context which allows to configure the constraints for one bean property.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
final class PropertyConstraintMappingContextImpl
		extends CascadableConstraintMappingContextImplBase<PropertyConstraintMappingContext>
		implements PropertyConstraintMappingContext {

	private final TypeConstraintMappingContextImpl<?> typeContext;

	// either Field or Method
	private final Member member;
	private final ConstraintLocationReflectionInformation location;

	PropertyConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Member member) {
		super( typeContext.getConstraintMapping(), ReflectionHelper.typeOf( member ) );
		this.typeContext = typeContext;
		this.member = member;
		if ( member instanceof Field ) {
			this.location = ConstraintLocationReflectionInformation.forProperty( (Field) member );
		}
		else {
			this.location = ConstraintLocationReflectionInformation.forProperty( (Method) member );
		}
	}

	@Override
	protected PropertyConstraintMappingContextImpl getThis() {
		return this;
	}

	@Override
	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		if ( member instanceof Field ) {
			super.addConstraint(
					ConfiguredConstraint.forProperty(
							definition, member
					)
			);
		}
		else {
			super.addConstraint(
					ConfiguredConstraint.forProperty(
							definition, (Method) member
					)
			);
		}
		return this;
	}

	@Override
	public PropertyConstraintMappingContext ignoreAnnotations() {
		return ignoreAnnotations( true );
	}

	@Override
	public PropertyConstraintMappingContext ignoreAnnotations(boolean ignoreAnnotations) {
		mapping.getAnnotationProcessingOptions().ignoreConstraintAnnotationsOnMember( member, ignoreAnnotations );
		return this;
	}

	@Override
	public PropertyConstraintMappingContext property(String property, ElementType elementType) {
		return typeContext.property( property, elementType );
	}

	@Override
	public ConstructorConstraintMappingContext constructor(Class<?>... parameterTypes) {
		return typeContext.constructor( parameterTypes );
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		return typeContext.method( name, parameterTypes );
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType() {
		return super.containerElement( this, typeContext, location );
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType(int index, int... nestedIndexes) {
		return super.containerElement( this, typeContext, location, index, nestedIndexes );
	}

	ConstrainedElement build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager, ExecutableParameterNameProvider executableParameterNameProvider) {
		if ( member instanceof Field ) {
			return ConstrainedProperty.forField(
					ConfigurationSource.API,
					new JavaBeansField( (Field) member ),
					getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager, executableParameterNameProvider ),
					getTypeArgumentConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager, executableParameterNameProvider ),
					getCascadingMetaDataBuilder()
			);
		}
		else {
			return ConstrainedProperty.forGetter(
					ConfigurationSource.API,
					new JavaBeansGetter( (Method) member ),
					getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager, executableParameterNameProvider ),
					getTypeArgumentConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager, executableParameterNameProvider ),
					getCascadingMetaDataBuilder()
			);
		}
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
