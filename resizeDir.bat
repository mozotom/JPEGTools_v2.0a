rem java -Xmx1536m -classpath release\MosaicMaker.jar com.tomaszmozolewski.jpegtools.ImageToolkit ScaledCutInstanceDir "F:/Photos" "C:/Photos_134x116" 134 116

java -Xmx1536m -verbosegc -classpath release\MosaicMaker.jar com.tomaszmozolewski.jpegtools.ImageToolkit ScaledCutInstanceDir %1 %2 %3 %4

