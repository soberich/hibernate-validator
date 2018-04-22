/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata;

import static org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.Option.IDENTITY_COMPARISONS;
import static org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.ReferenceType.SOFT;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.util.EnumSet;
import java.util.Set;

import org.hibernate.validator.internal.cfg.json.PropertyHolderConstraintMappingImpl;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.PropertyHolderMetaData;
import org.hibernate.validator.internal.metadata.aggregated.PropertyHolderMetaDataImpl;
import org.hibernate.validator.internal.metadata.aggregated.PropertyHolderMetaDataImpl.PropertyHolderMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.ProgrammaticPropertyHolderMetaDataProvider;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * Provides meta data for property holder constraints.
 *
 * @author Marko Bekhta
 */
public class PropertyHolderMetaDataManager {

	/**
	 * The default initial capacity for this cache.
	 */
	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * The default load factor for this cache.
	 */
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The default concurrency level for this cache.
	 */
	private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final ConcurrentReferenceHashMap<String, PropertyHolderMetaData> propertyHolderMetaDataCache;

	private final ProgrammaticPropertyHolderMetaDataProvider defaultProvider;

	private final PropertyHolderMetaDataBuilder builder;

	public PropertyHolderMetaDataManager(ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager,
			Set<PropertyHolderConstraintMappingImpl> constraintMappings) {
		this.builder = PropertyHolderMetaDataBuilder.getInstance( constraintHelper, typeResolutionHelper, valueExtractorManager );

		this.propertyHolderMetaDataCache = new ConcurrentReferenceHashMap<>(
				DEFAULT_INITIAL_CAPACITY,
				DEFAULT_LOAD_FACTOR,
				DEFAULT_CONCURRENCY_LEVEL,
				SOFT,
				SOFT,
				EnumSet.of( IDENTITY_COMPARISONS )
		);

		defaultProvider = new ProgrammaticPropertyHolderMetaDataProvider(
				constraintHelper,
				typeResolutionHelper,
				valueExtractorManager,
				constraintMappings
		);
	}

	public PropertyHolderMetaData getPropertyHolderMetaData(String mappingName) {
		Contracts.assertNotNull( mappingName, MESSAGES.mappingNameMustNotBeNull() );

		return propertyHolderMetaDataCache.computeIfAbsent( mappingName,
				bc -> createPropertyHolderMetaData( mappingName )
		);
	}

	public void clear() {
		propertyHolderMetaDataCache.clear();
	}

	public int numberOfCachedBeanMetaDataInstances() {
		return propertyHolderMetaDataCache.size();
	}

	private PropertyHolderMetaDataImpl createPropertyHolderMetaData(String mappingName) {
		return builder.build( defaultProvider.getPropertyHolderConfiguration( mappingName ) );
	}

}
