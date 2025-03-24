package com.nexgo.apiv3demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.nexgo.common.ByteUtils;
import com.nexgo.common.LogUtils;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.SdkResult;
import com.nexgo.oaf.apiv3.device.extpinpad.ExtPinPad;
import com.nexgo.oaf.apiv3.device.extpinpad.ExtPinPadCipherModeEnum;
import com.nexgo.oaf.apiv3.device.extpinpad.ExtPinPadInfoEntity;
import com.nexgo.oaf.apiv3.device.extpinpad.ExtPinPadKeyAlgTypeEnum;
import com.nexgo.oaf.apiv3.device.extpinpad.ExtPinPadKeyCode;
import com.nexgo.oaf.apiv3.device.extpinpad.LcdAlignEnum;
import com.nexgo.oaf.apiv3.device.extpinpad.LcdDisplayModeEnum;
import com.nexgo.oaf.apiv3.device.extpinpad.OnExtPinPadInputPinListener;
import com.nexgo.oaf.apiv3.device.pinpad.AlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.CalcModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.CipherModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.DesKeyModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.DukptAESGenerateKeyTypeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.DukptAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.DukptKeyTypeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.MacAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.WorkKeyTypeEnum;

import java.util.ArrayList;
import java.util.List;


/**
 * ExternalPinPad
 */
public class ExtPinPadActivity extends Activity {
    private ExtPinPad extPinPad;
    private final static int KEY_INDEX = 0;
    private TextView tvDesc;
    private static final Handler mHandler = new Handler();
    private DeviceEngine deviceEngine;
    int ret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ext_pinpad);
        tvDesc = (TextView) findViewById(R.id.tv_desc);
        deviceEngine = ((NexgoApplication) getApplication()).deviceEngine;
        extPinPad = deviceEngine.getExtPinPad();

        //N86 with base, port = 101, N5 with base, port = 0
        extPinPad.initExtPinPad(101, null);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mk_inject:
                byte[] TMK = ByteUtils.hexString2ByteArray("31313131313131313131313131313131");
                ret = extPinPad.writeMasterKey(KEY_INDEX, ExtPinPadKeyAlgTypeEnum.DES, TMK);
                LogUtils.debug("writeMasterKey ret:{}", ret);
                showMessage("writeMasterKey " + (ret == SdkResult.Success ? "success" : "ret:" + ret));
                break;

            case R.id.btn_wk_inject:
                byte[] TWK = ByteUtils.hexString2ByteArray("31313131313131313131313131313131");
                ret = extPinPad.writeWorkKey(KEY_INDEX, WorkKeyTypeEnum.TDKEY, ExtPinPadKeyAlgTypeEnum.DES,
                        ExtPinPadCipherModeEnum.ECB ,null, TWK);
                LogUtils.debug("writeWorkKey ret:{}", ret);
                showMessage("writeWorkKey " + (ret == SdkResult.Success ? "success" : "ret:" + ret));
                break;

            case R.id.btn_wk_calculate:
                byte[] data = ByteUtils.hexString2ByteArray("31313131313131313131313131313131");
                byte[] retData = extPinPad.calcByWorkKey(KEY_INDEX, WorkKeyTypeEnum.TDKEY, ExtPinPadKeyAlgTypeEnum.DES,
                        ExtPinPadCipherModeEnum.ECB, null, data);
                LogUtils.debug("calcByWorkKey retData:{}", retData);
                showMessage("calcByWorkKey:" + ByteUtils.byteArray2HexString(retData));
                break;

            case R.id.btn_mac_calculate:
                byte[] mac = extPinPad.calcMac(KEY_INDEX, MacAlgorithmModeEnum.CBC, ExtPinPadCipherModeEnum.ECB, null, ByteUtils.hexString2ByteArray("31313131313131313131313131313131"));
                LogUtils.debug("calcMac mac:{}", mac);
                showMessage("calcMac:" + ByteUtils.byteArray2HexString(mac));
                break;

            case R.id.btn_input_pin:
