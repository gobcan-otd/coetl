package es.gobcan.coetl.security;

import es.gobcan.coetl.config.AESProperties;
import es.gobcan.coetl.util.AESUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

public final class SecurityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);

    private SecurityUtils() {
    }

    public static String getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        String userName = null;
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                userName = springSecurityUser.getUsername();
            } else if (authentication.getPrincipal() instanceof String) {
                userName = (String) authentication.getPrincipal();
            }
        }
        return userName;
    }

    public static String passwordEncoder(String password, AESProperties aesProperties){
        String encodePassword = null;
        try {
            encodePassword = AESUtils.encrypt(password, aesProperties);
        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            LOG.error("Error encrypt password ", e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error encrypt password. Not souch algorithm  ", e);
        }
        return encodePassword;
    }

    public static String passwordDecode(String password, AESProperties aesProperties) {
        String decodePassword = null;
        try {
            decodePassword = AESUtils.decrypt(password, aesProperties);
        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException  e) {
            LOG.error("Error decrypt password ", e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error decrypt password. Not souch algorithm  ", e);
        }
        return decodePassword;
    }
}
