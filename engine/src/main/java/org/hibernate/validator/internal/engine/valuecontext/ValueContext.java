package org.hibernate.validator.internal.engine.valuecontext;

import java.lang.annotation.ElementType;
import java.util.List;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;

/**
 * @author Marko Bekhta
 */
public interface ValueContext<T, V> {
	PathImpl getPropertyPath();

	Class<?> getCurrentGroup();

	T getCurrentBean();

	Class<T> getCurrentBeanType();

	Validatable getCurrentValidatable();

	Object getCurrentValidatedValue();

	void appendNode(Cascadable node);

	void appendNode(ConstraintLocation location);

	void appendTypeParameterNode(String nodeName);

	void markCurrentPropertyAsIterable();

	void markCurrentPropertyAsIterableAndSetKey(Object key);

	void markCurrentPropertyAsIterableAndSetIndex(Integer index);

	void setTypeParameter(Class<?> containerClass, Integer typeParameterIndex);

	void setCurrentGroup(Class<?> currentGroup);

	void setCurrentValidatedValue(V currentValue);

	boolean validatingDefault();

	ElementType getElementType();

	void setElementType(ElementType elementType);

	ValueState<V> getCurrentValueState();

	void resetValueState(ValueState<V> valueState);

	Object getValue(Object parent, ConstraintLocation location);

	boolean defaultGroupSequenceIsRedefined();

	List<Class<?>> getDefaultGroupSequence(T currentBean);

	Iterable<MetaConstraint<?>> getCurrentMetaConstraints();
}
