package com.tomaszmozolewski.mosaicmaker;

import java.io.*;
import java.util.*;
import java.awt.image.*;
import java.awt.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.transform.*;

import com.tomaszmozolewski.jpegtools.*;

public class MosaicMaker implements Serializable {
        private static final String COPYRIGHT = "Copyright @2004 Tomasz Mozolewski mozotom@yahoo.com";
        private static final String COPYRIGHT_ERROR = "Please use unaltered copy of this class.";
        public static final String VERSION = "Mosaic Maker - Version 2.3.0 - 1/1/2005";

	public static final int MAX_STEPS = 10;
        transient protected String confFilename;
        
        protected float scoreBoard[][][];
        transient protected ImageToolkit matchSheet[][];
        transient protected ImageToolkit imgCache[];
        transient protected ImageToolkit srcImg, dstImg;
        protected int rankingTable[][][];
        protected int selectedImages[][];
        protected Vector imgFiles;

        transient protected String dstFilename;
        transient protected String srcFilename; 
        transient protected String dirname;
        transient protected double hexLength;
	transient protected double hexHeight;
	protected int hexXcount;
	protected int hexYcount; 
        transient protected int rep;
        transient protected double scale;
        transient protected double speed;
        transient protected float morphFactor;
        transient protected float similarityFraction;
        transient protected ImageToolkitComparator itc;
        transient protected ImageToolkitComparator simItc;
        transient protected PrintStream out;
	
	public static void main(String args[]) throws IOException {
	    if (verifyCopyright()) {
	        System.out.println(COPYRIGHT);
	        System.out.println(VERSION);
	        if (args.length == 0) {
	        	System.err.println("Please give at least one configuration file as parameter");
	        } else {
	        	int i;
	        	for (i=0; i<args.length; ++i) {
	        	        System.out.println("Configuration file " + (i+1) + " of " + args.length + ": \"" + args[i] +"\"");
	        	        try {
		        		MosaicMaker mm = MosaicMaker.getInstance(args[i]);
		        		if (mm.out != null) mm.out.println(mm.toString());
		        		mm.createMosaic();
		        	} catch(Exception e) {
					System.err.println(e.getMessage());
					System.err.println(e.toString());
					e.printStackTrace(System.err);
		        	}
	        	}
	        }
	    } else {
	    	System.err.println(COPYRIGHT_ERROR);
	    }
	}

	public static String getXMLValue(String attr, String def, org.jdom.Element elm) {
		String result = elm.getAttributeValue(attr);
		return(result==null?def:result);
	}

	public static int getXMLValue(String attr, int def, org.jdom.Element elm) {
		String result = elm.getAttributeValue(attr);
		return(result==null?def:Integer.parseInt(result));
	}

	public static float getXMLValue(String attr, float def, org.jdom.Element elm) {
		String result = elm.getAttributeValue(attr);
		return(result==null?def:Float.parseFloat(result));
	}

	public static double getXMLValue(String attr, double def, org.jdom.Element elm) {
		String result = elm.getAttributeValue(attr);
		return(result==null?def:Double.parseDouble(result));
	}

	public static MosaicMaker getInstance(String confFile) {
		MosaicMaker result = new MosaicMaker();
		result.confFilename = confFile;

		BufferedInputStream in = null;
		SAXBuilder saxBuilder = null;
		Document xmlDoc = null;
		org.jdom.Element root = null;

		try {
			in = new BufferedInputStream(new FileInputStream(confFile));
			saxBuilder = new SAXBuilder();
			xmlDoc = saxBuilder.build(in);
			root = xmlDoc.getRootElement();

		        result.dstFilename = getXMLValue("resultFileName", result.dstFilename, root);
		        result.srcFilename = getXMLValue("originalFileName", result.srcFilename, root);
		        result.dirname = getXMLValue("picturesDirectory", result.dirname, root);
		        result.hexLength = getXMLValue("hexLength", result.hexLength, root);
		        result.rep = getXMLValue("minimumDistanceOfRepetitions", result.rep, root);
		        result.scale = getXMLValue("scale", result.scale, root);
		        result.speed = getXMLValue("speed", result.speed, root);
		        result.morphFactor = getXMLValue("morphFactor", result.morphFactor, root);
		        result.similarityFraction = getXMLValue("similarityFraction", result.similarityFraction, root);

		        String outFilename = root.getAttributeValue("logFile");
		        if (outFilename == null) result.out = null; else
		          if (outFilename.length()==0) result.out = System.out; else
		          result.out = new PrintStream(new FileOutputStream(outFilename));;

			result.hexHeight = result.hexLength * Math.sqrt(3) / 2;

		} catch (Exception e) {
      			System.err.println(e.getMessage());
		} finally {
			if (in != null) try { in.close(); } catch (Exception eic) { }
		}
		return(result);
	}
	
