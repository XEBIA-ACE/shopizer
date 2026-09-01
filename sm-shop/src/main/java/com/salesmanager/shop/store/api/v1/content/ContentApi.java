package com.salesmanager.shop.store.api.v1.content;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.salesmanager.core.model.content.ContentType;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.content.ContentFile;
import com.salesmanager.shop.model.content.ContentFolder;
import com.salesmanager.shop.model.content.ContentName;
import com.salesmanager.shop.model.content.PersistableContentEntity;
import com.salesmanager.shop.model.content.ReadableContentEntity;
import com.salesmanager.shop.model.content.ReadableContentFull;
import com.salesmanager.shop.model.content.box.PersistableContentBox;
import com.salesmanager.shop.model.content.box.ReadableContentBox;
import com.salesmanager.shop.model.content.page.PersistableContentPage;
import com.salesmanager.shop.model.content.page.ReadableContentPage;
import com.salesmanager.shop.model.entity.Entity;
import com.salesmanager.shop.model.entity.EntityExists;
import com.salesmanager.shop.model.entity.ReadableEntityList;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.controller.content.facade.ContentFacade;
import com.salesmanager.shop.utils.ImageFilePath;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/v1")
@Api(tags = { "Content management resource (Content Management Api)" })
@SwaggerDefinition(tags = {
		@Tag(name = "Content management resource", description = "Add pages, content boxes, manage images and files") })
public class ContentApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentApi.class);

	private static final String DEFAULT_PATH = "/";
	
	private final static String BOX = "BOX";
	private final static String PAGE = "PAGE";

	@Inject
	private ContentFacade contentFacade;

	@Inject
	@Qualifier("img")
	private ImageFilePath imageUtils;

	/**
	 * List content pages
	 * @param merchantStore
	 * @param language
	 * @param page
	 * @param count
	 * @return
	 */
	@GetMapping(value = {"/private/content/pages", "/content/pages"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(httpMethod = "GET", value = "Get page names created for a given MerchantStore", notes = "", produces = "application/json", response = List.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public ReadableEntityList<ReadableContentPage> pages(
			@ApiIgnore MerchantStore merchantStore,
			@ApiIgnore Language language,
			int page,
			int count) {
		return contentFacade
				.getContentPages(merchantStore, language, page, count);
	}

	@Deprecated
	@GetMapping(value = "/content/summary", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(httpMethod = "GET", value = "Get pages summary created for a given MerchantStore. Content summary is a content bux having code summary.", notes = "", produces = "application/json", response = List.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public List<ReadableContentBox> pagesSummary(
			@ApiIgnore MerchantStore merchantStore, 
			@ApiIgnore Language language) {
		//return contentFacade.getContentBoxes(ContentType.BOX, "summary_", merchantStore, language);
		return null;
	}

	/**
	 * List all boxes
	 * 
	 * @param merchantStore
	 * @param language
	 * @return
	 */
	@GetMapping(value = {"/content/boxes","/private/content/boxes"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(httpMethod = "GET", value = "Get boxes for a given MerchantStore", notes = "", produces = "application/json", response = List.class)
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
		@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public ReadableEntityList<ReadableContentBox> boxes(
			@ApiIgnore MerchantStore merchantStore,
			@ApiIgnore Language language,
			int page,
			int count
			) {
		return contentFacade.getContentBoxes(ContentType.BOX, merchantStore, language, page, count);
	}

	/**
	 * List specific content box
	 * @param code
	 * @param merchantStore
	 * @param language
	 * @return
	 */
	@GetMapping(value = "/content/pages/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(httpMethod = "GET", value = "Get page content by code for a given MerchantStore", notes = "", produces = "application/json", response = ReadableContentPage.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public ReadableContentPage page(@PathVariable("code") String code, @ApiIgnore MerchantStore merchantStore,
			@ApiIgnore Language language) {

		return contentFacade.getContentPage(code, merchantStore, language);

	}

	/**
	 * Get content page by name
	 * @param name
	 * @param merchantStore
	 * @param language
	 * @return
	 */
	@GetMapping(value = "/content/pages/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(httpMethod = "GET", value = "Get page content by code for a given MerchantStore", notes = "", produces = "application/json", response = ReadableContentPage.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
			@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public ReadableContentPage pageByName(@PathVariable("name") String name, @ApiIgnore MerchantStore merchantStore,
			@ApiIgnore Language language) {

		return contentFacade.getContentPageByName(name, merchantStore, language);

	}
	
	/**
	 * Create content box
	 * 
	 * @param page
	 * @param merchantStore
	 * @param language
	 * @param pageCode
	 */
	@PostMapping(value = "/private/content/box")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(httpMethod = "POST", value = "Create content box", notes = "", response = Entity.class)
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
		@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public Entity createBox(
			@RequestBody @Valid PersistableContentBox box, 
			@ApiIgnore MerchantStore merchantStore,
			@ApiIgnore Language language) {

		Long id = contentFacade.saveContentBox(box, merchantStore, language);
		Entity entity = new Entity();
		entity.setId(id);
		return entity;
	}
	
	@GetMapping(value = "/private/content/box/{code}/exists")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(httpMethod = "GET", value = "Check unique content box", notes = "", response = EntityExists.class)
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
		@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public EntityExists boxExists(
			@PathVariable String code, 
			@ApiIgnore MerchantStore merchantStore,
			@ApiIgnore Language language) {

		boolean exists = contentFacade.codeExist(code, BOX, merchantStore);
		EntityExists entity = new EntityExists(exists);
		return entity;
	}
	
	@GetMapping(value = "/private/content/page/{code}/exists")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation(httpMethod = "GET", value = "Check unique content page", notes = "", response = EntityExists.class)
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
		@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public EntityExists pageExists(
			@PathVariable String code, 
			@ApiIgnore MerchantStore merchantStore,
			@ApiIgnore Language language) {

		boolean exists = contentFacade.codeExist(code, PAGE, merchantStore);
		EntityExists entity = new EntityExists(exists);
		return entity;
	}
	
	/**
	 * Create content page
	 * @param page
	 * @param merchantStore
	 * @param language
	 */
	@PostMapping(value = "/private/content/page")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation(httpMethod = "POST", value = "Create content page", notes = "", response = Entity.class)
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "store", dataType = "String", defaultValue = "DEFAULT"),
		@ApiImplicitParam(name = "lang", dataType = "String", defaultValue = "en") })
	public Entity createPage(
			@RequestBody @Valid PersistableContentPage page, 
			@ApiIgnore MerchantStore merchantStore,
		