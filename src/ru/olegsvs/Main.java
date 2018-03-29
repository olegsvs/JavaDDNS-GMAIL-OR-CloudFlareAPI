package ru.olegsvs;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static String ip = "";

    public static void main(String[] args) {
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Main::myTask, 0, 600, TimeUnit.SECONDS);
    }

    private static void sendEmail(String currentIp) throws MessagingException {
        Main.ip = currentIp;
        System.out.println("Sending email...");
        String host = "smtp.gmail.com";
        int port = 587;

        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.socketFactory.fallback", "true");

        Session session = Session.getInstance(properties);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@no-spam.com"));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("oleg.texet@gmail.com"));
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.ENGLISH);
        Date resultdate = new Date(System.currentTimeMillis());
        message.setSubject("IP Changed!");
        message.setText("NEW IP: " + currentIp +
                "\nTIME: " + sdf.format(resultdate));

        String username = "";
        String password = "";

        Transport transport = session.getTransport("smtp");
        transport.connect(host, port, username,password);
        transport.sendMessage(message, message.getAllRecipients());
        System.out.println("Email sended!\n");
        try {
            System.out.println(message.getContent().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testIP() {
        URL whatismyip = null;
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = null;
            ip = in.readLine();
            System.out.println(ip + "\n");
            if (!Main.ip.equals(ip)) {
                sendToCloudFlare(ip);
                sendEmail(ip);
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    private static void sendToCloudFlare(String ip) throws IOException {
        final String dnsrid = "";
        final String zoneid = "";
        final String login = "";
        final String globalKey = "";
        final String domain = "";

        System.out.println("Sending to CloudFlare...");
        URL url = new URL("https://api.cloudflare.com/client/v4/zones/"
                + zoneid +"/dns_records/" + dnsrid);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestProperty("X-Auth-Email", login);
        httpCon.setRequestProperty("X-Auth-Key", globalKey);
        httpCon.setRequestProperty("Content-Type", "application/json");
        httpCon.setDoOutput(true);
        httpCon.setRequestMethod("PUT");
        OutputStreamWriter out = new OutputStreamWriter(
                httpCon.getOutputStream());
        out.write("{\"id\":\""+ dnsrid +"\",\"type\":\"A\",\"name\":\""+ domain +"\",\"content\":\""+ ip +"\",\"ttl\":120}");
        out.close();
        httpCon.getInputStream();
        System.out.println("Sending to CloudFlare DNS success!");
    }

    private static void myTask() {
        System.out.println("Running");
        testIP();
    }
}
