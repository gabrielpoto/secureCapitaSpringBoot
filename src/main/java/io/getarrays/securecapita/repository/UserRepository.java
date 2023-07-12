package io.getarrays.securecapita.repository;

import io.getarrays.securecapita.domain.User;
import io.getarrays.securecapita.dto.UserDTO;

import java.util.Collection;

public interface UserRepository <T extends User>{
    /*Basic CRUD operation*/

    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T data);
    Boolean delete(Long id);

    /*MORE complex operation*/
    User getUserByEmail(String email);

    void sendVerificationCode(UserDTO user);

    User verifyCode(String email, String code);

    void resetPassword(String email);

    User verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    T verifyAccountKey(String key);
}
