package com.gathin.tvhome;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import javax.imageio.ImageIO;

/** Exact scrim pixels; schematic header/card and synthetic underlays. Not an Android screenshot. */
public final class RenderAquaCosmic {
    static AquaSurface galaxy() { return AquaSurface.galaxy(0xFF0B2030,0xFF07121D,0xFF123F50,0xFF1A6875,0xFF52C6C6,0xFF4256A8,0xFF80DEDA); }
    public static void main(String[] args) throws Exception {
        AquaSurface[] surfaces={galaxy(),galaxy().withCosmicDecor()};
        Font font=Font.createFont(Font.TRUETYPE_FONT,new File(args[1]));
        int w=AquaSurface.WIDTH,h=AquaSurface.HEIGHT;
        BufferedImage sheet=new BufferedImage(w*2+48,3*(h+44)+82,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=sheet.createGraphics(); g.setColor(new Color(0x07121D));g.fillRect(0,0,sheet.getWidth(),sheet.getHeight());
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);g.setFont(font.deriveFont(15f));
        g.drawString("PRODUCTION TEXTURE / SCHEMATIC LAYOUT / FIXTURE TIME - NOT A TV SCREENSHOT",16,26);
        String[] cases={"A / Idle", "B / Synthetic artwork + ambient", "C / Focus + readability reference"};
        for(int col=0;col<2;col++) {
            long start=System.nanoTime();int[] scrim=surfaces[col].pixels(false);
            System.out.printf("v2.%d host preparation %.2f ms; ARGB SHA-256 %s%n",col+2,(System.nanoTime()-start)/1e6,digest(scrim));
            int left=16+col*(w+16);g.setColor(new Color(0x80DEDA));g.setFont(font.deriveFont(17f));
            g.drawString(col==0?"GALAXY v2.2":"GALAXY v2.3 / STATIC COSMIC",left,59);
            for(int row=0;row<3;row++) {
                int top=82+row*(h+44);g.setColor(Color.WHITE);g.setFont(font.deriveFont(12f));g.drawString(cases[row],left,top-7);
                BufferedImage under=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);Graphics2D a=under.createGraphics();
                if(row==0)a.setColor(new Color(0x0B2030));else a.setPaint(new GradientPaint(0,0,new Color(0xD84329),w,h,new Color(0x234A81)));
                a.fillRect(0,0,w,h);a.dispose();int[] pixels=new int[w*h];
                for(int y=0;y<h;y++)for(int x=0;x<w;x++)pixels[y*w+x]=AquaSurface.over(scrim[y*w+x],under.getRGB(x,y));
                BufferedImage shown=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);shown.setRGB(0,0,w,h,pixels,0,w);g.drawImage(shown,left,top,null);
                drawHeader(g,font,left,top,col==1);
                g.setColor(Color.WHITE);g.setFont(font.deriveFont(25f));g.drawString(row==2?"Ứng dụng đang chọn":"Aqua Galaxy",left+24,top+159);
                if(row==2){g.setColor(new Color(0x173346));g.fillRoundRect(left+26,top+180,116,64,12,12);
                    g.setStroke(new BasicStroke(1.5f));g.setColor(new Color(0x80DEDA));g.drawRoundRect(left+26,top+180,116,64,12,12);
                    g.setFont(font.deriveFont(13f));g.setColor(Color.WHITE);g.drawString("App",left+70,top+219);}
            }
        }
        g.dispose();if(!ImageIO.write(sheet,"png",new File(args[0])))throw new IllegalStateException("PNG writer missing");
        System.out.println(new File(args[0]).getCanonicalPath());
    }
    private static void drawHeader(Graphics2D g,Font f,int x,int y,boolean cosmic) {
        String greeting="Xin chào, Ăng Hòn! Chúc buổi tối thư giãn.";
        g.setColor(Color.WHITE);
        if(!cosmic){g.setFont(f.deriveFont(11f));g.drawString(greeting,x+24,y+24);}
        g.setFont(f.deriveFont(34f));g.drawString("20:26",x+24,y+(cosmic?44:62));
        g.setFont(f.deriveFont(10f));int dateX=x+(cosmic?24:137),dateY=y+(cosmic?64:45);
        g.drawString("Chủ nhật · 01/11/2026",dateX,dateY);g.drawString("Lịch âm · theo giờ TV",dateX,dateY+14);
        if(cosmic){g.setColor(new Color(255,255,255,209));g.setFont(f.deriveFont(10f));g.drawString(greeting,x+24,y+98);}
    }
    static String digest(int[] pixels)throws Exception {
        MessageDigest sha=MessageDigest.getInstance("SHA-256");for(int p:pixels){sha.update((byte)(p>>>24));sha.update((byte)(p>>>16));sha.update((byte)(p>>>8));sha.update((byte)p);}
        return java.util.HexFormat.of().formatHex(sha.digest());
    }
}
