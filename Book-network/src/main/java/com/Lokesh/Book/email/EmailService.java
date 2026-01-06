package com.Lokesh.Book.email;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Service
@RequiredArgsConstructor

@Async//sending email might be time-consuming so we make it async
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine springTemplateEngine;
    public void sendEmail(
            String to,
            EmailTemplateName emailTemplate,
            String username,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {
        String templateName;
        if(emailTemplate==null){
            templateName="confirm-email";
        }else{
            templateName=emailTemplate.name();
        }

        MimeMessage mimeMessage=mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper= new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );
        HashMap<String,Object> properties= new HashMap<>();
        properties.put("username",username);
        properties.put("confirmationUrl",confirmationUrl);
        properties.put("activationCode",activationCode);

        Context context=new Context();
        context.setVariables(properties);

        mimeMessageHelper.setFrom("lokeshreddy3704@gmail.com");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);

        String template= springTemplateEngine.process(templateName,context);
        mimeMessageHelper.setText(template,true);
        mailSender.send(mimeMessage);
    }
}
