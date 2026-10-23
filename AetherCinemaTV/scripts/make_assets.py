#!/usr/bin/env python3
"""Generate original abstract, logo-free app assets. Pillow required."""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
from pathlib import Path
import math, random

ROOT = Path(__file__).resolve().parents[1] / 'app/src/main/res/drawable-nodpi'
ROOT.mkdir(parents=True, exist_ok=True)
FONT = '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
BOLD = '/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'
def font(size, bold=False): return ImageFont.truetype(BOLD if bold else FONT, size)
def bg(w,h,a,b):
    im=Image.new('RGB',(w,h)); pix=im.load()
    for y in range(h):
        t=y/h
        for x in range(w):
            s=.17*x/w + .83*t
            pix[x,y]=tuple(int(a[i]*(1-s)+b[i]*s) for i in range(3))
    return im

def art(name, title, subtitle, color1, color2, seed, motif):
    random.seed(seed)
    w,h=480,720
    im=bg(w,h,color1,color2).convert('RGBA')
    overlay=Image.new('RGBA',(w,h)); d=ImageDraw.Draw(overlay,'RGBA')
    for i in range(45):
        x=random.randrange(-100,w+100); y=random.randrange(-140,h+50)
        r=random.randint(10,100)
        d.ellipse([x-r,y-r,x+r,y+r], fill=(255,255,255,random.randrange(2,16)))
    if motif % 3 == 0:
        for i in range(6):
            radius=45+i*44
            d.ellipse((w//2-radius,190-radius,w//2+radius,190+radius), outline=(150,250,254,110-i*12),width=3)
        d.ellipse((125,55,420,350), fill=(236,177,128,32), outline=(254,240,203,45), width=6)
    elif motif % 3 == 1:
        for i in range(7):
            cx=80+i*58
            d.polygon([(cx-120,0),(cx+10,0),(cx+210,600),(cx+80,600)], fill=(117,238,224,8+i*4))
        d.arc((-50,0,560,620), 30, 310, fill=(197,237,255,125), width=6)
    else:
        for i in range(8):
            cy=70+i*51
            d.line([(0,cy+70),(110,cy-30),(270,cy+110),(480,cy-90)],fill=(240,211,253,90-i*9),width=3)
        d.ellipse((125,95,395,365), outline=(255,255,255,110), width=3)
    im = Image.alpha_composite(im, overlay)
    shadow=Image.new('RGBA',(w,h),(0,0,0,0)); sd=ImageDraw.Draw(shadow)
    for i in range(280):
        y=h-280+i
        alpha=int((i/280)**1.1*238)
        sd.line((0,y,w,y),fill=(4,9,18,alpha))
    im=Image.alpha_composite(im,shadow)
    d=ImageDraw.Draw(im)
    d.rounded_rectangle((27,25,168,60), radius=13, fill=(2,18,28,180), outline=(113,224,218,160), width=1)
    d.text((39,35),'AETHER  /  TV',font=font(14,True),fill=(155,244,237))
    words=title.upper().split(' ')
    lines=[]; line=''
    for word in words:
        maybe=(line+' '+word).strip()
        if d.textbbox((0,0), maybe,font=font(43,True))[2]>w-70 and line:
            lines.append(line); line=word
        else: line=maybe
    if line: lines.append(line)
    lines=lines[:3]
    sy=520-48*(len(lines)-1)
    for i,text in enumerate(lines):
        d.text((31,sy+51*i),text,font=font(43,True),fill=(247,249,251))
    d.line((34,648,112,648),fill=(108,240,231),width=4)
    d.text((34,660),subtitle.upper(),font=font(17,True),fill=(176,205,215))
    im.convert('RGB').save(ROOT/f'{name}.jpg',quality=86,optimize=True)

posters=[
('poster_bunny','Big Buck Bunny','Animasi • Demo',(21,59,74),(7,12,25),3,0),
('poster_flower','Flower Motion','Nature • Demo',(38,87,91),(13,18,39),7,1),
('poster_teaser','Bunny Preview','Cuplikan • Demo',(82,42,91),(12,18,43),12,2),
('poster_nature','Nature Flow','Visual • Demo',(29,82,58),(8,20,31),16,0),
('poster_cinema','Cinematic Test','Film • Demo',(75,49,67),(11,19,39),23,1),
('poster_studio','Studio Demo','Seri • Demo',(37,57,109),(13,15,29),27,2),
('poster_light','Frame & Light','Seri • Demo',(93,62,33),(18,17,34),33,0),
('poster_intro','Aether Intro','Film • Demo',(21,97,101),(13,17,34),38,1),
('poster_field','Visual Field','Seri • Demo',(70,36,113),(12,13,37),43,2),
('poster_episode','Demo Episode','Seri • Demo',(46,76,80),(12,24,32),49,0),
]
for x in posters: art(*x)
art('poster_default','Your Movie','Personal Cinema',(29,61,87),(11,17,35),52,1)

# Hero widescreen art
w,h=1600,760
im=bg(w,h,(11,44,67),(5,9,22)).convert('RGBA')
lay=Image.new('RGBA',(w,h));d=ImageDraw.Draw(lay,'RGBA')
random.seed(7)
for i in range(310):
    x=random.randrange(w);y=random.randrange(h)
    rad=random.choice([1,1,2,3,4])
    d.ellipse((x-rad,y-rad,x+rad,y+rad),fill=(177,237,255,random.randint(40,150)))
for i in range(13):
    r=170+i*39
    d.ellipse((1110-r,420-r,1110+r,420+r),outline=(87,233,222,max(8,115-8*i)),width=5)
d.ellipse((820,135,1450,765),fill=(30,175,187,33))
d.ellipse((940,260,1280,600),fill=(169,250,242,46),outline=(210,255,253,125),width=3)
for i in range(9):
    d.arc((715+i*6,120+i*6,1470-i*6,780-i*6),20,320,fill=(92,217,238,80),width=2)
im=Image.alpha_composite(im,lay)
im.convert('RGB').save(ROOT/'hero_cinema.jpg',quality=87,optimize=True)

# Icon for TV launcher (512 square, must be drawable file)
w=h=512
im=bg(w,h,(9,32,52),(8,15,26)).convert('RGBA');d=ImageDraw.Draw(im,'RGBA')
d.rounded_rectangle((40,40,472,472),radius=98,outline=(94,237,228),width=10,fill=(11,29,46,255))
d.ellipse((108,107,406,405),outline=(94,237,228),width=14)
d.polygon([(215,172),(215,342),(357,257)],fill=(94,237,228,255))
d.text((127,409),'AETHER',font=font(33,True),fill=(232,247,249,255))
im.convert('RGB').save(ROOT/'app_icon.png',optimize=True)

# Google TV banner 320x180
w,h=320,180
im=bg(w,h,(9,46,66),(8,11,27)).convert('RGBA');d=ImageDraw.Draw(im,'RGBA')
d.rounded_rectangle((11,12,309,169),radius=22,outline=(97,225,226,140),width=2)
d.ellipse((224,24,366,171),outline=(112,237,230,145),width=5)
d.polygon([(258,67),(258,123),(300,95)],fill=(97,237,228))
d.text((24,46),'AETHER',font=font(34,True),fill=(245,251,255))
d.text((26,97),'C I N E M A   T V',font=font(15,True),fill=(118,229,221))
im.convert('RGB').save(ROOT/'tv_banner.png',optimize=True)
print('Created',len(list(ROOT.iterdir())),'art assets')
