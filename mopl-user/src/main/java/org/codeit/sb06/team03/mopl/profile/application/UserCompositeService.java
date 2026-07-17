package org.codeit.sb06.team03.mopl.profile.application;

import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.account.application.AccountCommandService;
import org.codeit.sb06.team03.mopl.account.application.AccountQueryService;
import org.codeit.sb06.team03.mopl.account.application.in.*;
import org.codeit.sb06.team03.mopl.account.domain.Account;
import org.codeit.sb06.team03.mopl.profile.application.in.UpdateProfileCommand;
import org.codeit.sb06.team03.mopl.profile.domain.entity.Profile;
import org.codeit.sb06.team03.mopl.profile.infra.ProfileMapper;
import org.codeit.sb06.team03.mopl.profile.infra.in.*;
import org.codeit.sb06.team03.mopl.cache.ProfileImageCache;
import org.codeit.sb06.team03.mopl.image.application.ImageQueryService;
import org.codeit.sb06.team03.mopl.common.enums.SortDirection;
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

    private final AccountMapper accountMapper;
    private final ProfileMapper profileMapper;
    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;
    private final ProfileCommandService profileCommandService;
    private final ProfileQueryService profileQueryService;

    private final ProfileImageCache profileImageCache;
    private final ImageQueryService imageQueryService;

    public UserDto registerAccount(UserCreateRequest request) {
        RegisterAccountCommand command = accountMapper.toCommand(request);
        Account newAccount = accountCommandService.register(command);

        Account account = accountQueryService.getById(newAccount.getId());
        String presignedUrl = imageQueryService.getPresignedUrl(account.getProfile().getImageKey());
        return UserDto.from(account, account.getProfile(), presignedUrl);
    }

    public void updatePassword(UUID userId, PasswordUpdateRequest request) {
        UpdatePasswordCommand command = accountMapper.toCommand(request);
        accountCommandService.updatePassword(userId, command);
    }

    public void assignUserRole(UUID userId, UserRoleUpdateRequest request) {
        AssignRoleCommand command = accountMapper.toCommand(request);
        accountCommandService.assignRole(userId, command);
    }

    public void updateUserLockStatus(UUID userId, UserLockUpdateRequest request) {
        UpdateLockStatusCommand command = accountMapper.toCommand(request);
        accountCommandService.updateLocked(userId, command);
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
        UpdateProfileCommand command = profileMapper.toCommand(userId, request, image);
        Profile updated = profileCommandService.update(command);
        profileImageCache.evictProfileImageUrl(userId);
        Account account = accountQueryService.getById(updated.getAccountId());
        String presignedUrl = imageQueryService.getPresignedUrl(account.getProfile().getImageKey());
        return UserDto.from(account, account.getProfile(), presignedUrl);
    }
}
