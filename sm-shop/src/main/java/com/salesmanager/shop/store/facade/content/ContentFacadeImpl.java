package com.salesmanager.shop.store.facade.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.content.ContentService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.model.content.Content;
import com.salesmanager.core.model.content.ContentDescription;
import com.salesmanager.core.model.content.ContentType;
import com.salesmanager.core.model.content.FileContentType;
import com.salesmanager.core.model.content.InputContentFile;
import com.salesmanager.core.model.content.OutputContentFile;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.content.ContentDescriptionEntity;
import com.salesmanager.shop.model.content.ContentFile;
import com.salesmanager.shop.model.content.ContentFolder;
import com.salesmanager.shop.model.content.ContentImage;
import com.salesmanager.shop.model.content.ReadableContentEntity;
import com.salesmanager.shop.model.content.ReadableContentFull;
import com.salesmanager.shop.model.content.box.PersistableContentBox;
import com.salesmanager.shop.model.content.box.ReadableContentBox;
import com.salesmanager.shop.model.content.box.ReadableContentBoxFull;
import com.salesmanager.shop.model.content.page.PersistableContentPage;
import com.salesmanager.shop.model.content.page.ReadableContentPage;
import com.salesmanager.shop.model.content.page.ReadableContentPageFull;
import com.salesmanager.shop.model.entity.ReadableEntityList;
import com.salesmanager.shop.store.api.exception.ConstraintException;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.controller.content.facade.ContentFacade;
import com.salesmanager.shop.utils.FilePathUtils;
import com.salesmanager.shop.utils.ImageFilePath;

