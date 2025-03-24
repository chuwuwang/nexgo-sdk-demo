package com.nexgo.apiv3demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.nexgo.common.ByteUtils;
import com.nexgo.common.LogUtils;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.DeviceInfo;
import com.nexgo.oaf.apiv3.SdkResult;
import com.nexgo.oaf.apiv3.card.cpu.APDUEntity;
import com.nexgo.oaf.apiv3.card.cpu.CPUCardHandler;
import com.nexgo.oaf.apiv3.card.memory.MemoryCardHandler;
import com.nexgo.oaf.apiv3.card.memory.ReadEntity;
import com.nexgo.oaf.apiv3.card.mifare.AuthEntity;
import com.nexgo.oaf.apiv3.card.mifare.BlockEntity;
import com.nexgo.oaf.apiv3.card.mifare.M1CardHandler;
import com.nexgo.oaf.apiv3.card.mifare.M1CardOperTypeEnum;
import com.nexgo.oaf.apiv3.card.mifare.M1KeyTypeEnum;
import com.nexgo.oaf.apiv3.card.ultralight.UltralightCCardHandler;
import com.nexgo.oaf.apiv3.device.beeper.Beeper;
import com.nexgo.oaf.apiv3.device.led.LEDDriver;
import com.nexgo.oaf.apiv3.device.led.LightModeEnum;
import com.nexgo.oaf.apiv3.device.mdb.led.MdbLEDDriver;
import com.nexgo.oaf.apiv3.device.mdb.led.MdbLightModeEnum;
import com.nexgo.oaf.apiv3.device.mdb.serialport.MdbModeEnum;
import com.nexgo.oaf.apiv3.device.mdb.serialport.MdbModeEnum;
import com.nexgo.oaf.apiv3.device.mdb.serialport.MdbSerialPortDriver;
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity;
import com.nexgo.oaf.apiv3.device.reader.CardReader;
import com.nexgo.oaf.apiv3.device.reader.CardSlotTypeEnum;
import com.nexgo.oaf.apiv3.device.reader.CardTypeEnum;
import com.nexgo.oaf.apiv3.device.reader.OnCardInfoListener;
import com.nexgo.oaf.apiv3.device.reader.RfCardTypeEnum;
import com.nexgo.oaf.apiv3.device.scanner.OnScannerListener;
import com.nexgo.oaf.apiv3.device.scanner.Scanner;
import com.nexgo.oaf.apiv3.device.scanner.ScannerCfgEntity;
import com.nexgo.oaf.apiv3.device.serialport.SerialCfgEntity;
import com.nexgo.oaf.apiv3.device.serialport.SerialPortDriver;
import com.nexgo.oaf.apiv3.platform.BeepVolumeModeEnum;
import com.nexgo.oaf.apiv3.platform.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import cn.nexgo.protocol.iso8583.ConfigParser;
import cn.nexgo.protocol.iso8583.LenType;
import cn.nexgo.protocol.iso8583.Message;
import cn.nexgo.protocol.iso8583.MessageFactory;
import cn.nexgo.protocol.spdh.parse.IHead;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Logger log;
    private DeviceEngine deviceEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        log = LoggerFactory.getLogger(this.getClass().getName());
        deviceEngine = ((NexgoApplication) getApplication()).deviceEngine;
        LogUtils.setDebugEnable(true);

        if(!deviceEngine.getDeviceInfo().getModel().equalsIgnoreCase("UN20")){
            Button mdbLed = (Button) findViewById(R.id.MDBLed);
            Button mdbSeiral = (Button) findViewById(R.id.MDBSerial);
            mdbLed.setVisibility(View.GONE);
            mdbSeiral.setVisibility(View.GONE);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.beeper:
                beeperTest();
                break;
            case R.id.led:
                ledTest();
                break;
            case R.id.printer:
                startActivity(new Intent(this, DUKPTActivity.class));
                break;
            case R.id.Serialport:
                serialportTest();
                break;
            case R.id.Cpucard:
                cpucardTest();
                break;
            case R.id.cardReader:
                cardReaderTest();
                break;
            case R.id.M1Card:
                MifareCardTest();
                break;
            case R.id.pinpad:
                startActivity(new Intent(this, PinpadActivity.class));
                break;
            case R.id.emv:
                startActivity(new Intent(this, EmvActivity2.class));
                break;
            case R.id.scanner:
                scannerTest();
                break;
            case R.id.dev_info:
                devInfoTest();
                break;

            case R.id.memorycard:
                MemoryCardTest();
                break;

            case R.id.desfire:
                startActivity(new Intent(this, DesfireActivity.class));
                break;

            case R.id.Ultralight:
                UltralightCardTest();
                break;


            case R.id.PSAM:
                testPsamCard();
                break;

            case R.id.Platform:
                startActivity(new Intent(this, PlatformActivity.class));
                break;

            case R.id.MDBLed:
                mdbLedTest();
                break;

            case R.id.MDBSerial:
                testMdbSerial();
                break;

//            case R.id.Iso8583:
//                testIso8583();
//                break;
//
//            case R.id.SPDH:
//                testSpdh();
//                break;

            default:
                break;
        }
    }


    private void cardReaderTest() {
        final CardReader cardReader = deviceEngine.getCardReader();
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.SWIPE);
        slotTypes.add(CardSlotTypeEnum.ICC1);
        slotTypes.add(CardSlotTypeEnum.RF);
        cardReader.searchCard(slotTypes, 60, new OnCardInfoListener() {
            @Override
            public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
                final StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.prompt_return) + retCode + "\n");
                if (cardInfo != null) {
                    sb.append(getString(R.string.prompt_cardslot) + cardInfo.getCardExistslot() + "\n");
                    sb.append(getString(R.string.prompt_track1) + cardInfo.getTk1() + "\n");
                    sb.append(getString(R.string.prompt_track2) + cardInfo.getTk2() + "\n");
                    sb.append(getString(R.string.prompt_track3) + cardInfo.getTk3() + "\n");
                    sb.append(getString(R.string.prompt_cardno) + cardInfo.getCardNo() + "\n");
                    sb.append(getString(R.string.is_iccard) + cardInfo.isICC() + "\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSwipeIncorrect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getString(R.string.swip_again), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onMultipleCards() {
                cardReader.stopSearch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getString(R.string.search_toomany_cards), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        Toast.makeText(MainActivity.this, getString(R.string.prompt_card), Toast.LENGTH_SHORT).show();
    }

    private void beeperTest() {
        //app can control beep volume with below configuration
//        Platform platform = deviceEngine.getPlatform();
//        platform.setBeepMode(BeepVolumeModeEnum.BEEP_MODE_SYSTEM_DEFAULT, 0);//fix beep volume
//        platform.setBeepMode(BeepVolumeModeEnum.BEEP_MODE_CUSTOM, 50);//app can specify the beep volume,here is 50
//        platform.setBeepMode(BeepVolumeModeEnum.BEEP_MODE_SYSTEM_VOLUME, 0);//beep volume will follow system volume with volume + and -


        final Beeper beeper = deviceEngine.getBeeper();
        View dv = getLayoutInflater().inflate(R.layout.dialog_items_layout, null);
        ListView lv = (ListView) dv.findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Observable.just(null)
                                .observeOn(Schedulers.io())
                                .subscribe(new Action1<Object>() {
                                    @Override
                                    public void call(Object o) {
                                        beeper.beep(100);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {

                                    }
                                });
                        break;
                    case 1:
                        Observable.just(null)
                                .observeOn(Schedulers.io())
                                .subscribe(new Action1<Object>() {
                                    @Override
                                    public void call(Object o) {
                                        beeper.beep(500);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {

                                    }
                                });
                        break;
                    case 2:
                        Observable.interval(70, TimeUnit.MILLISECONDS)
                                .observeOn(Schedulers.io())
                                .takeUntil(new Func1<Long, Boolean>() {
                                    @Override
                                    public Boolean call(Long aLong) {
                                        return aLong == 1;
                                    }
                                })
                                .subscribe(new Action1<Object>() {
                                    @Override
                                    public void call(Object o) {
                                        beeper.beep(50);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {

                                    }
                                });
                        break;
                    case 3:
                        Observable.interval(600, TimeUnit.MILLISECONDS)
                                .observeOn(Schedulers.io())
                                .takeUntil(new Func1<Long, Boolean>() {
                                    @Override
                                    public Boolean call(Long aLong) {
                                        return aLong == 3;
                                    }
                                })
                                .subscribe(new Action1<Object>() {
                                    @Override
                                    public void call(Object o) {
                                        beeper.beep(100);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {

                                    }
                                });
                        break;
                    case 4:
                        Observable.interval(400, TimeUnit.MILLISECONDS)
                                .observeOn(Schedulers.io())
                                .takeUntil(new Func1<Long, Boolean>() {
                                    @Override
                                    public Boolean call(Long aLong) {
                                        return aLong == 2;
                                    }
                                })
                                .subscribe(new Action1<Object>() {
                                    @Override
                                    public void call(Object o) {
                                        beeper.beep(200);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {

                                    }
                                });
                        break;
                }
            }
        });
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.beeper_items));
        lv.setAdapter(stringArrayAdapter);
        new AlertDialog.Builder(this).setView(dv).create().show();
    }

    private void ledTest() {
        final LEDDriver ledDriver = deviceEngine.getLEDDriver();
        View dv = getLayoutInflater().inflate(R.layout.dialog_items_layout, null);
        ListView lv = (ListView) dv.findViewById(R.id.listView);
        final HashMap<Integer, LightModeEnum> hashMap = new HashMap<>();
        hashMap.put(0, LightModeEnum.BLUE);
        hashMap.put(1, LightModeEnum.YELLOW);
        hashMap.put(2, LightModeEnum.RED);
        hashMap.put(3, LightModeEnum.GREEN);

        hashMap.put(4, LightModeEnum.DECORATIVE_LIGHT);

        final boolean[] state = new boolean[hashMap.size()];
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                state[position] = !state[position];
                ledDriver.setLed(hashMap.get(position), state[position]);
            }
        });
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.led_items));
        lv.setAdapter(stringArrayAdapter);
        new AlertDialog.Builder(this).setView(dv).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                for (int i = 0; i < hashMap.size(); i++) {
                    ledDriver.setLed(hashMap.get(i), false);
                }
            }
        }).create().show();
    }

    private void serialportTest() {
        int portNum = 0;
        if(deviceEngine.getDeviceInfo().getModel().equalsIgnoreCase("UN20")){
            portNum = 1; //UN20 can use port 1 or 2, depends on which Physical serial port insert
        }else if(deviceEngine.getDeviceInfo().getModel().equalsIgnoreCase("N86")){
            //if N86 with base, the port is 101; if N86 alone, it is still 0.
            portNum = 101;
        }

        final SerialPortDriver port = deviceEngine.getSerialPortDriver(portNum);
        SerialCfgEntity serialCfgEntity = new SerialCfgEntity();
        serialCfgEntity.setBaudRate(115200);
        serialCfgEntity.setDataBits(8);
        serialCfgEntity.setParity('n');
        serialCfgEntity.setStopBits(1);
        int ret = port.connect(serialCfgEntity);
        if (ret == SdkResult.Success) {
            Toast.makeText(MainActivity.this, getString(R.string.serialport_connect_success), Toast.LENGTH_SHORT).show();
            ret = port.send(ByteUtils.hexString2ByteArray("01010101"), 4);
            if(ret != SdkResult.Success){
                return ;
            }

            byte[] rev = new byte[100];
            Arrays.fill(rev, (byte) 0);
            ret = port.recv(rev, 100, 2000);
            if(ret > 0){
                Log.d("nexgo", "receive data, len = " + ret);
            }else if(ret == 0){
                Log.d("nexgo", "serial port disconnect");
            }else{
                Log.d("nexgo", "receive error = " + ret);
            }

        } else {
            Toast.makeText(MainActivity.this, getString(R.string.serialport_connect_fail), Toast.LENGTH_SHORT).show();
        }


        port.disconnect();
    }

    private void cpucardTest() {
        final CardReader cardReader = deviceEngine.getCardReader();
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.ICC1);
        slotTypes.add(CardSlotTypeEnum.RF);
        cardReader.searchCard(slotTypes, 60, new OnCardInfoListener() {
            @Override
            public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
                CPUCardHandler cpuCardHandler;
                byte[] atr = new byte[128];
                int ret = -1;
                boolean powerOn = false;

                Log.d("nexgo", "retCode" + retCode);
                if(cardInfo.getCardExistslot() == CardSlotTypeEnum.RF)
                {
                    //active
                    cpuCardHandler = deviceEngine.getCPUCardHandler(CardSlotTypeEnum.RF);
                    powerOn = cpuCardHandler.active();
                }else {
                    //power on
                    cpuCardHandler = deviceEngine.getCPUCardHandler(CardSlotTypeEnum.ICC1);
                    powerOn = cpuCardHandler.powerOn(atr);
                }

                if(!powerOn){
                    Log.d("nexgo", "powerOn failed");
                    return ;
                }
                APDUEntity apduEntity = new APDUEntity();
                apduEntity.setP1((byte)0x04);
                apduEntity.setP2((byte)0x00);
                apduEntity.setCla((byte)0x00);
                apduEntity.setIns((byte)0xA4);
                apduEntity.setLc(8);
                apduEntity.setDataIn(ByteUtils.hexString2ByteArray("A000000172950001"));
                ret = cpuCardHandler.exchangeAPDUCmd(apduEntity);

                cardReader.close(cardInfo.getCardExistslot());
            }

            @Override
            public void onSwipeIncorrect() {

            }

            @Override
            public void onMultipleCards() {
                cardReader.stopSearch();
            }
        });
        Toast.makeText(MainActivity.this, getString(R.string.prompt_cardiccrf), Toast.LENGTH_SHORT).show();

    }

    public void MifareCardTest(){
        //step 1 search card
        final CardReader cardReader = deviceEngine.getCardReader();
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.RF);
        cardReader.searchCard(slotTypes, 60, new OnCardInfoListener() {
            @Override
            public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
                if (cardInfo != null) {
                    //step 2 ,judge if rf card type is Mifare card
                    if(cardInfo.getCardExistslot() == CardSlotTypeEnum.RF) {
                        int ret = -1;
                        final M1CardHandler m1CardHandler = deviceEngine.getM1CardHandler();

                        //step 3 , read UID
                        String uid = m1CardHandler.readUid();

                        //step 4, block authority
                        AuthEntity authEntity = new AuthEntity();
                        authEntity.setUid(uid);
                        authEntity.setKeyType(M1KeyTypeEnum.KEYTYPE_A); //keytype A or B
                        authEntity.setBlkNo(2);
                        authEntity.setPwd(ByteUtils.hexString2ByteArray("FFFFFFFFFFFF"));
                        ret = m1CardHandler.authority(authEntity);
                        if(ret != SdkResult.Success){
                            return ;
                        }


                        BlockEntity blockEntity5 = new BlockEntity();
                        blockEntity5.setOperType(M1CardOperTypeEnum.INCREMENT);
                        blockEntity5.setBlkNo(2);
                        ret = m1CardHandler.readBlock(blockEntity5);
                        if (ret != SdkResult.Success) {
                            return ;
                        }

                        //step 5 read block
                        BlockEntity blockEntity = new BlockEntity();
                        blockEntity.setOperType(M1CardOperTypeEnum.INCREMENT);
                        blockEntity.setBlkNo(4);
                        ret = m1CardHandler.readBlock(blockEntity);
                        if (ret != SdkResult.Success) {
                            return ;
                        }

                        //step 6 write block
                        BlockEntity blockEntity2 = new BlockEntity();
                        blockEntity2.setOperType(M1CardOperTypeEnum.INCREMENT);
                        blockEntity2.setBlkNo(2);
                        blockEntity2.setBlkData(ByteUtils.hexString2ByteArray("0e283de4ff980200e327002000000016"));
                        ret = m1CardHandler.writeBlock(blockEntity2);
                        if (ret != SdkResult.Success) {
                            return ;
                        }

                        //step 7 read block again
                        BlockEntity blockEntity3 = new BlockEntity();
                        blockEntity3.setOperType(M1CardOperTypeEnum.INCREMENT);
                        blockEntity3.setBlkNo(2);
                        ret = m1CardHandler.readBlock(blockEntity3);
                        if (ret != SdkResult.Success) {
                            return ;
                        }

                        cardReader.close(CardSlotTypeEnum.RF);
                    }
                }
            }

            @Override
            public void onSwipeIncorrect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getString(R.string.swip_again), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onMultipleCards() {
                cardReader.stopSearch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getString(R.string.search_toomany_cards), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        Toast.makeText(MainActivity.this, getString(R.string.prompt_cardiccrf), Toast.LENGTH_SHORT).show();
    }


    private void scannerTest() {
        final Scanner scanner = deviceEngine.getScanner();
        ScannerCfgEntity cfgEntity = new ScannerCfgEntity();
        cfgEntity.setAutoFocus(true);
        cfgEntity.setUsedFrontCcd(false);//true: front camera; false: back camera

        //if you want to customized , please enable below lines
//        Bundle bundle = new Bundle();
//        bundle.putString("Title", "xxx scan...");
//        bundle.putString("ScanTip", "please scan the Bar or QR code");
//        cfgEntity.setCustomBundle(bundle);

        scanner.initScanner(cfgEntity, new OnScannerListener() {
            @Override
            public void onInitResult(int retCode) {
                if (retCode == SdkResult.Success) {
                    int result = scanner.startScan(60, new OnScannerListener() {
                        @Override
                        public void onInitResult(int retCode) {

                        }

                        @Override
                        public void onScannerResult(int retCode, final String data) {
                            switch (retCode) {
                                case SdkResult.Success:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, getString(R.string.scanner_content) + " " + data, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    break;
                                case SdkResult.TimeOut:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, getString(R.string.scanner_timeout), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                                case SdkResult.Scanner_Customer_Exit:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, getString(R.string.scanner_customer_exit), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                                default:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, getString(R.string.scanner_fail), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                            }
                        }
                    });
                    if (result != SdkResult.Success) {
                        Toast.makeText(MainActivity.this, getString(R.string.scanner_start_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.scanner_init_error), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScannerResult(int retCode, String data) {

            }
        });
    }

    private void devInfoTest() {
        DeviceInfo deviceInfo = deviceEngine.getDeviceInfo();
        StringBuilder sb = new StringBuilder();
        sb.append("SN:" + deviceInfo.getSn() + "\n");
        sb.append("KSN:" + deviceInfo.getKsn() + "\n");
        sb.append("OSVer:" + deviceInfo.getOsVer() + "\n");
        sb.append("FirmWareVer:" + deviceInfo.getFirmWareVer() + "\n");
        sb.append("FirmWareFullVersion:" + deviceInfo.getFirmWareFullVersion() + "\n");
        sb.append("KernelVer:" + deviceInfo.getKernelVer() + "\n");
        sb.append("SDKVer:" + deviceInfo.getSdkVer() + "\n");
        sb.append("CoreVer:" + deviceInfo.getSpCoreVersion() + "\n");
        sb.append("BootVer:" + deviceInfo.getSpBootVersion() + "\n");
        sb.append("MODEL:" + deviceInfo.getModel());
        Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
    }
    private void MemoryCardTest() {
        final MemoryCardHandler memoryCardHandler = deviceEngine.getMemoryCardHandler(CardSlotTypeEnum.ICC1);
        int ret = memoryCardHandler.reset(CardTypeEnum.AT88SC153);
        if (ret == SdkResult.Success) {
            ReadEntity readEntity = new ReadEntity();
            readEntity.setCardType(CardTypeEnum.AT88SC153);
            readEntity.setZone(1);
            readEntity.setAddress(0);
            readEntity.setReadLen(16);
            byte[] data = memoryCardHandler.read(readEntity);
            if(data == null){
                Toast.makeText(MainActivity.this, "MemoryCard read fail", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(MainActivity.this, "M1Card readBlock SUCCESS", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(MainActivity.this, "MemoryCard test", Toast.LENGTH_SHORT).show();
    }

    /*test PSAM card, send Apdu command*/
    public void testPsamCard(){
        //open cardReader
        CardReader cardReader = deviceEngine.getCardReader();
        cardReader.open(CardSlotTypeEnum.PSAM1);

        //power on
        byte[] atr = new byte[128];
        CPUCardHandler cpuCardHandler = deviceEngine.getCPUCardHandler(CardSlotTypeEnum.PSAM1);

        boolean bpoweon = cpuCardHandler.powerOn(atr);
        Log.d("nexgo", "power on " + bpoweon);
        if(!bpoweon){
            return ;
        }

        //select MF
        APDUEntity apduEntity = new APDUEntity();
        int ret = -1;
        apduEntity.setP1((byte)0x00);
        apduEntity.setP2((byte)0x00);
        apduEntity.setCla((byte)0x00);
        apduEntity.setIns((byte)0xA4);
        apduEntity.setLc(2);
        apduEntity.setDataIn(ByteUtils.hexString2ByteArray("3F00"));

        ret = cpuCardHandler.exchangeAPDUCmd(apduEntity);
        if(ret != SdkResult.Success){
            return ;
        }
        if(apduEntity.getDataOutLen() > 0){
            Log.d("nexgo", "data" + ByteUtils.byteArray2HexString(apduEntity.getDataOut()));
        }
        Log.d("nexgo", "SWA" + ByteUtils.byte2BinaryString(apduEntity.getSwa()));
        Log.d("nexgo", "SWB" + ByteUtils.byte2BinaryString(apduEntity.getSwb()));

    }


    public void UltralightCardTest(){
        //step 1 search card
        final CardReader cardReader = deviceEngine.getCardReader();
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.RF);
        cardReader.searchCard(slotTypes, 60, new OnCardInfoListener() {
            @Override
            public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
                if(retCode == SdkResult.Success){
                    if (cardInfo != null) {
                        //step 2 ,judge if rf card type is ULTRALIGHT card
                        if(cardInfo.getCardExistslot() == CardSlotTypeEnum.RF) {
                            if(cardInfo.getRfCardType() == RfCardTypeEnum.ULTRALIGHT){
                                UltralightCCardHandler ultralightCCardHandler = deviceEngine.getUltralightCCardHandler();
                                byte[] readData = ultralightCCardHandler.readBlock((byte) 15);
                                LogUtils.debug("read data:{}", ByteUtils.byteArray2HexString(readData));
                                if (readData == null) {
                                    return ;
                                }

                                byte[] wirteData = {0x11, 0x34, (byte) 0xb5, 0x14};
                                int ret = ultralightCCardHandler.writeBlock((byte) 15, wirteData);
                                LogUtils.debug("write data ret:{}", ret);
                                if (ret != SdkResult.Success) {
                                    return ;
                                }

                                readData = ultralightCCardHandler.readBlock((byte) 15);
                                LogUtils.debug("read data:{}", ByteUtils.byteArray2HexString(readData));
                                if (readData == null) {
                                    return ;
                                }

                                byte[] key = {0x49, 0x45, 0x4d, 0x4b, 0x41, 0x45, 0x52, 0x42, 0x21, 0x4e, 0x41, 0x43, 0x55, 0x4f, 0x59, 0x46};
                                int result = ultralightCCardHandler.authority(key);
                                LogUtils.debug("authority ret:{}", result);
                            }

                            cardReader.close(cardInfo.getCardExistslot());
                        }
                    }
                }

            }

            @Override
            public void onSwipeIncorrect() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getString(R.string.swip_again), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onMultipleCards() {
                cardReader.stopSearch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getString(R.string.search_toomany_cards), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        Toast.makeText(MainActivity.this, getString(R.string.prompt_cardiccrf), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    private void mdbLedTest() {
        final MdbLEDDriver ledDriver = deviceEngine.getMdbLEDDriver();
        View dv = getLayoutInflater().inflate(R.layout.dialog_items_layout, null);
        ListView lv = (ListView) dv.findViewById(R.id.listView);
        final HashMap<Integer, MdbLightModeEnum> hashMap = new HashMap<>();
        hashMap.put(0, MdbLightModeEnum.RF_BLUE);
        hashMap.put(1, MdbLightModeEnum.RF_YELLOW);
        hashMap.put(2, MdbLightModeEnum.RF_GREEN);
        hashMap.put(3, MdbLightModeEnum.RF_RED);

        hashMap.put(4, MdbLightModeEnum.IC_BLUE);
        hashMap.put(5, MdbLightModeEnum.IC_GREEN);
        hashMap.put(6, MdbLightModeEnum.IC_RED);


        hashMap.put(7, MdbLightModeEnum.MAG_BLUE);
        hashMap.put(8, MdbLightModeEnum.MAG_GREEN);
        hashMap.put(9, MdbLightModeEnum.MAG_RED);

        hashMap.put(10, MdbLightModeEnum.SYS_GREEN);
        hashMap.put(11, MdbLightModeEnum.SYS_RED);


        final boolean[] state = new boolean[hashMap.size()];
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                state[position] = !state[position];
                ledDriver.setLed(hashMap.get(position), state[position]);
            }
        });
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.mdbled_items));
        lv.setAdapter(stringArrayAdapter);
        new AlertDialog.Builder(this).setView(dv).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                for (int i = 0; i < hashMap.size(); i++) {
                    ledDriver.setLed(hashMap.get(i), false);
                }
            }
        }).create().show();
    }

    private void testMdbSerial(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                MdbSerialPortDriver mdbSerialPortDriver = deviceEngine.getMdbSerialPortDriver();
                //Default Slave Mode
                int ret = mdbSerialPortDriver.mdbOpen(MdbModeEnum.MDB_MODE_SLAVE);
                LogUtils.debug("mdbOpen ret: {}", ret);
                if(ret != SdkResult.Success){
                    LogUtils.debug("mdbOpen failed,  ret:{}", ret);
                    return ;
                }

                long timer = System.currentTimeMillis();
                byte[] buffer = new byte[256];
                byte[] data = new byte[256];
                while (true){
                    if (System.currentTimeMillis() > 30*1000 + timer) {
                        LogUtils.debug("mdbOpen read timeout");
                        mdbSerialPortDriver.mdbClose();
                        return ;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ret = mdbSerialPortDriver.mdbRead(buffer);
                    LogUtils.debug("mdbRead read ret = {}", ret);
                    if(ret == 0){
                        continue;
                    }
                    if(ret > 0){
                        System.arraycopy(buffer, 0, data, 0, ret);
                        LogUtils.debug("mdbRead read data = {}", ByteUtils.byteArray2HexString(data));
                    }
                    mdbSerialPortDriver.mdbClose();
                    return ;
                }
            }
        }).start();


    }

    private void testIso8583(){
        MessageFactory iso8583MsgFactory = null;

        // init 8583 config file
        try {
            InputStream is = getAssets().open("iso8583.xml");
            iso8583MsgFactory = ConfigParser.creatFromStream(is);
            if(iso8583MsgFactory == null){
                is.close();
                return ;
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Message msg = iso8583MsgFactory.newMessage();

        //message heaher
        msg.setHeader("6000000070");

        //set message type
        if(msg.setFieldValue(0, "0800") == null){
            return ;
        }

        //set bitmap
        if(msg.setFieldValue(1, "0800001803000001") == null){
            return ;
        }

        //set field 2
        if(msg.setFieldValue(2, "622888888881212712") == null){
            return ;
        }

        //set field 3
        if(msg.setFieldValue(3, "000000") == null){
            return ;
        }

        //set field 64
        if(msg.setFieldValue(64, "8888888888888888") == null){
            return ;
        }

        //package iso8583 message
        byte[] msg8583 = msg.formMsg(2, LenType.HEX);
        if(msg8583 == null){
            return ;
        }

        Log.d("nexgo", "msg8583" + ByteUtils.byteArray2HexString(msg8583));

        //parse 8583 message
        Message parseMessage = iso8583MsgFactory.parseMsg(msg8583, 5 + 2);
        if(parseMessage == null){
            return;
        }

        Log.d("nexgo", "getFieldValue(2)-->" +  parseMessage.getFieldValue(2));
        Log.d("nexgo", "getFieldValue(3)-->" +  parseMessage.getFieldValue(3));
        Log.d("nexgo", "getFieldValue(64)-->" +  parseMessage.getFieldValue(64));

    }


    private void testSpdh(){
        cn.nexgo.protocol.spdh.MessageFactory spdhMsgFactor = null;
        // init spdh config file
        try {
            InputStream is = getAssets().open("spdh.xml");
            spdhMsgFactor = cn.nexgo.protocol.spdh.ConfigParser.creatFromStream(is);
            if(spdhMsgFactor == null){
                return ;
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        cn.nexgo.protocol.spdh.Message msg = spdhMsgFactor.getMessage();
        if(msg == null){
            return ;
        }
        //set header
        msg.setHead(new HeadRequest());

        //set FID A
        msg.getFID('A').setValue("00000012");

        //set FID B
        msg.getFID('B').setValue("11111");

        //set FID e
        msg.getFID('e').setValue("222222");

        //set FID 6/SFID q
        msg.getFID('6').getSFID('q').setValue("6623865");

        //set FID 6/SFID E
        msg.getFID('6').getSFID('E').setValue("12");

        byte[] spdhMessage = msg.formMsg();
        Log.d("nexgo", "spdhMessage" + ByteUtils.byteArray2HexString(spdhMessage));

        cn.nexgo.protocol.spdh.Message parseSpdhMessage = spdhMsgFactor.getMessage();
        if(parseSpdhMessage == null){
            return;
        }
        parseSpdhMessage.setHead(new HeadResponse());
        if(!parseSpdhMessage.parse(spdhMessage, 0)){
            return;
        }

        Log.d("nexgo", "getFID('A')-->" +  parseSpdhMessage.getFID('A').getValue());
        Log.d("nexgo", "getFID('B')-->" +  parseSpdhMessage.getFID('B').getValue());
        Log.d("nexgo", "getFID('e')-->" +  parseSpdhMessage.getFID('e').getValue());

        Log.d("nexgo", "getFID('6').getSFID('q')-->" +  parseSpdhMessage.getFID('6').getSFID('q').getValue());
        Log.d("nexgo", "getFID('6').getSFID('E')-->" +  parseSpdhMessage.getFID('6').getSFID('E').getValue());
    }


    private class HeadRequest implements IHead {
        @Override
        public boolean parse(byte[] head) {

            return true;
        }

        @Override
        public byte[] formMsg() {

            return "123456789".getBytes();
        }
    }

    private class HeadResponse implements IHead {
        @Override
        public boolean parse(byte[] head) {

            return true;
        }

        @Override
        public byte[] formMsg() {
            return null;
        }
    }


}
