package fiap_adj8.feedback_platform.infra.adapter.out.email;

import fiap_adj8.feedback_platform.application.port.out.email.EmailSender;
import fiap_adj8.feedback_platform.application.port.out.email.input.EmailInput;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.logging.Logger;

public class JakartaMailSender implements EmailSender {

    private static final Logger logger = Logger.getLogger(JakartaMailSender.class.getName());

    private final String from;
    private final String password;
    private final String host;
    private final String port;

    public JakartaMailSender() {
        this.from = System.getenv("EMAIL_SMTP_FROM");
        this.password = System.getenv("EMAIL_SMTP_PASSWORD");
        this.host = System.getenv("EMAIL_SMTP_HOST");
        this.port = System.getenv("EMAIL_SMTP_PORT");

        if (from == null || from.isBlank()) {
            throw new RuntimeException("EMAIL_SMTP_FROM não definida");
        }
        if (password == null || password.isBlank()) {
            throw new RuntimeException("EMAIL_SMTP_PASSWORD não definida");
        }
        if (host == null || host.isBlank()) {
            throw new RuntimeException("EMAIL_SMTP_HOST não definida");
        }
        if (port == null || port.isBlank()) {
            throw new RuntimeException("EMAIL_SMTP_PORT não definida");
        }
    }

    @Override
    public void send(EmailInput emailInput) {
        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.port", port);

            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailInput.to()));
            message.setSubject(emailInput.subject());
            message.setContent(emailInput.htmlContent(), "text/html; charset=UTF-8");

            Transport.send(message);

            logger.info("✅ Email enviado para: " + emailInput.to());

        } catch (Exception e) {
            logger.severe("❌ Falha ao enviar email: " + e.getMessage());
        }
    }
}

