package com.example.eposreceiptprintsample;

import android.content.Context;

import com.epson.eposprint.Print;
import com.epson.eposprint.EposException;
import com.epson.epsonio.IoStatus;

public class MsgMaker {
    private static final int BATTERY_NEAR_END = 0x3131;
    private static final int BATTERY_REAL_END = 0x3130;

    public static String makeErrorMessage(Context context, Result result) {
        String msg = "";

        if (result.getEposException() != null) {
            msg = makeEposExceptionHandlingText(result, context);
        }
        else if (result.getEpsonIoException() != null) {
            msg = makeEpsonIoExceptionHandlingText(result, context);
        }
        else {
            msg = makePrinterStatusErrorHandlingText(result, context);
        }

        return msg;
    }

    public static String makeWarningMessage(Context context, Result result) {
        String msg = "";
        int status = result.getPrinterStatus();
        int battery = result.getBatteryStatus();

        if ((status & Print.ST_RECEIPT_NEAR_END) == Print.ST_RECEIPT_NEAR_END) {
            msg += context.getString(R.string.handlingmsg_warn_receipt_near_end);
        }

        if (battery == BATTERY_NEAR_END) {
            msg += context.getString(R.string.handlingmsg_warn_battery_near_end);
        }

        return msg;
    }

    private static String makePrinterStatusErrorHandlingText(Result result, Context context) {
        String msg = "";
        int status = result.getPrinterStatus();
        int battery = result.getBatteryStatus();

        if ((status & Print.ST_NO_RESPONSE) == Print.ST_NO_RESPONSE) {
            msg += context.getString(R.string.handlingmsg_err_no_response);
        }

        if ((status & Print.ST_COVER_OPEN) == Print.ST_COVER_OPEN) {
            msg += context.getString(R.string.handlingmsg_err_cover_open);
        }

        if (((status & Print.ST_PAPER_FEED) == Print.ST_PAPER_FEED) || ((status & Print.ST_PANEL_SWITCH) == Print.ST_PANEL_SWITCH)) {
            msg += context.getString(R.string.handlingmsg_err_paper_feed);
        }

        if (((status & Print.ST_AUTOCUTTER_ERR) == Print.ST_AUTOCUTTER_ERR) || ((status & Print.ST_MECHANICAL_ERR) == Print.ST_MECHANICAL_ERR)) {
            msg += context.getString(R.string.handlingmsg_err_autocutter);
            msg += context.getString(R.string.handlingmsg_err_need_recover);
        }

        if ((status & Print.ST_UNRECOVER_ERR) == Print.ST_UNRECOVER_ERR) {
            msg += context.getString(R.string.handlingmsg_err_unrecover);
        }

        if ((status & Print.ST_RECEIPT_END) == Print.ST_RECEIPT_END) {
            msg += context.getString(R.string.handlingmsg_err_receipt_end);
        }

        if ((status & Print.ST_HEAD_OVERHEAT) == Print.ST_HEAD_OVERHEAT) {
            msg += context.getString(R.string.handlingmsg_err_overheat) + context.getString(R.string.handlingmsg_err_head);
        }

        if ((status & Print.ST_MOTOR_OVERHEAT) == Print.ST_MOTOR_OVERHEAT) {
            msg += context.getString(R.string.handlingmsg_err_overheat) + context.getString(R.string.handlingmsg_err_motor);
        }

        if ((status & Print.ST_BATTERY_OVERHEAT) == Print.ST_BATTERY_OVERHEAT) {
            msg += context.getString(R.string.handlingmsg_err_overheat) + context.getString(R.string.handlingmsg_err_battery);
        }

        if ((status & Print.ST_WRONG_PAPER) == Print.ST_WRONG_PAPER) {
            msg += context.getString(R.string.handlingmsg_err_wrong_paper);
        }

        if (battery == BATTERY_REAL_END) {
            msg += context.getString(R.string.handlingmsg_err_battery_real_end);
        }

        return msg;
    }

    private static String makeEposExceptionHandlingText(Result result, Context context) {
        int errorStatus = result.getEposException().getErrorStatus();
        String retMsg = "";
        String printerMsg = "";

        switch (errorStatus) {
        case EposException.ERR_PARAM:
            retMsg = context.getString(R.string.handlingmsg_ex_application_error);
            break;

        case EposException.ERR_OPEN:
            retMsg =  context.getString(R.string.handlingmsg_ex_open);
            break;

        case EposException.ERR_CONNECT:
            retMsg =  context.getString(R.string.handlingmsg_ex_connect);
            break;

        case EposException.ERR_TIMEOUT:
            retMsg =  context.getString(R.string.handlingmsg_ex_timeout);
            break;

        case EposException.ERR_MEMORY:
            retMsg =  context.getString(R.string.handlingmsg_ex_application_error);
            break;

        case EposException.ERR_ILLEGAL:
            retMsg =  context.getString(R.string.handlingmsg_ex_application_error);
            break;

        case EposException.ERR_PROCESSING:
            retMsg =  context.getString(R.string.handlingmsg_ex_application_error);
            break;

        case EposException.ERR_UNSUPPORTED:
            retMsg =  context.getString(R.string.handlingmsg_ex_application_error);
            break;

        case EposException.ERR_OFF_LINE:
            retMsg =  context.getString(R.string.handlingmsg_ex_off_line);
            break;

        case EposException.ERR_FAILURE:
            retMsg =  context.getString(R.string.handlingmsg_ex_application_error);
            break;

        default:
            break;
        }

        retMsg += "\n";
		retMsg += context.getString(R.string.handlingmsg_notice_printer_problems);

        printerMsg = makePrinterStatusErrorHandlingText(result, context);

        if (printerMsg.isEmpty()) {
            printerMsg += context.getString(R.string.handlingmsg_notice_failed);
        }
        retMsg += printerMsg;

        return retMsg;
    }

    private static String makeEpsonIoExceptionHandlingText(Result result, Context context) {

        int errorStatus = result.getEpsonIoException().getStatus();

        switch (errorStatus) {
        case IoStatus.ERR_PARAM:
            return context.getString(R.string.handlingmsg_ex_application_error);

        case IoStatus.ERR_OPEN:
            return context.getString(R.string.handlingmsg_ex_open);

        case IoStatus.ERR_CONNECT:
            return context.getString(R.string.handlingmsg_ex_connect);

        case IoStatus.ERR_TIMEOUT:
            return context.getString(R.string.handlingmsg_ex_timeout);

        case IoStatus.ERR_MEMORY:
            return context.getString(R.string.handlingmsg_ex_application_error);

        case IoStatus.ERR_ILLEGAL:
            return context.getString(R.string.handlingmsg_ex_application_error);

        case IoStatus.ERR_PROCESSING:
            return context.getString(R.string.handlingmsg_ex_application_error);

        case IoStatus.ERR_FAILURE:
            return context.getString(R.string.handlingmsg_ex_failure);

        default:
            return "";
        }
    }

}
