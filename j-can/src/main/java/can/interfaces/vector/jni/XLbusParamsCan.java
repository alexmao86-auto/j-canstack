package can.interfaces.vector.jni;

public class XLbusParamsCan {
     int bitRate;
     int sjw;
     int tseg1;
     int tseg2;
     int sam;  // 1 or 3
     @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("XLbusParamsCan [bitRate=");
		builder.append(bitRate);
		builder.append(", sjw=");
		builder.append(sjw);
		builder.append(", tseg1=");
		builder.append(tseg1);
		builder.append(", tseg2=");
		builder.append(tseg2);
		builder.append(", sam=");
		builder.append(sam);
		builder.append(", outputMode=");
		builder.append(outputMode);
		builder.append(", canOpMode=");
		builder.append(canOpMode);
		builder.append("]");
		return builder.toString();
	}
	int outputMode;
     int canOpMode;
}
