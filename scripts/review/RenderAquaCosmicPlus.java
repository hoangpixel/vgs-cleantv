package com.gathin.tvhome;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Desktop review only: baked texture pixels, schematic LTR/RTL UI, synthetic artwork. */
public final class RenderAquaCosmicPlus {
    private static final int W=AquaSurface.WIDTH,H=AquaSurface.HEIGHT,TOOLBAR_BOTTOM=48;
    // Inclusive occupied boxes of the schematic fixture, not measured TV bounds.
    private static final int[][] UI={{24,12,140,47},{24,52,240,83},{24,87,270,104},
        {24,130,310,166},{26,180,142,244},{352,16,464,48}};

    public static void main(String[] args)throws Exception {
        AquaSurface s=AquaSurface.galaxy(0xFF0B2030,0xFF07121D,0xFF123F50,0xFF1A6875,0xFF52C6C6,0xFF4256A8,0xFF80DEDA);
        int[] base=s.pixels(false),d=base.clone(),e=base.clone();
        CosmicDecorD.paint(d,false,s.accentColor());CosmicDecor.paint(e,false,s.accentColor());
        require(digest(base).equals("46e44756d7cf4c920c88f5558042f2be818dda077de58e66c9d9f545aa509b84"),"Galaxy changed");
        require(digest(d).equals("e84b9e49addea92fc04d75b0392833b42618f18e52138c1f41a0bafaeb9f6481"),"v2.3d baseline changed");
        require(CosmicDecorD.R==24&&CosmicDecorD.CY==88&&CosmicDecor.R==29&&CosmicDecor.CX==413&&CosmicDecor.CY==104&&CosmicDecor.HALO==8,"Geometry changed");
        int[][] oldStars=stars(CosmicDecorD.class),newStars=stars(CosmicDecor.class);
        require(oldStars.length==22&&newStars.length==28,"Wrong star count");
        for(int i=0;i<oldStars.length;i++){
            require(Arrays.equals(oldStars[i],newStars[i]),"v2.3d star definition changed");
            int x=oldStars[i][0],y=oldStars[i][1];
            for(int dy=-2;dy<=2;dy++)for(int dx=-2;dx<=2;dx++)require(d[(y+dy)*W+x+dx]==e[(y+dy)*W+x+dx],"Original star pixels changed");
        }
        int bright=0;for(int[] star:newStars){if(star[2]>=100)bright++;}
        require(bright==2,"Unexpected extra bright stars");
        for(int i=22;i<28;i++){
            int[] star=newStars[i];require(star[0]>W/2&&star[2]<=54,"New star too bright or on the text side");
            require(!intersectsUI(star[0]-2,star[1]-2,star[0]+2,star[1]+2,false),"New star intersects schematic UI");
            require(!intersectsUI(W-1-star[0]-2,star[1]-2,W-1-star[0]+2,star[1]+2,true),"RTL new star intersects UI");
            float dx=star[0]-CosmicDecor.CX,dy=star[1]-CosmicDecor.CY;
            require(Math.sqrt(dx*dx+dy*dy)>CosmicDecor.R+CosmicDecor.HALO+2,"New star on planet halo");
        }
        int extent=CosmicDecor.R+CosmicDecor.HALO;
        require(!intersectsUI(CosmicDecor.CX-extent,CosmicDecor.CY-extent,CosmicDecor.CX+extent,CosmicDecor.CY+extent,false),"Planet halo intersects UI");
        int mirroredX=W-1-CosmicDecor.CX;
        require(!intersectsUI(mirroredX-extent,CosmicDecor.CY-extent,mirroredX+extent,CosmicDecor.CY+extent,true),"RTL planet halo intersects UI");
        int changed=0,decor=0;
        for(int y=0;y<H;y++)for(int x=0;x<W;x++){
            if(d[y*W+x]!=e[y*W+x]){require(x>=W/2&&(CosmicDecor.region(x,y)||CosmicDecorD.region(x,y)),"Change outside decor regions");changed++;}
            if(base[y*W+x]!=e[y*W+x])decor++;
        }
        require(decor<W*H*.04,"Excessive decor footprint");
        int[] again=base.clone();CosmicDecor.paint(again,false,s.accentColor());require(Arrays.equals(e,again),"Nondeterministic texture");
        int[] rtl=s.pixels(true);CosmicDecor.paint(rtl,true,s.accentColor());
        for(int y=0;y<H;y++)for(int x=0;x<W;x++)require(e[y*W+x]==rtl[y*W+W-1-x],"RTL pixel mismatch");
        int light=e[(CosmicDecor.CY-10)*W+CosmicDecor.CX+15],dark=e[(CosmicDecor.CY+4)*W+CosmicDecor.CX-15];
        require(((light>>>16)&255)>((light>>>8)&255)&&((light>>>8)&255)>(light&255),"Light face not champagne");
        require((dark&255)>((dark>>>8)&255),"Dark face not smoky violet");
        int bodyGap=CosmicDecor.CY-CosmicDecor.R-TOOLBAR_BOTTOM,haloGap=bodyGap-CosmicDecor.HALO;
        System.out.printf("PASS: Galaxy/v2.3d hashes; all 22 star definitions/pixel patches unchanged; 28 stars (2 accents); diameter 58; centre (413,104); toolbar body/halo gaps %d/%d px; LTR/RTL fixture collision checks; determinism/RTL pixels%n",bodyGap,haloGap);
        System.out.printf("Changed vs v2.3d: %d; decor vs Galaxy: %d/%d (%.2f%%); light/dark #%06X / #%06X%n",changed,decor,W*H,decor*100.0/(W*H),light&0xFFFFFF,dark&0xFFFFFF);
        System.out.println("v2.3e ARGB SHA-256 "+digest(e));
        Font f=Font.createFont(Font.TRUETYPE_FONT,new File(args[1]));
        render(args[0],f,d,e,false);
        String rtlPath=args[0].replaceFirst("(?i)\\.png$","-rtl.png");require(!rtlPath.equals(args[0]),"PNG extension required");
        render(rtlPath,f,d,e,true);
    }

