package com.tomaszmozolewski.jpegtools;

import java.io.*;

public class ImageSplitter {
	public static void main(String args[]) throws IOException {
	    	switch (args.length) {
	            case 3: splitImage(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2])); break;
	            default: System.err.println("Please give filename and how many pices x and y");
	        }		
	}

	public static void splitImage(String filename, int xCut, int yCut) throws IOException {
		ImageToolkit img = ImageToolkit.readJPEG(filename);
		String sFilename = filename.substring(0, filename.lastIndexOf('.'));
		int w = img.getWidth();
		int h = img.getHeight();

		int xLen = w / xCut;
		int yLen = h / yCut;

		int i, j;
		for (i=0; i<xCut; ++i) for (j=0; j<yCut; ++j) {
			ImageToolkit sImg = new ImageToolkit(img.getSubimage(i*xLen, j*yLen, xLen, yLen));
			sImg.writeJPEG(sFilename + "_" + i + "x" + j + ".jpeg");
		}
	}
}
