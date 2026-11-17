package dev.trevorroll;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/** Small author-time bake of Kay Lousberg's CC0 Rogue mesh and three animation clips. */
public final class BethModel {
    private static final class Loaded { static final BethModel MODEL=read(); }
    public static BethModel get(){return Loaded.MODEL;}
    final int joints,vertexCount;
    final float[] vertices,weights;
    final int[] bones,indices;
    private final Clip[] clips;
    private record Clip(float duration,int frames,float[] matrices) {}

    /** Scratch space is reused by the render callback; never shared with extracted GUI state. */
    public static final class Pose {
        final float[] matrices,blend;
        public final float[] vertices;
        private Pose(BethModel model){
            matrices=new float[model.joints*12];blend=new float[matrices.length];
            vertices=new float[model.vertexCount*6];
        }
    }
    private BethModel(int joints,int vertexCount,float[] vertices,float[] weights,int[] bones,int[] indices,Clip[] clips){
        this.joints=joints;this.vertexCount=vertexCount;this.vertices=vertices;
        this.weights=weights;this.bones=bones;this.indices=indices;this.clips=clips;
    }
    public Pose newPose(){return new Pose(this);}
    public int vertexCount(){return vertexCount;}
    public int triangleCount(){return indices.length/3;}
    public int jointCount(){return joints;}
    public void animate(Pose pose,double seconds,double local){
        sample(clips[0],seconds,true,pose.matrices);
        double moving=Math.max(1-ElusiveSequence.smooth((local-.11)/.10),ElusiveSequence.smooth((local-.83)/.13));
        if(moving>0){sample(clips[1],seconds,true,pose.blend);blend(pose.matrices,pose.blend,moving);}
        double notice=ElusiveSequence.smooth((local-.23)/.10)*(1-ElusiveSequence.smooth((local-.72)/.12));
        if(notice>0){
            sample(clips[2],Math.clamp((local-.26)/.43,0,1)*clips[2].duration,false,pose.blend);
            blend(pose.matrices,pose.blend,notice*(1-moving));
        }
        skin(pose);
    }
    private static void blend(float[] a,float[] b,double weight){
        float t=(float)weight;for(int i=0;i<a.length;i++)a[i]+=(b[i]-a[i])*t;
    }
    private void sample(Clip clip,double seconds,boolean loop,float[] out){
        double time=loop?((seconds%clip.duration)+clip.duration)%clip.duration:Math.clamp(seconds,0,clip.duration);
        double frame=time*(clip.frames-1)/clip.duration;int lo=(int)frame,hi=Math.min(lo+1,clip.frames-1);
        float t=(float)(frame-lo);int stride=joints*12,a=lo*stride,b=hi*stride;
        for(int i=0;i<stride;i++)out[i]=clip.matrices[a+i]+(clip.matrices[b+i]-clip.matrices[a+i])*t;
    }
    private void skin(Pose pose){
        float[] m=pose.matrices,out=pose.vertices;
        for(int vertex=0;vertex<vertexCount;vertex++){
            int v=vertex*8,w=vertex*4,o=vertex*6;
            float x=vertices[v],y=vertices[v+1],z=vertices[v+2],nx=vertices[v+3],ny=vertices[v+4],nz=vertices[v+5];
            float px=0,py=0,pz=0,dx=0,dy=0,dz=0;
            for(int influence=0;influence<4;influence++){
                float weight=weights[w+influence];if(weight==0)continue;
                int at=bones[w+influence]*12;
                px+=(m[at]*x+m[at+1]*y+m[at+2]*z+m[at+3])*weight;
                py+=(m[at+4]*x+m[at+5]*y+m[at+6]*z+m[at+7])*weight;
                pz+=(m[at+8]*x+m[at+9]*y+m[at+10]*z+m[at+11])*weight;
                dx+=(m[at]*nx+m[at+1]*ny+m[at+2]*nz)*weight;
                dy+=(m[at+4]*nx+m[at+5]*ny+m[at+6]*nz)*weight;
                dz+=(m[at+8]*nx+m[at+9]*ny+m[at+10]*nz)*weight;
            }
            float inv=(float)(1/Math.sqrt(Math.max(1e-12,dx*dx+dy*dy+dz*dz)));
            out[o]=px;out[o+1]=py;out[o+2]=pz;out[o+3]=dx*inv;out[o+4]=dy*inv;out[o+5]=dz*inv;
        }
    }
    private static BethModel read(){
        try(var raw=BethModel.class.getResourceAsStream("/assets/trevorroll/models/beth.bin.gz")){
            if(raw==null)throw new IOException("Missing baked Beth model");
            try(var in=new DataInputStream(new GZIPInputStream(raw))){
                if(in.readInt()!=0x54524248||in.readInt()!=1)throw new IOException("Unsupported Beth model format");
                int joints=count(in,1,128),vertexCount=count(in,1,20000),indexCount=count(in,3,120000);
                if(indexCount%3!=0)throw new IOException("Incomplete model triangle");
                float[] vertices=new float[vertexCount*8],weights=new float[vertexCount*4];int[] bones=new int[weights.length],indices=new int[indexCount];
                for(int v=0;v<vertexCount;v++){
                    for(int i=0;i<8;i++)vertices[v*8+i]=finite(in);
                    for(int i=0;i<4;i++){int bone=in.readUnsignedShort();if(bone>=joints)throw new IOException("Invalid model joint");bones[v*4+i]=bone;}
                    float total=0;for(int i=0;i<4;i++){float w=finite(in);if(w<0||w>1)throw new IOException("Invalid skin weight");weights[v*4+i]=w;total+=w;}
                    if(Math.abs(total-1)>.001)throw new IOException("Skin weights must sum to one");
                }
                for(int i=0;i<indices.length;i++)indices[i]=count(in,0,vertexCount-1);
                if(in.readInt()!=3)throw new IOException("Missing animation clips");Clip[] clips=new Clip[3];
                for(int i=0;i<clips.length;i++){
                    float duration=finite(in);if(duration<=0||duration>10)throw new IOException("Invalid animation duration");
                    int frames=count(in,2,601);float[] matrices=new float[frames*joints*12];
                    for(int j=0;j<matrices.length;j++)matrices[j]=finite(in);
                    clips[i]=new Clip(duration,frames,matrices);
                }
                if(in.read()!=-1)throw new IOException("Unexpected model data");
                return new BethModel(joints,vertexCount,vertices,weights,bones,indices,clips);
            }
        }catch(IOException e){throw new IllegalStateException("Cannot load the bundled Beth model",e);}
    }
    private static int count(DataInputStream in,int min,int max)throws IOException{
        int n=in.readInt();if(n<min||n>max)throw new IOException("Model count outside supported bounds");return n;
    }
    private static float finite(DataInputStream in)throws IOException{
        float value=in.readFloat();if(!Float.isFinite(value))throw new IOException("Non-finite model value");return value;
    }
}
