$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
$mainClass = if ($args.Count -gt 0) { $args[0] } else { "com.project.Main" }

# Maven descarrega JavaFX i compila el projecte
mvn compile
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# JavaFX 22 per a Windows utilitza l'arquitectura x64
$javaSettings = cmd /c "java -XshowSettings:properties -version 2>&1"
$javaArch = ($javaSettings | Select-String '^\s*os.arch = (.*)$').Matches.Groups[1].Value
if ($javaArch -notin @("amd64", "x86_64")) {
    Write-Error "Arquitectura Java no compatible: $javaArch"
    exit 1
}

# Utilitza només els JAR de JavaFX 22 per a Windows
$fxJars = foreach ($module in @("base", "controls", "fxml", "graphics")) {
    $jar = "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-$module\22\javafx-$module-22-win.jar"
    if (-not (Test-Path $jar)) {
        Write-Error "No es pot trobar JavaFX: $jar"
        exit 1
    }
    $jar
}
$fxPath = $fxJars -join ";"

# Obtener las dependencias necesarias para ejecutar el proyecto.
mvn dependency:build-classpath "-Dmdep.outputFile=target/dependency-classpath.txt" "-Dmdep.includeScope=runtime"

if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

# Leer el classpath generado por Maven.
$dependencyClasspath = (Get-Content "target/dependency-classpath.txt" -Raw).Trim()

# JavaFX ya se carga mediante module-path.
# Excluir sus JAR del classpath convencional.
$dependencyJars = $dependencyClasspath.Split([char][IO.Path]::PathSeparator) |
    Where-Object {
        $_ -and ($_ -notmatch '[\\/]+org[\\/]+openjfx[\\/]')
    }

# Añadir las clases del proyecto y las dependencias externas.
$classPath = (@("$PSScriptRoot\target\classes") + @($dependencyJars)) `
    -join [IO.Path]::PathSeparator

# Ejecutar la aplicación indicada.
java --module-path $fxPath --add-modules javafx.controls,javafx.fxml `
    -cp $classPath $mainClass

exit $LASTEXITCODE