	public MosaicMaker() {
	        dstFilename = "";
	        srcFilename = ""; 
	        dirname = "";
	        hexLength = 50;
	        rep = 5;
	        scale = 1;
	        speed = 5;
	        morphFactor = (float) 0.25;
	        similarityFraction = (float) 0.001;
	        itc = new ImageToolkitCompMaxDiffSqrHex();
	        simItc = new ImageToolkitCompRegErrHex();
		out = System.out;
		confFilename = "";
	}

	public MosaicMaker(String confFilename, String dstFilename, String srcFilename, String dirname, int hexLength, int rep, double scale, double speed, float morphFactor, ImageToolkitComparator itc, ImageToolkitComparator simItc, PrintStream out) throws IOException {
		this.confFilename = confFilename;
	        this.dstFilename = dstFilename;
	        this.srcFilename = srcFilename; 
	        this.dirname = dirname;
	        this.hexLength = hexLength;
	        this.rep = rep;
	        this.scale = scale;
	        this.speed = speed;
	        this.morphFactor = morphFactor;
	        this.itc = itc;
	        this.simItc = simItc;
	        this.out = out;
	}

	protected String getProgressFilename(int step) {
	    return(confFilename + "." + step + ".log");
	}

	protected void saveProgress(int step) throws IOException {
	    if (out != null) out.println("Saving step  " + step + " of 10: " + new Date());
	    ObjectOutputStream saveFile = null;

	    try {
		if (dstImg != null) dstImg.writeJPEG(getProgressFilename(step) + ".jpeg", 1, false);
		saveFile = new ObjectOutputStream(new FileOutputStream(getProgressFilename(step)));
		saveFile.writeObject(this);
	    	if (out != null) out.println("Saved step  " + step + " of 10: " + new Date());
	    } catch (Exception e) {
		System.err.println(e.getMessage());
		e.printStackTrace(System.err);
	    } finally {
		if (saveFile != null) try { saveFile.close(); } catch (Exception fe) {}
	    }	
	}

	protected int loadProgress() throws IOException {
	    ObjectInputStream loadFile = null;
	    int step = 0;

	    try {
	        for (step=MAX_STEPS; step>0; --step)
	            if (new File(getProgressFilename(step)).exists()) break;

	        if (step == 0) return(0);
		if (step < 8) {
	    	  if (out != null) out.println("Loading step  " + step + " of 10: " + new Date());
		  loadFile = new ObjectInputStream(new FileInputStream(getProgressFilename(step)));
		  MosaicMaker mosaicMaker = (MosaicMaker) loadFile.readObject();
	    	  if (out != null) out.println("Loaded step  " + step + " of 10: " + new Date());

        	  this.scoreBoard = mosaicMaker.scoreBoard;
	          this.rankingTable = mosaicMaker.rankingTable;
	          this.selectedImages = mosaicMaker.selectedImages;
	          this.imgFiles = mosaicMaker.imgFiles;
	          this.hexHeight = this.hexLength * Math.sqrt(3) / 2;
	          this.hexXcount = mosaicMaker.hexXcount;
	          this.hexYcount = mosaicMaker.hexYcount;

	          srcImg = mosaicMaker.srcImg;
	          dstImg = mosaicMaker.dstImg;
	        }
	    } catch (Exception e) {
		System.err.println(e.getMessage());
		e.printStackTrace(System.err);
	    } finally {
		if (loadFile != null) try { loadFile.close(); } catch (Exception fe) {}
	    }	

	    if (out != null) out.println("Reseting step  " + step + " of 10: " + new Date());
	    if ((step > 2) && (step < 9)) srcImg = ImageToolkit.readJPEG(srcFilename);

	    if ((step > 6) && (step < 8)) {
		initializeOutput();
	    }

	    if (step == 8) {
		dstImg = ImageToolkit.readJPEG(getProgressFilename(step) + ".jpeg");
	    }
	    if (out != null) out.println("Reset done for step  " + step + " of 10: " + new Date());

	    return(step);
	}
	
