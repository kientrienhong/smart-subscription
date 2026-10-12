"""Synthesizes an original instrumental bed for the promo videos, so there is no music license to check.

    python3 compose.py SECONDS OUT.wav

A I-vi-IV-V progression in C at about 100 BPM: electric-piano chords, round bass, a quiet plucked
arpeggio, soft kick and brushed hats. It starts with two bars of keys alone, drops the drums for
the last two bars and lands on a final C chord that rings out at SECONDS. Needs numpy only.
"""
import sys
import wave

import numpy as np

SR = 44100
BPM = 100
BEAT = 60 / BPM
BAR = 4 * BEAT
rng = np.random.default_rng(7)  # fixed seed: same track every run


def hz(midi):
    return 440.0 * 2 ** ((midi - 69) / 12)


# One bar each. Voicings stay in the middle register so they sit under a voice, not on it.
PROGRESSION = [
    ("Cmaj7", 48, [60, 64, 67, 71]),
    ("Am7", 45, [57, 64, 67, 72]),
    ("Fmaj7", 41, [57, 60, 64, 65]),
    ("G6", 43, [59, 62, 64, 67]),
]


def env(n, attack, decay):
    t = np.arange(n) / SR
    return np.minimum(t / attack, 1.0) * np.exp(-t / decay)


def epiano(freq, length, vel):
    """FM electric piano: a sine carrier whose bell-like overtones fade faster than the tone."""
    n = int(length * SR)
    t = np.arange(n) / SR
    index = 1.8 * np.exp(-t / 0.25)
    tone = np.sin(2 * np.pi * freq * t + index * np.sin(2 * np.pi * freq * t))
    tremolo = 1 + 0.06 * np.sin(2 * np.pi * 4.5 * t)
    return vel * tone * tremolo * env(n, 0.004, 0.9)


def bass(freq, length, vel):
    n = int(length * SR)
    t = np.arange(n) / SR
    tone = np.sin(2 * np.pi * freq * t) + 0.25 * np.sin(4 * np.pi * freq * t)
    return vel * np.tanh(1.5 * tone) * env(n, 0.01, 0.6)


def pluck(freq, length, vel):
    """Karplus-Strong string."""
    n = int(length * SR)
    period = int(SR / freq)
    buf = rng.uniform(-1, 1, period)
    out = np.empty(n)
    for i in range(n):
        out[i] = buf[i % period]
        buf[i % period] = 0.996 * 0.5 * (buf[i % period] + buf[(i + 1) % period])
    return vel * out


def kick(vel):
    n = int(0.35 * SR)
    t = np.arange(n) / SR
    pitch = 50 + 70 * np.exp(-t / 0.03)
    return vel * np.sin(2 * np.pi * np.cumsum(pitch) / SR) * np.exp(-t / 0.12)


def brush(vel):
    n = int(0.12 * SR)
    noise = rng.uniform(-1, 1, n)
    noise = np.diff(noise, prepend=0)  # high-pass: keep the hiss, lose the thump
    noise = np.convolve(noise, np.ones(6) / 6, mode="same")  # then dull the very top so it brushes, not ticks
    return vel * noise * np.exp(-np.arange(n) / SR / 0.035)


def add(track, sound, at):
    i = int(at * SR)
    j = min(len(track), i + len(sound))
    if i < len(track):
        track[i:j] += sound[: j - i]


def reverb(x, seconds=1.6, mix=0.18):
    """Convolution with decaying noise, done in the frequency domain."""
    ir = rng.normal(0, 1, int(seconds * SR)) * np.exp(-np.arange(int(seconds * SR)) / SR / (seconds / 5))
    ir /= np.sqrt(np.sum(ir**2))
    size = 1 << int(np.ceil(np.log2(len(x) + len(ir))))
    wet = np.fft.irfft(np.fft.rfft(x, size) * np.fft.rfft(ir, size), size)[: len(x)]
    return (1 - mix) * x + mix * wet


def compose(total):
    global BEAT, BAR
    n = int(total * SR)
    keys, low, plucks, drums = (np.zeros(n) for _ in range(4))
    # The final chord lands about 2.5 s before the end so it can ring out. The tempo bends a few
    # percent around 100 BPM so a whole number of bars fills the time before it.
    final_at = total - 2.5
    bars = max(4, round(final_at / BAR))
    BAR = final_at / bars
    BEAT = BAR / 4
    start = 0.0

    for b in range(bars):
        t0 = start + b * BAR
        _, root, chord = PROGRESSION[b % len(PROGRESSION)]
        groove = 2 <= b < bars - 2  # drums and bass sit out the first two and last two bars
        # Keys: on 1 and on the "and" of 2, a light syncopation.
        for at, vel in [(0, 0.16), (1.5 * BEAT, 0.11)]:
            for k, note in enumerate(chord):
                add(keys, epiano(hz(note), BAR, vel), t0 + at + k * 0.008)
        if b >= 1:
            for k in range(8):
                note = chord[[0, 2, 1, 3, 2, 1, 3, 2][k]] + 12
                add(plucks, pluck(hz(note), BEAT, 0.05), t0 + k * BEAT / 2)
        if groove:
            add(low, bass(hz(root), 1.4 * BEAT, 0.32), t0)
            add(low, bass(hz(root), 0.9 * BEAT, 0.24), t0 + 2.5 * BEAT)
            add(drums, kick(0.45), t0)
            add(drums, kick(0.35), t0 + 2 * BEAT)
            for k in range(4):
                add(drums, brush(0.035), t0 + (k + 0.5) * BEAT)

    for note in [48, 60, 64, 67, 71, 74]:
        add(keys, epiano(hz(note), 2.5, 0.14), final_at)

    mix = reverb(keys + 0.8 * plucks) + low + drums
    fade = int(0.6 * SR)
    mix[-fade:] *= np.linspace(1, 0, fade)
    mix[: int(0.05 * SR)] *= np.linspace(0, 1, int(0.05 * SR))
    return mix / np.max(np.abs(mix)) * 0.89  # about -1 dBFS peak


def main():
    seconds, out = float(sys.argv[1]), sys.argv[2]
    mono = compose(seconds)
    # Slight width: the plucked line and reverb tail differ a little between channels.
    stereo = np.stack([mono, np.roll(mono, int(0.012 * SR))], axis=1)
    pcm = (np.clip(stereo, -1, 1) * 32767).astype("<i2")
    with wave.open(out, "wb") as w:
        w.setnchannels(2)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(pcm.tobytes())


if __name__ == "__main__":
    main()
