package com.tomaszmozolewski.jpegtools;

import java.awt.*;

public class ComparablePoint extends java.awt.geom.Point2D.Double implements Comparable {
	public ComparablePoint(int x, int y) {
		super(x, y);
	}

	public ComparablePoint(double x, double y) {
		super();
		setLocation(x, y);
	}

	public ComparablePoint(java.awt.geom.Point2D.Double p) {
		super(p.getX(), p.getY());
	}

	public ComparablePoint(ComparablePoint p) {
		this((java.awt.geom.Point2D.Double) p);
	}

	public int compareTo(Object o) {
		return compareTo((java.awt.geom.Point2D.Double) o);
	}

	public int compareTo(java.awt.geom.Point2D.Double p) {
		double y = getY() - p.getY();
		if (y > 0) return 1;
		if (y < 0) return -1;

		double x = getX() - p.getX();
		if (x > 0) return 1;
		if (x < 0) return -1;

		return 0;
	}
}

