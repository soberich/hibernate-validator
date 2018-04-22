/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.cfg.json.PropertyHolderConstraintMappingImpl;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.PropertyHolderConfiguration;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * A Json {@link MetaDataProvider} based on the programmatic constraint API.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
public class ProgrammaticPropertyHolderMetaDataProvider {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Immutable
	private final Map<String, PropertyHolderConfiguration> configuredBeans;

	public ProgrammaticPropertyHolderMetaDataProvider(ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager,
			Set<PropertyHolderConstraintMappingImpl> constraintMappings) {
		Contracts.assertNotNull( constraintMappings );

		configuredBeans = CollectionHelper.toImmutableMap(
				createBeanConfigurations( constraintMappings, constraintHelper, typeResolutionHelper, valueExtractorManager )
		);

		assertUniquenessOfConfiguredTypes( constraintMappings );
	}

	private static void assertUniquenessOfConfiguredTypes(Set<PropertyHolderConstraintMappingImpl> mappings) {
		Set<String> allConfiguredTypes = newHashSet();

		for ( PropertyHolderConstraintMappingImpl constraintMapping : mappings ) {
			for ( String propertyHolderMappingName : constraintMapping.getConfiguredMappingNames() ) {
				if ( allConfiguredTypes.contains( propertyHolderMappingName ) ) {
					throw LOG.getPropertyHolderMappingHasAlreadyBeenConfiguredViaProgrammaticApiException( propertyHolderMappingName );
				}
			}

			allConfiguredTypes.addAll( constraintMapping.getConfiguredMappingNames() );
		}
	}

	private static Map<String, PropertyHolderConfiguration> createBeanConfigurations(Set<PropertyHolderConstraintMappingImpl> mappings, ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager) {
		final Map<String, PropertyHolderConfiguration> configuredBeans = new HashMap<>();
		for ( PropertyHolderConstraintMappingImpl mapping : mappings ) {
			Set<PropertyHolderConfiguration> beanConfigurations = mapping.getPropertyHolderConfigurations( constraintHelper, typeResolutionHelper,
					valueExtractorManager
			);

			for ( PropertyHolderConfiguration beanConfiguration : beanConfigurations ) {
				configuredBeans.put( beanConfiguration.getMappingName(), beanConfiguration );
			}
		}
		return configuredBeans;
	}

	public PropertyHolderConfiguration getPropertyHolderConfiguration(String propertyHolderMappingName) {
		return configuredBeans.get( propertyHolderMappingName );
	}

}
