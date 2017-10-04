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
	
	// ���������ռ��û��ϴ��ļ����ļ�
	// sendMailBox, recvMailBox: "1922884165@qq.com"
	public static void SendResumeFileToQQMailBox(com.jspsmart.upload.File tmpFile, String filePath, String userEmail,
			String sendMailBox, String recvMailBox) throws UnsupportedEncodingException {
		
		String FileName = tmpFile.getFileName();
		String FileNamePath= filePath;
		
		// ����Properties �����ڼ�¼�����һЩ����
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true"); // SMTP�����ʼ���������������֤
		props.put("mail.smtp.host", "smtp.qq.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.user", sendMailBox);
		props.put("mail.password", "koriqnjdsjjceifd"); // STMP����
		
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
		
		// �����ʼ�����
	    try {
			message.setSubject("����");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	
	    Multipart multipart = new MimeMultipart();
	    BodyPart contentPart = new MimeBodyPart();
	    try {
			contentPart.setText("���䣺" + userEmail);
			multipart.addBodyPart(contentPart);
			BodyPart messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(FileNamePath);
			// ��Ӹ���������
			messageBodyPart.setDataHandler(new DataHandler(source));
			// ͨ��Base64�����ת���������ĸ����������ڷ���ʱ��������
			sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
			messageBodyPart.setFileName("=?GBK?B?" + enc.encode(FileName.getBytes("GBK")) + "?=");
			multipart.addBodyPart(messageBodyPart);
			
			message.setContent(multipart); //��multipart����ŵ�message��
			message.saveChanges();
		} catch (MessagingException e1) {
			e1.printStackTrace();
		}
	    
	    // �����ʼ�
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
		}
	}
	
	// ʹ��opencv.expert�������û������ʼ� (Ŀǰ����zoho SMTP������)
	// zoho: https://www.zoho.com/mail/help/zoho-smtp.html
	// messageText: The message system want to send, and it contains HTML format
	// How to send email: http://blog.csdn.net/sunyujia/article/details/2528696
	public static void SendHtmlMessage(String from, String to, String subject, String messageText) throws Exception {
    	// ����Properties �����ڼ�¼�����һЩ����
	    String _from = "";
	    if (from.isEmpty()){
	        _from = "no-reply@opencv.expert";
	    } else{
	        _from = from;
	    }
	    
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp"); 
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");          //��ʾSMTP�����ʼ���������������֤
        props.put("mail.smtp.host", "smtp.zoho.com"); //�˴���дSMTP������
        props.put("mail.smtp.port", "587");   
        props.put("mail.user", _from);
        props.put("mail.password", "jianLiPangPang#@!"); // STMP����(�������ʼ����ܴ������⣬����������Ƿ���ȷ��)
        
        final String userName = props.getProperty("mail.user");
        final String password = props.getProperty("mail.password");
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        
        // ����ʼ��Ự����  
        Session session = Session.getInstance(props, authenticator);
        
        // ����MIME�ʼ�����  
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(_from));                            // from
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(to));// to  
        mimeMessage.setSubject(subject);  
        mimeMessage.setSentDate(new java.util.Date());  // ��������  
        Multipart mp = new MimeMultipart("related");    // related��ζ�ſ��Է���html��ʽ���ʼ�  
        
        BodyPart bodyPart = new MimeBodyPart();         // �ʼ�����
        bodyPart.setDataHandler(new DataHandler(messageText, "text/html;charset=utf-8"));
        
        mp.addBodyPart(bodyPart);  
        mimeMessage.setContent(mp);                     // �����ʼ����ݶ��� 
        
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
