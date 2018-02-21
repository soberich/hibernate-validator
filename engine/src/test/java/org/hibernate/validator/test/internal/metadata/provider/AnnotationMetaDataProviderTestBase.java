/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.provider;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedPropertyKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedProperty;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Gunnar Morling
 */
public abstract class AnnotationMetaDataProviderTestBase {

	protected <T> ConstrainedProperty findConstrainedProperty(BeanConfiguration<T> beanConfiguration,
			Class<? super T> clazz, String propertyName, ConstrainedPropertyKind constrainedPropertyKind,
			Class<?>... parameterTypes) throws Exception {

		return (ConstrainedProperty) findConstrainedElement( beanConfiguration,
				constrainedPropertyKind == ConstrainedPropertyKind.FIELD
				? clazz.getDeclaredField( propertyName )
				: clazz.getMethod( propertyName, parameterTypes ) );
	}

	protected <T> ConstrainedExecutable findConstrainedMethod(BeanConfiguration<T> beanConfiguration,
															  Class<? super T> clazz, String methodName,
			Class<?>... parameterTypes) throws Exception {

		return (ConstrainedExecutable) findConstrainedElement( beanConfiguration, clazz.getMethod( methodName, parameterTypes ) );
	}

	protected <T> ConstrainedExecutable findConstrainedConstructor(BeanConfiguration<T> beanConfigurations, Class<T> clazz, Class<?>... parameterTypes)
			throws Exception {

		return (ConstrainedExecutable) findConstrainedElement( beanConfigurations, clazz.getConstructor( parameterTypes ) );
	}

	protected <T> ConstrainedType findConstrainedType(BeanConfiguration<T> beanConfiguration,
													  Class<? super T> type) {

		for ( ConstrainedElement constrainedElement : beanConfiguration.getConstrainedElements() ) {
			if ( constrainedElement.getKind() == ConstrainedElementKind.TYPE ) {
				ConstrainedType constrainedType = (ConstrainedType) constrainedElement;
				if ( constrainedType.getBeanClass().equals( type ) ) {
					return constrainedType;
				}
			}
		}

		throw new RuntimeException( "Found no constrained element for type " + type );
	}

	protected ConstrainedElement findConstrainedElement(BeanConfiguration<?> beanConfiguration, Member member) {

		for ( ConstrainedElement constrainedElement : beanConfiguration.getConstrainedElements() ) {
			if ( member instanceof Executable && constrainedElement instanceof ConstrainedExecutable ) {
				if ( member.equals( ( (ConstrainedExecutable) constrainedElement ).getExecutable() ) ) {
					return constrainedElement;
				}
			}
			else if ( constrainedElement instanceof ConstrainedProperty ) {
				ConstrainedProperty property = (ConstrainedProperty) constrainedElement;
				switch ( property.getConstrainedPropertyKind() ) {
					case FIELD:
						if ( member instanceof Field && property.getProperty().getName().equals( member.getName() ) ) {
							return constrainedElement;
						}
						break;
					case GETTER:
						if ( member instanceof Method && property.getProperty().getName().equals( ReflectionHelper.getPropertyName( member ) ) ) {
							return constrainedElement;
						}
						break;
						default:
							throw new IllegalStateException( "Unknown property kind" );
				}
			}
		}

		throw new RuntimeException( "Found no constrained element for " + member );
	}
}
