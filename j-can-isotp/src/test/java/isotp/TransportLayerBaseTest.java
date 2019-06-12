package isotp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Assert;

import isotp.errors.Error_handler;
import isotp.errors.IsoTpError;
import isotp.protocol.CanMessage;
import isotp.protocol.RxFunction;
import isotp.protocol.TransportLayer;
import isotp.protocol.TxFunction;
import junit.framework.TestCase;

/**
 * Just a class with some helper such as simulate_rx() to make the tests
 * cleaners.
 */
public abstract class TransportLayerBaseTest extends TestCase {
	public Error_handler error_handler;

	public BlockingQueue<CanMessage> ll_rx_queue;
	public BlockingQueue<CanMessage> ll_tx_queue;
	public Map<String, List<IsoTpError>> error_triggered = new HashMap<String, List<IsoTpError>>();

	public TransportLayer stack;// = new TransportLayer(rxfn, txfn, null, null, null);

	public RxFunction stack_rxfn;
	public TxFunction stack_txfn;

	public TransportLayerBaseTest() {
		super();

		this.ll_rx_queue = new ArrayBlockingQueue<CanMessage>(65535);
		this.ll_tx_queue = new ArrayBlockingQueue<CanMessage>(65535);
		this.error_triggered.clear();
		stack_rxfn = new RxFunction() {
			@Override
			public CanMessage recv() {
				return TransportLayerBaseTest.this.ll_rx_queue.poll();
//				try {
//					return TransportLayerBaseTest.this.ll_rx_queue.take();
//				} catch (InterruptedException e) {
//					return null;
//				}
			}
		};
		stack_txfn = new TxFunction() {
			@Override
			public void send(CanMessage msg) {
				TransportLayerBaseTest.this.ll_tx_queue.add(msg);
			}
		};

		error_handler = new Error_handler() {

			@Override
			public void handle(IsoTpError e) {
				if (!TransportLayerBaseTest.this.error_triggered.containsKey(e.getClass().getName())) {
					List<IsoTpError> errors = new ArrayList<IsoTpError>();
					TransportLayerBaseTest.this.error_triggered.put(e.getClass().getName(), errors);
				}
				TransportLayerBaseTest.this.error_triggered.get(e.getClass().getName()).add(e);
			}
		};
	}

	public byte[] rx_isotp_frame() {
		return this.stack.recv();
	}

	public void tx_isotp_frame(byte[] data) throws Exception {
		this.stack.send(data);
	}

	public CanMessage get_tx_can_msg() {
		return this.ll_tx_queue.poll();
	}

	public byte[] make_payload(int size, int start_val) {

		byte[] pl = new byte[size];
		for (int i = start_val; i < (start_val + size); i++) {
			pl[i - start_val] = (byte) (i % 0x100);
		}
		return pl;
	}

	public byte[] make_payload(int size) {
		return this.make_payload(size, 0);
	}

	public void assert_sent_flow_control(int stmin, int blocksize, byte[] prefix, Byte padding_byte, String extra_msg) {
		CanMessage msg = this.get_tx_can_msg();
		TestCase.assertNotNull(
				"Expected a Flow Control message, but none was sent." + extra_msg == null ? "" : extra_msg, msg);

		byte[] data;
		int index = 0;
		int datalen = 8;
		if (prefix == null && padding_byte == null) {
			datalen = 3;
		} else if (prefix != null && padding_byte == null) {
			datalen = 3 + prefix.length;
		}
		data = new byte[datalen];

		if (prefix != null) {
			System.arraycopy(prefix, 0, data, 0, prefix.length);
			index = prefix.length;
		}

		data[index++] = 0x30;
		data[index++] = (byte) blocksize;
		data[index++] = (byte) stmin;

		if (padding_byte != null) {
			for (; index < 8; index++) {
				data[index] = padding_byte;
			}
		}

		Assert.assertArrayEquals("Message sent is not the wanted flow control" + extra_msg == null ? "" : extra_msg,
				data, msg.data);
		TestCase.assertEquals(String.format("Flow control message has wrong DLC. Expecting=0x%02x, received=0x%02x",
				data.length, msg.dlc), msg.dlc.intValue(), data.length);
	}

	public void assert_error_triggered(String error_type) {
		if (this.error_triggered.containsKey(error_type)) {
			System.out.println(error_type + " exists in dict: (" + this.error_triggered.get(error_type).size() + ")");
			if (this.error_triggered.get(error_type).size() > 0) {
				return;
			}
		}
		throw new AssertionError("Error of type:" + error_type + " is not triggered");
	}

	public void assert_no_error_triggered() {
		if (this.error_triggered.size() > 0) {
			throw new AssertionError(
					String.format("%d errors hsa been triggered while non should have", this.error_triggered.size()));
		}
	}

	public void clear_errors() {
		this.error_triggered.clear();
	}

	public void init_test_case() {
		this.ll_rx_queue.clear();
		this.ll_tx_queue.clear();
		this.error_triggered.clear();
	}

	public void simulate_rx_msg(CanMessage msg) {
		this.ll_rx_queue.add(msg);
	}

	public byte[] make_flow_control_data(int flow_status, int stmin, int blocksize, byte[] prefix) {
		byte[] data;
		int index = 0;

		if (prefix != null) {
			data = new byte[prefix.length + 3];
			System.arraycopy(prefix, 0, data, 0, prefix.length);
			index = prefix.length;
		} else {
			data = new byte[3];
		}

		data[index++] = (byte) (0x30 | (flow_status & 0xF));
		data[index++] = (byte) blocksize;
		data[index++] = (byte) stmin;

		return data;
	}
}
