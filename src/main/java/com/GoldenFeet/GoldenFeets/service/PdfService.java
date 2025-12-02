package com.GoldenFeet.GoldenFeets.service;

import com.GoldenFeet.GoldenFeets.entity.DetalleVenta;
import com.GoldenFeet.GoldenFeets.entity.Entrega;
import com.GoldenFeet.GoldenFeets.entity.Venta;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class PdfService {

    // Colores corporativos (Golden Feets)
    private static final DeviceRgb COLOR_PRIMARIO = new DeviceRgb(76, 29, 149); // Morado #4C1D95
    private static final DeviceRgb COLOR_SECUNDARIO = new DeviceRgb(240, 240, 240); // Gris claro

    // === 1. REPORTE DE ENTREGAS (Tu método existente) ===
    public ByteArrayInputStream generarPdfEntregas(List<Entrega> entregas) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            Paragraph titulo = new Paragraph("Reporte de Entregas - Golden Feets")
                    .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER).setFontColor(COLOR_PRIMARIO);
            document.add(titulo);

            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 3, 3}));
            table.setWidth(UnitValue.createPercentValue(100)).setMarginTop(25);

            table.addHeaderCell(crearCeldaEncabezado("ID"));
            table.addHeaderCell(crearCeldaEncabezado("Fecha Creación"));
            table.addHeaderCell(crearCeldaEncabezado("Estado"));
            table.addHeaderCell(crearCeldaEncabezado("Cliente"));
            table.addHeaderCell(crearCeldaEncabezado("Distribuidor"));

            if (entregas.isEmpty()) {
                table.addCell(new Cell(1, 5).add(new Paragraph("No hay entregas para mostrar.")).setTextAlignment(TextAlignment.CENTER));
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
            e.printStackTrace();
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    // === 2. FACTURA DE VENTA (Nuevo Método para el Cliente) ===
    public ByteArrayInputStream generarFacturaVenta(Venta venta) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Formateadores
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

            // --- HEADER ---
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            headerTable.setWidth(UnitValue.createPercentValue(100));

            // Logo / Nombre
            Paragraph logo = new Paragraph("GOLDEN FEETS")
                    .setBold().setFontSize(24).setFontColor(COLOR_PRIMARIO);
            headerTable.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER));

            // Info Factura
            Paragraph infoFactura = new Paragraph()
                    .add(new Paragraph("FACTURA DE VENTA").setBold().setFontSize(14))
                    .add(new Paragraph("\nNº Pedido: GF-" + venta.getIdVenta()))
                    .add(new Paragraph("\nFecha: " + venta.getFechaVenta().format(dateFormatter)))
                    .setTextAlignment(TextAlignment.RIGHT);
            headerTable.addCell(new Cell().add(infoFactura).setBorder(Border.NO_BORDER));

            document.add(headerTable);
            document.add(new Paragraph("\n"));

            // --- INFO CLIENTE ---
            Table clienteTable = new Table(UnitValue.createPercentArray(1));
            clienteTable.setWidth(UnitValue.createPercentValue(100));

            Paragraph datosCliente = new Paragraph()
                    .add(new Paragraph("CLIENTE: ").setBold())
                    .add(new Paragraph(venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() + "\n"))
                    .add(new Paragraph("DIRECCIÓN DE ENVÍO: ").setBold())
                    .add(new Paragraph(venta.getCliente().getDireccion() + ", " + venta.getCliente().getBarrio() + "\n"))
                    .add(new Paragraph(venta.getCliente().getCiudad() + " - " + venta.getCliente().getDepartamento()));

            Cell cellCliente = new Cell().add(datosCliente)
                    .setBackgroundColor(COLOR_SECUNDARIO)
                    .setPadding(10)
                    .setBorder(Border.NO_BORDER);
            clienteTable.addCell(cellCliente);

            document.add(clienteTable);
            document.add(new Paragraph("\n"));

            // --- TABLA PRODUCTOS ---
            Table productosTable = new Table(UnitValue.createPercentArray(new float[]{4, 1, 1, 2, 2}));
            productosTable.setWidth(UnitValue.createPercentValue(100));

            // Encabezados
            productosTable.addHeaderCell(crearCeldaEncabezado("Producto / Descripción"));
            productosTable.addHeaderCell(crearCeldaEncabezado("Talla"));
            productosTable.addHeaderCell(crearCeldaEncabezado("Cant."));
            productosTable.addHeaderCell(crearCeldaEncabezado("Precio Unit."));
            productosTable.addHeaderCell(crearCeldaEncabezado("Subtotal"));

            // Filas
            for (DetalleVenta dv : venta.getDetallesVenta()) {
                // Producto y Color
                String desc = dv.getProducto().getNombre();
                if(dv.getProducto().getColor() != null) desc += "\nColor: " + dv.getProducto().getColor();

                productosTable.addCell(new Paragraph(desc).setFontSize(10));

                // Talla (Conversión explícita de Integer a String para evitar errores)
                String talla = dv.getProducto().getTalla() != null ? String.valueOf(dv.getProducto().getTalla()) : "-";
                productosTable.addCell(new Paragraph(talla).setTextAlignment(TextAlignment.CENTER).setFontSize(10));

                // Cantidad
                productosTable.addCell(new Paragraph(String.valueOf(dv.getCantidad())).setTextAlignment(TextAlignment.CENTER).setFontSize(10));

                // Precio
                productosTable.addCell(new Paragraph(currencyFormatter.format(dv.getPrecioUnitario())).setTextAlignment(TextAlignment.RIGHT).setFontSize(10));

                // Subtotal
                productosTable.addCell(new Paragraph(currencyFormatter.format(dv.getSubtotal())).setTextAlignment(TextAlignment.RIGHT).setFontSize(10));
            }
            document.add(productosTable);

            // --- TOTALES ---
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{3, 2}));
            totalTable.setWidth(UnitValue.createPercentValue(40));
            totalTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);
            totalTable.setMarginTop(10);

            // Cálculos simples (Ajusta según tu lógica de impuestos)
            double totalVenta = venta.getTotal().doubleValue();
            double subtotalBase = totalVenta / 1.19; // Asumiendo IVA incluido del 19%
            double iva = totalVenta - subtotalBase;

            agregarFilaTotal(totalTable, "Subtotal:", subtotalBase, currencyFormatter, false);
            agregarFilaTotal(totalTable, "IVA (19%):", iva, currencyFormatter, false);
            agregarFilaTotal(totalTable, "TOTAL:", totalVenta, currencyFormatter, true);

            document.add(totalTable);

            // --- FOOTER ---
            Paragraph footer = new Paragraph("\n\nGracias por su compra.\nPara cambios y garantías conserve este documento.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY);
            document.add(footer);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // --- Métodos Auxiliares ---

    private Cell crearCeldaEncabezado(String texto) {
        return new Cell().add(new Paragraph(texto).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(COLOR_PRIMARIO)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private void agregarFilaTotal(Table table, String label, double valor, NumberFormat format, boolean esTotal) {
        Paragraph pLabel = new Paragraph(label).setFontSize(10);
        Paragraph pValor = new Paragraph(format.format(valor)).setFontSize(10);

        if (esTotal) {
            pLabel.setBold().setFontSize(12);
            pValor.setBold().setFontSize(12).setFontColor(COLOR_PRIMARIO);
        }

        table.addCell(new Cell().add(pLabel).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
        table.addCell(new Cell().add(pValor).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
    }
}