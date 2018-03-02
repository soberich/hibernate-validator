/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.ValidationTypeResolutionHelper;

/**
 * @author Marko Bekhta
 */
public abstract class ConstraintLocationBuilder {

	public static ConstraintLocationBuilder forClass(Class<?> declaringClass) {
		return new BeanConstraintLocationBuilder( declaringClass );
	}

	public static ConstraintLocationBuilder forField(Field field) {
		return new FieldConstraintLocationBuilder( field );
	}

	public static ConstraintLocationBuilder forGetter(Method getter) {
		return new GetterConstraintLocationBuilder( getter );
	}

	public static ConstraintLocationBuilder forTypeArgument(ConstraintLocationBuilder delegate, TypeVariable<?> typeParameter, Type typeOfAnnotatedElement) {
		return new TypeArgumentConstraintLocationBuilder( delegate, typeParameter, typeOfAnnotatedElement );
	}

	public static ConstraintLocationBuilder forReturnValue(Executable executable) {
		return new ReturnValueConstraintLocationBuilder( executable );
	}

	public static ConstraintLocationBuilder forCrossParameter(Executable executable) {
		return new CrossParameterConstraintLocationBuilder( executable );
	}

	public static ConstraintLocationBuilder forParameter(Executable executable, int index) {
		return new ParameterConstraintLocationBuilder( executable, index );
	}

	private final Type typeForValidatorResolution;

	private ConstraintLocation location;

	protected ConstraintLocationBuilder(Type typeForValidatorResolution) {
		this.typeForValidatorResolution = typeForValidatorResolution;
	}

	public ConstraintLocation getLocation(ExecutableParameterNameProvider parameterNameProvider) {
		if ( location == null ) {
			location = build( parameterNameProvider );
		}
		return location;
	}

	protected abstract ConstraintLocation build(ExecutableParameterNameProvider parameterNameProvider);

	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	public abstract Member getMember();

	//	TODO nice toString

	private static class BeanConstraintLocationBuilder extends ConstraintLocationBuilder {

		private final Class<?> declaringClass;

		private BeanConstraintLocationBuilder(Class<?> declaringClass) {
			super( ValidationTypeResolutionHelper.getTypeForValidatorResolution( declaringClass ) );
			this.declaringClass = declaringClass;
		}

		@Override
		public ConstraintLocation build(ExecutableParameterNameProvider parameterNameProvider) {
			return new BeanConstraintLocation( declaringClass );
		}

		@Override
		public Member getMember() {
			return null;
		}
	}

	private static class FieldConstraintLocationBuilder extends ConstraintLocationBuilder {

		private final Field field;

		private FieldConstraintLocationBuilder(Field field) {
			super( ValidationTypeResolutionHelper.getTypeForValidatorResolution( field ) );
			this.field = field;
		}

		@Override
		public ConstraintLocation build(ExecutableParameterNameProvider parameterNameProvider) {
			return new FieldConstraintLocation( field );
		}

		@Override
		public Member getMember() {
			return field;
		}
	}

	private static class GetterConstraintLocationBuilder extends ConstraintLocationBuilder {

		private final Method method;

		private GetterConstraintLocationBuilder(Method method) {
			super( ValidationTypeResolutionHelper.getTypeForValidatorResolution( method ) );
			this.method = method;
		}

		@Override
		public ConstraintLocation build(ExecutableParameterNameProvider parameterNameProvider) {
			return new GetterConstraintLocation( method );
		}

		@Override
		public Member getMember() {
			return method;
		}
	}

	private static class TypeArgumentConstraintLocationBuilder extends ConstraintLocationBuilder {

		private final ConstraintLocationBuilder delegate;
		private final TypeVariable<?> typeParameter;
		private final Type typeOfAnnotatedElement;

		private TypeArgumentConstraintLocationBuilder(ConstraintLocationBuilder delegate, TypeVariable<?> typeParameter, Type typeOfAnnotatedElement) {
			super( ReflectionHelper.boxedType( typeOfAnnotatedElement ) );
			this.delegate = delegate;
			this.typeParameter = typeParameter;
			this.typeOfAnnotatedElement = typeOfAnnotatedElement;
		}

		@Override
		public ConstraintLocation build(ExecutableParameterNameProvider parameterNameProvider) {
			return new TypeArgumentConstraintLocation( delegate.getLocation( parameterNameProvider ), typeParameter, typeOfAnnotatedElement );
		}

		@Override
		public Member getMember() {
			return delegate.getMember();
		}
	}

	private static class ReturnValueConstraintLocationBuilder extends ConstraintLocationBuilder {

		private final Executable executable;

		private ReturnValueConstraintLocationBuilder(Executable executable) {
			super( ValidationTypeResolutionHelper.getTypeForValidatorResolution( executable ) );
			this.executable = executable;
		}

		@Override
		public ConstraintLocation build(ExecutableParameterNameProvider parameterNameProvider) {
			return new ReturnValueConstraintLocation( executable );
		}

		@Override
		public Member getMember() {
			return executable;
		}
	}

	private static class CrossParameterConstraintLocationBuilder extends ConstraintLocationBuilder {

		private final Executable executable;

		private CrossParameterConstraintLocationBuilder(Executable executable) {
			super( Object[].class );
			this.executable = executable;
		}

		@Override
		public ConstraintLocation build(ExecutableParameterNameProvider parameterNameProvider) {
			return new CrossParameterConstraintLocation( executable );
		}

		@Override
		public Member getMember() {
			return executable;
		}
	}

	private static class ParameterConstraintLocationBuilder extends ConstraintLocationBuilder {

		private final Executable executable;
		private final int parameterIndex;

		private ParameterConstraintLocationBuilder(Executable executable, int parameterIndex) {
			super( ValidationTypeResolutionHelper.getTypeForValidatorResolution( executable, parameterIndex ) );
			this.executable = executable;
			this.parameterIndex = parameterIndex;
		}

		@Override
		public ConstraintLocation build(ExecutableParameterNameProvider parameterNameProvider) {
			String name = parameterNameProvider.getParameterNames( executable ).get( parameterIndex );
			return new ParameterConstraintLocation( executable, name, parameterIndex );
		}

		@Override
		public Member getMember() {
			return executable;
		}
	}

}
