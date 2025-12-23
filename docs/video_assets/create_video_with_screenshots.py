#!/usr/bin/env python3
"""
Create a demonstration video for FOREGROUND_SERVICE_DATA_SYNC permission
Uses screenshots from emulator and adds text overlays
"""

import subprocess
import os
import sys

def check_ffmpeg():
    """Check if ffmpeg is available"""
    try:
        result = subprocess.run(['ffmpeg', '-version'], 
                              capture_output=True, text=True)
        return result.returncode == 0
    except FileNotFoundError:
        return False

def create_text_slide(text_lines, output_file, width=1920, height=1080, 
                     bg_color='0x1a1a1a', text_color='white', title_size=60, subtitle_size=40):
    """Create an image with multiple lines of text using ffmpeg"""
    # Build drawtext filter with multiple text lines
    drawtext_filters = []
    y_offset = 300
    
    for i, line in enumerate(text_lines):
        if i == 0:
            # Title line
            drawtext_filters.append(
                f"drawtext=text='{line}':fontsize={title_size}:fontcolor={text_color}:x=(w-text_w)/2:y={y_offset}"
            )
            y_offset += title_size + 20
        else:
            # Subtitle lines
            drawtext_filters.append(
                f"drawtext=text='{line}':fontsize={subtitle_size}:fontcolor=#4CAF50:x=(w-text_w)/2:y={y_offset}"
            )
            y_offset += subtitle_size + 15
    
    vf_filter = ','.join(drawtext_filters)
    
    cmd = [
        'ffmpeg',
        '-y',
        '-f', 'lavfi',
        '-i', f'color=c={bg_color}:s={width}x{1080}:d=1',
        '-vf', vf_filter,
        '-frames:v', '1',
        output_file
    ]
    subprocess.run(cmd, check=True, capture_output=True)

def add_text_overlay_to_image(input_image, text_lines, output_file, 
                              text_color='white', title_size=50, subtitle_size=30):
    """Add text overlay to an existing screenshot"""
    drawtext_filters = []
    y_offset = 50
    
    for i, line in enumerate(text_lines):
        if i == 0:
            # Title - centered at top
            drawtext_filters.append(
                f"drawtext=text='{line}':fontsize={title_size}:fontcolor={text_color}:x=(w-text_w)/2:y={y_offset}:box=1:boxcolor=0x000000@0.7:boxborderw=10"
            )
            y_offset += title_size + 30
        else:
            # Subtitle - centered
            drawtext_filters.append(
                f"drawtext=text='{line}':fontsize={subtitle_size}:fontcolor=#4CAF50:x=(w-text_w)/2:y={y_offset}:box=1:boxcolor=0x000000@0.7:boxborderw=5"
            )
            y_offset += subtitle_size + 20
    
    vf_filter = ','.join(drawtext_filters)
    
    cmd = [
        'ffmpeg',
        '-y',
        '-i', input_image,
        '-vf', vf_filter,
        output_file
    ]
    subprocess.run(cmd, check=True, capture_output=True)

def create_video_from_images(image_files, output_video, fps=1, duration=5):
    """Create video from sequence of images"""
    # Create file list for concat
    with open('image_list.txt', 'w') as f:
        for img in image_files:
            if os.path.exists(img):
                f.write(f"file '{os.path.abspath(img)}'\n")
                f.write(f"duration {duration}\n")
        # Repeat last frame
        if image_files and os.path.exists(image_files[-1]):
            f.write(f"file '{os.path.abspath(image_files[-1])}'\n")
    
    cmd = [
        'ffmpeg',
        '-y',
        '-f', 'concat',
        '-safe', '0',
        '-i', 'image_list.txt',
        '-vf', f'fps={fps},scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2',
        '-pix_fmt', 'yuv420p',
        '-c:v', 'libx264',
        output_video
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"FFmpeg error: {result.stderr}")
        return False
    
    if os.path.exists('image_list.txt'):
        os.remove('image_list.txt')
    return True

