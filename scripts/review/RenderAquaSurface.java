package com.gathin.tvhome;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Host-only visual check using the exact production pixels. Never packaged in either APK. */
public final class RenderAquaSurface {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) throw new IllegalArgumentException("output.png [Lexend.ttf]");
        AquaSurface surface = new AquaSurface(0xFF102637, 0xFF091923, 0xFF126A76, 0xFF80DEDA);
        long started = System.nanoTime();
        int[] tint = surface.pixels(false);
        double elapsedMs = (System.nanoTime() - started) / 1_000_000.0;
        int width = AquaSurface.WIDTH, height = AquaSurface.HEIGHT;
        BufferedImage sheet = new BufferedImage(width * 3 + 64, height * 2 + 150, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(new Color(0x091923));
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Font font = args.length > 1 ? Font.createFont(Font.TRUETYPE_FONT, new File(args[1]))
                : new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        g.setFont(font.deriveFont(18f));
        g.setColor(Color.WHITE);
        g.drawString("AQUA V2.1 / PIXEL COMPOSITION CHECK - NOT A TV SCREENSHOT", 16, 29);
        int[] colours = {0xFFE04020, 0xFF3465AD, 0xFFFFFFFF};
        String[] labels = {"Warm artwork", "Cool artwork", "Bright artwork"};
        for (int column = 0; column < colours.length; column++) {
            BufferedImage artwork = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D a = artwork.createGraphics();
            a.setPaint(new GradientPaint(0, 0, new Color(colours[column], true), width, height,
                    new Color(column == 2 ? 0xFFCCCCCC : 0xFF281C28, true)));
            a.fillRect(0, 0, width, height);
            a.dispose();
            int[] composite = new int[tint.length];
            for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
                composite[y * width + x] = AquaSurface.over(tint[y * width + x], artwork.getRGB(x, y));
            }
            BufferedImage shown = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            shown.setRGB(0, 0, width, height, composite, 0, width);
            int left = 16 + column * (width + 16);
            g.setColor(Color.WHITE);
            g.setFont(font.deriveFont(14f));
            g.drawString(labels[column] + " (synthetic input)", left, 64);
            g.drawImage(artwork, left, 76, null);
            g.drawString("Same input + production Aqua scrim", left, height + 105);
            g.drawImage(shown, left, height + 117, null);
            g.setFont(font.deriveFont(13f));
            g.drawString("Xin chào, Ăng Hòn!", left + 24, height + 149);
            g.setFont(font.deriveFont(27f));
            g.drawString("Navy / Aqua", left + 24, height + 300);
        }
        g.dispose();
        File output = new File(args[0]);
        if (!ImageIO.write(sheet, "png", output)) throw new IllegalStateException("PNG writer missing");
        System.out.println("Production texture: " + tint.length + " pixels / " + (tint.length * 4) + " bytes");
        System.out.printf("Host preparation %.2f ms (not a TCL frame-time measurement)%n", elapsedMs);
        System.out.println(output.getCanonicalPath());
    }
}
