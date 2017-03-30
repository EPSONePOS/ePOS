package com.example.eposeasyselectsample.quickpairing;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import android.graphics.Point;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

// ------------------------------------------------------------------------------------------------
public class BarcodeManager
{
	// --------------------------------------------------------------------------------------------
	public enum BARCODE_TYPE {
		AZTEC,
		CODABAR,
		CODE_128,
		CODE_39,
		CODE_93,
		DATA_MATRIX,
		EAN_13,
		EAN_8,
		ITF,
		MAXICODE,
		PDF_417,
		QR_CODE,
		RSS_14,
		RSS_EXPANDED,
		UPC_A,
		UPC_E,
		UPC_EAN_EXTENSION
	};

	// --------------------------------------------------------------------------------------------
	private Map<DecodeHintType, Object>	mDecodeHints	= null;	// decod type

	private Result						mDecodedResult	= null;

	// --------------------------------------------------------------------------------------------
	/**
	 *	set decod type
	 *
	 *	@param hints decord barcode type
	 *
	 */
	public void setDecodHints(
			BARCODE_TYPE[]	hints )
	{
		Collection<BarcodeFormat> formats = EnumSet.noneOf( BarcodeFormat.class );

		if (null == hints) {
			return ;
		}

		for (BARCODE_TYPE type : hints) {
			BarcodeFormat format = getBarcodeFormat( type );

			if (null != format) {
				formats.add( format );
			}
		}

		if (null == mDecodeHints) {
			mDecodeHints = new EnumMap<DecodeHintType, Object>( DecodeHintType.class );
		}

		mDecodeHints.put( DecodeHintType.POSSIBLE_FORMATS, formats );
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	clear decode type
	 *
	 */
	public void resetDecodeHints()
	{
		mDecodeHints.clear();
		mDecodeHints = null;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	decode
	 *
	 *	@param data
	 *	@param previewSize
	 *	@param frameWidth
	 *	@param frameHeigth
	 *	@param offsetX
	 *	@param offsetY
	 *
	 *	@return boolean
	 *
	 */
	public boolean decode(
			byte[]	data,
			Point	previewSize,
			int		frameWidth,
			int		frameHeigth,
			int		offsetX,
			int		offsetY )
	{
		PlanarYUVLuminanceSource source = null;

		mDecodedResult = null;

		try {
			source = new PlanarYUVLuminanceSource(	data,
													previewSize.y,
													previewSize.x,
													offsetX,
													offsetY,
													frameWidth,
													frameHeigth,
													false );

			BinaryBitmap		binaryBitmap		= new BinaryBitmap( new HybridBinarizer( source ) );
			MultiFormatReader	multiFormatReader	= new MultiFormatReader();
			Result				result				= null;

			// mDecodeHints = null -> support is all barcode type
			result = multiFormatReader.decode( binaryBitmap, mDecodeHints );

			mDecodedResult = result;

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	get decode data (data)
	 *
	 *	@return Data[]
	 *
	 */
	public byte[] getDataResult()
	{
		if (null == mDecodedResult) {
			return null;
		}

		return mDecodedResult.getRawBytes();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	get decode data (String）
	 *
	 *	@return String
	 *
	 */
	public String getStringResult()
	{
		if (null == mDecodedResult) {
			return null;
		}

		return mDecodedResult.getText();
	}

	// --------------------------------------------------------------------------------------------
	/**
	 *	get BarcodeFormat
	 *
	 *	@param type
	 *
	 *	@return BarcodeFormat
	 *
	 */
	private BarcodeFormat getBarcodeFormat(
		BARCODE_TYPE	type )
	{
		BarcodeFormat	format	= null;

		switch (type) {
		case AZTEC:				format = BarcodeFormat.AZTEC;				break;
		case CODABAR:			format = BarcodeFormat.CODABAR;				break;
		case CODE_128:			format = BarcodeFormat.CODE_128;			break;
		case CODE_39:			format = BarcodeFormat.CODE_39;				break;
		case CODE_93:			format = BarcodeFormat.CODE_93;				break;
		case DATA_MATRIX:		format = BarcodeFormat.DATA_MATRIX;			break;
		case EAN_13:			format = BarcodeFormat.EAN_13;				break;
		case EAN_8:				format = BarcodeFormat.EAN_8;				break;
		case ITF:				format = BarcodeFormat.ITF;					break;
		case MAXICODE:			format = BarcodeFormat.MAXICODE;			break;
		case PDF_417:			format = BarcodeFormat.PDF_417;				break;
		case QR_CODE:			format = BarcodeFormat.QR_CODE;				break;
		case RSS_14:			format = BarcodeFormat.RSS_14;				break;
		case RSS_EXPANDED:		format = BarcodeFormat.RSS_EXPANDED;		break;
		case UPC_A:				format = BarcodeFormat.UPC_A;				break;
		case UPC_E:				format = BarcodeFormat.UPC_E;				break;
		case UPC_EAN_EXTENSION:	format = BarcodeFormat.UPC_EAN_EXTENSION;	break;
		default:	break; }

		return format;
	}
}
