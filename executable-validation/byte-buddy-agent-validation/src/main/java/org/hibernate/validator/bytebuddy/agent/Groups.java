/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.bytebuddy.agent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.hibernate.validator.executable.validation.Validate;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder.ParameterBinder;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;

/**
 * @author Marko Bekhta
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.PARAMETER)
public @interface Groups {

	class ForGroups implements Advice.OffsetMapping {

		/**
		 * The component target type.
		 */
		private final TypeDescription.Generic target;

		public ForGroups(TypeDescription.Generic target) {
			this.target = target;
		}

		@Override
		public Target resolve(TypeDescription instrumentedType,
				MethodDescription instrumentedMethod,
				Assigner assigner,
				Advice.ArgumentHandler argumentHandler,
				Sort sort) {
			Class<?>[] groups = instrumentedMethod.getDeclaredAnnotations().ofType( Validate.class ).loadSilent().groups();

			return new Target.ForArray.ReadOnly(
					target,
					Arrays.stream( groups )
							.map( group -> ClassConstant.of( TypeDescription.ForLoadedType.of( group ) ) )
							.collect( Collectors.toList() )
			);
		}

		/**
		 * A factory for an offset mapping that maps all arguments values of the instrumented method.
		 */
		public enum Factory implements Advice.OffsetMapping.Factory<Groups> {

			/**
			 * The singleton instance.
			 */
			INSTANCE;

			@Override
			public Class<Groups> getAnnotationType() {
				return Groups.class;
			}

			@Override
			public Advice.OffsetMapping make(ParameterDescription.InDefinedShape target,
					AnnotationDescription.Loadable<Groups> annotation,
					AdviceType adviceType) {
				if ( !target.getType().represents( Object.class ) && !target.getType().isArray() ) {
					throw new IllegalStateException( "Cannot use Groups annotation on a non-array type" );
				}
				else {
					return new ForGroups( target.getType().represents( Object.class )
							? TypeDescription.Generic.OBJECT
							: target.getType().getComponentType() );
				}
			}
		}
	}


	/**
	 * Not used for now as method delegation is not used.
	 */
	enum Binder implements ParameterBinder<Groups> {

		/**
		 * The singleton instance.
		 */
		INSTANCE;

		@Override
		public Class<Groups> getHandledType() {
			return Groups.class;
		}

		@Override
		public MethodDelegationBinder.ParameterBinding<?> bind(AnnotationDescription.Loadable<Groups> annotation,
				MethodDescription source,
				ParameterDescription target,
				Implementation.Target implementationTarget,
				Assigner assigner,
				Assigner.Typing typing) {

			TypeDescription typeDescription = target.getType().asErasure();
			if ( !typeDescription.isArray() || typeDescription.getComponentType().asErasure().represents( Class.class ) ) {
				throw new IllegalStateException( target + " makes illegal use of @Validate" );
			}

			Class<?>[] groups = source.getDeclaredAnnotations().ofType( Validate.class ).loadSilent().groups();

			return new MethodDelegationBinder.ParameterBinding.Anonymous(
					ArrayFactory.forType( new TypeDescription.Generic.OfNonGenericType.ForLoadedType( Class.class ) )
							.withValues(
									Arrays.stream( groups )
											.map( group -> ClassConstant.of( TypeDescription.ForLoadedType.of( group ) ) )
											.collect( Collectors.toList() )
							)
			);
		}
	}
}
