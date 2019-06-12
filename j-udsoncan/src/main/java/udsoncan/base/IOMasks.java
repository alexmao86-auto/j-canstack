package udsoncan.base;

import java.util.HashMap;
import java.util.Map;

/**
 * Allow to specify a list of masks for a
 * :ref:`InputOutputControlByIdentifier<InputOutputControlByIdentifier>`
 * composite codec.
 * 
 * Example : IOMasks(mask1,mask2, mask3=True, mask4=False)
 * 
 * :param args: Masks to set to True :param kwargs: Masks and their values
 */
public class IOMasks {

	private Map<String, Boolean> maskdict = new HashMap<String, Boolean>();

	public IOMasks(String[] args) {
		for (String k : args) {
			this.maskdict.put(k, true);
		}
	}

	public IOMasks(Map<String, Boolean> maskdict) {
		this.maskdict = maskdict;
	}

	public IOMasks(String[] args, Map<String, Boolean> maskdict) {
		this.maskdict = maskdict;
		for (String k : args) {
			this.maskdict.put(k, true);
		}
	}

	public Map<String, Boolean> get_dict() {
		return this.maskdict;
	}
}
