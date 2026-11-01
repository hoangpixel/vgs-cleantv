package com.gathin.tvhome;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Host-only comparison: exact baked textures, schematic header/cards and synthetic artwork. */
public final class RenderAquaCosmicLowered {
    public static void main(String[] args)throws Exception {
        int w=AquaSurface.WIDTH,h=AquaSurface.HEIGHT;
        AquaSurface s=AquaSurface.galaxy(0xFF0B2030,0xFF07121D,0xFF123F50,0xFF1A6875,0xFF52C6C6,0xFF4256A8,0xFF80DEDA);
        int[] base=s.pixels(false),b=base.clone(),c=base.clone();
        CosmicDecorC.paint(b,false,s.accentColor());CosmicDecor.paint(c,false,s.accentColor());
        require(digest(base).equals("46e44756d7cf4c920c88f5558042f2be818dda077de58e66c9d9f545aa509b84"),"Galaxy changed");
        require(digest(b).equals("61b836ba35389065601e687afa875b84e43d4556d34437179cb6fff418201885"),"Shipped v2.3c changed");
        require(CosmicDecor.starCount()==22&&CosmicDecorC.starCount()==12&&CosmicDecor.R==24&&CosmicDecor.CY-CosmicDecorC.CY==24&&CosmicDecor.CX==CosmicDecorC.CX,"Geometry/count changed");
        int changed=0;
        for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(b[y*w+x]!=c[y*w+x]){
            require(x>=w/2&&(CosmicDecor.region(x,y)||CosmicDecorC.region(x,y)),"Changed pixel outside decor regions");changed++;
        }
        int[] again=base.clone();CosmicDecor.paint(again,false,s.accentColor());
        require(Arrays.equals(c,again),"Nondeterministic texture");
        int[] rtl=s.pixels(true);CosmicDecor.paint(rtl,true,s.accentColor());
        for(int y=0;y<h;y++)for(int x=0;x<w;x++)require(c[y*w+x]==rtl[y*w+w-1-x],"RTL mismatch");
        int light=c[(CosmicDecor.CY-8)*w+CosmicDecor.CX+12],dark=c[(CosmicDecor.CY+3)*w+CosmicDecor.CX-12];
        require(((light>>>16)&255)>((light>>>8)&255)&&((light>>>8)&255)>(light&255),"Light face is not warm");
        require((dark&255)>((dark>>>8)&255),"Dark face lost violet");
        int right=w-1-CosmicDecor.CX-CosmicDecor.R,top=CosmicDecor.CY-CosmicDecor.R;
        require(right>CosmicDecor.HALO&&top>CosmicDecor.HALO,"Planet/halo touches edge");
        require(top-CosmicDecor.HALO>48,"Planet halo intrudes into reference toolbar band");
        int[][] originalStars={{278,38},{331,22},{451,89},{442,138},{298,119},{360,91},{278,80},{317,70},{380,30},{466,45},{351,145},{465,157}};
        for(int[] star:originalStars)require(b[star[1]*w+star[0]]==c[star[1]*w+star[0]],"Original star changed");
        int altered=0;for(int i=0;i<base.length;i++)if(base[i]!=c[i])altered++;
        require(altered<w*h*.04,"Excessive decor footprint");
        System.out.printf("PASS: Galaxy/v2.3c hashes; 12 original star cores unchanged; 22 stars; 24 px down; %d changed pixels only in decor regions; deterministic/RTL; body 48 px; top/right body margins %d/%d px%n",changed,top,right);
        System.out.printf("Light/dark sampled RGB: #%06X / #%06X%n",light&0xFFFFFF,dark&0xFFFFFF);
        System.out.println("v2.3d ARGB SHA-256 "+digest(c));
        Font f=Font.createFont(Font.TRUETYPE_FONT,new File(args[1]));
        BufferedImage sheet=new BufferedImage(w*2+48,3*(h+44)+82,BufferedImage.TYPE_INT_RGB);Graphics2D g=sheet.createGraphics();
        g.setColor(new Color(0x07121D));g.fillRect(0,0,sheet.getWidth(),sheet.getHeight());
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);g.setFont(f.deriveFont(12f));g.drawString("BAKED TEXTURES / SCHEMATIC HEADER + TOOLBAR / SYNTHETIC ARTWORK - NOT TV CAPTURE",16,26);
        String[] rows={"A / Idle","B / Artwork reference","C / Focus + readability reference"};
        for(int col=0;col<2;col++){
            int left=16+col*(w+16);g.setColor(new Color(0x80DEDA));g.setFont(f.deriveFont(17f));
            g.drawString(col==0?"v2.3c / 12 STARS / Y=64":"v2.3d / 22 STARS / Y=88",left,59);
            for(int row=0;row<3;row++){
                int y0=82+row*(h+44);g.setColor(Color.WHITE);g.setFont(f.deriveFont(12f));g.drawString(rows[row],left,y0-7);
                BufferedImage input=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);Graphics2D a=input.createGraphics();
                if(row==0)a.setColor(new Color(0x0B2030));else a.setPaint(new GradientPaint(0,0,new Color(0xD84329),w,h,new Color(0x234A81)));
                a.fillRect(0,0,w,h);a.dispose();int[] tex=col==0?b:c,pixels=new int[w*h];
                for(int y=0;y<h;y++)for(int x=0;x<w;x++)pixels[y*w+x]=AquaSurface.over(tex[y*w+x],input.getRGB(x,y));
                BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);image.setRGB(0,0,w,h,pixels,0,w);g.drawImage(image,left,y0,null);
                drawToolbarReference(g,left,y0);
                g.setColor(Color.WHITE);g.setFont(f.deriveFont(34f));g.drawString("20:26",left+24,y0+44);
                g.setFont(f.deriveFont(10f));g.drawString("Chủ nhật · 01/11/2026",left+24,y0+64);g.drawString("Lịch âm · theo giờ TV",left+24,y0+78);
                g.setColor(new Color(255,255,255,209));g.drawString("Xin chào, Ăng Hòn! Chúc buổi tối thư giãn.",left+24,y0+98);
                g.setColor(Color.WHITE);g.setFont(f.deriveFont(25f));g.drawString(row==2?"Ứng dụng đang chọn":"Aqua Galaxy",left+24,y0+159);
                if(row==2){g.setColor(new Color(0x173346));g.fillRoundRect(left+26,y0+180,116,64,12,12);g.setStroke(new BasicStroke(1.5f));g.setColor(new Color(0x80DEDA));g.drawRoundRect(left+26,y0+180,116,64,12,12);g.setFont(f.deriveFont(13f));g.setColor(Color.WHITE);g.drawString("App",left+70,y0+219);}
            }
        }
        g.dispose();require(ImageIO.write(sheet,"png",new File(args[0])),"PNG writer missing");
        System.out.println(new File(args[0]).getCanonicalPath());
    }

    /** Schematic unchanged toolbar footprint for spacing review; not measured Android coordinates. */
    private static void drawToolbarReference(Graphics2D g,int left,int top) {
        g.setStroke(new BasicStroke(1f));
        for(int i=0;i<3;i++){
            int cx=left+368+i*40,cy=top+32;
            g.setColor(new Color(16,38,55,180));g.fillRoundRect(cx-16,cy-16,32,32,18,18);
            g.setColor(new Color(255,255,255,65));g.drawRoundRect(cx-16,cy-16,32,32,18,18);
            g.setColor(new Color(255,255,255,210));
            if(i==0){
                g.drawOval(cx-4,cy-4,8,8);
                for(int k=0;k<8;k++){
                    double a=k*Math.PI/4;
                    g.drawLine(cx+(int)Math.round(6*Math.cos(a)),cy+(int)Math.round(6*Math.sin(a)),cx+(int)Math.round(8*Math.cos(a)),cy+(int)Math.round(8*Math.sin(a)));
                }
            }else if(i==1){
                g.drawArc(cx-8,cy-6,16,12,35,110);g.drawArc(cx-5,cy-3,10,8,35,110);g.fillOval(cx-1,cy+3,3,3);
            }else{
                g.drawOval(cx-3,cy-7,6,6);g.drawArc(cx-7,cy+1,14,12,0,180);
            }
        }
    }
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private static String digest(int[] p)throws Exception{MessageDigest sha=MessageDigest.getInstance("SHA-256");for(int c:p){sha.update((byte)(c>>>24));sha.update((byte)(c>>>16));sha.update((byte)(c>>>8));sha.update((byte)c);}return java.util.HexFormat.of().formatHex(sha.digest());}
}
