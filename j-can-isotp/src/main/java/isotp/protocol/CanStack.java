package isotp.protocol;

import can.BusABC;
import can.Message;
import isotp.address.Address;
import isotp.errors.Error_handler;

/**
 * The IsoTP transport using `python-can <https://python-can.readthedocs.io>`_
 * as CAN layer. python-can must be installed in order to use this class. All
 * parameters except the ``bus`` parameter will be given to the
 * :class:`TransportLayer<isotp.TransportLayer>` constructor
 * 
 * :param bus: A python-can bus object implementing ``recv`` and ``send`` :type
 * bus: BusABC
 * 
 * :param address: The address information of CAN messages. Includes the
 * addressing mode, txid/rxid, source/target address and address extension. See
 * :class:`isotp.Address<isotp.Address>` for more details. :type address:
 * isotp.Address
 * 
 * :param error_handler: A function to be called when an error has been
 * detected. An :class:`isotp.protocol.IsoTpError<isotp.protocol.IsoTpError>`
 * (inheriting Exception class) will be given as sole parameter :type
 * error_handler: Callable
 * 
 * :param params: List of parameters for the transport layer :type params: dict
 */
public class CanStack extends TransportLayer {

	public BusABC bus;

	public CanStack(BusABC bus, Address address) {
		super(address);
		this.set_bus(bus);
		this.rxfn =new RxFunction() {
			
			@Override
			public CanMessage recv() {
				Message msg = null;
				try {
					msg = CanStack.this.bus.recv(0l);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (msg != null) {
					return new CanMessage((int) msg.arbitration_id, msg.dlc, msg.data, msg.is_extended_id);// TODO extended_id;
				}
				return null;
			}
		};
		this.txfn = new TxFunction() {
			
			@Override
			public void send(CanMessage msg) {
	
				try {
					CanStack.this.bus.send(new Message(System.currentTimeMillis(), msg.arbitration_id, msg.is_extended_id, null, null, null, null, msg.data, null, null, null, null));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		};
		
	}

	public void tx_canbus(Message msg) {
		this.bus.send(msg);
	}

	public CanMessage rx_canbus() {
		Message msg = null;
		try {
			msg = this.bus.recv(0l);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (msg != null) {
			return new CanMessage((int) msg.arbitration_id, msg.dlc, msg.data, msg.is_extended_id);// TODO extended_id;
		}
		return null;
	}

	private void set_bus(BusABC bus) {
		this.bus = bus;

	}

	public CanStack() {
		// TODO Auto-generated constructor stub
	}

	public CanStack(RxFunction rxfn, TxFunction txfn, Address address, Error_handler error_handler, Params params) {
		super(rxfn, txfn, address, error_handler, params);
		// TODO Auto-generated constructor stub
	}

}
