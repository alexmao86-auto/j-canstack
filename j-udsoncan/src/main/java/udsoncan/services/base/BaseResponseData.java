package udsoncan.services.base;

public class BaseResponseData {
	public BaseResponseData(Class service_class) throws Exception {
		if (!(service_class.getSuperclass().equals(BaseService.class))) {
			throw new Exception("service_class must be a service class");
		}

		this.service_class = service_class;
	}

	public Class service_class;
	public Byte echo = null;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<");
		builder.append(this.getClass().getSimpleName());
		builder.append("(");
		builder.append(this.service_class.getSimpleName());
		builder.append(")");
//		builder.append(") at 0x");
//		builder.append(this.getClass());
		builder.append(">");
		return builder.toString();
	}

}
