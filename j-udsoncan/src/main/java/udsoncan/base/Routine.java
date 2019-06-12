package udsoncan.base;

/**
 * # Routine class that containes few definitions for usage with nice syntax. #
 * myRoutine = Routine.EraseMemory or print(Routine.name_from_id(myRoutine))
 * 
 * Defines a list of constants that are routine identifiers defined by the UDS
 * standard. This class provides no functionality apart from defining these
 * constants
 * 
 * @classmethod def name_from_id(cls, routine_id): # Helper to print the type of
 *              requests (logging purpose) as defined by ISO-14229:2006, Annex F
 *              if not isinstance(routine_id, int) or routine_id < 0 or
 *              routine_id > 0xFFFF: raise ValueError('Routine ID must be a
 *              valid integer between 0 and 0xFFFF')
 * 
 * 
 */
public class Routine {
	public static final int DeployLoopRoutineID = 0xE200;
	public static final int EraseMemory = 0xFF00;
	public static final int CheckProgrammingDependencies = 0xFF01;
	public static final int EraseMirrorMemoryDTCs = 0xFF02;

	public String name;

	public static String name_from_id(int routine_id) throws Exception {
		// Helper to print the type of requests (logging purpose) as defined by
		// ISO-14229:2006, Annex F
		if (routine_id < 0 || routine_id > 0xFFFF) {
			throw new Exception("Routine ID must be a valid integer between 0 and 0xFFFF");
		}
		if (routine_id >= 0x0000 && routine_id <= 0x00FF)
			return "ISOSAEReserved";
		if (routine_id >= 0x0100 && routine_id <= 0x01FF)
			return "TachographTestIds";
		if (routine_id >= 0x0200 && routine_id <= 0xDFFF)
			return "VehicleManufacturerSpecific";
		if (routine_id >= 0xE000 && routine_id <= 0xE1FF)
			return "OBDTestIds";
		if (routine_id == 0xE200)
			return "DeployLoopRoutineID";
		if (routine_id >= 0xE201 && routine_id <= 0xE2FF)
			return "SafetySystemRoutineIDs";
		if (routine_id >= 0xE300 && routine_id <= 0xEFFF)
			return "ISOSAEReserved";
		if (routine_id >= 0xF000 && routine_id <= 0xFEFF)
			return "SystemSupplierSpecific";
		if (routine_id == 0xFF00)
			return "EraseMemory";
		if (routine_id == 0xFF01)
			return "CheckProgrammingDependencies";
		if (routine_id == 0xFF02)
			return "EraseMirrorMemoryDTCs";
		if (routine_id >= 0xFF03 && routine_id <= 0xFFFF)
			return "ISOSAEReserved";
		
		return "";
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Routine [name=");
		builder.append(name);
		builder.append("]");
		return builder.toString();
	}
	
	
}
