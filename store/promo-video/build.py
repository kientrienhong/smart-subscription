"""Builds the 9:16 promo video from raw emulator recordings (adb screenrecord, 1080x2400).

    python3 build.py RECORDINGS_DIR ICON_PNG OUT_MP4 [MUSIC_WAV]

RECORDINGS_DIR holds s1.mp4 ... s5.mp4 (see SCENES). Each scene gets the Play screenshots' layout:
caption on top, the real screen below running off the bottom edge. MUSIC_WAV (for example
../music/bed-28s.wav from ../music/compose.py) becomes the soundtrack; without it the audio track
is silent. Needs ffmpeg and Pillow.
"""
import os
import subprocess
import sys
import tempfile

from PIL import Image, ImageDraw, ImageFont

rec_dir, icon_path, out_path = sys.argv[1:4]
music_path = sys.argv[4] if len(sys.argv) > 4 else None
here = os.path.dirname(os.path.abspath(__file__))
fonts = os.path.join(here, "..", "..", "app", "src", "main", "res", "font")

W, H, FPS = 1080, 1920, 30
GROUND, INK, TEXT2, LINE = (0xF3, 0xF4, 0xF8), (0x0A, 0x2A, 0x66), (0x5B, 0x64, 0x78), (0xDF, 0xE3, 0xEC)
SHOT_W, SHOT_H, SHOT_X, SHOT_Y, RADIUS = 860, 1912, 110, 400, 44

# (recording, caption, seconds to skip, length). Skips cut the idle frames before each tap;
# a recording shorter than its length holds its last frame.
SCENES = [
    ("s1", "See what renews this month, and when", 0.0, 6.0),
    ("s2", "Mark a renewal when the charge lands", 0.0, 6.5),
    ("s3", "What you paid, next to what's coming", 0.5, 5.8),
    ("s4", "Cancelled ones keep their lifetime cost", 0.95, 3.5),
    ("s5", "This week's renewals on your home screen", 0.9, 3.5),
]
END_SECONDS = 3.0
MUSIC_GAIN = 0.7  # about -3 dB: the bed is the only sound here, so it sits near -19 LUFS


def font(weight, size):
    return ImageFont.truetype(os.path.join(fonts, f"bricolage_{weight}.ttf"), size)


def wrap(draw, text, f, width):
    lines, line = [], ""
    for word in text.split():
        trial = (line + " " + word).strip()
        if draw.textlength(trial, font=f) <= width:
            line = trial
        else:
            lines.append(line)
            line = word
    return lines + [line]


def scene_background(caption, path):
    canvas = Image.new("RGB", (W, H), GROUND)
    draw = ImageDraw.Draw(canvas)
    f, y = font(800, 68), 120
    for line in wrap(draw, caption, f, W - 180):
        draw.text((90, y), line, font=f, fill=INK)
        y += 82
    draw.rounded_rectangle((SHOT_X - 3, SHOT_Y - 3, SHOT_X + SHOT_W + 2, SHOT_Y + SHOT_H + 2), RADIUS + 3, fill=LINE)
    canvas.save(path)


def end_card(path):
    canvas = Image.new("RGB", (W, H), GROUND)
    draw = ImageDraw.Draw(canvas)
    icon = Image.open(icon_path).convert("RGB").resize((320, 320), Image.LANCZOS)
    mask = Image.new("L", icon.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, 319, 319), 72, fill=255)
    canvas.paste(icon, ((W - 320) // 2, 560), mask)
    for text, f, color, y in [
        ("SmartSubscription", font(800, 84), INK, 960),
        ("Know what renews, and what it costs you.", font(500, 44), TEXT2, 1080),
        ("No account. No bank link.", font(500, 44), TEXT2, 1140),
    ]:
        draw.text(((W - draw.textlength(text, font=f)) / 2, y), text, font=f, fill=color)
    canvas.save(path)


def run(*args):
    subprocess.run(["ffmpeg", "-v", "error", "-y", *args], check=True)


with tempfile.TemporaryDirectory() as tmp:
    mask_path = os.path.join(tmp, "mask.png")
    mask = Image.new("L", (SHOT_W, SHOT_H), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, SHOT_W - 1, SHOT_H - 1), RADIUS, fill=255)
    mask.save(mask_path)

    parts = []
    for name, caption, skip, length in SCENES:
        bg = os.path.join(tmp, f"{name}.png")
        scene_background(caption, bg)
        part = os.path.join(tmp, f"{name}.mp4")
        run(
            "-loop", "1", "-i", bg, "-i", os.path.join(rec_dir, f"{name}.mp4"), "-loop", "1", "-i", mask_path,
            "-filter_complex",
            f"[1:v]trim=start={skip},setpts=PTS-STARTPTS,fps={FPS},scale={SHOT_W}:{SHOT_H},"
            f"tpad=stop_mode=clone:stop_duration={length},trim=duration={length},format=rgba[v];"
            f"[2:v]format=gray[m];[v][m]alphamerge[vm];"
            f"[0:v][vm]overlay={SHOT_X}:{SHOT_Y},fps={FPS},format=yuv420p",
            "-t", str(length), "-c:v", "libx264", "-crf", "18", "-preset", "slow", part,
        )
        parts.append(part)

    card = os.path.join(tmp, "end.png")
    end_card(card)
    end = os.path.join(tmp, "end.mp4")
    run("-loop", "1", "-i", card, "-t", str(END_SECONDS), "-vf", f"fps={FPS},format=yuv420p",
        "-c:v", "libx264", "-crf", "18", "-preset", "slow", end)
    parts.append(end)

    listing = os.path.join(tmp, "parts.txt")
    with open(listing, "w") as f:
        f.writelines(f"file '{p}'\n" for p in parts)
    # Without music, a silent track: some upload paths reject video with no audio stream at all.
    audio = ["-i", music_path, "-af", f"volume={MUSIC_GAIN}"] if music_path else ["-f", "lavfi", "-i", "anullsrc=r=48000:cl=stereo"]
    run("-f", "concat", "-safe", "0", "-i", listing, *audio,
        "-shortest", "-c:v", "copy", "-c:a", "aac", "-b:a", "192k", "-movflags", "+faststart", out_path)
