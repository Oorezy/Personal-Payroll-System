package com.introtech.authenticationservice.service;

import com.introtech.authenticationservice.entity.AuthUser;
import com.introtech.authenticationservice.CustomException;
import com.introtech.authenticationservice.repository.AuthUserRepository;
import com.introtech.introtechutil.email.EmailNotificationVO;
import com.introtech.introtechutil.email.EmailService;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final EmailService emailService;

    //TEMPORARY
    private final ConcurrentHashMap<String, String> usersOtp = new ConcurrentHashMap<>();
    private final AuthUserRepository authUserRepository;

    public AuthService(EmailService emailService, AuthUserRepository authUserRepository) {
        this.emailService = emailService;
        this.authUserRepository = authUserRepository;
    }

    public void sendOtp(AuthUser authUser){
        String otp = RandomStringUtils.randomNumeric(6);

        EmailNotificationVO emailNotificationVO = new EmailNotificationVO();
        emailNotificationVO.setEmail(authUser.getEmail());
        emailNotificationVO.setSubject("Email Verification");
        emailNotificationVO.setTemplateId("verification-otp");
        Map<String, Object> map = new HashMap<>();
        map.put("otp", otp);
        map.put("name", authUser.getFirstName() + " " + authUser.getLastName());
        emailNotificationVO.setVariables(map);

        otp = otp + "-" + Instant.now().plus(5, ChronoUnit.MINUTES);
        usersOtp.put(authUser.getEmail(), otp);
        emailService.sendEmail(emailNotificationVO);
    }

    public void resendOtp(String email) throws CustomException {
        AuthUser authUser = authUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomException("User not found"));

        if (authUser.isVerified()) {
            throw new CustomException("Email already verified");
        }

        sendOtp(authUser);
    }

    public boolean verifyOtp(String email, String otp){

        String normalizedEmail = email.toLowerCase();
        String savedOtp = usersOtp.get(normalizedEmail);

        if(savedOtp == null) {
            return false;
        }
        String time = savedOtp.substring(savedOtp.indexOf("-") + 1);
        savedOtp = savedOtp.substring(0, savedOtp.indexOf("-"));

        if (! savedOtp.equals(otp)){
            return false;
        }
        if(Instant.now().isBefore(Instant.parse(time))){
            AuthUser user = authUserRepository.findByEmailIgnoreCase(email).orElse(null);
            if(user != null){
                user.setVerified(true);
                authUserRepository.save(user);
            }
            usersOtp.remove(normalizedEmail);
            return true;
        }
        return false;
    }
}
