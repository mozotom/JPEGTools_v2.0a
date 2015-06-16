package com.tomaszmozolewski.helpers;

public class MathHelper {
    public static double adjustToRange(double min, double value, double max) {
	if (value < min) return(min);
	if (value > max) return(max);
	return(value);
    }
}