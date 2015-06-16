rem Set variables

set timestamp=%DATE:/=-%_%TIME::=-%
set bkDir=C:\docs\java\JPEGTools_v2.0\bk\bk_JPEGTools_%timestamp: =_%
set PATH=C:\Program Files\winrar\;%PATH%
set rarexec=rar.EXE a -dh -mdG -r -s -x*.class -x*.jpg -x*.jpeg -x*.jar -x*.zip -x*.log

mkdir %bkDir%

rem rar and ftp files

%rarexec% -m5 -x*.rar %bkDir%\src.rar "C:\docs\java\JPEGTools_v2.0\"

rem rar in one file and remove others

%rarexec% -m0 %bkDir%.rar %bkDir%
del /s /q %bkDir%
rmdir %bkDir%