	public void createMosaic() throws Exception {
	    int step = loadProgress();
	    if (step < 1) {
		if (out != null) out.println("Step  1 of 10: " + new Date() + " Creating image list");
		imgFiles = ImageToolkit.getImageFilenames(new File(dirname));
		saveProgress(1);
	    }

	    if (step < 2) {
		if (out != null) out.println("Step  2 of 10: " + new Date() + " Reading source image: " + srcFilename);		
		readSrcImg();
	    }

	    if (step < 3) {
		if (out != null) out.println("Step  3 of 10: " + new Date() + " Scoring images, size: " + matchSheet[0][0].getWidth() + " x " + matchSheet[0][0].getHeight());		
		scoreImages();

		matchSheet = null;
		saveProgress(3);
	    }

	    if (step < 4) {
		if (out != null) out.println("Step  4 of 10: " + new Date() + " Ranking images");
		rankImages();
		saveProgress(4);
	    }

	    if (step < 5) {
		if (out != null) out.println("Step  5 of 10: " + new Date() + " Initializing selection");
		initializeSelection();
	    }

	    if (step < 6) {
		if (out != null) out.println("Step  6 of 10: " + new Date() + " Initializing output");		
		initializeOutput();
	    }

	    if (step < 7) {
		if (out != null) out.println("Step  7 of 10: " + new Date() + " Selecting images");
		selectImages();

		scoreBoard = null;
		saveProgress(7);
	    }

	    if (step < 8) {
		if (out != null) out.println("Step  8 of 10: " + new Date() + " joining output image");
		createOutput();

	        rankingTable = null;
	        selectedImages = null;
	        imgFiles = null;
		imgCache = null;
		saveProgress(8);
	    }

	    if (step < 9) {
		if (out != null) out.println("Step  9a of 10: " + new Date() + " averaging blanks");
		dstImg = dstImg.averageColor(0);

		if (out != null) out.println("Step  9b of 10: " + new Date() + " morphing output image " + morphFactor);
		dstImg = dstImg.morphOne(srcImg.getScaledInstance(dstImg.getWidth(), dstImg.getHeight()), morphFactor);

		//if (out != null) out.println("Step  9c of 10: " + new Date() + " adding border");
		//ImageHexer.hexImage(dstImg, (int) Math.round(hexLength * scale), 1);

		srcImg = null;
		saveProgress(9);
	    }

	    if (step < 10) {
		dstImg.writeJPEG(dstFilename, 1, false);
		if (out != null) out.println("Step 10 of 10: " + new Date() + " Done.");
		if (out != null) out.close();
	    }
	}

	public void createOutput() {
	        int w = (int) Math.round(srcImg.getWidth() * scale);
	        int h = (int) Math.round(srcImg.getHeight() * scale);

	        int i, j;

	        int sw = (int) Math.round(hexLength * scale * 2);
	        int sh = (int) Math.round(hexHeight * scale * 2);

		dstImg = new ImageToolkit(new BufferedImage(w, h, srcImg.getType()));
		for (j=0; j<hexYcount; ++j) for (i=0; i<hexXcount; ++i) {
			if (out != null) out.println("" + new Date() + " - joining: " + (j*hexXcount+i+1) + " of " + (hexXcount*hexYcount) + " - " + rankingTable[i][j][selectedImages[i][j]] + " ");

			dstImg.setImage(
			  cacheReadScaledImage(i, j, sw, sh), 
			  (int) Math.round((1.5 * i - 1) * hexLength * scale),
			  (int) Math.round((2 * j - 1 + i % 2) * hexHeight * scale),
			  0
			);
		}
		if (out != null) out.println("");
	}

	public void initializeOutput() {
		imgCache = new ImageToolkit[imgFiles.size()];
		if (out != null) out.println("");
	}

