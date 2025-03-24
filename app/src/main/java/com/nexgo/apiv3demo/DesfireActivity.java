package com.nexgo.apiv3demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.Toolbar;

import com.nexgo.common.ByteUtils;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.card.mifare.ApplicationEntity;
import com.nexgo.oaf.apiv3.card.mifare.DataFileEntity;
import com.nexgo.oaf.apiv3.card.mifare.DesfireHandler;
import com.nexgo.oaf.apiv3.card.mifare.DfNameEntity;
import com.nexgo.oaf.apiv3.card.mifare.FileSettingsEntity;
import com.nexgo.oaf.apiv3.card.mifare.KeySettingsEntity;
import com.nexgo.oaf.apiv3.card.mifare.KeyTypeEnum;
import com.nexgo.oaf.apiv3.card.mifare.RecordFileEntity;
import com.nexgo.oaf.apiv3.card.mifare.ValueFileEntity;
import com.nexgo.oaf.apiv3.card.mifare.VersionEntity;
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity;
import com.nexgo.oaf.apiv3.device.reader.CardReader;
import com.nexgo.oaf.apiv3.device.reader.CardSlotTypeEnum;
import com.nexgo.oaf.apiv3.device.reader.OnCardInfoListener;
import com.nexgo.oaf.apiv3.device.reader.RfCardTypeEnum;
import com.xinguodu.ddiinterface.Ddi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.nexgo.apiv3demo.DesfireActivity.AuthenTypeEnum.AuthenMode;
import static com.nexgo.apiv3demo.DesfireActivity.AuthenTypeEnum.ISOMode;
import static com.nexgo.apiv3demo.DesfireActivity.AuthenTypeEnum.AESMode;
import static com.nexgo.oaf.apiv3.card.mifare.KeyTypeEnum.AES;//only for AES authentication
import static com.nexgo.oaf.apiv3.card.mifare.KeyTypeEnum.DES_TDES_128; // for both authentication and ISO authentication
import static com.nexgo.oaf.apiv3.card.mifare.KeyTypeEnum.TDES_192;//only for ISO authentication
import static java.lang.System.arraycopy;

/**
 * Created by liushaopu on 2018/8/24.
 */

