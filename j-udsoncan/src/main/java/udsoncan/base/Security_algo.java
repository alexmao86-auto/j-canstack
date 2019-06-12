package udsoncan.base;

public interface Security_algo {
	public byte[] calKey(byte[] seed, Object params);
}
