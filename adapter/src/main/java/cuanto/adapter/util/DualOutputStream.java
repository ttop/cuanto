package cuanto.adapter.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Split the output stream into two streams.
 *
 * @author Suk-Hyun Cho
 */
public class DualOutputStream extends OutputStream {
	private OutputStream firstStream;
	private OutputStream secondStream;

	/**
	 * Construct a DualOutputStream that splits the stream into the two specified OutputStreams.
	 *
	 * @param firstStream  the first OutputStream
	 * @param secondStream the second OutputStream
	 */
	public DualOutputStream(OutputStream firstStream, OutputStream secondStream) {
		this.firstStream = firstStream;
		this.secondStream = secondStream;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Output to the two OutputStreams, firstStream firstly and secondStream secondly.
	 * If writing to firstStream throws IOException, secondStream will not be written.
	 */
	@Override
	public void write(int b) throws IOException {
		firstStream.write(b);
		secondStream.write(b);
	}

	/**
	 * Get the first stream.
	 *
	 * @return the first OutputStream
	 */
	public OutputStream getFirstStream() {
		return firstStream;
	}

	/**
	 * Flush and close the first stream, if applicable, then set it to the specified stream.
	 *
	 * @param firstStream to replace the first OutputStream
	 */
	public void setFirstStream(OutputStream firstStream) {
		if (this.firstStream != null)
			flush(firstStream);

		this.firstStream = firstStream;
	}

	/**
	 * Get the second stream.
	 *
	 * @return the second OutputStream
	 */
	public OutputStream getSecondStream() {
		return secondStream;
	}

	/**
	 * Flush and close the second stream, if applicable, then set it to the specified stream.
	 *
	 * @param secondStream to replace the second OutputStream
	 */
	public void setSecondStream(OutputStream secondStream) {
		if (this.secondStream != null)
			flush(secondStream);

		this.secondStream = secondStream;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Flush and close both the streams.
	 */
	@Override
	protected void finalize() {
		flush(firstStream);
		flush(secondStream);
	}

	/**
	 * Flush and close the given stream.
	 *
	 * @param stream to flush and close
	 */
	private void flush(OutputStream stream) {
		try {
			firstStream.flush();
		} catch (IOException e) {
			try {
				firstStream.close();
			} catch (IOException e1) {
				// ignore
			}
		}
	}
}
