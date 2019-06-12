package udsoncan.services.base;

import java.lang.reflect.Field;

public class BaseSubfunction {

	public Field fields;

	public static String get_name(int subfn_id) {
		String className = null;
		Class clazz = null;
		try {
			throw new Exception();
		} catch (Exception e) {
			StackTraceElement[] element = e.getStackTrace();
			className = element[0].getClassName();
		}
		try {
			clazz = Class.forName(className);
			Field[] fields = clazz.getDeclaredFields();
			for (Field f : fields) {
				if (subfn_id == f.getInt(clazz)) {
					return f.getName();
				}
			}
			String name = (String) clazz.getDeclaredField("__pretty_name__").get(clazz);
			return "Custom " + name;
		} catch (Exception e) {
		}

		return "";

	}

	public static String get_name(Class clazz, Integer subfn_id) {

		try {
			Field[] fields = clazz.getDeclaredFields();
			String name = null;
			for (Field f : fields) {
				name = f.getName();
				if (name.equals("__pretty_name__")) {
					continue;
				}
				if (subfn_id == f.getInt(clazz)) {
					return f.getName();
				}
			}
			name = (String) clazz.getDeclaredField("__pretty_name__").get(clazz);
			return "Custom " + name;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}
}
