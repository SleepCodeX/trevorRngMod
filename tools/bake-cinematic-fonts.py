#!/usr/bin/env python3
"""Bake the OFL Rye/Outfit fonts: --title Rye-Regular.ttf --body Outfit.ttf."""
import argparse,json
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
parser=argparse.ArgumentParser()
parser.add_argument('--title',required=True,type=Path)
parser.add_argument('--body',required=True,type=Path)
a=parser.parse_args()
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/trevorroll'
chars=''.join(chr(i) for i in range(32,127))+'…’–−•'
for label,path in [('title',a.title),('body',a.body)]:
    font=ImageFont.truetype(str(path),64)
    if label=='body':font.set_variation_by_axes([500])
    image=Image.new('RGBA',(1536,896));draw=ImageDraw.Draw(image);metrics={}
    for i,ch in enumerate(chars):
        x,y=i%16*96,i//16*112
        left,top,right,bottom=font.getbbox(ch,anchor='ls')
        assert left>=-12 and right<=83 and top>=-80 and bottom<=30,(ch,(left,top,right,bottom))
        draw.text((x+12,y+80),ch,font=font,fill=(255,255,255,255),anchor='ls')
        metrics[str(ord(ch))]=[x,y,round(font.getlength(ch),4)]
    image.save(root/'textures/gui'/('cinematic_'+label+'.png'),optimize=True)
    (root/('cinematic_'+label+'.json')).write_text(json.dumps(metrics,separators=(',',':'))+'\n')
    print(label,len(chars),'glyphs')
