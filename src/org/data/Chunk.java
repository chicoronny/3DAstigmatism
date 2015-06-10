package org.data;

public class Chunk {
	public Chunk(final long startPosition, final long loopSize) {
		this.startPosition = startPosition;
		this.loopSize = loopSize;
	}

	public long getStartPosition() {
		return startPosition;
	}

	public long getLoopSize() {
		return loopSize;
	}

	protected long startPosition;

	protected long loopSize;
}
