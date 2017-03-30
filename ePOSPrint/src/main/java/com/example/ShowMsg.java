package com.example;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.epson.eposprint.*;
import com.epson.epsonio.*;

public class ShowMsg {
    
    static final int BITCNT_INT = 32;
    
    static void showException(Exception e, String method, Context context){
        String msg;
        if(e instanceof EpsonIoException) {
            msg = String.format(
                    "%s\n\t%s\n%s\n\t%s",
                    context.getString(R.string.methoderr_errcode),
                    getEpsonIoExceptionText(((EpsonIoException)e).getStatus()),
                    context.getString(R.string.methoderr_method),
                    method);
        }else if(e instanceof EposException) {
            msg = String.format(
                    "%s\n\t%s\n%s\n\t%s",
                    context.getString(R.string.methoderr_errcode),
                    getEposExceptionText(((EposException)e).getErrorStatus()),
                    context.getString(R.string.methoderr_method),
                    method);
        }else{
            msg = e.toString();
        }
        show(msg, context);
    }
    
    static void showError(int errMsg, Context context){
        String msg = context.getString(errMsg);
        show(msg, context);
    }
    
    static void showStatus(int result, int status, int battery, Context context){
        String msg;
        msg = String.format(
                "%s\n\t%s\n%s\n%s\n%s\n\t0x%04X",
                context.getString(R.string.statusmsg_result),
                getEposExceptionText(result),
                context.getString(R.string.statusmsg_status),
                getEposStatusText(status), 
                context.getString(R.string.statusmsg_batterystatus),
                battery);
        show(msg, context);
    }
    
    static void showPrinterName(String printerName, String languageName, Context context){
        String msg;
        msg = String.format(
                "%s\n\t%s\n%s\n\t%s",
                context.getString(R.string.namemsg_name),
                printerName,
                context.getString(R.string.namemsg_language),
                languageName);
        show(msg, context);
    }
    
    static void showStatusChangeEvent(String deviceName, int status, Context context){
        String msg;
        msg = String.format(
                "%s\n\t%s\n%s\n%s",
                context.getString(R.string.statusmsg_ipaddress),
                deviceName,
                context.getString(R.string.statusmsg_status),
                getEposStatusText(status));
        show(msg, context);
    }
    
    static void showBatteryStatusChangeEvent(String deviceName, int battery, Context context){
        String msg;
        msg = String.format(
                "%s\n\t%s\n%s\n\t0x%04X",
                context.getString(R.string.statusmsg_ipaddress),
                deviceName,
                context.getString(R.string.statusmsg_batterystatus),
                battery);
        show(msg, context);
    }
    
    private static void show(String msg, Context context){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int whichButton) {
                return ;
            }
        });
        alertDialog.create();
        alertDialog.show();
    }
    
    private static String getEposExceptionText(int state){
        switch(state){
        case EposException.SUCCESS:
            return "SUCCESS";
        case    EposException.ERR_PARAM:
            return "ERR_PARAM";
        case    EposException.ERR_OPEN:
            return "ERR_OPEN";
        case    EposException.ERR_CONNECT:
            return "ERR_CONNECT";
        case    EposException.ERR_TIMEOUT:
            return "ERR_TIMEOUT";
        case    EposException.ERR_MEMORY:
            return "ERR_MEMORY";
        case    EposException.ERR_ILLEGAL:
            return "ERR_ILLEGAL";
        case    EposException.ERR_PROCESSING:
            return "ERR_PROCESSING";
        case    EposException.ERR_UNSUPPORTED:
            return "ERR_UNSUPPORTED";
        case    EposException.ERR_OFF_LINE:
            return "ERR_OFF_LINE";
        case    EposException.ERR_FAILURE:
            return "ERR_FAILURE";
        default:
            return String.format("%d", state);
        }
    }

    private static String getEpsonIoExceptionText(int state){
        switch(state){
        case    IoStatus.SUCCESS:
            return "SUCCESS";
        case    IoStatus.ERR_PARAM:
            return "ERR_PARAM";
        case    IoStatus.ERR_OPEN:
            return "ERR_OPEN";
        case    IoStatus.ERR_CONNECT:
            return "ERR_CONNECT";
        case    IoStatus.ERR_TIMEOUT:
            return "ERR_TIMEOUT";
        case    IoStatus.ERR_MEMORY:
            return "ERR_MEMORY";
        case    IoStatus.ERR_ILLEGAL:
            return "ERR_ILLEGAL";
        case    IoStatus.ERR_PROCESSING:
            return "ERR_PROCESSING";
        case    IoStatus.ERR_FAILURE:
            return "ERR_FAILURE";
        default:
            return String.format("%d", state);
        }
    }
    
    private static String getEposStatusText(int status){
        String result = "";
        
        for(int bit = 0; bit <BITCNT_INT; bit++){
            int value = 1 << bit;
            if((value & status) != 0){
                String msg = "";
                switch(value){
                case    Print.ST_NO_RESPONSE:
                    msg = "NO_RESPONSE";
                    break;
                case    Print.ST_PRINT_SUCCESS:
                    msg = "PRINT_SUCCESS";
                    break;
                case    Print.ST_DRAWER_KICK:
                    msg = "DRAWER_KICK";
                    break;
                case    Print.ST_OFF_LINE:
                    msg = "OFF_LINE";
                    break;
                case    Print.ST_COVER_OPEN:
                    msg = "COVER_OPEN";
                    break;
                case    Print.ST_PAPER_FEED:
                    msg = "PAPER_FEED";
                    break;
                case    Print.ST_WAIT_ON_LINE:
                    msg = "WAIT_ON_LINE";
                    break;
                case    Print.ST_PANEL_SWITCH:
                    msg = "PANEL_SWITCH";
                    break;
                case    Print.ST_MECHANICAL_ERR:
                    msg = "MECHANICAL_ERR";
                    break;
                case    Print.ST_AUTOCUTTER_ERR:
                    msg = "AUTOCUTTER_ERR";
                    break;
                case    Print.ST_UNRECOVER_ERR:
                    msg = "UNRECOVER_ERR";
                    break;
                case    Print.ST_AUTORECOVER_ERR:
                    msg = "AUTORECOVER_ERR";
                    break;
                case    Print.ST_RECEIPT_NEAR_END:
                    msg = "RECEIPT_NEAR_END";
                    break;
                case    Print.ST_RECEIPT_END:
                    msg = "RECEIPT_END";
                    break;
                case    Print.ST_BUZZER:
                    break;
                default:
                    msg = String.format("%d", value);
                    break;
                }
                if(!msg.isEmpty()){
                    if(!result.isEmpty()){
                        result += "\n";
                    }
                    result += "\t" + msg;
                }
            }
        }
        
        return result;
    }
}
