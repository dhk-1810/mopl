package org.codeit.sb06.team03.mopl.controller;

import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.codeit.sb06.team03.mopl.composite.UserCompositeService;
import org.codeit.sb06.team03.mopl.profile.infra.in.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {

    private final UserCompositeService userCompositeService;

    @Override
    @PostMapping
    public ResponseEntity<UserDto> postUsers(@RequestBody UserCreateRequest request) {
        UserDto response = userCompositeService.registerAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable UUID userId, @RequestBody PasswordUpdateRequest request){
        userCompositeService.updatePassword(userId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);

    }

    @PatchMapping("/{userId}/role")
    @RolesAllowed("ADMIN")
    public ResponseEntity<Void> patchUsersRole(
            @PathVariable(name = "userId") UUID userId,
            @RequestBody UserRoleUpdateRequest request
    ) {
        userCompositeService.assignUserRole(userId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PatchMapping("/{userId}/locked")
    @RolesAllowed("ADMIN")
    public ResponseEntity<Void> patchUsersLockStatus(
            @PathVariable(name = "userId") UUID userId,
            @RequestBody UserLockUpdateRequest request
    ) {
        userCompositeService.updateUserLockStatus(userId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @GetMapping
    @RolesAllowed("ADMIN")
    public ResponseEntity<CursorResponseUserDto> getUsers(@ModelAttribute CursorRequestUserDto request) {
        CursorResponseUserDto response = userCompositeService.getUsers(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
        UserDto response = userCompositeService.getUserDto(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> patchUserProfile(
            @PathVariable UUID userId,
            @RequestPart UserUpdateRequest request,
            @Nullable @RequestPart(required = false) MultipartFile image
    ) {
        UserDto response = userCompositeService.updateProfile(userId, request, image);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
