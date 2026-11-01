package com.gathin.tvhome;

/** Isolated visual-review candidate. v2.3e Cosmic Plus static decor; not an input to the shipped patch/APKs. */
final class CosmicDecor {
    private static final int W=AquaSurface.WIDTH,H=AquaSurface.HEIGHT;
    static final int CX=413,CY=104,R=29,HALO=8;
    static final int LIGHT=0xFFE8C8A0,DARK=0xFF77627F,RIM=0xFFF3D9B7,SHADOW=0xFF392C46;
    private static final int[][] STARS={
        {278,38,140},{331,22,58},{451,89,130},{442,138,66},
        {298,119,76},{360,91,60},{278,80,68},{317,70,56},
        {380,30,74},{466,45,54},{351,145,70},{465,157,50},
        {286,185,48},{315,176,58},{391,153,62},{452,183,64},
        {353,204,52},{278,226,42},{421,220,58},{470,233,46},
        {330,245,44},{396,251,50},
        {305,104,48},{340,124,54},{464,119,46},
        {370,183,52},{309,216,42},{445,246,48}
    };
    private CosmicDecor() { }
    static int starCount() { return STARS.length; }

    static void paint(int[] pixels,boolean rtl,int accent) {
        for(int y=CY-R-HALO;y<=CY+R+HALO;y++)for(int x=CX-R-HALO;x<=CX+R+HALO;x++) {
            float dx=x-CX,dy=y-CY;
            float distance=(float)Math.sqrt(dx*dx+dy*dy);
            float halo=clamp((R+HALO-distance)/HALO);
            // Warm, very faint halo. No aqua is used by the planet itself.
            blend(pixels,x,y,alpha(RIM,Math.round(14*halo*halo)),rtl);
            float coverage=clamp(R+.5f-distance);
            if(coverage==0)continue;
            float nx=dx/R,ny=dy/R;
            float nz=(float)Math.sqrt(Math.max(0f,1f-nx*nx-ny*ny));
            // A spherical normal and an upper-right light create a soft terminator, not a flat yellow disc.
            float diffuse=clamp((nx*.82f-ny*.30f+nz*.42f+.02f)/.92f);
            diffuse=diffuse*diffuse*(3f-2f*diffuse);
            int shaded=mix(SHADOW,DARK,.12f+.48f*nz);
            int body=mix(shaded,LIGHT,.90f*diffuse);
            blend(pixels,x,y,alpha(body,Math.round(252*coverage)),rtl);
            float edge=clamp((distance-(R-1.2f))/1.2f);
            blend(pixels,x,y,alpha(RIM,Math.round(92*coverage*edge*diffuse)),rtl);
        }
        // Preserve all 22 v2.3d stars, adding six small points. Keep only the original two sparkle accents.
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

    static boolean planetRegion(int x,int y) {
        return Math.abs(x-CX)<=R+HALO&&Math.abs(y-CY)<=R+HALO;
    }
    static boolean region(int x,int y) {
        if(planetRegion(x,y))return true;
        for(int[] s:STARS)if(Math.abs(x-s[0])<=2&&Math.abs(y-s[1])<=2)return true;
        return false;
    }
    private static int mix(int a,int b,float t) {
        int r=Math.round(((a>>>16)&255)*(1-t)+((b>>>16)&255)*t);
        int g=Math.round(((a>>>8)&255)*(1-t)+((b>>>8)&255)*t);
        int blue=Math.round((a&255)*(1-t)+(b&255)*t);
        return 0xFF000000|r<<16|g<<8|blue;
    }
    private static void blend(int[] pixels,int x,int y,int colour,boolean rtl) {
        if(x<0||x>=W||y<0||y>=H)return;
        int index=y*W+(rtl?W-1-x:x);pixels[index]=AquaSurface.over(colour,pixels[index]);
    }
    private static float clamp(float v) { return Math.max(0f,Math.min(1f,v)); }
    private static int alpha(int c,int a) { return (c&0xFFFFFF)|(a<<24); }
}
