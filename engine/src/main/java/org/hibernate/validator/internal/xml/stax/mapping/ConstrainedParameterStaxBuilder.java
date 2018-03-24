/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.stax.mapping;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ValidationException;
import javax.xml.namespace.QName;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.ClassLoadingHelper;
import org.hibernate.validator.internal.xml.stax.mapping.ContainerElementTypeConfigurationBuilder.ContainerElementTypeConfiguration;

/**
 * Builder for constraint parameters.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class ConstrainedParameterStaxBuilder extends AbstractConstrainedElementStaxBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String PARAMETER_QNAME_LOCAL_PART = "parameter";
	private static final QName TYPE_QNAME = new QName( "type" );

	ConstrainedParameterStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager, DefaultPackageStaxBuilder defaultPackageStaxBuilder,
			AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		super( classLoadingHelper, constraintHelper, typeResolutionHelper, valueExtractorManager, defaultPackageStaxBuilder, annotationProcessingOptions );
	}

	@Override
	Optional<QName> getMainAttributeValueQname() {
		return Optional.of( TYPE_QNAME );
	}

	@Override
	protected String getAcceptableQname() {
		return PARAMETER_QNAME_LOCAL_PART;
	}

	public Class<?> getParameterType(Class<?> beanClass) {
		try {
			return classLoadingHelper.loadClass( mainAttributeValue, defaultPackageStaxBuilder.build().orElse( "" ) );
		}
		catch (ValidationException e) {
			throw LOG.getInvalidParameterTypeException( mainAttributeValue, beanClass );
		}
	}

	ConstrainedParameter build(Executable executable, int index) {

		ConstraintLocation constraintLocation = ConstraintLocation.forParameter( executable, index );
		Type type = ReflectionHelper.typeOf( executable, index );

		Set<MetaConstraint<?>> metaConstraints = constraintTypeStaxBuilders.stream()
				.map( builder -> builder.build( constraintLocation, java.lang.annotation.ElementType.PARAMETER, null ) )
				.collect( Collectors.toSet() );

		ContainerElementTypeConfiguration containerElementTypeConfiguration = getContainerElementTypeConfiguration( type, constraintLocation );

		// ignore annotations
		if ( ignoreAnnotations.isPresent() ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsOnParameter(
					executable,
					index,
					ignoreAnnotations.get()
			);
		}

		ConstrainedParameter constrainedParameter = new ConstrainedParameter(
				ConfigurationSource.XML,
				executable,
				type,
				index,
				metaConstraints,
				containerElementTypeConfiguration.getMetaConstraints(),
				getCascadingMetaDataForParameter( containerElementTypeConfiguration.getTypeParametersCascadingMetaData(), type )
		);
		return constrainedParameter;
	}

	private CascadingMetaDataBuilder getCascadingMetaDataForParameter(Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData, Type type) {
		Map<Class<?>, Class<?>> groupConversions = groupConversionBuilder.build();

		return CascadingMetaDataBuilder.annotatedObject( type, validStaxBuilder.build(), containerElementTypesCascadingMetaData, groupConversions );
	}
}
