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
    private static final JsonObject METRICS;
    static {
        try(var in=MenuFont.class.getResourceAsStream("/assets/trevorroll/menu_font.json")){
            METRICS=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
        }catch(Exception e){throw new ExceptionInInitializerError(e);}
    }
    private static JsonArray metric(int ch){return METRICS.getAsJsonArray(Integer.toString(METRICS.has(Integer.toString(ch))?ch:63));}
    static double width(String text){double w=0;for(int ch:text.codePoints().toArray())w+=metric(ch).get(2).getAsDouble()*9/64;return w;}
    static void text(GuiGraphicsExtractor g,String text,double x,double y,double scale,int color){
        double pixel=9/64.*scale;
        g.pose().pushMatrix();g.pose().translate((float)x,(float)(y+8*scale));g.pose().scale((float)pixel,(float)pixel);
        try{
            double at=0;
            for(int ch:text.codePoints().toArray()){
                var m=metric(ch);
                if(ch!=32)g.blit(RenderPipelines.GUI_TEXTURED,TEXTURE,(int)Math.round(at)-5,-72,m.get(0).getAsInt(),m.get(1).getAsInt(),80,96,80,96,1280,768,color);
                at+=m.get(2).getAsDouble();
            }
        }finally{g.pose().popMatrix();}
    }
    static String ellipsis(String text,double max){if(width(text)<=max)return text;while(!text.isEmpty()&&width(text+"…")>max)text=text.substring(0,text.length()-1);return text+"…";}
}
