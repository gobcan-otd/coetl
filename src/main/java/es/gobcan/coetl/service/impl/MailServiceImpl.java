package es.gobcan.coetl.service.impl;

import java.util.Date;
import java.util.Locale;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import es.gobcan.coetl.config.ApplicationProperties;
import es.gobcan.coetl.config.Constants;
import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Usuario;
import es.gobcan.coetl.service.MailService;
import io.github.jhipster.config.JHipsterProperties;

/**
 * Service for sending emails.
 * <p>
 * We use the @Async annotation to send emails asynchronously.
 */
@Service
public class MailServiceImpl implements MailService {

    private final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    private static final String USER = "user";

    private static final String BASE_URL = "baseUrl";

    private static final String EMAIL_TEMPLATE_ETL_ERROR = "etlExecutionErrorEmail";

    private final JHipsterProperties jHipsterProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;
    
    private final ApplicationProperties applicationProperties;

    public MailServiceImpl(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine, ApplicationProperties applicationProperties) {

        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.applicationProperties = applicationProperties;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug("Enviar email [multipart '{}' y html '{}'] a '{}' con asunto '{}' y contenido={}", isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, CharEncoding.UTF_8);
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.debug("Correo enviado al usuario '{}'", to);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Email no pudo ser enviado al usuario '{}'", to, e);
            } else {
                log.warn("Email no pudo ser enviado al usuario '{}': {}", to, e.getMessage());
            }
        }
    }

    @Async
    public void sendEmails(String[] to, String subject, String content, boolean isMultipart, boolean isHtml) {
    	log.debug("Se procede a enviar correo a una lista");
    	
    	// Se envían de uno en uno porque se detectó que el servidor de correo de gobierno bloquea todo el envío si 
    	// alguno de los correos es erróneo o está dado de baja
        for(int i = 0; i < to.length; i++) {
        	sendEmail(to[i], subject, content, isMultipart, isHtml);
        }
    }

    @Async
    public void sendEmailFromTemplate(Usuario user, String templateName, String titleKey) {
        Locale locale = Constants.DEFAULT_LOCALE;
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmail(user.getEmail(), subject, content, false, true);
    }

    @Override
    public void sendEmailErrorETL(String[] emails, Etl etl, String errorType) {
        Locale locale = Constants.DEFAULT_LOCALE;
        Context context = new Context(locale);
        context.setVariable("etl", etl);
        context.setVariable("tipoError", errorType);
        context.setVariable("date", new Date());
        String content = templateEngine.process(EMAIL_TEMPLATE_ETL_ERROR, context);
        String[] args = {etl.getCode()};

        String subject = messageSource.getMessage("email.etl.error.title", args, locale);
        if (!Constants.ERROR_MESSAGE_SUBJECT_DISABLED_ENVIROMENT.equalsIgnoreCase(applicationProperties.getEnviroment())) {
        	subject = String.format("[COETL - %s] %s", applicationProperties.getEnviroment(), subject);
        }
        sendEmails(emails, subject, content, false, true);
    }

    @Async
    public void sendCreationEmail(Usuario user) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "creationEmail", "email.creation.title");
    }

}
