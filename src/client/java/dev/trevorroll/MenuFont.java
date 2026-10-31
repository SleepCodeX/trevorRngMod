package dev.trevorroll;
import com.google.gson.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import java.io.*;
import java.nio.charset.StandardCharsets;
/** Bundled OFL Nunito glyph atlas: stable rounded typography at all GUI scales. */
final class MenuFont {
    private static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("trevorroll","textures/gui/menu_font.png");
    private record Glyph(int u, int v, double advance) {}
    private static final Glyph[] GLYPHS = new Glyph[256];
    private static final java.util.Map<Integer,Glyph> EXTRA = new java.util.HashMap<>();
    private static double opacity = 1;
    static double opacity() { return opacity; }
    static void opacity(double value) { opacity = value; }
    static {
        try(var in=MenuFont.class.getResourceAsStream("/assets/trevorroll/menu_font.json")){
            var metrics=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
            for(var entry:metrics.entrySet()) {
                int ch=Integer.parseInt(entry.getKey()); var m=entry.getValue().getAsJsonArray();
                var glyph=new Glyph(m.get(0).getAsInt(),m.get(1).getAsInt(),m.get(2).getAsDouble());
                if(ch<GLYPHS.length)GLYPHS[ch]=glyph;else EXTRA.put(ch,glyph);
            }
        }catch(Exception e){throw new ExceptionInInitializerError(e);}
    }
    private static Glyph metric(int ch){
        Glyph glyph=ch<GLYPHS.length?GLYPHS[ch]:EXTRA.get(ch);
        return glyph==null?GLYPHS[63]:glyph;
    }
    static double width(String text){
        double w=0;for(int i=0;i<text.length();) {int ch=text.codePointAt(i);i+=Character.charCount(ch);w+=metric(ch).advance*9/64;}
        return w;
    }
    static void text(GuiGraphicsExtractor g,String text,double x,double y,double scale,int color){
        color=(color&0xFFFFFF)|((int)Math.round((color>>>24)*opacity)<<24);
        if((color>>>24)==0)return;
        double pixel=9/64.*scale;
        g.pose().pushMatrix();g.pose().translate((float)x,(float)(y+8*scale));g.pose().scale((float)pixel,(float)pixel);
        try{
            double at=0;
            for(int i=0;i<text.length();){
                int ch=text.codePointAt(i);i+=Character.charCount(ch);
                var m=metric(ch);
                if(ch!=32)g.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,(int)Math.round(at)-5,-72,m.u,m.v,80,96,80,96,1280,768,color);
                at+=m.advance;
            }
        }finally{g.pose().popMatrix();}
    }
    static String ellipsis(String text,double max){if(width(text)<=max)return text;while(!text.isEmpty()&&width(text+"…")>max)text=text.substring(0,text.length()-1);return text+"…";}
}
