package udsoncan.base;

/**
 * Defines a list of constants that are data identifiers defined by the UDS
 * standard. This class provides no functionality apart from defining these
 * constants
 */
public class DataIdentifier {
	public static final int BootSoftwareIdentification = 0xF180;
	public static final int ApplicationSoftwareIdentification = 0xF181;
	public static final int ApplicationDataIdentification = 0xF182;
	public static final int BootSoftwareFingerprint = 0xF183;
	public static final int ApplicationSoftwareFingerprint = 0xF184;
	public static final int ApplicationDataFingerprint = 0xF185;
	public static final int ActiveDiagnosticSession = 0xF186;
	public static final int VehicleManufacturerSparePartNumber = 0xF187;
	public static final int VehicleManufacturerECUSoftwareNumber = 0xF188;
	public static final int VehicleManufacturerECUSoftwareVersionNumber = 0xF189;
	public static final int SystemSupplierIdentifier = 0xF18A;
	public static final int ECUManufacturingDate = 0xF18B;
	public static final int ECUSerialNumber = 0xF18C;
	public static final int SupportedFunctionalUnits = 0xF18D;
	public static final int VehicleManufacturerKitAssemblyPartNumber = 0xF18E;
	public static final int ISOSAEReservedStandardized = 0xF18F;
	public static final int VIN = 0xF190;
	public static final int VehicleManufacturerECUHardwareNumber = 0xF191;
	public static final int SystemSupplierECUHardwareNumber = 0xF192;
	public static final int SystemSupplierECUHardwareVersionNumber = 0xF193;
	public static final int SystemSupplierECUSoftwareNumber = 0xF194;
	public static final int SystemSupplierECUSoftwareVersionNumber = 0xF195;
	public static final int ExhaustRegulationOrTypeApprovalNumber = 0xF196;
	public static final int SystemNameOrEngineType = 0xF197;
	public static final int RepairShopCodeOrTesterSerialNumber = 0xF198;
	public static final int ProgrammingDate = 0xF199;
	public static final int CalibrationRepairShopCodeOrCalibrationEquipmentSerialNumber = 0xF19A;
	public static final int CalibrationDate = 0xF19B;
	public static final int CalibrationEquipmentSoftwareNumber = 0xF19C;
	public static final int ECUInstallationDate = 0xF19D;
	public static final int ODXFile = 0xF19E;
	public static final int Entity = 0xF19F;

