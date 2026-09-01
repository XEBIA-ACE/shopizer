--- pom.xml ---
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://www.apache.org/maven-v4_0_0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.shopizer</groupId>
	<artifactId>shopizer</artifactId>
	<packaging>pom</packaging>
	<version>3.2.3</version>

	<name>shopizer</name>
	<url>http://ww.shopizer.com</url>

	<licenses>
		<license>
			<name>Apache License, Version 2.0</name>
			<url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
		</license>
	</licenses>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.2.0</version> <!-- Upgraded version -->
	</parent>

	<modules>
		<module>sm-core-model</module>
		<module>sm-core-modules</module>
		<module>sm-core</module>
		<module>sm-shop-model</module>
		<module>sm-shop</module>
	</modules>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

		<!--java version -->
		<java.version>17</java.version> <!-- Minimum required Java version for Spring Boot 3.x -->

		<maven.compiler.source>${java.version}</maven.compiler.source>
		<maven.compiler.target>${java.version}</maven.compiler.target>

		<shopizer.search.version>2.11.1</shopizer.search.version>
		<shopizer-canadapost.version>2.15.0</shopizer-canadapost.version>

		<!-- TODO replace with starter -->
		<elasticsearch.version>7.5.2</elasticsearch.version>
		<guava.version>27.1-jre</guava.version>
		<commons-lang.version>3.5</commons-lang.version>
		<commons-io.version>2.7</commons-io.version>
		<commons-collections4.version>4.1</commons-collections4.version>
		<commons-validator.version>1.5.1</commons-validator.version>
		<commons-fileupload>1.3.3</commons-fileupload>
		<org.mapstruct.version>1.3.0.Final</org.mapstruct.version>

		<org.apache.httpcomponent.version>4.5.2</org.apache.httpcomponent.version>
		<javax.el.version>3.0.1</javax.el.version>
		<infinispan.version>14.0.6.Final</infinispan.version> <!-- Latest stable version -->
		<mysql-jdbc-version>8.0.21</mysql-jdbc-version>
		<oracle.version>18.3.0.0</oracle.version>
		<postgresql.version>42.2.18</postgresql.version>
		<simple-json-version>1.1.1</simple-json-version>
		<jackson-version-databind>2.12.6.1</jackson-version-databind>
		<jackson-version>2.10.2</jackson-version>
		<geoip2.version>2.7.0</geoip2.version>
		<drools.version>7.32.0.Final</drools.version>
		<google-client-maps-services-version>0.1.6</google-client-maps-services-version>
		<jwt.version>0.8.0</jwt.version>

		<!-- api documentation -->
		<swagger.version>2.9.2</swagger.version>
		<!-- Must be updated to an alternative due to springfox deprecation in Spring Boot 3.x -->

		<!-- jacoco coverage -->
		<coverage.lines>.30</coverage.lines>
		<coverage.branches>.37</coverage.branches>

	</properties>

	<!--BOM -->
	<dependencyManagement>
		<dependencies>

			<dependency>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-starter-web</artifactId>
			</dependency>
			<dependency>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-starter-cache</artifactId>
			</dependency>
			<dependency>
				<groupId>com.shopizer</groupId>
				<artifactId>sm-core</artifactId>
				<version>3.2.3</version>
			</dependency>
			<dependency>
				<groupId>com.shopizer</groupId>
				<artifactId>sm-core-model</artifactId>
				<version>3.2.3</version>
			</dependency>
			<dependency>
				<groupId>com.shopizer</groupId>
				<artifactId>sm-core-modules</artifactId>
				<version>3.2.3</version>
			</dependency>
			<dependency>
				<groupId>com.shopizer</groupId>
				<artifactId>sm-shop-model</artifactId>
				<version>3.2.3</version>
			</dependency>

			<!-- https://mvnrepository.com/artifact/javax.inject/javax.inject -->
			<dependency>
				<groupId>jakarta.inject</groupId> <!-- Namespace upgrade -->
				<artifactId>jakarta.inject-api</artifactId>
				<version>2.0.1</version>
			</dependency>

			<!-- https://mvnrepository.com/artifact/org.apache.commons/commons-lang3 -->
			<dependency>
				<groupId>org.apache.commons</groupId>
				<artifactId>commons-lang3</artifactId>
				<version>${commons-lang.version}</version>
			</dependency>

			<dependency>
				<groupId>org.mapstruct</groupId>
				<artifactId>mapstruct</artifactId>
				<version>${org.mapstruct.version}</version>
			</dependency>

			<dependency>
				<groupId>io.jsonwebtoken</groupId>
				<artifactId>jjwt</artifactId>
				<version>${jwt.version}</version>
			</dependency>

			<!-- Jackson JSON Processor -->
			<dependency>
				<groupId>com.fasterxml.jackson.core</groupId>
				<artifactId>jackson-databind</artifactId>
				<version>${jackson-version-databind}</version>
			</dependency>

			<dependency>
				<groupId>com.fasterxml.jackson.core</groupId>
				<artifactId>jackson-core</artifactId>
				<version>${jackson-version}</version>
			</dependency>

			<dependency>
				<groupId>com.fasterxml.jackson.core</groupId>
				<artifactId>jackson-annotations</artifactId>
				<version>${jackson-version}</version>
			</dependency>

			<!-- https://mvnrepository.com/artifact/com.sun.mail/javax.mail -->
			<dependency>
				<groupId>com.sun.mail</groupId>
				<artifactId>jakarta.mail</artifactId> <!-- Migrated to Jakarta -->
				<version>2.0.1</version>
			</dependency>

			<!-- http://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple -->
			<dependency>
				<groupId>com.googlecode.json-simple</groupId>
				<artifactId>json-simple</artifactId>
				<version>${simple-json-version}</version>
			</dependency>

			<dependency>
				<groupId>mysql</groupId>
				<artifactId>mysql-connector-java</artifactId>
				<version>${mysql-jdbc-version}</version>
			</dependency>

			<!-- For connecting to oracle -->
			<!-- <dependency> <groupId>com.oracle.database.jdbc</groupId> <artifactId>ojdbc8</artifactId> 
				<version>${oracle.version}</version> </dependency> -->


			<!-- For connecting to postgresql -->
			<!-- <dependency> <groupId>org.postgresql</groupId> <artifactId>postgresql</artifactId> 
				<version>${postgresql.version}</version> <scope>runtime</scope> </dependency> -->

			<!-- Google Map API -->
			<dependency>
				<groupId>com.google.maps</groupId>
				<artifactId>google-maps-services</artifactId>
				<version>${google-client-maps-services-version}</version>
			</dependency>

			<dependency>
				<groupId>org.kie</groupId>
				<artifactId>kie-ci</artifactId>
				<version>${drools.version}</version>
				<exclusions>
					<exclusion>
						<groupId>com.google.guava</groupId>
						<artifactId>guava</artifactId>
					</exclusion>
				</exclusions>
			</dependency>
			<dependency>
				<groupId>org.drools</groupId>
				<artifactId>drools-decisiontables</artifactId>
				<version>${drools.version}</version>
			</dependency>
			<dependency>
				<groupId>org.drools</groupId>
				<artifactId>drools-core</artifactId>
				<version>${drools.version}</version>
			</dependency>
			<dependency>
				<groupId>org.drools</groupId>
				<artifactId>drools-compiler</artifactId>
				<version>${drools.version}</version>
			</dependency>
			<!--spring integration -->
			<dependency>
				<groupId>org.kie</groupId>
				<artifactId>kie-spring</artifactId>
				<version>${drools.version}</version>
			</dependency>
			<!-- end rules engine -->

			<!-- Infinispan -->
			<dependency>
				<groupId>org.infinispan</groupId>
				<artifactId>infinispan-core</artifactId>
				<version>${infinispan.version}</version>
			</dependency>

			<dependency>
				<groupId>org.infinispan</groupId>
				<artifactId>infinispan-cachestore-jdbc</artifactId>
				<version>${infinispan.version}</version>
			</dependency>

			<dependency>
				<groupId>org.infinispan</groupId>
				<artifactId>infinispan-tree</artifactId>
				<version>${infinispan.version}</version>
			</dependency>

			<!-- https://mvnrepository.com/artifact/org.apache.commons/commons-collections4 -->
			<dependency>
				<groupId>org.apache.commons</groupId>
				<artifactId>commons-collections4</artifactId>
				<version>${commons-collections4.version}</version>
			</dependency>


			<!-- https://mvnrepository.com/artifact/commons-validator/commons-validator -->
			<dependency>
				<groupId>commons-validator</groupId>
				<artifactId>commons-validator</artifactId>
				<version>${commons-validator.version}</version>

				<exclusions>
					<exclusion>
						<groupId>commons-collections</groupId>
						<artifactId>commons-collections</artifactId>
					</exclusion>
				</exclusions>
			</dependency>

			<!-- https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-s3 -->
			<dependency>
				<groupId>com.amazonaws</groupId>
				<artifactId>aws-java-sdk-s3</artifactId>
				<version>1.11.640</version>
			</dependency>

			<!-- https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-ses -->
			<dependency>
				<groupId>com.amazonaws</groupId>
				<artifactId>aws-java-sdk-ses</artifactId>
				<version>1.11.640</version>
			</dependency>

			<!-- google cloud storage -->
			<dependency>
				<groupId>com.google.cloud</groupId>
				<artifactId>google-cloud-storage</artifactId>
				<version>1.74.0</version>
				<exclusions>
					<exclusion>
						<groupId>com.google.guava</groupId>
						<artifactId>guava</artifactId>
					</exclusion>
				</exclusions>
			</dependency>


			<!-- Payment dependencies -->

			<!-- Paypal -->
			<dependency>
				<groupId>com.paypal.sdk</groupId>
				<artifactId>merchantsdk</artifactId>
				<version>2.6.109</version>
			</dependency>

			<!-- Stripe -->
			<dependency>
				<groupId>com.stripe</groupId>
				<artifactId>stripe-java</artifactId>
				<version>19.5.0</version>
			</dependency>

			<!-- Braintree -->
			<dependency>
				<groupId>com.braintreepayments.gateway</groupId>
				<artifactId>braintree-java</artifactId>
				<version>2.73.0</version>
			</dependency>

			<!-- https://mvnrepository.com/artifact/com.maxmind.geoip2/geoip2 -->
			<dependency>
				<groupId>com.maxmind.geoip2</groupId>
				<artifactId>geoip2</artifactId>
				<version>${geoip2.version}</version>
			</dependency>

		</dependencies>
	</dependencyManagement>
