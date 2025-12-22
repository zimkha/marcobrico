package com.mmd.marcobrico.service;


import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mmd.marcobrico.domain.Invoice;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class InvoicePdfGenerator {

    public byte[] generate(Invoice invoice) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        Font title = new Font(Font.HELVETICA, 20, Font.BOLD);
        Font normal = new Font(Font.HELVETICA, 11);

        document.add(new Paragraph("FACTURE", title));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Numéro : " + invoice.getInvoiceNumber(), normal));
        document.add(new Paragraph("Date : " + invoice.getIssuedAt(), normal));
        document.add(new Paragraph("Client : " + invoice.getSale().getUser().getUsername(), normal));

        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);

        table.addCell("Produit");
        table.addCell("Quantité");
        table.addCell("Prix");
        table.addCell("Total");

        invoice.getSale().getItems().forEach(i -> {
            table.addCell(i.getProduct().getName());
            table.addCell(String.valueOf(i.getQuantity()));
            table.addCell(i.getPrice().toString());
            table.addCell(i.getPrice().multiply(
                    BigDecimal.valueOf(i.getQuantity())).toString()
            );
        });

        document.add(table);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("TOTAL : " + invoice.getTotalAmount(), title));

        document.close();
        return out.toByteArray();
    }
}