def main():
    if not check_ffmpeg():
        print("Error: ffmpeg not found. Please install ffmpeg first.")
        print("Download from: https://ffmpeg.org/download.html")
        sys.exit(1)
    
    print("Creating demonstration video for FOREGROUND_SERVICE_DATA_SYNC permission...")
    
    # Create output directory
    os.makedirs('frames', exist_ok=True)
    
    image_files = []
    
    # Slide 1: Title slide
    print("Creating slide 1: Title...")
    create_text_slide(
        ["FOREGROUND_SERVICE_DATA_SYNC", "Permission Demonstration", "QKSMS Backup Restore Feature"],
        'frames/slide_01_title.png'
    )
    image_files.append('frames/slide_01_title.png')
    
    # Slide 2: What is it?
    print("Creating slide 2: What is it?...")
    create_text_slide(
        ["What is FOREGROUND_SERVICE_DATA_SYNC?", 
         "Required permission for Android 14+ (API 34+)",
         "Allows foreground services with dataSync type",
         "Essential for background data operations"],
        'frames/slide_02_what.png'
    )
    image_files.append('frames/slide_02_what.png')
    
    # Slide 3: Why needed?
    print("Creating slide 3: Why needed?...")
    create_text_slide(
        ["Why is it needed?",
         "RestoreBackupService runs as foreground service",
         "Synchronizes backup data to local database",
         "Without permission, service fails on Android 14+"],
        'frames/slide_03_why.png'
    )
    image_files.append('frames/slide_03_why.png')
    
    # Slide 4: Screenshot of backup screen (if available)
    screenshot_path = 'screenshot_01_backup_screen.png'
    if os.path.exists(screenshot_path):
        print("Creating slide 4: Backup screen screenshot...")
        add_text_overlay_to_image(
            screenshot_path,
            ["Backup & Restore Screen", "Tap 'Restore' to start the service"],
            'frames/slide_04_backup_screen.png'
        )
        image_files.append('frames/slide_04_backup_screen.png')
    else:
        print("Screenshot not found, creating text slide instead...")
        create_text_slide(
            ["Backup & Restore Screen",
             "Navigate to: Drawer Menu -> Backup",
             "Tap 'Restore' button to start restore operation"],
            'frames/slide_04_backup_screen.png'
        )
        image_files.append('frames/slide_04_backup_screen.png')
    
    # Slide 5: Service running
    print("Creating slide 5: Service running...")
    create_text_slide(
        ["Service Running",
         "RestoreBackupService starts as foreground service",
         "Shows persistent notification with progress",
         "Continues even when app is backgrounded"],
        'frames/slide_05_service.png'
    )
    image_files.append('frames/slide_05_service.png')
    
    # Slide 6: Code example - Manifest
    print("Creating slide 6: Manifest declaration...")
    create_text_slide(
        ["AndroidManifest.xml",
         "Permission: FOREGROUND_SERVICE_DATA_SYNC",
         "Service: foregroundServiceType='dataSync'",
         "Both must be declared for Android 14+"],
        'frames/slide_06_manifest.png'
    )
    image_files.append('frames/slide_06_manifest.png')
    
    # Slide 7: Code example - Service
    print("Creating slide 7: Service code...")
    create_text_slide(
        ["RestoreBackupService.kt",
         "startForeground() - becomes foreground service",
         "dataSync type - indicates data synchronization",
         "Permission auto-granted - no user approval needed"],
        'frames/slide_07_service_code.png'
    )
    image_files.append('frames/slide_07_service_code.png')
    
    # Slide 8: Summary
    print("Creating slide 8: Summary...")
    create_text_slide(
        ["Summary",
         "Permission required for Android 14+",
         "Enables backup restore to work reliably",
         "Automatically granted - transparent to users"],
        'frames/slide_08_summary.png'
    )
    image_files.append('frames/slide_08_summary.png')
    
    # Create video
    output_video = '../FOREGROUND_SERVICE_DATA_SYNC_DEMO.mp4'
    print(f"\nCreating video: {output_video}")
    if create_video_from_images(image_files, output_video, fps=1, duration=5):
        print(f"\n[SUCCESS] Video created successfully: {output_video}")
        file_size = os.path.getsize(output_video) if os.path.exists(output_video) else 0
        print(f"Video size: {file_size / 1024 / 1024:.2f} MB")
    else:
        print("\n[ERROR] Failed to create video")
        sys.exit(1)

if __name__ == '__main__':
    main()

