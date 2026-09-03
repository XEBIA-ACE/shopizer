package com.salesmanager.shop.store.facade.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.modules.email.Email;
import com.salesmanager.core.business.services.merchant.MerchantStoreService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.services.system.EmailService;
import com.salesmanager.core.business.services.user.PermissionService;
import com.salesmanager.core.business.services.user.UserService;
import com.salesmanager.core.model.common.CredentialsReset;
import com.salesmanager.core.model.common.Criteria;
import com.salesmanager.core.model.common.GenericEntityList;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.Permission;
import com.salesmanager.core.model.user.User;
import com.salesmanager.core.model.user.UserCriteria;
import com.salesmanager.shop.constants.Constants;
import com.salesmanager.shop.constants.EmailConstants;
import com.salesmanager.shop.model.security.PersistableGroup;
import com.salesmanager.shop.model.security.ReadableGroup;
import com.salesmanager.shop.model.security.ReadablePermission;
import com.salesmanager.shop.model.user.PersistableUser;
import com.salesmanager.shop.model.user.ReadableUser;
import com.salesmanager.shop.model.user.ReadableUserList;
import com.salesmanager.shop.model.user.UserPassword;
import com.salesmanager.shop.populator.user.PersistableUserPopulator;
import com.salesmanager.shop.populator.user.ReadableUserPopulator;
import com.salesmanager.shop.store.api.exception.ConversionRuntimeException;
import com.salesmanager.shop.store.api.exception.GenericRuntimeException;
import com.salesmanager.shop.store.api.exception.OperationNotAllowedException;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.store.api.exception.UnauthorizedException;
import com.salesmanager.shop.store.controller.security.facade.SecurityFacade;
import com.salesmanager.shop.store.controller.user.facade.UserFacade;
import com.salesmanager.shop.utils.DateUtil;
import com.salesmanager.shop.utils.EmailUtils;
import com.salesmanager.shop.utils.FilePathUtils;
import com.salesmanager.shop.utils.ImageFilePath;
import com.salesmanager.shop.utils.LabelUtils;

@Service("userFacade")
public class UserFacadeImpl implements UserFacade {

	private static final String PRIVATE_PATH = "/private/";

	private static final String resetUserLink = "user/%s/reset/%s"; // front

	private static final String ACCOUNT_PASSWORD_RESET_TPL = "email_template_password_reset_request_user.ftl";

	private static final String RESET_PASSWORD_LINK = "RESET_PASSWORD_LINK";

	private static final String RESET_PASSWORD_TEXT = "RESET_PASSWORD_TEXT";

	@Inject
	private MerchantStoreService merchantStoreService;

	@Inject
	private UserService userService;

	@Inject
	private PermissionService permissionService;

	@Inject
	private LanguageService languageService;

	@Inject
	private PersistableUserPopulator persistableUserPopulator;

	@Inject
	private SecurityFacade securityFacade;

	@Autowired
	private FilePathUtils filePathUtils;

	@Autowired
	private LanguageService lamguageService;

	@Autowired
	private EmailUtils emailUtils;

	@Autowired
	private EmailService emailService;

	@Autowired
	@Qualifier("img")
	private ImageFilePath imageUtils;

	@Inject
	private LabelUtils messages;

