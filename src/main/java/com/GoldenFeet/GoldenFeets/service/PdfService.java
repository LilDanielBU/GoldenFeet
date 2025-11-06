package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    public ByteArrayInputStream generarPdfEntregas(List<Entrega> entregas) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // --- Estilos y Formato ---
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // --- Título del Documento ---
            Paragraph titulo = new Paragraph("Reporte de Entregas - Golden Feets")
                    .setBold()
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(titulo);

            // --- Tabla de Contenido ---
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 3, 3}));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(25);

            // --- Encabezados de la Tabla ---
            table.addHeaderCell(new Cell().add(new Paragraph("ID").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Fecha Creación").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Estado").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Cliente").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Distribuidor").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            // --- Llenado de la Tabla con los Datos ---
            if (entregas.isEmpty()) {
                table.addCell(new Cell(1, 5).add(new Paragraph("No hay entregas para mostrar con los filtros seleccionados.")).setTextAlignment(TextAlignment.CENTER).setPadding(10));
            } else {
                for (Entrega entrega : entregas) {
                    table.addCell(String.valueOf(entrega.getIdEntrega()));
                    table.addCell(entrega.getFechaCreacion() != null ? entrega.getFechaCreacion().format(formatter) : "N/A");
                    table.addCell(entrega.getEstado() != null ? entrega.getEstado() : "N/A");
                    table.addCell(entrega.getVenta().getCliente() != null ? entrega.getVenta().getCliente().getNombre() : "N/A");
                    table.addCell(entrega.getDistribuidor() != null ? entrega.getDistribuidor().getNombre() : "Sin Asignar");
                }
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            // Es una buena práctica registrar el error
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}