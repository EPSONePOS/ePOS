package com.example.eposeasyselectsample.printer;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.epson.eposprint.Builder;
import com.epson.epsonio.DevType;
import com.epson.epsonio.EpsonIo;
import com.epson.epsonio.EpsonIoException;
import com.epson.epsonio.IoStatus;

// ------------------------------------------------------------------------------------------------
public class ComEpsonIo
{
	// --------------------------------------------------------------------------------------------
	public static final int		RESULT_SUCCESS			= 0;
	public static final int		RESULT_ERROR_PARAMETER	= 1;
	public static final int		RESULT_ERROR_OPEN		= 2;
	public static final int		RESULT_ERROR_NOT_OPEN	= 3;
	public static final int		RESULT_ERROR_TIME_OUT	= 4;
	public static final int		RESULT_ERROR_FAILER		= 5;
	public static final int		RESULT_ERROR_NOT_SUPPORT= 5;
	public static final int		RESULT_ERROR_UNKNOWN	= 255;

	// --------------------------------------------------------------------------------------------
	public static final int		GS_I_PRINTER_NAME		= 0x43;
	public static final int		GS_I_PRINTER_LANGUAGE	= 0x45;

	// --------------------------------------------------------------------------------------------
	private final static int	CMD_GS					= 0x1D;
	private final static int	RESPONSE_HEADER_GSI		= 0x5F;

