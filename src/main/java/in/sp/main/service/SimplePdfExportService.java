package in.sp.main.service;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure-Java minimal PDF 1.4 generator for casting analytics reports without external dependencies.
 */
@Service
public class SimplePdfExportService {

    public record PdfSection(String heading, List<String> lines) {}

    public byte[] buildReportPdf(String reportTitle, String subtitle, List<PdfSection> sections) {
        List<StringBuilder> pages = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        pages.add(cur);

        float y = 780f;
        y = drawText(cur, "F2", 16, 48, y, sanitize(reportTitle));
        y = drawText(cur, "F1", 10, 48, y - 4, sanitize(subtitle));
        y -= 16;

        for (PdfSection sec : sections) {
            if (y < 90f) {
                cur = new StringBuilder();
                pages.add(cur);
                y = 780f;
            }
            y = drawText(cur, "F2", 12, 48, y, sanitize(sec.heading()));
            y -= 4;
            for (String line : sec.lines()) {
                for (String wrapped : wrapLine(sanitize(line), 92)) {
                    if (y < 60f) {
                        cur = new StringBuilder();
                        pages.add(cur);
                        y = 780f;
                    }
                    y = drawText(cur, "F1", 9.5f, 54, y, wrapped);
                }
            }
            y -= 10;
        }

        return assemblePdf(pages);
    }

    private float drawText(StringBuilder sb, String fontKey, float size, float x, float y, String text) {
        sb.append("BT /").append(fontKey).append(" ").append(size).append(" Tf ")
          .append(x).append(" ").append(y).append(" Td (")
          .append(escapePdf(text)).append(") Tj ET\n");
        return y - (size + 4.5f);
    }

    private List<String> wrapLine(String text, int maxLen) {
        if (text.length() <= maxLen) return List.of(text);
        List<String> out = new ArrayList<>();
        int idx = 0;
        while (idx < text.length()) {
            int end = Math.min(text.length(), idx + maxLen);
            if (end < text.length()) {
                int space = text.lastIndexOf(' ', end);
                if (space > idx + 20) end = space;
            }
            out.add(text.substring(idx, end).trim());
            idx = end;
            while (idx < text.length() && text.charAt(idx) == ' ') idx++;
        }
        return out;
    }

    private byte[] assemblePdf(List<StringBuilder> pageStreams) {
        int pageCount = pageStreams.size();
        int totalObjs = 4 + pageCount * 2;
        List<byte[]> objects = new ArrayList<>(totalObjs);

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pageCount; i++) {
            kids.append(5 + i).append(" 0 R ");
        }

        objects.add("<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.US_ASCII));
        objects.add(("<< /Type /Pages /Count " + pageCount + " /Kids [ " + kids + "] >>").getBytes(StandardCharsets.US_ASCII));
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".getBytes(StandardCharsets.US_ASCII));
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>".getBytes(StandardCharsets.US_ASCII));

        for (int i = 0; i < pageCount; i++) {
            int contentObjId = 5 + pageCount + i;
            String pageDict = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> "
                    + "/Contents " + contentObjId + " 0 R >>";
            objects.add(pageDict.getBytes(StandardCharsets.US_ASCII));
        }
        for (StringBuilder stream : pageStreams) {
            byte[] streamBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);
            byte[] prefix = ("<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.US_ASCII);
            byte[] suffix = "\nendstream".getBytes(StandardCharsets.US_ASCII);
            ByteArrayOutputStream objStream = new ByteArrayOutputStream();
            objStream.writeBytes(prefix);
            objStream.writeBytes(streamBytes);
            objStream.writeBytes(suffix);
            objects.add(objStream.toByteArray());
        }

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        pdf.writeBytes("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.size());
            pdf.writeBytes(((i + 1) + " 0 obj\n").getBytes(StandardCharsets.US_ASCII));
            pdf.writeBytes(objects.get(i));
            pdf.writeBytes("\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        }

        int xrefPos = pdf.size();
        pdf.writeBytes(("xref\n0 " + (objects.size() + 1) + "\n").getBytes(StandardCharsets.US_ASCII));
        pdf.writeBytes("0000000000 65535 f \n".getBytes(StandardCharsets.US_ASCII));
        for (int i = 1; i <= objects.size(); i++) {
            pdf.writeBytes(String.format("%010d 00000 n \n", offsets.get(i)).getBytes(StandardCharsets.US_ASCII));
        }
        pdf.writeBytes(("trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefPos + "\n%%EOF\n")
                .getBytes(StandardCharsets.US_ASCII));
        return pdf.toByteArray();
    }

    private String sanitize(String input) {
        if (input == null) return "";
        String cleaned = input.replace("₹", "Rs. ")
                .replace("—", "-")
                .replace("–", "-")
                .replace("•", "*")
                .replace("’", "'")
                .replace("‘", "'")
                .replace("“", "\"")
                .replace("”", "\"");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c >= 32 && c <= 126) {
                out.append(c);
            } else if (c == '\t') {
                out.append(' ');
            }
        }
        return out.toString();
    }

    private String escapePdf(String s) {
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
