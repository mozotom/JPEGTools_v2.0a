package com.tomaszmozolewski.jpegtools;

import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

public class ImageHexer {
	public static final double sqrt3 = Math.sqrt(3);

	public static java.awt.geom.Point2D.Double[] getHexPoints(java.awt.geom.Point2D.Double m, double a) {

		//    1   2
		//
		//  0   m   3
		//
		//    5   4

		double h = a * Math.sqrt(3) / 2;
		java.awt.geom.Point2D.Double result[] = new java.awt.geom.Point2D.Double[6];

		int i;
		for (i=0; i<6; ++i) result[i] = new java.awt.geom.Point2D.Double();

		result[0].setLocation(m.getX() - a  , m.getY()  );
		result[1].setLocation(m.getX() - a/2, m.getY()-h);
		result[2].setLocation(m.getX() + a/2, m.getY()-h);
		result[3].setLocation(m.getX() + a  , m.getY()  );
		result[4].setLocation(m.getX() + a/2, m.getY()+h);
		result[5].setLocation(m.getX() - a/2, m.getY()+h);

		return result;
	}

	public static java.awt.geom.Point2D.Double[] getHexCenters(java.awt.geom.Point2D.Double m, double a) {
                                
		//         .   .
		//
		//   .   .   1   .   .
		//         
		// .   0   .   .   2   .
		//
		//   .   .   m   .   .
		//
		// .   5   .   .   3   .
		//
		//   .   .   4   .   .
		//
		//         .   .

		double h = a * Math.sqrt(3) / 2;
		java.awt.geom.Point2D.Double result[] = new java.awt.geom.Point2D.Double[6];

		int i;
		for (i=0; i<6; ++i) result[i] = new java.awt.geom.Point2D.Double();

		result[0].setLocation(m.getX() - a * 3 / 2, m.getY() - h    );
		result[1].setLocation(m.getX()            , m.getY() - h * 2);
		result[2].setLocation(m.getX() + a * 3 / 2, m.getY() - h    );
		result[3].setLocation(m.getX() + a * 3 / 2, m.getY() + h    );
		result[4].setLocation(m.getX()            , m.getY() + h * 2);
		result[5].setLocation(m.getX() - a * 3 / 2, m.getY() + h    );

		return result;
	}

	protected static void getAllHexCenters(TreeSet result, ComparablePoint m, int a, int W, int H) {
		Vector todoList = new Vector();
		double h = a * Math.sqrt(3) / 2;
		java.awt.geom.Point2D.Double p[];
		int i;

                todoList.add(m);
		while (todoList.size()>0) {
			m = (ComparablePoint) todoList.remove(0);
			if (
			    (result.contains(m)) ||
			    (m.getX() < -a) || (m.getY() < -h) ||
			    (m.getX() >= W+a) || (m.getY() >= H+h)
			) continue;

			// System.out.println(m.toString());
			// System.out.println("hc: " + result.size() + "   \r");
			result.add(m); 
	
			p = getHexCenters(m, a);
			for (i=0; i<p.length; ++i) todoList.add(new ComparablePoint(p[i]));
		}

	}

	public static java.awt.geom.Point2D.Double[] getAllHexCenters(int a, int W, int H) {
		TreeSet result = new TreeSet();
		double h = a * Math.sqrt(3) / 2;
		ComparablePoint m = new ComparablePoint(0, 0);

		getAllHexCenters(result, m, a, W, H);

		int i;
		Object o[] = result.toArray();
		java.awt.geom.Point2D.Double res[] = new java.awt.geom.Point2D.Double[o.length];
		for (i=0; i<o.length; ++i) res[i] = (java.awt.geom.Point2D.Double) o[i];		

		return res;
	}

	public static void main(String args[]) throws IOException {
	    	switch (args.length) {
		    case 1: hexImage(args[0]); break;
	            case 2: hexImage(args[0], Double.parseDouble(args[1])); break;
	            case 4: hexDir(args[0], args[1], Double.parseDouble(args[2]), Integer.parseInt(args[3])); break;
	            default: System.err.println("Parmeters: input file, shift (.5=middle) or length and border");
	        }		
	}

	public static void hexImage(String dirname) throws IOException {
		new HexImageFrame(ImageToolkit.getImageFilenames(new File(dirname)));
	}

