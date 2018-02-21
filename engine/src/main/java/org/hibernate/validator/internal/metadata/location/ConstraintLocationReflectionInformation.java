/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.properties.java.beans.JavaBeansExecutable;
import org.hibernate.validator.internal.properties.java.beans.JavaBeansField;
import org.hibernate.validator.internal.properties.java.beans.JavaBeansGetter;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;

/**
 * @author Marko Bekhta
 */
public interface ConstraintLocationReflectionInformation {

	ConstraintLocation toConstraintLocation(ExecutableParameterNameProvider executableParameterNameProvider);

	static ConstraintLocationReflectionInformation forClass(Class<?> declaringClass) {
		return new BeanConstraintLocationReflectionInformation( declaringClass );
	}

	static ConstraintLocationReflectionInformation forProperty(Field property) {
		return new FieldConstraintLocationReflectionInformation( property );
	}

	static ConstraintLocationReflectionInformation forProperty(Method property) {
		return new GetterConstraintLocationReflectionInformation( property );
	}

	static ConstraintLocationReflectionInformation forTypeArgument(ConstraintLocationReflectionInformation delegate, TypeVariable<?> typeParameter, Type typeOfAnnotatedElement) {
		return new TypeArgumentConstraintLocationReflectionInformation( delegate, typeParameter, typeOfAnnotatedElement );
	}

	static ConstraintLocationReflectionInformation forReturnValue(Executable executable) {
		return new ReturnValueConstraintLocationReflectionInformation( executable );
	}

	static ConstraintLocationReflectionInformation forCrossParameter(Executable executable) {
		return new CrossParameterConstraintLocationReflectionInformation( executable );
	}

	static ConstraintLocationReflectionInformation forParameter(Executable executable, int index) {
		return new ParameterConstraintLocationReflectionInformation( executable, index );
	}

	Type getTypeForValidatorResolution();

	class FieldConstraintLocationReflectionInformation implements ConstraintLocationReflectionInformation {

		private final Field field;
		private Type typeForValidationResolution;

		FieldConstraintLocationReflectionInformation(Field field) {
			this.field = field;
			// TODO: extract these typeForValidationResolution into its own util/helper class. And replace usages in other places
			this.typeForValidationResolution = ReflectionHelper.boxedType( ReflectionHelper.typeOf( field ) );
		}

		@Override
		public ConstraintLocation toConstraintLocation(ExecutableParameterNameProvider executableParameterNameProvider) {
			return ConstraintLocation.forProperty( new JavaBeansField( field ) );
		}

		@Override
		public Type getTypeForValidatorResolution() {
			return typeForValidationResolution;
		}
	}

	class GetterConstraintLocationReflectionInformation implements ConstraintLocationReflectionInformation {

		private final Method method;
		private final Type typeForValidatorResolution;

		GetterConstraintLocationReflectionInformation(Method method) {
			this.method = method;
			this.typeForValidatorResolution = ReflectionHelper.boxedType( ReflectionHelper.typeOf( method ) );
		}

		@Override
		public ConstraintLocation toConstraintLocation(ExecutableParameterNameProvider executableParameterNameProvider) {
			return ConstraintLocation.forProperty( new JavaBeansGetter( method ) );
		}

		@Override
		public Type getTypeForValidatorResolution() {
			return typeForValidatorResolution;
		}
	}

	class ReturnValueConstraintLocationReflectionInformation implements ConstraintLocationReflectionInformation {

		private final Executable executable;
		private final Type typeForValidatorResolution;

		public ReturnValueConstraintLocationReflectionInformation(Executable executable) {
			this.executable = executable;
			this.typeForValidatorResolution = ReflectionHelper.boxedType( ReflectionHelper.typeOf( executable ) );
		}

		@Override
		public ConstraintLocation toConstraintLocation(ExecutableParameterNameProvider executableParameterNameProvider) {
			return ConstraintLocation.forReturnValue( new JavaBeansExecutable( executable ) );
		}

		@Override
		public Type getTypeForValidatorResolution() {
			return typeForValidatorResolution;
		}
	}

	class CrossParameterConstraintLocationReflectionInformation implements ConstraintLocationReflectionInformation {

		private final Executable executable;

		public CrossParameterConstraintLocationReflectionInformation(Executable executable) {
			this.executable = executable;
		}

		@Override
		public ConstraintLocation toConstraintLocation(ExecutableParameterNameProvider executableParameterNameProvider) {
			return ConstraintLocation.forCrossParameter( new JavaBeansExecutable( executable ) );
		}

		@Override
		public Type getTypeForValidatorResolution() {
			return Object[].class;
		}
	}

	class ParameterConstraintLocationReflectionInformation implements ConstraintLocationReflectionInformation {

		private final Executable executable;
		private final int index;
		private final Type typeForValidatorResolution;

		public ParameterConstraintLocationReflectionInformation(Executable executable, int index) {
			this.executable = executable;
			this.index = index;
			this.typeForValidatorResolution = ReflectionHelper.boxedType( ReflectionHelper.typeOf( executable, index ) );
		}

		@Override
		public ConstraintLocation toConstraintLocation(ExecutableParameterNameProvider executableParameterNameProvider) {
			return ConstraintLocation.forParameter( new JavaBeansExecutable( executable ), executableParameterNameProvider.getParameterNames( executable ).get( index ), index );
		}

		@Override
		public Type getTypeForValidatorResolution() {
			return typeForValidatorResolution;
		}
	}

	class BeanConstraintLocationReflectionInformation implements ConstraintLocationReflectionInformation {

		private final Class<?> declaringClass;
		private final Type typeForValidatorResolution;

		public BeanConstraintLocationReflectionInformation(Class<?> declaringClass) {
			this.declaringClass = declaringClass;
			// HV-623 - create a ParameterizedType in case the class has type parameters. Needed for constraint validator
			// resolution (HF)
			this.typeForValidatorResolution = declaringClass.getTypeParameters().length == 0 ?
					declaringClass :
					TypeHelper.parameterizedType( declaringClass, declaringClass.getTypeParameters() );
		}

		@Override
		public ConstraintLocation toConstraintLocation(ExecutableParameterNameProvider executableParameterNameProvider) {
			return ConstraintLocation.forClass( declaringClass );
		}

		@Override
		public Type getTypeForValidatorResolution() {
			return typeForValidatorResolution;
		}
	}

	class TypeArgumentConstraintLocationReflectionInformation implements ConstraintLocationReflectionInformation {

		private final ConstraintLocationReflectionInformation delegate;
		private final TypeVariable<?> typeParameter;
		private final Type typeOfAnnotatedElement;
		private final Type typeForValidatorResolution;

		public TypeArgumentConstraintLocationReflectionInformation(ConstraintLocationReflectionInformation delegate, TypeVariable<?> typeParameter, Type typeOfAnnotatedElement) {
			this.delegate = delegate;
			this.typeParameter = typeParameter;
			this.typeOfAnnotatedElement = typeOfAnnotatedElement;
			this.typeForValidatorResolution = ReflectionHelper.boxedType( typeOfAnnotatedElement );
		}

		@Override
		public ConstraintLocation toConstraintLocation(ExecutableParameterNameProvider executableParameterNameProvider) {
			return ConstraintLocation.forTypeArgument( delegate.toConstraintLocation( executableParameterNameProvider ), typeParameter, typeOfAnnotatedElement );
		}

		@Override
		public Type getTypeForValidatorResolution() {
			return typeForValidatorResolution;
		}
	}
}