	@Inject
	private PasswordEncoder passwordEncoder;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserFacadeImpl.class);

	@Override
	public ReadableUser findByUserName(String userName, String storeCode, Language lang) {
		ReadableUser user = findByUserName(userName, lang);
		if (user == null) {
			throw new ResourceNotFoundException("User [" + userName + "] not found");
		}

		return user;

	}

	private ReadableUser findByUserName(String userName, Language lang) {
		User user = getByUserName(userName);
		if (user == null) {
			throw new ResourceNotFoundException("User [" + userName + "] not found");
		}
		return convertUserToReadableUser(lang, user);
	}

	private ReadableUser convertUserToReadableUser(Language lang, User user) {
		ReadableUserPopulator populator = new ReadableUserPopulator();
		try {
			ReadableUser readableUser = new ReadableUser();
			readableUser = populator.populate(user, readableUser, user.getMerchantStore(), lang);

			List<Integer> groupIds = readableUser.getGroups().stream().map(ReadableGroup::getId).map(Long::intValue)
					.collect(Collectors.toList());
			List<ReadablePermission> permissions = findPermissionsByGroups(groupIds);
			readableUser.setPermissions(permissions);

			return readableUser;
		} catch (ConversionException e) {
			throw new ConversionRuntimeException(e);
		}
	}

	private User converPersistabletUserToUser(MerchantStore store, Language lang, User userModel,
			PersistableUser user) {
		try {
			return persistableUserPopulator.populate(user, userModel, store, lang);
		} catch (ConversionException e) {
			throw new ConversionRuntimeException(e);
		}
	}

	private User getByUserName(String userName) {
		try {
			return userService.getByUserName(userName);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}

	private User getByUserName(String userName, String storeCode) {
		try {
			return userService.getByUserName(userName, storeCode);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}

	private User getByUserId(Long id, String storeCode) {
		try {
			return userService.findByStore(id, storeCode);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}

	private User getByUserId(Long id) {
		try {
			return userService.getById(id);
		} catch (Exception e) {
			throw new ServiceRuntimeException(e);
		}
	}

	@Override
	public List<ReadablePermission> findPermissionsByGroups(List<Integer> ids) {
		return getPermissionsByIds(ids).stream().map(permission -> convertPermissionToReadablePermission(permission))
				.collect(Collectors.toList());
	}

	private ReadablePermission convertPermissionToReadablePermission(Permission permission) {
		ReadablePermission readablePermission = new ReadablePermission();
		readablePermission.setId(permission.getId());
		readablePermission.setName(permission.getPermissionName());
		return readablePermission;
	}

	private List<Permission> getPermissionsByIds(List<Integer> ids) {
		try {
			return permissionService.getPermissions(ids);
		} catch (ServiceException e) {
			throw new ServiceRuntimeException(e);
		}
	}

	@Override
	public boolean authorizedStore(String userName, String merchantStoreCode) {

		try {

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			Set<String> roles = authentication.getAuthorities().stream().map(r -> r.getAuthority())
					.collect(Collectors.toSet());

			ReadableUser readableUser = findByUserName(userName, languageService.defaultLanguage());

			// unless superadmin
			for (ReadableGroup group : readableUser.getGroups()) {
				if (Constants.GROUP_SUPERADMIN.equals(group.getName())) {
					return true;
				}
			}

			boolean authorized = false;
			User user = userService.findByStore(readableUser.getId(), merchantStoreCode);
			if (user != null) {
				authorized = true;
			} else {
				user = userService.getByUserName(userName);
			}

			if (user != null && !authorized) {

				// get parent
				MerchantStore store = merchantStoreService.getParent(merchantStoreCode);

				// user can be in parent
				MerchantStore st = user.getMerchantStore();
				if (store != null && st.getCode().equals(store.getCode())) {
					authorized = true;
				}
			}

			return authorized;
		} catch (Exception e) {
			throw new ServiceRuntimeException("Cannot authorize user " + userName + " for store " + merchantStoreCode,
					e.getMessage());
		}
	}

	@Override
	public void authorizedGroup(String userName, List<String> groupName) {

		ReadableUser readableUser = findByUserName(userName, languageService.defaultLanguage());

		// unless superadmin
		for (ReadableGroup group : readableUser.getGroups()) {
			if (groupName.contains(group.getName())) {
				return;
			}
		}

		throw new UnauthorizedException("User " + userName + " not authorized");

	}

	@Override
	public String authenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null) {
			throw new UnauthorizedException("User Not authorized");
		}

		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			String currentUserName = authentication.getName();
			return currentUserName;
		}
		return null;
	}

	@Override
	public ReadableUser cre