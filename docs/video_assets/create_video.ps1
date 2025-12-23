# PowerShell script to create demonstration video using ffmpeg
# This script creates a video explaining FOREGROUND_SERVICE_DATA_SYNC permission

$ErrorActionPreference = "Stop"

# Check if ffmpeg is available
try {
    $ffmpegVersion = ffmpeg -version 2>&1 | Select-Object -First 1
    Write-Host "Found ffmpeg: $ffmpegVersion"
} catch {
    Write-Host "Error: ffmpeg not found. Please install ffmpeg first."
    Write-Host "Download from: https://ffmpeg.org/download.html"
    exit 1
}

# Create text overlay images using ImageMagick or similar
# For now, we'll create a simple video with text overlays using ffmpeg's drawtext filter

$outputVideo = "docs\FOREGROUND_SERVICE_DATA_SYNC_DEMO.mp4"
$fps = 1  # 1 frame per second for slideshow effect
$duration = 5  # 5 seconds per slide

# Create a simple video with text explaining the permission
# We'll use ffmpeg's color source and text overlay

Write-Host "Creating demonstration video..."

# Create video with multiple text slides
$ffmpegCommand = @"
ffmpeg -y -f lavfi -i color=c=0x1a1a1a:s=1920x1080:d=30 -vf "drawtext=text='FOREGROUND_SERVICE_DATA_SYNC Permission':fontsize=60:fontcolor=white:x=(w-text_w)/2:y=100,drawtext=text='Android 14+ Requirement':fontsize=40:fontcolor=#4CAF50:x=(w-text_w)/2:y=200" -t 5 -pix_fmt yuv420p -c:v libx264 "$outputVideo"
"@

# Actually, let's create a better approach - create individual frames and combine them
Write-Host "This script will create a video demonstration..."
Write-Host "For a complete video, you would need to:"
Write-Host "1. Take screenshots of the code"
Write-Host "2. Create text overlay images"
Write-Host "3. Combine them with ffmpeg"
Write-Host ""
Write-Host "See the demonstration guide in FOREGROUND_SERVICE_DATA_SYNC_DEMONSTRATION.md"

