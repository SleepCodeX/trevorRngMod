package dev.trevorroll;
/** Small vector-style module symbols, independent of OS emoji/font coverage. */
final class ModuleIcons {
    private ModuleIcons(){}
    static void draw(MinecraftCanvas c,int module,double x,double y,double s,int ink){
        c.push(x,y,s);
        try{
            switch(module){
                case 0 -> {for(int i=0;i<3;i++){c.round(-10+i*8,-8,6,16,2,ink);c.round(-8+i*8,-4,2,4,1,0xFF302B44);}}
                case 1 -> {c.ellipse(-6,-1,12,9,ink);for(int i=0;i<4;i++)c.ellipse(-11+i*6,-10-Math.sin(i*Math.PI/3)*2,5,7,ink);}
                case 2 -> {c.ellipse(-10,-10,20,20,ink);c.ellipse(-5,-6,4,4,0xFF29253B);c.ellipse(2,-3,4,4,0xFF29253B);c.ellipse(-3,3,4,4,0xFF29253B);}
                case 3 -> {c.round(-10,-8,20,16,3,ink);c.round(-8,-6,16,12,2,0xFF29253B);c.round(-5,-3,6,4,1,ink);c.round(2,2,5,3,1,ink);}
                case 4 -> {c.ellipse(-11,-11,22,22,ink);c.ellipse(-8,-8,16,16,0xFF29253B);c.ellipse(-4,-3,8,7,ink);for(int i=0;i<3;i++)c.ellipse(-6+i*4,-7,3,3,ink);}
                case 5 -> {c.round(-11,-4,5,8,1,ink);c.round(-6,-8,7,16,2,ink);for(int i=0;i<3;i++)c.round(5+i*3,-3-i*2,1,6+i*4,1,ink);}
                default -> {for(int i=0;i<3;i++)c.round(-9+i*7,4-i*5,5,8+i*5,1,ink);}
            }
        }finally{c.pop();}
    }
}
