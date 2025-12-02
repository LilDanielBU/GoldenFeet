package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource; // IMPORTANTE
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Async
    public void enviarCorreoDeEntregaCompletada(Entrega entrega) {
        if (entrega.getVenta() == null || entrega.getVenta().getCliente() == null) return;

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(entrega.getVenta().getCliente().getEmail());
            helper.setSubject("✅ ¡Tu pedido de Golden Feets ha sido entregado!");

            // Construir HTML simple
            StringBuilder html = new StringBuilder();
            html.append("<html><body>");
            html.append("<h1>¡Pedido #GF-").append(entrega.getVenta().getIdVenta()).append(" Entregado!</h1>");
            html.append("<p>Gracias por tu compra. Esperamos que disfrutes tus productos.</p>");
            html.append("</body></html>");

            helper.setText(html.toString(), true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void enviarConfirmacionCompra(Venta venta, ByteArrayInputStream pdfStream) {
        if (venta.getCliente() == null) return;

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(venta.getCliente().getEmail());
            helper.setSubject("🎉 ¡Pedido Confirmado! #GF-" + venta.getIdVenta());

            // HTML Resumen
            StringBuilder html = new StringBuilder();
            NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

            html.append("<html><body style='font-family: sans-serif; padding: 20px;'>");
            html.append("<h1 style='color: #4C1D95;'>¡Gracias por tu compra!</h1>");
            html.append("<p>Tu pedido <strong>#GF-").append(venta.getIdVenta()).append("</strong> ha sido procesado.</p>");
            html.append("<p>Adjunto encontrarás tu factura en PDF.</p>");
            html.append("<h3>Total Pagado: ").append(formato.format(venta.getTotal())).append("</h3>");
            html.append("</body></html>");

            helper.setText(html.toString(), true);

            // --- CORRECCIÓN CRÍTICA ---
            if (pdfStream != null) {
                // Convertir Stream a ByteArrayResource para evitar error de flujo cerrado
                byte[] bytes = pdfStream.readAllBytes();
                ByteArrayResource pdfResource = new ByteArrayResource(bytes);
                helper.addAttachment("Factura_GF_" + venta.getIdVenta() + ".pdf", pdfResource);
            }

            javaMailSender.send(message);
            System.out.println("Correo enviado a: " + venta.getCliente().getEmail());

        } catch (Exception e) {
            System.err.println("Error enviando correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}