</project>

--- sm-shop/src/main/java/com/salesmanager/shop/store/facade/shipping/ShippingConfigurationFacadeImpl.java ---
package com.salesmanager.shop.store.facade.shipping;

import java.util.List;

import org.springframework.stereotype.Service;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.shop.model.configuration.PersistableConfiguration;
import com.salesmanager.shop.model.configuration.ReadableConfiguration;
import com.salesmanager.shop.store.controller.configurations.ConfigurationsFacade;

@Service("shippingConfigurationFacade")
public class ShippingConfigurationFacadeImpl implements ConfigurationsFacade {

	@Override
	public List<ReadableConfiguration> configurations(MerchantStore store) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReadableConfiguration configuration(String module, MerchantStore store) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveConfiguration(PersistableConfiguration configuration, MerchantStore store) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteConfiguration(String module, MerchantStore store) {
		// TODO Auto-generated method stub
		
	}

}

--- sm-shop/src/main/java/com/salesmanager/shop/store/controller/security/facade/SecurityFacadeImpl.java ---
package com.salesmanager.shop.store.controller.security.facade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.inject.Inject; // Updated import to jakarta namespace

import org.jsoup.helper.Validate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.user.GroupService;
import com.salesmanager.core.business.services.user.PermissionService;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.PermissionCriteria;
import com.salesmanager.core.model.user.PermissionList;
import com.salesmanager.shop.model.security.ReadablePermission;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;

@Service("securityFacade")
public class SecurityFacadeImpl implements SecurityFacade {
  
  private static final String USER_PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z]).{6,12})";
  
  private Pattern userPasswordPattern = Pattern.compile(USER_PASSWORD_PATTERN);

  @Inject
  private PermissionService permissionService;

  @Inject
  private GroupService groupService;
  
  @Inject
  private PasswordEncoder passwordEncoder;

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public List<ReadablePermission> getPermissions(List<String> groups) {

    List<Group> userGroups = null;
    try {
      userGroups = groupService.listGroupByNames(groups);

      List<Integer> ids = new ArrayList<Integer>();
      for (Group g : userGroups) {
        ids.add(g.getId());
      }

      PermissionCriteria criteria = new PermissionCriteria();
      criteria.setGroupIds(new HashSet(ids));

      PermissionList permissions = permissionService.listByCriteria(criteria);
      throw new ServiceRuntimeException("Not implemented");
    } catch (ServiceException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public boolean validateUserPassword(String password) {

    Matcher matcher = userPasswordPattern.matcher(password);
    return matcher.matches();
  }

  @Override
  public String encodePassword(String password) {
    return passwordEncoder.encode(password);
  }

  /**
   * Match non encoded to encoded
   * Don't use this as a simple raw password check
   */
  @Override
  public boolean matchPassword(String modelPassword, String newPassword) {
    return passwordEncoder.matches(newPassword, modelPassword);
  }

@Override
public boolean matchRawPasswords(String password, String repeatPassword) {
	Validate.notNull(password,"password is null");
	Validate.notNull(repeatPassword,"repeat password is null");
	return password.equals(repeatPassword);
}
  
  

}

--- sm-shop/src/main/java/com/salesmanager/shop/application/config/AsyncConfig.java ---
package com.salesmanager.shop.application.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  private static final int EXECUTOR_SERVICE_NUMBER_THREADS = 5;

  @Override
  public Executor getAsyncExecutor() {
    return Executors.newFixedThreadPool(EXECUTOR_SERVICE_NUMBER_THREADS);
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new SimpleAsyncUncaughtExceptionHandler();
  }
}

