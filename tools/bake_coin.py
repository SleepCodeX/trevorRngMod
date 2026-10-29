"""Deterministic 512px, 64-angle gold medallion; ray intersections resolve occlusion per pixel."""
from pathlib import Path
import numpy as np
from PIL import Image
SIZE=512; FRAMES=64
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/trevorroll/textures/gui/coin'
root.mkdir(parents=True,exist_ok=True)
# Orthographic rays are transformed into model space; nearest positive intersection wins.
y,x=np.mgrid[:SIZE,:SIZE].astype(np.float32);x=(x+.5-SIZE/2)*96/SIZE;y=(y+.5-SIZE/2)*96/SIZE
light=np.array([-.45,-.6,-1.],np.float32);light/=np.linalg.norm(light)
for sheet in range(4):
 atlas=Image.new('RGBA',(SIZE*4,SIZE*4))
 for cell in range(16):
  angle=(sheet*16+cell)*2*np.pi/FRAMES;c,s=np.cos(angle),np.sin(angle);tilt=.16;ct,st=np.cos(tilt),np.sin(tilt)
  # camera at z=-100, ray points toward +z
  oy=y*ct-100*st;oz=-y*st-100*ct
  ox=x*c-oz*s;oz=x*s+oz*c
  dx=-ct*s;dy=st;dz=ct*c
  depth=np.full(x.shape,np.inf,np.float32);norm=np.zeros((*x.shape,3),np.float32);mat=np.zeros(x.shape,np.int8)
  def hit(t,nx,ny,nz,mask,material):
   global depth,norm,mat
   valid=mask&(t>0)&(t<depth);depth[valid]=t[valid]
   # Rotate normals back to camera space.
   xx=nx*c+nz*s;zz=-nx*s+nz*c
   nnx=xx;nny=ny*ct-zz*st;nnz=ny*st+zz*ct
   norm[:,:,0][valid]=np.broadcast_to(nnx,x.shape)[valid];norm[:,:,1][valid]=np.broadcast_to(nny,x.shape)[valid];norm[:,:,2][valid]=np.broadcast_to(nnz,x.shape)[valid];mat[valid]=material
  def cylinder(radius,z0,z1,material):
   for zplane,sign in [(z0,-1),(z1,1)]:
    t=(zplane-oz)/(dz if abs(dz)>1e-7 else 1e-7);xx=ox+t*dx;yy=oy+t*dy
    hit(t,0.,0.,float(sign),xx*xx+yy*yy<=radius*radius,material)
   a=dx*dx+dy*dy;b=2*(ox*dx+oy*dy);cc=ox*ox+oy*oy-radius*radius;disc=b*b-4*a*cc
   for sign in [-1,1]:
    t=(-b+sign*np.sqrt(np.maximum(0,disc)))/(2*a+1e-12);xx=ox+t*dx;yy=oy+t*dy;zz=oz+t*dz
    hit(t,xx/radius,yy/radius,np.zeros(x.shape),(disc>=0)&(zz>=z0)&(zz<=z1),material)
  def ellipsoid(cx,cy,cz,rx,ry,rz,material):
   px=(ox-cx)/rx;py=(oy-cy)/ry;pz=(oz-cz)/rz;vx=dx/rx;vy=dy/ry;vz=dz/rz
   a=vx*vx+vy*vy+vz*vz;b=2*(px*vx+py*vy+pz*vz);cc=px*px+py*py+pz*pz-1;disc=b*b-4*a*cc
   t=(-b-np.sqrt(np.maximum(0,disc)))/(2*a);nx=(ox+t*dx-cx)/(rx*rx);ny=(oy+t*dy-cy)/(ry*ry);nz=(oz+t*dz-cz)/(rz*rz)
   hit(t,nx,ny,nz,disc>=0,material)
  cylinder(35,-4,4,1)
  # Rounded milled rim, built from a dense ring of small ellipsoids.
  for a in np.linspace(0,2*np.pi,160,endpoint=False):
   ellipsoid(33.8*np.cos(a),33.8*np.sin(a),0,2.1,2.1,5.2,2)
  cylinder(31.7,-4.9,4.9,0)
  # Four non-overlapping toes on BOTH faces; no face-average sorting.
  for side in [-1,1]:
   ellipsoid(0,9,side*5.5,14,11,3.7,3)
   for i,(cx,cy) in enumerate([(-18,-7),(-7,-18),(7,-18),(18,-7)]):
    ellipsoid(cx,cy,side*5.5,5.8,7.6,3.4,4+i)
  norm/=np.maximum(np.linalg.norm(norm,axis=2,keepdims=True),1e-8)
  ndl=np.maximum(0,norm@light);half=light+np.array([0,0,-1]);half/=np.linalg.norm(half);spec=np.maximum(0,norm@half)**45
  reflection=np.clip(.5+.5*np.sin((norm[:,:,0]*1.5+norm[:,:,1]*.7)*6),0,1)
  base=np.zeros((*x.shape,3),np.float32);base[:]=[.85,.46,.08];base[mat==1]=[.72,.36,.055];base[mat==2]=[1,.68,.2];base[mat>=3]=[1,.76,.32]
  rgb=base*(.3+.55*ndl[:,:,None]+.23*reflection[:,:,None])+spec[:,:,None]*np.array([.8,.72,.5])
  rgba=np.zeros((*x.shape,4),np.uint8);rgba[:,:,:3]=np.uint8(np.clip(rgb,0,1)*255);rgba[:,:,3]=np.where(np.isfinite(depth),255,0)
  frame=Image.fromarray(rgba).resize((SIZE,SIZE),Image.Resampling.LANCZOS)
  atlas.paste(frame,((cell%4)*SIZE,(cell//4)*SIZE))
  if sheet==0 and cell==3: frame.save(root/'poster.png')
 atlas.save(root/f'coin_{sheet}.png',optimize=True)
 (root/f'coin_{sheet}.png.mcmeta').write_text('{"texture":{"blur":true,"clamp":true}}\n')
 print('Baked sheet',sheet,flush=True)
