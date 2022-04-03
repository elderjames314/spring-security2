package com.aapeliltd.springsecurityclient.entity.listener;

import com.aapeliltd.springsecurityclient.entity.User;
import com.aapeliltd.springsecurityclient.events.RegistrationCompleteEvent;
import com.aapeliltd.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistrationCompleteListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private UserService userService;
    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        //create the verification token for the user url
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationForUser(token, user);
        //send mail to the user
        String url = event.getApplicationUrl()+ "/verifyRegistration?token="+token;

        //send verification email

        log.info("Click to verify acount: {}", url);

    }
}
