package com.roadwarnings.narino.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender,
            SpringTemplateEngine templateEngine
    ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        if (mailSender == null) {
            log.warn("JavaMailSender no configurado. Los emails no serán enviados.");
        }
    }

    @Value("${spring.mail.username:noreply@roadwarnings.com}")
    private String fromEmail;

    @Value("${app.name:RoadWarnings Nariño}")
    private String appName;

    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        if (mailSender == null) {
            log.warn("Email no enviado (JavaMailSender no configurado) - To: {}, Subject: {}", to, subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email simple enviado a: {}", to);
        } catch (Exception e) {
            log.error("Error al enviar email simple a {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (mailSender == null) {
            log.warn("Email HTML no enviado (JavaMailSender no configurado) - To: {}, Subject: {}", to, subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email HTML enviado a: {}", to);
        } catch (MessagingException e) {
            log.error("Error al enviar email HTML a {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("welcome-email", context);

            sendHtmlEmail(to, "Bienvenido a " + appName, htmlContent);
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida a {}: {}", to, e.getMessage());
            // Fallback a email simple
            sendSimpleEmail(to, "Bienvenido a " + appName,
                    "Hola " + username + ",\n\nBienvenido a " + appName + "!\n\nGracias por registrarte.");
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("resetToken", resetToken);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("password-reset-email", context);

            sendHtmlEmail(to, "Recuperación de Contraseña - " + appName, htmlContent);
        } catch (Exception e) {
            log.error("Error al enviar email de recuperación a {}: {}", to, e.getMessage());
            // Fallback a email simple
            String resetLink = "http://yourapp.com/reset-password?token=" + resetToken;
            sendSimpleEmail(to, "Recuperación de Contraseña - " + appName,
                    "Hola " + username + ",\n\nPara resetear tu contraseña, haz clic en el siguiente enlace:\n" +
                    resetLink + "\n\nEste enlace expira en 24 horas.");
        }
    }

    @Async
    public void sendAlertNotificationEmail(String to, String username, String alertTitle, String alertLocation) {
        String subject = "Nueva alerta cerca de tu ruta - " + appName;
        String text = String.format(
                "Hola %s,\n\n" +
                "Se ha reportado una nueva alerta cerca de una de tus rutas favoritas:\n\n" +
                "Título: %s\n" +
                "Ubicación: %s\n\n" +
                "Visita la app para más detalles.\n\n" +
                "Saludos,\n%s",
                username, alertTitle, alertLocation, appName
        );

        sendSimpleEmail(to, subject, text);
    }

    @Async
    public void sendAlertResolvedEmail(String to, String username, String alertTitle) {
        String subject = "Alerta resuelta - " + appName;
        String text = String.format(
                "Hola %s,\n\n" +
                "La siguiente alerta ha sido marcada como resuelta:\n\n" +
                "Título: %s\n\n" +
                "Gracias por tu contribución a la comunidad.\n\n" +
                "Saludos,\n%s",
                username, alertTitle, appName
        );

        sendSimpleEmail(to, subject, text);
    }

    @Async
    public void sendBadgeEarnedEmail(String to, String username, String badgeName, String badgeDescription) {
        String subject = "¡Nuevo logro desbloqueado! - " + appName;
        String text = String.format(
                "¡Felicidades %s!\n\n" +
                "Has desbloqueado un nuevo logro:\n\n" +
                "%s: %s\n\n" +
                "Sigue contribuyendo a la comunidad para ganar más logros.\n\n" +
                "Saludos,\n%s",
                username, badgeName, badgeDescription, appName
        );

        sendSimpleEmail(to, subject, text);
    }
}