--- sm-shop/src/main/java/com/salesmanager/shop/application/config/DocumentationConfiguration.java ---
package com.salesmanager.shop.application.config;

import static io.swagger.models.auth.In.HEADER;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class DocumentationConfiguration {

	public static final Contact DEFAULT_CONTACT = new Contact("Shopizer", "https://www.shopizer.com", "");
	
	private static final String HOST = "localhost:8080";

	/**
	 * http://localhost:8080/swagger-ui.html#/ http://localhost:8080/v2/api-docs
	 */

	@Bean
	public Docket api() {

		final List<ResponseMessage> getMessages = new ArrayList<ResponseMessage>();
		getMessages.add(new ResponseMessageBuilder().code(500).message("500 message")
				.responseModel(new ModelRef("Error")).build());
		getMessages.add(new ResponseMessageBuilder().code(403).message("Forbidden").build());
		getMessages.add(new ResponseMessageBuilder().code(401).message("Unauthorized").build());

		Set<String> produces = new HashSet<>();
		produces.add("application/json");

		Set<String> consumes = new HashSet<>();
		consumes.add("application/json");

		return new Docket(DocumentationType.SWAGGER_2)
				.host(HOST)
				.select()
				.apis(requestHandlers()).build()
				.securitySchemes(Collections.singletonList(new ApiKey("JWT", AUTHORIZATION, HEADER.name())))
		        .securityContexts(singletonList(
		            SecurityContext.builder()
		                .securityReferences(
		                    singletonList(SecurityReference.builder()
		                        .reference("JWT")
		                        .scopes(new AuthorizationScope[0])
		                        .build()
		                    )
		                )
		                .build())
		        )
				.produces(produces).consumes(consumes).globalResponseMessage(RequestMethod.GET, getMessages)
	            .globalResponseMessage(RequestMethod.GET, getMessages);

	}
	
	final Predicate<RequestHandler> requestHandlers() {
		
		   Set<Predicate<RequestHandler>> matchers = new HashSet<Predicate<RequestHandler>>();
		   matchers.add(RequestHandlerSelectors.basePackage("com.salesmanager.shop.store.api.v1"));
		   matchers.add(RequestHandlerSelectors.basePackage("com.salesmanager.shop.store.api.v2"));
		   
		   return Predicates.or(matchers);

	}

	@SuppressWarnings("rawtypes")
	private ApiInfo apiInfo() {
		return new ApiInfo("Shopizer REST API",
				"API for Shopizer e-commerce. Contains public end points as well as private end points requiring basic authentication and remote authentication based on jwt bearer token. URL patterns containing /private/** use bearer token; those are authorized customer and administrators administration actions.",
				"1.0", "urn:tos", DEFAULT_CONTACT, "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0",
				new ArrayList<VendorExtension>());

	}

	private static ArrayList<? extends SecurityScheme> securitySchemes() {
		return (ArrayList<? extends SecurityScheme>) Stream.of(new ApiKey("Bearer", "Authorization", "header"))
				.collect(Collectors.toList());
	}

}

--- sm-core-modules/pom.xml ---
<?xml version="1.0"?>
<project
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://www.apache.org/xsd/maven-4.0.0.xsd"
  xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>


  <parent>
    <groupId>com.shopizer</groupId>
    <artifactId>shopizer</artifactId>
    <version>3.2.3</version>
  </parent>


  <name>sm-core-modules</name>
  <artifactId>sm-core-modules</artifactId>
  <description>sm-core-modules is used for create new external modules implementation deployed in
    maven.
  </description>
  <packaging>jar</packaging>
  <url>http://www.shopizer.com</url>

  <distributionManagement>
    <snapshotRepository>
      <id>ossrh</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    </snapshotRepository>
    <repository>
      <id>ossrh</id>
      <url>https://oss.sonatype.org/service/local/staging/deploy/maven2/</url>
    </repository>
  </distributionManagement>

  <scm>
    <connection>scm:git:git://github.com:shopizer-ecommerce/shopizer-sm-core-modules.git
    </connection>
    <developerConnection>scm:git:ssh://github.com:shopizer-ecommerce/shopizer-sm-core-modules.git
    </developerConnection>
    <url>https://github.com/shopizer-ecommerce/shopizer-sm-core-modules</url>
  </scm>

  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>

  <developers>
    <developer>
      <name>Shopizer Team</name>
      <email>team@shopizer.com</email>
      <organization>Shopizer</organization>
      <organizationUrl>http://www.shopizer.com</organizationUrl>
    </developer>
  </developers>

  <dependencies>
    <dependency>
      <groupId>com.shopizer</groupId>
      <artifactId>sm-core-model</artifactId>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <!--
      <plugin>
        <groupId>org.sonatype.plugins</groupId>
        <artifactId>nexus-staging-maven-plugin</artifactId>
        <version>1.6.7</version>
        <extensions>true</extensions>
        <configuration>
          <serverId>ossrh</serverId>
          <nexusUrl>https://oss.sonatype.org/</nexusUrl>
          <autoReleaseAfterClose>true</autoReleaseAfterClose>
        </configuration>
      </plugin>
      
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-release-plugin</artifactId>
        <version>2.5.3</version>
        <configuration>
          <autoVersionSubmodules>true</autoVersionSubmodules>
          <useReleaseProfile>false</useReleaseProfile>
          <releaseProfiles>release</releaseProfiles>
          <goals>deploy</goals>
        </configuration>
      </plugin>
      
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-source-plugin</artifactId>
        <executions>
          <execution>
            <id>attach-sources</id>
            <goals>
              <goal>jar-no-fork</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      
      
	
      		<plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-javadoc-plugin</artifactId>
              <executions>
                <execution>
                  <id>attach-javadocs</id>
                  <goals>
                    <goal>jar</goal>
                  </goals>
                </execution>
              </executions>
            </plugin>
            <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-gpg-plugin</artifactId>
              <version>1.5</version>
              <executions>
                <execution>
                  <id>sign-artifacts</id>
                  <phase>verify</phase>
                  <goals>
                    <goal>sign</goal>
                  </goals>
                </execution>
              </executions>
            </plugin>
            -->
           
    </plugins>
  </build>
</project>

--- sm-shop/src/main/java/com/salesmanager/shop/application/ShopApplication.java ---
package com.salesmanager.shop.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }

}

--- sm-shop/pom.xml ---
<?xml version="1.0"?>