public class DesfireActivity extends Activity {
    private Button btnKey;
    private Button btnApp;
    private Button btnFile;
    private Button btnPay;
    private ListView applv;
    private android.support.v7.app.AlertDialog appAlertDialog;
    private DeviceEngine deviceEngine;
    private static final String TAG = "DesfireActivity";
    private DesfireHandler desfireHandler;
    private CardReader cardReader;
    private byte selectApp = 0;
    final private byte piccKeyNo = 0;//can't be modified cause there is only one picc master key
    byte[] defaultKey = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    byte[] defaultAppKey = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18};
    byte[] defaultAesKey = new byte[]{0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11};
    byte[] defaultIsokey = new byte[]{0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11,0x11,0x11,0x11,0x11,0x11};

    public enum AuthenTypeEnum {
        AuthenMode,
        ISOMode,
        AESMode
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceEngine = ((NexgoApplication) getApplication()).deviceEngine;
        cardReader = deviceEngine.getCardReader();
        desfireHandler = deviceEngine.getDesfireHandler();
        View dAppLv = getLayoutInflater().inflate(R.layout.dialog_app_list, null);
        applv = (ListView) dAppLv.findViewById(R.id.aidlistView);
        appAlertDialog = new android.support.v7.app.AlertDialog.Builder(this).setView(dAppLv).create();
        appAlertDialog.setCanceledOnTouchOutside(false);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Toolbar toolbar = new Toolbar(this);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
        toolbar.setBackgroundResource(typedValue.resourceId);
        toolbar.setNavigationIcon(getDrawable(R.drawable.arrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setTitle("      DesfireCardTest");
        toolbar.setTitleTextColor(Color.WHITE);
        linearLayout.addView(toolbar);

        params.setMargins(0,10,0,0);
        btnKey = new Button(this);
        btnKey.setText("OPERATE PICC KEY");
        btnKey.setBackground(getDrawable(R.drawable.btnselector));
        btnKey.setLayoutParams(params);
        btnKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OperatePiccKey();
            }
        });
        linearLayout.addView(btnKey,params);


        btnApp = new Button(this);
        btnApp.setText("OPERATE APP");
        btnApp.setBackground(getDrawable(R.drawable.btnselector));
        btnApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OperateApp();
            }
        });
        linearLayout.addView(btnApp,params);

        btnFile = new Button(this);
        btnFile.setText("OPERATER FILE");
        btnFile.setBackground(getDrawable(R.drawable.btnselector));
        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OperateFile();
            }
        });
        linearLayout.addView(btnFile,params);

        btnPay = new Button(this);
        btnPay.setText("Selcom Pay");
        btnPay.setLayoutParams(params);
        btnPay.setBackground(getDrawable(R.drawable.btnselector));
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PayFlow();
            }
        });
        linearLayout.addView(btnPay,params);
        setContentView(linearLayout);

    }


    private int authenticate(boolean isAuthen,byte keyNo,AuthenTypeEnum authenType,byte[] authenKey){
        int ret = 0;
        if(isAuthen == false){
            ret = -100;
            Log.d(TAG, "authenticate( No need to authenticate ): " + ret);
        } else {
            if (authenType == AuthenMode) {
                ret = desfireHandler.authenticate(keyNo, authenKey);
                Log.d(TAG, "authenticate(" + isAuthen + ", "  + keyNo + ", " + ByteUtils.byteArray2HexString(authenKey) + "): " + ret);
            } else if (authenType == ISOMode) {
                ret = desfireHandler.authenticateIso(keyNo, authenKey);
                Log.d(TAG, "authenticateIso(" + isAuthen + ", "  + keyNo + ", " + ByteUtils.byteArray2HexString(authenKey) + "): " + ret);
            } else if (authenType == AESMode) {
                ret = desfireHandler.authenticateAes(keyNo, authenKey);
                Log.d(TAG, "authenticateAes(" + isAuthen + ", "   + keyNo + ", " + ByteUtils.byteArray2HexString(authenKey) + "): " + ret);
            }
        }
        return ret;
    }

    private void OperatePiccKey(){
        //see chapter 9.3.6 only ONE PICC master key is stored on MIFARE DESFire EV1
        final CardReader cardReader = deviceEngine.getCardReader();
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.RF);
        cardReader.searchCard(slotTypes, 60, new OnCardInfoListener() {
            @Override
            public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
                    if (cardInfo != null) {
                    int ret = -1;
                    Log.d(TAG, "RF TYPE; " + cardInfo.getRfCardType());
                    if (cardInfo.getCardExistslot() == CardSlotTypeEnum.RF
                            && (cardInfo.getRfCardType() == RfCardTypeEnum.TYPE_A_CPU)) {
                        final DesfireHandler desfireHandler = deviceEngine.getDesfireHandler();
                        byte keySetting = 0, maxKeyNum = 0;
                        byte[] configKey = new byte[25];
                        Arrays.fill(configKey, (byte) 0);

                        boolean poweron = deviceEngine.getCPUCardHandler(CardSlotTypeEnum.RF).active();
                        Log.d(TAG, "CPUCardHandler active " + poweron);
                        if(poweron == false) return ;


                        ret = desfireHandler.selectApplication(new byte[]{0x00, 0x00, 0x00});
                        Log.d(TAG, "selectApplication(0x00,0x00,0x00): " + ret);


                        authenticate(true,piccKeyNo,AuthenMode,defaultKey);
                        Log.d(TAG, "formatPicc: " + desfireHandler.formatPicc());
                        Log.d(TAG, "getFreeMemory(): " + desfireHandler.getFreeMemory());
                        VersionEntity versionEntity = desfireHandler.getVersion();
                        Log.d(TAG,"getVersion = > " +"\n" +
                                "       hwVendorId : " + versionEntity.getHwVendorId()+"\n" +
                                "       hwType : " + versionEntity.getHwType()  + "\n" +
                                "       hwSubType : " + versionEntity.getHwSubType()   + "\n" +
                                "       hwMajorVer : " + versionEntity.getHwMajorVer() +"\n" +
                                "       hwMinorVer : " + versionEntity.getHwMinorVer() +"\n" +
                                "       hwSize : " + versionEntity.getHwSize() +"\n" +
                                "       hwProtocol : " + versionEntity.getHwProtocol() +"\n" +
                                "       swVendorId : " + versionEntity.getSwVendorId() +"\n" +
                                "       swType : " + versionEntity.getSwType() + "\n" +
                                "       swSubType : " + versionEntity.getSwSubType() + "\n" +
                                "       swMajorVer : " +  versionEntity.getSwMajorVer() + "\n" +
                                "       swMinorVer : " + versionEntity.getSwMinorVer() + "\n" +
                                "       swSize : " + versionEntity.getSwSize() + "\n" +
                                "       swProtocol : " +  versionEntity.getSwProtocol() + "\n" +
                                "       uid : " +  ByteUtils.byteArray2HexString(versionEntity.getUid()) + "\n" +
                                "       batchNo : " + ByteUtils.byteArray2HexString(versionEntity.getBatchNo()) + "\n" +
                                "       weekOfProduction : "  + versionEntity.getWeekOfProduction() + "\n" +
                                "       yearOfProduction : " + versionEntity.getYearOfProduction());



                        KeySettingsEntity keySettingsEntity = desfireHandler.getKeySettings();
                        if (keySettingsEntity != null) {
                            maxKeyNum = keySettingsEntity.getMaxKeyNum();
                            keySetting = keySettingsEntity.getKeySettings();
                            Log.d(TAG, "getKeySettings=> keySettings: " + ByteUtils.byte2BinaryString(keySetting) + ", maxKeyNum: " + maxKeyNum);
                        }

                        ret = desfireHandler.setConfiguration((byte)0x00,new byte[]{0x02});
                        Log.d(TAG, "setConfiguration(0x00,0x02); " + ret );//Format card and Random ID enabled; can not be reset
                        arraycopy(defaultAppKey,0,configKey,0,24);
                        ret = desfireHandler.setConfiguration((byte)0x01,configKey);//all applications will be personalized during creation with the default key
                        Log.d(TAG, "setConfiguration(0x01,"+ ByteUtils.byteArray2HexString(configKey) + "); " + ret );

                        Log.d(TAG, "CardUid [" + ByteUtils.byteArray2HexString(desfireHandler.getCardUid()) + "]" );

                        if ((keySetting & 0x01) == 0x01) {
                            //Case 1: Change PICC Master Key
                            ret = desfireHandler.changePiccMasterkey(AES, defaultAesKey, (byte) 0);
                            Log.d(TAG, "changePiccMasterkey(AES" +  ", " + ByteUtils.byteArray2HexString(defaultAesKey)+ ", " + 0 + "): " + ret);

                            //Authentication again
                            authenticate(true,piccKeyNo,AESMode,defaultAesKey);

                            //recover change key
                            ret = desfireHandler.changePiccMasterkey(DES_TDES_128, defaultKey, (byte) 0);
                            Log.d(TAG, "changePiccMasterkey(DES_TDES_128 " +  ", " + ByteUtils.byteArray2HexString(defaultKey)+  ", " +  0 + "): " + ret);

                            authenticate(true,piccKeyNo,AuthenMode,defaultKey);
                        }
                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DesfireActivity.this, "PICC Master Key Not changeable", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        if ((keySetting & 0x08) == 0x08) {
                            //Case 2: Change PICC Master Key Setting
                            ret = desfireHandler.changeKeySettings((byte)0x09);
                            Log.d(TAG, "changeKeySettings( " + 0x09 + " ); " + ret);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(DesfireActivity.this, "PICC Master KeySetting Not changeable", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        deviceEngine.getCPUCardHandler(CardSlotTypeEnum.RF).powerOff();
                    }
                }
            }

            @Override
            public void onSwipeIncorrect() {

            }

            @Override
            public void onMultipleCards() {

            }

        });
    }

    private int createApp(int appNum,byte masterKeySetting,byte numberOfKey,KeyTypeEnum keyType,boolean isSupFid){
        ApplicationEntity appInfo = new ApplicationEntity();
        //here use appNum,numberOfKey, masterKeySettign as Aid
        appInfo.setAid(new byte[]{numberOfKey,masterKeySetting,(byte)appNum});
        //here use appNum,isSupFid as IsoFid
        appInfo.setIsoFid(new byte[]{(byte)(isSupFid?1:0),(byte)appNum});
        appInfo.setDfName(ByteUtils.hexString2ByteArray("desfire.app." + appNum));
        appInfo.setMasterKeySetting(masterKeySetting);
        appInfo.setNumberOfKey(numberOfKey);
        appInfo.setKeyType(keyType);
        appInfo.setSupFid(isSupFid);
        appInfo.setSupIsoFid(isSupFid);
        int ret = desfireHandler.createApplication(appInfo);//Err 1C : Command Code Not Supported
        final String appStr = "createApp("+ appNum + "," + ByteUtils.byte2BinaryString(masterKeySetting) + ","  + numberOfKey + ","  + keyType + ","  + isSupFid + ") : " + ret;
        if(ret == 0) {
            Log.d(TAG, "ddi_rf_desfire_create_application(" + ByteUtils.byteArray2HexString(appInfo.getAid())+ "," + appInfo.getMasterKeySetting() + ","  + appInfo.getNumberOfKey()+ "," +
                    appInfo.getKeyType() + "," + appInfo.isSupFid() + "," + appInfo.isSupIsoFid() + "," + ByteUtils.byteArray2HexString(appInfo.getIsoFid()) +", "+ appInfo.getDfName().length + "," +
                    ByteUtils.byteArray2HexString(appInfo.getDfName()) + "," + 0 +"): " + ret);
        }else {
            //        ret = Ddi.ddi_rf_desfire_create_application(appInfo.getAid(),appInfo.getMasterKeySetting(), appInfo.getNumberOfKey(), (byte)appInfo.getKeyType().ordinal(),
//                (byte)(appInfo.isSupFid()?1:0),(byte)(appInfo.isSupIsoFid()?1:0),appInfo.getIsoFid(),(byte)appInfo.getDfName().length,appInfo.getDfName(),(byte)0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DesfireActivity.this, appStr, Toast.LENGTH_LONG).show();
                }
            });
        }
        return ret;
    }

    private void OperateApp(){
        // One PICC can hold up to 28 Applications ,Each application is linked to a set of up to 14 different user definable access keys
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.RF);
        cardReader.searchCard(slotTypes, 60, new OnCardInfoListener() {
            @Override
            public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
                if (cardInfo != null) {
                    Log.d(TAG, "RF TYPE; " + cardInfo.getRfCardType());
                    //determine if rf card type is Mifare card
                    if (cardInfo.getCardExistslot() == CardSlotTypeEnum.RF
                            && (cardInfo.getRfCardType() == RfCardTypeEnum.TYPE_A_CPU)){
                        int ret = -1;
                        byte maxKeyNum = 0,keySetting = 0;
                        boolean isCdAppAuth = true, isAppDirAuth = true;
                        boolean poweron = deviceEngine.getCPUCardHandler(CardSlotTypeEnum.RF).active();
                        Log.d(TAG, "CPUCardHandler active " + poweron);
                        if(poweron == false) return ;

                        ret = desfireHandler.selectApplication(new byte[]{0x00, 0x00, 0x00});
                        Log.d(TAG, "selectApplication(0x00,0x00,0x00): " + ret);

                        authenticate(true,piccKeyNo,AuthenMode,defaultKey);
                        Log.d(TAG, "formatPicc: " + desfireHandler.formatPicc());//Err 1C: Command Code not supported
                        KeySettingsEntity keySettingsEntity = desfireHandler.getKeySettings();
                        if (keySettingsEntity != null) {
                            maxKeyNum = keySettingsEntity.getMaxKeyNum();
                            keySetting = keySettingsEntity.getKeySettings();
                            Log.d(TAG, "getKeySettings=> keySettings: " + ByteUtils.byte2BinaryString(keySetting) + ", maxKeyNum: " + maxKeyNum);
                            if(0 == (keySetting & 0x04))isCdAppAuth = false;//bit2: whether PICC master key authentication is needed before Create- / DeleteApplication
                            if(0 == (keySetting & 0x02)) isAppDirAuth = false;//bit1: whether PICC master key authentication is needed for application directory access(GetApplicationIDs,GetDFNames,GetKeySettings)
                        }

                        authenticate(isCdAppAuth,piccKeyNo,AuthenMode,defaultKey);
                        byte masterKeySetting = 0, numberOfKey = 14,appCnt = 0,appNum = 0;
                        KeyTypeEnum keyType = DES_TDES_128;
                        boolean isSupFid = true ,isEnough = false;// isSupFid and isSupIsoFid has the same meaning
                        for(int i = 0; i <= 0x0F; i++){
                            for(int j = 0; j<= 0x0F; j++){
                                masterKeySetting = 0;
                                keyType = DES_TDES_128;
                                if(appNum%3 == 1) keyType = TDES_192;
                                else if(appNum%3 == 2) keyType = AES;
                                masterKeySetting |= (i<<4);
                                masterKeySetting |= j;
                                if(appCnt > 10) break;
                                ret = createApp(++appNum, masterKeySetting, numberOfKey, keyType, isSupFid);// Err 9E: Value of the parameters invalid
                                if (ret == 0) appCnt++;
                                else break;
                            }
                            if(0 != ret || appCnt > 10) break;
                        }

                        authenticate(isAppDirAuth,piccKeyNo,AuthenMode,defaultKey);

                        // Get card id and number (app id  have 3 Bytes)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final List<byte[]> aidList =  desfireHandler.getAids();
                                List<Map<String, String>> listItem = new ArrayList<>();
                                for(int i = 0; i < aidList.size(); i ++){
//                                    Log.d(TAG,"getAids" + i + "->Aid = " + ByteUtils.byteArray2HexString(aidList.get(i)));
                                    Map<String, String> map = new HashMap<>();
                                    map.put("appIdx", (i + 1) + "");
                                    map.put("appName", ByteUtils.byteArray2HexString(aidList.get(i)));
                                    listItem.add(map);
                                }
                                if(aidList.size() > 0) {
                                    SimpleAdapter adapter = new SimpleAdapter(DesfireActivity.this,
                                            listItem,
                                            R.layout.app_list_item,
                                            new String[]{"appIdx", "appName"},
                                            new int[]{R.id.tv_appIndex, R.id.tv_appName});
                                            applv.setAdapter(adapter);

                                            applv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                @Override
                                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                    appAlertDialog.dismiss();
                                                    appAlertDialog.cancel();

                                                    //Select APP
                                                    byte appkeyNo = 0, keySetting = 0, maxKeyNum = 0, keyVer = 0,specKeyNo = 0;
                                                    selectApp = (byte)position;
                                                    int ret = desfireHandler.selectApplication(aidList.get(position));
                                                    Log.d(TAG, "selectApplication " + ByteUtils.byteArray2HexString(aidList.get(position)) + ": " + ret);


                                                    if(0 != authenticate(true,appkeyNo,ISOMode,defaultAppKey)) {
                                                        if(0 != authenticate(true,appkeyNo,AuthenMode,defaultKey)){
                                                            return;
                                                        }
                                                    }

                                                    KeySettingsEntity keySettingsEntity = desfireHandler.getKeySettings();
                                                    if (keySettingsEntity == null) {
                                                        byte[] var1 = new byte[1], var2 = new byte[1];
                                                        ret = Ddi.ddi_rf_desfire_get_key_settings(var1,var2);
                                                        Log.d(TAG, "ddi_rf_desfire_get_key_settings => [ " + "keySetting = " + var1[0] + ", maxKeyNum = " + maxKeyNum + "]: "  +  ret);
                                                        return ;
                                                    }
                                                    maxKeyNum = keySettingsEntity.getMaxKeyNum();
                                                    keySetting = keySettingsEntity.getKeySettings();
                                                    Log.d(TAG, "getKeySettings=> keySettings: " + ByteUtils.byte2BinaryString(keySetting) + ", maxKeyNum: " + maxKeyNum);
                                                    if((keySetting & 0xE0)< 0xE) specKeyNo = (byte)(keySetting&0xE0);

                                                    if ((keySetting & 0x01) == 0x01) {//whether the application master key is changeable
                                                        if(0 != authenticate(true,specKeyNo,ISOMode,defaultAppKey)) return;
                                                        ret = desfireHandler.changeAppKey(TDES_192, appkeyNo, defaultAppKey, defaultIsokey, keyVer);
                                                        Log.d(TAG, "changeAppKey( " + TDES_192  + ", " + appkeyNo  + ", " +
                                                                ByteUtils.byteArray2HexString(defaultAppKey)  + ", " +  ByteUtils.byteArray2HexString(defaultIsokey)  + "): " + ret);
                                                        if(0 == ret) {
                                                            authenticate(true, appkeyNo, ISOMode, defaultIsokey);
                                                            // recover the key and authenticate again
                                                            ret = desfireHandler.changeAppKey(TDES_192, (byte) appkeyNo, defaultIsokey, defaultAppKey, keyVer);
                                                            Log.d(TAG, "changeAppKey( " + TDES_192 + ", " + appkeyNo + ", " +
                                                                    ByteUtils.byteArray2HexString(defaultIsokey) + ", " + ByteUtils.byteArray2HexString(defaultAppKey) + "): " + ret);
                                                            authenticate(true, appkeyNo, AuthenMode, defaultAppKey);
                                                        }
                                                    }
                                                    if ((keySetting & 0x08) == 0x08) {//whether a change of the application master key settings is allowed
                                                        ret = desfireHandler.changeKeySettings((byte) 0xE9);
                                                        Log.d(TAG, "changeKeySettings( " + 0xE9 + ") : " + ret);
                                                        //recover the original keysettings
                                                        ret = desfireHandler.changeKeySettings(keySetting);
                                                        Log.d(TAG, "changeKeySettings(" + keySetting + ") : " + ret);
                                                    }

                                                    Log.d(TAG, "getFreeMemory(): " + desfireHandler.getFreeMemory());
                                                    deviceEngine.getCPUCardHandler(CardSlotTypeEnum.RF).powerOff();
                                                }
                                    });
                                    appAlertDialog.show();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onSwipeIncorrect() {

            }

            @Override
            public void onMultipleCards() {

            }
        });
    }

    private FileSettingsEntity fileInfoShow(byte appNum,byte fileNo){
        List<byte[]> aidList =  desfireHandler.getAids();

        final StringBuilder sb = new StringBuilder();
        FileSettingsEntity fileInfo = desfireHandler.getFileSettings((byte) fileNo);
        if(fileInfo != null) {
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > fileSize : " + fileInfo.getFileSize() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > fileType : " + fileInfo.getFileType() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > recordSize : " + fileInfo.getRecordSize() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > commSettings : " + fileInfo.getCommSettings() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > lowerLimit : " + fileInfo.getLowerLimit() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > upperLimit : " + fileInfo.getUpperLimit() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > maxNumberOfRecords : " + fileInfo.getMaxNumberOfRecords() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > currentNumberOfRecords : " + fileInfo.getCurrentNumberOfRecords() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > readAccessRightKeyNum : " + fileInfo.getReadAccessRightKeyNum() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > writeAccessRightKeyNum : " + fileInfo.getWriteAccessRightKeyNum() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > readAndWriteAccessRightKeyNum : " + fileInfo.getReadAndWriteAccessRightKeyNum() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > changeAccessRightKeyNum : " + fileInfo.getChangeAccessRightKeyNum() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > limitedCreditValue : " + fileInfo.getLimitedCreditValue() + "\n");
            sb.append(aidList.get(appNum) + "[" + fileNo + "] - > limitedCreditEnabled : " + fileInfo.isLimitedCreditEnabled() + "\n");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(DesfireActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
        return fileInfo;
    }

    private void OperateFile(){
        final CardReader cardReader = deviceEngine.getCardReader();
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.RF);
        cardReader.searchCard(slotTypes, 60, new OnCardInfoListener() {
            @Override
            public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
                if (cardInfo != null) {
                    int ret = -1;
                    Log.d(TAG, "RF TYPE; " + cardInfo.getRfCardType());
                    //determine if rf card type is Mifare card
                    if (cardInfo.getCardExistslot() == CardSlotTypeEnum.RF
                            && (cardInfo.getRfCardType() == RfCardTypeEnum.TYPE_A_CPU)) {
                        int size = 0;
                        byte keyNo = 0,appNum = 0,fileNo = 0,maxKeyNum = 0,keySetting = 0;
                        boolean isCdFileAuth = true,isFileDirAuth = true,isAppDirAuth = true ;
                        boolean poweron = deviceEngine.getCPUCardHandler(CardSlotTypeEnum.RF).active();
                        Log.d(TAG, "CPUCardHandler active " + poweron);


                        //Authentication
                        if(0 != authenticate(true,piccKeyNo,AuthenMode,defaultKey));
                        Log.d(TAG, "getFreeMemory(): " + desfireHandler.getFreeMemory());

                        //get PIcc master key settings
                        KeySettingsEntity keySettingsEntity = desfireHandler.getKeySettings();
                        if (keySettingsEntity != null) {
                            maxKeyNum = keySettingsEntity.getMaxKeyNum();
                            keySetting = keySettingsEntity.getKeySettings();
                            Log.d(TAG, "getKeySettings=> keySettings: " + ByteUtils.byte2BinaryString(keySetting) + ", maxKeyNum: " + maxKeyNum);
                            if(0 == (keySetting & 0x02)) isAppDirAuth = false;//whether PICC master key authentication is needed for application directory access(GetApplicationIDs,GetDFNames,GetKeySettings)
                        }


                        //get DfNames
                        List<DfNameEntity>  dfnameList = desfireHandler.getDfNames();
                        size = dfnameList.size();
                        for (int i = 0; i < size; i++) {
                            Log.d(TAG,"getDfNames" + i + "->Aid = " + ByteUtils.byteArray2HexString(dfnameList.get(i).getAid()));
                            Log.d(TAG,"getDfNames" + i + "->DfName = " + ByteUtils.byteArray2HexString(dfnameList.get(i).getDfName()));
                            Log.d(TAG,"getDfNames" + i + "->IsoFid = " + ByteUtils.byteArray2HexString(dfnameList.get(i).getIsoFid()));
                        }


                        //select app: Up to 32 files of different size and type can be created within  each application
                        ret = desfireHandler.selectApplication(dfnameList.get(selectApp).getAid());
                        Log.d(TAG,"selectApplication " + ByteUtils.byteArray2HexString(dfnameList.get(selectApp).getAid()) +  ": " + ret);

                        keySettingsEntity = desfireHandler.getKeySettings();
                        if (keySettingsEntity != null) {
                            maxKeyNum = keySettingsEntity.getMaxKeyNum();
                            keySetting = keySettingsEntity.getKeySettings();
                            Log.d(TAG, "getKeySettings=> keySettings: " + ByteUtils.byte2BinaryString(keySetting) + ", maxKeyNum: " + maxKeyNum);
                            if((keySetting & 0x04)==0) isCdFileAuth = false;//bit2:whether application master key authentication is needed before CreateFile / DeleteFile
                            if((keySetting & 0x02)==0) isFileDirAuth = false;//bit1:whether application master key authentication is needed for file directory access
                            return;
                        }

                        if(0 != authenticate(isCdFileAuth,keyNo,ISOMode,defaultAppKey)) {
                            if(0 != authenticate(isCdFileAuth,keyNo,AuthenMode,defaultKey)){
                                if(isCdFileAuth == true) return;
                            }
                        }

                        fileNo++;
                        DataFileEntity dataFile = new DataFileEntity();
                        dataFile.setFileSize(30);
                        dataFile.setCommSettings((byte) 0x00);// 0x00 ;Plain communication
                        dataFile.setReadAccessRightKeyNum((byte)1);
                        dataFile.setWriteAccessRightKeyNum((byte)2);
                        dataFile.setReadAndWriteAccessRightKeyNum((byte)3);
                        dataFile.setChangeAccessRightKeyNum((byte)4);
                        dataFile.setIsoFid(new byte[]{0x00,0x01});
                        dataFile.setIsoFidEnable(true);
                        ret = desfireHandler.createBackupDatafile(fileNo,dataFile);
                        if(ret == -1 ) return;
                        fileInfoShow(appNum,fileNo);

                        fileNo++;
//                        if(0 != authenticate(isCdFileAuth,keyNo,ISOMode,defaultAppKey)) return;
                        dataFile.setFileSize(20);
                        dataFile.setIsoFid(new byte[]{0x00,0x02});
                        ret = desfireHandler.createStdDataFile(fileNo,dataFile);
                        if(ret == -1 ) return;
                        fileInfoShow(appNum,fileNo);

                        //Record File: Write,Read,Clear
                        fileNo++;
//                        if(0 != authenticate(isCdFileAuth,keyNo,AESMode,defaultAppKey)) return;
                        RecordFileEntity recordFile = new RecordFileEntity();
                        recordFile.setIsoFid(new byte[]{0x00,0x03});
                        recordFile.setIsoFidEnable(true);
                        recordFile.setCommSettings((byte)0x02);//0x02: Plain communication
                        recordFile.setReadAccessRightKeyNum((byte)1);
                        recordFile.setWriteAccessRightKeyNum((byte)2);
                        recordFile.setReadAndWriteAccessRightKeyNum((byte)3);
                        recordFile.setChangeAccessRightKeyNum((byte)4);
                        recordFile.setAllowedRandomWriteAccess(true);
                        recordFile.setSpecifiesRandomWriteAccessOption((byte)0x01);//0x00 - not, 0x01 - yes
                        recordFile.setRecordSize(20);
                        recordFile.setMaxNumberOfRecords(3);
                        ret = desfireHandler.createCyclicRecordFile(fileNo,recordFile);
                        if(ret == -1 ) return;
                        fileInfoShow(appNum,fileNo);

                        fileNo++;
//                        if(0 != authenticate(isCdFileAuth,keyNo,AuthenMode,defaultAppKey)) return;
                        recordFile.setCommSettings((byte)0x04);//0x03: Fully enciphered communication
                        recordFile.setIsoFid(new byte[]{0x00,0x03});
                        ret = desfireHandler.createLinearRecordFile(fileNo,recordFile);
                        if(ret == -1 ) return;
                        fileInfoShow(appNum,fileNo);


                        fileNo++;
//                        if(0 != authenticate(isCdFileAuth,keyNo,AuthenMode,defaultAppKey)) return;
                        //Value File :Create,Credit,Debit,Limited Credit,Read,Delete
                        ValueFileEntity valueFile = new ValueFileEntity();
                        valueFile.setCommSettings((byte)0x01); //0x01: Plain communication secured by MAC
                        valueFile.setChangeAccessRightKeyNum((byte) 5);
                        valueFile.setReadAccessRightKeyNum((byte)6);
                        valueFile.setWriteAccessRightKeyNum((byte)7);
                        valueFile.setReadAndWriteAccessRightKeyNum((byte)8);
                        valueFile.setInitValue(1024);
                        valueFile.setLimitedCreditEnabled(true);
                        valueFile.setLowerLimit(0);
                        valueFile.setUpperLimit(10066329);//0x00,0x99.0x99.0x99
                        ret = desfireHandler.createValueFile(fileNo,valueFile);
                        if(ret == -1 ) return;
                        fileInfoShow(appNum,fileNo);

                        //Get file ID
                        if(0 != authenticate(isFileDirAuth,keyNo,AuthenMode,defaultAppKey)) return;
                        List<byte[]> fids  = desfireHandler.getFids();
                        for(int i = 0; i < fids.size(); i ++ ){
                            Log.d(TAG,"getFids" + i + " = " + ByteUtils.byteArray2HexString(fids.get(i)));
                        }
                        List<byte[]> isoFids  = desfireHandler.getIsoFids();
                        size = isoFids.size();
                        for(int i = 0; i < size; i ++ ){
                            Log.d(TAG,"getFids" + i + " = " + ByteUtils.byteArray2HexString(isoFids.get(i)));
                        }

                        for(byte i = 0; i < fileNo; i++){
                            desfireHandler.deleteFile(i);
                        }

                        Log.d(TAG, "getFreeMemory(): " + desfireHandler.getFreeMemory());
                        deviceEngine.getCPUCardHandler(CardSlotTypeEnum.RF).powerOff();

                    }
                }
            }

            @Override
            public void onSwipeIncorrect() {

            }

            @Override
            public void onMultipleCards() {

            }
        });
    }

    private void PerformStdDataFile(byte fileNo,boolean isStdFile){
        int ret = -1;
        DataFileEntity dataFile = new DataFileEntity();
        dataFile.setFileSize(0x3c);//The MF3 internally allocates NV-memory in a multiple of 32 bytes
        dataFile.setChangeAccessRightKeyNum((byte)1);
        dataFile.setCommSettings((byte) 0x00);// 0x00 ;Plain communication
        dataFile.setReadAccessRightKeyNum((byte)2);
        dataFile.setWriteAccessRightKeyNum((byte)3);
        dataFile.setReadAndWriteAccessRightKeyNum((byte)4);
        dataFile.setChangeAccessRightKeyNum((byte)5);
        dataFile.setIsoFid(new byte[]{0x00,0x01});
        dataFile.setIsoFidEnable(true);//this value should be filled according to parameter isSupFid of current application
        if(isStdFile == true) {
            ret = desfireHandler.createStdDataFile(fileNo, dataFile);
            Toast.makeText(DesfireActivity.this, "createStdDataFile : " + ret, Toast.LENGTH_SHORT).show();
//            ret = Ddi.ddi_rf_desfire_create_std_datafile(fileNo, (byte)(dataFile.isIsoFidEnable()?1:0),dataFile.getIsoFid(),dataFile.getCommSettings(),
//        dataFile.getReadAccessRightKeyNum(),dataFile.getWriteAccessRightKeyNum(),dataFile.getReadAndWriteAccessRightKeyNum(),dataFile.getChangeAccessRightKeyNum(),
//                dataFile.getFileSize());
            Log.d(TAG, "ddi_rf_desfire_create_std_datafile(" + fileNo + "," + dataFile.isIsoFidEnable() + ","  +ByteUtils.byteArray2HexString(dataFile.getIsoFid()) + "," +
                    dataFile.getCommSettings() + "," + dataFile.getReadAccessRightKeyNum() + "," + dataFile.getWriteAccessRightKeyNum() + "," + dataFile.getReadAndWriteAccessRightKeyNum() + "," +
                    dataFile.getChangeAccessRightKeyNum() + "," + dataFile.getFileSize() +"): " + ret);
        }else {
            ret = desfireHandler.createStdDataFile(fileNo, dataFile);
            Toast.makeText(DesfireActivity.this, "createStdDataFile : " + ret, Toast.LENGTH_SHORT).show();
        }

        if(0 == ret) {
            authenticate(true,dataFile.getWriteAccessRightKeyNum(),ISOMode,defaultAppKey);
            ret = desfireHandler.writeData(fileNo, dataFile.getCommSettings(), 0,
                    ByteUtils.hexString2ByteArray("int createStdDataFile(byte fileNo, DataFileEntity dataFile);"));
            Toast.makeText(DesfireActivity.this, "writeData : " + ret, Toast.LENGTH_SHORT).show();
            desfireHandler.commitTransaction();

            authenticate(true,dataFile.getReadAccessRightKeyNum(),ISOMode,defaultAppKey);
            byte readData[] = desfireHandler.readData(fileNo, dataFile.getCommSettings(), 0, 60);
            Log.d(TAG,"readData : " + ByteUtils.byteArray2HexString(readData));
        }
    }

    private void PerformValueFile(byte fileNo){
        //Value File :Create,Credit,Debit,Limited Credit,Read,Delete
        ValueFileEntity valueFile = new ValueFileEntity();
        valueFile.setCommSettings((byte)0x01); //0x01: Plain communication secured by MAC
        valueFile.setChangeAccessRightKeyNum((byte) 5);
        valueFile.setReadAccessRightKeyNum((byte)6);
        valueFile.setWriteAccessRightKeyNum((byte)7);
        valueFile.setReadAndWriteAccessRightKeyNum((byte)8);
        valueFile.setInitValue(1024);
        valueFile.setLimitedCreditEnabled(true);
        valueFile.setLowerLimit(0);
        valueFile.setUpperLimit(10066329);//0x00,0x99.0x99.0x99:10066329
        int ret = desfireHandler.createValueFile(fileNo,valueFile);
        Toast.makeText(DesfireActivity.this, "createValueFile : " + ret, Toast.LENGTH_SHORT).show();
        if(ret == 0){

            //Read the currently stored value from Value Files.
            if(0 != authenticate(true,valueFile.getReadAccessRightKeyNum(),ISOMode,defaultAppKey)) return;
            ret = desfireHandler.getValue(fileNo,valueFile.getCommSettings());
            Toast.makeText(DesfireActivity.this, "getValue : " + ret, Toast.LENGTH_SHORT).show();


            //Allows a limited increase of a value stored in a Value File
            int value = 1;
            if(0 != authenticate(true,valueFile.getWriteAccessRightKeyNum(),ISOMode,defaultAppKey)) return;
//            ret = desfireHandler.limitedCredit(fileNo,valueFile.getCommSettings(),value);
//            Toast.makeText(DesfireActivity.this, "limitedCredit " + value + "; " + ret, Toast.LENGTH_SHORT).show();
            ret = Ddi.ddi_rf_desfire_limited_credit(fileNo,valueFile.getCommSettings(),value);
            Log.d(TAG,"ddi_rf_desfire_limited_credit( " + fileNo + ","  + valueFile.getCommSettings() + ", " + value + ") : " + ret);
            //Increase a value stored in a Value File.
            value = 1048;
            if(0 != authenticate(true,valueFile.getReadAndWriteAccessRightKeyNum(),ISOMode,defaultAppKey)) return;
            ret = desfireHandler.credit(fileNo,valueFile.getCommSettings(),value);
            Toast.makeText(DesfireActivity.this, "credit " + value + "; " + ret, Toast.LENGTH_SHORT).show();

            value = 2000;
            ret = desfireHandler.credit(fileNo,valueFile.getCommSettings(),value);
            Toast.makeText(DesfireActivity.this, "credit " + value + "; " + ret, Toast.LENGTH_SHORT).show();

            //Decrease a value stored in a Value File.
            value = 3000;
            if(0 != authenticate(true,valueFile.getWriteAccessRightKeyNum(),ISOMode,defaultAppKey)) return;
            ret = desfireHandler.debit(fileNo,valueFile.getCommSettings(),value);
            Toast.makeText(DesfireActivity.this, "debit " + value + "; " + ret, Toast.LENGTH_SHORT).show();
            desfireHandler.commitTransaction();


            //Read the currently stored value from Value Files.
            if(0 != authenticate(true,valueFile.getReadAndWriteAccessRightKeyNum(),ISOMode,defaultAppKey));
            ret = desfireHandler.getValue(fileNo,valueFile.getCommSettings());
            Toast.makeText(DesfireActivity.this, "getValue : " + ret, Toast.LENGTH_SHORT).show();
            desfireHandler.abortTransaction();
        }
    }

    private void PerformRecordFile(byte fileNo,boolean isLinearFile){
        int ret = -1;
        RecordFileEntity recordFile = new RecordFileEntity();
        recordFile.setIsoFid(new byte[]{0x00,0x03});
        recordFile.setIsoFidEnable(true);
        recordFile.setCommSettings((byte)0x03);//0x02: Plain communication
        recordFile.setReadAccessRightKeyNum((byte)1);
        recordFile.setWriteAccessRightKeyNum((byte)2);
        recordFile.setChangeAccessRightKeyNum((byte)3);
        recordFile.setReadAndWriteAccessRightKeyNum((byte)4);
        recordFile.setAllowedRandomWriteAccess(true);
        recordFile.setSpecifiesRandomWriteAccessOption((byte)0x01);//0x00 - not, 0x01 - yes
        recordFile.setRecordSize(64);
        recordFile.setMaxNumberOfRecords(5);
        if(isLinearFile == true){
            ret = desfireHandler.createLinearRecordFile(fileNo,recordFile);
            Toast.makeText(DesfireActivity.this, "createLinearRecordFile : " + ret, Toast.LENGTH_SHORT).show();
        }
        else{
            ret = desfireHandler.createCyclicRecordFile(fileNo,recordFile);
            Toast.makeText(DesfireActivity.this, "createLinearRecordFile : " + ret, Toast.LENGTH_SHORT).show();
        }
        if(0 == ret){
            if(0 !=authenticate(true,recordFile.getWriteAccessRightKeyNum(),ISOMode,defaultAppKey)) return;
            ret = desfireHandler.writeRecord(fileNo, recordFile.getCommSettings(), 0, 0x0A ,new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A});
            Toast.makeText(DesfireActivity.this, "writeRecord : " + ret, Toast.LENGTH_SHORT).show();
            Log.d(TAG,"writeRecord(" + fileNo + "," + recordFile.getCommSettings() + "," + 0 + "," + 0x0A + "," +  ByteUtils.byteArray2HexString(new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A}) + "); " + ret);
            desfireHandler.commitTransaction();

            ret = desfireHandler.writeRecord(fileNo, recordFile.getCommSettings(), 0x0, 0x0A ,new byte[]{0x11,0x22,0x33,0x44,0x55,0x66,0x77,(byte)0x88,(byte)0x99,(byte)0xAA});
            Log.d(TAG,"writeRecord(" + fileNo + "," + recordFile.getCommSettings() + "," + 0 + "," + 0x0A + ","  +  ByteUtils.byteArray2HexString(new byte[]{0x11,0x22,0x33,0x44,0x55,0x66,0x77,(byte)0x88,(byte)0x99,(byte)0xAA}) + "); " + ret);
            Toast.makeText(DesfireActivity.this, "writeRecord : " + ret, Toast.LENGTH_SHORT).show();
            desfireHandler.commitTransaction();

            if(0 !=authenticate(true,recordFile.getReadAccessRightKeyNum(),ISOMode,defaultAppKey)) return;
            byte[] readData1   = desfireHandler.readRecords(fileNo, recordFile.getCommSettings(), 0x0A,1, 0x00);
            Log.d(TAG,"readRecords( : " + fileNo + "," + recordFile.getCommSettings() + "," + 0x0A + "," + 0 + "," + 0x00 +"); " + ByteUtils.byteArray2HexString(readData1));
            desfireHandler.clearRecordFile(fileNo);
        }
    }

    private void PayFlow(){
        final CardReader cardReader = deviceEngine.getCardReader();
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.RF);
        cardReader.searchCard(slotTypes, 60, new OnCardInfoListener() {
            @Override
            public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
                if (cardInfo != null) {
                    int ret = -1;
                    boolean isCdAppAuth = true, isCdFileAuth = true,isAppDirAuth = true;
                    byte keyNo = 0, keySetting = 0, maxKeyNum = 0,numberOfKey = 0,masterKeySetting = 0;
                    Log.d(TAG, "RF TYPE; " + cardInfo.getRfCardType());
                    //step 2 ,judge if rf card type is Mifare card
                    if (cardInfo.getCardExistslot() == CardSlotTypeEnum.RF
                            && (cardInfo.getRfCardType() == RfCardTypeEnum.TYPE_A_CPU)) {
                        boolean poweron = deviceEngine.getCPUCardHandler(CardSlotTypeEnum.RF).active();
                        Log.d(TAG, "active: " + poweron);
                        if(poweron == false) return;

                        Log.d(TAG, "getFreeMemory(): " + desfireHandler.getFreeMemory());
                        authenticate(true,piccKeyNo,AuthenMode,defaultKey);
                        Log.d(TAG, "formatPicc(): " + desfireHandler.formatPicc());

                        //get picc master key settings
                        KeySettingsEntity keySettingsEntity = desfireHandler.getKeySettings();
                        if (keySettingsEntity != null) {
                            maxKeyNum = keySettingsEntity.getMaxKeyNum();
                            keySetting = keySettingsEntity.getKeySettings();
                            Log.d(TAG, "getKeySettings=> keySettings: " + ByteUtils.byte2BinaryString(keySetting) + ", maxKeyNum: " + maxKeyNum);
                            if(0 == (keySetting & 0x04)) isCdAppAuth = false;//bit2 ----- whether PICC master key authentication is needed before Create- / DeleteApplication
                            if(0 == (keySetting & 0x02)) isAppDirAuth = false;//bit1: whether PICC master key authentication is needed for application directory access(GetApplicationIDs,GetDFNames,GetKeySettings)
                        }

                        //create application
                        numberOfKey = 14;
                        masterKeySetting = (byte)0xE9;
                        byte random = (byte)new Random().nextInt(100);
                        authenticate(isCdAppAuth,piccKeyNo,AuthenMode,defaultKey);
                        if(0 != createApp(random,masterKeySetting,numberOfKey,DES_TDES_128,true)) return;

                        //select application
                        ret = desfireHandler.selectApplication(new byte[]{numberOfKey,masterKeySetting,random});
                        Log.d(TAG, "selectApplication(" +  numberOfKey + masterKeySetting + random + "): " + ret);

                        //get application key settings
//                        authenticate(isAppDirAuth,keyNo,AuthenMode,defaultAppKey);
                        keySettingsEntity = desfireHandler.getKeySettings();
                        if (keySettingsEntity != null) {
                            maxKeyNum = keySettingsEntity.getMaxKeyNum();
                            keySetting = keySettingsEntity.getKeySettings();
                            Log.d(TAG, "getKeySettings=> keySettings: " + ByteUtils.byte2BinaryString(keySetting) + ", maxKeyNum: " + maxKeyNum);
                            if(0 == (keySetting & 0x04)) isCdFileAuth = false;//bit2 ----- whether application master key authentication is needed before CreateFile / DeleteFile
                        }

                        //data manipulation
                        authenticate(isCdFileAuth,keyNo,AuthenMode,defaultAppKey);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String[] items = new String[]{"StdData File", "BackupData File", "Value File", "LinearRecord File", "CyclicRecord File"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(DesfireActivity.this)
                                        .setTitle("File Type Select")
                                        .setItems(items, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                byte fileNo = 0;
                                                switch (which) {
                                                    case 0:
                                                        PerformStdDataFile(fileNo,true);
                                                        break;

                                                    case 1:
                                                        PerformStdDataFile(fileNo,false);
                                                        break;

                                                    case 2:
                                                        PerformValueFile(fileNo);
                                                        break;

                                                    case 3:
                                                        PerformRecordFile(fileNo,true);
                                                        break;

                                                    case 4:
                                                        PerformRecordFile(fileNo,false);
                                                        break;

                                                    default:
                                                        break;

                                                }
                                                desfireHandler.deleteFile(fileNo);
                                                deviceEngine.getCPUCardHandler(CardSlotTypeEnum.RF).powerOff();
                                            }
                                        });
                                builder.create().show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onSwipeIncorrect() {

            }

            @Override
            public void onMultipleCards() {

            }
        });
    }

}

