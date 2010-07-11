package cuanto.adapter.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * String wrapper around OutputStream.
 *
 * @author Suk-Hyun Cho
 */
public class StringOutputStream extends OutputStream {
	private StringBuffer buffer;

	public StringOutputStream() {
		this.buffer = new StringBuffer();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(int b) throws IOException {
		buffer.append((char) b);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the toString() of the buffer
	 */
	@Override
	public String toString() {
		return buffer.toString();
	}
}
