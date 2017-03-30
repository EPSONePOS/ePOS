package com.example.eposeasyselectsample.printer;

import com.epson.eposprint.Print;
import com.epson.epsonio.DevType;

// ------------------------------------------------------------------------------------------------
public class InterfaceType
{
	// --------------------------------------------------------------------------------------------
	/**
	 *	Print -> DevType
	 *
	 *	@param	interfaceType	Print
	 *
	 *	@return	int type of Print class
	 *
	 */
	public static int convPtoD(
			int interfaceType )
	{
		int type = -1;

		switch (interfaceType) {
		case Print.DEVTYPE_TCP:			type = DevType.TCP;			break;
		case Print.DEVTYPE_BLUETOOTH:	type = DevType.BLUETOOTH;	break;
		case Print.DEVTYPE_USB:			type = DevType.USB;			break;
		default:													break; }

		return type;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	DevType -> Print
	 *
	 *	@param	interfaceType	DevType
	 *
	 *	@return	int	DevType
	 *
	 */
	public static int convDtoP(
			int interfaceType )
	{
		int type = DevType.ANY;

		switch (interfaceType) {
		case DevType.TCP:		type = Print.DEVTYPE_TCP;		break;
		case DevType.BLUETOOTH:	type = Print.DEVTYPE_BLUETOOTH;	break;
		case DevType.USB:		type = Print.DEVTYPE_USB;		break;
		default:												break; }

		return type;
	}
}
