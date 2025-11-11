package util;

import model.Bill;
import model.Patient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Desktop;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class PdfGenerator {

    private static final String PDF_DIRECTORY = "generated_bills";
    private static final PDType1Font FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDType1Font FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font FONT_MONO = new PDType1Font(Standard14Fonts.FontName.COURIER);
    
    // Modern color scheme
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);      // Modern Blue
    private static final Color COLOR_ACCENT = new Color(59, 130, 246);      // Light Blue
    private static final Color COLOR_SUCCESS = new Color(34, 197, 94);      // Green
    private static final Color COLOR_BG_LIGHT = new Color(248, 250, 252);   // Very Light Gray
    private static final Color COLOR_TABLE_HEADER = new Color(241, 245, 249); // Light Gray
    private static final Color COLOR_BORDER = new Color(226, 232, 240);     // Border Gray
    private static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);  // Almost Black
    private static final Color COLOR_TEXT_SECONDARY = new Color(100, 116, 139); // Medium Gray

    public static File generateBillPdf(Bill bill, Patient patient) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDRectangle mediaBox = page.getMediaBox();
        float margin = 50;
        float width = mediaBox.getWidth() - 2 * margin;
        float y = mediaBox.getHeight() - 60;

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            // === 1. Modern Header with Colored Bar ===
            drawBox(contentStream, 0, mediaBox.getHeight() - 8, mediaBox.getWidth(), 8, COLOR_PRIMARY);
            
            y -= 30;
            drawText(contentStream, FONT_BOLD, 24, margin, y, "MEDICAL INVOICE", COLOR_PRIMARY);
            y -= 30;
            drawText(contentStream, FONT_REGULAR, 11, margin, y, "Hospital Management System • Patient Billing Statement", COLOR_TEXT_SECONDARY);
            y -= 30;

            // === 2. Patient & Bill Info Cards ===
            float cardHeight = 110;
            float cardSpacing = 15;
            float cardWidth = (width - cardSpacing) / 2;
            
            float cardY = y;
            drawRoundedCard(contentStream, margin, cardY - cardHeight, cardWidth, cardHeight);
            
            float textY = cardY - 25;
            drawText(contentStream, FONT_BOLD, 10, margin + 15, textY, "PATIENT DETAILS", COLOR_PRIMARY);
            textY -= 20;
            
            drawCardRow(contentStream, margin + 15, textY, "Name", patient.getName());
            textY -= 16;
            drawCardRow(contentStream, margin + 15, textY, "Patient ID", String.valueOf(patient.getPatientId()));
            textY -= 16;
            if (patient.getAdmittedDate() != null) {
                drawCardRow(contentStream, margin + 15, textY, "Admitted", 
                    new SimpleDateFormat("dd MMM yyyy").format(patient.getAdmittedDate()));
                textY -= 16;
            }
            drawCardRow(contentStream, margin + 15, textY, "Diagnosis", 
                (patient.getIllness() != null ? patient.getIllness() : "N/A"));

            float card2X = margin + cardWidth + cardSpacing;
            drawRoundedCard(contentStream, card2X, cardY - cardHeight, cardWidth, cardHeight);
            
            textY = cardY - 25;
            drawText(contentStream, FONT_BOLD, 10, card2X + 15, textY, "INVOICE DETAILS", COLOR_PRIMARY);
            textY -= 20;
            
            drawCardRow(contentStream, card2X + 15, textY, "Invoice No", String.format("#INV-%04d", bill.getBillId()));
            textY -= 16;
            drawCardRow(contentStream, card2X + 15, textY, "Invoice Date", 
                new SimpleDateFormat("dd MMM yyyy").format(bill.getBillDate()));
            textY -= 16;
            drawCardRow(contentStream, card2X + 15, textY, "Status", "Due");

            y = cardY - cardHeight - 30;

            // === 3. Modern Table ===
            drawText(contentStream, FONT_BOLD, 12, margin, y, "CHARGES BREAKDOWN", COLOR_TEXT_PRIMARY);
            y -= 25;
            
            float tableStartY = y;
            float rowHeight = 35;
            
            drawBox(contentStream, margin, y - rowHeight, width, rowHeight, COLOR_TABLE_HEADER);
            drawBox(contentStream, margin, y - rowHeight, width, rowHeight, null, COLOR_BORDER, 1);
            
            float col1X = margin + 20;
            float col2X = margin + width - 150;
            
            drawText(contentStream, FONT_BOLD, 10, col1X, y - 21, "DESCRIPTION", COLOR_TEXT_PRIMARY);
            drawText(contentStream, FONT_BOLD, 10, col2X, y - 21, "AMOUNT (INR)", COLOR_TEXT_PRIMARY);
            
            y -= rowHeight;
            
            y = drawModernTableRow(contentStream, y, "Hospital Bed Charges", bill.getBedCharge(), 
                rowHeight, margin, width, col1X, col2X, true);
            y = drawModernTableRow(contentStream, y, "Base Service Charges", bill.getServiceCharge(), 
                rowHeight, margin, width, col1X, col2X, false);
            y = drawModernTableRow(contentStream, y, "Doctor Consultation Fee", bill.getDoctorFee(), 
                rowHeight, margin, width, col1X, col2X, true);
            
            drawLine(contentStream, margin, y, margin + width, 1.5f, COLOR_BORDER);
            y -= 25;

            // === 4. Total Section with Highlight ===
            float totalBoxHeight = 50;
            drawBox(contentStream, margin, y - totalBoxHeight, width, totalBoxHeight, COLOR_BG_LIGHT);
            drawBox(contentStream, margin, y - totalBoxHeight, width, totalBoxHeight, null, COLOR_PRIMARY, 2);
            
            float totalY = y - 30;
            drawText(contentStream, FONT_BOLD, 14, margin + 20, totalY, "TOTAL AMOUNT DUE", COLOR_TEXT_PRIMARY);
            
            String totalText = String.format("Rs. %.2f", bill.getTotal());
            float totalTextWidth = (FONT_BOLD.getStringWidth(totalText) / 1000f) * 20;
            drawText(contentStream, FONT_BOLD, 20, margin + width - totalTextWidth - 20, totalY, totalText, COLOR_PRIMARY);
            
            y -= totalBoxHeight + 30;

            // === 5. Payment Info Box ===
            float infoBoxHeight = 45;
            drawBox(contentStream, margin, y - infoBoxHeight, width, infoBoxHeight, new Color(254, 249, 195));
            drawBox(contentStream, margin, y - infoBoxHeight, width, infoBoxHeight, null, new Color(234, 179, 8), 1);
            
            float infoY = y - 18;
            
            // [START] THE FIX
            // Replaced '⚠' with '(!)'
            drawText(contentStream, FONT_BOLD, 9, margin + 15, infoY, "(!) Payment Information", new Color(133, 77, 14));
            // [END] THE FIX

            infoY -= 14;
            drawText(contentStream, FONT_REGULAR, 8, margin + 15, infoY, "Please settle this invoice within 7 days. For payment queries, contact our billing department.", new Color(133, 77, 14));
            
            y -= infoBoxHeight + 40;

            // === 6. Modern Footer ===
            float footerY = 70;
            drawLine(contentStream, margin, footerY + 15, margin + width, 0.5f, COLOR_BORDER);
            
            drawText(contentStream, FONT_REGULAR, 8, margin, footerY, 
                "Thank you for choosing our healthcare services. We wish you a speedy recovery.", COLOR_TEXT_SECONDARY);
            footerY -= 12;
            drawText(contentStream, FONT_REGULAR, 8, margin, footerY, 
                "This is a computer-generated invoice. No signature required.", COLOR_TEXT_SECONDARY);
            
            String footerRight = String.format("Invoice #%04d", bill.getBillId());
            float footerRightWidth = (FONT_REGULAR.getStringWidth(footerRight) / 1000f) * 8;
            drawText(contentStream, FONT_REGULAR, 8, margin + width - footerRightWidth, footerY + 12, footerRight, COLOR_TEXT_SECONDARY);
        }

        // Save the file
        File pdfDir = new File(PDF_DIRECTORY);
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }
        String fileName = "Invoice-" + bill.getBillId() + "-" + patient.getName().replaceAll("\\s+", "_") + ".pdf";
        File file = new File(pdfDir, fileName);
        
        document.save(file);
        document.close();
        
        return file;
    }
    
    // === HELPER METHODS ===

    private static void drawText(PDPageContentStream stream, PDType1Font font, int fontSize, 
                                 float x, float y, String text) throws IOException {
        drawText(stream, font, fontSize, x, y, text, COLOR_TEXT_PRIMARY);
    }
    
    private static void drawText(PDPageContentStream stream, PDType1Font font, int fontSize, 
                                 float x, float y, String text, Color color) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.setNonStrokingColor(color);
        stream.newLineAtOffset(x, y);
        stream.showText(text != null ? text : "N/A");
        stream.endText();
    }
    
    private static void drawCardRow(PDPageContentStream stream, float x, float y, String label, String value) throws IOException {
        drawText(stream, FONT_REGULAR, 9, x, y, label + ":", COLOR_TEXT_SECONDARY);
        drawText(stream, FONT_BOLD, 9, x + 80, y, value, COLOR_TEXT_PRIMARY);
    }

    private static void drawLine(PDPageContentStream stream, float xStart, float y, float xEnd, 
                                 float thickness, Color color) throws IOException {
        stream.setStrokingColor(color);
        stream.setLineWidth(thickness);
        stream.moveTo(xStart, y);
        stream.lineTo(xEnd, y);
        stream.stroke();
    }

    private static void drawBox(PDPageContentStream stream, float x, float y, float width, float height, 
                                Color fillColor) throws IOException {
        drawBox(stream, x, y, width, height, fillColor, null, 0);
    }

    private static void drawBox(PDPageContentStream stream, float x, float y, float width, float height, 
                                Color fillColor, Color strokeColor, float lineThickness) throws IOException {
        
        if (fillColor != null) {
            stream.setNonStrokingColor(fillColor);
            stream.addRect(x, y, width, height);
            stream.fill();
        }

        if (strokeColor != null && lineThickness > 0) {
            stream.setStrokingColor(strokeColor);
            stream.setLineWidth(lineThickness);
            stream.addRect(x, y, width, height);
            stream.stroke();
        }
    }
    
    private static void drawRoundedCard(PDPageContentStream stream, float x, float y, float width, float height) throws IOException {
        drawBox(stream, x, y, width, height, Color.WHITE);
        drawBox(stream, x, y, width, height, null, COLOR_BORDER, 1);
    }
    
    private static float drawModernTableRow(PDPageContentStream stream, float y, String description, double amount, 
                                          float rowHeight, float margin, float width, float col1X, float col2X, 
                                          boolean alternateColor) throws IOException {
        
        float rowY = y;
        float rowBottomY = y - rowHeight;
        
        if (alternateColor) {
            drawBox(stream, margin, rowBottomY, width, rowHeight, new Color(249, 250, 251));
        }
        
        drawBox(stream, margin, rowBottomY, width, rowHeight, null, COLOR_BORDER, 0.5f);
        
        float textY = rowBottomY + (rowHeight - 10) / 2;
        
        drawText(stream, FONT_REGULAR, 10, col1X, textY, description, COLOR_TEXT_PRIMARY);
        
        String amountStr = String.format("Rs. %.2f", amount);
        drawText(stream, FONT_BOLD, 10, col2X, textY, amountStr, COLOR_TEXT_PRIMARY);
        
        return rowBottomY;
    }

    public static void openPdf(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                throw new IOException("Desktop support is not available.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}