package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.entity.InventarioMovimiento;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteInventarioPDF {

    private List<InventarioMovimiento> listaMovimientos;

    public ReporteInventarioPDF(List<InventarioMovimiento> listaMovimientos) {
        this.listaMovimientos = listaMovimientos;
    }

    private void escribirCabeceraTabla(PdfPTable tabla) {
        PdfPCell celda = new PdfPCell();
        celda.setBackgroundColor(new Color(124, 58, 237)); // Tu color morado --primary
        celda.setPadding(5);

        Font fuente = FontFactory.getFont(FontFactory.HELVETICA);
        fuente.setColor(Color.WHITE);
        fuente.setSize(10); // Letra un poco más pequeña para que quepa todo

        celda.setPhrase(new Phrase("Fecha", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Producto", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Tipo", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Cant.", fuente));
        tabla.addCell(celda);

        celda.setPhrase(new Phrase("Motivo", fuente));
        tabla.addCell(celda);
    }

    private void escribirDatosTabla(PdfPTable tabla) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Font fuenteData = FontFactory.getFont(FontFactory.HELVETICA);
        fuenteData.setSize(9); // Letra pequeña para datos

        for (InventarioMovimiento mov : listaMovimientos) {
            tabla.addCell(new Phrase(mov.getFecha().format(formatter), fuenteData));
            tabla.addCell(new Phrase(mov.getProducto().getNombre(), fuenteData));

            // Colorear el texto según el tipo
            Font fuenteTipo = FontFactory.getFont(FontFactory.HELVETICA);
            fuenteTipo.setSize(9);
            if(mov.getTipoMovimiento().contains("INGRESO")) {
                fuenteTipo.setColor(new Color(16, 185, 129)); // Verde
            } else {
                fuenteTipo.setColor(new Color(239, 68, 68)); // Rojo
            }
            tabla.addCell(new Phrase(mov.getTipoMovimiento(), fuenteTipo));

            tabla.addCell(new Phrase(String.valueOf(mov.getCantidad()), fuenteData));
            tabla.addCell(new Phrase(mov.getMotivo(), fuenteData));
        }
    }

    public void exportar(HttpServletResponse response) throws DocumentException, IOException {
        Document documento = new Document(PageSize.A4);
        PdfWriter.getInstance(documento, response.getOutputStream());

        documento.open();

        Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fuenteTitulo.setSize(18);
        fuenteTitulo.setColor(new Color(59, 28, 95)); // --purple-900

        Paragraph titulo = new Paragraph("Historial Completo de Inventario - Golden Feets", fuenteTitulo);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        documento.add(titulo);

        documento.add(new Paragraph(" ")); // Espacio vacío

        // Tabla de 5 columnas
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100f);
        tabla.setWidths(new float[] {2.5f, 3.5f, 2.0f, 1.0f, 3.0f}); // Anchos relativos
        tabla.setSpacingBefore(10);

        escribirCabeceraTabla(tabla);
        escribirDatosTabla(tabla);

        documento.add(tabla);
        documento.close();
    }
}