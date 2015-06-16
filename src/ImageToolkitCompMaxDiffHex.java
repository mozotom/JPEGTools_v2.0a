package com.tomaszmozolewski.jpegtools;

import java.io.*;
import java.util.*;

public class ImageToolkitCompMaxDiffHex implements ImageToolkitComparator {
    public static final int MAX=0xff;
    public static final int IGNORE_COLOR=0;

    public static void main(String args[]) throws IOException {
    	switch (args.length) {
            case 2: testCompare(args[0], args[1]); break;
            default: System.err.println("Please give two filenames to compare");
        }
    }

    private static void testCompare(String fileA, String fileB) throws IOException {
    	ImageToolkit imgA = ImageToolkit.readJPEG(fileA);
    	ImageToolkit imgB = ImageToolkit.readJPEG(fileB);
    	System.out.println("" + new ImageToolkitCompMaxDiffHex().compare(imgA, imgB));
    }

    public double compare(ImageToolkit imgA, ImageToolkit imgB) {
	// long start = new Date().getTime();
        double result = 0;
        
        int w = Math.min(imgA.getWidth(), imgB.getWidth());
        int h = Math.min(imgA.getHeight(), imgB.getHeight());

        int i, T = w * h * 3, t = 0;
	int i3, t3 = T / 3;
        
        int[] sA = (int[]) imgA.getScaledInstance(w, h).getRaster().getPixels(0, 0, w, h, new int[T]);
        int[] sB = (int[]) imgB.getScaledInstance(w, h).getRaster().getPixels(0, 0, w, h, new int[T]);

	for (i=0; i<t3; ++i) {
	  i3 = i * 3;
	  if (sA[i3] != IGNORE_COLOR) { 
	    result += Math.max(sqr(sA[i3] - sB[i3]), Math.max(sqr(sA[i3+1] - sB[i3+1]), sqr(sA[i3+2] - sB[i3+2])));
	    ++t; 
	  }
	}

        result /= t * sqr(MAX);
	// System.out.println("< compare " + (new Date().getTime() - start));
        return(result);
    }

     public static double sqr(double a) {
        return(a * a);
    }
}
