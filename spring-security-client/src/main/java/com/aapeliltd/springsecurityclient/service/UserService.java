package com.aapeliltd.springsecurityclient.service;


import com.aapeliltd.springsecurityclient.entity.User;
import com.aapeliltd.springsecurityclient.entity.VerificationToken;
import com.aapeliltd.springsecurityclient.model.UserModel;

import java.util.Optional;

public interface UserService{
    User registerUser(UserModel userModel);

    void saveVerificationForUser(String token, User user);

    String validateVerificationtoken(String token);

    VerificationToken generateNewVerificationToken(String token);

    User findUserByEmail(String email);

    void createPasswordResetToken(User user, String token);

    String validPasswordResetToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    boolean checkIfValidOldPassword(User user, String oldPassword);
}