@Component("contentFacade")
public class ContentFacadeImpl implements ContentFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentFacade.class);

	public static final String FILE_CONTENT_DELIMETER = "/";

	@Inject
	private ContentService contentService;

	@Inject
	private LanguageService languageService;

	@Inject
	@Qualifier("img")
	private ImageFilePath imageUtils;

	@Inject
	private FilePathUtils fileUtils;

	@Override
	public ContentFolder getContentFolder(String folder, MerchantStore store) throws Exception {
		try {
			List<String> imageNames = Optional
					.ofNullable(contentService.getContentFilesNames(store.getCode(), FileContentType.IMAGE))
					.orElseThrow(() -> new ResourceNotFoundException("No Folder found for path : " + folder));

			// images from CMS
			List<ContentImage> contentImages = imageNames.stream().map(name -> convertToContentImage(name, store))
					.collect(Collectors.toList());

			ContentFolder contentFolder = new ContentFolder();
			if (!StringUtils.isBlank(folder)) {
				contentFolder.setPath(URLEncoder.encode(folder, "UTF-8"));
			}
			contentFolder.getContent().addAll(contentImages);
			return contentFolder;

		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Error while getting folder " + e.getMessage(), e);
		}
	}

	private ContentImage convertToContentImage(String name, MerchantStore store) {
		String path = absolutePath(store, null);
		ContentImage contentImage = new ContentImage();
		contentImage.setName(name);
		contentImage.setPath(path);
		return contentImage;
	}

	@Override
	public String absolutePath(MerchantStore store, String file) {
		return new StringBuilder().append(imageUtils.getContextPath())
				.append(imageUtils.buildStaticImageUtils(store, file)).toString();
	}

	@Override
	public void delete(MerchantStore store, String fileName, String fileType) {
		Validate.notNull(store, "MerchantStore cannot be null");
		Validate.notNull(fileName, "File name cannot be null");
		try {
			FileContentType t = FileContentType.valueOf(fileType);
			contentService.removeFile(store.getCode(), t, fileName);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ReadableEntityList<ReadableContentPage> getContentPages(MerchantStore store, Language language, int page,
			int count) {
		Validate.notNull(store, "MerchantStore cannot be null");

		@SuppressWarnings("rawtypes")
		ReadableEntityList items = new ReadableEntityList();
		Page<Content> contentPages;
		try {
			contentPages = contentService.listByType(ContentType.PAGE, store, page, count);

			items.setTotalPages(contentPages.getTotalPages());
			items.setNumber(contentPages.getContent().size());
			items.setRecordsTotal(contentPages.getNumberOfElements());

			List<ReadableContentBox> boxes = contentPages.getContent().stream()
					.map(content -> convertContentToReadableContentBox(store, language, content))
					.collect(Collectors.toList());

			items.setItems(boxes);
			return items;

		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Exception while getting content ", e);
		}

	}

	private ReadableContentPage contentDescriptionToReadableContent(MerchantStore store, Content content,
			ContentDescription contentDescription) {

		ReadableContentPage page = new ReadableContentPage();

		ContentDescription desc = new ContentDescription();

		desc.setName(contentDescription.getName());
		desc.setDescription(contentDescription.getDescription());

		page.setId(content.getId());
		desc.setSeUrl(contentDescription.getSeUrl());
		page.setLinkToMenu(content.isLinkToMenu());
		desc.setTitle(contentDescription.getTitle());
		desc.setMetatagDescription(contentDescription.getMetatagDescription());
		page.setContentType(ContentType.PAGE.name());
		page.setCode(content.getCode());
		page.setPath(fileUtils.buildStaticFilePath(store.getCode(), contentDescription.getSeUrl()));
		return page;

	}

	@Deprecated
	private ReadableContentFull convertContentToReadableContentFull(MerchantStore store, Language language,
			Content content) {
		ReadableContentFull contentFull = new ReadableContentFull();

		try {
			List<ContentDescriptionEntity> descriptions = this.createContentDescriptionEntitys(store, content,
					language);

			contentFull.setDescriptions(descriptions);
			contentFull.setId(content.getId());
			contentFull.setDisplayedInMenu(content.isLinkToMenu());
			contentFull.setContentType(content.getContentType().name());
			contentFull.setCode(content.getCode());
			contentFull.setId(content.getId());
			contentFull.setVisible(content.isVisible());

			return contentFull;

		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Error while creating ReadableContentFull", e);
		}
	}

	@Deprecated
	private ReadableContentEntity convertContentToReadableContentEntity(MerchantStore store, Language language,
			Content content) {

		ReadableContentEntity contentEntity = new ReadableContentEntity();

		ContentDescriptionEntity description = this.create(content.getDescription());

		contentEntity.setDescription(description);
		contentEntity.setId(content.getId());
		contentEntity.setDisplayedInMenu(content.isLinkToMenu());
		contentEntity.setContentType(content.getContentType().name());
		contentEntity.setCode(content.getCode());
		contentEntity.setId(content.getId());
		contentEntity.setVisible(content.isVisible());

		return contentEntity;

	}

	private Content convertContentPageToContent(MerchantStore store, Content model, PersistableContentPage content) throws Exception {
		
		
		
		Content contentModel = new Content();
		if(model != null) {
			contentModel = model;
		}

		List<ContentDescription> descriptions = buildDescriptions(contentModel, content.getDescriptions());
		contentModel.setCode(content.getCode());
		contentModel.setContentType(ContentType.PAGE);
		contentModel.setMerchantStore(store);
		contentModel.setLinkToMenu(content.isLinkToMenu());
		contentModel.setVisible(content.isVisible());
		contentModel.setDescriptions(descriptions);
		contentModel.setId(content.getId());
		return contentModel;
	}

	private Content convertContentBoxToContent(MerchantStore store, Content model, PersistableContentBox content) throws Exception {
		Content contentModel = new Content();
		if(model != null) {
			contentModel = model;
		}

		List<ContentDescription> descriptions = buildDescriptions(contentModel, content.getDescriptions());
		for(ContentDescription cd : descriptions) {
			cd.setContent(contentModel);
		}

		contentModel.setCode(content.getCode());
		contentModel.setContentType(ContentType.BOX);
		contentModel.setMerchantStore(store);
		contentModel.setVisible(content.isVisible());
		contentModel.setDescriptions(descriptions);
		contentModel.setId(content.getId());
		return contentModel;
	}

	/*
	 * private Content convertContentPageToContent(MerchantStore store, Language
	 * language, Content content, PersistableContentEntity contentPage) throws
	 * ServiceException {
	 * 
	 * ContentType contentType =
	 * ContentType.valueOf(contentPage.getContentType()); if (contentType ==
	 * null) { throw new
	 * ServiceRuntimeException("Invalid specified contentType [" +
	 * contentPage.getContentType() + "]"); }
	 * 
	 * List<ContentDescription> descriptions = createContentDescription(store,
	 * content, contentPage); description