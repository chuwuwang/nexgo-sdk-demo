package com.nexgo.apiv3demo;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nexgo.common.ByteUtils;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.device.pinpad.OnPinPadInputListener;
import com.nexgo.oaf.apiv3.device.pinpad.PinAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinKeyboardViewModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinPad;
import com.nexgo.oaf.apiv3.device.pinpad.PinpadLayoutEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinpadCustomLayoutActivity extends AppCompatActivity {
    private View mKeyboard;
    private Button mKeyboard_0;
    private Button mKeyboard_1;
    private Button mKeyboard_2;
    private Button mKeyboard_3;
    private Button mKeyboard_4;
    private Button mKeyboard_5;
    private Button mKeyboard_6;
    private Button mKeyboard_7;
    private Button mKeyboard_8;
    private Button mKeyboard_9;
    private Button mKeyboard_cancel;
    private Button mKeyboard_clear;
    private Button mKeyboard_confirm;
    private DeviceEngine deviceEngine;
    private PinPad pinpad;
    private Logger log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinpad_custom_layout);
        log = LoggerFactory.getLogger("PINPADCUSTOMLAYOUT");
        deviceEngine = ((NexgoApplication) getApplication()).deviceEngine;
        pinpad = deviceEngine.getPinPad();

        mKeyboard = findViewById(R.id.keyboard_view);
        mKeyboard_1 = (Button) findViewById(R.id.keyboard_1);
        mKeyboard_2 = (Button) findViewById(R.id.keyboard_2);
        mKeyboard_3 = (Button) findViewById(R.id.keyboard_3);
        mKeyboard_4 = (Button) findViewById(R.id.keyboard_4);
        mKeyboard_5 = (Button) findViewById(R.id.keyboard_5);
        mKeyboard_6 = (Button) findViewById(R.id.keyboard_6);
        mKeyboard_7 = (Button) findViewById(R.id.keyboard_7);
        mKeyboard_8 = (Button) findViewById(R.id.keyboard_8);
        mKeyboard_9 = (Button) findViewById(R.id.keyboard_9);
        mKeyboard_0 = (Button) findViewById(R.id.keyboard_0);
        mKeyboard_cancel = (Button) findViewById(R.id.keyboard_cancel);
        mKeyboard_clear = (Button) findViewById(R.id.keyboard_clear);
        mKeyboard_confirm = (Button) findViewById(R.id.keyboard_confirm);
        mKeyboard.post(new Runnable() {
            @Override
            public void run() {
                PinpadLayoutEntity pinpadLayout = new PinpadLayoutEntity();
                int[] location = new int[2];
                Rect r;

                mKeyboard_1.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_1.getWidth() + r.left;
                r.bottom = mKeyboard_1.getHeight() + r.top;
                pinpadLayout.setKey1(r);

                mKeyboard_2.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_2.getWidth() + r.left;
                r.bottom = mKeyboard_2.getHeight() + r.top;
                pinpadLayout.setKey2(r);

                mKeyboard_3.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_3.getWidth() + r.left;
                r.bottom = mKeyboard_3.getHeight() + r.top;
                pinpadLayout.setKey3(r);

                mKeyboard_4.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_4.getWidth() + r.left;
                r.bottom = mKeyboard_4.getHeight() + r.top;
                pinpadLayout.setKey4(r);

                mKeyboard_5.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_5.getWidth() + r.left;
                r.bottom = mKeyboard_5.getHeight() + r.top;
                pinpadLayout.setKey5(r);

                mKeyboard_6.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_6.getWidth() + r.left;
                r.bottom = mKeyboard_6.getHeight() + r.top;
                pinpadLayout.setKey6(r);

                mKeyboard_7.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_7.getWidth() + r.left;
                r.bottom = mKeyboard_7.getHeight() + r.top;
                pinpadLayout.setKey7(r);

                mKeyboard_8.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_8.getWidth() + r.left;
                r.bottom = mKeyboard_8.getHeight() + r.top;
                pinpadLayout.setKey8(r);

                mKeyboard_9.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_9.getWidth() + r.left;
                r.bottom = mKeyboard_9.getHeight() + r.top;
                pinpadLayout.setKey9(r);

                mKeyboard_0.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_0.getWidth() + r.left;
                r.bottom = mKeyboard_0.getHeight() + r.top;
                pinpadLayout.setKey10(r);

                mKeyboard_cancel.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_cancel.getWidth() + r.left;
                r.bottom = mKeyboard_cancel.getHeight() + r.top;
                pinpadLayout.setKeyCancel(r);

                mKeyboard_clear.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_clear.getWidth() + r.left;
                r.bottom = mKeyboard_clear.getHeight() + r.top;
                pinpadLayout.setKeyClear(r);

                mKeyboard_confirm.getLocationOnScreen(location);
                r = new Rect();
                r.left = location[0];
                r.top = location[1];
                r.right = mKeyboard_confirm.getWidth() + r.left;
                r.bottom = mKeyboard_confirm.getHeight() + r.top;
                pinpadLayout.setKeyConfirm(r);

                byte[] number = pinpad.setPinpadLayout(pinpadLayout);
                if (number != null) {
                    mKeyboard_1.setText(String.valueOf(number[1]));
                    mKeyboard_2.setText(String.valueOf(number[2]));
                    mKeyboard_3.setText(String.valueOf(number[3]));
                    mKeyboard_4.setText(String.valueOf(number[4]));
                    mKeyboard_5.setText(String.valueOf(number[5]));
                    mKeyboard_6.setText(String.valueOf(number[6]));
                    mKeyboard_7.setText(String.valueOf(number[7]));
                    mKeyboard_8.setText(String.valueOf(number[8]));
                    mKeyboard_9.setText(String.valueOf(number[9]));
                    mKeyboard_0.setText(String.valueOf(number[0]));

                    int[] supperLen = new int[]{0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b};
                    byte[] pan = ByteUtils.string2ASCIIByteArray("1234567890123");
                    pinpad.setPinKeyboardViewMode(PinKeyboardViewModeEnum.DEFAULT);
                    pinpad.inputOnlinePin(supperLen, 30, pan, 0, PinAlgorithmModeEnum.ISO9564FMT0, new OnPinPadInputListener() {
                        @Override
                        public void onInputResult(final int retCode, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PinpadCustomLayoutActivity.this, retCode + "", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onSendKey(byte keyCode) {
                        }
                    });
                }
            }
        });
    }
}
