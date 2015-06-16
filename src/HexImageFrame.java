package com.tomaszmozolewski.jpegtools;

import java.io.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

public class HexImageFrame extends JFrame implements MouseListener, MouseWheelListener {
	final HexImageFrameParams hifp = new HexImageFrameParams();
	final static Random rnd = new Random();

	public static Color getRandomColor() {
	  return new Color(((rnd.nextInt(2) * 255) * 256 + rnd.nextInt(2) * 255) * 256 + rnd.nextInt(2) * 255);
	}

	protected void loadImage() {
		int maxWidth = getWidth() - 11;
		int maxHeight = getHeight() - 60;

		try {
		  hifp.filename = (String) hifp.imgFiles.elementAt(hifp.imgIndex);
		  setTitle(hifp.filename);
		  hifp.orgImg = ImageToolkit.readJPEG(hifp.filename);
		  hifp.img = hifp.orgImg;

		  if ((hifp.img.getWidth() > maxWidth) || (hifp.img.getHeight() > maxHeight)) {
		    hifp.scale = Math.min((double) maxWidth / hifp.img.getWidth(), (double) maxHeight / hifp.img.getHeight());
		    hifp.img = hifp.img.getScaledInstance((int) Math.round(hifp.img.getWidth() * hifp.scale), (int) Math.round(hifp.img.getHeight() * hifp.scale));
		  } else {
		    hifp.scale = 1;
		  }
		  hifp.length = (int) Math.min(hifp.img.getWidth() / 2, hifp.img.getHeight() / Math.sqrt(3));
		  hifp.maxLength = hifp.length;

		} catch (Exception e) {
		  System.err.println(e.getMessage());
		  e.printStackTrace(System.err);
		  hifp.img = null;
		  hifp.filename = null;
		}
		hifp.jl.setIcon(new ImageIcon(hifp.img));
	}
	
	public HexImageFrame(Vector imgFiles) {		
		if (imgFiles.size() <= 0) {
		  System.err.println("No files specified, or no jpeg files found in specified location");
		  dispose();
		  return;
		}

		hifp.imgFiles = imgFiles;
		addWindowListener(new WindowAdapter() {
		  public void windowClosing(WindowEvent e) {
		    dispose();
		  }
		});

         	GridBagLayout gridbag = new GridBagLayout();
         	GridBagConstraints c = new GridBagConstraints();
		JPanel jp = new JPanel(gridbag);
	        setContentPane(jp);

		hifp.jl = new JLabel();
		hifp.jl.addMouseListener(this);
		hifp.jl.addMouseWheelListener(this);

		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = 1;
		gridbag.setConstraints(hifp.jl, c);
		jp.add(hifp.jl, c);

		JButton jbPrev = new JButton("Previous");
		jbPrev.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
		    hifp.movePrev();
		    loadImage();
		  }
		});
		c.gridwidth = 1;
		c.gridheight = 0;
		gridbag.setConstraints(jbPrev, c);
		jp.add(jbPrev, c);

		JButton jbNext = new JButton("Next");
		jbNext.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
		    hifp.moveNext();
		    loadImage();
		  }
		});
		c.gridwidth = 1;
		c.gridheight = 0;
		gridbag.setConstraints(jbNext, c);
		jp.add(jbNext, c);

		JButton jbSave = new JButton("Save");
		jbSave.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
		    setTitle(hifp.filename + " -> " + hifp.saveSelection());
		  }
		});

		c.gridwidth = 1;
		c.gridheight = 0;
		gridbag.setConstraints(jbSave, c);
		jp.add(jbSave, c);

		JButton jbSaveNext = new JButton("Save & Next");
		jbSaveNext.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent e) {
		    hifp.saveSelection();
		    hifp.moveNext();
  		    loadImage();
		  }
		});

		c.gridwidth = 1;
		c.gridheight = 0;
		gridbag.setConstraints(jbSaveNext, c);
		jp.add(jbSaveNext, c);


		setSize(1024, 768);	
		loadImage();
		show();
	}

	public void drawHex() {
		Graphics g = hifp.jl.getGraphics();
		hifp.jl.paint(g);
		g.setColor(hifp.drawColor);
		ImageHexer.drawHexBorder(g, new java.awt.geom.Point2D.Double(hifp.mouseClickPos.getX(), hifp.mouseClickPos.getY()), hifp.length, new Dimension(hifp.img.getWidth(), hifp.img.getHeight()));
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == e.BUTTON1) {
		  hifp.mouseClickPos = e.getPoint();
		} else if (e.getButton() == e.BUTTON3) {
		  hifp.drawColor = getRandomColor();
		}
		drawHex();
	}

	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	public void mouseWheelMoved(MouseWheelEvent e) {
		hifp.length -= Math.pow(10, Math.abs(e.getWheelRotation())-1) / e.getWheelRotation();
		if (hifp.length < 1) hifp.length = 1;
		if (hifp.length > hifp.maxLength) hifp.length = hifp.maxLength;
		drawHex();
	}
}

class HexImageFrameParams {
	public Vector imgFiles;
	public int imgIndex;
	public JLabel jl;
	public String filename;
	public ImageToolkit img;
	public ImageToolkit orgImg;
	public double scale;
	public int length;
	public int maxLength;
	public Point mouseClickPos;
	public Color drawColor;
	private long saveCount;

	public HexImageFrameParams() {
		imgIndex = 0;
		scale = 1;
		mouseClickPos = new Point(0, 0);
		saveCount = 10000;
		drawColor = new Color(0);
	}

	public String saveSelection() {
	    int sLength = (int) Math.round(length / scale);
	    int sHeight = (int) Math.round(sLength * Math.sqrt(3) / 2);

	    int x = (int) Math.round(mouseClickPos.getX() / scale) - sLength;
	    int y = (int) Math.round(mouseClickPos.getY() / scale) - sHeight;
	    int w = sLength * 2;
	    int h = sHeight * 2;

	    if (x < 0) x = 0; 
	    if (y < 0) y = 0;
	    if (x + w >= orgImg.getWidth()) w = orgImg.getWidth() - x - 1;
	    if (y + h >= orgImg.getHeight()) h = orgImg.getHeight() - y - 1;

	    ImageToolkit simg = new ImageToolkit(orgImg.getSubimage(x, y, w, h));

	    String sFilename = "Not Saved !";
	    try {
		File f;

		do {
		  sFilename = filename.substring(0, filename.lastIndexOf('.')) + "_hex_" + (++saveCount) + ".jpeg";
		  f = new File(sFilename);
		} while (f.exists());
	    	simg.writeJPEG(sFilename); 
	    } catch (Exception ioe) {
	      System.err.println(ioe.getMessage());
	      ioe.printStackTrace(System.err);
	    }
	    return sFilename;
 	}

	public void moveNext() {
		imgIndex = (imgIndex + 1) % imgFiles.size();
	}

	public void movePrev() {
		imgIndex = (imgIndex + imgFiles.size() - 1) % imgFiles.size();
	}
}