    private static void render(String output,Font f,int[] d,int[] e,boolean rtl)throws Exception {
        BufferedImage sheet=new BufferedImage(W*2+48,3*(H+44)+82,BufferedImage.TYPE_INT_RGB);Graphics2D g=sheet.createGraphics();
        g.setColor(new Color(0x07121D));g.fillRect(0,0,sheet.getWidth(),sheet.getHeight());
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);g.setFont(f.deriveFont(12f));g.drawString((rtl?"RTL GEOMETRY / ":"LTR / ")+"BAKED TEXTURES / SCHEMATIC UI + TOOLBAR / SYNTHETIC ARTWORK / NOT TV CAPTURE",16,26);
        String[] rows={"A / Idle","B / Artwork reference","C / Focus + readability reference"};
        for(int col=0;col<2;col++){
            int left=16+col*(W+16);g.setColor(new Color(0x80DEDA));g.setFont(f.deriveFont(16f));
            label(g,col==0?"v2.3d / 48 PX / Y=88 / 22 STARS":"v2.3e / 58 PX / Y=104 / 28 STARS",left,59);
            for(int row=0;row<3;row++){
                int top=82+row*(H+44);g.setColor(Color.WHITE);g.setFont(f.deriveFont(12f));label(g,rows[row],left,top-7);
                BufferedImage input=new BufferedImage(W,H,BufferedImage.TYPE_INT_ARGB);Graphics2D a=input.createGraphics();
                if(row==0)a.setColor(new Color(0x0B2030));else a.setPaint(new GradientPaint(0,0,new Color(0xD84329),W,H,new Color(0x234A81)));
                a.fillRect(0,0,W,H);a.dispose();int[] tex=col==0?d:e,pixels=new int[W*H];
                for(int y=0;y<H;y++)for(int x=0;x<W;x++){int sourceX=rtl?W-1-x:x;pixels[y*W+x]=AquaSurface.over(tex[y*W+sourceX],input.getRGB(sourceX,y));}
                BufferedImage image=new BufferedImage(W,H,BufferedImage.TYPE_INT_ARGB);image.setRGB(0,0,W,H,pixels,0,W);g.drawImage(image,left,top,null);
                drawToolbar(g,left,top,rtl);
                g.setColor(Color.WHITE);g.setFont(f.deriveFont(34f));text(g,"20:26",left,top+44,rtl);
                g.setFont(f.deriveFont(10f));text(g,"Chủ nhật · 01/11/2026",left,top+64,rtl);text(g,"Lịch âm · theo giờ TV",left,top+78,rtl);
                g.setColor(new Color(255,255,255,209));text(g,"Xin chào, Ăng Hòn! Chúc buổi tối thư giãn.",left,top+98,rtl);
                g.setColor(Color.WHITE);g.setFont(f.deriveFont(25f));text(g,row==2?"Ứng dụng đang chọn":"Aqua Galaxy",left,top+159,rtl);
                if(row==2){
                    int x=left+(rtl?W-1-142:26);g.setColor(new Color(0x173346));g.fillRoundRect(x,top+180,116,64,12,12);
                    g.setStroke(new BasicStroke(1.5f));g.setColor(new Color(0x80DEDA));g.drawRoundRect(x,top+180,116,64,12,12);
                    g.setFont(f.deriveFont(13f));g.setColor(Color.WHITE);g.drawString("App",x+44,top+219);
                }
            }
        }
        g.dispose();
        for(int col=0;col<2;col++){
            int left=16+col*(W+16);require(visiblePixels(sheet,left,45,W,17)>50,"Missing column label");
            for(int row=0;row<3;row++)require(visiblePixels(sheet,left,62+row*(H+44),W,15)>30,"Missing state label");
        }
        require(ImageIO.write(sheet,"png",new File(output)),"PNG writer missing");System.out.println(new File(output).getCanonicalPath());
    }
    private static void label(Graphics2D g,String value,int x,int y){g.drawGlyphVector(g.getFont().createGlyphVector(g.getFontRenderContext(),value),x,y);}
    private static int visiblePixels(BufferedImage image,int x,int y,int w,int h){int count=0;for(int yy=y;yy<y+h;yy++)for(int xx=x;xx<x+w;xx++)if((image.getRGB(xx,yy)&0xFFFFFF)!=0x07121D)count++;return count;}
    private static void text(Graphics2D g,String value,int left,int baseline,boolean rtl){g.drawString(value,left+(rtl?W-1-24-g.getFontMetrics().stringWidth(value):24),baseline);}
    private static void drawToolbar(Graphics2D g,int left,int top,boolean rtl){
        g.setStroke(new BasicStroke(1f));
        for(int i=0;i<3;i++){
            int px=368+i*40,cx=left+(rtl?W-1-px:px),cy=top+32;
            // Schematic UI is above the baked texture; no stars are drawn over the buttons.
            g.setColor(new Color(16,38,55,180));g.fillRoundRect(cx-16,cy-16,32,32,18,18);
            g.setColor(new Color(255,255,255,65));g.drawRoundRect(cx-16,cy-16,32,32,18,18);
            g.setColor(new Color(255,255,255,210));
            if(i==0){g.drawOval(cx-4,cy-4,8,8);for(int k=0;k<8;k++){double a=k*Math.PI/4;g.drawLine(cx+(int)Math.round(6*Math.cos(a)),cy+(int)Math.round(6*Math.sin(a)),cx+(int)Math.round(8*Math.cos(a)),cy+(int)Math.round(8*Math.sin(a)));}}
            else if(i==1){g.drawArc(cx-8,cy-6,16,12,35,110);g.drawArc(cx-5,cy-3,10,8,35,110);g.fillOval(cx-1,cy+3,3,3);}
            else{g.drawOval(cx-3,cy-7,6,6);g.drawArc(cx-7,cy+1,14,12,0,180);}
        }
    }
    private static int[][] stars(Class<?> type)throws Exception{java.lang.reflect.Field field=type.getDeclaredField("STARS");field.setAccessible(true);return (int[][])field.get(null);}
    private static boolean intersectsUI(int x1,int y1,int x2,int y2,boolean rtl){for(int[] r:UI){int left=rtl?W-1-r[2]:r[0],right=rtl?W-1-r[0]:r[2];if(x1<=right&&x2>=left&&y1<=r[3]&&y2>=r[1])return true;}return false;}
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private static String digest(int[] p)throws Exception{MessageDigest sha=MessageDigest.getInstance("SHA-256");for(int c:p){sha.update((byte)(c>>>24));sha.update((byte)(c>>>16));sha.update((byte)(c>>>8));sha.update((byte)c);}return java.util.HexFormat.of().formatHex(sha.digest());}
}
