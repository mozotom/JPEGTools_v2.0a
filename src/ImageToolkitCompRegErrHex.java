package com.tomaszmozolewski.jpegtools;

import java.io.*;
import java.util.*;

public class ImageToolkitCompRegErrHex implements ImageToolkitComparator {
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
    	System.out.println("" + new ImageToolkitCompRegErrHex().compare(imgA, imgB));
    }

    public double compare(ImageToolkit X, ImageToolkit Y) {
	// long start = new Date().getTime();
	double result1, result2;

        int w = Math.min(X.getWidth(), Y.getWidth());
        int h = Math.min(X.getHeight(), Y.getHeight());

        int i, N = w * h * 3, n = 0;
        
        int[] x = (int[]) X.getScaledInstance(w, h).getRaster().getPixels(0, 0, w, h, new int[N]);
        int[] y = (int[]) Y.getScaledInstance(w, h).getRaster().getPixels(0, 0, w, h, new int[N]);

        double Exy = 0, Ex = 0, Ey = 0, Exx = 0, Eyy = 0, SSE1 = 0, SSE2 = 0;
        double b0, b1, e;
	for (i=0; i<N; ++i) {
	  if ((x[i] != IGNORE_COLOR) && (y[i] != IGNORE_COLOR)) { 
	    Exy += x[i] * y[i];
	    Ex += x[i];
	    Ey += y[i];
	    Exx += x[i] * x[i];
	    Eyy += y[i] * y[i];
	    ++n;
	  }
	}

	b1 = (n * Exy - Ex * Ey) / (n * Exx - Ex * Ex);
	b0 = (Ey - b1 * Ex) / n;

	for (i=0; i<n; ++i) {
	        e = b0 + b1 * x[i] - y[i];
		SSE1 += e * e;
	}

	b1 = (n * Exy - Ex * Ey) / (n * Eyy - Ey * Ey);
	b0 = (Ex - b1 * Ey) / n;

	for (i=0; i<n; ++i) {
	        e = b0 + b1 * y[i] - x[i];
		SSE2 += e * e;
	}

	result1 = SSE1 / n / sqr((double) ImageToolkitCompRegErrHex.MAX / 2);
	result2 = SSE2 / n / sqr((double) ImageToolkitCompRegErrHex.MAX / 2);
	// System.out.println("< compare " + (new Date().getTime() - start));
        return(Math.min(result1, result2));
    }

    public static double sqr(double a) {
        return(a * a);
    }
}
