/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.stax.mapping;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.xml.ClassLoadingHelper;
import org.hibernate.validator.internal.xml.stax.AbstractStaxBuilder;
import org.hibernate.validator.internal.xml.stax.mapping.ContainerElementTypeConfigurationBuilder.ContainerElementTypeConfiguration;

/**
 * Builder to be extend from for all constrained element builders that can have constraints and type argument constraints.
 *
 * @author Marko Bekhta
 */
abstract class AbstractConstrainedElementStaxBuilder extends AbstractStaxBuilder {

	private static final QName IGNORE_ANNOTATIONS_QNAME = new QName( "ignore-annotations" );

	protected final ClassLoadingHelper classLoadingHelper;
	protected final ConstraintHelper constraintHelper;
	protected final TypeResolutionHelper typeResolutionHelper;
	protected final ValueExtractorManager valueExtractorManager;
	protected final DefaultPackageStaxBuilder defaultPackageStaxBuilder;
	protected final AnnotationProcessingOptionsImpl annotationProcessingOptions;

	protected String mainAttributeValue;
	protected Optional<Boolean> ignoreAnnotations;
	protected final GroupConversionStaxBuilder groupConversionBuilder;
	protected final ValidStaxBuilder validStaxBuilder;
	protected final List<ConstraintTypeStaxBuilder> constraintTypeStaxBuilders;
	protected final ContainerElementTypeConfigurationBuilder containerElementTypeConfigurationBuilder;

	AbstractConstrainedElementStaxBuilder(ClassLoadingHelper classLoadingHelper, ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager,
			DefaultPackageStaxBuilder defaultPackageStaxBuilder, AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		this.classLoadingHelper = classLoadingHelper;
		this.defaultPackageStaxBuilder = defaultPackageStaxBuilder;
		this.constraintHelper = constraintHelper;
		this.typeResolutionHelper = typeResolutionHelper;
		this.valueExtractorManager = valueExtractorManager;

		this.groupConversionBuilder = new GroupConversionStaxBuilder( classLoadingHelper, defaultPackageStaxBuilder );
		this.validStaxBuilder = new ValidStaxBuilder();
		this.containerElementTypeConfigurationBuilder = new ContainerElementTypeConfigurationBuilder();
		this.annotationProcessingOptions = annotationProcessingOptions;

		this.constraintTypeStaxBuilders = new ArrayList<>();
	}

	abstract Optional<QName> getMainAttributeValueQname();

	@Override
	protected void add(XMLEventReader xmlEventReader, XMLEvent xmlEvent) throws XMLStreamException {
		Optional<QName> mainAttributeValueQname = getMainAttributeValueQname();
		if ( mainAttributeValueQname.isPresent() ) {
			mainAttributeValue = readAttribute( xmlEvent.asStartElement(), mainAttributeValueQname.get() ).get();
		}
		ignoreAnnotations = readAttribute( xmlEvent.asStartElement(), IGNORE_ANNOTATIONS_QNAME ).map( Boolean::parseBoolean );
		ConstraintTypeStaxBuilder constraintTypeStaxBuilder = getNewConstraintTypeStaxBuilder();
		ContainerElementTypeStaxBuilder containerElementTypeStaxBuilder = getNewContainerElementTypeStaxBuilder();
		while ( !( xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals( getAcceptableQname() ) ) ) {
			xmlEvent = xmlEventReader.nextEvent();
			validStaxBuilder.process( xmlEventReader, xmlEvent );
			groupConversionBuilder.process( xmlEventReader, xmlEvent );
			if ( constraintTypeStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				constraintTypeStaxBuilders.add( constraintTypeStaxBuilder );
				constraintTypeStaxBuilder = getNewConstraintTypeStaxBuilder();
			}
			if ( containerElementTypeStaxBuilder.process( xmlEventReader, xmlEvent ) ) {
				containerElementTypeConfigurationBuilder.add( containerElementTypeStaxBuilder );
				containerElementTypeStaxBuilder = getNewContainerElementTypeStaxBuilder();
			}
		}
	}

	private ConstraintTypeStaxBuilder getNewConstraintTypeStaxBuilder() {
		return new ConstraintTypeStaxBuilder( classLoadingHelper, constraintHelper, typeResolutionHelper, valueExtractorManager, defaultPackageStaxBuilder );
	}

	private ContainerElementTypeStaxBuilder getNewContainerElementTypeStaxBuilder() {
		return new ContainerElementTypeStaxBuilder( classLoadingHelper, constraintHelper, typeResolutionHelper, valueExtractorManager, defaultPackageStaxBuilder );
	}

	protected ContainerElementTypeConfiguration getContainerElementTypeConfiguration(Type type, ConstraintLocation constraintLocation) {
		return containerElementTypeConfigurationBuilder.build( constraintLocation, type );
	}

}
