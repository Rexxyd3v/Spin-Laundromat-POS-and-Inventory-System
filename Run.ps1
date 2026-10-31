param(
    [switch]$Test,
    [switch]$Preview
)

$ErrorActionPreference = 'Stop'
$taskRoot = $PSScriptRoot
$taskProject = Join-Path $taskRoot 'JavaProject'
$taskBuild = Join-Path $taskRoot '.build/classes'
$taskTemporary = Join-Path $taskRoot '.build/test-data'

$jdkCandidates = @()
if ($env:JAVA_HOME) { $jdkCandidates += $env:JAVA_HOME }
$javaInstallFolder = Join-Path $env:ProgramFiles 'Java'
if (Test-Path -LiteralPath $javaInstallFolder) {
    $jdkCandidates += @(Get-ChildItem -LiteralPath $javaInstallFolder -Directory |
        Sort-Object Name -Descending | ForEach-Object { $_.FullName })
}
$taskJdk = $jdkCandidates | Where-Object {
    (Test-Path -LiteralPath (Join-Path $_ 'bin/javac.exe')) -and
    (Test-Path -LiteralPath (Join-Path $_ 'bin/java.exe'))
} | Select-Object -First 1
if ($taskJdk) {
    $taskCompiler = Join-Path $taskJdk 'bin/javac.exe'
    $taskJava = Join-Path $taskJdk 'bin/java.exe'
} else {
    $taskCompiler = (Get-Command javac -ErrorAction Stop).Source
    $taskJava = (Get-Command java -ErrorAction Stop).Source
}

# Compile into a clean output so removed source files cannot survive as stale classes.
$taskResolvedRoot = [System.IO.Path]::GetFullPath($taskRoot).TrimEnd('\','/')
$taskResolvedBuild = [System.IO.Path]::GetFullPath($taskBuild)
$taskExpectedBuild = [System.IO.Path]::GetFullPath((Join-Path $taskResolvedRoot '.build/classes'))
if ($taskResolvedBuild -ne $taskExpectedBuild -or
    -not $taskResolvedBuild.StartsWith($taskResolvedRoot + [System.IO.Path]::DirectorySeparatorChar,
        [System.StringComparison]::OrdinalIgnoreCase)) {
    throw 'Build output must stay inside this workspace.'
}
if (Test-Path -LiteralPath $taskResolvedBuild) {
    Remove-Item -LiteralPath $taskResolvedBuild -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $taskBuild,$taskTemporary | Out-Null
$taskSourceFolders = @((Join-Path $taskProject 'src'))
if ($Test -or $Preview) { $taskSourceFolders += Join-Path $taskProject 'test' }
$taskSources = @(Get-ChildItem -LiteralPath $taskSourceFolders -Recurse -Filter '*.java' |
    ForEach-Object { $_.FullName })
& $taskCompiler --release 21 -encoding UTF-8 -d $taskBuild $taskSources
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed. A JDK version 21 or later is required.' }
$taskResources = Join-Path $taskProject 'src/resources'
if (Test-Path -LiteralPath $taskResources) {
    Copy-Item -LiteralPath $taskResources -Destination $taskBuild -Recurse -Force
}


Push-Location -LiteralPath $taskProject
try {
    if ($Test) {
        foreach ($taskCheck in @('ModernPosTest','EnhancementTest','UxFlowTest','LoginMotionTest','StorageOperationsTest','FeatureUiTest')) {
            & $taskJava '-Djava.awt.headless=true' "-Djava.io.tmpdir=$taskTemporary" -cp $taskBuild $taskCheck
            if ($LASTEXITCODE -ne 0) { throw "Verification failed: $taskCheck" }
        }
        & $taskJava '-Djava.awt.headless=false' "-Djava.io.tmpdir=$taskTemporary" -cp $taskBuild CatalogEditorLayoutTest
        if ($LASTEXITCODE -ne 0) { throw 'Verification failed: CatalogEditorLayoutTest' }
        & $taskJava '-Djava.awt.headless=false' "-Djava.io.tmpdir=$taskTemporary" -cp $taskBuild ResponsiveDashboardTest
        if ($LASTEXITCODE -ne 0) { throw 'Verification failed: ResponsiveDashboardTest' }
    }
    if ($Preview) {
        & $taskJava "-Djava.io.tmpdir=$taskTemporary" -cp $taskBuild RenderPreview (Join-Path $taskRoot 'preview')
        if ($LASTEXITCODE -ne 0) { throw 'Preview generation failed.' }
    }
    if (-not $Test -and -not $Preview) {
        & $taskJava -cp $taskBuild Main
        if ($LASTEXITCODE -ne 0) { throw 'The application could not start.' }
    }
} finally {
    Pop-Location
}
