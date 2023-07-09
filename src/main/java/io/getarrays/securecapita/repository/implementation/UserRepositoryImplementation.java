package io.getarrays.securecapita.repository.implementation;

import io.getarrays.securecapita.domain.Role;
import io.getarrays.securecapita.domain.User;
import io.getarrays.securecapita.domain.UserPrincipal;
import io.getarrays.securecapita.exception.ApiException;
import io.getarrays.securecapita.repository.RoleRepository;
import io.getarrays.securecapita.repository.UserRepository;
import io.getarrays.securecapita.rowmapper.UserRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static io.getarrays.securecapita.enumeration.RoleType.ROLE_USER;
import static io.getarrays.securecapita.enumeration.VerificationType.ACCOUNT;
import static io.getarrays.securecapita.query.UserQuery.*;
import static java.util.Map.of;
import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImplementation implements UserRepository<User>, UserDetailsService {

    private final NamedParameterJdbcTemplate jdbc;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public User create(User user){
        // Check the email is unique
        if(getEmailCount(user.getEmail().trim().toLowerCase() ) > 0){
            throw new ApiException("Email already in use. Please use a different email and try again");
        }
        //Save new User
        try {
            KeyHolder holder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getParametersSource(user);
            jdbc.update(INSERT_USER_QUERY,parameters,holder);
            // reset ID on the user
            user.setId(requireNonNull(holder.getKey()).longValue());

            // Add role the user
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());

            // Send verification URL
            String verificationurl = getVerificationurl(UUID.randomUUID().toString(), ACCOUNT.getType());
            //Sacve URL in verification table
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationurl));
            //Send email to user with verification URL
            //emailService.sendVerificationUrl(user.getFirstName(),user.getEmail(),verificationurl,ACCOUNT);
            user.setEnabled(false);
            user.setNotLocked(true);
            //Return the newly created user
            return user;
        }catch (Exception exception){
            log.error(exception.getMessage());
            throw new ApiException("An error occured. Please try again");

        }
    }




    @Override
    public Collection<User> list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email",email), Integer.class);
    }

    private SqlParameterSource getParametersSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", encoder.encode(user.getPassword()));

    }


    private String getVerificationurl(String key, String type) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/"+ type + "/" + key).toString();
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if(email == null){
            log.error("User not found in the database");
            throw new UsernameNotFoundException("User not found in the database");

        }else {
            log.info("User found in the database: {}", email);
          /*  UserPrincipal userPrincipal = new UserPrincipal(user,roleRepository.getRoleByUserId(user.getId()).getPermission());
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    userPrincipal.getAuthorities()
            );*/

            return new UserPrincipal(user,roleRepository.getRoleByUserId(user.getId()).getPermission());
        }
    }

    @Override
    public User getUserByEmail(String email) {
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());
            return user;
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No User found by email: " + email);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }
}
