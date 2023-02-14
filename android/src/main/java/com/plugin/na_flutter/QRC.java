package com.plugin.na_flutter;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import com.evolute.qrimage.supp.BarcodeFormat;
import com.evolute.qrimage.supp.EncodeHintType;
import com.evolute.qrimage.supp.MultiFormatWriter;
import com.evolute.qrimage.supp.common.BitMatrix;
import com.evolute.textimage.TextGenerator;
import com.evolute.textimage.TextGenerator.ImageWidth;
import java.util.EnumMap;
import java.util.Map;

public final class QRC {
    private static final int WHITE = -1;
    private static final int BLACK = -16777216;

    public QRC() {
    }

    public static Bitmap bmpDrawQRCode(String sData) throws IllegalArgumentException, Exception {
        return bmpDrawQRCode(ImageWidth.Inch_2, sData);
    }

    public static Bitmap bmpDrawQRCode(String sData, int iHeight) throws IllegalArgumentException, Exception {
        return bmpDrawQRCode(ImageWidth.Inch_2, sData, iHeight);
    }

    public static Bitmap bmpDrawQRCode(TextGenerator.ImageWidth eWidth, String sData) throws IllegalArgumentException, Exception {
        return bmpDrawQRCode(eWidth, sData, 350);
    }

    public static Bitmap bmpDrawQRCode(TextGenerator.ImageWidth eWidth, String sData, int iHeight) throws IllegalArgumentException, Exception {
        try {
            int iWidth = 384;
            if (eWidth == ImageWidth.Inch_3) {
                iWidth = 576;
            }

            if (iHeight >= 100 && iHeight <= 350) {
                if (sData != null && sData.length() != 0 && sData.length() <= 80) {
                    Map<EncodeHintType, Object> hints = null;
                    String encoding = guessAppropriateEncoding(sData);
                    if (encoding != null) {
                        hints = new EnumMap(EncodeHintType.class);
                        hints.put(EncodeHintType.CHARACTER_SET, encoding);
                    }

                    MultiFormatWriter writer = new MultiFormatWriter();
                    BitMatrix result = writer.encode(sData, BarcodeFormat.QR_CODE, iWidth, iHeight, hints);
                    int width = result.getWidth();
                    int height = result.getHeight();
                    int[] pixels = new int[width * height];

                    for(int y = 0; y < height; ++y) {
                        int offset = y * width;

                        for(int x = 0; x < width; ++x) {
                            pixels[offset + x] = result.get(x, y) ? -16777216 : -1;
                        }
                    }

                    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
                    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                    return bitmap;
                } else {
                    throw new IllegalArgumentException(" Barcode input sData is invalid ");
                }
            } else {
                throw new IllegalArgumentException(" Barcode input iHeight out of Range 100-350 ");
            }
        } catch (Exception var14) {
            throw var14;
        }
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        for(int i = 0; i < contents.length(); ++i) {
            if (contents.charAt(i) > 255) {
                return "UTF-8";
            }
        }

        return null;
    }
}
