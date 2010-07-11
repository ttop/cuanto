package cuanto.adapter.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Split the output stream into two streams.
 *
 * @author Suk-Hyun Cho
 */
public class DualOutputStream extends OutputStream {
	private OutputStream stream1;
	private OutputStream stream2;

	/**
	 * Construct a DualOutputStream that splits the stream into the two specified OutputStreams.
	 *
	 * @param stream1 the first OutputStream
	 * @param stream2 the second OutputStream
	 */
	public DualOutputStream(OutputStream stream1, OutputStream stream2) {
		this.stream1 = stream1;
		this.stream2 = stream2;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Output to the two OutputStreams, stream1 firstly and stream2 secondly.
	 * If writing to stream1 throws IOException, stream2 will not be written.
	 */
	@Override
	public void write(int b) throws IOException {
		stream1.write(b);
		stream2.write(b);
	}

	/**
	 * Get the first stream.
	 *
	 * @return the first OutputStream
	 */
	public OutputStream getStream1() {
		return stream1;
	}

	/**
	 * Flush and close the first stream, if applicable, then set it to the specified stream.
	 *
	 * @param stream1 to replace the first OutputStream
	 */
	public void setStream1(OutputStream stream1) {
		if (this.stream1 != null)
			flush(stream1);

		this.stream1 = stream1;
	}

	/**
	 * Get the second stream.
	 *
	 * @return the second OutputStream
	 */
	public OutputStream getStream2() {
		return stream2;
	}

	/**
	 * Flush and close the second stream, if applicable, then set it to the specified stream.
	 *
	 * @param stream2 to replace the second OutputStream
	 */
	public void setStream2(OutputStream stream2) {
		if (this.stream2 != null)
			flush(stream2);

		this.stream2 = stream2;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * Flush and close both the streams.
	 */
	@Override
	protected void finalize() {
		flush(stream1);
		flush(stream2);
	}

	/**
	 * Flush and close the given stream.
	 *
	 * @param stream to flush and close
	 * @throws IOException if flushing fails
	 */
	private void flush(OutputStream stream) {
		try {
			stream1.flush();
		} catch (IOException e) {
			try {
				stream1.close();
			} catch (IOException e1) {
				// ignore
			}
		}
	}
}
