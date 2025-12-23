#!/usr/bin/env python3
"""
Create a demonstration video for FOREGROUND_SERVICE_DATA_SYNC permission
This script creates a video with text overlays explaining the permission
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

def create_text_image(text, output_file, width=1920, height=1080, 
                     bg_color='0x1a1a1a', text_color='white', font_size=60):
    """Create an image with text using ffmpeg"""
    cmd = [
        'ffmpeg',
        '-y',  # Overwrite output
        '-f', 'lavfi',
        '-i', f'color=c={bg_color}:s={width}x{height}:d=1',
        '-vf', f"drawtext=text='{text}':fontsize={font_size}:fontcolor={text_color}:x=(w-text_w)/2:y=(h-text_h)/2",
        '-frames:v', '1',
        output_file
    ]
    subprocess.run(cmd, check=True, capture_output=True)

def create_video_from_images(image_files, output_video, fps=1, duration=5):
    """Create video from sequence of images"""
    # Create file list for concat
    with open('image_list.txt', 'w') as f:
        for img in image_files:
            f.write(f"file '{img}'\n")
            f.write(f"duration {duration}\n")
        # Repeat last frame
        if image_files:
            f.write(f"file '{image_files[-1]}'\n")
    
    cmd = [
        'ffmpeg',
        '-y',
        '-f', 'concat',
        '-safe', '0',
        '-i', 'image_list.txt',
        '-vf', f'fps={fps}',
        '-pix_fmt', 'yuv420p',
        '-c:v', 'libx264',
        output_video
    ]
    subprocess.run(cmd, check=True, capture_output=True)
    os.remove('image_list.txt')

def main():
    if not check_ffmpeg():
        print("Error: ffmpeg not found. Please install ffmpeg first.")
        print("Download from: https://ffmpeg.org/download.html")
        sys.exit(1)
    
    print("Creating demonstration video for FOREGROUND_SERVICE_DATA_SYNC permission...")
    
    # Create output directory
    os.makedirs('frames', exist_ok=True)
    
    # Define slides with text
    slides = [
        ("FOREGROUND_SERVICE_DATA_SYNC", "Permission Demonstration", 60, 40),
        ("Android 14+ Requirement", "Required for foreground services with dataSync type", 50, 35),
        ("Used in RestoreBackupService", "Allows backup restore to run in background", 50, 35),
        ("Automatic Permission", "No user approval needed - auto-granted", 50, 35),
        ("Essential for Backup Restore", "Without it, restore fails on Android 14+", 50, 35),
    ]
    
    image_files = []
    
    for i, (title, subtitle, title_size, subtitle_size) in enumerate(slides):
        output_file = f'frames/slide_{i:02d}.png'
        
        # Create image with title and subtitle
        cmd = [
            'ffmpeg',
            '-y',
            '-f', 'lavfi',
            '-i', 'color=c=0x1a1a1a:s=1920x1080:d=1',
            '-vf', f"drawtext=text='{title}':fontsize={title_size}:fontcolor=white:x=(w-text_w)/2:y=400,drawtext=text='{subtitle}':fontsize={subtitle_size}:fontcolor=#4CAF50:x=(w-text_w)/2:y=500",
            '-frames:v', '1',
            output_file
        ]
        subprocess.run(cmd, check=True, capture_output=True)
        image_files.append(output_file)
        print(f"Created slide {i+1}/{len(slides)}")
    
    # Create video
    output_video = '../FOREGROUND_SERVICE_DATA_SYNC_DEMO.mp4'
    print(f"\nCreating video: {output_video}")
    create_video_from_images(image_files, output_video, fps=1, duration=5)
    
    print(f"\n[SUCCESS] Video created successfully: {output_video}")
    print("\nNote: This is a basic demonstration. For a complete video, you would:")
    print("1. Take screenshots of the actual code")
    print("2. Show the app in action")
    print("3. Demonstrate the service running")
    print("\nSee FOREGROUND_SERVICE_DATA_SYNC_DEMONSTRATION.md for detailed script.")

if __name__ == '__main__':
    main()

