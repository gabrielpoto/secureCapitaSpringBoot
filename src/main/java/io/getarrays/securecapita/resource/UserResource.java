package io.getarrays.securecapita.resource;

import io.getarrays.securecapita.domain.HttpResponse;
import io.getarrays.securecapita.domain.User;
import io.getarrays.securecapita.domain.UserPrincipal;
import io.getarrays.securecapita.dto.UserDTO;
import io.getarrays.securecapita.form.LoginForm;
import io.getarrays.securecapita.provider.TokenProvider;
import io.getarrays.securecapita.service.RoleService;
import io.getarrays.securecapita.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.Map;

import static io.getarrays.securecapita.dtomapper.UserDTOMapper.toUser;
import static java.time.LocalDateTime.now;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

@RestController
@RequestMapping(path = "/user")
@RequiredArgsConstructor
public class UserResource {
    private final UserService userService;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm){
        authenticationManager.authenticate(unauthenticated(loginForm.getEmail(), loginForm.getPassword()));
        UserDTO user = userService.getUserByEmail(loginForm.getEmail());
        return user.isUsingMfa() ? sendVerificationCode(user) : sendResponse(user);

    }



    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user){

        UserDTO userDTO = userService.createUser(user);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", userDTO))
                        .message("user created")
                        .status(HttpStatus.CREATED)
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }

    @GetMapping ("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication){

        UserDTO user = userService.getUserByEmail(authentication.getName());
        System.out.println(authentication.getPrincipal());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of(
                                "user", user
                        ))
                        .message("Profile Retrieved")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }


    @RequestMapping ("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request){

        return ResponseEntity.badRequest().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("There is no mapping for a " + request.getMethod() + " request for this path on the server")
                        .status(HttpStatus.BAD_REQUEST)
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
    }


    @GetMapping ("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email,@PathVariable("code") String code ){

        UserDTO user = userService.verifyCode(email,code);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of(
                                "user", user,
                                "access_token", tokenProvider.createAccessToken(getUserprincipal(user)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserprincipal(user))
                        ))
                        .message("Login Success")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toString());
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDTO user) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of(
                                "user", user,
                                "access_token", tokenProvider.createAccessToken(getUserprincipal(user)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserprincipal(user))
                        ))
                        .message("Login Success")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    private UserPrincipal getUserprincipal(UserDTO user) {
        return new UserPrincipal(toUser(userService.getUserByEmail(user.getEmail())), roleService.getRoleByUserId(user.getId()).getPermission());
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO user) {
        userService.sendVerificationCode(user);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(Map.of("user", user))
                        .message("Verification Code Sent")
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }
}
