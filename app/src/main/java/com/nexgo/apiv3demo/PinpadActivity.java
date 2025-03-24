package com.nexgo.apiv3demo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.nexgo.common.ByteUtils;
import com.nexgo.common.LogUtils;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.SdkResult;
import com.nexgo.oaf.apiv3.device.pinpad.AlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.CalcModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.CipherModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.MacAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.OnPinPadInputListener;
import com.nexgo.oaf.apiv3.device.pinpad.PinAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinKeyboardModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinPad;
import com.nexgo.oaf.apiv3.device.pinpad.PinPadKeyCode;
import com.nexgo.oaf.apiv3.device.pinpad.WorkKeyTypeEnum;

import java.util.Arrays;

public class PinpadActivity extends AppCompatActivity {

    private DeviceEngine deviceEngine;
    private static final byte[] main_key_data = new byte[16];
    private static final byte[] work_key_data = new byte[16];
    private PinPad pinpad;
    private Context mContext;
    private TextView tvDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinpad);
        mContext = this;
        deviceEngine = ((NexgoApplication) getApplication()).deviceEngine;
        pinpad = deviceEngine.getPinPad();
        pinpad.setAlgorithmMode(AlgorithmModeEnum.DES);
        pinpad.setPinKeyboardMode(PinKeyboardModeEnum.FIXED);
        Arrays.fill(main_key_data, (byte) 0x31);
        Arrays.fill(work_key_data, (byte) 0x31);

        tvDesc = (TextView) findViewById(R.id.tv_desc);

        LogUtils.setDebugEnable(true);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_key_inject:
                mainKeyInjectTest();
                break;
            case R.id.work_key_inject:
                workKeyInjectTest();
                break;
            case R.id.work_key_enc:
                workKeyEncTest();
                break;
            case R.id.input_pin:
                inputPinTest();
                break;
            case R.id.pinpad_calMac:
                calcMacValue();
                break;
            case R.id.pinpad_tken:
                tkEnTest();
                break;
            case R.id.pinpad_custom_layout:
                startActivity(new Intent(this, PinpadCustomLayoutActivity.class));
                break;

            case R.id.pinpad_dukpt_layout:
                Intent intent = new Intent(this, DUKPTActivity.class);
                startActivity(intent);
                break;

            case R.id.ext_pinpad:
                startActivity(new Intent(this, ExtPinPadActivity.class));
                break;

            default:
                break;
        }
    }

    private void tkEnTest() {
        String tk = "6212260200103692998D26032209259991693";
        byte[] bTk = ByteUtils.hexString2ByteArray(tk);
        byte[] enData = pinpad.encryptTrackData(0, bTk, bTk.length);
        Toast.makeText(this, enData == null ? getString(R.string.encryption_fail): ByteUtils.byteArray2HexString(enData), Toast.LENGTH_SHORT).show();

        showMessage("encryptTrackData TDKEY: " + ByteUtils.byteArray2HexString(enData));
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
        byte[] pan = ByteUtils.string2ASCIIByteArray("1234567890123");
        pinpad.inputOnlinePin(supperLen, 60, pan, 0, PinAlgorithmModeEnum.ISO9564FMT0, new OnPinPadInputListener() {
            @Override
            public void onInputResult(final int retCode, byte[] data) {
                alertDialog.dismiss();
                System.out.println("onInputResult->"+retCode+","+ByteUtils.byteArray2HexStringWithSpace(data));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PinpadActivity.this, retCode + "", Toast.LENGTH_SHORT).show();
                    }
                });
                showMessage("inputOnlinePin PinBlock: " + ByteUtils.byteArray2HexString(data));
            }

            @Override
            public void onSendKey(byte keyCode) {
                if (keyCode == PinPadKeyCode.KEYCODE_CLEAR) {
                    text = "";
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

    private void workKeyEncTest() {
        byte[] result = pinpad.calcByWKey(0, WorkKeyTypeEnum.TDKEY, new byte[16], 16, CalcModeEnum.ENCRYPT);
        Toast.makeText(PinpadActivity.this, ByteUtils.byteArray2HexString(result), Toast.LENGTH_SHORT).show();

        showMessage("calcByWKey TDKEY: " + ByteUtils.byteArray2HexString(result));
    }

    private void workKeyInjectTest() {
        int resultMACKEY = pinpad.writeWKey(0, WorkKeyTypeEnum.MACKEY, work_key_data, work_key_data.length);
        int resultPINKEY = pinpad.writeWKey(0, WorkKeyTypeEnum.PINKEY, work_key_data, work_key_data.length);
        int resultTDKEY = pinpad.writeWKey(0, WorkKeyTypeEnum.TDKEY, work_key_data, work_key_data.length);
        int resultTEK = pinpad.writeWKey(0, WorkKeyTypeEnum.ENCRYPTIONKEY, work_key_data, work_key_data.length);

        showMessage("write MACKEY: " + (resultMACKEY == SdkResult.Success ? "success" : "ret:" + resultMACKEY) + "\n"
        + "write PINKEY: " + (resultPINKEY == SdkResult.Success ? "success" : "ret:" + resultPINKEY) + "\n"
        + "write TDKEY: " + (resultTDKEY == SdkResult.Success ? "success" : "ret:" + resultTDKEY) + "\n"
        + "write ENCRYPTIONKEY: " + (resultTEK == SdkResult.Success ? "success" : "ret:" + resultTEK) );
    }

    private void mainKeyInjectTest() {
        int result = pinpad.writeMKey(0, main_key_data, main_key_data.length);
        Toast.makeText(PinpadActivity.this, getString(R.string.result) + result + "", Toast.LENGTH_SHORT).show();

        showMessage("writeMKey: " + (result == SdkResult.Success ? "success" : "ret:" + result));

    }

    public void calcMacValue() {
        String mac = "4984511233489929|000000001000|4984511233489929=21051010000021800001||PTbEBBZDDCivdD9v|";
        byte[] macData = pinpad.calcMac(0, MacAlgorithmModeEnum.X919, mac.getBytes());
        LogUtils.debug("macData:{}", ByteUtils.byteArray2HexString(macData));
        Toast.makeText(PinpadActivity.this, "mac:" + ByteUtils.byteArray2HexString(macData) + "", Toast.LENGTH_SHORT).show();

        showMessage("calcMac X919: " + ByteUtils.byteArray2HexString(macData));
    }

    private void showMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvDesc.setText(msg);
            }
        });
    }
}
