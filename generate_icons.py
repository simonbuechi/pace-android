import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def make_gradient(width, height, c1=(145, 35, 166), c2=(215, 25, 95)):
    # 45-degree diagonal gradient from top-left to bottom-right
    img = Image.new("RGBA", (width, height))
    pixels = img.load()
    for y in range(height):
        for x in range(width):
            t = (x / width + y / height) / 2.0
            r = int(c1[0] + t * (c2[0] - c1[0]))
            g = int(c1[1] + t * (c2[1] - c1[1]))
            b = int(c1[2] + t * (c2[2] - c1[2]))
            pixels[x, y] = (r, g, b, 255)
    return img

def extract_emblem(logo_path):
    logo = Image.open(logo_path).convert("RGB")
    w, h = logo.size
    emblem = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for y in range(h):
        for x in range(w):
            r, g, b = logo.getpixel((x, y))
            bg_g = 35 - 10 * (x + y) / (w + h)
            alpha = max(0.0, min(1.0, (g - bg_g) / (255.0 - bg_g)))
            if alpha > 0.05:
                a_byte = int(pow(alpha, 0.9) * 255)
                emblem.putpixel((x, y), (255, 255, 255, a_byte))
    bbox = emblem.getbbox()
    return emblem.crop(bbox)

def create_round_mask(size):
    # 4x supersampling for smooth antialiased circle
    scale = 4
    mask = Image.new("L", (size * scale, size * scale), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size * scale - 1, size * scale - 1), fill=255)
    return mask.resize((size, size), Image.Resampling.LANCZOS)