<project
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://www.apache.org/xsd/maven-4.0.0.xsd"
	xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<modelVersion>4.0.0</modelVersion>

	<parent>
		<groupId>com.shopizer</groupId>
		<artifactId>shopizer</artifactId>
		<version>3.2.3</version>
	</parent>

	<artifactId>sm-shop</artifactId>
	<name>sm-shop</name>
	<url>http://www.shopizer.com</url>

	<properties>
		<coverage.lines>.04</coverage.lines>
		<coverage.branches>.01</coverage.branches>
		<commons-rng-simple.version>1.3</commons-rng-simple.version>
	</properties>

	<dependencies>

		<dependency>
			<groupId>com.shopizer</groupId>
			<artifactId>sm-core</artifactId>
		</dependency>

		<dependency>
			<groupId>com.shopizer</groupId>
			<artifactId>sm-core-model</artifactId>
		</dependency>

		<dependency>
			<groupId>com.shopizer</groupId>
			<artifactId>sm-shop-model</artifactId>
		</dependency>

		<!-- Spring boot starters -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-aop</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>

		<dependency>
			<groupId>com.google.guava</groupId>
			<artifactId>guava</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
			<scope>provided</scope>
		</dependency>

		<dependency>
			<groupId>org.apache.tomcat.embed</groupId>
			<artifactId>tomcat-embed-jasper</artifactId>
			<scope>provided</scope>
		</dependency>

		<!-- https://mvnrepository.com/artifact/commons-collections/commons-collections -->
		<!-- For Tiles -->
		<dependency>
			<groupId>commons-collections</groupId>
			<artifactId>commons-collections</artifactId>
			<version>3.2.2</version>
		</dependency>

		<!-- Mapstruct -->
		<dependency>
			<groupId>org.mapstruct</groupId>
			<artifactId>mapstruct</artifactId>
		</dependency>

		<!-- H2 session -->
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
		</dependency>

		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt</artifactId>
		</dependency>

		<!-- Swagger 2 documentation -->
		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger2</artifactId>
		</dependency>

		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger-ui</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>org.springframework.security</groupId>
			<artifactId>spring-security-test</artifactId>
			<scope>test</scope>
		</dependency>

		<!-- https://mvnrepository.com/artifact/org.apache.commons/commons-rng-simple -->
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-rng-simple</artifactId>
			<version>${commons-rng-simple.version}</version>
		</dependency>

		<!-- https://mvnrepository.com/artifact/org.owasp.antisamy/antisamy -->
		<!-- xss filter complement -->
		<dependency>
			<groupId>org.owasp.antisamy</groupId>
			<artifactId>antisamy</artifactId>
			<version>1.6.7</version>
		</dependency>

		<!-- https://mvnrepository.com/artifact/org.passay/passay -->
		<!-- Password rules library -->
		<dependency>
			<groupId>org.passay</groupId>
			<artifactId>passay</artifactId>
			<version>1.6.0</version>
		</dependency>

	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
		<finalName>shopizer</finalName>
	</build>

	<packaging>jar</packaging>
</project>

--- sm-core/src/main/java/com/salesmanager/core/business/utils/SecurityGroupsBuilder.java ---
package com.salesmanager.core.business.utils;

import java.util.ArrayList;
import java.util.List;

import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.GroupType;
import com.salesmanager.core.model.user.Permission;

/**
 * Helper for building security groups and permissions
 * @author carlsamson
 *
 */
public class SecurityGroupsBuilder {
	
	private List<Group> groups = new ArrayList<Group>();
	private Group lastGroup = null;
	
	
	public SecurityGroupsBuilder addGroup(String name, GroupType type) {
		
		Group g = new Group();
		g.setGroupName(name);
		g.setGroupType(type);
		groups.add(g);
		this.lastGroup = g;
		
		return this;
	}
	
	public SecurityGroupsBuilder addPermission(String name) {
		if(this.lastGroup == null) {
			Group g = this.groups.get(0);
			if(g == null) {
				g = new Group();
				g.setGroupName("UNDEFINED");
				g.setGroupType(GroupType.ADMIN);
				groups.add(g);
				this.lastGroup = g;
			}
		}
		
		Permission permission = new Permission();
		permission.setPermissionName(name);
		lastGroup.getPermissions().add(permission);
		
		return this;
	}
	
	public SecurityGroupsBuilder addPermission(Permission permission) {
		
		if(this.lastGroup == null) {
			Group g = this.groups.get(0);
			if(g == null) {
				g = new Group();
				g.setGroupName("UNDEFINED");
				g.setGroupType(GroupType.ADMIN);
				groups.add(g);
				this.lastGroup = g;
			}
		}
		

		lastGroup.getPermissions().add(permission);
		
		return this;
	}
	
	public List<Group> build() {
		return groups;
	}

}

--- sm-core-model/pom.xml ---
<?xml version="1.0"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://www.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<parent>
		<groupId>com.shopizer</groupId>
		<artifactId>shopizer</artifactId>
		<version>3.2.3</version>
	</parent>


	<artifactId>sm-core-model</artifactId>
	<description>sm-core-modules is used for create new external modules implementation deployed in maven.</description>
	<name>sm-core-model</name>
	<url>http://www.shopizer.com</url>

	<properties>
		<java.version>17</java.version> <!-- Updated Java version -->
		<maven.compiler.source>${java.version}</maven.compiler.source>
		<maven.compiler.target>${java.version}</maven.compiler.target>
	</properties>



	<scm>
		<connection>scm:git:git://github.com:shopizer-ecommerce/shopizer-sm-core-model.git</connection>
		<developerConnection>scm:git:ssh://github.com:shopizer-ecommerce/shopizer-sm-core-model.git</developerConnection>
		<url>https://github.com/shopizer-ecommerce/shopizer-sm-core-model</url>
	</scm>

	<distributionManagement>
		<snapshotRepository>
			<id>ossrh</id>
			<url>https://oss.sonatype.org/content/repositories/snapshots</url>
		</snapshotRepository>
		<repository>
			<id>ossrh</id>
			<url>https://oss.sonatype.org/service/local/staging/deploy/maven2/</url>
		</repository>
	</distributionManagement>

	<licenses>
		<license>
			<name>Apache License, Version 2.0</name>
			<url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
		</license>
	</licenses>

	<developers>
		<developer>
			<name>Shopizer Team</name>
			<email>contact@shopizer.com</email>
			<organization>Shopizer</organization>
			<organizationUrl>http://www.shopizer.com</organizationUrl>
		</developer>
	</developers>

	<dependencies>

		<!-- Spring Data JPA -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>

		<dependency>
			<groupId>jakarta.validation</groupId> <!-- Updated to Jakarta -->
			<artifactId>jakarta.validation-api</artifactId>
		</dependency>

		<dependency>
			<groupId>com.fasterxml.jackson.core</groupId>
			<artifactId>jackson-annotations</artifactId>
		</dependency>

		<dependency>
			<groupId>com.fasterxml.jackson.core</groupId>
			<artifactId>jackson-databind</artifactId>
		</dependency>

		<!-- https://mvnrepository.com/artifact/org.json/json -->
		<dependency>
			<groupId>org.json</groupId>
			<artifactId>json</artifactId>
			<version>20211205</version>
		</dependency>

		<!-- https://mvnrepository.com/artifact/com.googlecode.json-simple/json-simple -->
		<dependency>
			<groupId>com.googlecode.json-simple</groupId>
			<artifactId>json-simple</artifactId>
		</dependency>


		<!-- https://mvnrepository.com/artifact/org.apache.commons/commons-lang3 -->
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
		</dependency>


		<!-- For transient Multipart File only -->
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-webmvc</artifactId>
		</dependency>


		<!-- http://mvnrepository.com/artifact/commons-io/commons-io -->
		<dependency>
			<groupId>commons-io</groupId>
			<artifactId>commons-io</artifactId>
		</dependency>

		<!-- http://mvnrepository.com/artifact/commons-fileupload/commons-fileupload -->
		<dependency>
			<groupId>commons-fileupload</groupId>
			<artifactId>commons-fileupload</artifactId>
		</dependency>

	</dependencies>

	<build>
		<plugins>
			<!--
			<plugin>
				<groupId>org.sonatype.plugins</groupId>
				<artifactId>nexus-staging-maven-plugin</artifactId>
				<version>1.6.7</version>
				<extensions>true</extensions>
				<configuration>
					<serverId>ossrh</serverId>
					<nexusUrl>https://oss.sonatype.org/</nexusUrl>
					<autoReleaseAfterClose>true</autoReleaseAfterClose>
				</configuration>
			</plugin>

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-release-plugin</artifactId>
				<version>2.5.3</version>
				<configuration>
					<autoVersionSubmodules>true</autoVersionSubmodules>
					<useReleaseProfile>false</useReleaseProfile>
					<releaseProfiles>release</releaseProfiles>
					<goals>deploy</goals>
				</configuration>
			</plugin>

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-source-plugin</artifactId>
				<executions>
					<execution>
						<id>attach-sources</id>
						<goals>
							<goal>jar-no-fork</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-javadoc-plugin</artifactId>
				<executions>
					<execution>
						<id>attach-javadocs</id>
						<goals>
							<goal>jar</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-gpg-plugin</artifactId>
				<version>1.5</version>
				<executions>
					<execution>
						<id>sign-artifacts</id>
						<phase>verify</phase>
						<goals>
							<goal>sign</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
			-->
		</plugins>
	</build>
