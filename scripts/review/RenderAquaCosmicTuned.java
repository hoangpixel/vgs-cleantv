package com.gathin.tvhome;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Review only. Exact baked pixels, schematic unchanged v2.3 header/card, synthetic underlay. */
public final class RenderAquaCosmicTuned {
    public static void main(String[] args)throws Exception {
        int w=AquaSurface.WIDTH,h=AquaSurface.HEIGHT;
        AquaSurface s=AquaSurface.galaxy(0xFF0B2030,0xFF07121D,0xFF123F50,0xFF1A6875,0xFF52C6C6,0xFF4256A8,0xFF80DEDA);
        int[] base=s.pixels(false),old=base.clone(),tuned=base.clone();
        CosmicDecorOriginal.paint(old,false,s.accentColor());CosmicDecor.paint(tuned,false,s.accentColor());
        if(!digest(base).equals("46e44756d7cf4c920c88f5558042f2be818dda077de58e66c9d9f545aa509b84"))throw new IllegalStateException("Galaxy gradient changed");
        if(!digest(old).equals("fc37adc2cc58ceaac569ca4a3afd96e7b02bc1ce3bb7a8f3e9fba5a7dc428471"))throw new IllegalStateException("Original v2.3 pixels changed");
        int changed=0;
        for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(base[y*w+x]!=tuned[y*w+x]){
            if(x<w/2||!CosmicDecor.region(x,y))throw new IllegalStateException("Decor outside safe region");changed++;
        }
        int[] again=base.clone();CosmicDecor.paint(again,false,s.accentColor());
        if(!Arrays.equals(tuned,again)||changed>=w*h*.03)throw new IllegalStateException("Unstable/excessive decoration");
        int[] mirrored=s.pixels(true);CosmicDecor.paint(mirrored,true,s.accentColor());
        for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(tuned[y*w+x]!=mirrored[y*w+w-1-x])throw new IllegalStateException("RTL mismatch");
        System.out.printf("PASS: Galaxy/v2.3 hashes unchanged; %d stars; diameter %d -> %d px; changed %d/%d pixels (%.2f%%), right-side only; deterministic/RTL%n",CosmicDecor.starCount(),20,2*CosmicDecor.R,changed,w*h,changed*100.0/(w*h));
        System.out.println("Candidate ARGB SHA-256 "+digest(tuned));
        Font f=Font.createFont(Font.TRUETYPE_FONT,new File(args[1]));
        BufferedImage sheet=new BufferedImage(w*2+48,3*(h+44)+82,BufferedImage.TYPE_INT_RGB);Graphics2D g=sheet.createGraphics();
        g.setColor(new Color(0x07121D));g.fillRect(0,0,sheet.getWidth(),sheet.getHeight());
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);g.setFont(f.deriveFont(14f));g.drawString("BAKED TEXTURE CANDIDATE / SCHEMATIC LAYOUT - NOT A TV SCREENSHOT",16,26);
        String[] rows={"A / Idle","B / Synthetic artwork + ambient","C / Focus + readability reference"};
        for(int col=0;col<2;col++){
            int left=16+col*(w+16);g.setColor(new Color(0x80DEDA));g.setFont(f.deriveFont(17f));
            g.drawString(col==0?"CURRENT v2.3 / 5 STARS":"TUNED v2.3b / 12 STARS",left,59);
            for(int row=0;row<3;row++){
                int top=82+row*(h+44);g.setColor(Color.WHITE);g.setFont(f.deriveFont(12f));g.drawString(rows[row],left,top-7);
                BufferedImage input=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);Graphics2D a=input.createGraphics();
                if(row==0)a.setColor(new Color(0x0B2030));else a.setPaint(new GradientPaint(0,0,new Color(0xD84329),w,h,new Color(0x234A81)));
                a.fillRect(0,0,w,h);a.dispose();int[] tex=col==0?old:tuned,pixels=new int[w*h];
                for(int y=0;y<h;y++)for(int x=0;x<w;x++)pixels[y*w+x]=AquaSurface.over(tex[y*w+x],input.getRGB(x,y));
                BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);image.setRGB(0,0,w,h,pixels,0,w);g.drawImage(image,left,top,null);
                g.setColor(Color.WHITE);g.setFont(f.deriveFont(34f));g.drawString("20:26",left+24,top+44);
                g.setFont(f.deriveFont(10f));g.drawString("Chủ nhật · 01/11/2026",left+24,top+64);g.drawString("Lịch âm · theo giờ TV",left+24,top+78);
                g.setColor(new Color(255,255,255,209));g.drawString("Xin chào, Ăng Hòn! Chúc buổi tối thư giãn.",left+24,top+98);
                g.setColor(Color.WHITE);g.setFont(f.deriveFont(25f));g.drawString(row==2?"Ứng dụng đang chọn":"Aqua Galaxy",left+24,top+159);
                if(row==2){g.setColor(new Color(0x173346));g.fillRoundRect(left+26,top+180,116,64,12,12);g.setStroke(new BasicStroke(1.5f));g.setColor(new Color(0x80DEDA));g.drawRoundRect(left+26,top+180,116,64,12,12);g.setFont(f.deriveFont(13f));g.setColor(Color.WHITE);g.drawString("App",left+70,top+219);}
            }
        }
        g.dispose();if(!ImageIO.write(sheet,"png",new File(args[0])))throw new IllegalStateException("PNG writer missing");
        System.out.println(new File(args[0]).getCanonicalPath());
    }
    private static String digest(int[] p)throws Exception{MessageDigest sha=MessageDigest.getInstance("SHA-256");for(int c:p){sha.update((byte)(c>>>24));sha.update((byte)(c>>>16));sha.update((byte)(c>>>8));sha.update((byte)c);}return java.util.HexFormat.of().formatHex(sha.digest());}
}
