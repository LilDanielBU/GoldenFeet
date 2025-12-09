package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.resend.Resend;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.Locale;

@Service
public class EmailService {

    // Inyectamos las variables desde application.properties
    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    @Async
    public void enviarCorreoDeEntregaCompletada(Entrega entrega) {
        if (entrega.getVenta() == null || entrega.getVenta().getCliente() == null) return;

        try {
            // 1. Inicializar Resend
            Resend resend = new Resend(apiKey);
            String emailCliente = entrega.getVenta().getCliente().getEmail();

            // 2. Construir HTML
            StringBuilder html = new StringBuilder();
            html.append("<html><body>");
            html.append("<h1>¡Pedido #GF-").append(entrega.getVenta().getIdVenta()).append(" Entregado!</h1>");
            html.append("<p>Gracias por tu compra. Esperamos que disfrutes tus productos.</p>");
            html.append("</body></html>");

            // 3. Configurar parámetros
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Golden Feets <" + fromEmail + ">")
                    .to(emailCliente)
                    .subject("✅ ¡Tu pedido de Golden Feets ha sido entregado!")
                    .html(html.toString())
                    .build();

            // 4. Enviar
            resend.emails().send(params);
            System.out.println("Correo de entrega enviado a: " + emailCliente);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error enviando correo entrega: " + e.getMessage());
        }
    }

    @Async
    public void enviarConfirmacionCompra(Venta venta, ByteArrayInputStream pdfStream) {
        if (venta.getCliente() == null) return;

        try {
            Resend resend = new Resend(apiKey);
            String emailCliente = venta.getCliente().getEmail();

            // HTML Resumen
            StringBuilder html = new StringBuilder();
            NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

            html.append("<html><body style='font-family: sans-serif; padding: 20px;'>");
            html.append("<h1 style='color: #4C1D95;'>¡Gracias por tu compra!</h1>");
            html.append("<p>Tu pedido <strong>#GF-").append(venta.getIdVenta()).append("</strong> ha sido procesado.</p>");
            html.append("<p>Adjunto encontrarás tu factura en PDF.</p>");
            html.append("<h3>Total Pagado: ").append(formato.format(venta.getTotal())).append("</h3>");
            html.append("</body></html>");

            // Configuración básica del correo
            CreateEmailOptions.Builder paramsBuilder = CreateEmailOptions.builder()
                    .from("Golden Feets <" + fromEmail + ">")
                    .to(emailCliente)
                    .subject("🎉 ¡Pedido Confirmado! #GF-" + venta.getIdVenta())
                    .html(html.toString());

            // --- LÓGICA DE ADJUNTO (PDF) ---
            if (pdfStream != null) {
                // Resend requiere el archivo en Base64
                byte[] pdfBytes = pdfStream.readAllBytes();
                String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

                Attachment adjunto = Attachment.builder()
                        .fileName("Factura_GF_" + venta.getIdVenta() + ".pdf")
                        .content(pdfBase64) // Aquí va el string en Base64
                        .build();

                paramsBuilder.attachments(adjunto);
            }

            // Enviar
            CreateEmailResponse data = resend.emails().send(paramsBuilder.build());
            System.out.println("Correo compra enviado con ID: " + data.getId());

        } catch (Exception e) {
            System.err.println("Error enviando correo compra con Resend: " + e.getMessage());
            e.printStackTrace();
        }
    }
}