package can.interfaces.vector.jni;

public class XLchannelConfig {
	

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("XLchannelConfig [name=");
		builder.append(name);
		builder.append(", hwType=");
		builder.append(hwType);
		builder.append(", hwIndex=");
		builder.append(hwIndex);
		builder.append(", hwChannel=");
		builder.append(hwChannel);
		builder.append(", transceiverType=");
		builder.append(transceiverType);
		builder.append(", transceiverState=");
		builder.append(transceiverState);
		builder.append(", channelIndex=");
		builder.append(channelIndex);
		builder.append(", channelMask=");
		builder.append(channelMask);
		builder.append(", channelCapabilities=");
		builder.append(channelCapabilities);
		builder.append(", channelBusCapabilities=");
		builder.append(channelBusCapabilities);
		builder.append(", isOnBus=");
		builder.append(isOnBus);
		builder.append(", connectedBusType=");
		builder.append(connectedBusType);
		builder.append(", busParams=");
		builder.append(busParams);
		builder.append("]");
		return builder.toString();
	}
	public String name = "default";
	public int hwType; // !< HWTYPE_xxxx (see above)
	public int hwIndex; // !< Index of the hardware (same type) (0,1,...)
	public int hwChannel; // !< Index of the channel (same hardware) (0,1,...)
	public int transceiverType; // !< TRANSCEIVER_TYPE_xxxx (see above)
	public int transceiverState; // !< transceiver state (XL_TRANSCEIVER_STATUS...)
	public int configError; // !< XL_CHANNEL_CONFIG_ERROR_XXX (see above)
	public int channelIndex; // !< Global channel index (0,1,...)
	public long channelMask; // !< Global channel mask (=1<<channelIndex)
	public int channelCapabilities; // !< capabilities which are supported (e.g CHANNEL_FLAG_XXX)
	public int channelBusCapabilities; // !< what buses are supported and which are possible to be
										// !< activated (e.g. XXX_BUS_ACTIVE_CAP_CAN)
	// Channel
	public int isOnBus; // !< The channel is on bus
	public int connectedBusType; // !< currently selected bus
	public XLbusParamsCan busParams = new XLbusParamsCan();
	public int _doNotUse; // !< introduced for compatibility reasons since EM00056439

	public int driverVersion;
	public int interfaceVersion; // !< version of interface with driver
	public int[] raw_data = new int[10];

	public int serialNumber;
	public int articleNumber;

	public String transceiverName = "default"; // !< name for CANcab or another transceiver

	public int specialCabFlags; // !< XL_SPECIAL_CAB_XXX flags
	public int dominantTimeout; // !< Dominant Timeout in us.
	public int dominantRecessiveDelay; // !< Delay in us.
	public int recessiveDominantDelay; // !< Delay in us.
	public int connectionInfo; // !< XL_CONNECTION_INFO_XXX
	public int currentlyAvailableTimestamps; // !< XL_CURRENTLY_AVAILABLE_TIMESTAMP...
	public int minimalSupplyVoltage; // !< Minimal Supply Voltage of the Cab/Piggy in 1/100 V
	public int maximalSupplyVoltage; // !< Maximal Supply Voltage of the Cab/Piggy in 1/100 V
	public int maximalBaudrate; // !< Maximal supported LIN baudrate
	public int fpgaCoreCapabilities; // !< e.g.: XL_FPGA_CORE_TYPE_XXX
	public int specialDeviceStatus; // !< e.g.: XL_SPECIAL_DEVICE_STAT_XXX
	public int channelBusActiveCapabilities; // !< like channelBusCapabilities (but without core dependencies)
	public int breakOffset; // !< compensation for edge asymmetry in ns
	public int delimiterOffset; // !< compensation for edgdfde asymmetry in ns
	public int[] reserved = new int[3];
}
