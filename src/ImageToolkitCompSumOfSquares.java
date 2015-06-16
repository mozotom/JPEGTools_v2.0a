package com.tomaszmozolewski.jpegtools;

import java.io.*;
import java.util.*;

public class ImageToolkitCompSumOfSquares implements ImageToolkitComparator {
    public static final int MAX=0xff;

    public static void main(String args[]) throws IOException {
    	switch (args.length) {
            case 2: testCompare(args[0], args[1]); break;
            default: System.err.println("Please give two filenames to compare");
        }
    }

    private static void testCompare(String fileA, String fileB) throws IOException {
    	ImageToolkit imgA = ImageToolkit.readJPEG(fileA);
    	ImageToolkit imgB = ImageToolkit.readJPEG(fileB);
    	System.out.println("" + new ImageToolkitCompSumOfSquares().compare(imgA, imgB));
    }

    public double compare(ImageToolkit imgA, ImageToolkit imgB) {
	// long start = new Date().getTime();
        double result = 0;
        
        int w = Math.min(imgA.getWidth(), imgB.getWidth());
        int h = Math.min(imgA.getHeight(), imgB.getHeight());

        int i, t = w * h * 3;
        
        int[] sA = (int[]) imgA.getScaledInstance(w, h).getRaster().getPixels(0, 0, w, h, new int[t]);
        int[] sB = (int[]) imgB.getScaledInstance(w, h).getRaster().getPixels(0, 0, w, h, new int[t]);

	for (i=0; i<t; ++i) result += sqr(sA[i] - sB[i]);

        result /= t * sqr(MAX);
	// System.out.println("< compare " + (new Date().getTime() - start));
        return(result);
    }

     public static double sqr(double a) {
        return(a * a);
    }
}
