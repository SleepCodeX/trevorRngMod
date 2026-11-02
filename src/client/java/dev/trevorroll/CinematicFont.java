package dev.trevorroll;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/** Lazily loaded Western display and clean caption atlases, independent of normal menu text. */
final class CinematicFont {
    private CinematicFont() {}
    private record Glyph(int u,int v,double advance) {}
    private static final class Typeface {
        final Identifier texture;
        final Glyph[] ascii=new Glyph[128];
        final Map<Integer,Glyph> extra=new HashMap<>();
        Typeface(String name){
            texture=Identifier.fromNamespaceAndPath("trevorroll","textures/gui/cinematic_"+name+".png");
            try(var in=CinematicFont.class.getResourceAsStream("/assets/trevorroll/cinematic_"+name+".json")){
                var json=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
                for(var entry:json.entrySet()){
                    int ch=Integer.parseInt(entry.getKey());var m=entry.getValue().getAsJsonArray();
                    var glyph=new Glyph(m.get(0).getAsInt(),m.get(1).getAsInt(),m.get(2).getAsDouble());
                    if(ch<ascii.length)ascii[ch]=glyph;else extra.put(ch,glyph);
                }
            }catch(Exception e){throw new ExceptionInInitializerError(e);}
        }
        Glyph glyph(int ch){Glyph g=ch<ascii.length?ascii[ch]:extra.get(ch);return g==null?ascii['?']:g;}
        double width(String text){double n=0;for(int i=0;i<text.length();){int ch=text.codePointAt(i);i+=Character.charCount(ch);n+=glyph(ch).advance*9/64.;}return n;}
        void draw(GuiGraphicsExtractor g,String text,double x,double y,double scale,int color){
            if((color>>>24)==0)return;
            g.pose().pushMatrix();g.pose().translate((float)x,(float)(y+8*scale));g.pose().scale((float)(9/64.*scale),(float)(9/64.*scale));
            try{
                double at=0;
                for(int i=0;i<text.length();){int ch=text.codePointAt(i);i+=Character.charCount(ch);Glyph m=glyph(ch);
                    if(ch!=' ')g.blit(RenderPipelines.GUI_TEXTURED,texture,(int)Math.round(at)-12,-80,m.u,m.v,96,112,96,112,1536,896,color);
                    at+=m.advance;
                }
            }finally{g.pose().popMatrix();}
        }
    }
    private static final class Fonts {static final Typeface TITLE=new Typeface("title"),BODY=new Typeface("body");}
    static double titleWidth(String text){return Fonts.TITLE.width(text);}
    static double bodyWidth(String text){return Fonts.BODY.width(text);}
    static void title(GuiGraphicsExtractor g,String text,double x,double y,double scale,int color){Fonts.TITLE.draw(g,text,x,y,scale,color);}
    static void body(GuiGraphicsExtractor g,String text,double x,double y,double scale,int color){Fonts.BODY.draw(g,text,x,y,scale,color);}
}