//                MK/SK online pin
                extPinPad.beep(true);
                extPinPad.inputPin(KEY_INDEX, ExtPinPadKeyAlgTypeEnum.DES, PinAlgorithmModeEnum.ISO9564FMT0, 4, 12, "4234567890123456", 60, new OnExtPinPadInputPinListener() {
                    @Override
                    public void onInputPinResult(int retCode, byte[] data) {
                        LogUtils.debug("dukptInputPin retCode:{} pinBlock:{}", retCode, ByteUtils.byteArray2HexString(data));
                        showMessage("retCode:" + retCode + " pinBlock:" + ByteUtils.byteArray2HexString(data));
                    }
                });
                break;

            case R.id.btn_KCV:
                byte[] buffer = ByteUtils.hexString2ByteArray("0000000000000000");
                byte[] KCV = new byte[3];
                byte[] temp = extPinPad.calcByWorkKey(KEY_INDEX, WorkKeyTypeEnum.TDKEY, ExtPinPadKeyAlgTypeEnum.DES,
                        ExtPinPadCipherModeEnum.ECB, null, buffer);
                LogUtils.debug("calcByWorkKey temp:{}", temp);
                System.arraycopy(temp, 0, KCV, 0, 3);
                showMessage("calcByWorkKey KCV:" + ByteUtils.byteArray2HexString(KCV));
                break;

            case R.id.btn_Key_Exist:
                boolean existTMK = extPinPad.isKeyExist(KEY_INDEX, null);
                boolean existTDK = extPinPad.isKeyExist(KEY_INDEX, WorkKeyTypeEnum.TDKEY);
                LogUtils.debug("isKeyExist existTMK:{}, existTDK{}", existTMK, existTDK);
                showMessage("isKeyExist:" + "KEY_INDEX[" + KEY_INDEX +"]" + " TMK:" + existTMK + " TDK:" + existTDK);
                break;

            case R.id.btn_DUKPT_Exist:
                byte[] KSN = extPinPad.dukptGetKsn(KEY_INDEX, DukptAlgorithmModeEnum.DES);
                LogUtils.debug("dukptGetKsn: {}", ByteUtils.byteArray2HexString(KSN));
                showMessage("isKeyExist DUKPT:" + "KEY_INDEX[" + KEY_INDEX +"] " + (KSN == null ? "false" : "true"));
                break;


            case R.id.btn_dukpt_inject:
                byte[] KSNHex = ByteUtils.hexString2ByteArray("FFFF9876543210E10000");
                byte[] BDKHex = ByteUtils.hexString2ByteArray("C1D0F8FB4958670DBA40AB1F3752EF0D");
                ret = extPinPad.dukptInjectKey(KEY_INDEX, DukptAlgorithmModeEnum.DES, DukptAESGenerateKeyTypeEnum.DUKPT_MODE_KEY_TYPE_2TDEA, DukptKeyTypeEnum.BDK, BDKHex, KSNHex);
                LogUtils.debug("dukptInject ret:{}", ret);
                showMessage("inject key " + (ret == SdkResult.Success ? "success" : "ret:" + ret));
                break;

            case R.id.btn_get_ksn:
                byte[] ksn = extPinPad.dukptGetKsn(KEY_INDEX, DukptAlgorithmModeEnum.DES);
                LogUtils.debug("KSN:{}", ByteUtils.byteArray2HexString(ksn));
                showMessage("ksn:" + ByteUtils.byteArray2HexString(ksn));
                break;

            case R.id.btn_increase_ksn:
                ret = extPinPad.dukptIncreaseKsn(KEY_INDEX, DukptAlgorithmModeEnum.DES);
                LogUtils.debug("increase ksn ret:" + ret);
                showMessage("increase ksn " + (ret == SdkResult.Success ? "success" : "ret:" + ret));
                break;

            case R.id.btn_dukpt_online_pin:
                //DUKPT pin
