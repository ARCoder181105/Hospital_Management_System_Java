package util;

import model.Bill;
import model.Patient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class PdfGenerator {

    // Helper method to draw a line of text
    private static void drawText(PDPageContentStream contentStream, float x, float y, String text,
            PDType1Font font, int fontSize) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    // Helper method to draw a horizontal line
    private static void drawLine(PDPageContentStream contentStream, float xStart, float y, float xEnd) throws IOException {
        contentStream.moveTo(xStart, y);
        contentStream.lineTo(xEnd, y);
        contentStream.stroke();
    }

    public static File generateBillPdf(Bill bill, Patient patient) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float y = 700;
        float margin = 50;
        float width = page.getMediaBox().getWidth() - 2 * margin;

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            // === Header ===
            drawText(contentStream, margin, y, "HOSPITAL MANAGEMENT SYSTEM", fontBold, 18);
            y -= 25;
            drawText(contentStream, margin, y, "PATIENT INVOICE / DISCHARGE SUMMARY", fontRegular, 14);
            y -= 20;
            drawLine(contentStream, margin, y, margin + width);
            y -= 30;

            // === Patient & Bill Details ===
            drawText(contentStream, margin, y, "Patient Name:", fontBold, 12);
            drawText(contentStream, margin + 150, y, patient.getName(), fontRegular, 12);
            drawText(contentStream, margin + 300, y, "Bill ID:", fontBold, 12);
            drawText(contentStream, margin + 400, y, String.valueOf(bill.getBillId()), fontRegular, 12);
            y -= 20;

            drawText(contentStream, margin, y, "Patient ID:", fontBold, 12);
            drawText(contentStream, margin + 150, y, String.valueOf(patient.getPatientId()), fontRegular, 12);
            drawText(contentStream, margin + 300, y, "Bill Date:", fontBold, 12);
            drawText(contentStream, margin + 400, y, new SimpleDateFormat("yyyy-MM-dd").format(bill.getBillDate()), fontRegular, 12);
            y -= 20;

            drawText(contentStream, margin, y, "Admitted Date:", fontBold, 12);
            drawText(contentStream, margin + 150, y, new SimpleDateFormat("yyyy-MM-dd").format(patient.getAdmittedDate()), fontRegular, 12);
            y -= 20;

            drawText(contentStream, margin, y, "Illness:", fontBold, 12);
            drawText(contentStream, margin + 150, y, patient.getIllness(), fontRegular, 12);
            y -= 30;

            // === Itemized Charges ===
            drawLine(contentStream, margin, y, margin + width);
            y -= 20;
            drawText(contentStream, margin, y, "Description", fontBold, 12);
            drawText(contentStream, margin + 400, y, "Amount (INR)", fontBold, 12);
            y -= 10;
            drawLine(contentStream, margin, y, margin + width);
            y -= 20;

            drawText(contentStream, margin, y, "Bed Charges", fontRegular, 12);
            drawText(contentStream, margin + 400, y, String.format("Rs. %.2f", bill.getBedCharge()), fontRegular, 12);
            y -= 20;

            drawText(contentStream, margin, y, "Base Service Charges", fontRegular, 12);
            drawText(contentStream, margin + 400, y, String.format("Rs. %.2f", bill.getServiceCharge()), fontRegular, 12);
            y -= 20;

            drawText(contentStream, margin, y, "Doctor Consultation Fee", fontRegular, 12);
            drawText(contentStream, margin + 400, y, String.format("Rs. %.2f", bill.getDoctorFee()), fontRegular, 12);
            y -= 30;

            // === Total ===
            drawLine(contentStream, margin, y, margin + width);
            y -= 25;

            drawText(contentStream, margin + 300, y, "TOTAL AMOUNT DUE:", fontBold, 14);
            drawText(contentStream, margin + 420, y, String.format("Rs. %.2f", bill.getTotal()), fontBold, 14);
            y -= 50;

            // === Footer ===
            drawText(contentStream, margin, y, "Thank you for choosing our services.", fontRegular, 12);
            y -= 15;
            drawText(contentStream, margin, y, "We wish you a speedy recovery.", fontRegular, 12);
        }

        // Save the file
        String fileName = "Bill-" + bill.getBillId() + "-" + patient.getName().replaceAll("\\s+", "_") + ".pdf";
        File file = new File(fileName);
        document.save(file);
        document.close();
        
        return file;
    }

    // Helper to open the saved file
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