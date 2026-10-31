"""Original procedural lo-fi instrumental for Foco, no samples or downloads.
Requires numpy and ffmpeg on the build workstation, never on the phone.
"""
from pathlib import Path
import subprocess
import wave
import numpy as np

rate=22050
beat=60/68
duration=64*beat
length=round(duration*rate)
rng=np.random.default_rng(51)
song=np.zeros((length,2),dtype=np.float64)

def add(sound,when,level=1,pan=0):
    at=round(when*rate)
    idx=(np.arange(len(sound))+at)%length
    song[idx,0]+=sound*level*(1-pan*.3)
    song[idx,1]+=sound*level*(1+pan*.3)

def key(note,seconds=3.7):
    t=np.arange(round(seconds*rate))/rate
    f=440*2**((note-69)/12)
    env=(1-np.exp(-t*65))*np.exp(-t/1.15)
    return env*(np.sin(2*np.pi*f*t)+.24*np.sin(2*np.pi*f*2*t)*np.exp(-t*2)+.08*np.sin(2*np.pi*f*3*t))

# Warm major seventh / minor ninth progression, gently swung keys and soft drums.
chords=[[48,55,59,64],[45,52,55,59],[41,48,52,57],[43,50,53,57]]
for bar in range(16):
    chord=chords[bar%4]
    for offset,vol in [(0,.055),(2.55,.033)]:
        for i,note in enumerate(chord):
            when=(bar*4+offset)*beat+i*.027
            sound=key(note+12)
            add(sound,when,vol,(i-1.5)/2)
            add(sound,when+beat*.75,vol*.12,-(i-1.5)/2)
    t=np.arange(round(1.7*rate))/rate
    bass=np.sin(2*np.pi*(440*2**((chord[0]-12-69)/12))*t)*(1-np.exp(-t*35))*np.exp(-t*2)
    add(bass,bar*4*beat,.075)
    for b in [0,2.5]:
        t=np.arange(round(.22*rate))/rate
        kick=np.sin(2*np.pi*(44*t+45*.035*(1-np.exp(-t/.035))))*np.exp(-t*23)*(1-np.exp(-t*350))
        add(kick,(bar*4+b)*beat,.06)
    for b in [1,3]:
        t=np.arange(round(.14*rate))/rate
        noise=rng.normal(0,1,len(t));noise=np.convolve(noise,np.ones(7)/7,mode='same')
        add(noise*np.exp(-t*40),(bar*4+b+.025)*beat,.025,.2)
    for b in np.arange(0,4,.5):
        t=np.arange(round(.04*rate))/rate
        hat=rng.normal(0,1,len(t));hat=np.diff(hat,prepend=0)
        add(hat*np.exp(-t*110),(bar*4+b+(.06 if b%1 else 0))*beat,.0025,-.3)

# Sparse melody, no vocal material.
for bar,note in enumerate([76,74,72,71,69,71,72,74,76,79,76,74,72,71,69,67]):
    add(key(note,2.8),(bar*4+1.6)*beat,.025,.25)

# Low-pass and gentle saturation; circular smoothing keeps the loop continuous.
for channel in range(2):
    s=song[:,channel]
    song[:,channel]=sum(np.roll(s,i) for i in range(5))/5
song=np.tanh(song*1.8)
song*=.65/max(.65,np.max(np.abs(song)))
root=Path(__file__).resolve().parents[1]
wav=root/'scripts/sendero.wav'
with wave.open(str(wav),'wb') as f:
    f.setnchannels(2);f.setsampwidth(2);f.setframerate(rate)
    f.writeframes((song*32767).astype('<i2').tobytes())
target=root/'app/src/main/res/raw/sendero.ogg'
target.parent.mkdir(parents=True,exist_ok=True)
subprocess.run(['ffmpeg','-y','-loglevel','error','-i',str(wav),'-c:a','libvorbis','-q:a','3',str(target)],check=True)
wav.unlink()
print(target, target.stat().st_size)
