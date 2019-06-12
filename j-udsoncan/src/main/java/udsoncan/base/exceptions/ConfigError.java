package udsoncan.base.exceptions;

public class ConfigError extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8355112925440118105L;
	public int key;

	public ConfigError(int key, String message) {
		super(message);
		this.key = key;
	}

	public ConfigError(int key) {
		super("<No details given>");
		this.key = key;
	}

}
