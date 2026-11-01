package com.gathin.tvhome;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import javax.imageio.ImageIO;

/** Host-only A/B checks: actual production scrim pixels, synthetic underlay and reference text/rim. */
public final class RenderAquaGalaxy {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) throw new IllegalArgumentException("output.png Lexend.ttf");
        AquaSurface[] surfaces = {
            new AquaSurface(0xFF102637, 0xFF091923, 0xFF126A76, 0xFF80DEDA),
            AquaSurface.galaxy(0xFF0B2030, 0xFF07121D, 0xFF123F50, 0xFF1A6875, 0xFF52C6C6, 0xFF4256A8, 0xFF80DEDA)
        };
        Font font = Font.createFont(Font.TRUETYPE_FONT, new File(args[1]));
        int w = AquaSurface.WIDTH, h = AquaSurface.HEIGHT;
        BufferedImage sheet = new BufferedImage(2 * w + 48, 3 * (h + 44) + 82, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(0x07121D)); g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE); g.setFont(font.deriveFont(16f));
        g.drawString("PRODUCTION PIXELS / SYNTHETIC INPUT - NOT A TV SCREENSHOT", 16, 26);
        String[] cases = {"A / Idle: no artwork", "B / Premixed artwork + ambient (synthetic)", "C / Focus rim + text reference (schematic)"};
        for (int col = 0; col < 2; col++) {
            long start = System.nanoTime();
            int[] scrim = surfaces[col].pixels(false);
            System.out.printf("%s texture preparation on HOST: %.2f ms (not TCL frame time)%n", col == 0 ? "v2.1" : "Galaxy", (System.nanoTime() - start) / 1e6);
            if (col == 0) System.out.println("legacy ARGB SHA-256: " + digest(scrim));
            int left = 16 + col * (w + 16);
            g.setColor(new Color(0x80DEDA)); g.setFont(font.deriveFont(17f));
            g.drawString(col == 0 ? "AQUA CINEMATIC v2.1" : "AQUA GALAXY v2.2", left, 59);
            for (int row = 0; row < 3; row++) {
                int top = 82 + row * (h + 44);
                g.setColor(Color.WHITE); g.setFont(font.deriveFont(12f)); g.drawString(cases[row], left, top - 7);
                BufferedImage input = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics2D a = input.createGraphics();
                if (row == 0) a.setColor(new Color(0x0B2030));
                else a.setPaint(new GradientPaint(0, 0, new Color(0xD84329), w, h, new Color(0x234A81)));
                a.fillRect(0, 0, w, h); a.dispose();
                int[] pixels = new int[w * h];
                for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) pixels[y * w + x] = AquaSurface.over(scrim[y * w + x], input.getRGB(x, y));
                BufferedImage shown = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                shown.setRGB(0, 0, w, h, pixels, 0, w); g.drawImage(shown, left, top, null);
                g.setColor(Color.WHITE); g.setFont(font.deriveFont(12f)); g.drawString("Xin chào, Ăng Hòn!", left + 24, top + 27);
                g.setFont(font.deriveFont(28f)); g.drawString(row == 2 ? "Ứng dụng đang chọn" : "Aqua Cinematic", left + 24, top + 156);
                if (row == 2) {
                    g.setColor(new Color(0x173346)); g.fillRoundRect(left + 26, top + 180, 116, 64, 12, 12);
                    g.setStroke(new BasicStroke(1.5f)); g.setColor(new Color(AquaSurface.rimColor(surfaces[col].accentColor(), 0xFFFFFFFF), true));
                    g.drawRoundRect(left + 26, top + 180, 116, 64, 12, 12);
                    g.setFont(font.deriveFont(13f)); g.setColor(Color.WHITE); g.drawString("App", left + 70, top + 219);
                }
            }
        }
        g.dispose();
        if (!ImageIO.write(sheet, "png", new File(args[0]))) throw new IllegalStateException("PNG writer missing");
        System.out.println(new File(args[0]).getCanonicalPath());
    }

    static String digest(int[] pixels) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        for (int p : pixels) { sha.update((byte)(p >>> 24)); sha.update((byte)(p >>> 16)); sha.update((byte)(p >>> 8)); sha.update((byte)p); }
        return java.util.HexFormat.of().formatHex(sha.digest());
    }
}
