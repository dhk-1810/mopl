package org.codeit.sb06.team03.mopl.service;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.controller.*;
import org.codeit.sb06.team03.mopl.dto.request.*;
import org.codeit.sb06.team03.mopl.dto.response.CursorResponseUserDto;
import org.codeit.sb06.team03.mopl.dto.response.UserDto;
import org.codeit.sb06.team03.mopl.enums.SortDirection;
import org.codeit.sb06.team03.mopl.entity.vo.EmailAddress;
import org.codeit.sb06.team03.mopl.entity.Account;
import org.codeit.sb06.team03.mopl.entity.Profile;
import org.codeit.sb06.team03.mopl.cache.ProfileImageCache;
import org.codeit.sb06.team03.mopl.service.cqrs.ExternalImageQueryService;
import org.springframework.data.domain.Slice;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserCompositeService {

    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;
    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;

    private final ProfileImageCache profileImageCache;
    private final ExternalImageQueryService imageQueryService;

    public UserDto registerAccount(UserCreateRequest request) {
        Account newAccount = accountCommandService.register(request.name(), new EmailAddress(request.email()), request.password());

        Account account = accountQueryService.getById(newAccount.getId());
        String presignedUrl = imageQueryService.getPresignedUrl(account.getProfile().getImageKey());
        return UserDto.from(account, account.getProfile(), presignedUrl);
    }

    public void updatePassword(UUID userId, PasswordUpdateRequest request) {
        accountCommandService.updatePassword(userId, request.password());
    }

    public void assignUserRole(UUID userId, UserRoleUpdateRequest request) {
        accountCommandService.assignRole(userId, request.role());
    }

    public void updateUserLockStatus(UUID userId, UserLockUpdateRequest request) {
        accountCommandService.updateLocked(userId, request.locked());
    }

    public CursorResponseUserDto getUsers(CursorRequestUserDto request) {
        Slice<Account> accountSlice = accountQueryService.getById(request);
        List<Account> accounts = accountSlice.getContent();
        int limit = request.limit();
        boolean hasNext = accountSlice.hasNext();
        
        List<UUID> accountIds = accounts.stream()
                .map(Account::getId)
                .toList();

        List<Profile> profiles = profileQueryService.getByIdsIn(accountIds);
        Map<UUID, Profile> profileMap = profiles.stream()
                .collect(Collectors.toMap(Profile::getAccountId, Function.identity()));
                
        List<Account> pageAccounts = accounts.size() > limit ? accounts.subList(0, limit) : accounts;
        
        List<String> imageKeys = pageAccounts.stream()
                .map(account -> {
                    Profile profile = profileMap.get(account.getId());
                    return profile != null ? profile.getImageKey() : null;
                })
                .filter(Objects::nonNull)
                .toList();
        Map<String, String> urls = imageQueryService.getPresignedUrls(imageKeys);
        
        List<UserDto> userDtos = pageAccounts.stream()
                .map(account -> {
                    Profile profile = profileMap.get(account.getId());
                    String url = (profile != null) ? urls.get(profile.getImageKey()) : null;
                    return UserDto.from(account, profile, url);
                })
                .toList();
                
        String nextCursor = null;
        String nextIdAfter = null;
        if (hasNext && !accounts.isEmpty()) {
            Account nextItem = accounts.getLast();
            nextCursor = switch (request.sortBy()) {
                case "name" -> {
                    Profile profile = profileMap.get(nextItem.getId());
                    yield profile != null ? profile.getName() : "";
                }
                case "email" -> nextItem.getEmailAddress().value();
                case "createdAt" -> nextItem.getCreatedAt().toString();
                case "isLocked" -> String.valueOf(nextItem.isLocked());
                default -> nextItem.getRole().name();
            };
            nextIdAfter = nextItem.getId().toString();
        }
        
        Long totalCount = accountQueryService.count(request);
        
        return new CursorResponseUserDto(
                userDtos,
                nextCursor,
                nextIdAfter,
                hasNext,
                totalCount,
                request.sortBy(),
                SortDirection.parse(request.sortDirection())
        );
    }

    public UserDto getUserDto(UUID userId) {
        Account account = accountQueryService.getById(userId);
        String presignedUrl = imageQueryService.getPresignedUrl(account.getProfile().getImageKey());
        return UserDto.from(account, account.getProfile(), presignedUrl);
    }

    public UserDto updateProfile(UUID userId, UserUpdateRequest request, @Nullable MultipartFile image) {
        Profile updated = profileCommandService.update(userId, request.name(), image);
        profileImageCache.evictProfileImageUrl(userId);
        Account account = accountQueryService.getById(updated.getAccountId());
        String presignedUrl = imageQueryService.getPresignedUrl(account.getProfile().getImageKey());
        return UserDto.from(account, account.getProfile(), presignedUrl);
    }
}
