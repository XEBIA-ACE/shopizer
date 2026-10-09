package com.salesmanager.test.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.sql.DataSource;

import org.hibernate.Version;
import org.junit.Test;
import org.springframework.boot.SpringBootVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.salesmanager.core.business.repositories.catalog.product.ProductRepository;
import com.salesmanager.core.business.repositories.merchant.MerchantRepository;
import com.salesmanager.core.business.repositories.merchant.PageableMerchantRepository;
import com.salesmanager.core.business.repositories.system.SystemConfigurationRepository;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.system.SystemConfiguration;
import com.salesmanager.test.common.AbstractSalesManagerCoreTestCase;

/**
 * Verifies the persistence stack (Spring Boot managed Hibernate, Spring Data JPA, HikariCP and the H2 or MySQL driver)
 * against the shopizer schema and repositories.
 */
public class DatabaseCompatibilityTest extends AbstractSalesManagerCoreTestCase {

	private static final String SCHEMA = "SALESMANAGER";

	@Inject
	private DataSource dataSource;

	@Inject
	private EntityManagerFactory entityManagerFactory;

	@Inject
	private PlatformTransactionManager transactionManager;

	@Inject
	private SystemConfigurationRepository systemConfigurationRepository;

	@Inject
	private MerchantRepository merchantRepository;

	@Inject
	private PageableMerchantRepository pageableMerchantRepository;

	@Inject
	private ProductRepository productRepository;

	@Test
	public void persistenceStackRunsOnBootManagedVersions() throws Exception {
		assertTrue("Spring Boot " + SpringBootVersion.getVersion(), SpringBootVersion.getVersion().startsWith("2.7."));
		assertTrue("Hibernate " + Version.getVersionString(), Version.getVersionString().startsWith("5.6."));

		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			String product = metaData.getDatabaseProductName();
			String version = product + " " + metaData.getDatabaseProductVersion();
			if ("H2".equals(product)) {
				assertTrue(version, metaData.getDatabaseMajorVersion() >= 2);
			} else {
				assertEquals("MySQL", product);
				assertTrue(version, metaData.getDatabaseMajorVersion() >= 8);
			}
			assertTrue(connection.isValid(5));
		}
	}

	@Test
	public void everyMappedEntityHasATableAndIsQueryable() throws Exception {
		Set<String> tables = new TreeSet<>();
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			boolean catalogIsSchema = "MySQL".equals(metaData.getDatabaseProductName());
			try (ResultSet rs = metaData.getTables(catalogIsSchema ? SCHEMA : null, catalogIsSchema ? null : SCHEMA, "%",
					new String[] { "TABLE" })) {
				while (rs.next()) {
					tables.add(rs.getString("TABLE_NAME").toUpperCase());
				}
			}
		}
		assertTrue("schema " + SCHEMA + " has no tables", tables.size() > 50);

		List<String> failures = new ArrayList<>();
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		try {
			for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
				try {
					entityManager.createQuery("select count(e) from " + entity.getName() + " e", Long.class)
							.getSingleResult();
				} catch (RuntimeException e) {
					failures.add(entity.getName() + ": " + e.getMessage());
				}
			}
		} finally {
			entityManager.close();
		}
		assertTrue("Entities not queryable: " + failures, failures.isEmpty());
	}

	@Test
	public void reservedKeywordColumnsRoundTrip() {
		SystemConfiguration configuration = new SystemConfiguration();
		configuration.setKey("DB_COMPAT_KEY");
		configuration.setValue("db-compat-value");
		systemConfigurationRepository.saveAndFlush(configuration);

		SystemConfiguration loaded = systemConfigurationRepository.findByKey("DB_COMPAT_KEY");
		assertNotNull(loaded);
		assertEquals("db-compat-value", loaded.getValue());

		systemConfigurationRepository.delete(loaded);
		assertNull(systemConfigurationRepository.findByKey("DB_COMPAT_KEY"));
	}

	@Test
	public void nativeQueriesResolveSchemaPlaceholderAndNullParameters() throws Exception {
		MerchantStore store = merchantService.getByCode(MerchantStore.DEFAULT_STORE);
		assertNotNull(store);

		List<MerchantStore> group = merchantRepository.listByGroup(MerchantStore.DEFAULT_STORE, null);
		assertFalse(group.isEmpty());

		Page<MerchantStore> page = pageableMerchantRepository.listByGroup(MerchantStore.DEFAULT_STORE, null, null,
				PageRequest.of(0, 10));
		assertTrue(page.getTotalElements() >= 1);

		assertTrue(productRepository.findBySku("DB-COMPAT-UNKNOWN-SKU", store.getId()).isEmpty());
	}

	@Test
	public void jpqlPaginationAndSorting() {
		Page<MerchantStore> page = pageableMerchantRepository.listAll(null, PageRequest.of(0, 5));
		assertTrue(page.getTotalElements() >= 1);
		assertEquals(MerchantStore.DEFAULT_STORE, page.getContent().get(0).getCode());

		assertFalse(merchantRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).isEmpty());
		assertFalse(merchantRepository.findAllStoreCodeNameEmail().isEmpty());
	}

	@Test
	public void rolledBackTransactionIsNotPersisted() {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.executeWithoutResult(status -> {
			SystemConfiguration configuration = new SystemConfiguration();
			configuration.setKey("DB_COMPAT_ROLLBACK");
			configuration.setValue("rolled-back");
			systemConfigurationRepository.saveAndFlush(configuration);
			assertNotNull(systemConfigurationRepository.findByKey("DB_COMPAT_ROLLBACK"));
			status.setRollbackOnly();
		});
		assertNull(systemConfigurationRepository.findByKey("DB_COMPAT_ROLLBACK"));
	}
}
