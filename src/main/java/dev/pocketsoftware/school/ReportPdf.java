package dev.pocketsoftware.school;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import dev.pocketsoftware.school.model.Grade;
import dev.pocketsoftware.school.model.Student;
import java.io.ByteArrayOutputStream;
import java.util.List;

/** Renders one student's report card as a PDF, in memory. */
public final class ReportPdf {

    /** Builds the PDF bytes for a student's term report. */
    public static byte[] render(Student s, String term, List<Grade> grades) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font bold = new Font(Font.HELVETICA, 11, Font.BOLD);

            Paragraph school = new Paragraph("PocketSoftware School", title);
            school.setAlignment(Element.ALIGN_CENTER);
            school.setSpacingAfter(4);
            doc.add(school);

            Paragraph termLine = new Paragraph("Report Card — " + term, normal);
            termLine.setAlignment(Element.ALIGN_CENTER);
            termLine.setSpacingAfter(14);
            doc.add(termLine);

            doc.add(new Paragraph("Student: " + s.fullName(), bold));
            doc.add(new Paragraph("Admission No: " + s.admissionNo(), normal));
            doc.add(new Paragraph("Class: " + (s.classId().isBlank() ? "—" : s.classId()), normal));
            doc.add(new Paragraph(" ", normal));
            doc.add(new Paragraph(" ", normal));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[] {50, 25, 25});
            table.addCell(new Phrase("Subject", bold));
            table.addCell(new Phrase("Score", bold));
            table.addCell(new Phrase("Grade", bold));
            for (Grade g : grades) {
                table.addCell(new Phrase(g.subject(), normal));
                table.addCell(new Phrase(String.valueOf(g.score()), normal));
                table.addCell(new Phrase(letter(g.score()), normal));
            }
            doc.add(table);

            double avg = grades.stream().mapToInt(Grade::score).average().orElse(0);
            doc.add(new Paragraph(" ", normal));
            doc.add(new Paragraph("Average: " + String.format("%.1f", avg) + "  (" + letter(avg) + ")", bold));

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Report PDF failed: " + e.getMessage(), e);
        }
    }

    /** Score to letter grade. */
    static String letter(double score) {
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }

    private ReportPdf() {
    }
}