</project>

--- sm-shop-model/pom.xml ---
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://www.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>com.shopizer</groupId>
    <artifactId>shopizer</artifactId>
    <version>3.2.3</version>
  </parent>

  <artifactId>sm-shop-model</artifactId>
  <name>sm-shop-model</name>
  <description>sm-shop-model contains Shopizer model objects for api</description>
  <url>http://www.shopizer.com</url>

  <distributionManagement>
    <snapshotRepository>
      <id>ossrh</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    </snapshotRepository>
    <repository>
      <id>ossrh</id>
      <url>https://oss.sonatype.org/service/local/staging/deploy/maven2/</url>
    </repository>
  </distributionManagement>

  <scm>
    <connection>scm:git:git://github.com:shopizer-ecommerce/shopizer-sm-shop-model.git</connection>
    <developerConnection>scm:git:ssh://github.com/shopizer-ecommerce/shopizer-sm-shop-model.git
    </developerConnection>
    <url>https://github.com/shopizer-ecommerce/shopizer-sm-shop-model</url>
  </scm>

  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>

  <developers>
    <developer>
      <name>Shopizer Team</name>
      <email>team@shopizer.com</email>
      <organization>Shopizer</organization>
      <organizationUrl>http://www.shopizer.com</organizationUrl>
    </developer>
  </developers>

  <dependencies>
  
    <!-- sm-core-model -->
    <dependency>
      <groupId>com.shopizer</groupId>
      <artifactId>sm-core-model</artifactId>
    </dependency>
    
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-core</artifactId>
    </dependency>
    
	<dependency>
		<groupId>org.springframework</groupId>
		<artifactId>spring-webmvc</artifactId>
	</dependency>

    <dependency>
      <groupId>jakarta.annotation</groupId> <!-- Updated to Jakarta -->
      <artifactId>jakarta.annotation-api</artifactId>
    </dependency>

    <!-- Swagger 2 documentation -->
    <dependency>
      <groupId>io.springfox</groupId>
      <artifactId>springfox-swagger2</artifactId>
    </dependency>

  </dependencies>

  <build>
    <plugins>
      <!--OSSRH -->
      <!--
      <plugin>
        <groupId>org.sonatype.plugins</groupId>
        <artifactId>nexus-staging-maven-plugin</artifactId>
        <version>1.6.7</version>
        <extensions>true</extensions>
        <configuration>
          <serverId>ossrh</serverId>
          <nexusUrl>https://oss.sonatype.org/</nexusUrl>
          <autoReleaseAfterClose>true</autoReleaseAfterClose>
        </configuration>
      </plugin>

      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-release-plugin</artifactId>
        <version>2.5.3</version>
        <configuration>
          <autoVersionSubmodules>true</autoVersionSubmodules>
          <useReleaseProfile>false</useReleaseProfile>
          <releaseProfiles>release</releaseProfiles>
          <goals>deploy</goals>
        </configuration>
      </plugin>

      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-source-plugin</artifactId>
        <executions>
          <execution>
            <id>attach-sources</id>
            <goals>
              <goal>jar-no-fork</goal>
            </goals>
          </execution>
        </executions>
      </plugin>

      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-javadoc-plugin</artifactId>
        <executions>
          <execution>
            <id>attach-javadocs</id>
            <goals>
              <goal>jar</goal>
            </goals>
            <configuration>
              <additionalparam>${javadoc.opts}</additionalparam>
              <doclint>none</doclint>
              <source>11</source>
    		  <detectJavaApiLink>false</detectJavaApiLink>
            </configuration>
          </execution>
        </executions>
      </plugin>
 
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-gpg-plugin</artifactId>
        <version>1.5</version>
        <executions>
          <execution>
            <id>sign-artifacts</id>
            <phase>verify</phase>
            <goals>
              <goal>sign</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      -->
      
      
    </plugins>
  </build>

</project>

--- sm-shop/src/main/java/com/salesmanager/shop/store/facade/payment/PaymentConfigurationFacadeImpl.java ---
package com.salesmanager.shop.store.facade.payment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.payments.PaymentService;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.payments.PaymentMethod;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.shop.model.configuration.PersistableConfiguration;
import com.salesmanager.shop.model.configuration.ReadableConfiguration;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.controller.configurations.ConfigurationsFacade;


@Service("paymentConfigurationFacade")
public class PaymentConfigurationFacadeImpl implements ConfigurationsFacade {
	
	
	@Autowired
	private PaymentService paymentService;

	@Override
	public List<ReadableConfiguration> configurations(MerchantStore store) {
		
		try {
			
			List<PaymentMethod> methods = paymentService.getAcceptedPaymentMethods(store);
			List<ReadableConfiguration> configurations = 
					methods.stream()
					.map(m -> configuration(m.getInformations(), store)).collect(Collectors.toList());
			return configurations;
			
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Error while getting payment configurations",e);
		}

	}