	public ImageToolkit readScaledImage(int imgPos, int sw, int sh) {
		ImageToolkit img = null;
		try { 
			img = ImageToolkit.readJPEG((String) imgFiles.elementAt(imgPos));
			img = ImageHexer.hexImage(img.getScaledCutInstance(sw, sh));
		} catch(Exception ioe) {
			System.err.println(ioe.getMessage());
			ioe.printStackTrace();
		}
		return img;
	}

	public ImageToolkit cacheReadScaledImage(int imgPos, int sw, int sh) {
		if (imgCache[imgPos] == null) imgCache[imgPos] = readScaledImage(imgPos, sw, sh);
		return imgCache[imgPos];
	}

	public ImageToolkit cacheReadScaledImage(int i, int j, int sw, int sh) {
		int imgPos = rankingTable[i][j][selectedImages[i][j]];
		return cacheReadScaledImage(imgPos, sw, sh);
	}

	public boolean isSimilarImage(int k1, int k2, byte comparedImages[][], int sw, int sh) {
		if (k1 == k2) return true; else {
		  int kk1 = Math.max(k1, k2);
		  int kk2 = Math.min(k1, k2);
		  if (similarityFraction > 0) {
		    if (comparedImages[kk1][kk2] == -1) {
                      double simFrac = simItc.compare(cacheReadScaledImage(kk1, sw, sh), cacheReadScaledImage(kk2, sw, sh));
                      // out.println("simFrac=" + simFrac + " " + kk1 + " x " + kk2);
		      if (simFrac < similarityFraction) {
		        comparedImages[kk1][kk2] = (byte) 1; 
		      } else {
		        comparedImages[kk1][kk2] = (byte) 0;
		      }
		    }
                  }
		  return (comparedImages[kk1][kk2] == 1);
		}
	}

	public Point findSimilarImage(int i1, int j1, int k1, byte comparedImages[][], int sw, int sh) {
	  int i2, j2, s2, k2;
	  for (i2=i1-rep; i2<i1+rep; ++i2) for (j2=j1-rep; j2<j1+rep; ++j2) {
	    if ((i2>=0) && (j2>=0) && (i2<hexXcount) && (j2<hexYcount) && (sqr(i2-i1) + sqr(j2-j1) <= sqr(rep)) && ((i2!=i1) || (j2!=j1))) {
	      s2 = selectedImages[i2][j2];
	      if (s2 >= 0) {
	        k2 = rankingTable[i2][j2][s2];
	        if (isSimilarImage(k1, k2, comparedImages, sw, sh)) return new Point(i2, j2);
	      }
	    }
	  }
	  return null;
	}

	public boolean isSimilarImage(int i1, int j1, int k1, byte comparedImages[][], int sw, int sh) {
	  return (findSimilarImage(i1, j1, k1, comparedImages, sw, sh) != null);
	}

	public void selectImage(int i, int j, byte comparedImages[][]) {
	    int sw = (int) Math.round(hexLength * scale * 2);
	    int sh = (int) Math.round(hexHeight * scale * 2);

	    int s, k = imgFiles.size();
	    int rep2 = sqr(rep);

	    for (s=0; s<k; ++s) {
	      if (!isSimilarImage(i, j, rankingTable[i][j][s], comparedImages, sw, sh)) {
		selectedImages[i][j] = s;
		break;
	      }
	    }
	}

	public void selectImages() {
	    int i, j, x, y;
	    int size2 = hexXcount * hexYcount;

	    int rep2 = sqr(rep);

	    if (out != null) out.println("" + new Date() + " Selecting images...");

	    byte comparedImages[][];
	    comparedImages = new byte[imgFiles.size()][];

	    for (i=1; i<imgFiles.size(); ++i) {
		comparedImages[i] = new byte[i];
		for (j=0; j<i; ++j) comparedImages[i][j] = (byte) -1;
	    }

	    DistancePoint sortedPoints[] = new DistancePoint[hexYcount * hexXcount];
	    Point m = new Point((int) Math.round((srcImg.getWidth() * scale) / 2), (int) Math.round((srcImg.getHeight() * scale) / 2));

	    for (j=0; j<hexYcount; ++j) for (i=0; i<hexXcount; ++i) {
		x = (int) Math.round((1.5 * i) * hexLength * scale);
		y = (int) Math.round((2 * j + i % 2) * hexHeight * scale);

		sortedPoints[j * hexXcount + i] = new DistancePoint(i, j, x, y, m);
	    }
	    Arrays.sort(sortedPoints);

	    for (i=0; i<sortedPoints.length; ++i) {
		selectImage(sortedPoints[i].posX, sortedPoints[i].posY, comparedImages);
		if (out != null) out.println("" + new Date() + " - Selecting images - " + (i+1) + " of " + sortedPoints.length + " - [" + sortedPoints[i].posX + ", " + sortedPoints[i].posY + "] - Choice: " + selectedImages[sortedPoints[i].posX][sortedPoints[i].posY] + " Image: " + rankingTable[sortedPoints[i].posX][sortedPoints[i].posY][selectedImages[sortedPoints[i].posX][sortedPoints[i].posY]] + " ");
	    }

	    if (out != null) out.println("");
	}

