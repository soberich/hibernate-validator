/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valuecontext;

import java.lang.annotation.ElementType;
import java.lang.reflect.TypeVariable;
import java.util.List;

import javax.validation.groups.Default;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeVariables;

/**
 * An instance of this class is used to collect all the relevant information for validating a single class, property or
 * method invocation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class BeanValueContext<T, V> implements ValueContext<T, V> {

	private final ExecutableParameterNameProvider parameterNameProvider;

	/**
	 * The current bean which gets validated. This is the bean hosting the constraints which get validated.
	 */
	private final T currentBean;

	/**
	 * The class of the current bean.
	 */
	private final Class<T> currentBeanType;

	/**
	 * The metadata of the current bean.
	 */
	private final BeanMetaData<T> currentBeanMetaData;

	/**
	 * The current property path we are validating.
	 */
	private PathImpl propertyPath;

	/**
	 * The current group we are validating.
	 */
	private Class<?> currentGroup;

	/**
	 * The value which gets currently evaluated.
	 */
	private V currentValue;

	private final Validatable currentValidatable;

	/**
	 * The {@code ElementType} the constraint was defined on
	 */
	private ElementType elementType;

	public static <T, V> BeanValueContext<T, V> getLocalExecutionContext(BeanMetaDataManager beanMetaDataManager,
																		 ExecutableParameterNameProvider parameterNameProvider, T value, Validatable validatable, PathImpl propertyPath) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanType = (Class<T>) value.getClass();
		return new BeanValueContext<>( parameterNameProvider, value, rootBeanType, beanMetaDataManager.getBeanMetaData( rootBeanType ), validatable, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> BeanValueContext<T, V> getLocalExecutionContext(ExecutableParameterNameProvider parameterNameProvider, T value,
																		 BeanMetaData<?> currentBeanMetaData, PathImpl propertyPath) {
		Class<T> rootBeanType = (Class<T>) value.getClass();
		return new BeanValueContext<>( parameterNameProvider, value, rootBeanType, (BeanMetaData<T>) currentBeanMetaData, currentBeanMetaData, propertyPath );
	}

	public static <T, V> BeanValueContext<T, V> getLocalExecutionContext(BeanMetaDataManager beanMetaDataManager,
																		 ExecutableParameterNameProvider parameterNameProvider, Class<T> rootBeanType, Validatable validatable, PathImpl propertyPath) {
		BeanMetaData<T> rootBeanMetaData = rootBeanType != null ? beanMetaDataManager.getBeanMetaData( rootBeanType ) : null;
		return new BeanValueContext<>( parameterNameProvider, null, rootBeanType, rootBeanMetaData, validatable, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> BeanValueContext<T, V> getLocalExecutionContext(ExecutableParameterNameProvider parameterNameProvider, Class<T> currentBeanType,
																		 BeanMetaData<?> currentBeanMetaData, PathImpl propertyPath) {
		return new BeanValueContext<>( parameterNameProvider, null, currentBeanType, (BeanMetaData<T>) currentBeanMetaData, currentBeanMetaData, propertyPath );
	}

	private BeanValueContext(ExecutableParameterNameProvider parameterNameProvider, T currentBean, Class<T> currentBeanType, BeanMetaData<T> currentBeanMetaData, Validatable validatable, PathImpl propertyPath) {
		this.parameterNameProvider = parameterNameProvider;
		this.currentBean = currentBean;
		this.currentBeanType = currentBeanType;
		this.currentBeanMetaData = currentBeanMetaData;
		this.currentValidatable = validatable;
		this.propertyPath = propertyPath;
	}

	@Override
	public final PathImpl getPropertyPath() {
		return propertyPath;
	}

	@Override
	public final Class<?> getCurrentGroup() {
		return currentGroup;
	}

	@Override
	public final T getCurrentBean() {
		return currentBean;
	}

	@Override
	public final Class<T> getCurrentBeanType() {
		return currentBeanType;
	}

	public final BeanMetaData<T> getCurrentBeanMetaData() {
		return currentBeanMetaData;
	}

	@Override
	public Validatable getCurrentValidatable() {
		return currentValidatable;
	}

	/**
	 * Returns the current value to be validated.
	 */
	@Override
	public final Object getCurrentValidatedValue() {
		return currentValue;
	}

	@Override
	public final void appendNode(Cascadable node) {
		PathImpl newPath = PathImpl.createCopy( propertyPath );
		node.appendTo( newPath );
		propertyPath = newPath;
	}

	@Override
	public final void appendNode(ConstraintLocation location) {
		PathImpl newPath = PathImpl.createCopy( propertyPath );
		location.appendTo( parameterNameProvider, newPath );
		propertyPath = newPath;
	}

	@Override
	public final void appendTypeParameterNode(String nodeName) {
		PathImpl newPath = PathImpl.createCopy( propertyPath );
		newPath.addContainerElementNode( nodeName );
		propertyPath = newPath;
	}

	@Override
	public final void markCurrentPropertyAsIterable() {
		propertyPath.makeLeafNodeIterable();
	}

	@Override
	public final void markCurrentPropertyAsIterableAndSetKey(Object key) {
		propertyPath.makeLeafNodeIterableAndSetMapKey( key );
	}

	@Override
	public final void markCurrentPropertyAsIterableAndSetIndex(Integer index) {
		propertyPath.makeLeafNodeIterableAndSetIndex( index );
	}

	/**
	 * Sets the container element information.
	 *
	 * @param containerClass the class of the container
	 * @param typeParameterIndex the index of the actual type parameter
	 *
	 * @see TypeVariables#getContainerClass(TypeVariable)
	 * @see TypeVariables#getActualTypeParameter(TypeVariable)
	 * @see AnnotatedObject
	 * @see ArrayElement
	 */
	@Override
	public final void setTypeParameter(Class<?> containerClass, Integer typeParameterIndex) {
		if ( containerClass == null ) {
			return;
		}

		propertyPath.setLeafNodeTypeParameter( containerClass, typeParameterIndex );
	}

	@Override
	public final void setCurrentGroup(Class<?> currentGroup) {
		this.currentGroup = currentGroup;
	}

	@Override
	public final void setCurrentValidatedValue(V currentValue) {
		propertyPath.setLeafNodeValueIfRequired( currentValue );
		this.currentValue = currentValue;
	}

	@Override
	public final boolean validatingDefault() {
		return getCurrentGroup() != null && getCurrentGroup().getName().equals( Default.class.getName() );
	}

	@Override
	public final ElementType getElementType() {
		return elementType;
	}

	@Override
	public final void setElementType(ElementType elementType) {
		this.elementType = elementType;
	}

	@Override
	public final ValueState<V> getCurrentValueState() {
		return new ValueState<V>( propertyPath, currentValue );
	}

	@Override
	public final void resetValueState(ValueState<V> valueState) {
		this.propertyPath = valueState.getPropertyPath();
		this.currentValue = valueState.getCurrentValue();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BeanValueContext" );
		sb.append( "{currentBean=" ).append( currentBean );
		sb.append( ", currentBeanType=" ).append( currentBeanType );
		sb.append( ", propertyPath=" ).append( propertyPath );
		sb.append( ", currentGroup=" ).append( currentGroup );
		sb.append( ", currentValue=" ).append( currentValue );
		sb.append( ", elementType=" ).append( elementType );
		sb.append( '}' );
		return sb.toString();
	}

	@Override
	public Object getValue(Object parent, ConstraintLocation location) {
		// TODO: For BVAL-214 we'd get the value from a map or another alternative structure instead
		return location.getValue( parent );
	}

	@Override
	public boolean defaultGroupSequenceIsRedefined() {
		return currentBeanMetaData.defaultGroupSequenceIsRedefined();
	}

	@Override
	public List<Class<?>> getDefaultGroupSequence(T currentBean) {
		return currentBeanMetaData.getDefaultGroupSequence( currentBean );
	}

	@Override
	public Iterable<MetaConstraint<?>> getCurrentMetaConstraints() {
		return currentBeanMetaData.getMetaConstraints();
	}

}