	@Override
	public ReadableConfiguration configuration(String module, MerchantStore store) {
		
		try {
			
			ReadableConfiguration config = null;
			List<PaymentMethod> methods = paymentService.getAcceptedPaymentMethods(store);
			Optional<ReadableConfiguration> configuration = 
					methods.stream()
					.filter(m -> module.equals(m.getModule().getCode()))
					.map(m -> this.configuration(m.getInformations(), store))
					.findFirst();
			
			if(configuration.isPresent()) {
				config = configuration.get();
			}
			
			return config;
		
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Error while getting payment configuration [" + module + "]",e);
		}

	}

	@Override
	public void saveConfiguration(PersistableConfiguration configuration, MerchantStore store) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteConfiguration(String module, MerchantStore store) {
		// TODO Auto-generated method stub

	}

	
	private ReadableConfiguration configuration(IntegrationConfiguration source, MerchantStore store) {
		
		ReadableConfiguration config = new ReadableConfiguration();
		config.setActive(source.isActive());
		config.setCode(source.getModuleCode());
		config.setKeys(source.getIntegrationKeys());
		config.setIntegrationOptions(source.getIntegrationOptions());
		
		return config;
	}


}

--- sm-core-model/src/main/java/com/salesmanager/core/model/system/MerchantConfiguration.java ---
package com.salesmanager.core.model.system;

import java.io.Serializable;
import jakarta.persistence.Column; // Migrate to Jakarta
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Type;
import com.salesmanager.core.constants.SchemaConstant;
import com.salesmanager.core.model.common.audit.AuditListener;
import com.salesmanager.core.model.common.audit.AuditSection;
import com.salesmanager.core.model.common.audit.Auditable;
import com.salesmanager.core.model.generic.SalesManagerEntity;
import com.salesmanager.core.model.merchant.MerchantStore;

/**
 * Merchant configuration information
 * 
 * @author Carl Samson
 *
 */
@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "MERCHANT_CONFIGURATION",
    uniqueConstraints = @UniqueConstraint(columnNames = {"MERCHANT_ID", "CONFIG_KEY"}))
public class MerchantConfiguration extends SalesManagerEntity<Long, MerchantConfiguration>
    implements Serializable, Auditable {

  /**
   * 
   */
  private static final long serialVersionUID = 4246917986731953459L;

  @Id
  @Column(name = "MERCHANT_CONFIG_ID")
  @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
      valueColumnName = "SEQ_COUNT", pkColumnValue = "MERCH_CONF_SEQ_NEXT_VAL")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "MERCHANT_ID", nullable = true)
  private MerchantStore merchantStore;

  @Embedded
  private AuditSection auditSection = new AuditSection();

  @Column(name = "CONFIG_KEY")
  private String key;

  /**
   * activate and deactivate configuration
   */
  @Column(name = "ACTIVE", nullable = true)
  private Boolean active = new Boolean(false);


  @Column(name = "VALUE")
  @Type(type = "org.hibernate.type.TextType")
  private String value;

  @Column(name = "TYPE")
  @Enumerated(value = EnumType.STRING)
  private MerchantConfigurationType merchantConfigurationType =
      MerchantConfigurationType.INTEGRATION;

  public void setKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public AuditSection getAuditSection() {
    return auditSection;
  }

  public void setAuditSection(AuditSection auditSection) {
    this.auditSection = auditSection;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }



  public MerchantStore getMerchantStore() {
    return merchantStore;
  }

  public void setMerchantStore(MerchantStore merchantStore) {
    this.merchantStore = merchantStore;
  }

  public void setMerchantConfigurationType(MerchantConfigurationType merchantConfigurationType) {
    this.merchantConfigurationType = merchantConfigurationType;
  }

  public MerchantConfigurationType getMerchantConfigurationType() {
    return merchantConfigurationType;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }


}

--- sm-core/src/main/java/com/salesmanager/core/business/configuration/events/AsynchronousEventsConfiguration.java ---
package com.salesmanager.core.business.configuration.events;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * Events will be asynchronous (in a different thread)
 * @author carlsamson
 *
 */
@Configuration
public class AsynchronousEventsConfiguration {
	
	   @Bean(name = "applicationEventMulticaster")
	   public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
	       SimpleApplicationEventMulticaster eventMulticaster
	         = new SimpleApplicationEventMulticaster();
	        
	       eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
	       return eventMulticaster;
	   }

}

--- sm-core/src/main/java/com/salesmanager/core/business/services/system/MerchantConfigurationServiceImpl.java ---
package com.salesmanager.core.business.services.system;

import java.util.List;
import jakarta.inject.Inject; // Updated to use Jakarta inject
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.system.MerchantConfigurationRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.system.MerchantConfig;
import com.salesmanager.core.model.system.MerchantConfiguration;
import com.salesmanager.core.model.system.MerchantConfigurationType;

@Service("merchantConfigurationService")
public class MerchantConfigurationServiceImpl extends
		SalesManagerEntityServiceImpl<Long, MerchantConfiguration> implements
		MerchantConfigurationService {

	private MerchantConfigurationRepository merchantConfigurationRepository;
	
	@Inject
	public MerchantConfigurationServiceImpl(
			MerchantConfigurationRepository merchantConfigurationRepository) {
			super(merchantConfigurationRepository);
			this.merchantConfigurationRepository = merchantConfigurationRepository;
	}
	

	@Override
	public MerchantConfiguration getMerchantConfiguration(String key, MerchantStore store) throws ServiceException {
		return merchantConfigurationRepository.findByMerchantStoreAndKey(store.getId(), key);
	}
	
	@Override
	public List<MerchantConfiguration> listByStore(MerchantStore store) throws ServiceException {
		return merchantConfigurationRepository.findByMerchantStore(store.getId());
	}
	
	@Override
	public List<MerchantConfiguration> listByType(MerchantConfigurationType type, MerchantStore store) throws ServiceException {
		return merchantConfigurationRepository.findByMerchantStoreAndType(store.getId(), type);
	}
	
	@Override
	public void saveOrUpdate(MerchantConfiguration entity) throws ServiceException {
		

		
		if(entity.getId()!=null && entity.getId()>0) {
			super.update(entity);
		} else {
			super.create(entity);

		}
	}
	
	
	@Override
	public void delete(MerchantConfiguration merchantConfiguration) throws ServiceException {
		MerchantConfiguration config = merchantConfigurationRepository.getOne(merchantConfiguration.getId());
		if(config!=null) {
			super.delete(config);
		}
	}
	
	@Override
	public MerchantConfig getMerchantConfig(MerchantStore store) throws ServiceException {

		MerchantConfiguration configuration = merchantConfigurationRepository.findByMerchantStoreAndKey(store.getId(), MerchantConfigurationType.CONFIG.name());
		
		MerchantConfig config = null;
		if(configuration!=null) {
			String value = configuration.getValue();
			
			ObjectMapper mapper = new ObjectMapper();
			try {
				config = mapper.readValue(value, MerchantConfig.class);
			} catch(Exception e) {
				throw new ServiceException("Cannot parse json string " + value);
			}
		}
		return config;
		
	}
	
	@Override
	public void saveMerchantConfig(MerchantConfig config, MerchantStore store) throws ServiceException {
		
		MerchantConfiguration configuration = merchantConfigurationRepository.findByMerchantStoreAndKey(store.getId(), MerchantConfigurationType.CONFIG.name());

		if(configuration==null) {
			configuration = new MerchantConfiguration();
			configuration.setMerchantStore(store);
			configuration.setKey(MerchantConfigurationType.CONFIG.name());
			configuration.setMerchantConfigurationType(MerchantConfigurationType.CONFIG);
		}
		
		String value = config.toJSONString();
		configuration.setValue(value);
		if(configuration.getId()!=null && configuration.getId()>0) {
			super.update(configuration);
		} else {
			super.create(configuration);

		}
		
	}
	

}

