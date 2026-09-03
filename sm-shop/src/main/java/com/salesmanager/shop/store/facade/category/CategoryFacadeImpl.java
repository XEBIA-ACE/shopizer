package com.salesmanager.shop.store.facade.category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.category.CategoryService;
import com.salesmanager.core.business.services.catalog.product.attribute.ProductAttributeService;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.model.catalog.category.Category;
import com.salesmanager.core.model.catalog.product.attribute.ProductAttribute;
import com.salesmanager.core.model.catalog.product.attribute.ProductOption;
import com.salesmanager.core.model.catalog.product.attribute.ProductOptionDescription;
import com.salesmanager.core.model.catalog.product.attribute.ProductOptionValueDescription;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.mapper.Mapper;
import com.salesmanager.shop.model.catalog.category.PersistableCategory;
import com.salesmanager.shop.model.catalog.category.ReadableCategory;
import com.salesmanager.shop.model.catalog.category.ReadableCategoryList;
import com.salesmanager.shop.model.catalog.product.attribute.ReadableProductVariant;
import com.salesmanager.shop.model.catalog.product.attribute.ReadableProductVariantValue;
import com.salesmanager.shop.model.entity.ListCriteria;
import com.salesmanager.shop.populator.catalog.PersistableCategoryPopulator;
import com.salesmanager.shop.populator.catalog.ReadableCategoryPopulator;
import com.salesmanager.shop.store.api.exception.OperationNotAllowedException;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.api.exception.UnauthorizedException;
import com.salesmanager.shop.store.controller.category.facade.CategoryFacade;

@Service(value = "categoryFacade")
public class CategoryFacadeImpl implements CategoryFacade {

	@Inject
	private CategoryService categoryService;

	@Inject
	private MerchantStoreService merchantStoreService;

	@Inject
	private PersistableCategoryPopulator persistableCatagoryPopulator;

	@Inject
	private Mapper<Category, ReadableCategory> categoryReadableCategoryConverter;

	@Inject
	private ProductAttributeService productAttributeService;

	private static final String FEATURED_CATEGORY = "featured";
	private static final String VISIBLE_CATEGORY = "visible";
	private static final String ADMIN_CATEGORY = "admin";

	@Override
	public ReadableCategoryList getCategoryHierarchy(MerchantStore store, ListCriteria criteria, int depth,
			Language language, List<String> filter, int page, int count) {

		Validate.notNull(store,"MerchantStore can not be null");


		//get parent store
		try {

			MerchantStore parent = merchantStoreService.getParent(store.getCode());


			List<Category> categories = null;
			ReadableCategoryList returnList = new ReadableCategoryList();
			if (!CollectionUtils.isEmpty(filter) && filter.contains(FEATURED_CATEGORY)) {
				categories = categoryService.getListByDepthFilterByFeatured(parent, depth, language);
				returnList.setRecordsTotal(categories.size());
				returnList.setNumber(categories.size());
				returnList.setTotalPages(1);
			} else {
				org.springframework.data.domain.Page<Category> pageable = categoryService.getListByDepth(parent, language,
						criteria != null ? criteria.getName() : null, depth, page, count);
				categories = pageable.getContent();
				returnList.setRecordsTotal(pageable.getTotalElements());
				returnList.setTotalPages(pageable.getTotalPages());
				returnList.setNumber(categories.size());
			}



			List<ReadableCategory> readableCategories = null;
			if (filter != null && filter.contains(VISIBLE_CATEGORY)) {
				readableCategories = categories.stream().filter(Category::isVisible)
						.map(cat -> categoryReadableCategoryConverter.convert(cat, store, language))
						.collect(Collectors.toList());
			} else {
				readableCategories = categories.stream()
						.map(cat -> categoryReadableCategoryConverter.convert(cat, store, language))
						.collect(Collectors.toList());
			}

			Map<Long, ReadableCategory> readableCategoryMap = readableCategories.stream()
					.collect(Collectors.toMap(ReadableCategory::getId, Function.identity()));

			readableCategories.stream()
					// .filter(ReadableCategory::isVisible)
					.filter(cat -> Objects.nonNull(cat.getParent()))
					.filter(cat -> readableCategoryMap.containsKey(cat.getParent().getId())).forEach(readableCategory -> {
						ReadableCategory parentCategory = readableCategoryMap.get(readableCategory.getParent().getId());
						if (parentCategory != null) {
							parentCategory.getChildren().add(readableCategory);
						}
					});
			
			List<ReadableCategory> filteredList = readableCategoryMap.values().stream().collect(Collectors.toList());

			//execute only if not admin filtered
			if(filter == null || (filter!=null && !filter.contains(ADMIN_CATEGORY))) {
					filteredList = readableCategoryMap.values().stream().filter(cat -> cat.getDepth() == 0)
						.sorted(Comparator.comparing(ReadableCategory::getSortOrder)).collect(Collectors.toList());
				
					returnList.setNumber(filteredList.size());

			}
			
			returnList.setCategories(filteredList);

			
			
			return returnList;

		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}

	}

