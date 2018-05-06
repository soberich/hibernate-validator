/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.propertyholder;

import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.properties.propertyholder.map.MapPropertyCreator;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.GetInstancesFromServiceLoader;
import org.hibernate.validator.spi.propertyholder.PropertyCreator;

/**
 * @author Marko Bekhta
 */
public class PropertyCreatorProvider {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Map<Class<?>, PropertyCreator<?>> configuredPropertyCreators = new HashMap<>();

	public PropertyCreatorProvider() {
		//add default property creator for a Map
		configuredPropertyCreators.put( Map.class, new MapPropertyCreator() );

		List<PropertyCreator> propertyCreators = run( GetInstancesFromServiceLoader.action(
				run( GetClassLoader.fromContext() ),
				PropertyCreator.class
		) );
		for ( PropertyCreator propertyCreator : propertyCreators ) {
			configuredPropertyCreators.put( propertyCreator.getPropertyHolderType(), propertyCreator );
		}
	}

	@SuppressWarnings("unchecked")
	public <T> PropertyCreator<T> getPropertyCreatorFor(Class<T> propertyHolderType) {
		PropertyCreator<T> propertyCreator = (PropertyCreator<T>) configuredPropertyCreators.get( propertyHolderType );
		if ( propertyCreator == null ) {
			throw LOG.getUnableToFindPropertyCreatorException( propertyHolderType );
		}
		return propertyCreator;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 *
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

}
