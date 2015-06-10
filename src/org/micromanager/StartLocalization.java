package org.micromanager;

import org.calibration.Pipeline;

public class StartLocalization {

	public static void main(String[] args) {

		Pipeline pipe = new Pipeline("config.properties");
		pipe.run();
		System.exit(0);
	}
}