	public static void hexImage(String filename, double shift) throws IOException {
			ImageToolkit img = ImageToolkit.readJPEG(filename);
			String sFilename = filename.substring(0, filename.lastIndexOf('.'));
			ImageToolkit cutImg = hexImage(img, shift);
			// img.writeJPEG(sFilename + "_" + shift + ".jpeg"); // Full image
			cutImg.writeJPEG(sFilename + "_" + shift + ".jpeg"); // Hex cut image
	}

	public static void hexDir(String dirname, String dst_dir, double shift, int size) throws IOException {
		File file = new File(dirname);

		if (file.isDirectory()) {
			Vector files = ImageToolkit.getImageFilenames(file);
			for (Enumeration e = files.elements(); e.hasMoreElements();) {
			    String filename = (String) e.nextElement();
			    System.out.println(filename);

			    ImageToolkit img = ImageToolkit.readJPEG(filename);
			    String sFilename = dst_dir + File.separator + filename.substring(0, filename.lastIndexOf('.')).substring(dirname.length());
			    new File(sFilename).getParentFile().mkdirs();

			    int org_h = img.getHeight();
			    int org_w = img.getWidth();

			    int hex_ws = org_w / 2;
			    int hex_hs = (int) Math.floor(((double) org_h) / sqrt3);

			    int hex_s = Math.min(hex_ws, hex_hs);
			    hex_ws = 2 * hex_s;
			    hex_hs = (int) Math.ceil(sqrt3 * hex_s);

			    int x = (int) Math.round((org_w - hex_ws) * shift);
			    int y = (int) Math.round((org_h - hex_hs) * shift);

			    ImageToolkit cImg = new ImageToolkit(img.getSubimage(x, y, hex_ws, hex_hs));
			    ImageToolkit sImg = cImg.getScaledInstance(size * 2, (int) Math.ceil(size * sqrt3));

			    sImg.writeJPEG(sFilename + "_hex_" + shift + "_s" + size + ".jpeg"); // Hex cut image
			}
		}
	}

	public static void hexImage(String filename, int length, int border) throws IOException {
		ImageToolkit img = ImageToolkit.readJPEG(filename);
		String sFilename = filename.substring(0, filename.lastIndexOf('.'));
		hexImage(img, length, border);
		img.writeJPEG(sFilename + "_" + length + "_" + border + ".jpeg"); // Hex net image
	}

	public static void hexImage(ImageToolkit img, int length, int border) {
		java.awt.geom.Point2D.Double[] hexCenters = getAllHexCenters(length, img.getWidth(), img.getHeight());

		int W = img.getWidth();
		int H = img.getHeight();
		int[] imgPixels = img.getRGB(0, 0, W, H, new int[W * H], 0, W);

		int i;
		for (i=0; i<hexCenters.length; ++i) {
			drawHexBorder(imgPixels, W, H, hexCenters[i], length);
			// System.out.println("db: " + i + "   \r");
		}
		// System.out.println(hexCenters.length);
		img.setRGB(0, 0, W, H, imgPixels, 0, W);
	}

	public static void drawHexBorder(int[] img, int W, int H, java.awt.geom.Point2D.Double hexCenter, int length) {
		java.awt.geom.Point2D.Double[] hexPoints = getHexPoints(hexCenter, length);
		int i;

		for (i=0; i<hexPoints.length; ++i) drawLine(img, W, H, hexPoints[i], hexPoints[(i+1)%hexPoints.length]);
	}

	public static void drawHexBorder(Graphics g, java.awt.geom.Point2D.Double hexCenter, int length, Dimension dim) {
		int height = (int) Math.round(length * Math.sqrt(3) / 2);
		if (hexCenter.getX()-length < 0) hexCenter.x=length;
		if (hexCenter.getX()+length > dim.getWidth()) hexCenter.x = (int) dim.getWidth()-length;
		if (hexCenter.getY()-height < 0) hexCenter.y=height;
		if (hexCenter.getY()+height > dim.getHeight()) hexCenter.y = (int) dim.getHeight()-height;

		java.awt.geom.Point2D.Double[] hexPoints = getHexPoints(hexCenter, length);
		int i;
		for (i=0; i<hexPoints.length; ++i) {
		  g.drawLine((int) Math.round(hexPoints[i].getX()), Math.round((int) hexPoints[i].getY()), Math.round((int) hexPoints[(i+1)%hexPoints.length].getX()), Math.round((int) hexPoints[(i+1)%hexPoints.length].getY()));
		  g.drawLine((int) Math.round(hexPoints[i].getX()+1), Math.round((int) hexPoints[i].getY()), Math.round((int) hexPoints[(i+1)%hexPoints.length].getX()+1), Math.round((int) hexPoints[(i+1)%hexPoints.length].getY()));
		  g.drawLine((int) Math.round(hexPoints[i].getX()-1), Math.round((int) hexPoints[i].getY()), Math.round((int) hexPoints[(i+1)%hexPoints.length].getX()-1), Math.round((int) hexPoints[(i+1)%hexPoints.length].getY()));
		  g.drawLine((int) Math.round(hexPoints[i].getX()), Math.round((int) hexPoints[i].getY()+1), Math.round((int) hexPoints[(i+1)%hexPoints.length].getX()), Math.round((int) hexPoints[(i+1)%hexPoints.length].getY()+1));
		  g.drawLine((int) Math.round(hexPoints[i].getX()), Math.round((int) hexPoints[i].getY()-1), Math.round((int) hexPoints[(i+1)%hexPoints.length].getX()), Math.round((int) hexPoints[(i+1)%hexPoints.length].getY()-1));
		}
	}

