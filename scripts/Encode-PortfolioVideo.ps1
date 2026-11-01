param(
    [Parameter(Mandatory=$true)][string]$FramesPath,
    [Parameter(Mandatory=$true)][string]$OutputPath,
    [string]$FfmpegPath = ''
)
$ErrorActionPreference = 'Stop'
if (-not $FfmpegPath -and $env:FFMPEG_PATH) { $FfmpegPath = $env:FFMPEG_PATH }
if (-not $FfmpegPath) {
    $portfolioEncoder = Get-Command ffmpeg -ErrorAction SilentlyContinue
    if ($portfolioEncoder) { $FfmpegPath = $portfolioEncoder.Source }
}
if (-not $FfmpegPath) {
    $portfolioCachedEncoder = Join-Path $PSScriptRoot '../.build/portfolio-tools/ffmpeg.exe'
    if (Test-Path -LiteralPath $portfolioCachedEncoder) { $FfmpegPath = $portfolioCachedEncoder }
}
if (-not $FfmpegPath -or -not (Test-Path -LiteralPath $FfmpegPath)) {
    throw 'Install FFmpeg or pass -FfmpegPath to export the portfolio MP4. The GIF and screenshots are already generated.'
}
$portfolioFrameCount = @(Get-ChildItem -LiteralPath $FramesPath -Filter 'frame-*.png' -File).Count
if ($portfolioFrameCount -lt 2) { throw 'The portfolio recording contains no usable frames.' }
$portfolioFrameInput = Join-Path $FramesPath 'frame-%05d.png'
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $OutputPath) | Out-Null
& $FfmpegPath -hide_banner -loglevel warning -framerate 10 -i $portfolioFrameInput -vf 'fps=30' -c:v libx264 -preset medium -crf 20 -pix_fmt yuv420p -movflags +faststart -an -metadata 'title=Spin Laundromat - Portfolio preview' -metadata 'comment=Demo data. Passwords are masked.' -y $OutputPath
if ($LASTEXITCODE -ne 0) { throw 'Portfolio video encoding failed.' }
# Decode the complete output to check the actual MP4, not just the encoder exit code.
& $FfmpegPath -hide_banner -loglevel error -i $OutputPath -f null -
if ($LASTEXITCODE -ne 0) { throw 'Portfolio video playback verification failed.' }
Write-Output ('Verified portfolio MP4: ' + $OutputPath + ' (' + ($portfolioFrameCount / 10) + ' seconds, 1920x1080, 30 fps).')
