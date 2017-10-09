package Util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;

public class MailUtil{
    
    // 采用邮箱收集用户上传的简历文件
    // sendMailBox, recvMailBox: "1922884165@qq.com"
    public static void SendResumeFileToQQMailBox(com.jspsmart.upload.File tmpFile, String filePath, String userEmail,
            String sendMailBox, String recvMailBox) throws UnsupportedEncodingException {
        
        String FileName = tmpFile.getFileName();
        String FileNamePath = filePath;
        
        // 创建Properties 类用于记录邮箱的一些属性
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true"); // SMTP发送邮件，必须进行身份验证
        props.put("mail.smtp.host", "smtp.qq.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.user", sendMailBox);
        props.put("mail.password", "koriqnjdsjjceifd"); // STMP口令
        
        final String userName = props.getProperty("mail.user");
        final String password = props.getProperty("mail.password");
        
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        
        Session mailSession = Session.getInstance(props, authenticator);
        MimeMessage message = new MimeMessage(mailSession);
        
        InternetAddress from; // IP address for sender
        try {
            from = new InternetAddress(props.getProperty("mail.user"));
            try { message.setFrom(from);} 
            catch (MessagingException e) { e.printStackTrace(); }
        } catch (AddressException e) {
            e.printStackTrace();
        }
        
        InternetAddress to; // IP address for receiver
        try {
            to = new InternetAddress(recvMailBox);
            try { message.setRecipient(RecipientType.TO, to); } 
            catch (MessagingException e) { e.printStackTrace(); }
        } catch (AddressException e1) {
            e1.printStackTrace();
        }
        
        // 设置邮件标题
        try {
            message.setSubject("简历");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    
        Multipart multipart = new MimeMultipart();
        BodyPart contentPart = new MimeBodyPart();
        try {
            contentPart.setText("邮箱：" + userEmail);
            multipart.addBodyPart(contentPart);
            BodyPart messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(FileNamePath);
            // 添加附件的内容
            messageBodyPart.setDataHandler(new DataHandler(source));
            // 通过Base64编码的转换可以中文附件标题名在发送时不会乱码
            sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
            messageBodyPart.setFileName("=?GBK?B?" + enc.encode(FileName.getBytes("GBK")) + "?=");
            multipart.addBodyPart(messageBodyPart);
            
            message.setContent(multipart); //将multipart对象放到message中
            message.saveChanges();
        } catch (MessagingException e1) {
            e1.printStackTrace();
        }
        
        // 发送邮件
        try {
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-Java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
            CommandMap.setDefaultCommandMap(mc);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    // 使用opencv.expert邮箱向用户发送邮件 (目前采用zoho SMTP服务器)
    // zoho: https://www.zoho.com/mail/help/zoho-smtp.html
    // messageText: The message system want to send, and it contains HTML format
    // How to send email: http://blog.csdn.net/sunyujia/article/details/2528696
    public static void SendHtmlMessage(String from, String to, String subject, String messageText) throws Exception {
        // 创建Properties 类用于记录邮箱的一些属性
        String _from = "";
        if (from.isEmpty()){
            _from = "no-reply@opencv.expert";
        } else{
            _from = from;
        }
        
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp"); 
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");          //表示SMTP发送邮件，必须进行身份验证
        props.put("mail.smtp.host", "smtp.zoho.com"); //此处填写SMTP服务器
        props.put("mail.smtp.port", "587");   
        props.put("mail.user", _from);
        props.put("mail.password", "jianLiPangPang#@!"); // STMP口令(若发送邮件功能存在问题，需检查该密码是否正确！)
        
        final String userName = props.getProperty("mail.user");
        final String password = props.getProperty("mail.password");
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        
        // 获得邮件会话对象  
        Session session = Session.getInstance(props, authenticator);
        
        // 创建MIME邮件对象  
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(_from));                            // from
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));// to  
        mimeMessage.setSubject(subject);  
        mimeMessage.setSentDate(new java.util.Date());  // 发送日期  
        Multipart mp = new MimeMultipart("related");    // related意味着可以发送html格式的邮件  
        
        BodyPart bodyPart = new MimeBodyPart();         // 邮件正文
        bodyPart.setDataHandler(new DataHandler(messageText, "text/html;charset=utf-8"));
        
        mp.addBodyPart(bodyPart);  
        mimeMessage.setContent(mp);                     // 设置邮件内容对象 
        
        try{
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-Java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
            CommandMap.setDefaultCommandMap(mc);
            Transport.send(mimeMessage);
        } catch (MessagingException e){
            e.printStackTrace();
        }
    }   
}