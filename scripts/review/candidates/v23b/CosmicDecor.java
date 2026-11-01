package com.gathin.tvhome;

/** Review candidate only: not used by the launcher patch or current APKs. Baked static decoration. */
final class CosmicDecor {
    private static final int W=AquaSurface.WIDTH,H=AquaSurface.HEIGHT;
    static final int CX=413,CY=56,R=18,HALO=7;
    private static final int[][] STARS={
        {278,38,140},{331,22,58},{451,89,130},{442,138,66},
        {298,119,76},{360,91,60},{278,80,68},{317,70,56},
        {380,30,74},{466,45,54},{351,145,70},{465,157,50}
    };
    private CosmicDecor() { }
    static int starCount() { return STARS.length; }

    static void paint(int[] pixels,boolean rtl,int accent) {
        for(int y=CY-R-HALO;y<=CY+R+HALO;y++)for(int x=CX-R-HALO;x<=CX+R+HALO;x++) {
            float dx=x-CX,dy=y-CY;
            float distance=(float)Math.sqrt(dx*dx+dy*dy);
            float halo=clamp((R+HALO-distance)/HALO);
            blend(pixels,x,y,alpha(accent,Math.round(20*halo*halo)),rtl);
            float edge=clamp(R+.5f-distance);
            if(edge==0)continue;
            blend(pixels,x,y,alpha(0xFF172C49,Math.round(190*edge)),rtl);
            // Eclipse offset scales by 1.8 with the planet, preserving the original crescent shape.
            float shadowDistance=(float)Math.sqrt((dx+5.04f)*(dx+5.04f)+(dy-2.52f)*(dy-2.52f));
            float crescent=clamp(shadowDistance-(R-1.17f));
            blend(pixels,x,y,alpha(accent,Math.round(112*edge*crescent)),rtl);
        }
        for(int i=0;i<STARS.length;i++) {
            int x=STARS[i][0],y=STARS[i][1];
            blend(pixels,x,y,alpha(accent,STARS[i][2]),rtl);
            int arm=(i==0||i==2)?38:12;
            blend(pixels,x-1,y,alpha(accent,arm),rtl);blend(pixels,x+1,y,alpha(accent,arm),rtl);
            blend(pixels,x,y-1,alpha(accent,arm),rtl);blend(pixels,x,y+1,alpha(accent,arm),rtl);
            if(i==0||i==2) {
                blend(pixels,x-2,y,alpha(accent,14),rtl);blend(pixels,x+2,y,alpha(accent,14),rtl);
                blend(pixels,x,y-2,alpha(accent,14),rtl);blend(pixels,x,y+2,alpha(accent,14),rtl);
            }
        }
    }

    static boolean region(int x,int y) {
        if(Math.abs(x-CX)<=R+HALO&&Math.abs(y-CY)<=R+HALO)return true;
        for(int[] s:STARS)if(Math.abs(x-s[0])<=2&&Math.abs(y-s[1])<=2)return true;
        return false;
    }
    private static void blend(int[] pixels,int x,int y,int colour,boolean rtl) {
        if(x<0||x>=W||y<0||y>=H)return;
        int index=y*W+(rtl?W-1-x:x);pixels[index]=AquaSurface.over(colour,pixels[index]);
    }
    private static float clamp(float v) { return Math.max(0f,Math.min(1f,v)); }
    private static int alpha(int c,int a) { return (c&0xFFFFFF)|(a<<24); }
}
