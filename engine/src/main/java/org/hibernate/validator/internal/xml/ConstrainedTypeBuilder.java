/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocationBuilder;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.xml.binding.ClassType;
import org.hibernate.validator.internal.xml.binding.ConstraintType;
import org.hibernate.validator.internal.xml.binding.GroupSequenceType;

/**
 * Builder for constraint types.
 *
 * @author Hardy Ferentschik
 */
class ConstrainedTypeBuilder {

	private final ClassLoadingHelper classLoadingHelper;
	private final XMLMetaConstraintBuilder metaConstraintBuilder;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
	private final Map<Class<?>, List<Class<?>>> defaultSequences;

	public ConstrainedTypeBuilder(ClassLoadingHelper classLoadingHelper,
			XMLMetaConstraintBuilder metaConstraintBuilder,
			AnnotationProcessingOptionsImpl annotationProcessingOptions,
			Map<Class<?>, List<Class<?>>> defaultSequences) {
		this.classLoadingHelper = classLoadingHelper;
		this.metaConstraintBuilder = metaConstraintBuilder;
		this.annotationProcessingOptions = annotationProcessingOptions;
		this.defaultSequences = defaultSequences;
	}

	ConstrainedType buildConstrainedType(ClassType classType, Class<?> beanClass, String defaultPackage) {
		if ( classType == null ) {
			return null;
		}

		// group sequence
		List<Class<?>> groupSequence = createGroupSequence( classType.getGroupSequence(), defaultPackage );
		if ( !groupSequence.isEmpty() ) {
			defaultSequences.put( beanClass, groupSequence );
		}

		// constraints
		ConstraintLocationBuilder constraintLocation = ConstraintLocationBuilder.forClass( beanClass );
		Set<MetaConstraint<?>> metaConstraints = newHashSet();
		for ( ConstraintType constraint : classType.getConstraint() ) {
			MetaConstraint<?> metaConstraint = metaConstraintBuilder.buildMetaConstraint(
					constraintLocation,
					constraint,
					java.lang.annotation.ElementType.TYPE,
					defaultPackage,
					null
			);
			metaConstraints.add( metaConstraint );
		}

		// ignore annotation
		if ( classType.getIgnoreAnnotations() != null ) {
			annotationProcessingOptions.ignoreClassLevelConstraintAnnotations(
					beanClass,
					classType.getIgnoreAnnotations()
			);
		}

		return new ConstrainedType(
				ConfigurationSource.XML,
				beanClass,
				metaConstraints
		);
	}

	private List<Class<?>> createGroupSequence(GroupSequenceType groupSequenceType, String defaultPackage) {
		List<Class<?>> groupSequence = newArrayList();
		if ( groupSequenceType != null ) {
			for ( String groupName : groupSequenceType.getValue() ) {
				Class<?> group = classLoadingHelper.loadClass( groupName, defaultPackage );
				groupSequence.add( group );
			}
		}
		return groupSequence;
	}
}


