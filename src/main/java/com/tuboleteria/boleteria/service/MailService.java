package com.tuboleteria.boleteria.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    public void enviarBoleta(String to, String subject, String text, ByteArrayOutputStream pdfStream) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true); // HTML content

        helper.addAttachment("Boleta.pdf", new ByteArrayResource(pdfStream.toByteArray()));

        mailSender.send(message);
    }
}

