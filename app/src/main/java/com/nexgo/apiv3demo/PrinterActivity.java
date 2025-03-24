package com.nexgo.apiv3demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.device.printer.AlignEnum;
import com.nexgo.oaf.apiv3.device.printer.BarcodeFormatEnum;
import com.nexgo.oaf.apiv3.device.printer.DotMatrixFontEnum;
import com.nexgo.oaf.apiv3.device.printer.FontEntity;
import com.nexgo.oaf.apiv3.device.printer.GrayLevelEnum;
import com.nexgo.oaf.apiv3.device.printer.OnPrintListener;
import com.nexgo.oaf.apiv3.device.printer.Printer;

public class PrinterActivity extends AppCompatActivity {

    private DeviceEngine deviceEngine;
    private Printer printer;
    private final int FONT_SIZE_SMALL = 20;
    private final int FONT_SIZE_NORMAL = 24;
    private final int FONT_SIZE_BIG = 24;
    private FontEntity fontSmall = new FontEntity(DotMatrixFontEnum.CH_SONG_20X20, DotMatrixFontEnum.ASC_SONG_8X16);
    private FontEntity fontNormal = new FontEntity(DotMatrixFontEnum.CH_SONG_24X24, DotMatrixFontEnum.ASC_SONG_12X24);
    private FontEntity fontBold = new FontEntity(DotMatrixFontEnum.CH_SONG_24X24, DotMatrixFontEnum.ASC_SONG_BOLD_16X24);
    private FontEntity fontBig = new FontEntity(DotMatrixFontEnum.CH_SONG_24X24, DotMatrixFontEnum.ASC_SONG_12X24, false, true);

    private EditText etGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);
        deviceEngine = ((NexgoApplication) getApplication()).deviceEngine;
        printer = deviceEngine.getPrinter();
        printer.setTypeface(Typeface.DEFAULT);

        etGray = (EditText) findViewById(R.id.print_gray);
    }

    private GrayLevelEnum getGrayLevel(){
        int gray = 1;
        GrayLevelEnum grayLevelEnum = GrayLevelEnum.LEVEL_1;
        if(etGray.getText().length() > 0){
            gray = Integer.parseInt(etGray.getText().toString());
        }

        switch (gray) {
            case 0:
                grayLevelEnum = GrayLevelEnum.LEVEL_0;
                break;
            case 1:
                grayLevelEnum = GrayLevelEnum.LEVEL_1;
                break;
            case 2:
                grayLevelEnum = GrayLevelEnum.LEVEL_2;
                break;
            case 3:
                grayLevelEnum = GrayLevelEnum.LEVEL_3;
                break;
            case 4:
                grayLevelEnum = GrayLevelEnum.LEVEL_4;
                break;
            default:
                grayLevelEnum = GrayLevelEnum.LEVEL_1;
                break;
        }

        return grayLevelEnum;
    }

    public void onClick(View view) {
        Bitmap bitmap;
        switch (view.getId()) {
            case R.id.print_sale_order:
                printer.initPrinter();                              //init printer
                printer.setTypeface(Typeface.DEFAULT);            //change print type
                printer.setLetterSpacing(5);                        //change the line space between each line

//                printer.setGray(GrayLevelEnum.LEVEL_2);             //change print gray
                printer.setGray(getGrayLevel());

                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logonexgo);
                printer.appendImage(bitmap, AlignEnum.CENTER);      //append image
                printer.appendPrnStr(getString(R.string.print_merchantname), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_merchantno), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_terminalno), getString(R.string.print_operator), FONT_SIZE_NORMAL, false);
                printer.appendPrnStr(getString(R.string.print_issurebank), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_shoudan), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_expiredate), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.cardnum), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_cardinfo), FONT_SIZE_BIG, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_tradetype), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_unsale), FONT_SIZE_BIG, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_batchno), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_voucher), getString(R.string.print_authorcode), FONT_SIZE_NORMAL, false);
                printer.appendPrnStr(getString(R.string.print_refrenceno), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_tradedate), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_amount), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.money), FONT_SIZE_BIG, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_beizhu), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_originalvoucher), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_addinfo), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendBarcode(getString(R.string.print_barcode), 50, 0, 2, BarcodeFormatEnum.CODE_128, AlignEnum.CENTER);
                printer.appendQRcode(getString(R.string.print_qrcode), 200, AlignEnum.CENTER);
                printer.appendPrnStr("---------------------------", FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_cardhold), FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr("\n", FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr("\n", FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr("\n", FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr("---------------------------", FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_cardhold_ensure), FONT_SIZE_SMALL, AlignEnum.LEFT, false);
                printer.appendPrnStr("---------------------------", FONT_SIZE_NORMAL, AlignEnum.LEFT, false);
                printer.appendPrnStr(getString(R.string.print_merchant_dan), FONT_SIZE_NORMAL, AlignEnum.RIGHT, false);
                printer.startPrint(false, new OnPrintListener() {       //roll paper or not
                    @Override
                    public void onPrintResult(final int retCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PrinterActivity.this, retCode + "", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                break;
            case R.id.print_sale_order2:
                printer.initPrinter();
                printer.setLetterSpacing(5);

                printer.setGray(getGrayLevel());

                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logonexgo);
                printer.appendImage(bitmap, AlignEnum.CENTER);
                printer.appendPrnStr(getString(R.string.print_merchantname), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_merchantno), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_terminalno), getString(R.string.print_operator), fontNormal);
                printer.appendPrnStr(getString(R.string.print_issurebank), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_shoudan), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_expiredate), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.cardnum), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_cardinfo), fontBold, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_tradetype), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_unsale), fontBig, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_batchno), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_voucher), getString(R.string.print_authorcode), fontNormal);
                printer.appendPrnStr(getString(R.string.print_refrenceno), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_tradedate), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_amount), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.money), fontBold, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_beizhu), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_originalvoucher), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_addinfo), fontNormal, AlignEnum.LEFT);
                printer.appendBarcode(getString(R.string.print_barcode), 50, 0, 2, BarcodeFormatEnum.CODE_128, AlignEnum.CENTER);
                printer.appendQRcode(getString(R.string.print_qrcode), 200, AlignEnum.CENTER);
                printer.appendPrnStr("---------------------------", fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_cardhold), fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr("\n", fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr("\n", fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr("\n", fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr("---------------------------", fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_cardhold_ensure), fontSmall, AlignEnum.LEFT);
                printer.appendPrnStr("I ACKNOWLEDGE SATISFACTORY RECEIPT OF RELATIVE GOODS/SERVICES", fontSmall, AlignEnum.LEFT);
                printer.appendPrnStr("---------------------------", fontNormal, AlignEnum.LEFT);
                printer.appendPrnStr(getString(R.string.print_merchant_dan), fontNormal, AlignEnum.RIGHT);
                printer.startPrint(true, new OnPrintListener() {
                    @Override
                    public void onPrintResult(final int retCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PrinterActivity.this, retCode + "", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                break;
        }
    }


}
