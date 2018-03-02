/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.ValidationTypeResolutionHelper;

/**
 * Parameter constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ParameterConstraintLocation implements ConstraintLocation {

	private final Executable executable;
	private final int index;
	private final Type typeForValidatorResolution;
	private final String name;

	ParameterConstraintLocation(Executable executable, String name, int index) {
		this.executable = executable;
		this.name = name;
		this.index = index;
		this.typeForValidatorResolution = ValidationTypeResolutionHelper.getTypeForValidatorResolution( executable, index );
	}

	@Override
	public Class<?> getDeclaringClass() {
		return executable.getDeclaringClass();
	}

	@Override
	public Member getMember() {
		return executable;
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public void appendTo(PathImpl path) {
		path.addParameterNode( name, index );
	}

	@Override
	public Object getValue(Object parent) {
		return ( (Object[]) parent )[index];
	}

	@Override
	public String toString() {
		return "ParameterConstraintLocation [executable=" + executable + ", index=" + index
				+ ", typeForValidatorResolution=" + typeForValidatorResolution + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( executable == null ) ? 0 : executable.hashCode() );
		result = prime * result + index;
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
		ParameterConstraintLocation other = (ParameterConstraintLocation) obj;
		if ( executable == null ) {
			if ( other.executable != null ) {
				return false;
			}
		}
		else if ( !executable.equals( other.executable ) ) {
			return false;
		}
		if ( index != other.index ) {
			return false;
		}
		return true;
	}
}
