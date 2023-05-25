package es.gobcan.coetl.service;

import es.gobcan.coetl.domain.Etl;
import es.gobcan.coetl.domain.Usuario;

public interface MailService {

    void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml);

    void sendEmails(String[] to, String subject, String content, boolean isMultipart, boolean isHtml);

    void sendEmailFromTemplate(Usuario user, String templateName, String titleKey);

    void sendEmailErrorETL(String[] emails, Etl etl, String errorType);

    void sendCreationEmail(Usuario user);
}