def create_rounded_rect_mask(size, radius):
    scale = 4
    mask = Image.new("L", (size * scale, size * scale), 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle((0, 0, size * scale - 1, size * scale - 1), radius=radius * scale, fill=255)
    return mask.resize((size, size), Image.Resampling.LANCZOS)

def main():
    base_res = "app/src/main/res"
    logo_path = os.path.join(base_res, "drawable", "logo.webp")
    emblem = extract_emblem(logo_path)
    ew, eh = emblem.size
    aspect = eh / ew

    # Mipmap densities and sizes
    # (density, launcher_size, adaptive_size)
    densities = [
        ("mipmap-mdpi", 48, 108),
        ("mipmap-hdpi", 72, 162),
        ("mipmap-xhdpi", 96, 216),
        ("mipmap-xxhdpi", 144, 324),
        ("mipmap-xxxhdpi", 192, 432),
    ]

    for folder, l_sz, a_sz in densities:
        out_dir = os.path.join(base_res, folder)
        os.makedirs(out_dir, exist_ok=True)

        # 1. Adaptive foreground (108dp canvas, emblem ~58% width to sit safely inside 66dp zone)
        fg = Image.new("RGBA", (a_sz, a_sz), (0, 0, 0, 0))
        target_w = int(a_sz * 0.58)
        target_h = int(target_w * aspect)
        scaled_emblem = emblem.resize((target_w, target_h), Image.Resampling.LANCZOS)
        offset_x = (a_sz - target_w) // 2
        offset_y = (a_sz - target_h) // 2
        fg.paste(scaled_emblem, (offset_x, offset_y), scaled_emblem)
        fg.save(os.path.join(out_dir, "ic_launcher_foreground.png"), "PNG")

        # 2. Legacy launcher icon (rounded square with soft corner radius)
        bg = make_gradient(l_sz, l_sz)
        l_target_w = int(l_sz * 0.62)
        l_target_h = int(l_target_w * aspect)
        l_emblem = emblem.resize((l_target_w, l_target_h), Image.Resampling.LANCZOS)
        l_ox = (l_sz - l_target_w) // 2
        l_oy = (l_sz - l_target_h) // 2
        bg.paste(l_emblem, (l_ox, l_oy), l_emblem)
        
        # Apply rounded corner mask for legacy square
        sq_mask = create_rounded_rect_mask(l_sz, int(l_sz * 0.22))
        sq_icon = Image.new("RGBA", (l_sz, l_sz), (0, 0, 0, 0))
        sq_icon.paste(bg, (0, 0), sq_mask)
        sq_icon.save(os.path.join(out_dir, "ic_launcher.png"), "PNG")

        # 3. Legacy round icon
        round_mask = create_round_mask(l_sz)
        round_icon = Image.new("RGBA", (l_sz, l_sz), (0, 0, 0, 0))
        round_icon.paste(bg, (0, 0), round_mask)
        round_icon.save(os.path.join(out_dir, "ic_launcher_round.png"), "PNG")
        print(f"Generated icons for {folder}: adaptive={a_sz}x{a_sz}, launcher={l_sz}x{l_sz}")

    # Play Store 512x512 High-Res Icon
    os.makedirs("playstore", exist_ok=True)
    ps_sz = 512
    ps_bg = make_gradient(ps_sz, ps_sz)
    ps_tw = int(ps_sz * 0.60)
    ps_th = int(ps_tw * aspect)
    ps_emblem = emblem.resize((ps_tw, ps_th), Image.Resampling.LANCZOS)
    ps_ox = (ps_sz - ps_tw) // 2
    ps_oy = (ps_sz - ps_th) // 2
    ps_bg.paste(ps_emblem, (ps_ox, ps_oy), ps_emblem)
    ps_bg.save(os.path.join("playstore", "icon-512.png"), "PNG")
    print("Generated playstore/icon-512.png (512x512)")

    # Play Store 1024x500 Feature Graphic
    fg_w, fg_h = 1024, 500
    feat_img = make_gradient(fg_w, fg_h)
    
    # Add subtle background decorative glow rings
    overlay = Image.new("RGBA", (fg_w, fg_h), (0, 0, 0, 0))
    ov_draw = ImageDraw.Draw(overlay)
    ov_draw.ellipse((-50, -50, 450, 450), outline=(255, 255, 255, 18), width=3)
    ov_draw.ellipse((-100, -100, 500, 500), outline=(255, 255, 255, 12), width=2)
    ov_draw.ellipse((fg_w - 200, fg_h - 200, fg_w + 200, fg_h + 200), outline=(255, 255, 255, 15), width=2)
    feat_img.paste(overlay, (0, 0), overlay)

    # Place emblem on the left
    f_emblem_w = 320
    f_emblem_h = int(f_emblem_w * aspect)
    f_emblem = emblem.resize((f_emblem_w, f_emblem_h), Image.Resampling.LANCZOS)
    feat_img.paste(f_emblem, (90, (fg_h - f_emblem_h) // 2), f_emblem)

    # Place typography on the right
    draw = ImageDraw.Draw(feat_img)
    fonts_dir = os.path.join(os.environ.get("WINDIR", "C:\\Windows"), "Fonts")
    bold_font_path = os.path.join(fonts_dir, "segoeuib.ttf")
    reg_font_path = os.path.join(fonts_dir, "segoeui.ttf")
    if not os.path.exists(bold_font_path):
        bold_font_path = os.path.join(fonts_dir, "arialbd.ttf")
        reg_font_path = os.path.join(fonts_dir, "arial.ttf")

    title_font = ImageFont.truetype(bold_font_path, 80)
    subtitle_font = ImageFont.truetype(reg_font_path, 34)
    tagline_font = ImageFont.truetype(reg_font_path, 22)

    text_x = 460
    draw.text((text_x, 140), "Pace", font=title_font, fill=(255, 255, 255, 255))
    draw.text((text_x, 245), "Interval & Workout Timer", font=subtitle_font, fill=(255, 245, 250, 240))
    draw.text((text_x, 298), "Custom Routines  •  Minimalist Design  •  Pure Focus", font=tagline_font, fill=(255, 235, 245, 200))

    feat_img.save(os.path.join("playstore", "feature-graphic-1024x500.png"), "PNG")
    print("Generated playstore/feature-graphic-1024x500.png (1024x500)")

if __name__ == "__main__":
    main()