//                extPinPad.lcdClean();
//                extPinPad.lcdShowText(3, 0, true, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.LEFT, " Amount             17,212 ".getBytes());
//                extPinPad.lcdShowText(4, 0, true, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.LEFT, " PIN  ".getBytes());
                extPinPad.beep(true);
                ret = extPinPad.dukptInputPin(KEY_INDEX, DukptAlgorithmModeEnum.DES, DukptAESGenerateKeyTypeEnum.DUKPT_MODE_KEY_TYPE_2TDEA, PinAlgorithmModeEnum.ISO9564FMT0,
                        "1234567890123456", 0, 8, 60, new OnExtPinPadInputPinListener() {
                            @Override
                            public void onInputPinResult(int retCode, byte[] data) {
                                LogUtils.debug("dukptInputPin retCode:{} pinBlock:{}", retCode, ByteUtils.byteArray2HexString(data));
                                showMessage("retCode:" + retCode + " pinBlock:" + ByteUtils.byteArray2HexString(data));
                            }
                        });
                LogUtils.debug("dukptInputPin ret:{}", ret);
                break;

            case R.id.btn_dukpt_mac:
                byte[] dukptData = ByteUtils.hexString2ByteArray("31313131313131313131313131313131");
                byte[] dukptMac = extPinPad.dukptCalcMac(KEY_INDEX, DukptAlgorithmModeEnum.DES, DukptAESGenerateKeyTypeEnum.DUKPT_MODE_KEY_TYPE_2TDEA, MacAlgorithmModeEnum.CBC, dukptData);
                LogUtils.debug("dukptCalcMac dukptMac:{}", dukptMac);
                showMessage("dukptCalcMac:" + ByteUtils.byteArray2HexString(dukptMac));
                break;

            case R.id.btn_dukpt_encrypt:
                byte[] encryptData = extPinPad.dukptEncrypt(KEY_INDEX, DukptAlgorithmModeEnum.DES, ExtPinPadCipherModeEnum.ECB, null, new byte[8]);
                LogUtils.debug("dukptEncrypt encryptData:{}", encryptData);
                showMessage("dukptEncrypt:" + ByteUtils.byteArray2HexString(encryptData));
                break;

            case R.id.btn_input_amount:
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //clear screen first
                        extPinPad.lcdClean();

                        int ret = extPinPad.lcdShowText(1, 0, true, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.CENTER, "Please Input Amount:".getBytes());
                        LogUtils.debug("showText ret:{}", ret);
                        if (ret != SdkResult.Success) {
                            LogUtils.debug("show text error");
                            showMessage("show text error");
                            return;
                        }
                        LogUtils.debug("show text success");

                        String amount = extPinPad.inputAmount(3, 2, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.RIGHT, 60);
                        LogUtils.debug("input amount: {}", amount);
                        showMessage("amount:" + amount);
                        extPinPad.backToMainScreen();
                    }
                });

                break;

            case R.id.btn_show_text:
                extPinPad.lcdClean();
                ret = extPinPad.lcdShowText(1, 0, true, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.LEFT, "MC Installments".getBytes());
                LogUtils.debug("showText1 ret:{}", ret);

                ret = extPinPad.lcdShowText(3, 0, true, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.CENTER, "RPSN:  20,00 %".getBytes());
                LogUtils.debug("showText2 ret:{}", ret);

                ret = extPinPad.lcdShowText(5, 0, true, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.LEFT, " No (x)           Yes (o) ".getBytes());
                LogUtils.debug("showText3 ret:{}", ret);

                final byte[] keysArray = new byte[]{ExtPinPadKeyCode.KEYCODE_OK, ExtPinPadKeyCode.KEYCODE_CANCEL};
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ret = extPinPad.extPinPadKeyEcho(keysArray, 30);
                        LogUtils.debug("extPinPadKeyEcho ret:{}", ret);
                        if (ret == ExtPinPadKeyCode.KEYCODE_OK) {
                            LogUtils.debug("selected key OK");
                        } else if (ret == ExtPinPadKeyCode.KEYCODE_CANCEL) {
                            LogUtils.debug("selected key Cancel");
                        } else {
                            LogUtils.debug("extPinPadKeyEcho ret:{}", ret);
                            showMessage("keyEcho error ret:" + ret);
                        }
                        extPinPad.backToMainScreen();

                        LogUtils.debug("-----------------------");
                    }
                });
                break;

            case R.id.btn_key_echo:
                keyEcho();
                break;

            case R.id.btn_led:
                ret = extPinPad.setLedLight(true, false, false, true);
                LogUtils.debug("setLedLight ret: {}", ret);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        extPinPad.setLedLight(false, false, false, false);
                    }
                }, 5 * 1000);
                break;

            case R.id.btn_get_pinpad_info:
                ExtPinPadInfoEntity entity = extPinPad.getExtPinPadInfo();
                if (entity != null) {
                    LogUtils.debug("bootVersion:{}", entity.getBootVersion());
                    LogUtils.debug("coreVersion:{}", entity.getCoreVersion());
                    LogUtils.debug("AppVersion:{}", entity.getAppVersion());
                    showMessage("AppVersion:" + entity.getAppVersion());
                }
                break;

            case R.id.btn_get_sn:
                String sn = extPinPad.extPinPadGetSn();
                LogUtils.debug("sn:{}", sn);
                showMessage("sn:" + sn);
                break;

            case R.id.btn_show_image:
                int ret = extPinPad.lcdShowImage(0, 0, 320, 240, "rfui1.bmp");
                LogUtils.debug("showImage ret:{}", ret);
                showMessage("show image " + (ret == SdkResult.Success ? "Success" : " ret:" + ret));
                break;
            case R.id.btn_main_screen:
                extPinPad.backToMainScreen();
                break;


            default:
                break;
        }
    }

    private void keyEcho() {
        final List<String> txtList = new ArrayList<>();
        txtList.add("1. First item");
        txtList.add("2. Second item");
        txtList.add("3. Third item");
        txtList.add("4. Fourth item");
        txtList.add("5. fifth item");
        txtList.add("6. sixth item");
        txtList.add("7. seven item");
        extPinPad.lcdClean();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret;
                    for (int i = 0; i < 4; i++) {
                        ret = extPinPad.lcdShowText(i + 1, 0, true, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.LEFT, txtList.get(i).getBytes());
                        if (ret != SdkResult.Success) {
                            LogUtils.debug("show Text error ret:{}", ret);
                            return;
                        }
                    }
                    ret = extPinPad.lcdShowText(5, 0, true, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.LEFT, "            PageDown(↓) ".getBytes("GB18030"));
                    LogUtils.debug("show Text ret:{}", ret);

                    byte[] keysArray = new byte[]{ExtPinPadKeyCode.KEYCODE_1, ExtPinPadKeyCode.KEYCODE_2, ExtPinPadKeyCode.KEYCODE_3, ExtPinPadKeyCode.KEYCODE_4, ExtPinPadKeyCode.KEYCODE_PAGE_DOWN, ExtPinPadKeyCode.KEYCODE_CANCEL};
                    ret = extPinPad.extPinPadKeyEcho(keysArray, 30);
                    LogUtils.debug("keyEcho ret:{}", ret);
                    switch (ret) {
                        case ExtPinPadKeyCode.KEYCODE_1:
                            LogUtils.debug("current item:" + txtList.get(0));
                            showMessage("current item:" + txtList.get(0));
                            extPinPad.backToMainScreen();
                            return;
                        case ExtPinPadKeyCode.KEYCODE_2:
                            LogUtils.debug("current item:" + txtList.get(1));
                            showMessage("current item:" + txtList.get(1));
                            extPinPad.backToMainScreen();
                            return;
                        case ExtPinPadKeyCode.KEYCODE_3:
                            LogUtils.debug("current item:" + txtList.get(2));
                            showMessage("current item:" + txtList.get(2));
                            extPinPad.backToMainScreen();
                            return;
                        case ExtPinPadKeyCode.KEYCODE_4:
                            LogUtils.debug("current item:" + txtList.get(3));
                            showMessage("current item:" + txtList.get(3));
                            extPinPad.backToMainScreen();
                            return;
                        case ExtPinPadKeyCode.KEYCODE_PAGE_DOWN:
                            LogUtils.debug("next page");
                            break;
                        case ExtPinPadKeyCode.KEYCODE_CANCEL:
                            LogUtils.debug("Cancel");
                            showMessage("Cancel");
                            extPinPad.backToMainScreen();
                            return;
                        default:
                            break;
                    }

                    extPinPad.lcdClean();
                    keysArray = new byte[]{ExtPinPadKeyCode.KEYCODE_5, ExtPinPadKeyCode.KEYCODE_6, ExtPinPadKeyCode.KEYCODE_7, ExtPinPadKeyCode.KEYCODE_PAGE_UP, ExtPinPadKeyCode.KEYCODE_CANCEL};
                    for (int i = 4; i < 7; i++) {
                        ret = extPinPad.lcdShowText(i - 3, 0, true, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.LEFT, txtList.get(i).getBytes());
                        if (ret != SdkResult.Success) {
                            showMessage("showText error ret:" + ret);
                            return;
                        }
                    }

                    ret = extPinPad.lcdShowText(5, 0, true, LcdDisplayModeEnum.POSITIVE, LcdAlignEnum.LEFT, " PageUp(↑)              ".getBytes("GB18030"));
                    LogUtils.debug("showText ret:{}", ret);
                    ret = extPinPad.extPinPadKeyEcho(keysArray, 30);
                    LogUtils.debug("key:" + ByteUtils.byteArray2HexString(new byte[]{(byte) ret}));
                    switch (ret) {
                        case ExtPinPadKeyCode.KEYCODE_5:
                            LogUtils.debug("current item:" + txtList.get(4));
                            showMessage("current item:" + txtList.get(4));
                            break;
                        case ExtPinPadKeyCode.KEYCODE_6:
                            LogUtils.debug("current item:" + txtList.get(5));
                            showMessage("current item:" + txtList.get(5));
                            break;
                        case ExtPinPadKeyCode.KEYCODE_7:
                            LogUtils.debug("current item:" + txtList.get(6));
                            showMessage("current item:" + txtList.get(6));
                            break;
                        case ExtPinPadKeyCode.KEYCODE_CANCEL:
                            LogUtils.debug("Cancel");
                            showMessage("Cancel");
                            break;
                        case ExtPinPadKeyCode.KEYCODE_PAGE_UP:
                            keyEcho();
                            return;
                        default:
                            break;
                    }
                    LogUtils.debug("Finish.......");
                    extPinPad.backToMainScreen();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
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
