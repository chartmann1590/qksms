#!/usr/bin/env python3
"""
Create a demonstration video showing FOREGROUND_SERVICE_DATA_SYNC in action
Uses screenshots from emulator with text overlays explaining the service
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

def add_text_overlay_to_image(input_image, text_lines, output_file, 
                              text_color='white', title_size=50, subtitle_size=30,
                              position='bottom'):
    """Add text overlay to an existing screenshot"""
    if not os.path.exists(input_image):
        print(f"Warning: Input image not found: {input_image}")
        return False
    
    drawtext_filters = []
    
    if position == 'bottom':
        # Calculate starting Y position from bottom
        y_start = 900  # Start near bottom
        y_offset = y_start
    else:
        # Top position
        y_offset = 50
    
    for i, line in enumerate(text_lines):
        # Escape single quotes and special characters for drawtext
        escaped_text = line.replace("'", "\\'").replace(":", "\\:")
        if i == 0:
            # Title - larger, bold
            drawtext_filters.append(
                f"drawtext=text='{escaped_text}':fontsize={title_size}:fontcolor={text_color}:"
                f"x=(w-text_w)/2:y={y_offset}:"
                f"box=1:boxcolor=0x000000@0.8:boxborderw=10"
            )
            y_offset += title_size + 20
        else:
            # Subtitle - smaller
            drawtext_filters.append(
                f"drawtext=text='{escaped_text}':fontsize={subtitle_size}:fontcolor=#4CAF50:"
                f"x=(w-text_w)/2:y={y_offset}:"
                f"box=1:boxcolor=0x000000@0.7:boxborderw=5"
            )
            y_offset += subtitle_size + 15
    
    vf_filter = ','.join(drawtext_filters)
    
    cmd = [
        'ffmpeg',
        '-y',
        '-i', input_image,
        '-vf', vf_filter,
        output_file
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Error adding text overlay: {result.stderr}")
        return False
    return True

def create_text_slide(text_lines, output_file, width=1920, height=1080, 
                     bg_color='0x1a1a1a', text_color='white', title_size=60, subtitle_size=40):
    """Create an image with multiple lines of text using ffmpeg"""
    drawtext_filters = []
    y_offset = 300
    
    for i, line in enumerate(text_lines):
        # Escape single quotes and special characters for drawtext
        escaped_text = line.replace("'", "\\'").replace(":", "\\:")
        if i == 0:
            drawtext_filters.append(
                f"drawtext=text='{escaped_text}':fontsize={title_size}:fontcolor={text_color}:x=(w-text_w)/2:y={y_offset}"
            )
            y_offset += title_size + 20
        else:
            drawtext_filters.append(
                f"drawtext=text='{escaped_text}':fontsize={subtitle_size}:fontcolor=#4CAF50:x=(w-text_w)/2:y={y_offset}"
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

def create_video_from_images(image_files, output_video, fps=1, duration=6):
    """Create video from sequence of images"""
    # Filter out non-existent files
    existing_files = [f for f in image_files if os.path.exists(f)]
    
    if not existing_files:
        print("Error: No image files found")
        return False
    
    # Create file list for concat
    with open('image_list.txt', 'w') as f:
        for img in existing_files:
            abs_path = os.path.abspath(img)
            f.write(f"file '{abs_path}'\n")
            f.write(f"duration {duration}\n")
        # Repeat last frame
        if existing_files:
            f.write(f"file '{os.path.abspath(existing_files[-1])}'\n")
    
    cmd = [
        'ffmpeg',
        '-y',
        '-f', 'concat',
        '-safe', '0',
        '-i', 'image_list.txt',
        '-vf', 'fps=1,scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2:color=black',
        '-pix_fmt', 'yuv420p',
        '-c:v', 'libx264',
        '-preset', 'medium',
        output_video
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"FFmpeg error: {result.stderr}")
        if os.path.exists('image_list.txt'):
            os.remove('image_list.txt')
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
    print("Using screenshots from emulator with explanatory text overlays\n")
    
    # Create output directory
    os.makedirs('frames', exist_ok=True)
    
    image_files = []
    
    # Slide 1: Title
    print("Creating slide 1: Title...")
    create_text_slide(
        ["FOREGROUND_SERVICE_DATA_SYNC", "Backup Restore Demonstration", "QKSMS Messenger"],
        'frames/slide_01_title.png'
    )
    image_files.append('frames/slide_01_title.png')
    
    # Slide 2: Introduction
    print("Creating slide 2: Introduction...")
    create_text_slide(
        ["What We'll Demonstrate",
         "How backup restore uses foreground service",
         "Why FOREGROUND_SERVICE_DATA_SYNC is needed",
         "Service runs even when app is backgrounded"],
        'frames/slide_02_intro.png'
    )
    image_files.append('frames/slide_02_intro.png')
    
    # Slide 3: Backup screen screenshot with overlay
    screenshot1 = 'screenshot_backup_01_main.png'
    if os.path.exists(screenshot1):
        print("Creating slide 3: Backup screen with explanation...")
        add_text_overlay_to_image(
            screenshot1,
            ["Backup & Restore Screen",
             "Tap 'Restore' to start RestoreBackupService",
             "Service will run as foreground service with dataSync type"],
            'frames/slide_03_backup_screen.png',
            position='bottom'
        )
        image_files.append('frames/slide_03_backup_screen.png')
    else:
        print(f"Warning: {screenshot1} not found, creating text slide...")
        create_text_slide(
            ["Backup & Restore Screen",
             "Navigate to: Drawer Menu -> Backup",
             "This screen allows restoring messages from backup"],
            'frames/slide_03_backup_screen.png'
        )
        image_files.append('frames/slide_03_backup_screen.png')
    
    # Slide 4: Restore clicked
    screenshot2 = 'screenshot_backup_02_restore_clicked.png'
    if os.path.exists(screenshot2):
        print("Creating slide 4: Restore operation started...")
        add_text_overlay_to_image(
            screenshot2,
            ["Restore Operation Started",
             "RestoreBackupService.start() is called",
             "Service becomes foreground service immediately",
             "Shows notification to user"],
            'frames/slide_04_restore_started.png',
            position='bottom'
        )
        image_files.append('frames/slide_04_restore_started.png')
    else:
        print(f"Warning: {screenshot2} not found...")
        create_text_slide(
            ["Restore Operation Started",
             "User selects backup file and confirms",
             "RestoreBackupService.start() is called"],
            'frames/slide_04_restore_started.png'
        )
        image_files.append('frames/slide_04_restore_started.png')
    
    # Slide 5: Service running explanation
    print("Creating slide 5: Service running...")
    create_text_slide(
        ["Service Running in Foreground",
         "RestoreBackupService uses startForeground()",
         "Shows persistent notification with progress",
         "FOREGROUND_SERVICE_DATA_SYNC permission required"],
        'frames/slide_05_service_running.png'
    )
    image_files.append('frames/slide_05_service_running.png')
    
    # Slide 6: Notification screenshot
    screenshot3 = 'screenshot_backup_03_notification.png'
    if os.path.exists(screenshot3):
        print("Creating slide 6: Notification shown...")
        add_text_overlay_to_image(
            screenshot3,
            ["Foreground Service Notification",
             "Service continues running in background",
             "User can switch apps - restore continues",
             "Permission enables this background operation"],
            'frames/slide_06_notification.png',
            position='bottom'
        )
        image_files.append('frames/slide_06_notification.png')
    else:
        print(f"Warning: {screenshot3} not found...")
        create_text_slide(
            ["Foreground Service Notification",
             "Persistent notification shows restore progress",
             "Service continues even when app is closed"],
            'frames/slide_06_notification.png'
        )
        image_files.append('frames/slide_06_notification.png')
    
    # Slide 7: Code explanation - Manifest
    print("Creating slide 7: Manifest declaration...")
    create_text_slide(
        ["AndroidManifest.xml Declaration",
         "Permission: FOREGROUND_SERVICE_DATA_SYNC",
         "Service: foregroundServiceType='dataSync'",
         "Both required for Android 14+ compatibility"],
        'frames/slide_07_manifest.png'
    )
    image_files.append('frames/slide_07_manifest.png')
    
    # Slide 8: Code explanation - Service
    print("Creating slide 8: Service code...")
    create_text_slide(
        ["RestoreBackupService Implementation",
         "startForeground() - becomes foreground service",
         "dataSync type - indicates data synchronization",
         "Permission auto-granted when service starts"],
        'frames/slide_08_service_code.png'
    )
    image_files.append('frames/slide_08_service_code.png')
    
    # Slide 9: Why it matters
    print("Creating slide 9: Why it matters...")
    create_text_slide(
        ["Why This Permission Matters",
         "Without it: Restore fails on Android 14+",
         "With it: Reliable backup restore works",
         "User experience: Seamless background operations"],
        'frames/slide_09_why_matters.png'
    )
    image_files.append('frames/slide_09_why_matters.png')
    
    # Slide 10: Summary
    print("Creating slide 10: Summary...")
    create_text_slide(
        ["Summary",
         "FOREGROUND_SERVICE_DATA_SYNC enables",
         "reliable backup restore on Android 14+",
         "Essential for background data operations"],
        'frames/slide_10_summary.png'
    )
    image_files.append('frames/slide_10_summary.png')
    
    # Create video
    output_video = '../FOREGROUND_SERVICE_DATA_SYNC_DEMO.mp4'
    print(f"\nCreating video: {output_video}")
    if create_video_from_images(image_files, output_video, fps=1, duration=6):
        if os.path.exists(output_video):
            file_size = os.path.getsize(output_video)
            print(f"\n[SUCCESS] Video created successfully: {output_video}")
            print(f"Video size: {file_size / 1024 / 1024:.2f} MB")
            print(f"Duration: ~{len(image_files) * 6} seconds")
        else:
            print("\n[ERROR] Video file not created")
            sys.exit(1)
    else:
        print("\n[ERROR] Failed to create video")
        sys.exit(1)

if __name__ == '__main__':
    main()

