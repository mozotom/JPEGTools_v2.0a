cd src
call c.bat
more c.out
cd ..

xcopy /s /r /y classes\*.* build\

cd build
call build.bat
cd ..

rem xcopy /s /r /y build\MosaicMaker.jar release
rem xcopy /s /r /y build\MosaicMaker.jar home
