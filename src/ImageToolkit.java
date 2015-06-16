package com.tomaszmozolewski.jpegtools;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import com.sun.image.codec.jpeg.*;

import com.tomaszmozolewski.helpers.MathHelper;

// Create new instance with BufferedImage.TYPE_INT_RGB

public class ImageToolkit extends BufferedImage {
    public static final int NUMBER_OF_COLORS = 256;
    public static final int R_SHIFT = 16;
    public static final int G_SHIFT =  8;
    public static final int B_SHIFT =  0;

    public static final int R_MASK = 0xff << R_SHIFT;
    public static final int G_MASK = 0xff << G_SHIFT;
    public static final int B_MASK = 0xff << B_SHIFT;

    public static final int RGB_MASK = R_MASK | G_MASK | B_MASK;

    public static void main(String args[]) throws Exception {
	String action = args[0];

	if (action.compareTo("TestShow") == 0) testShow(args[1]);
	if (action.compareTo("TestCopy") == 0) testCopy(args[1], args[2]);
	if (action.compareTo("MorphSeq") == 0) morphSeq(args[1], args[2], Float.parseFloat(args[3]));
	if (action.compareTo("ScaledCutInstance") == 0) readJPEG(args[1]).getScaledCutInstance(Integer.parseInt(args[2]), Integer.parseInt(args[3])).writeJPEG(args[4]);
	if (action.compareTo("ResizeDir") == 0) resizeDir(args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), 0, 0);
	if (action.compareTo("ResizeDirReduce") == 0) resizeDir(args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), 0, 1);
	if (action.compareTo("FadeFileBorders") == 0) fadeFileBorders(args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Double.parseDouble(args[5]));
	if (action.compareTo("CutOut") == 0) cutOut(args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
	if (action.compareTo("SpreadColor") == 0) readJPEG(args[1]).spreadColor(Double.parseDouble(args[3]), Double.parseDouble(args[4])).writeJPEG(args[2]);
	if (action.compareTo("ScaledCutInstanceDir") == 0) scaledCutInstanceDir(args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
	// if (action.compareTo("") == 0) ;
    }

    private static void testShow(String filename) throws IOException {
    }

    private static void testCopy(String srcFile, String dstFile) throws IOException {
    	ImageToolkit srcImg = readJPEG(srcFile);
    	ImageToolkit dstImg = new ImageToolkit(new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), srcImg.getType()));

    	int x, y;
    	for (y=0; y<srcImg.getHeight(); ++y ) for (x=0; x<srcImg.getWidth(); ++x) {
    		dstImg.setRGB(x, y, srcImg.getRGB(x, y));
    	}

    	dstImg.writeJPEG(dstFile);
    }

    public ImageToolkit(BufferedImage bufferedImage) {
    	super(bufferedImage.getColorModel(), bufferedImage.getRaster(), bufferedImage.isAlphaPremultiplied(), getProperties(bufferedImage));
    }

    public static ImageToolkit readJPEG(String filename) throws IOException, ImageFormatException {
	InputStream in = new FileInputStream(filename);
	JPEGImageDecoder imgDec = JPEGCodec.createJPEGDecoder(in);
	BufferedImage bufImg = imgDec.decodeAsBufferedImage();
	ImageToolkit imgToolkit = new ImageToolkit(bufImg);
	in.close();
	return(imgToolkit);
    }

    public void writeJPEG(String filename) throws IOException {
	OutputStream out = new FileOutputStream(filename);
	JPEGImageEncoder imgEnc = JPEGCodec.createJPEGEncoder(out);
	imgEnc.encode(this);
	out.close();
    }
	
    public void writeJPEG(String filename, float quality, boolean forceBaseline) throws IOException {
	OutputStream out = new FileOutputStream(filename);
	JPEGImageEncoder imgEnc = JPEGCodec.createJPEGEncoder(out);

	JPEGEncodeParam jep = imgEnc.getDefaultJPEGEncodeParam(this);
	jep.setQuality(quality, forceBaseline);	

	imgEnc.encode(this, jep);
	out.close();
    }
	
    public static Hashtable getProperties(BufferedImage bufferedImage) {
	Hashtable result = new Hashtable();
	String names[] = bufferedImage.getPropertyNames();
	int i;

	if (names!=null) 
		for (i=0; i<names.length; ++i) 
			result.put(names[i], bufferedImage.getProperty(names[i]));
	return(result);
    }

    public Hashtable getProperties() {
    	return(getProperties(this));
    }

    public static int getRed(int rgb) {
    	return((rgb & R_MASK) >> R_SHIFT);
    }

    public static int getGreen(int rgb) {
    	return((rgb & G_MASK) >> G_SHIFT);
    }

    public static int getBlue(int rgb) {
    	return((rgb & B_MASK) >> B_SHIFT);
    }

    public int getRed(int x, int y) {
	return(getRed(getRGB(x, y)));
    }

    public int getGreen(int x, int y) {
	return(getGreen(getRGB(x, y)));
    }

    public int getBlue(int x, int y) {
	return(getBlue(getRGB(x, y)));
    }

    public void setRed(int x, int y, int value) {
	setRGB(x, y, (getRGB(x, y) & ~R_MASK) | (value << R_SHIFT));
    }

    public void setGreen(int x, int y, int value) {
	setRGB(x, y, (getRGB(x, y) & ~G_MASK) | (value << G_SHIFT));
    }

    public void setBlue(int x, int y, int value) {
	setRGB(x, y, (getRGB(x, y) & ~B_MASK) | (value << B_SHIFT));
    }

    public static int getRGB(int r, int g, int b) {
    	return((r << R_SHIFT) | (g << G_SHIFT) | (b << B_SHIFT));
    }

    public void setRGB(int x, int y, int r, int g, int b) {
	setRGB(x, y, (getRGB(x, y) & ~RGB_MASK) | (r << R_SHIFT) | (g << G_SHIFT) | (b << B_SHIFT));
    }

    public void setImage(ImageToolkit img, int sx, int sy) {
    	int x, y;
    	for (y=0; y<img.getHeight(); ++y ) for (x=0; x<img.getWidth(); ++x) {
    		setRGB(sx+x, sy+y, img.getRGB(x, y));
    	}
    }

    public ImageToolkit spreadColor(double black, double white) {
	black = MathHelper.adjustToRange(0, black, 1);
	white = MathHelper.adjustToRange(0, white, 1);

	// Red, Green, Blue histogram
	long r[] = new long[NUMBER_OF_COLORS];
	long g[] = new long[NUMBER_OF_COLORS];
	long b[] = new long[NUMBER_OF_COLORS];

	// Width, Height
	int w = getWidth(), h = getHeight();
	
	// Iterator, Size
	int i, s = w * h;

	// Red, Green, Blue, White and Black Index
	int rwi = 0, rbi = NUMBER_OF_COLORS - 1;
	int gwi = 0, gbi = NUMBER_OF_COLORS - 1;
	int bwi = 0, bbi = NUMBER_OF_COLORS - 1;

	// Red, Green, Blue, White and Black Totals
	int rwt = (int) Math.round(white * s);
	int rbt = (int) Math.round(black * s);
	int gwt = (int) Math.round(white * s);
	int gbt = (int) Math.round(black * s);
	int bwt = (int) Math.round(white * s);
	int bbt = (int) Math.round(black * s);

	int[] pixels;
	pixels = getRGB(0, 0, w, h, new int[w * h], 0, w);

	// Calculate Histogram
	for (i=0; i<s; ++i) {
	    ++r[getRed(pixels[i])];
	    ++g[getGreen(pixels[i])];
	    ++b[getBlue(pixels[i])];
	}
	
	// Calculate Black and White index for each color
	for (i=0; i<NUMBER_OF_COLORS; ++i) {
	    if (rbt > 0) { rbt -= r[i]; rbi = i; }
	    if (rwt > 0) { rwt -= r[NUMBER_OF_COLORS - 1 - i]; rwi = NUMBER_OF_COLORS - 1 - i; }
	    if (gbt > 0) { gbt -= g[i]; gbi = i; }
	    if (gwt > 0) { gwt -= g[NUMBER_OF_COLORS - 1 - i]; gwi = NUMBER_OF_COLORS - 1 - i; }
	    if (bbt > 0) { bbt -= b[i]; bbi = i; }
	    if (bwt > 0) { bwt -= b[NUMBER_OF_COLORS - 1 - i]; bwi = NUMBER_OF_COLORS - 1 - i; }
	}

	// Calculate range of each color
	double rr = (double) (NUMBER_OF_COLORS - 1) / (rwi - rbi);
	double gr = (double) (NUMBER_OF_COLORS - 1) / (gwi - gbi);
	double br = (double) (NUMBER_OF_COLORS - 1) / (bwi - bbi);

System.out.println("rwi = " + rwi);
System.out.println("rbi = " + rbi);
System.out.println("gwi = " + gwi);
System.out.println("gbi = " + gbi);
System.out.println("bwi = " + bwi);
System.out.println("bbi = " + bbi);
System.out.println("");


	// Spread color
	for (i=0; i<s; ++i) {
	    pixels[i] = getRGB(
  	        (int) Math.round((MathHelper.adjustToRange(0, (getRed(pixels[i]) - rbi) * rr, NUMBER_OF_COLORS - 1))),
	        (int) Math.round((MathHelper.adjustToRange(0, (getGreen(pixels[i]) - gbi) * gr, NUMBER_OF_COLORS - 1))),
	        (int) Math.round((MathHelper.adjustToRange(0, (getBlue(pixels[i]) - bbi) * br, NUMBER_OF_COLORS - 1)))
	    );
	}

	setRGB(0, 0, w, h, pixels, 0, w);
	return(this);
    }

    public ImageToolkit getRotatedInstance(int r) {
	// r - rotation right: 0 - none, 1 - 90, 2 - 180, 3 - 270
	int w = getWidth(), h = getHeight();
	int i, j, x, y;
	int[] srcPixels, dstPixels;
	ImageToolkit result = null;

	switch (r % 4) {
		case 0: result = this; break;
		case 1:
			result = new ImageToolkit(new BufferedImage(h, w, getType()));
			srcPixels = getRGB(0, 0, w, h, new int[w * h], 0, w);
			dstPixels = new int[w * h];

			for (j=0; j<h; ++j) for (i=0; i<w; ++i) {
				y = i; x = h - 1 - j;
				dstPixels[y*h+x] = srcPixels[j*w+i];
			}
			result.setRGB(0, 0, h, w, dstPixels, 0, h);
	        break;
		case 2:
			result = new ImageToolkit(new BufferedImage(w, h, getType()));
			srcPixels = getRGB(0, 0, w, h, new int[w * h], 0, w);
			dstPixels = new int[w * h];

			for (j=0; j<h; ++j) for (i=0; i<w; ++i) {
				x = w - 1 - i; y = h - 1 - j;
				dstPixels[y*w+x] = srcPixels[j*w+i];
			}
			result.setRGB(0, 0, w, h, dstPixels, 0, w);
	        break;
		case 3:
			result = new ImageToolkit(new BufferedImage(h, w, getType()));
			srcPixels = getRGB(0, 0, w, h, new int[w * h], 0, w);
			dstPixels = new int[w * h];

			for (j=0; j<h; ++j) for (i=0; i<w; ++i) {
				x = j; y = w - 1 - i;
				dstPixels[y*h+x] = srcPixels[j*w+i];
			}
			result.setRGB(0, 0, h, w, dstPixels, 0, h);
	        break;
	}
	return(result);
    }

    public static int getAverageColor(int[] pixels, int i, int len, int stride) {
	int k, p;
	double r = 0, g = 0, b = 0;

	for (k=0; k<len; ++k) {
	    p = i + k * stride;
	    r += getRed(pixels[p]);
	    g += getGreen(pixels[p]);
	    b += getBlue(pixels[p]);
	}

	return(getRGB((int) Math.round(r/len), (int) Math.round(g/len), (int) Math.round(b/len)));
    }

    public static int getScaledPixel(int pixel, double scale) {
	return(getRGB(
	    (int) Math.round(getRed(pixel) * scale), 
	    (int) Math.round(getGreen(pixel) * scale), 
	    (int) Math.round(getBlue(pixel) * scale)
	));
    }

    public static void setAverageColor(int[] pixels, int i, int len, int stride, int sRGB, int eRGB) {
	int k;
	int sR = getRed(sRGB); int sG = getGreen(sRGB); int sB = getBlue(sRGB);

	double kR = (getRed(eRGB) - sR) / len;
	double kG = (getGreen(eRGB) - sG) / len;
	double kB = (getBlue(eRGB) - sB) / len;

	for (k=0; k<len; ++k)
	    pixels[i + k * stride] = ImageToolkit.getRGB(
	    	(int) Math.round(sR + k * kR),
	    	(int) Math.round(sG + k * kG),
	    	(int) Math.round(sB + k * kB)
	    );
    }

    public ImageToolkit getCopy() {
	int W = getWidth(), H = getHeight();
	int[] orgPixels = getRGB(0, 0, W, H, new int[W * H], 0, W);

	ImageToolkit result = new ImageToolkit(new BufferedImage(W, H, getType()));
	result.setRGB(0, 0, W, H, orgPixels, 0, W);

	return(result);
    }


    public ImageToolkit getScaledCutInstance(int sw, int sh) {
	ImageToolkit scaled = null, result = null;

	if ((sw >=0)  && (sh >= 0)) {
   	  int W = getWidth(), H = getHeight();

	  scaled = getScaledInstance(
	    Math.max(sw, (int) Math.round(((double) W) * sh / H)),
	    Math.max(sh, (int) Math.round(((double) H) * sw / W))
	  );

	  W = scaled.getWidth(); H = scaled.getHeight();
	  result = new ImageToolkit(scaled.getSubimage((W - sw) / 2, (H - sh) / 2, sw, sh));
	} else {
	  result = getScaledInstance(sw, sh);
	}

	return result;
    }

    public ImageToolkit getScaledInstance(int sw, int sh) {
	int W = getWidth(), H = getHeight();

	if (sw < 0) sw = (int) Math.round(((double) W) * sh / H);
	if (sh < 0) sh = (int) Math.round(((double) H) * sw / W);

	int i, j;

	if (((((float) W) / sw) == 1) && ((((float) H) / sh) == 1)) return(this);

	int[] orgPixels = getRGB(0, 0, W, H, new int[W * H], 0, W);
	int[] newPixels;

	// Scale Horizontally
	if (W > sw) {
	    // Make smaller - average
	    double x = ((double) W) / sw; 
	    newPixels = new int[sw * H];

	    for (j=0; j<H; ++j) for (i=0; i<sw; ++i)
		newPixels[j * sw + i] = getAverageColor(orgPixels, (int) (j * W + Math.round(i * x)), (int) (Math.round((i+1) * x) - Math.round(i * x)), 1);

	    orgPixels = newPixels;
	} else if (W < sw) {	
	    // Make bigger - smooth
	    double x = ((double) sw) / (W-1); 
	    newPixels = new int[sw * H];

	    for (j=0; j<H; ++j) for (i=0; i<W-1; ++i)
		setAverageColor(newPixels, (int) Math.round(j * sw + i * x), (int) (Math.round((i+1) * x) - Math.round(i * x)), 1, orgPixels[j * W + i], orgPixels[j * W + i + 1]);

	    orgPixels = newPixels;
	}

	// Scale Vertically
	if (H > sh) {
	    // Make smaller - average
	    double y = ((double) H) / sh; 
	    newPixels = new int[sw * sh];

	    for (j=0; j<sh; ++j) for (i=0; i<sw; ++i)
		newPixels[j * sw + i] = getAverageColor(orgPixels, (int) (Math.round(j*y) * sw + i), (int) (Math.round((j+1) * y) - Math.round(j * y)), sw);

	    orgPixels = newPixels;
	} else if (H < sh) {
	    // Make bigger - smooth
	    double y = ((double) sh) / (H-1); 
	    newPixels = new int[sw * sh];

	    for (j=0; j<H-1; ++j) for (i=0; i<sw; ++i)
		setAverageColor(newPixels, (int) Math.round(j*y) * sw + i, (int) (Math.round((j+1) * y) - Math.round(j * y)), sw, orgPixels[j * sw + i], orgPixels[(j+1) * sw + i]);

	    orgPixels = newPixels;
	}

	ImageToolkit result = new ImageToolkit(new BufferedImage(sw, sh, getType()));
	result.setRGB(0, 0, sw, sh, orgPixels, 0, sw);

	return(result);
    }

	public static Vector getImageFilenames(File file) {
	    Vector result = new Vector();
	    int i;

	    if (file.isDirectory()) {
		File[] files = file.listFiles();
		for (i=0; i<files.length; ++i) result.addAll(getImageFilenames(files[i]));
	    } else if (file.isFile()) {
		String name = file.getName().toLowerCase();
		if (name.endsWith(".jpeg") || name.endsWith(".jpg")) result.add(file.getAbsolutePath());
	    }

	    return(result);
	}

	public static void resizeDir(String fromDirname, String toDirname, int sw, int sh, int rotate, int resizeType) {
	// resizeType: 0: enlarge or reduce; 1: reduce only; 2: enlarge only
		int count = 0;
	        boolean dstPortrait = sh > sw;

		File fromDir = new File(fromDirname);
		File toDir = new File(toDirname);

		fromDirname = fromDir.getAbsolutePath();
		toDirname = toDir.getAbsolutePath();

		Vector imageFiles = ImageToolkit.getImageFilenames(fromDir);

     		for (Enumeration e = imageFiles.elements() ; e.hasMoreElements() ;) {
     		  try {
         	    String imageFilename = (String) e.nextElement();
         	    File dstFile = new File(toDirname + imageFilename.substring(fromDirname.length()));

		    System.out.println("" + (++count) + "/" + imageFiles.size() + ": " + imageFilename);
         	    if (dstFile.exists()) continue;

         	    File srcFile = new File(imageFilename);
         	    dstFile.getParentFile().mkdirs();
         	    ImageToolkit img = ImageToolkit.readJPEG(srcFile.getAbsolutePath());
         	    boolean srcPortrate = img.getHeight() > img.getWidth();

         	    boolean doResize = false;
         	    switch (resizeType) {
			case 0: doResize = true; break;
			case 1: 
	         	      	if (dstPortrait ^ srcPortrate) {
				  if ((img.getHeight() > sw) && (img.getWidth() > sh)) doResize = true; 
	         	      	} else {
				  if ((img.getHeight() > sh) && (img.getWidth() > sw)) doResize = true; 
	         	      	}
				break;
			case 2: 
	         	      	if (dstPortrait ^ srcPortrate) {
				  if ((img.getHeight() < sw) && (img.getWidth() < sh)) doResize = true; 
	         	      	} else {
				  if ((img.getHeight() < sh) && (img.getWidth() < sw)) doResize = true; 
	         	      	}
				break;
         	    }

         	    if (doResize) {
         	      if (dstPortrait ^ srcPortrate) {
			img = img.getScaledInstance(sh, sw);
         	    	img = img.getRotatedInstance(rotate);
         	      } else {
         	    	img = img.getScaledInstance(sw, sh);
         	      }
         	      img.writeJPEG(dstFile.getAbsolutePath(), 1, false);
         	    }
         	  } catch (Exception ee) { System.err.println(ee.getMessage()); ee.printStackTrace(System.err); }
     		}
	}

	public static void scaledCutInstanceDir(String fromDirname, String toDirname, int sw, int sh) {
		int count = 0;

		File fromDir = new File(fromDirname);
		File toDir = new File(toDirname);

		fromDirname = fromDir.getAbsolutePath();
		toDirname = toDir.getAbsolutePath();

		Vector imageFiles = ImageToolkit.getImageFilenames(fromDir);

     		for (Enumeration e = imageFiles.elements() ; e.hasMoreElements() ;) {
     		  try {
         	    String imageFilename = (String) e.nextElement();
         	    File dstFile = new File(toDirname + imageFilename.substring(fromDirname.length()));

		    System.out.println("" + (++count) + "/" + imageFiles.size() + ": " + imageFilename);
         	    if (dstFile.exists()) continue;

         	    File srcFile = new File(imageFilename);
         	    dstFile.getParentFile().mkdirs();
         	    ImageToolkit img = ImageToolkit.readJPEG(srcFile.getAbsolutePath());

		    img = img.getScaledCutInstance(sw, sh);
         	    img.writeJPEG(dstFile.getAbsolutePath(), 1, false);
         	  } catch (Exception ee) { System.err.println(ee.getMessage()); ee.printStackTrace(System.err); }
     		}
	}

    public ImageToolkit morphOne(ImageToolkit dstImg, float delta) {
        delta = Math.min(Math.max(0, delta), 1);

        if (delta == 0 ) return(this);
        if (delta == 1 ) return(dstImg);

	int w = getWidth(), h = getHeight();
	int i, x, j, s;
	int[] srcPixels, dstPixels;
	ImageToolkit result = new ImageToolkit(new BufferedImage(w, h, getType()));

        // Runtime.getRuntime().gc();
        // long maxMem = Runtime.getRuntime().freeMemory();
	// x = (int) Math.min(Integer.MAX_VALUE, maxMem / (2*3*4*2*2) / w);
	x = 1;

	if (x>0) {
	  j=0; 
          while (j<h) {
            System.out.println("" + new Date().toString() + " " + j + " of " + h + " ");
	    if (j + x > h) x = h - j;
	    s = w * x * 3;

	    srcPixels = getRaster().getPixels(0, j, w, x, new int[s]);
	    dstPixels = dstImg.getRaster().getPixels(0, j, w, x, new int[s]);

	    for (i=0; i<s; ++i) {
		dstPixels[i] = (int) (srcPixels[i] + (dstPixels[i] - srcPixels[i]) * delta);
	    }

	    srcPixels = null;
	    result.getRaster().setPixels(0, j, w, x, dstPixels);
	    dstPixels = null;

	    j += x;

          }
	}
	return(result);
    }

	public static void morphSeq(String fromFilename, String toFilename, float delta) throws IOException {
		System.out.println("" + new Date().toString() + ": Loading " + fromFilename);
		ImageToolkit fromImg = ImageToolkit.readJPEG(fromFilename);
		System.out.println("" + new Date().toString() + ": Loading " + toFilename);
		ImageToolkit toImg = ImageToolkit.readJPEG(toFilename);

		int H = Math.max(fromImg.getHeight(), toImg.getHeight());
		int W = Math.max(fromImg.getWidth(), toImg.getWidth());

		System.out.println("" + new Date().toString() + ": Scaling " + fromFilename);
		fromImg = fromImg.getScaledInstance(W, H);
		System.out.println("" + new Date().toString() + ": Scaling " + toFilename);
		toImg = toImg.getScaledInstance(W, H);

		String resFilename = toFilename + "_" + delta + ".jpeg";
		System.out.println("" + new Date().toString() + ": Morphing " + fromFilename + " and " + toFilename);
		ImageToolkit result = fromImg.morphOne(toImg, delta);
		System.out.println("" + new Date().toString() + ": Saving " + resFilename);
		result.writeJPEG(resFilename, 1, false);
	}

	public static void morphSeq(String fromFilename, String toFilename, int steps) throws IOException {
		ImageToolkit fromImg = ImageToolkit.readJPEG(fromFilename);
		ImageToolkit toImg = ImageToolkit.readJPEG(toFilename);

		int H = Math.max(fromImg.getHeight(), toImg.getHeight());
		int W = Math.max(fromImg.getWidth(), toImg.getWidth());

		fromImg = fromImg.getScaledInstance(W, H);
		toImg = toImg.getScaledInstance(W, H);

		int i;

		for (i=0; i<=steps; ++i) {
			String resFilename = toFilename + "_" + (i / 100) + ((i % 100) / 10) + (i % 10) + ".jpeg";
System.out.println("Processing " + i + " of " + steps + ": " + resFilename);
			ImageToolkit result = fromImg.morphOne(toImg, ((float) i) / steps);
			result.writeJPEG(resFilename, 1, false);
		}
	}

	public static void cutOut(String from, String to, int x, int y, int w, int h) throws Exception {
		new ImageToolkit(ImageToolkit.readJPEG(from).getSubimage(x, y, w, h)).writeJPEG(to, 1, false);
	}

	public static void fadeFileBorders(String from, String to, int KX, int KY, double margin) throws Exception {
		new ImageToolkit(ImageToolkit.readJPEG(from).fadeBorders(KX, KY, margin)).writeJPEG(to, 1, false);
	}

	public ImageToolkit fadeBorders(int KX, int KY, double margin) {
	    if (margin <= 0) return(this);

	    int W = getWidth(), H = getHeight();
	    int w = W / KX, h = H / KY;
	    int marginY = (int) Math.round(margin * h);
	    int marginX = (int) Math.round(margin * w);
	    int sw = (w -  marginX), sh = (h - marginY);
	    int nw = sw * (KX - 1) + w;
	    int nh = sh * (KY - 1) + h;
	    double s1, s2, s3, s4, s;

	    int i, j, kx, ky, px, py, pi, pj, avgCount;
	    double wi, wj;
	    int[] orgPixels = getRGB(0, 0, W, H, new int[W * H], 0, W);

	    int[] newPixels = new int[j = nw * nh];
	    for (i=0; i<j; ++i) newPixels[i] = 0;

	    for (j=0; j<H; ++j) for (i=0; i<W; ++i) {
	    	ky = j / h;  kx = i / w;  // Picture number
	    	py = ky * marginY; px = kx * marginX; // Shift
	    	pj = j - ky * h;  pi = i - kx * w;  // Relative position to start of picture

	    	wi = wj = 1;

		if ( (pj < marginY) && (ky > 0) ) {
		    wj = ((double) pj + 1) / (marginY + 1);
		}

		if ( ((h-pj) <= marginY) && (ky < (KY-1)) ) {
		    wj = ((double) h - pj) / (marginY + 1);
		}

		if ( (pi < marginX) && (kx > 0) ) {
		    wi = ((double) pi + 1) / (marginX + 1);
		}

		if ( ((w-pi) <= marginX) && (kx < (KX-1)) ) {
		    wi = ((double) w - pi) / (marginX + 1);
		}

		s1 = wj * wi; s4 = (wj - 1) * (1 - wi);
		s2 = (wj - 1) * wi; s3 = wj * (1 - wi);
	        s = (s1 * s1) / (s1 * s1 + s2 * s2 + s3 * s3 + s4 * s4);

	        newPixels[(j - py) * nw + i - px] += getScaledPixel(orgPixels[j * W + i], s);
	    }

	    ImageToolkit result = new ImageToolkit(new BufferedImage(nw, nh, getType()));
	    result.setRGB(0, 0, nw, nh, newPixels, 0, nw);

	    return(result);
	}

	public BufferedImage getSubimage(int x, int y, int w, int h) {
		int W = getWidth(), H = getHeight();
		if ((x >= 0) && (y >= 0) && (x + w <= W) && (y + h <= H)) return super.getSubimage(x, y, w, h);

		int orgX = x>=0?x:0;
		int orgY = y>=0?y:0;

		int orgW = orgX+w-(orgX-x)<=W?w-(orgX-x):W-(orgX-x)-orgX;
		int orgH = orgY+h-(orgY-y)<=H?h-(orgY-y):H-(orgY-y)-orgY;

		BufferedImage result = new BufferedImage(w, h, getType());

		if ((orgW>0) && (orgH>0)) {
			int[] orgPixels = getRGB(orgX, orgY, orgW, orgH, new int[orgW * orgH], 0, orgW);
			result.setRGB(orgX - x, orgY - y, orgW, orgH, orgPixels, 0, orgW);
		}

		return(result);
	}

        public void setImage(ImageToolkit img, int x, int y, int ignoreColor) {
		int W = getWidth(), H = getHeight();
		int w = img.getWidth(), h = img.getHeight();

		int orgX = x>=0?x:0;
		int orgY = y>=0?y:0;

		int orgW = orgX+w-(orgX-x)<=W?w-(orgX-x):W-(orgX-x)-orgX;
		int orgH = orgY+h-(orgY-y)<=H?h-(orgY-y):H-(orgY-y)-orgY;

		if ((orgW>0) && (orgH>0)) {
			int[] orgPixels = img.getRGB(orgX - x, orgY - y, orgW, orgH, new int[orgW * orgH], 0, orgW);

    			int i, j, c;			
		    	for (j=0; j<orgH; ++j) for (i=0; i<orgW; ++i) {
				c = orgPixels[j*orgW+i] & RGB_MASK;
		    		if (c != ignoreColor) setRGB(orgX + i, orgY + j, c);
		    	}
		}
	}

        public void setImage(ImageToolkit img, int x, int y, int ignoreRed, int ignoreGreen, int ignoreBlue, int ignoreRangeEach, int ignoreRangeAll) {
		// At least one (of RGB) must be out of ignore range, and each must be different than ignored component (RGB)
		int W = getWidth(), H = getHeight();
		int w = img.getWidth(), h = img.getHeight();

		int orgX = x>=0?x:0;
		int orgY = y>=0?y:0;

		int orgW = orgX+w-(orgX-x)<=W?w-(orgX-x):W-(orgX-x)-orgX;
		int orgH = orgY+h-(orgY-y)<=H?h-(orgY-y):H-(orgY-y)-orgY;

		if ((orgW>0) && (orgH>0)) {
			int[] orgPixels = img.getRGB(orgX - x, orgY - y, orgW, orgH, new int[orgW * orgH], 0, orgW);

    			int i, j, c;			
		    	for (j=0; j<orgH; ++j) for (i=0; i<orgW; ++i) {
				c = orgPixels[j*orgW+i];
		    		if (
				  (
				    (Math.abs(getRed(c) - ignoreRed) > ignoreRangeAll) ||
				    (Math.abs(getGreen(c) - ignoreGreen) > ignoreRangeAll) ||
				    (Math.abs(getBlue(c) - ignoreBlue) > ignoreRangeAll)
				  ) && 
				  (Math.abs(getRed(c) - ignoreRed) > ignoreRangeEach) &&
				  (Math.abs(getGreen(c) - ignoreGreen) > ignoreRangeEach) &&
				  (Math.abs(getBlue(c) - ignoreBlue) > ignoreRangeEach)
				) setRGB(orgX + i, orgY + j, c);
		    	}
		}
	}

	public ImageToolkit averageColor(int ignoreColor) {
		ImageToolkit result = getCopy();
		int W = getWidth(), H = getHeight();

		int x, y, i, j, n, r, g, b, c;
		for (y=0; y<H; ++y) for (x=0; x<W; ++x) {
		  if ((result.getRGB(x, y) & RGB_MASK) == ignoreColor) {
		    r = g = b = n = 0;
		    for (i=-1; i<2; ++i) for (j=-1; j<2; ++j) {
		      if ((x + i>=0) && (x + i<W) && (y + j>=0) && (y + j<H)) {
		        c = getRGB(x + i, y + j);
		        if ((c & RGB_MASK) != 0) {
		          r += getRed(c);
		          g += getGreen(c);
		          b += getBlue(c);
			  ++n;
			}
		      }
		    }
		    if (n>0) {
		      result.setRGB(x, y, r/n, g/n, b/n);
		    }
		  }
		}

		return result;
	}
}

