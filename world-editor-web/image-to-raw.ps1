param(
  [Parameter(Mandatory = $true)][string]$Image,
  [Parameter(Mandatory = $true)][string]$Out
)

Add-Type -AssemblyName System.Drawing

$source = [System.Drawing.Bitmap]::FromFile((Resolve-Path $Image))
try {
  $width = $source.Width
  $height = $source.Height
  $rect = New-Object System.Drawing.Rectangle(0, 0, $width, $height)
  $data = $source.LockBits($rect, [System.Drawing.Imaging.ImageLockMode]::ReadOnly, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
  $stride = $data.Stride
  try {
    $row = New-Object byte[] $stride
    $stream = [System.IO.File]::Create($Out)
    try {
      for ($y = 0; $y -lt $height; $y++) {
        [System.Runtime.InteropServices.Marshal]::Copy(
          [IntPtr]($data.Scan0.ToInt64() + $y * $data.Stride),
          $row,
          0,
          $row.Length
        )
        $stream.Write($row, 0, $row.Length)
      }
    } finally {
      $stream.Dispose()
    }
  } finally {
    $source.UnlockBits($data)
  }
} finally {
  $source.Dispose()
}

Write-Output "raw=$Out width=$width height=$height stride=$stride bytes=$((Get-Item $Out).Length)"