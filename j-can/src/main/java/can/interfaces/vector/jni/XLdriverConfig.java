package can.interfaces.vector.jni;

public class XLdriverConfig {
	public static final int XL_CONFIG_MAX_CHANNELS = 64;
	public int dllVersion = -1;
	public int channelCount = -1;
	public int[] reserved = new int[10];
	public XLchannelConfig[] channel = new XLchannelConfig[XL_CONFIG_MAX_CHANNELS];

	public XLdriverConfig() {
		for (int i = 0; i < XL_CONFIG_MAX_CHANNELS; i++) {
			channel[i] = new XLchannelConfig();
		}
	}
}
