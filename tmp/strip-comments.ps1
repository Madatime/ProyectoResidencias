$single = [System.Text.RegularExpressions.RegexOptions]::Singleline
$utf8 = New-Object System.Text.UTF8Encoding($false)

Get-ChildItem 'src\main\resources' -Recurse -File -Include *.html | ForEach-Object {
  $c = [System.IO.File]::ReadAllText($_.FullName)
  $u = [regex]::Replace($c, '<!--[\s\S]*?-->', '', $single)
  $u = [regex]::Replace($u, '(?m)^[ \t]*//.*(?:\r?\n)?', '')
  if ($u -ne $c) { [System.IO.File]::WriteAllText($_.FullName, $u, $utf8) }
}

Get-ChildItem 'src\main\resources' -Recurse -File -Include *.css | ForEach-Object {
  $c = [System.IO.File]::ReadAllText($_.FullName)
  $u = [regex]::Replace($c, '/\*(?!\[\[)[\s\S]*?\*/', '', $single)
  if ($u -ne $c) { [System.IO.File]::WriteAllText($_.FullName, $u, $utf8) }
}

Get-ChildItem 'src\main\java','src\main\resources' -Recurse -File -Include *.java,*.js,*.html | ForEach-Object {
  $c = [System.IO.File]::ReadAllText($_.FullName)
  $u = [regex]::Replace($c, '/\*(?!\[\[)[\s\S]*?\*/', '', $single)
  $u = [regex]::Replace($u, '(?m)^[ \t]*//.*(?:\r?\n)?', '')
  $u = [regex]::Replace($u, '(?m)([^:])\s+//.*$', '$1')
  if ($u -ne $c) { [System.IO.File]::WriteAllText($_.FullName, $u, $utf8) }
}