	@Override
	public boolean existByCode(MerchantStore store, String code) {
		try {
			Category c = categoryService.getByCode(store, code);
			return c != null ? true : false;
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}

	@Override
	public PersistableCategory saveCategory(MerchantStore store, PersistableCategory category) {
		try {

			Long categoryId = category.getId();
			Category target = Optional.ofNullable(categoryId)
					.filter(merchant -> store !=null)
					.filter(id -> id > 0)
					.map(categoryService::getById)
					.orElse(new Category());

			Category dbCategory = populateCategory(store, category, target);
			saveCategory(store, dbCategory, null);

			// set category id
			category.setId(dbCategory.getId());
			return category;
		} catch (ServiceException e) {
			throw new ServiceRuntimeException("Error while updating category", e);
		}
	}

	private Category populateCategory(MerchantStore store, PersistableCategory category, Category target) {
		try {
			return persistableCatagoryPopulator.populate(category, target, store, store.getDefaultLanguage());
		} catch (ConversionException e) {
			throw new ServiceRuntimeException(e);
		}
	}

	private void saveCategory(MerchantStore store, Category category, Category parent) throws ServiceException {

		/**
		 * c.children1
		 *
		 * <p>
		 * children1.children1 children1.children2
		 *
		 * <p>
		 * children1.children2.children1
		 */

		/** set lineage * */
		if (parent != null) {
			category.setParent(category);

			String lineage = parent.getLineage();
			int depth = parent.getDepth();

			category.setDepth(depth + 1);
			category.setLineage(new StringBuilder().append(lineage).toString());// service
																										// will
																										// adjust
																										// lineage
		}

		category.setMerchantStore(store);

		// remove children
		List<Category> children = category.getCategories();
		List<Category> saveAfter = children.stream().filter(c -> c.getId() == null || c.getId().longValue()==0).collect(Collectors.toList());
		List<Category> saveNow = children.stream().filter(c -> c.getId() != null && c.getId().longValue()>0).collect(Collectors.toList());
		category.setCategories(saveNow);

		/** set parent * */
		if (parent != null) {
			category.setParent(parent);
		}

		categoryService.saveOrUpdate(category);

		if (!CollectionUtils.isEmpty(saveAfter)) {
			parent = category;
			for(Category c: saveAfter) {
				if(c.getId() == null || c.getId().longValue()==0) {
					for (Category sub : children) {
						saveCategory(store, sub, parent);
					}
				}
			}
		}

	}

	@Override
	public ReadableCategory getById(MerchantStore store, Long id, Language language) {

			Category categoryModel = null;
			if (language != null) {
				categoryModel = getCategoryById(id, language);
			} else {// all langs
				categoryModel = getById(store, id);
			}

			if (categoryModel == null)
				throw new ResourceNotFoundException("Categori id [" + id + "] not found");

			StringBuilder lineage = new StringBuilder().append(categoryModel.getLineage());

			ReadableCategory readableCategory = categoryReadableCategoryConverter.convert(categoryModel, store,
					language);

			// get children
			List<Category> children = getListByLineage(store, lineage.toString());

			List<ReadableCategory> childrenCats = children.stream()
					.map(cat -> categoryReadableCategoryConverter.convert(cat, store, language))
					.collect(Collectors.toList());

			addChildToParent(readableCategory, childrenCats);
			return readableCategory;

	}

	private void addChildToParent(ReadableCategory readableCategory, List<ReadableCategory> childrenCats) {
		Map<Long, ReadableCategory> categoryMap = childrenCats.stream()
				.collect(Collectors.toMap(ReadableCategory::getId, Function.identity()));
		categoryMap.put(readableCategory.getId(), readableCategory);

		// traverse map and add child to parent
		for (ReadableCategory readable : childrenCats) {

			if (reada