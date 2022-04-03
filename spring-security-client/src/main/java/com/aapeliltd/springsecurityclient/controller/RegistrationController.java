package com.aapeliltd.springsecurityclient.controller;

import com.aapeliltd.springsecurityclient.entity.User;
import com.aapeliltd.springsecurityclient.entity.VerificationToken;
import com.aapeliltd.springsecurityclient.events.RegistrationCompleteEvent;
import com.aapeliltd.springsecurityclient.model.PasswordModel;
import com.aapeliltd.springsecurityclient.model.UserModel;
import com.aapeliltd.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @GetMapping
    public String hello() {
        return "Hello Welcome to appeliled api";
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        User user = userService.registerUser(userModel);
        applicationEventPublisher.publishEvent(new RegistrationCompleteEvent(user,
                applicationUrl(request)));
        return "success";
    }

    public String changePassword(@RequestBody PasswordModel passwordModel) {
        User user = userService.findUserByEmail(passwordModel.getEmail());
        if(!userService.checkIfValidOldPassword(user, passwordModel.getOldPassword())){
            return "Invalid old password";
        }
        //save new password functionality
        userService.changePassword(user, passwordModel.getNewPassword());
        return "Password changed successfully";
    }

    @GetMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {
        User user = userService.findUserByEmail(passwordModel.getEmail());
        String url = "";
        if(user != null){
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetToken(user, token);
            url = passwordResetTokenMail(user, applicationUrl(request), token);
        }
        return url;

    }

    @PostMapping("savePassword")
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody PasswordModel passwordModel) {
        String result = userService.validPasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")){
            return "invalid token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()) {
            //change the password
            userService.changePassword(user.get(), passwordModel.getNewPassword());
            return "Password reset successfully";
        }
        return "invalid token";

    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url = applicationUrl+ "/resetPassword?token="+token;

        //send verification email

        log.info("Click to reset your password: {}", url);
        return url;
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = userService.validateVerificationtoken(token);
        if(result.equalsIgnoreCase("valid")){
            return "user verified successfully";
        }
        return "bad user";
    }

    @GetMapping("/resendVerificationToken")
    public String resendVerificationToken(@RequestParam("token") String token, HttpServletRequest request) {
        VerificationToken verificationToken = userService.generateNewVerificationToken(token);
        User user = verificationToken.getUser();
        resendVerificationMail(user, applicationUrl(request), verificationToken.getToken());
        return "Verification link sent successfully";

    }

    private void resendVerificationMail(User user, String applicationUrl, String token) {
        String url = applicationUrl+ "/verifyRegistration?token="+token;

        //send verification email

        log.info("Click to verify acount: {}", url);
    }


    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName()+
                ":"+
                request.getServerPort() +
                request.getContextPath();
    }
}
