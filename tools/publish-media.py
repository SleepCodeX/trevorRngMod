#!/usr/bin/env python3
"""Prepare original game screenshots and lossless feature animations."""
from pathlib import Path
from PIL import Image, ImageSequence
import argparse, hashlib, re, shutil
from datetime import datetime, timezone
p=argparse.ArgumentParser();p.add_argument('screenshots',type=Path);p.add_argument('source_sha');p.add_argument('run_id');a=p.parse_args()
root=Path(__file__).resolve().parents[1];src=a.screenshots;out=root/'docs/media/1.9.1';out.mkdir(parents=True,exist_ok=True);records=[]
def pick(suffix):
 paths=list(src.glob('*_'+suffix+'.png'));assert len(paths)==1,(suffix,paths);return paths[0]
def sequence(suffix,count):
 paths=sorted(src.glob('*_'+suffix+'-*.png'));assert len(paths)==count,(suffix,len(paths));return paths
def still(name,suffix):
 s=pick(suffix);shutil.copyfile(s,out/name);assert s.read_bytes()==(out/name).read_bytes();records.append((name,s.name,'Unchanged PNG'))
def animation(name,paths,duration,crop=None):
 frames=[]
 for path in paths:
  with Image.open(path) as im:
   frame=im.convert('RGB');frames.append(frame.crop(crop) if crop else frame)
 frames[0].save(out/name,format='WEBP',save_all=True,append_images=frames[1:],duration=duration,loop=0,lossless=True,quality=100,method=4)
 with Image.open(out/name) as check:
  assert check.convert('RGB').tobytes()==frames[0].tobytes(),name
  check.seek(check.n_frames-1);assert check.convert('RGB').tobytes()==frames[-1].tobytes(),name
  for frame in ImageSequence.Iterator(check):frame.load()
 names=', '.join(x.name for x in paths) if len(paths)<6 else paths[0].name+' ... '+paths[-1].name+' ('+str(len(paths))+' frames)'
 records.append((name,names,'Lossless animated WebP'+('; crop '+str(crop) if crop else '; full frames')));print(name,(out/name).stat().st_size,flush=True)
still('settings-modules.png','settings-dashboard-1280x720-scale-2');still('hud-editor.png','layout-editor')
for name,suffix in [('extra-reward.png','elusive-reward-extra'),('extreme-reward.png','elusive-reward-jackpot'),('beth-hunter.png','elusive-scene-stage-0'),('dragon-reveal.png','elusive-scene-stage-2')]:still(name,suffix)
animation('crest-states.webp',[pick('crest-'+x+'-scale-2') for x in ['uncommon','rare','missing']],2400,(780,150,1280,500))
animation('elusive-styles.webp',[pick('elusive-reward-'+x) for x in ['minimal','classic','extra','jackpot']],2200)
for name,page in [('appearance.webp',2),('audio-mixer.webp',5),('menu-keys.webp',7),('jackpots.webp',4),('pelt-tracker.webp',6)]:animation(name,[pick('settings-page-'+str(page)+'-1280x720-scale-2'),pick('settings-page-'+str(page)+'-bottom-1280x720-scale-2')],2600)
animation('rarity-reveals.webp',[pick('rarity-result-'+x) for x in ['trackable','untrackable','undetected','endangered','elusive']],1800,(200,110,1080,485))
animation('solid-models.webp',sequence('solid-model-motion',60),100)
animation('elusive-journey.webp',sequence('elusive-journey',85),[100]*84+[1100])
# CI ZIP extraction does not preserve capture mtimes, so use a documented comparison rate.
animation('modules-advanced.webp',sequence('settings-advanced-rotation',24),500)
(out/'cowboy-hunter.png').unlink(missing_ok=True)
intro=f'''# Trevor Roll 1.9.1 media

Runtime/capture source: {a.source_sha}.
[Production-client run {a.run_id}](https://github.com/SleepCodeX/trevorRngMod/actions/runs/{a.run_id}).
Published frames come from that successful run's validation-evidence artifact.
Losslessly encoded on {datetime.now(timezone.utc).date().isoformat()}.

PNGs are unchanged game screenshots. WebP preserves the original game pixels.
Only the two documented HUD crops remove surrounding space; no text, model or
scenery was added to the captures.

The gallery samples native model poses every 100 ms for six seconds. The journey
samples its 8.5-second native timeline every 100 ms, then pauses on the last frame
for one extra second. The dashboard compares 24 real rotation captures at a fixed
500 ms documentation cadence. These are game-rendered documentation sequences,
not continuous screen recordings or performance measurements. Settings and HUD
comparisons pause for reading.

| Published file | Original capture(s) | Processing |
| --- | --- | --- |
'''
rows='\n'.join('| '+n+' | '+s+' | '+t+' |' for n,s,t in records)
(out/'README.md').write_text(intro+rows+'\n\nTests use a disposable offline world; live Hypixel and the complete personal modpack are not covered. [Asset credits](../../3D-MODELS.md#asset-credits). Previous versioned media stays available for historical links.\n')
for doc in ['README.md','RELEASE-NOTES.md']:
 for target in re.findall(r'!\[[^\]]*\]\(([^)]+)\)',(root/doc).read_text()):
  local=target if target.startswith('docs/') else target.split('/1.9.1/',1)[1] if '/1.9.1/docs/' in target else None
  if local:assert (root/local).is_file(),target
print('All feature and release image references resolve.')