	// --------------------------------------------------------------------------------------------
	private int				mDeviceType		= DevType.NONE;
	private	EpsonIo			mPort			= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	open printer
	 *
	 *	@param	type
	 *	@param	device
	 *
	 *	@return	int
	 *
	 */
	public int open(
			int		type,
			String	device )
	{
		int result	= RESULT_ERROR_UNKNOWN;

		if (null != mPort) {
			close();
		}

		try {
			mPort = new  EpsonIo();

			mDeviceType = type;

			mPort.open( mDeviceType, device, null );

			result = RESULT_SUCCESS;

		} catch (EpsonIoException ie) {
			mPort = null;
			mDeviceType	= DevType.NONE;

			result = RESULT_ERROR_OPEN;
		}

		return result;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	close printer
	 *
	 *	@return	int
	 *
	 */
	public int close()
	{
		int result	= RESULT_ERROR_UNKNOWN;

		if (null == mPort) {
			return RESULT_ERROR_NOT_OPEN;
		}

		try {
			mPort.close();
			mPort = null;

			mDeviceType	= DevType.NONE;

			result = RESULT_SUCCESS;

		} catch (EpsonIoException ie) {
			result = RESULT_ERROR_FAILER;
		}

		return result;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	receive data
	 *
	 *	@param	data
	 *	@param	dataSize
	 *	@param	timeout
	 *	@param	resultSize
	 *
	 *	@return	int
	 *
	 */
	public int receive(
			byte[]	data,
			int		dataSize,
			int		timeout,
			int[]	resultSize )
	{
		int result	= RESULT_ERROR_UNKNOWN;

		resultSize[0] = 0;

		if (null == mPort) {
			return RESULT_ERROR_NOT_OPEN;
		}

		try {
			resultSize[0] = mPort.read( data, 0, dataSize, timeout );

			result = RESULT_SUCCESS;

		} catch (EpsonIoException ie) {
			if (ie.getStatus() == IoStatus.ERR_TIMEOUT) {
				result = RESULT_ERROR_TIME_OUT;
			} else {
				result = RESULT_ERROR_FAILER;
			}
		}

		return result;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	send data
	 *
	 *	@param	data
	 *	@param	dataSize
	 *	@param	timeout
	 *	@param	resultSize
	 *
	 *	@return	int
	 *
	 */
	public int send(
			byte[]	data,
			int		dataSize,
			int		timeout,
			int[]	resultSize )
	{
		int result	= RESULT_ERROR_UNKNOWN;

		if (null == mPort) {
			return RESULT_ERROR_NOT_OPEN;
		}

		try {
			resultSize[0] = mPort.write( data, 0, dataSize, timeout );

			result = RESULT_SUCCESS;

		} catch (EpsonIoException ie) {
			if (ie.getStatus() == IoStatus.ERR_TIMEOUT) {
				result = RESULT_ERROR_TIME_OUT;
			} else {
				result = RESULT_ERROR_FAILER;
			}
		}

		return result;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	get printer information
	 *
	 *	@param	infoId
	 *	@param	value
	 *
	 *	@return	int
	 *
	 */
	public int getPrinterInfo(
			int			infoId,
			String[]	value )
	{
		byte[]			cmd			= { CMD_GS, 'I', 0x00 };
		byte[]			receiveData	= new byte[4096];
		ArrayList<Byte>	responce	= new ArrayList<Byte>();
		int[]			retSize		= { 0 };
		int 			result		= RESULT_ERROR_UNKNOWN;

		if (null == mPort) {
			return RESULT_ERROR_NOT_OPEN;
		}

		clearBuffer();

		// set command
		switch (infoId) {
		case (GS_I_PRINTER_NAME):		cmd[2] = GS_I_PRINTER_NAME;		break;
		case (GS_I_PRINTER_LANGUAGE):	cmd[2] = GS_I_PRINTER_LANGUAGE;	break;
		default:														break; }

		result = send( cmd, cmd.length, 5000, retSize );
		if (RESULT_SUCCESS != result) {
			return result;
		}

		int offset = 0;

		while (true) {
			byte[]	work	= new byte[4096];
			boolean	loopEnd	= false;

			result = receive( work, work.length, 1000, retSize );
			if (RESULT_SUCCESS != result) {
				return result;
			}

			if (0 == retSize[0]) {
				continue;
			}

			for (int i = 0; i < retSize[0]; i++) {
				receiveData[offset] = work[i];

				if (0x00 == work[i]) {
					loopEnd = true;
					break;
				}
				offset++;
			}

			if (false != loopEnd) {
				break;
			}
		}

		if (false != analyzeGSI( receiveData, receiveData.length, responce )) {
			byte[] buff = new byte[responce.size()];

			for (int i= 0; i < responce.size(); i++) {
				buff[i] = responce.get( i );
			}

			try {
				value[0] = new String( buff, 0, buff.length, "UTF-8" );
			} catch (UnsupportedEncodingException e) {
				// nothing
			}

			result = RESULT_SUCCESS;

		} else {
			// not support printer
			result = RESULT_ERROR_NOT_SUPPORT;
		}

		return result;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	get printer languager id
	 *
	 *	@param	printerLanguage
	 *
	 *	@return	int
	 *
	 */
	public int getPrinterLanguageId(
			String	printerLanguage )
	{
		int	language	= Builder.MODEL_ANK;

		if (0 == printerLanguage.compareToIgnoreCase( "" )) {
			language = Builder.MODEL_ANK;

		} else if (0 == printerLanguage.compareToIgnoreCase( "KANJI JAPANESE" )) {
			language = Builder.MODEL_JAPANESE;

		} else if (0 == printerLanguage.compareToIgnoreCase( "CHINA GB18030" )) {
			language = Builder.MODEL_CHINESE;

		} else if (0 == printerLanguage.compareToIgnoreCase( "TAIWAN BIG-5" )) {
			language = Builder.MODEL_TAIWAN;

		} else if (0 == printerLanguage.compareToIgnoreCase( "KOREA C-5601C" )) {
			language = Builder.MODEL_KOREAN;

		} else if (0 == printerLanguage.compareToIgnoreCase( "THAI 3 PASS" )) {
			language = Builder.MODEL_THAI;

		} else if (0 == printerLanguage.compareToIgnoreCase( "CHINA GB2312" )) {
			language = Builder.MODEL_SOUTHASIA;

		} else {
			language = Builder.MODEL_ANK;
		}

		return language;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	clear buffer
	 *
	 */
	private void clearBuffer()
	{
		byte[]	buffer	= new byte[4096];
		int[]	retSize	= { 0 };

		int 	ret		= 0;

		ret = receive( buffer, buffer.length, 1000, retSize );
		if (0 == retSize[0]) {
			return ;
		}

		for (int i = 0; i < 3; i++) {
			while (RESULT_SUCCESS != ret) {
				try {
					Thread.sleep( 500 );
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ret = receive( buffer, buffer.length, 1000, retSize );
			}

			try {
				Thread.sleep( 500 );
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (RESULT_SUCCESS != receive( buffer, buffer.length, 1000, retSize ) ) {
				break;
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	analyze receive data for GS I
	 *
	 *	@param	buffer
	 *	@param	bufferSize
	 *	@param	responce
	 *
	 *	@return	boolean
	 *
	 */
	private boolean analyzeGSI(
			byte[]			buffer,
			int				bufferSize,
			ArrayList<Byte>	responce )
	{
		Boolean	findHeader		= false;
		Boolean	findTerminater	= false;
		Boolean	find			= false;

		responce.clear();

		for (int i = 0; i < bufferSize; i++) {
			//check 0x5F header
			if (RESPONSE_HEADER_GSI == buffer[i]){
				findHeader = true;
				continue;
			}

			if (0x00 == buffer[i]) {
				findTerminater = true;
				break;
			}

			responce.add( buffer[i] );
		}

		if ((false != findHeader) && (false != findTerminater)) {
			find = true;
		} else {
			responce.clear();
		}

		return find;
	}
}
