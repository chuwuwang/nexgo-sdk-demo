package com.nexgo.apiv3demo;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nexgo.common.ByteUtils;
import com.nexgo.common.LogUtils;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.SdkResult;
import com.nexgo.oaf.apiv3.device.pinpad.AlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.DesAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.DukptKeyModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.DukptKeyTypeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.MacAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.OnPinPadInputListener;
import com.nexgo.oaf.apiv3.device.pinpad.PinAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinPad;
import com.nexgo.oaf.apiv3.device.pinpad.PinPadKeyCode;

public class DUKPTActivity extends AppCompatActivity {

    private DeviceEngine deviceEngine;
    private PinPad pinpad;
    private final int KEYINDEX = 1;
    private final byte[] BDK = new byte[]{1,2,3,4,5,6,7,8,1,2,3,4,5,6,7,8};
    private final byte[] KSN = new byte[]{1,2,3,4,5,6,7,0,0,0};
    private final byte[] needCryDatas = new byte[]{1,2,3,4,5,6,7,8,1,2,3,4,5,6,7,8,1,2,3,4,5,6,7,8};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dukpt);

        initView();

        initDUPKT();
    }

    // initView
    private TextView log_txt;
    private void initView(){
        log_txt = (TextView) findViewById(R.id.log_txt);
    }

    // init DUKPT
    private void initDUPKT(){
        deviceEngine = ((NexgoApplication) getApplication()).deviceEngine;
        pinpad = deviceEngine.getPinPad();

        pinpad.setAlgorithmMode(AlgorithmModeEnum.DUKPT);
    }

    // inject BDK and KSN
    private void injectBDKAndKSN(){
        int result = pinpad.dukptKeyInject(KEYINDEX, DukptKeyTypeEnum.BDK, BDK, BDK.length, KSN);
        Toast.makeText(this, getString(R.string.result)+result, Toast.LENGTH_SHORT).show();
    }

    // encry datas
    private void encryDatas(){
        byte[] cryDatas = pinpad.dukptEncrypt(KEYINDEX, DukptKeyModeEnum.REQUEST, needCryDatas, needCryDatas.length, DesAlgorithmModeEnum.CBC, new byte[]{0,0,0,0,0,0,0,0});
        if(cryDatas == null){
            Toast.makeText(this, getString(R.string.dukptencryptfail), Toast.LENGTH_SHORT).show();
            return;
        }
        log_txt.setText("encryDatas:"+ByteUtils.byteArray2HexStringWithSpace(cryDatas));

    }
    // EC increase
    private void ECIncrease(){
        System.out.println("ECIncrease");
        pinpad.dukptKsnIncrease(KEYINDEX);
        Toast.makeText(this, "EC Increased", Toast.LENGTH_SHORT).show();
    }

    // calcMAC
    private void calcDUKPTMAC(){
        byte[] data = ByteUtils.hexString2ByteArray("3131313131313131313131313131313131313131313131313131313131313131");

        byte[] macData = pinpad.calcMac(KEYINDEX, MacAlgorithmModeEnum.CBC, data);
        LogUtils.debug("CBC macData:{}", ByteUtils.byteArray2HexString(macData));
        Toast.makeText(this, "mac:" + ByteUtils.byteArray2HexString(macData) + "", Toast.LENGTH_SHORT).show();
        log_txt.setText("encryDatas:"+ByteUtils.byteArray2HexStringWithSpace(macData));

//        macData = pinpad.calcMac(KEYINDEX, MacAlgorithmModeEnum.ECB, data)
//        LogUtils.debug("ECB macData:{}", ByteUtils.byteArray2HexString(macData));
//        Toast.makeText(this, "mac:" + ByteUtils.byteArray2HexString(macData) + "", Toast.LENGTH_SHORT).show();
//
//
//        macData = pinpad.calcMac(KEYINDEX, MacAlgorithmModeEnum.X919, data);
//        LogUtils.debug("X919 macData:{}", ByteUtils.byteArray2HexString(macData));
//        Toast.makeText(this, "mac:" + ByteUtils.byteArray2HexString(macData) + "", Toast.LENGTH_SHORT).show();
//
    }

    // current KSN
    private void currentKSN(){
        byte[] nowKsn = pinpad.dukptCurrentKsn(KEYINDEX);
        if(nowKsn == null){
            Toast.makeText(this, getString(R.string.dukptencryptfail), Toast.LENGTH_SHORT).show();
            return;
        }
        log_txt.setText("KSN:"+ByteUtils.byteArray2HexStringWithSpace(nowKsn));
    }

    String text = "";

    private void inputPinTest() {
        text = "";

        View dv = getLayoutInflater().inflate(R.layout.dialog_inputpin_layout, null);
        final TextView tv = (TextView) dv.findViewById(R.id.input_pin);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).setView(dv).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        int[] supperLen = new int[]{0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c};
        byte[] pan = ByteUtils.string2ASCIIByteArray("1234567891234567890");
        pinpad.inputOnlinePin(supperLen, 60, pan, KEYINDEX, PinAlgorithmModeEnum.ISO9564FMT0, new OnPinPadInputListener() {
            @Override
            public void onInputResult(final int retCode, final byte[] data) {
                alertDialog.dismiss();
                LogUtils.debug("PinBlock:{}", ByteUtils.byteArray2HexString(data));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        log_txt.setText("PinBlock:"+ByteUtils.byteArray2HexStringWithSpace(data));
                        Toast.makeText(DUKPTActivity.this, retCode + "", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onSendKey(byte keyCode) {
                if (keyCode == PinPadKeyCode.KEYCODE_CLEAR) {
                    text = "";
                } else if (keyCode == PinPadKeyCode.KEYCODE_BACKSPACE) {
                    if (!TextUtils.isEmpty(text)) {
                        text = text.substring(0, text.length() - 2);
                    }
                } else {
                    text += "* ";
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(text);
                    }
                });
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.injectkey_btn:
                injectBDKAndKSN();
                break;

            case R.id.encrypdatas_btn:
                encryDatas();
                break;

            case R.id.dukptpin_btn:
                inputPinTest();
                break;

            case R.id.dukptmac_btn:
                calcDUKPTMAC();
                break;

            case R.id.ecincrease_btn:
                ECIncrease();
                break;

            case R.id.currentksn_btn:
                currentKSN();
                break;
        }
    }
}
