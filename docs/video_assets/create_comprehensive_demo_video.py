#!/usr/bin/env python3
"""
Create a comprehensive demonstration video showing FOREGROUND_SERVICE_DATA_SYNC
Uses screenshots from emulator with detailed text overlays explaining everything
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
                              text_color='white', title_size=60, subtitle_size=35,
                              position='bottom'):
    """Add text overlay to an existing screenshot"""
    if not os.path.exists(input_image):
        print(f"Warning: Input image not found: {input_image}")
        return False
    
    drawtext_filters = []
    
    if position == 'bottom':
        # Calculate starting Y position from bottom
        y_start = 850  # Start near bottom
        y_offset = y_start
    else:
        # Top position
        y_offset = 80
    
    for i, line in enumerate(text_lines):
        # Escape single quotes and special characters for drawtext
        escaped_text = line.replace("'", "\\'").replace(":", "\\:").replace("=", "\\=")
        if i == 0:
            # Title - larger, bold
            drawtext_filters.append(
                f"drawtext=text='{escaped_text}':fontsize={title_size}:fontcolor={text_color}:"
                f"x=(w-text_w)/2:y={y_offset}:"
                f"box=1:boxcolor=0x000000@0.85:boxborderw=15"
            )
            y_offset += title_size + 25
        else:
            # Subtitle - smaller
            drawtext_filters.append(
                f"drawtext=text='{escaped_text}':fontsize={subtitle_size}:fontcolor=#4CAF50:"
                f"x=(w-text_w)/2:y={y_offset}:"
                f"box=1:boxcolor=0x000000@0.75:boxborderw=8"
            )
            y_offset += subtitle_size + 18
    
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
                     bg_color='0x1a1a1a', text_color='white', title_size=70, subtitle_size=45):
    """Create an image with multiple lines of text using ffmpeg"""
    drawtext_filters = []
    y_offset = 250
    
    for i, line in enumerate(text_lines):
        # Escape single quotes and special characters for drawtext
        escaped_text = line.replace("'", "\\'").replace(":", "\\:").replace("=", "\\=")
        if i == 0:
            drawtext_filters.append(
                f"drawtext=text='{escaped_text}':fontsize={title_size}:fontcolor={text_color}:x=(w-text_w)/2:y={y_offset}"
            )
            y_offset += title_size + 30
        else:
            drawtext_filters.append(
                f"drawtext=text='{escaped_text}':fontsize={subtitle_size}:fontcolor=#4CAF50:x=(w-text_w)/2:y={y_offset}"
            )
            y_offset += subtitle_size + 20
    
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
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Error creating text slide: {result.stderr}")
        return False
    return True

def create_video_from_images(image_files, output_video, fps=1, duration=8):
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
    
    print("Creating comprehensive demonstration video for FOREGROUND_SERVICE_DATA_SYNC permission...")
    print("Using screenshots from emulator with detailed explanatory text overlays\n")
    
    # Create output directory
    os.makedirs('frames', exist_ok=True)
    
    image_files = []
    
    # Slide 1: Title
    print("Creating slide 1: Title...")
    create_text_slide(
        ["FOREGROUND_SERVICE_DATA_SYNC", "Complete Demonstration", "QKSMS Messenger - Backup Restore Feature"],
        'frames/slide_01_title.png'
    )
    image_files.append('frames/slide_01_title.png')
    
    # Slide 2: What is FOREGROUND_SERVICE_DATA_SYNC
    print("Creating slide 2: What is the permission...")
    create_text_slide(
        ["What is FOREGROUND_SERVICE_DATA_SYNC?",
         "Required permission for Android 14+ (API 34+)",
         "Needed when using foreground services for data synchronization",
         "Must be declared in AndroidManifest.xml",
         "Service must specify foregroundServiceType='dataSync'"],
        'frames/slide_02_what_is_permission.png'
    )
    image_files.append('frames/slide_02_what_is_permission.png')
    
    # Slide 3: Why we need it
    print("Creating slide 3: Why we need it...")
    create_text_slide(
        ["Why Does QKSMS Need This Permission?",
         "RestoreBackupService performs data synchronization",
         "Reads backup file and writes to local database",
         "This is classified as 'data sync' operation",
         "Without permission: Service fails on Android 14+",
         "With permission: Reliable backup restore works"],
        'frames/slide_03_why_need.png'
    )
    image_files.append('frames/slide_03_why_need.png')
    
    # Slide 4: Main menu screenshot with overlay
    screenshot_main = 'screenshot_main_menu.png'
    if os.path.exists(screenshot_main):
        print("Creating slide 4: Main menu...")
        add_text_overlay_to_image(
            screenshot_main,
            ["Main Menu - QKSMS Messenger",
             "Navigate to Backup & Restore",
             "Tap the menu drawer (hamburger icon)",
             "Then select 'Backup' option"],
            'frames/slide_04_main_menu.png',
            position='bottom'
        )
        image_files.append('frames/slide_04_main_menu.png')
    else:
        print(f"Warning: {screenshot_main} not found, creating text slide...")
        create_text_slide(
            ["Main Menu",
             "Navigate to: Menu Drawer -> Backup",
             "This is where users access backup features"],
            'frames/slide_04_main_menu.png'
        )
        image_files.append('frames/slide_04_main_menu.png')
    
    # Slide 5: Drawer menu screenshot
    screenshot_drawer = 'screenshot_drawer.png'
    if os.path.exists(screenshot_drawer):
        print("Creating slide 5: Drawer menu...")
        add_text_overlay_to_image(
            screenshot_drawer,
            ["Navigation Drawer",
             "Shows all app features and settings",
             "Tap 'Backup' to access backup & restore",
             "This is the entry point for restore operations"],
            'frames/slide_05_drawer.png',
            position='bottom'
        )
        image_files.append('frames/slide_05_drawer.png')
    else:
        print(f"Warning: {screenshot_drawer} not found...")
        create_text_slide(
            ["Navigation Drawer",
             "Shows app menu options",
             "Select 'Backup' to continue"],
            'frames/slide_05_drawer.png'
        )
        image_files.append('frames/slide_05_drawer.png')
    
    # Slide 6: Backup screen screenshot
    screenshot_backup = 'screenshot_backup_screen.png'
    if os.path.exists(screenshot_backup):
        print("Creating slide 6: Backup screen...")
        add_text_overlay_to_image(
            screenshot_backup,
            ["Backup & Restore Screen",
             "This screen allows users to backup or restore messages",
             "When user taps 'Restore' button:",
             "RestoreBackupService.start() is called",
             "Service immediately becomes foreground service",
             "Shows persistent notification with progress"],
            'frames/slide_06_backup_screen.png',
            position='bottom'
        )
        image_files.append('frames/slide_06_backup_screen.png')
    else:
        print(f"Warning: {screenshot_backup} not found...")
        create_text_slide(
            ["Backup & Restore Screen",
             "User selects backup file and confirms restore",
             "This triggers RestoreBackupService"],
            'frames/slide_06_backup_screen.png'
        )
        image_files.append('frames/slide_06_backup_screen.png')
    
    # Slide 7: Service starts explanation
    print("Creating slide 7: Service starts...")
    create_text_slide(
        ["What Happens When Restore Starts?",
         "1. User selects backup file and confirms",
         "2. RestoreBackupService.start() is called",
         "3. Service calls startForeground() immediately",
         "4. Shows persistent notification to user",
         "5. Service continues running even if app is closed",
         "6. FOREGROUND_SERVICE_DATA_SYNC permission is required"],
        'frames/slide_07_service_starts.png'
    )
    image_files.append('frames/slide_07_service_starts.png')
    
    # Slide 8: Service running explanation
    print("Creating slide 8: Service running...")
    create_text_slide(
        ["Service Running in Foreground",
         "RestoreBackupService uses startForeground()",
         "foregroundServiceType='dataSync' is specified",
         "Shows persistent notification with progress updates",
         "FOREGROUND_SERVICE_DATA_SYNC permission auto-granted",
         "Service continues even when user switches apps"],
        'frames/slide_08_service_running.png'
    )
    image_files.append('frames/slide_08_service_running.png')
    
    # Slide 9: Manifest declaration
    print("Creating slide 9: Manifest declaration...")
    create_text_slide(
        ["AndroidManifest.xml Declaration",
         "Permission declaration:",
         "<uses-permission android:name=\"android.permission.FOREGROUND_SERVICE_DATA_SYNC\" />",
         "",
         "Service declaration:",
         "<service android:name=\"...RestoreBackupService\"",
         "    android:foregroundServiceType=\"dataSync\" />",
         "",
         "Both are required for Android 14+ compatibility"],
        'frames/slide_09_manifest.png'
    )
    image_files.append('frames/slide_09_manifest.png')
    
    # Slide 10: What happens without permission
    print("Creating slide 10: Without permission...")
    create_text_slide(
        ["What Happens Without This Permission?",
         "On Android 14+ devices:",
         "Service fails to start as foreground service",
         "RestoreBackupService throws ForegroundServiceTypeException",
         "Backup restore functionality is broken",
         "Users cannot restore their messages",
         "",
         "This permission ensures compatibility"],
        'frames/slide_10_without_permission.png'
    )
    image_files.append('frames/slide_10_without_permission.png')
    
    # Slide 11: What happens with permission
    print("Creating slide 11: With permission...")
    create_text_slide(
        ["What Happens With This Permission?",
         "On Android 14+ devices:",
         "Service starts successfully as foreground service",
         "RestoreBackupService runs reliably",
         "Backup restore works as expected",
         "Users can restore messages without issues",
         "Permission is auto-granted by system",
         "",
         "Seamless user experience"],
        'frames/slide_11_with_permission.png'
    )
    image_files.append('frames/slide_11_with_permission.png')
    
    # Slide 12: Summary
    print("Creating slide 12: Summary...")
    create_text_slide(
        ["Summary",
         "FOREGROUND_SERVICE_DATA_SYNC is essential for:",
         "• Reliable backup restore on Android 14+",
         "• Foreground service data synchronization",
         "• Compliance with latest Android requirements",
         "• Seamless user experience",
         "",
         "Without it: Feature breaks on new Android versions",
         "With it: Feature works reliably across all versions"],
        'frames/slide_12_summary.png'
    )
    image_files.append('frames/slide_12_summary.png')
    
    # Create video
    output_video = '../FOREGROUND_SERVICE_DATA_SYNC_DEMO.mp4'
    print(f"\nCreating video: {output_video}")
    if create_video_from_images(image_files, output_video, fps=1, duration=8):
        if os.path.exists(output_video):
            file_size = os.path.getsize(output_video)
            print(f"\n[SUCCESS] Video created successfully: {output_video}")
            print(f"Video size: {file_size / 1024 / 1024:.2f} MB")
            print(f"Duration: ~{len(image_files) * 8} seconds")
        else:
            print("\n[ERROR] Video file not created")
            sys.exit(1)
    else:
        print("\n[ERROR] Failed to create video")
        sys.exit(1)

if __name__ == '__main__':
    main()

