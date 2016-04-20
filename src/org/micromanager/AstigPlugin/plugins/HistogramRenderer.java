package org.micromanager.AstigPlugin.plugins;

import ij.process.ByteProcessor;

import org.micromanager.AstigPlugin.interfaces.Element;
import org.micromanager.AstigPlugin.pipeline.ElementMap;
import org.micromanager.AstigPlugin.pipeline.LocalizationPrecision3D;
import org.micromanager.AstigPlugin.pipeline.Renderer;
import org.micromanager.AstigPlugin.tools.LemmingUtils;

public class HistogramRenderer extends Renderer {

	private final int xBins;
	private final double xmin;
	private final double ymin;
	private final double xwidth;
	private final double ywidth;
	private volatile byte[] values; // volatile keyword keeps the array on the heap available
	private final double xmax;
	private final double ymax;
	private final double zmin;
	private final double zmax;

	public HistogramRenderer() {
		this(256, 256, 0, 256, 0, 256, 0, 255);
	}

	public HistogramRenderer(int xBins, int yBins, double xmin, double xmax, double ymin, double ymax, double zmin,
			double zmax) {
		this.xBins = xBins;
		this.xmin = xmin;
		this.ymin = ymin;
		this.xmax = xmax;
		this.ymax = ymax;
		this.xwidth = (xmax - xmin) / xBins;
		this.ywidth = (ymax - ymin) / yBins;
		this.zmin = zmin;
		this.zmax = zmax;
		if (Runtime.getRuntime().freeMemory() < (xBins * yBins * 4)) {
			cancel();
			return;
		}
		values = new byte[xBins * yBins];
		ByteProcessor sp = new ByteProcessor(xBins, yBins, values, LemmingUtils.Ice());
		ip.setProcessor(sp);
		ip.updateAndRepaintWindow();
	}

	@Override
	public Element processData(Element data) {
		final double x, y, z;
		if (data instanceof LocalizationPrecision3D) {
			LocalizationPrecision3D loc = (LocalizationPrecision3D) data;
			x = (float) loc.getX();
			y = (float) loc.getY();
			z = (float) loc.getZ();
		} else if (data instanceof ElementMap) {
			ElementMap map = (ElementMap) data;
			try {
				x = map.get("x").doubleValue();
				y = map.get("y").doubleValue();
				z = map.get("z").doubleValue();
			} catch (NullPointerException ne) {
				return null;
			}
		} else {
			return null;
		}
		if (data.isLast())
			cancel();
		long rz = StrictMath.round((z - zmin) / (zmax - zmin) * 256) + 1;

		if ((x >= xmin) && (x <= xmax) && (y >= ymin) && (y <= ymax)) {
			// synchronized(this){
			final long xindex = StrictMath.round((x - xmin) / xwidth);
			final long yindex = StrictMath.round((y - ymin) / ywidth);
			final int index = (int) (xindex + yindex * xBins);
			if (index >= 0 && index < values.length) {
				if (values[index] > 0)
					values[index] = (byte) ((values[index] + rz + 1) / 2);
				else
					values[index] = (byte) rz;
			}
			// }
		}
		return null;
	}

}
