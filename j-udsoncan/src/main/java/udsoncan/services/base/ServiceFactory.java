package udsoncan.services.base;

import udsoncan.services.DiagnosticSessionControl;
import udsoncan.services.ECUReset;
import udsoncan.services.ReadDataByIdentifier;
import udsoncan.services.ReadMemoryByAddress;
import udsoncan.services.RequestDownload;
import udsoncan.services.RequestTransferExit;
import udsoncan.services.RoutineControl;
import udsoncan.services.SecurityAccess;
import udsoncan.services.TesterPresent;
import udsoncan.services.TransferData;
import udsoncan.services.WriteDataByIdentifier;
import udsoncan.services.WriteMemoryByAddress;

public class ServiceFactory {
	public static final int PCode = 0x40;

	public static BaseService getService(int id) {
		BaseService service = null;
		switch (id) {
		case 0x10:
		case 0x10 + PCode:
			service = new DiagnosticSessionControl();
			break;
		case 0x11:
		case 0x11 + PCode:
			service = new ECUReset();
			break;
		case 0x22:
		case 0x22 + PCode:
			service = new ReadDataByIdentifier();
			break;
		case 0x23:
		case 0x23 + PCode:
			service = new ReadMemoryByAddress();
			break;
		case 0x27:
		case 0x27 + PCode:
			service = new SecurityAccess();
			break;
		case 0x2E:
		case 0x2E + PCode:
			service = new WriteDataByIdentifier();
			break;
		case 0x31:
		case 0x31 + PCode:
			service = new RoutineControl();
			break;
		case 0x34:
		case 0x34 + PCode:
			service = new RequestDownload();
			break;
		case 0x36:
		case 0x36 + PCode:
			service = new TransferData();
			break;
		case 0x37:
		case 0x37 + PCode:
			service = new RequestTransferExit();
			break;
		case 0x3D:
		case 0x3D + PCode:
			service = new WriteMemoryByAddress();
			break;
		case 0x3E:
		case 0x3E + PCode:
			service = new TesterPresent();
			break;
		default:
			break;
		}
		return service;
	}

}
