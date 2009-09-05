package com.example.GuitarTuner;

public class PitchDetectionRepresentation {
	PitchDetectionRepresentation(double pitch_, int string_no_, int fret_) {
		pitch = pitch_; string_no = string_no_; fret = fret_;
		creation_date_ = System.currentTimeMillis();
	}
	
	public int GetAlpha() {
		final long age = System.currentTimeMillis() - creation_date_;
		if (age > LIFE_TIME_MS) return 0;
		return (int) Math.floor(255 - age * 1.0 / LIFE_TIME_MS * 255);
	}
	
	
	public double pitch;
	public int string_no;
	public int fret;
	private long creation_date_;
	
	private final static int LIFE_TIME_MS = 3000;

}
