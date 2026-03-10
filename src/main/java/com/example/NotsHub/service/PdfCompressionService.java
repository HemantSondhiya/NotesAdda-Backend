package com.example.NotsHub.service;

import com.example.NotsHub.exceptions.APIException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class PdfCompressionService {

    private static final int MIN_WIDTH = 200;
    private static final int MIN_HEIGHT = 200;

    public byte[] compress(byte[] originalPdfBytes) {
        return compress(originalPdfBytes, 0.4f);
    }

    public byte[] compress(byte[] originalPdfBytes, float imageQuality) {
        if (originalPdfBytes == null || originalPdfBytes.length == 0) {
            throw new APIException("PDF file is required");
        }

        if (imageQuality <= 0 || imageQuality > 1) {
            throw new APIException("imageQuality must be between 0 and 1");
        }

        try (PDDocument document = Loader.loadPDF(originalPdfBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Set<COSBase> visited = new HashSet<>();

            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources != null) {
                    recompressResources(document, resources, imageQuality, visited);
                }
            }

            document.save(outputStream);
            byte[] compressed = outputStream.toByteArray();

            return (compressed.length > 0 && compressed.length < originalPdfBytes.length)
                    ? compressed
                    : originalPdfBytes;

        } catch (Exception ex) {
            throw new APIException("Failed to compress PDF", ex);
        }
    }

    private void recompressResources(PDDocument document,
                                     PDResources resources,
                                     float imageQuality,
                                     Set<COSBase> visited) throws IOException {

        COSBase resourcesBase = resources.getCOSObject();
        if (!visited.add(resourcesBase)) {
            return;
        }

        for (org.apache.pdfbox.cos.COSName xObjectName : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(xObjectName);

            if (xObject instanceof PDImageXObject imageXObject) {
                recompressImage(document, resources, xObjectName, imageXObject, imageQuality);
            } else if (xObject instanceof PDFormXObject formXObject) {
                PDResources nestedResources = formXObject.getResources();
                if (nestedResources != null) {
                    recompressResources(document, nestedResources, imageQuality, visited);
                }
            }
        }
    }

    private void recompressImage(PDDocument document,
                                 PDResources resources,
                                 org.apache.pdfbox.cos.COSName xObjectName,
                                 PDImageXObject imageXObject,
                                 float imageQuality) throws IOException {

        BufferedImage bufferedImage = imageXObject.getImage();
        if (bufferedImage == null) {
            return;
        }

        if (bufferedImage.getWidth() < MIN_WIDTH || bufferedImage.getHeight() < MIN_HEIGHT) {
            return;
        }

        PDImageXObject compressedImage =
                JPEGFactory.createFromImage(document, bufferedImage, imageQuality);

        resources.put(xObjectName, compressedImage);
    }
}