	public void initializeSelection() {
	    selectedImages = new int[hexXcount][hexYcount];
	    int i, j, k, x, y, s1, s2, c=0;
	    for (i=0; i<hexXcount; ++i) for (j=0; j<hexYcount; ++j) selectedImages[i][j] = -1;
	    if (out != null) out.println("");
	}

	public void rankImages() {
		rankingTable = new int[hexXcount][hexYcount][imgFiles.size()];
		int i, j, k, x;

		for (i=0; i<hexXcount; ++i) for (j=0; j<hexYcount; ++j) {
			rankingTable[i][j] = getOrder(scoreBoard[i][j]);
		}
	}

	public void scoreImages() throws IOException {
		scoreBoard = new float[hexXcount][hexYcount][imgFiles.size()];
		ImageToolkit littleImg;
		int i, j, k;
		int sw = matchSheet[0][0].getWidth(), sh = matchSheet[0][0].getHeight();

		for (i=0; i<imgFiles.size(); ++i) {
			if (out != null) out.print("" + new Date() + " Scoring: " + (i+1) + " of " + imgFiles.size() + " - [");
			littleImg = readScaledImage(i, sw, sh);

			if (out != null) out.print("C");
			for (j=0; j<hexXcount; ++j) for (k=0; k<hexYcount; ++k) {
				scoreBoard[j][k][i] = (float) itc.compare(matchSheet[j][k], littleImg);
			}
			if (out != null) out.println("]");
		}
		if (out != null) out.println("");
	}

	public void readSrcImg() throws IOException {
		srcImg = ImageToolkit.readJPEG(srcFilename);

		hexXcount = (int) Math.ceil(((double) srcImg.getWidth() + hexLength) / (1.5 * hexLength));
		hexYcount = (int) Math.ceil(((double) srcImg.getHeight() + hexHeight) / (2 * hexHeight));

		double xLen = 2 * hexLength;
		double yLen = 2 * hexHeight;

		int matchX = Math.max(1, Math.min((int) Math.round(xLen), (int) (xLen * scale / speed)));
		int matchY = Math.max(1, Math.min((int) Math.round(yLen), (int) (yLen * scale / speed)));

		// Calculate a (base of hex) and h (high of triangle in hex) from image size
		double a = ((double) matchX) / 2;
		double h = ((double) matchY) / 2;

		// Resize a or h down to match hex proportions
		if (h > a * Math.sqrt(3) / 2) h = a * Math.sqrt(3) / 2;
		if (a > h * 2 * Math.sqrt(3) / 3) a = h * 2 * Math.sqrt(3) / 3;

		matchX = (int) Math.round(a * 2);
		matchY = (int) Math.round(h * 2);

		if (out != null) out.println("Number of small pictures: " + hexXcount + " x " + hexYcount + " = " + (hexXcount * hexYcount) + " ");
		if (out != null) out.println("Size of small pictures: " + matchX + " x " + matchY + " ");
		if (out != null) out.println("Size of scaled pictures: " + ((int) Math.round(hexLength * scale * 2)) + " x " + ((int) Math.round(hexHeight * scale * 2)) + " ");

		matchSheet = new ImageToolkit[hexXcount][hexYcount];
		int i, j;
		for (i=0; i<hexXcount; ++i) for (j=0; j<hexYcount; ++j) {
		    matchSheet[i][j] = new ImageToolkit(srcImg.getSubimage(
			(int) Math.round((1.5 * i - 1) * hexLength), 
			(int) Math.round((2 * j - 1 + i % 2) * hexHeight), 
			(int) Math.round(xLen), 
			(int) Math.round(yLen)
		    )).getScaledInstance(matchX, matchY);

		    // matchSheet[i][j].writeJPEG("a"+i+"x"+j+".jpeg"); // DEBUG: write out hexed pieces
		    matchSheet[i][j] = ImageHexer.hexImage(matchSheet[i][j].getCopy(), 0);
		}
	}

