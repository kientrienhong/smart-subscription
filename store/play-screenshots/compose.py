"""Frame real screenshots for Google Play: 1080x1920 (9:16), caption on top, the screen running off the bottom edge."""
import sys
from PIL import Image, ImageDraw, ImageFont

shots, font_path, out = sys.argv[1:4]
W, H = 1080, 1920
GROUND, INK, LINE = (0xF3, 0xF4, 0xF8), (0x0A, 0x2A, 0x66), (0xDF, 0xE3, 0xEC)
font = ImageFont.truetype(font_path, 68)

FRAMES = [
    ("home", "See what renews this month, and when"),
    ("analytics", "What you paid, next to what's coming"),
    ("detail", "Mark a renewal when the charge lands"),
    ("archive", "Cancelled ones keep their lifetime cost"),
    ("widget", "This week's renewals on your home screen"),
]

def wrap(draw, text, width):
    lines, line = [], ""
    for word in text.split():
        trial = (line + " " + word).strip()
        if draw.textlength(trial, font=font) <= width:
            line = trial
        else:
            lines.append(line)
            line = word
    return lines + [line]

for i, (name, caption) in enumerate(FRAMES, 1):
    canvas = Image.new("RGB", (W, H), GROUND)
    draw = ImageDraw.Draw(canvas)
    y = 120
    for line in wrap(draw, caption, W - 180):
        draw.text((90, y), line, font=font, fill=INK)
        y += 82

    shot_w, top, radius = 860, 400, 44
    shot = Image.open(f"{shots}/{name}.png").convert("RGB")
    shot = shot.resize((shot_w, shot.height * shot_w // shot.width), Image.LANCZOS)
    x = (W - shot_w) // 2
    mask = Image.new("L", shot.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, shot.width - 1, shot.height - 1), radius, fill=255)
    # A 3px line around the screen keeps its edge readable where the app's own background is the same grey.
    draw.rounded_rectangle((x - 3, top - 3, x + shot_w + 2, top + shot.height + 2), radius + 3, fill=LINE)
    canvas.paste(shot, (x, top), mask)
    canvas.save(f"{out}/{i:02d}-{name}.png", optimize=True)