	public static String name_from_id(int did) throws Exception {
		// As defined by ISO-14229:2006, Annex F
		if (did < 0 || did > 0xFFFF) {
			throw new Exception("Data IDentifier must be a valid integer between 0 and 0xFFFF");
		}

		String rtn = "unknown";
		if (did >= 0x0000 && did <= 0x00FF)
			rtn = "ISOSAEReserved";
		if (did >= 0x0100 && did <= 0xEFFF)
			rtn = "VehicleManufacturerSpecific";
		if (did >= 0xF000 && did <= 0xF00F)
			rtn = "NetworkConfigurationDataForTractorTrailerApplicationDataIdentifier";
		if (did >= 0xF010 && did <= 0xF0FF)
			rtn = "VehicleManufacturerSpecific";
		if (did >= 0xF100 && did <= 0xF17F)
			rtn = "IdentificationOptionVehicleManufacturerSpecificDataIdentifier";

		if (did == 0xF180)
			rtn = "BootSoftwareIdentificationDataIdentifier";
		if (did == 0xF181)
			rtn = "ApplicationSoftwareIdentificationDataIdentifier";
		if (did == 0xF182)
			rtn = "ApplicationDataIdentificationDataIdentifier";
		if (did == 0xF183)
			rtn = "BootSoftwareFingerprintDataIdentifier";
		if (did == 0xF184)
			rtn = "ApplicationSoftwareFingerprintDataIdentifier";
		if (did == 0xF185)
			rtn = "ApplicationDataFingerprintDataIdentifier";
		if (did == 0xF186)
			rtn = "ActiveDiagnosticSessionDataIdentifier";
		if (did == 0xF187)
			rtn = "VehicleManufacturerSparePartNumberDataIdentifier";
		if (did == 0xF188)
			rtn = "VehicleManufacturerECUSoftwareNumberDataIdentifier";
		if (did == 0xF189)
			rtn = "VehicleManufacturerECUSoftwareVersionNumberDataIdentifier";
		if (did == 0xF18A)
			rtn = "SystemSupplierIdentifierDataIdentifier";
		if (did == 0xF18B)
			rtn = "ECUManufacturingDateDataIdentifier";
		if (did == 0xF18C)
			rtn = "ECUSerialNumberDataIdentifier";
		if (did == 0xF18D)
			rtn = "SupportedFunctionalUnitsDataIdentifier";
		if (did == 0xF18E)
			rtn = "VehicleManufacturerKitAssemblyPartNumberDataIdentifier";
		if (did == 0xF18F)
			rtn = "ISOSAEReservedSt&&ardized";
		if (did == 0xF190)
			rtn = "VINDataIdentifier";
		if (did == 0xF191)
			rtn = "VehicleManufacturerECUHardwareNumberDataIdentifier";
		if (did == 0xF192)
			rtn = "SystemSupplierECUHardwareNumberDataIdentifier";
		if (did == 0xF193)
			rtn = "SystemSupplierECUHardwareVersionNumberDataIdentifier";
		if (did == 0xF194)
			rtn = "SystemSupplierECUSoftwareNumberDataIdentifier";
		if (did == 0xF195)
			rtn = "SystemSupplierECUSoftwareVersionNumberDataIdentifier";
		if (did == 0xF196)
			rtn = "ExhaustRegulationOrTypeApprovalNumberDataIdentifier";
		if (did == 0xF197)
			rtn = "SystemNameOrEngineTypeDataIdentifier";
		if (did == 0xF198)
			rtn = "RepairShopCodeOrTesterSerialNumberDataIdentifier";
		if (did == 0xF199)
			rtn = "ProgrammingDateDataIdentifier";
		if (did == 0xF19A)
			rtn = "CalibrationRepairShopCodeOrCalibrationEquipmentSerialNumberDataIdentifier";
		if (did == 0xF19B)
			rtn = "CalibrationDateDataIdentifier";
		if (did == 0xF19C)
			rtn = "CalibrationEquipmentSoftwareNumberDataIdentifier";
		if (did == 0xF19D)
			rtn = "ECUInstallationDateDataIdentifier";
		if (did == 0xF19E)
			rtn = "ODXFileDataIdentifier";
		if (did == 0xF19F)
			rtn = "EntityDataIdentifier";

		if (did >= 0xF1A0 && did <= 0xF1EF)
			rtn = "IdentificationOptionVehicleManufacturerSpecific";
		if (did >= 0xF1F0 && did <= 0xF1FF)
			rtn = "IdentificationOptionSystemSupplierSpecific";
		if (did >= 0xF200 && did <= 0xF2FF)
			rtn = "PeriodicDataIdentifier";
		if (did >= 0xF300 && did <= 0xF3FF)
			rtn = "DynamicallyDefinedDataIdentifier";
		if (did >= 0xF400 && did <= 0xF4FF)
			rtn = "OBDDataIdentifier";
		if (did >= 0xF500 && did <= 0xF5FF)
			rtn = "OBDDataIdentifier";
		if (did >= 0xF600 && did <= 0xF6FF)
			rtn = "OBDMonitorDataIdentifier";
		if (did >= 0xF700 && did <= 0xF7FF)
			rtn = "OBDMonitorDataIdentifier";
		if (did >= 0xF800 && did <= 0xF8FF)
			rtn = "OBDInfoTypeDataIdentifier";
		if (did >= 0xF900 && did <= 0xF9FF)
			rtn = "TachographDataIdentifier";
		if (did >= 0xFA00 && did <= 0xFA0F)
			rtn = "AirbagDeploymentDataIdentifier";
		if (did >= 0xFA10 && did <= 0xFAFF)
			rtn = "SafetySystemDataIdentifier";
		if (did >= 0xFB00 && did <= 0xFCFF)
			rtn = "ReservedForLegislativeUse";
		if (did >= 0xFD00 && did <= 0xFEFF)
			rtn = "SystemSupplierSpecific";
		if (did >= 0xFF00 && did <= 0xFFFF)
			rtn = "ISOSAEReserved";

		return rtn;
	}
}