	public int[] getOrder(float[] values) {
		int[] result = new int[values.length];

		FloatOrderKeeper sortTable[] = new FloatOrderKeeper[values.length];
		int i;
		for (i=0; i<values.length; ++i) sortTable[i] = new FloatOrderKeeper(i, values[i]);
		Arrays.sort(sortTable);
		for (i=0; i<values.length; ++i) result[i] = sortTable[i].order;
		return(result);
	}

	public int sqr(int x) {
		return(x*x);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("Configuration file:   ").append(confFilename).append(".").append('\n');
		sb.append("Destination file:     ").append(dstFilename).append(".").append('\n');
		sb.append("Source file:          ").append(srcFilename).append(".").append('\n');
		sb.append("Image database dir:   ").append(dirname).append(".").append('\n');
		sb.append("hexLength:            ").append(hexLength).append(".").append('\n');
		sb.append("hexHeight:            ").append(hexHeight).append(".").append('\n');
		sb.append("Repetitions spaceing: ").append(rep).append(".").append('\n');
		sb.append("Scaling factor:       ").append(scale).append(".").append('\n');
		sb.append("Speed up factor:      ").append(speed).append(".").append('\n');
		sb.append("Morphing factor:      ").append(morphFactor).append(".").append('\n');
		sb.append("Similarity fraction:  ").append(similarityFraction).append(".").append('\n');

		return (sb.toString());
	}

	private static boolean verifyCopyright() {
		int i; double s;
		byte[] b = COPYRIGHT.getBytes();

		s = 0; for (i=0; i<b.length; ++i) s += ((b[i] + 2.3245) * b[i] - 6.2345) * b[i] + 5.4576; if (s != 5.42625200846E7) return(false);
		s = 0; for (i=0; i<b.length; ++i) s += ((b[i] + 0.4765) * b[i] - 5.2456) * b[i] - 0.2356; if (s != 5.335104328049998E7) return(false);
		s = 0; for (i=0; i<b.length; ++i) s += ((b[i] - 1.7685) * b[i] + 3.4745) * b[i] - 5.7876; if (s != 5.228005926540002E7) return(false);

		return(true);
	}
}

class FloatOrderKeeper implements Comparable {
	public int order;
	public float value;

	public FloatOrderKeeper(int order, float value) {
		this.order = order;
		this.value = value;
	}

	public int compareTo(Object o) {
		return(compareTo(((FloatOrderKeeper) o).value));
	} 

	public int compareTo(float f) {
		return(value==f?0:value<f?-1:1);
	} 
}

class DistancePoint extends Point implements Comparable {
	/** Note: this class has a natural ordering that is inconsistent with equals. */
	double distanceFromCenter;
	public int posX, posY;

	public DistancePoint(int posX, int posY, double x, double y, double distanceFromCenter) {
	  super();
	  super.setLocation(x, y);
	  this.distanceFromCenter = distanceFromCenter;
	  this.posX = posX;
	  this.posY = posY;
	} 

	public DistancePoint(int posX, int posY, Point p, double distanceFromCenter) {
	  super(p);
	  this.distanceFromCenter = distanceFromCenter;
	  this.posX = posX;
	  this.posY = posY;
	} 

	public DistancePoint(DistancePoint p) {
	  this(p.posX, p.posY, (Point) p, p.distanceFromCenter);
	} 

	public DistancePoint(int posX, int posY, double x, double y, Point p) {
	  this(posX, posY, x, y, p.distance(x, y));
	} 

	public int compareTo(Object o) {
		return(compareTo((DistancePoint) o));
	} 

	public int compareTo(DistancePoint p) {
		return(distanceFromCenter==p.distanceFromCenter?0:distanceFromCenter<p.distanceFromCenter?-1:1);
	} 
}
