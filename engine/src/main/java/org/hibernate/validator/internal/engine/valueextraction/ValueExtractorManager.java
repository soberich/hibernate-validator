/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.validation.ValidationException;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class ValueExtractorManager {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Immutable
	public static final Set<ValueExtractorDescriptor> SPEC_DEFINED_EXTRACTORS;

	static {
		LinkedHashSet<ValueExtractorDescriptor> specDefinedExtractors = new LinkedHashSet<>();

		if ( isJavaFxInClasspath() ) {
			specDefinedExtractors.add( ObservableValueValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( ListPropertyValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( ReadOnlyListPropertyValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( MapPropertyValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( ReadOnlyMapPropertyValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( MapPropertyKeyExtractor.DESCRIPTOR );
			specDefinedExtractors.add( ReadOnlyMapPropertyKeyExtractor.DESCRIPTOR );
			specDefinedExtractors.add( SetPropertyValueExtractor.DESCRIPTOR );
			specDefinedExtractors.add( ReadOnlySetPropertyValueExtractor.DESCRIPTOR );
		}

		specDefinedExtractors.add( ByteArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( ShortArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( IntArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( LongArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( FloatArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( DoubleArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( CharArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( BooleanArrayValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( ObjectArrayValueExtractor.DESCRIPTOR );

		specDefinedExtractors.add( ListValueExtractor.DESCRIPTOR );

		specDefinedExtractors.add( MapValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( MapKeyExtractor.DESCRIPTOR );

		specDefinedExtractors.add( IterableValueExtractor.DESCRIPTOR );

		specDefinedExtractors.add( OptionalValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( OptionalIntValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( OptionalDoubleValueExtractor.DESCRIPTOR );
		specDefinedExtractors.add( OptionalLongValueExtractor.DESCRIPTOR );

		SPEC_DEFINED_EXTRACTORS = Collections.unmodifiableSet( specDefinedExtractors );
	}

	@Immutable
	private final Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> valueExtractors;

	private final ValueExtractorResolutionCache valueExtractorResolutionCache;

	public ValueExtractorManager(Set<ValueExtractor<?>> externalExtractors) {
		LinkedHashMap<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> tmpValueExtractors = new LinkedHashMap<>();

		// first all built-in extractors
		for ( ValueExtractorDescriptor descriptor : SPEC_DEFINED_EXTRACTORS ) {
			tmpValueExtractors.put( descriptor.getKey(), descriptor );
		}

		// then any custom ones, overriding the built-in ones
		for ( ValueExtractor<?> valueExtractor : externalExtractors ) {
			ValueExtractorDescriptor descriptor = new ValueExtractorDescriptor( valueExtractor );
			tmpValueExtractors.put( descriptor.getKey(), descriptor );
		}

		valueExtractors = Collections.unmodifiableMap( tmpValueExtractors );
		valueExtractorResolutionCache = new ValueExtractorResolutionCache();
	}

	public ValueExtractorManager(ValueExtractorManager template, Map<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> externalValueExtractorDescriptors) {
		LinkedHashMap<ValueExtractorDescriptor.Key, ValueExtractorDescriptor> tmpValueExtractors = new LinkedHashMap<>( template.valueExtractors );
		tmpValueExtractors.putAll( externalValueExtractorDescriptors );

		valueExtractors = Collections.unmodifiableMap( tmpValueExtractors );
		valueExtractorResolutionCache = new ValueExtractorResolutionCache();
	}

	public static Set<ValueExtractor<?>> getDefaultValueExtractors() {
		return SPEC_DEFINED_EXTRACTORS.stream()
				.map( d -> d.getValueExtractor() )
				.collect( Collectors.collectingAndThen( Collectors.toSet(), Collections::unmodifiableSet ) );
	}

	/**
	 * Returns the maximally specific type compliant value extractors or an empty set if none was found.
	 */
	public Set<ValueExtractorDescriptor> getMaximallySpecificValueExtractors(Class<?> valueType) {
		Set<ValueExtractorDescriptor> typeCompatibleExtractors = valueExtractors.values()
				.stream()
				.filter( e -> TypeHelper.isAssignable( TypeHelper.getErasedReferenceType( e.getContainerType() ), valueType ) )
				.collect( Collectors.toSet() );

		return getMaximallySpecificValueExtractors( valueType, typeCompatibleExtractors );
	}

	/**
	 * Returns the maximally specific type-compliant and container-element-compliant value extractor or
	 * {@code null} if none was found.
	 * <p>
	 * Throws an exception if more than 2 maximally specific container-element-compliant value extractors are found.
	 */
	public ValueExtractorDescriptor getMaximallySpecificAndContainerElementCompliantValueExtractor(Class<?> declaredType, TypeVariable<?> typeParameter) {
		Set<ValueExtractorDescriptor> maximallySpecificContainerElementCompliantValueExtractors = getMaximallySpecificValueExtractors( declaredType,
				getTypeCompliantAndContainerElementCompliantValueExtractors( declaredType, typeParameter ) );

		if ( maximallySpecificContainerElementCompliantValueExtractors.isEmpty() ) {
			return null;
		}
		else if ( maximallySpecificContainerElementCompliantValueExtractors.size() == 1 ) {
			return maximallySpecificContainerElementCompliantValueExtractors.iterator().next();
		}
		else {
			throw LOG.getUnableToGetMostSpecificValueExtractorDueToSeveralMaximallySpecificValueExtractorsDeclaredException( declaredType,
					ValueExtractorHelper.toValueExtractorClasses( maximallySpecificContainerElementCompliantValueExtractors ) );
		}
	}

	/**
	 * Returns the maximally specific type-compliant and container-element-compliant value extractor
	 * from a set of preselected value extractors or {@code null} if none was found.
	 * <p>
	 * The preselected value extractors are chosen based on the declared type whereas the maximally specific value extractor here
	 * is elected based on the runtime type.
	 * <p>
	 * Throws an exception if more than 2 maximally specific container-element-compliant value extractors are found.
	 */
	public ValueExtractorDescriptor getMaximallySpecificAndRuntimeContainerElementCompliantValueExtractor(Type declaredType, Class<?> runtimeType, TypeVariable<?> typeParameter) {
		Set<ValueExtractorDescriptor> maximallySpecificContainerElementCompliantValueExtractors =
				valueExtractorResolutionCache.getValueExtractors( declaredType, runtimeType, typeParameter );

		if ( maximallySpecificContainerElementCompliantValueExtractors.isEmpty() ) {
			return null;
		}
		else if ( maximallySpecificContainerElementCompliantValueExtractors.size() == 1 ) {
			return maximallySpecificContainerElementCompliantValueExtractors.iterator().next();
		}
		else {
			throw LOG.getUnableToGetMostSpecificValueExtractorDueToSeveralMaximallySpecificValueExtractorsDeclaredException( runtimeType,
					ValueExtractorHelper.toValueExtractorClasses( maximallySpecificContainerElementCompliantValueExtractors ) );
		}
	}

	/**
	 * Returns the set of type-compliant and container-element-compliant value extractors or an empty set if none was found.
	 */
	private Set<ValueExtractorDescriptor> getTypeCompliantAndContainerElementCompliantValueExtractors(Type declaredType, TypeVariable<?> typeParameter) {
		boolean isInternal = TypeVariables.isInternal( typeParameter );
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings = null;
		if ( !isInternal ) {
			allBindings = TypeVariableBindings.getTypeVariableBindings( (Class<?>) typeParameter.getGenericDeclaration() );
		}

		Set<ValueExtractorDescriptor> typeCompatibleExtractors = valueExtractors.values()
				.stream()
				.filter( e -> TypeHelper.isAssignable( e.getContainerType(), declaredType ) )
				.collect( Collectors.toSet() );

		Set<ValueExtractorDescriptor> containerElementCompliantExtractors = new HashSet<>();

		for ( ValueExtractorDescriptor extractorDescriptor : typeCompatibleExtractors ) {
			TypeVariable<?> typeParameterBoundToExtractorType;

			if ( !isInternal ) {
				Map<TypeVariable<?>, TypeVariable<?>> bindingsForExtractorType = allBindings.get( extractorDescriptor.getContainerType() );
				typeParameterBoundToExtractorType = bind( typeParameter, bindingsForExtractorType );
			}
			else {
				typeParameterBoundToExtractorType = typeParameter;
			}

			if ( Objects.equals( extractorDescriptor.getExtractedTypeParameter(), typeParameterBoundToExtractorType ) ) {
				containerElementCompliantExtractors.add( extractorDescriptor );
			}
		}

		return containerElementCompliantExtractors;
	}

	private Set<ValueExtractorDescriptor> getMaximallySpecificValueExtractors(Class<?> valueType, Set<ValueExtractorDescriptor> extractors) {
		Set<ValueExtractorDescriptor> candidates = CollectionHelper.newHashSet( extractors.size() );

		for ( ValueExtractorDescriptor descriptor : extractors ) {
			// in the case of cascaded validation, some of the proposed value extractors
			// might not be compatible with the runtime type we have in the end so we need
			// to skip them
			if ( !TypeHelper.isAssignable( descriptor.getContainerType(), valueType ) ) {
				continue;
			}
			if ( candidates.isEmpty() ) {
				candidates.add( descriptor );
				continue;
			}
			Iterator<ValueExtractorDescriptor> candidatesIterator = candidates.iterator();
			boolean isNewRoot = true;
			while ( candidatesIterator.hasNext() ) {
				ValueExtractorDescriptor candidate = candidatesIterator.next();

				// we consider the strictly more specific value extractor so 2 value extractors for the same container
				// type should throw an error in the end if no other more specific value extractor is found.
				if ( candidate.getContainerType().equals( descriptor.getContainerType() ) ) {
					continue;
				}

				if ( TypeHelper.isAssignable( candidate.getContainerType(), descriptor.getContainerType() ) ) {
					candidatesIterator.remove();
				}
				else if ( TypeHelper.isAssignable( descriptor.getContainerType(), candidate.getContainerType() ) ) {
					isNewRoot = false;
				}
			}
			if ( isNewRoot ) {
				candidates.add( descriptor );
			}
		}

		return candidates;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( valueExtractors == null ) ? 0 : valueExtractors.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ValueExtractorManager other = (ValueExtractorManager) obj;

		return valueExtractors.equals( other.valueExtractors );
	}

	private TypeVariable<?> bind(TypeVariable<?> typeParameter, Map<TypeVariable<?>, TypeVariable<?>> bindings) {
		return bindings != null ? bindings.get( typeParameter ) : null;
	}

	private static boolean isJavaFxInClasspath() {
		return isClassPresent( "javafx.application.Application", false );
	}

	private static boolean isClassPresent(String className, boolean fallbackOnTCCL) {
		try {
			run( LoadClass.action( className, ValueExtractorManager.class.getClassLoader(), fallbackOnTCCL ) );
			return true;
		}
		catch (ValidationException e) {
			return false;
		}
	}

	private final class ValueExtractorResolutionCache {

		private final ConcurrentHashMap<ValueExtractorCacheKey, Set<ValueExtractorDescriptor>> possibleValueExtractorsByRuntimeTypes = new ConcurrentHashMap<>();
		private final Set<Class<?>> nonContainerTypes = new HashSet<>();

		public Set<ValueExtractorDescriptor> getValueExtractors(Type declaredType, Class<?> runtimeType, TypeVariable<?> typeParameter ) {
			if ( nonContainerTypes.contains( runtimeType ) ) {
				return Collections.emptySet();
			}
			ValueExtractorCacheKey cacheKey = new ValueExtractorCacheKey( runtimeType, typeParameter );

			Set<ValueExtractorDescriptor> valueExtractorDescriptors = possibleValueExtractorsByRuntimeTypes.get( cacheKey );
			if ( valueExtractorDescriptors == null ) {
				Set<ValueExtractorDescriptor> possibleValueExtractors = getMaximallySpecificValueExtractors( runtimeType );

				valueExtractorDescriptors = CollectionHelper.newHashSet( possibleValueExtractors.size() );

				boolean isInternal = TypeVariables.isInternal( typeParameter );
				Class<?> erasedDeclaredType = TypeHelper.getErasedReferenceType( declaredType );

				for ( ValueExtractorDescriptor extractorDescriptor : possibleValueExtractors ) {
					// depending which type is wider - declared or the one from the possible extractor we need to pass
					// parameters in different order to check if the extractor is compatible
					if ( TypeHelper.isAssignable( extractorDescriptor.getContainerType(), erasedDeclaredType )
							? validateValueExtractorCompatibility( isInternal, erasedDeclaredType, extractorDescriptor.getContainerType(), typeParameter, extractorDescriptor.getExtractedTypeParameter() )
							: validateValueExtractorCompatibility( isInternal, extractorDescriptor.getContainerType(), erasedDeclaredType, extractorDescriptor.getExtractedTypeParameter(), typeParameter
					) ) {
						valueExtractorDescriptors.add( extractorDescriptor );
					}
				}

				if ( valueExtractorDescriptors.isEmpty() ) {
					nonContainerTypes.add( runtimeType );
				}
				else {
					possibleValueExtractorsByRuntimeTypes.put( cacheKey, valueExtractorDescriptors );
				}
			}

			return CollectionHelper.toImmutableSet( valueExtractorDescriptors );
		}

		private boolean validateValueExtractorCompatibility(boolean isInternal,
				Class<?> typeForBinding,
				Class<?> typeToBind,
				TypeVariable<?> typeParameterForBinding,
				TypeVariable<?> typeParameterToCompare) {
			TypeVariable<?> typeParameterBoundToExtractorType;
			if ( !isInternal ) {
				Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings =
						TypeVariableBindings.getTypeVariableBindings( typeForBinding );

				Map<TypeVariable<?>, TypeVariable<?>> bindingsForExtractorType = allBindings.get( typeToBind );
				typeParameterBoundToExtractorType = bind( typeParameterForBinding, bindingsForExtractorType );
			}
			else {
				typeParameterBoundToExtractorType = typeParameterForBinding;
			}
			return Objects.equals( typeParameterToCompare, typeParameterBoundToExtractorType );
		}

		private class ValueExtractorCacheKey {

			private final Class<?> runtimeType;
			private final TypeVariable<?> typeParameter;
			private final int hashCode;

			ValueExtractorCacheKey(Class<?> runtimeType, TypeVariable<?> typeParameter) {
				this.runtimeType = runtimeType;
				this.typeParameter = typeParameter;
				this.hashCode = Objects.hash( this.runtimeType, this.typeParameter );
			}

			@Override
			public boolean equals(Object o) {
				if ( this == o ) {
					return true;
				}
				if ( o == null || this.getClass() != o.getClass() ) {
					return false;
				}
				ValueExtractorCacheKey that = (ValueExtractorCacheKey) o;
				return Objects.equals( this.runtimeType, that.runtimeType ) &&
						Objects.equals( this.typeParameter, that.typeParameter );
			}

			@Override
			public int hashCode() {
				return hashCode;
			}
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