	protected static void drawLine(int[] img, int W, int H, java.awt.geom.Point2D.Double p1, java.awt.geom.Point2D.Double p2) {           
		drawLine(img, W, H, p1, p2, 0);
	}

	protected static void drawLine(int[] img, int W, int H, java.awt.geom.Point2D.Double p1, java.awt.geom.Point2D.Double p2, int colorRGB) {
		int i, x, y;

		int d = (int) Math.round(Math.max(Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getY() - p2.getY())));
		double dx = (p2.getX() - p1.getX()) / d;
		double dy = (p2.getY() - p1.getY()) / d;

		for (i=0; i<=d; ++i) {
		  x = (int) Math.round(p1.getX() + dx * i);
		  y = (int) Math.round(p1.getY() + dy * i);
		  if ((x>=0) && (x<W) && (y>=0) && (y<H)) img[y*W+x] = colorRGB;
		}
	}

	public static ImageToolkit hexImage(ImageToolkit img) {
		return hexImage(img, .5);
	}

	public static ImageToolkit hexImage(ImageToolkit img, double shift) {
		// Read Image size
		int W = img.getWidth();
		int H = img.getHeight();

		// Calculate a (base of hex) and h (high of triangle in hex) from image size
		double a = ((double) W) / 2;
		double h = ((double) H) / 2;

		// Resize a or h down to match hex proportions
		if (h > a * Math.sqrt(3) / 2) h = a * Math.sqrt(3) / 2;
		if (a > h * 2 * Math.sqrt(3) / 3) a = h * 2 * Math.sqrt(3) / 3;

		// Find difference between hex size and pic size
		double dx = W - 2 * a;
		double dy = H - 2 * h;

		// Calculate shift
		double sx = dx * shift;
		double sy = dy * shift;

		// Find shifted middle
		java.awt.geom.Point2D.Double m = new java.awt.geom.Point2D.Double();
		m.setLocation(sx + a, sy + h);
		java.awt.geom.Point2D.Double[] hexPoints = getHexPoints(m, a);

		int[] imgPixels = img.getRGB(0, 0, W, H, new int[W * H], 0, W);
		int i, j;

		double slope = - Math.sqrt(3);
		double const01 = hexPoints[0].getY() - slope * hexPoints[0].getX();
		double const05 = hexPoints[0].getY() + slope * hexPoints[0].getX();
		double const23 = hexPoints[3].getY() + slope * hexPoints[3].getX(); 
		double const43 = hexPoints[3].getY() - slope * hexPoints[3].getX(); 

		for (j=0; j<H; ++j) for (i=0; i<W; ++i) {
			if (
			    (j < hexPoints[1].getY()) || (j > hexPoints[5].getY()) ||
			    (j < slope * i + const01) ||
			    (j > - slope * i + const05) ||
			    (j < - slope * i + const23) ||
			    (j > slope * i + const43)
			) imgPixels[j*W+i]=0;
                }

		img.setRGB(0, 0, W, H, imgPixels, 0, W);

		int x1 = (int) Math.max(Math.round(hexPoints[0].getX()), 0);
		int y1 = (int) Math.max(Math.round(hexPoints[1].getY()), 0);
		int w1 = (int) Math.min(Math.round(hexPoints[3].getX() - hexPoints[0].getX()), W - x1);
		int h1 = (int) Math.min(Math.round(hexPoints[5].getY() - hexPoints[1].getY()), H - y1);

		return new ImageToolkit(img.getSubimage(x1, y1, w1, h1));
	}

}
