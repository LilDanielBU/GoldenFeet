package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import com.GoldenFeet.GoldenFeets.entity.Entrega;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Async
    public void enviarCorreoDeEntregaCompletada(Entrega entrega) {
        if (entrega.getVenta() == null || entrega.getVenta().getCliente() == null) {
            System.err.println("Error: No se puede enviar correo. Faltan datos de la venta o del cliente.");
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(entrega.getVenta().getCliente().getEmail());
            helper.setSubject("✅ ¡Tu pedido de Golden Feets ha sido entregado!");

            // --- Construcción del cuerpo del correo en HTML ---
            StringBuilder htmlBody = new StringBuilder();
            htmlBody.append("<html><body style='font-family: Arial, sans-serif; color: #333;'>");
            htmlBody.append("<h1 style='color: #4D1C95;'>¡Hola, ").append(entrega.getVenta().getCliente().getNombre()).append("!</h1>");
            htmlBody.append("<p>Nos complace informarte que tu pedido <strong>#").append(entrega.getVenta().getIdVenta()).append("</strong> ha sido entregado exitosamente.</p>");
            htmlBody.append("<h2 style='color: #6C4AB6;'>Detalles de tu Compra:</h2>");
            htmlBody.append("<table border='1' cellpadding='10' style='border-collapse: collapse; width: 100%;'>");
            htmlBody.append("<thead style='background-color: #f2f2f2;'><tr><th>Producto</th><th>Cantidad</th><th>Precio Unitario</th><th>Subtotal</th></tr></thead>");
            htmlBody.append("<tbody>");

            for (DetalleVenta detalle : entrega.getVenta().getDetallesVenta()) {
                htmlBody.append("<tr>");
                htmlBody.append("<td>").append(detalle.getProducto().getNombre()).append("</td>");
                htmlBody.append("<td style='text-align: center;'>").append(detalle.getCantidad()).append("</td>");
                htmlBody.append("<td style='text-align: right;'>$").append(detalle.getPrecioUnitario()).append("</td>");
                htmlBody.append("<td style='text-align: right;'>$").append(detalle.getSubtotal()).append("</td>");
                htmlBody.append("</tr>");
            }

            htmlBody.append("</tbody></table>");
            htmlBody.append("<h3 style='text-align: right; color: #4D1C95;'>Total: $").append(entrega.getVenta().getTotal()).append("</h3>");
            htmlBody.append("<p>¡Gracias por tu compra en <strong>Golden Feets</strong>!</p>");
            htmlBody.append("</body></html>");

            helper.setText(htmlBody.toString(), true); // true indica que el texto es HTML

            javaMailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}