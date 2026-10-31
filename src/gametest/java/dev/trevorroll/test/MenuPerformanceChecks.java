package dev.trevorroll;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Compare the previous allocation-heavy text measurement with the cached metrics.
 * Timing is diagnostic evidence on this runner, never a game-FPS or startup claim. */
public final class MenuPerformanceChecks {
    private MenuPerformanceChecks() {}
    public static void run() {
        JsonObject metrics;
        try(var in=MenuPerformanceChecks.class.getResourceAsStream("/assets/trevorroll/menu_font.json")) {
            metrics=JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();
        } catch(Exception e){throw new AssertionError(e);}
        String text="Your hunt. Your way. Trapper Crest settings";
        double expected=legacy(metrics,text);
        if(Math.abs(expected-MenuFont.width(text))>1e-9)throw new AssertionError("Cached metrics preserve text width");
        for(int i=0;i<5000;i++){sink+=legacy(metrics,text);sink+=MenuFont.width(text);}
        long[] before=new long[5],after=new long[5];
        for(int n=0;n<5;n++) {
            long start=System.nanoTime();double sum=0;
            for(int i=0;i<20000;i++)sum+=legacy(metrics,text);
            before[n]=System.nanoTime()-start;sink=sum;
            start=System.nanoTime();sum=0;
            for(int i=0;i<20000;i++)sum+=MenuFont.width(text);
            after[n]=System.nanoTime()-start;sink=sum;
        }
        Arrays.sort(before);Arrays.sort(after);
        System.out.println("MENU PROFILE: 20,000 identical text widths; previous median ns="+before[2]+", cached median ns="+after[2]+"; runner-only CPU diagnostic");
    }
    private static volatile double sink;
    private static double legacy(JsonObject metrics,String text) {
        double width=0;
        for(int ch:text.codePoints().toArray()) {
            var m=metrics.getAsJsonArray(Integer.toString(metrics.has(Integer.toString(ch))?ch:63));
            width+=m.get(2).getAsDouble()*9/64;
        }
        return width;
    }
}