--- sm-core/src/main/java/com/salesmanager/core/business/configuration/CoreApplicationConfiguration.java ---
package com.salesmanager.core.business.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan({"com.salesmanager.core.business"})
@EnableAutoConfiguration
@EnableConfigurationProperties(ApplicationSearchConfiguration.class)
@EnableJpaRepositories(basePackages = "com.salesmanager.core.business.repositories")
@EntityScan(basePackages = "com.salesmanager.core.model")
@EnableTransactionManagement
@ImportResource("classpath:/spring/shopizer-core-context.xml")
public class CoreApplicationConfiguration {



}

--- sm-core/src/main/java/com/salesmanager/core/business/services/search/SearchServiceImpl.java ---
package com.salesmanager.core.business.services.search;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct; // Updated to Jakarta annotation
import jakarta.inject.Inject; // Updated to Jakarta inject

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.configuration.ApplicationSearchConfiguration;
import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.inventory.ProductInventoryService;
import com.salesmanager.core.business.services.catalog.pricing.PricingService;
import com.salesmanager.core.business.utils.CoreConfiguration;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.category.CategoryDescription;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.attribute.ProductAttribute;
import com.salesmanager.core.model.catalog.product.attribute.ProductOptionDescription;
import com.salesmanager.core.model.catalog.product.attribute.ProductOptionValueDescription;
import com.salesmanager.core.model.catalog.product.description.ProductDescription;
import com.salesmanager.core.model.catalog.product.image.ProductImage;
import com.salesmanager.core.model.catalog.product.inventory.ProductInventory;
import com.salesmanager.core.model.catalog.product.manufacturer.Manufacturer;
import com.salesmanager.core.model.catalog.product.manufacturer.ManufacturerDescription;
import com.salesmanager.core.model.catalog.product.variant.ProductVariant;
import com.salesmanager.core.model.merchant.MerchantStore;

import modules.commons.search.SearchModule;
import modules.commons.search.configuration.SearchConfiguration;
import modules.commons.search.request.Document;
import modules.commons.search.request.IndexItem;
import modules.commons.search.request.RequestOptions;
import modules.commons.search.request.SearchRequest;
import modules.commons.search.request.SearchResponse;

@Service("productSearchService")
@EnableConfigurationProperties(value = ApplicationSearchConfiguration.class)
public class SearchServiceImpl implements com.salesmanager.core.business.services.search.SearchService {
	
	
    @Value("${search.noindex:false}")//skip indexing process
    private boolean noIndex;

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceImpl.class);

	private final static String INDEX_PRODUCTS = "INDEX_PRODUCTS";
	
	private final static String SETTINGS = "search/SETTINGS";
	
	private final static String PRODUCT_MAPPING_DEFAULT = "search/MAPPINGS.json";
	
	private final static String QTY = "QTY";
	private final static String PRICE = "PRICE";
	private final static String DISCOUNT_PRICE = "DISCOUNT";
	private final static String SKU = "SKU";
	private final static String VSKU = "VSKU";
	
	
	/**
	 * TODO properties file
	 */
	
	private final static String KEYWORDS_MAPPING_DEFAULT = "{\"properties\":"
			+ "      {\n\"id\": {\n"
			+ "        \"type\": \"long\"\n"
			+ "      }\n"
			+ "     }\n"
			+ "    }";	
	


	@Inject
	private CoreConfiguration configuration;

	@Autowired
	private ApplicationSearchConfiguration applicationSearchConfiguration;
	
	@Autowired
	private ProductInventoryService productInventoryService;

	@Autowired(required = false)
	private SearchModule searchModule;

	@PostConstruct
	public void init() throws Exception {

		/**
		 * Configure search module
		 */

		if (searchModule != null && !noIndex) {

			SearchConfiguration searchConfiguration = config();
			try {
				searchModule.configure(searchConfiguration);
			} catch (Exception e) {
				LOGGER.error("SearchModule cannot be configured [" + e.getMessage() + "]", e);
			}
		}
	}

	public void index(MerchantStore store, Product product) throws ServiceException {

		Validate.notNull(product.getId(), "Product.id cannot be null");

		if (configuration.getProperty(INDEX_PRODUCTS) == null
				|| configuration.getProperty(INDEX_PRODUCTS).equals(Constants.FALSE) || searchModule == null) {
			return;
		}

		List<String> languages = languages(product);

		// existing documents
		List<Document> documents;
		List<Map<String, String>> variants = null;
		try {
			documents = document(product.getId(), languages, RequestOptions.DO_NOT_FAIL_ON_NOT_FOUND);

				if (!CollectionUtils.isEmpty(product.getVariants())) {
					variants = new ArrayList<Map<String, String>>();
					variants = product.getVariants().stream().map(i -> variants(i)).collect(Collectors.toList());
				}
	
				if (!CollectionUtils.isEmpty(documents)) {
					if (documents.iterator().next() != null) {
						searchModule.delete(languages, product.getId());
					}
				}


		} catch (Exception e) {
			throw new ServiceException(e);
		}

		Set<ProductDescription> descriptions = product.getDescriptions();

		for (ProductDescription description : descriptions) {
			indexProduct(store, description, product, variants);
		}

	}

	private List<Document> document(Long id, List<String> languages, RequestOptions options) throws Exception {
		List<Optional<Document>> documents = null;
		try {
			documents = searchModule.getDocument(id, languages, options);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		for(Optional<Document> d : documents) {
			if(d == null) {//not allowed
				return Collections.emptyList();
			}
		}
		
		List<Document> filteredList = documents.stream().filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toList());

		return filteredList;

	}

	private void indexProduct(MerchantStore store, ProductDescription description, Product product,
			List<Map<String, String>> variants) throws ServiceException {

		try {
			ProductImage image = null;
			if (!CollectionUtils.isEmpty(product.getImages())) {
				image = product.getImages().stream().filter(i -> i.isDefaultImage()).findFirst().get();
			}
			
			/**
			 * Inventory
			 */
			
			/**
			 * SKU, QTY, PRICE, DISCOUNT
			 */
			
			List<Map<String, String>> itemInventory = new ArrayList<Map<String, String>>();
			
			itemInventory.add(inventory(product));
			
			if (!CollectionUtils.isEmpty(product.getVariants())) {
				for(ProductVariant variant : product.getVariants()) {
					itemInventory.add(inventory(variant));
				}
			}

			IndexItem item = new IndexItem();
			item.setId(product.getId());
			item.setStore(store.getCode().toLowerCase());
			item.setDescription(description.getDescription());
			item.setName(description.getName());
			item.setInventory(itemInventory);


			if (product.getManufacturer() != null) {
				item.setBrand(manufacturer(product.getManufacturer(), description.getLanguage().getCode()));
			}

			if (!CollectionUtils.isEmpty(product.getCategories())) {
				item.setCategory(
						category(product.getCategories().iterator().next(), description.getLanguage().getCode()));
			}

			if (!CollectionUtils.isEmpty(product.getAttributes())) {
				Map<String, String> attributes = attributes(product, description.getLanguage().getCode());
				item.setAttributes(attributes);
			}

			if (image != null) {
				item.setImage(image.getProductImage());
			}

			if (product.getProductReviewAvg() != null) {
				item.setReviews(product.getProductReviewAvg().toString());
			}

			if (!CollectionUtils.isEmpty(variants)) {
				item.setVariants(variants);
			}

			item.setLanguage(description.getLanguage().getCode());
			item.setLink(description.getSeUrl());

			searchModule.index(item);
		} catch (Exception e) {
			throw new ServiceException(e);
		}

	}

	private SearchConfiguration config() throws Exception {

		SearchConfiguration config = new SearchConfiguration();
		config.setClusterName(applicationSearchConfiguration.getClusterName());
		config.setHosts(applicationSearchConfiguration.getHost());
		config.setCredentials(applicationSearchConfiguration.getCredentials());

		config.setLanguages(applicationSearchConfiguration.getSearchLanguages());
		
		config.getLanguages().stream().forEach(l -> {
			try {
				this.mappings(config,l);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});
		config.getLanguages().stream().forEach(l -> {
			try {
				this.settings(config,l);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});
		


		/**
		 * The mapping
		 */
		/*
		 * config.getProductMappings().put("variants", "nested");
		 * config.getProductMappings().put("attributes", "nested");
		 * config.getProductMappings().put("brand", "keyword");
		 * config.getProductMappings().put("store", "keyword");
		 * config.getProductMappings().put("reviews", "keyword");
		 * config.getProductMappings().put("image", "keyword");
		 * config.getProductMappings().put("category", "text");
		 * config.getProductMappings().put("name", "text");
		 * config.getProductMappings().put("description", "text");
		 * config.getProductMappings().put("price", "float");
		 * config.getProductMappings().put("id", "long");
		 
		config.getKeywordsMappings().put("store", "keyword");
		*/

		return config;

	}
	
	private Map<String, String> inventory(Product product) throws Exception {
		
		
		/**
		 * Default inventory
		 */
		
		ProductInventory inventory = productInventoryService.inventory(product);
		
		Map<String, String> inventoryMap = new HashMap<String, String>();
		inven

--- sm-shop/src/main/java/com/salesmanager/shop/admin/security/SecurityDataAccessException.java ---
package com.salesmanager.shop.admin.security;

import org.springframework.dao.DataAccessException;

public class SecurityDataAccessException extends DataAccessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SecurityDataAccessException(String msg) {
		super(msg);
	}
	
	public SecurityDataAccessException(String msg, Exception e) {
		super(msg,e);
	}

}

--- sm-core/pom.xml ---
<?xml version="1.0"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://www.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<modelVersion>4.0.0</modelVersion>

	<parent>
		<groupId>com.shopizer</groupId>
		<artifactId>shopizer</artifactId>
		<version>3.2.3</version>
	</parent>

	<artifactId>sm-core</artifactId>
	<packaging>jar</packaging>

	<name>sm-core</name>
	<url>http://www.shopizer.com</url>

	<properties>
		<coverage.lines>.00</coverage.lines>
		<coverage.branches>.00</coverage.branches>
		<google-client-maps-services-version>0.1.6</google-client-maps-services-version>
	</properties>

	<dependencies>

		<!-- jpa, crud repository -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>

		<!-- https://mvnrepository.com/artifact/org.hibernate/hibernate-ehcache -->
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-ehcache</artifactId>
		</dependency>

		<!-- sm-core-model -->
		<dependency>
			<groupId>com.shopizer</groupId>
			<artifactId>sm-core-model</artifactId>
		</dependency>

		<!-- sm-core-modules -->
		<dependency>
			<groupId>com.shopizer</groupId>
			<artifactId>sm-core-modules</artifactId>
		</dependency>

		<!-- canadapost -->
		<dependency>
			<groupId>com.shopizer</groupId>
			<artifactId>shipping-canadapost-spring-boot-starter</artifactId>
			<version>2.17.0</version>
		</dependency>

		<dependency>
			<groupId>com.shopizer</groupId>
			<artifactId>shopizer-commons</artifactId>
			<version>1.0.6</version>
		</dependency>

		<!-- open search client -->
		
		<dependency>
			<groupId>com.shopizer</groupId>
			<artifactId>shopizer-search-opensearch-spring-boot-starter</artifactId>
			<version>1.0.3</version>
		</dependency>
		

		<!--
		Want to use Square Payment Module ?
        <dependency>
            <groupId>com.shopizer</groupId>
            <artifactId>shopizer-payment-square-spring-boot-starter</artifactId>
            <version>1.0.0</version>
        </dependency>
        -->


		<!-- Google Map API -->
		<dependency>
			<groupId>com.google.maps</groupId>
			<artifactId>google-maps-services</artifactId>
		</dependency>

		<dependency>
			<groupId>org.kie</groupId>
			<artifactId>kie-ci</artifactId>
			<exclusions>
				<exclusion>
					<groupId>com.google.guava</groupId>
					<artifactId>guava</artifactId>
				</exclusion>
				<exclusion>
					<groupId>jakarta.annotation</groupId> <!-- Incremental configuration to Jakarta -->
					<artifactId>jakarta.annotation-api</artifactId>
				</exclusion>
			</exclusions>
		</dependency>

		<dependency>
			<groupId>org.drools</groupId>
			<artifactId>drools-decisiontables</artifactId>
		</dependency>

		<dependency>
			<groupId>org.drools</groupId>
			<artifactId>drools-core</artifactId>
		</dependency>

		<dependency>
			<groupId>org.drools</groupId>
			<artifactId>drools-compiler</artifactId>
		</dependency>

		<!--spring integration -->
		<dependency>
			<groupId>org.kie</groupId>
			<artifactId>kie-spring</artifactId>
		</dependency>
		<!-- end rules engine -->

		<!-- Infinispan -->
		<dependency>
			<groupId>org.in