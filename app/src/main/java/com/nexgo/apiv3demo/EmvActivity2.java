package com.nexgo.apiv3demo;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nexgo.common.ByteUtils;
import com.nexgo.common.LogUtils;
import com.nexgo.common.TlvUtils;
import com.nexgo.oaf.apiv3.DeviceEngine;
import com.nexgo.oaf.apiv3.SdkResult;
import com.nexgo.oaf.apiv3.device.extpinpad.ExtPinPad;
import com.nexgo.oaf.apiv3.device.extpinpad.ExtPinPadKeyAlgTypeEnum;
import com.nexgo.oaf.apiv3.device.extpinpad.OnExtPinPadInputPinListener;
import com.nexgo.oaf.apiv3.device.pinpad.AlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.OnPinPadInputListener;
import com.nexgo.oaf.apiv3.device.pinpad.PinAlgorithmModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinKeyboardModeEnum;
import com.nexgo.oaf.apiv3.device.pinpad.PinPad;
import com.nexgo.oaf.apiv3.device.pinpad.PinPadKeyCode;
import com.nexgo.oaf.apiv3.device.pinpad.PinPadTypeEnum;
import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity;
import com.nexgo.oaf.apiv3.device.reader.CardReader;
import com.nexgo.oaf.apiv3.device.reader.CardSlotTypeEnum;
import com.nexgo.oaf.apiv3.device.reader.OnCardInfoListener;
import com.nexgo.oaf.apiv3.device.reader.ReaderTypeEnum;
import com.nexgo.oaf.apiv3.emv.AidEntity;
import com.nexgo.oaf.apiv3.emv.AmexTransDataEntity;
import com.nexgo.oaf.apiv3.emv.CandidateAppInfoEntity;
import com.nexgo.oaf.apiv3.emv.CapkEntity;
import com.nexgo.oaf.apiv3.emv.DynamicReaderLimitEntity;
import com.nexgo.oaf.apiv3.emv.EmvCardBrandEnum;
import com.nexgo.oaf.apiv3.emv.EmvCvmResultEnum;
import com.nexgo.oaf.apiv3.emv.EmvDataSourceEnum;
import com.nexgo.oaf.apiv3.emv.EmvEntryModeEnum;
import com.nexgo.oaf.apiv3.emv.EmvHandler2;
import com.nexgo.oaf.apiv3.emv.EmvOnlineResultEntity;
import com.nexgo.oaf.apiv3.emv.EmvProcessFlowEnum;
import com.nexgo.oaf.apiv3.emv.EmvProcessResultEntity;
import com.nexgo.oaf.apiv3.emv.EmvTransConfigurationEntity;
import com.nexgo.oaf.apiv3.emv.OnEmvProcessListener2;
import com.nexgo.oaf.apiv3.emv.PromptEnum;
import com.nexgo.oaf.apiv3.emv.UnionPayTransDataEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmvActivity2 extends AppCompatActivity implements OnCardInfoListener, OnEmvProcessListener2, OnPinPadInputListener {
    private DeviceEngine deviceEngine;
    private EmvHandler2 emvHandler2;
    private String cardNo;
    private String pwdText;
    private TextView pwdTv;
    private AlertDialog pwdAlertDialog;
    private View dv;
    private Context mContext;
    private EmvUtils emvUtils;
    private boolean externalReader = false;

    private TextView tvDesc;

    private int pinpadPort = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emv2);
        mContext = this;
        deviceEngine = ((NexgoApplication) getApplication()).deviceEngine;
        emvHandler2 = deviceEngine.getEmvHandler2("app2");
        dv = getLayoutInflater().inflate(R.layout.dialog_inputpin_layout, null);
        pwdTv = (TextView) dv.findViewById(R.id.input_pin);
        pwdAlertDialog = new AlertDialog.Builder(this).setView(dv).create();
        pwdAlertDialog.setCanceledOnTouchOutside(false);

        //enable below lines to capture the EMV logs
        emvHandler2.emvDebugLog(true);
        LogUtils.setDebugEnable(true);

        emvHandler2.initReader(ReaderTypeEnum.INNER, 0);

        emvUtils = new EmvUtils(EmvActivity2.this);

        RadioGroup rgAlg;
        rgAlg = (RadioGroup) findViewById(R.id.rgAlg);
        if (rgAlg != null)
            rgAlg.setOnCheckedChangeListener(onCheckedChangeListener);

        tvDesc = (TextView) findViewById(R.id.tv_desc);

        if(deviceEngine.getDeviceInfo().getModel().equalsIgnoreCase("N86")){
            pinpadPort = 101;
        }else{
            pinpadPort = 0;
        }
    }

    RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.Internal_Reader:
                    emvHandler2.initReader(ReaderTypeEnum.INNER, 0);
                    externalReader = false;
                    break;

                case R.id.External_Reader:
                    //if external contactless reader, N86 with base port is 101, N5 with base port is 0
                    emvHandler2.initReader(ReaderTypeEnum.OUTER, pinpadPort);
                    externalReader = true;
                    break;

                default:
                    break;
            }
        }

    };

    private void initEmvAid() {
        emvHandler2.delAllAid();
        if(emvHandler2.getAidListNum() <= 0){
            List<AidEntity> aidEntityList = emvUtils.getAidList();
            if (aidEntityList == null) {
                Log.d("nexgo", "initAID failed");
                return;
            }

            int i = emvHandler2.setAidParaList(aidEntityList);
            Log.d("nexgo", "setAidParaList " + i);
            showMessage("setAidParaList: " + (i == SdkResult.Success ? "success" : "ret:" + i));
        }else{
            Log.d("nexgo", "setAidParaList " + "already load aid");
        }

    }



    private void initEmvCapk() {
        emvHandler2.delAllCapk();
        int capk_num = emvHandler2.getCapkListNum();
        Log.d("nexgo", "capk_num " + capk_num);
        if(capk_num <= 0){
            List<CapkEntity> capkEntityList = emvUtils.getCapkList();
            if (capkEntityList == null) {
                Log.d("nexgo", "initCAPK failed");
                return;
            }
            int j = emvHandler2.setCAPKList(capkEntityList);
            Log.d("nexgo", "setCAPKList " + j);
            showMessage("setCAPKList: " + (j == SdkResult.Success ? "success" : "ret:" + j));

        }else{
            Log.d("nexgo", "setCAPKList " + "already load capk");
        }

    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_aid:
                initEmvAid();
                break;
            case R.id.add_capk:
                initEmvCapk();
                break;

            case R.id.drl_paywave:
                //set DRL for paypave , test cases :T5_12_01_DRL T5_12_02 T5_12_04 T5_12_05
                setPaywaveDrl();
                break;

            case R.id.drl_express:
                //set expresspay DRL
                setExpressPayDrl();
                break;

            case R.id.start_emv:
                inputAmount();
                break;
            case R.id.cancel_emv:
                emvHandler2.emvProcessCancel();
                break;

            case R.id.checkaid:
                int aidNum = emvHandler2.getAidListNum();
                int capkNum = emvHandler2.getCapkListNum();
                Log.d("nexgo", "getAidListNum: " + aidNum);
                Log.d("nexgo", "getCapkListNum: " + capkNum);
                showMessage("aid num: " + aidNum + "\ncapk num: " + capkNum);

                break;

            case R.id.checkaiddetail:
                Toast.makeText(EmvActivity2.this, "Please check Logcat", Toast.LENGTH_SHORT).show();
                List<AidEntity> aidEntities = emvHandler2.getAidList();
                if(aidEntities == null){
                    Log.d("nexgo", "AID = null");
                }else{
                    for(AidEntity aidEntity:aidEntities){
                        Log.d("nexgo", "AID = " + new Gson().toJson(aidEntity));
                    }
                }

                List<CapkEntity> capkEntities = emvHandler2.getCapkList();
                if(capkEntities == null){
                    Log.d("nexgo", "capkEntities = null");
                }else{
                    for(CapkEntity capkEntity:capkEntities){
                        Log.d("nexgo", "CAPK = " + new Gson().toJson(capkEntity));
                    }
                }

                showMessage("Check details: Please check logcat");
                break;
        }
    }

    private void startEmvTest() {
        CardReader cardReader = deviceEngine.getCardReader();
        HashSet<CardSlotTypeEnum> slotTypes = new HashSet<>();
        slotTypes.add(CardSlotTypeEnum.ICC1);
        slotTypes.add(CardSlotTypeEnum.RF);
        if(externalReader){
            cardReader.setSearchReader(ReaderTypeEnum.OUTER);
        }else{
            cardReader.setSearchReader(ReaderTypeEnum.INNER);
        }
        cardReader.searchCard(slotTypes, 60, this);
        Toast.makeText(EmvActivity2.this, "please insert or tap card", Toast.LENGTH_SHORT).show();
    }

    String amount ;
    private void inputAmount(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvDesc.setText("");
                final EditText et = new EditText(mContext);
                et.setInputType(InputType.TYPE_CLASS_NUMBER);
                new AlertDialog.Builder(mContext).setTitle("Input Amount")
                        .setView(et)

                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String input = et.getText().toString();
                                if ("".equals(input) || !TextUtils.isDigitsOnly(input)) {
                                    Toast.makeText(getApplicationContext(), "Format Error" + input, Toast.LENGTH_LONG).show();
                                    return ;
                                } else {
                                    amount = leftPad(input, 12, '0');
                                    Log.d("nexgo", "amount = " + amount);
                                    startEmvTest();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        })
                        .show();
            }
        });
    }

    private CardSlotTypeEnum mExistSlot;
    private boolean flag = true;


    @Override
    public void onCardInfo(int retCode, CardInfoEntity cardInfo) {
        if (retCode == SdkResult.Success && cardInfo != null) {
            mExistSlot = cardInfo.getCardExistslot();
            EmvTransConfigurationEntity transData = new EmvTransConfigurationEntity();
            transData.setTransAmount(amount);
//            transData.setCashbackAmount("000000000100"); //if support cashback amount
            transData.setEmvTransType((byte) 0x00); //0x00-sale, 0x20-refund,0x09-sale with cashback
            transData.setCountryCode("840");    //CountryCode
            transData.setCurrencyCode("840");    //CurrencyCode, 840 indicate USD dollar
            transData.setTermId("00000001");
            transData.setMerId("000000000000001");
            transData.setTransDate(new SimpleDateFormat("yyMMdd", Locale.getDefault()).format(new Date()));
            transData.setTransTime(new SimpleDateFormat("hhmmss", Locale.getDefault()).format(new Date()));
            transData.setTraceNo("00000000");

            transData.setEmvProcessFlowEnum(EmvProcessFlowEnum.EMV_PROCESS_FLOW_STANDARD);
            if (cardInfo.getCardExistslot() == CardSlotTypeEnum.RF) {
                transData.setEmvEntryModeEnum(EmvEntryModeEnum.EMV_ENTRY_MODE_CONTACTLESS);
            } else {
                transData.setEmvEntryModeEnum(EmvEntryModeEnum.EMV_ENTRY_MODE_CONTACT);
            }


            if(isExpressPaySeePhoneTapCardAgain){
                AmexTransDataEntity amexTransDataEntity = new AmexTransDataEntity();
                amexTransDataEntity.setExpressPaySeePhoneTapCardAgain(true);
            }

            //for UPI
            UnionPayTransDataEntity unionPayTransDataEntity = new UnionPayTransDataEntity();
            unionPayTransDataEntity.setQpbocForGlobal(true);
            unionPayTransDataEntity.setSupportCDCVM(true);
            //if support QPS, please enable below lines
            //unionPayTransDataEntity.setSupportContactlessQps(true);
            //unionPayTransDataEntity.setContactlessQpsLimit("000000030000");
            transData.setUnionPayTransDataEntity(unionPayTransDataEntity);

            emvHandler2.contactlessAppendAidIntoKernel(EmvCardBrandEnum.EMV_CARD_BRAND_MASTER, (byte)0x08, ByteUtils.hexString2ByteArray("A000000732100123"));

            Log.d("nexgo", "start emv " );
            emvHandler2.emvProcess(transData, this);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(EmvActivity2.this,"search card failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setExpressPayDrl(){
        DynamicReaderLimitEntity defaultDynamicReaderLimitEntity = new DynamicReaderLimitEntity();
        defaultDynamicReaderLimitEntity.setAppProgID(new byte[]{(byte) 0xFF});
        defaultDynamicReaderLimitEntity.setReaderCVMReqLimit(ByteUtils.hexString2ByteArray("000000001000"));
        defaultDynamicReaderLimitEntity.setReaderContactlessTransLimit(ByteUtils.hexString2ByteArray("000000001500"));
        defaultDynamicReaderLimitEntity.setReaderContactlessFloorLimit(ByteUtils.hexString2ByteArray("000000001200"));

        List<DynamicReaderLimitEntity> dynamicReaderLimitEntities = new ArrayList<>();
        DynamicReaderLimitEntity dynamicReaderLimitEntity = new DynamicReaderLimitEntity();
        dynamicReaderLimitEntity.setAppProgID(new byte[]{(byte) 0x06});
        dynamicReaderLimitEntity.setReaderCVMReqLimit(ByteUtils.hexString2ByteArray("000000000200"));
        dynamicReaderLimitEntity.setReaderContactlessTransLimit(ByteUtils.hexString2ByteArray("000000000700"));
        dynamicReaderLimitEntity.setReaderContactlessFloorLimit(ByteUtils.hexString2ByteArray("000000000400"));
        dynamicReaderLimitEntities.add(dynamicReaderLimitEntity);

        dynamicReaderLimitEntity = new DynamicReaderLimitEntity();
        dynamicReaderLimitEntity.setAppProgID(new byte[]{(byte) 0x0B});
        dynamicReaderLimitEntity.setReaderCVMReqLimit(ByteUtils.hexString2ByteArray("000000000200"));
        dynamicReaderLimitEntity.setReaderContactlessTransLimit(ByteUtils.hexString2ByteArray("000000000300"));
        dynamicReaderLimitEntity.setReaderContactlessFloorLimit(ByteUtils.hexString2ByteArray("000000000100"));
        dynamicReaderLimitEntities.add(dynamicReaderLimitEntity);

        int ret = emvHandler2.setDynamicReaderLimitListForExpressPay(defaultDynamicReaderLimitEntity, dynamicReaderLimitEntities);
        showMessage("setDynamicReaderLimitListForExpressPay: " + (ret == SdkResult.Success ? "success" : "ret:" + ret));

    }


    private void setPaywaveDrl() {
        List<DynamicReaderLimitEntity> dynamicReaderLimitEntity = new ArrayList<>();

        DynamicReaderLimitEntity entity = new DynamicReaderLimitEntity();
        entity.setDrlSupport(true);
        entity.setAppProgID(new byte[]{0x31, 0x02, 0x68, 0x26, 0x20});//get from 9f5a
        entity.setAuthOfZeroCheck(true);
        entity.setStatusCheck(false);
        entity.setReaderCVMReqLimitCheck(true);
        entity.setReaderContactlessFloorLimitCheck(true);
        entity.setReaderContactlessTransLimitCheck(false);
        entity.setReaderCVMReqLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x50, 0x01});
        entity.setReaderContactlessFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x50, 0x00});
        entity.setReaderContactlessTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x60, 0x01});
        dynamicReaderLimitEntity.add(entity);

        DynamicReaderLimitEntity entity1 = new DynamicReaderLimitEntity();
        entity1.setDrlSupport(true);
        entity1.setAppProgID(new byte[]{0x31, 0x02, 0x68, 0x26, 0x12, 0x00,0x00,0x03});//get from 9f5a
        entity1.setStatusCheck(false);
        entity1.setAuthOfZeroCheck(true);
        entity1.setReaderCVMReqLimitCheck(true);
        entity1.setReaderContactlessFloorLimitCheck(true);
        entity1.setReaderContactlessTransLimitCheck(false);
        entity1.setReaderCVMReqLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x50, 0x01});
        entity1.setReaderContactlessFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x50, 0x00});
        entity1.setReaderContactlessTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x60, 0x01});
        dynamicReaderLimitEntity.add(entity1);

        DynamicReaderLimitEntity entity2 = new DynamicReaderLimitEntity();
        entity2.setDrlSupport(true);
        entity2.setAppProgID(new byte[]{0x31, 0x02, 0x68, 0x26, 0x12});//get from 9f5a
        entity2.setAuthOfZeroCheck(true);
        entity2.setStatusCheck(false);
        entity2.setReaderCVMReqLimitCheck(true);
        entity2.setReaderContactlessFloorLimitCheck(true);
        entity2.setReaderContactlessTransLimitCheck(false);
        entity2.setReaderCVMReqLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x50, 0x01});
        entity2.setReaderContactlessFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x50, 0x00});
        entity2.setReaderContactlessTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x60, 0x01});
        dynamicReaderLimitEntity.add(entity2);

        DynamicReaderLimitEntity entity3 = new DynamicReaderLimitEntity();
        entity3.setDrlSupport(true);
        entity3.setAppProgID(new byte[]{0x31, 0x02, 0x68, 0x26,0x00});//get from 9f5a
        entity3.setAuthOfZeroCheck(true);
        entity3.setStatusCheck(false);
        entity3.setReaderCVMReqLimitCheck(true);
        entity3.setReaderContactlessFloorLimitCheck(true);
        entity3.setReaderContactlessTransLimitCheck(false);
        entity3.setReaderCVMReqLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x50, 0x01});
        entity3.setReaderContactlessFloorLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x50, 0x00});
        entity3.setReaderContactlessTransLimit(new byte[]{0x00, 0x00, 0x00, 0x00, 0x60, 0x01});
        dynamicReaderLimitEntity.add(entity3);

        int ret = emvHandler2.setDynamicReaderLimitListForPaywave(dynamicReaderLimitEntity);
        showMessage("setDynamicReaderLimitListForPaywave: " + (ret == SdkResult.Success ? "success" : "ret:" + ret));

    }

    @Override
    public void onSwipeIncorrect() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EmvActivity2.this, "please search card again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMultipleCards() {
        //cardReader.stopSearch(); //before next search card, please stopSearch first

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EmvActivity2.this, "please tap one card", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String leftPad(String str, int size, char padChar) {
        StringBuilder padded = new StringBuilder(str == null ? "" : str);
        while (padded.length() < size) {
            padded.insert(0, padChar);
        }
        return padded.toString();
    }



    @Override
    public void onSelApp(final List<String> appNameList, List<CandidateAppInfoEntity> appInfoList, boolean isFirstSelect) {
        Log.d("nexgo", "onAfterFinalSelectedApp->");

//        int k = 0;
//        List<String> tlv;
//        List<String> subTlv;
//        for (k = 0; k < appInfoList.size(); k++) {
//            Log.d("nexgo", "appInfoList getAid " + k + ":" + ByteUtils.byteArray2HexString(appInfoList.get(k).getAid()));
//            Log.d("nexgo", "appInfoList getAppLabel " + k + ":" +ByteUtils.byteArray2HexString(appInfoList.get(k).getAppLabel()));
//            Log.d("nexgo", "appInfoList getRfu " + k + ":" + ByteUtils.byteArray2HexString(appInfoList.get(k).getRfu()));
//            if(appInfoList.get(k).getRfu()!= null){
//                tlv = TlvUtils.decodingTLV(ByteUtils.byteArray2HexString(appInfoList.get(k).getRfu()), "73");
//                if (!tlv.isEmpty()) {
//                    subTlv = TlvUtils.decodingTLV(tlv.get(0), "42");
//                    if(!subTlv.isEmpty()){
//                        Log.d("nexgo", "appInfoList 42 " + k + ":" +subTlv);
//                    }
//                    subTlv = TlvUtils.decodingTLV(tlv.get(0), "5F55");
//                    if(!subTlv.isEmpty()){
//                        Log.d("nexgo", "appInfoList 5F55 " + k + ":" +subTlv);
//                    }
//                }
//            }
//        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View dv = getLayoutInflater().inflate(R.layout.dialog_app_list, null);
                final AlertDialog alertDialog = new AlertDialog.Builder(mContext).setView(dv).create();
                ListView lv = (ListView) dv.findViewById(R.id.aidlistView);
                List<Map<String, String>> listItem = new ArrayList<>();
                for (int i = 0; i < appNameList.size(); i++) {
                    Map<String, String> map = new HashMap<>();
                    map.put("appIdx", (i + 1) + "");
                    map.put("appName", appNameList.get(i));
                    listItem.add(map);
                }
                SimpleAdapter adapter = new SimpleAdapter(mContext,
                        listItem,
                        R.layout.app_list_item,
                        new String[]{"appIdx", "appName"},
                        new int[]{R.id.tv_appIndex, R.id.tv_appName});
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        emvHandler2.onSetSelAppResponse(position + 1);
                        alertDialog.dismiss();
                        alertDialog.cancel();
                    }
                });
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });
    }




    private void configPaywaveParameters(){
//        byte[] TTQ ;
//        byte[] kernelTTQ = emvHandler2.getTlv(ByteUtils.hexString2ByteArray("9F66"), EmvDataSourceEnum.FROM_KERNEL);
//        Log.d("nexgo",  "configPaywaveParameters, TTQ" + ByteUtils.byteArray2HexString(kernelTTQ));
//        //default TTQ value
//        TTQ = ByteUtils.hexString2ByteArray("36004000");
//        kernelTTQ[0] = TTQ[0];
//        kernelTTQ[2] = TTQ[2];
//        kernelTTQ[3] = TTQ[3];
//
//        // FIXME: 2019/3/20
//        //If there is no special requirements, do not change TTQ byte1
//        //if online force required , can set byte2 bit 8 = 1
//        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F66"), kernelTTQ);
    }


    private void configPaypassParameter(byte[] aid){
        //kernel configuration, enable RRP and cdcvm
        emvHandler2.setTlv(new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x1B}, new byte[]{(byte) 0x30});

        //EMV MODE :amount >contactless cvm limit, set 60 = online pin and signature
        emvHandler2.setTlv(new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x18}, new byte[]{(byte) 0x60});
        //EMV mode :amount < contactless cvm limit, set 08 = no cvm
        emvHandler2.setTlv(new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x19}, new byte[]{(byte) 0x08});

        emvHandler2.setTlv(new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x25}, ByteUtils.hexString2ByteArray("000999999999"));

        if (ByteUtils.byteArray2HexString(aid).toUpperCase().contains("A0000000043060")) {
            Log.d("nexgo",  "======maestro===== ");
            //maestro only support online pin
            emvHandler2.setTlv(new byte[]{(byte) 0x9F, (byte) 0x33}, new byte[]{(byte) 0xE0, (byte) 0x40, (byte) 0xC8});
            emvHandler2.setTlv(new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x18}, new byte[]{(byte) 0x40});
            emvHandler2.setTlv(new byte[]{(byte) 0xDF, (byte) 0x81, (byte) 0x19}, new byte[]{(byte) 0x08});

            //set 9F1D terminal risk management - Maestro. it should be same with the MTIP configuration for 9F1D
            emvHandler2.setTlv(new byte[]{(byte) 0x9F, (byte) 0x1d}, ByteUtils.hexString2ByteArray("4C00800000000000"));
        }else{
            //set 9F1D terminal risk management - MasterCard. it should be same with the MTIP configuration for 9F1D
            emvHandler2.setTlv(new byte[]{(byte) 0x9F, (byte) 0x1d}, ByteUtils.hexString2ByteArray("6C00800000000000"));
        }


    }

    private void configExpressPayParameter(){
        //set terminal capability...
        byte[] TTC ;
        byte[] kernelTTC = emvHandler2.getTlv(ByteUtils.hexString2ByteArray("9F6E"), EmvDataSourceEnum.FROM_KERNEL);

        TTC = ByteUtils.hexString2ByteArray("D8C00000");
        kernelTTC[1] = TTC[1];

        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F6E"), kernelTTC);

//
//        //TacDefault
//        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF8120"), ByteUtils.hexString2ByteArray("fc50b8a000"));
//
//        //TacDecline
//        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF8121"), ByteUtils.hexString2ByteArray("0000000000"));
//
//        //TacOnline
//        emvHandler2.setTlv(ByteUtils.hexString2ByteArray("DF8122"), ByteUtils.hexString2ByteArray("fc50808800"));
    }

    private void configPureContactlessParameter(){
        Log.d("nexgo",  "configPureContactlessParameter" );
//        emvHandler2.setPureKernelCapab(ByteUtils.hexString2ByteArray("3400400A99"));

    }

    private void configJcbContactlessParameter(){
        Log.d("nexgo",  "configJcbContactlessParameter" );


        //emvHandler2.setTlv(ByteUtils.hexString2ByteArray("9F52"), ByteUtils.hexString2ByteArray("03"));
    }

    @Override
    public void onTransInitBeforeGPO() {
        Log.d("nexgo",  "onTransInitBeforeGPO" );
        byte[] aid = emvHandler2.getTlv(new byte[]{0x4F}, EmvDataSourceEnum.FROM_KERNEL);

        if (mExistSlot == CardSlotTypeEnum.RF) {
            if (aid != null) {
                if(ByteUtils.byteArray2HexString(aid).toUpperCase().contains("A000000004")){
                    //Paypass
                    configPaypassParameter(aid);
                }else if(ByteUtils.byteArray2HexString(aid).toUpperCase().contains("A000000003")){
                    //Paywave
//                    configPaywaveParameters();
                }else if(ByteUtils.byteArray2HexString(aid).toUpperCase().contains("A000000025")){
                    //ExpressPay
//                    configExpressPayParameter();
                }else if(ByteUtils.byteArray2HexString(aid).toUpperCase().contains("A000000541")){
                    //configPureContactlessParameter();
                }else if(ByteUtils.byteArray2HexString(aid).toUpperCase().contains("A000000065")){
                    configJcbContactlessParameter();
                }
            }
        }else{
            //contact terminal capability ; if different card brand(depend on aid) have different terminal capability
//            if(ByteUtils.byteArray2HexString(aid).toUpperCase().contains("A000000004")){
//                emvHandler2.setTlv(new byte[]{(byte)0x9F,(byte)0x33}, new byte[]{(byte)0xE0,(byte)0xF8,(byte)0xC8});
//                emvHandler2.setTlv(new byte[]{(byte)0x9F,(byte)0x1D}, ByteUtils.hexString2ByteArray("6C00800000000000"));//terminal risk

//            }
        }

        emvHandler2.onSetTransInitBeforeGPOResponse(true);
    }

    @Override
    public void onContactlessTapCardAgain() {
        Log.d("nexgo", "onReadCardAgain");

        //this method only used for EMV contactless card if the host response the script. Such as paywave , AMEX...

        //for paywave, onOnlineProc-->onSetOnlineProcResponse->onContactlessTapCardAgain--> search contactless card ->onReadCardAgainResponse->onFinish

//        emvHandler.onSetReadCardAgainResponse(true);
    }





    @Override
    public void onConfirmCardNo(final CardInfoEntity cardInfo) {
        Log.d("nexgo",  "onConfirmCardNo" );
        Log.d("nexgo",  "onConfirmCardNo" + cardInfo.getTk2() );
        Log.d("nexgo",  "onConfirmCardNo" + cardInfo.getCardNo() );



        if(mExistSlot == CardSlotTypeEnum.RF ){
            cardNo = cardInfo.getCardNo();
            emvHandler2.onSetConfirmCardNoResponse(true);
            return ;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cardNo = cardInfo.getCardNo();
                final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                        .setTitle("Please Confirm Card Number")
                        .setMessage(cardNo)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                emvHandler2.onSetConfirmCardNoResponse(true);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                emvHandler2.onSetConfirmCardNoResponse(false);
                            }
                        })
                        .create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();

            }
        });
    }

    @Override
    public void onCardHolderInputPin(final boolean isOnlinePin, int leftTimes) {
        Log.d("nexgo",  "onCardHolderInputPin isOnlinePin = " + isOnlinePin);
        Log.d("nexgo",  "onCardHolderInputPin leftTimes = " + leftTimes);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showInputPin(isOnlinePin);
            }
        });
    }


    @Override
    public void onRemoveCard() {
        Log.d("nexgo",  "onRemoveCard" );

        emvHandler2.onSetRemoveCardResponse();
    }


    @Override
    public void onPrompt(PromptEnum promptEnum) {
        Log.d("nexgo",  "onPrompt->" + promptEnum);
        emvHandler2.onSetPromptResponse(true);
    }


    @Override
    public void onOnlineProc() {
        Log.d("nexgo", "onOnlineProc");

        Log.d("nexgo", "getEmvContactlessMode:" + emvHandler2.getEmvContactlessMode());
        Log.d("nexgo", "getcardinfo:" + new Gson().toJson(emvHandler2.getEmvCardDataInfo()));
        Log.d("nexgo", "getEmvCvmResult:" + emvHandler2.getEmvCvmResult());
        Log.d("nexgo", "getSignNeed--" + emvHandler2.getSignNeed());

        byte[] tlv_5A = emvHandler2.getTlv(new byte[]{(byte) 0x5A}, EmvDataSourceEnum.FROM_KERNEL);
        Log.d("nexgo", "tlv_5A--" + ByteUtils.byteArray2HexString(tlv_5A));

        byte[] tlv_95 = emvHandler2.getTlv(new byte[]{(byte) 0x95}, EmvDataSourceEnum.FROM_KERNEL);
        Log.d("nexgo", "tlv_95--" + ByteUtils.byteArray2HexString(tlv_95));


        byte[] tlv_84 = emvHandler2.getTlv(new byte[]{(byte) 0x84}, EmvDataSourceEnum.FROM_KERNEL);
        Log.d("nexgo", "tlv_84--" + ByteUtils.byteArray2HexString(tlv_84));

        byte[] tlv_50 = emvHandler2.getTlv(new byte[]{(byte) 0x50}, EmvDataSourceEnum.FROM_KERNEL);
        Log.d("nexgo", "tlv_50--" + ByteUtils.byteArray2HexString(tlv_50));

        EmvOnlineResultEntity emvOnlineResult = new EmvOnlineResultEntity();
        emvOnlineResult.setAuthCode("123450");
        emvOnlineResult.setRejCode("00");
        //fill with the host response 55 field EMV data to do second auth, the format should be TLV format.
        // for example: 910870741219600860008a023030710axxxxxx7210xxx  91 = tag, 08 = len, 7074121960086000 = value;
        // 8a = tag, 02 = len, 3030 = value
        emvOnlineResult.setRecvField55(null);
        emvHandler2.onSetOnlineProcResponse(SdkResult.Success, emvOnlineResult);

    }
    private boolean isExpressPaySeePhoneTapCardAgain = false;
    @Override
    public void onFinish(final int retCode, EmvProcessResultEntity entity) {
        Log.d("nexgo", "onFinish" + "retCode :" + retCode);

        boolean flag = false;
        byte[] aid = emvHandler2.getTlv(new byte[]{0x4F}, EmvDataSourceEnum.FROM_KERNEL);
        if(aid != null){
            if(mExistSlot == CardSlotTypeEnum.RF){
                if(ByteUtils.byteArray2HexString(aid).toUpperCase().contains("A000000025")){
                    if(retCode == SdkResult.Emv_Plz_See_Phone){
                        isExpressPaySeePhoneTapCardAgain = true;
                        flag = true;
                    }
                }
            }
        }
        if(!flag){
            isExpressPaySeePhoneTapCardAgain = false;
        }

        //get CVM result
        EmvCvmResultEnum emvCvmResultEnum =  emvHandler2.getEmvCvmResult();
        Log.d("nexgo", "getEmvCvmResult:" + emvCvmResultEnum);

        Log.d("nexgo", "emvHandler2.getSignNeed()--" + emvHandler2.getSignNeed());

        //get card number, track 2 data...etc
        Log.d("nexgo", "getEmvCardDataInfo:" + new Gson().toJson(emvHandler2.getEmvCardDataInfo()));


        byte[] tlv_5A = emvHandler2.getTlv(new byte[]{(byte) 0x5A}, EmvDataSourceEnum.FROM_KERNEL);
        Log.d("nexgo", "tlv_5A--" + ByteUtils.byteArray2HexString(tlv_5A));

        byte[] tlv_95 = emvHandler2.getTlv(new byte[]{(byte) 0x95}, EmvDataSourceEnum.FROM_KERNEL);
        Log.d("nexgo", "tlv_95--" + ByteUtils.byteArray2HexString(tlv_95));


        byte[] tlv_84 = emvHandler2.getTlv(new byte[]{(byte) 0x84}, EmvDataSourceEnum.FROM_KERNEL);
        Log.d("nexgo", "tlv_84--" + ByteUtils.byteArray2HexString(tlv_84));

        byte[] tlv_50 = emvHandler2.getTlv(new byte[]{(byte) 0x50}, EmvDataSourceEnum.FROM_KERNEL);
        Log.d("nexgo", "tlv_50--" + ByteUtils.byteArray2HexString(tlv_50));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EmvActivity2.this, retCode + "", Toast.LENGTH_SHORT).show();
            }
        });

        switch (retCode){
            case SdkResult.Emv_Success_Arpc_Fail:
            case SdkResult.Success:
            case SdkResult.Emv_Script_Fail:
                //online approve
                break;

            case SdkResult.Emv_Qpboc_Offline:// EMV Contactless: Offline Approval
            case SdkResult.Emv_Offline_Accept://EMV Contact: Offline Approval
                //offline approve
                break;

            //this retcode is Abolished
            case SdkResult.Emv_Qpboc_Online://EMV Contactless: Online Process for union pay
                //union pay online contactless--application should go online
                break;

            case SdkResult.Emv_Candidatelist_Empty:// Application have no aid list
            case SdkResult.Emv_FallBack://  FallBack ,chip card reset failed
                //fallback process
                break;

            case SdkResult.Emv_Arpc_Fail: //
            case SdkResult.Emv_Declined:
                //online decline ,if it is in second gac, application should decide if it is need reversal the transaction
                break;

            case SdkResult.Emv_Cancel:// Transaction Cancel
                //user cancel
                break;

            case SdkResult.Emv_Offline_Declined: //
                //offline decline
                break;

            case SdkResult.Emv_Card_Block: //Card Block
                //card is blocked
                break;

            case SdkResult.Emv_App_Block: // Application Block
                //card application block
                break;

            case SdkResult.Emv_App_Ineffect:
                //card not active
                break;

            case SdkResult.Emv_App_Expired:
                //card Expired
                break;

            case SdkResult.Emv_Other_Interface:
                //try other entry mode, like contact or mag-stripe
                break;

            case SdkResult.Emv_Plz_See_Phone:
                //see phone flow
                //prompt a dialog to user to check phone-->search contactless card(another card) -->start new emvProcess again
                break;

            case SdkResult.Emv_Terminate:
                //transaction terminate
                break;

            default:
                //other error
                break;
        }

        String[] tags = {"9f26", "9f27", "9f10", "9f37", "9f36", "95", "9a", "9c", "9f02", "5f2a", "82", "9f1a", "9f03","9f33", "9f34", "9f35", "9f1e", "9f09", "84", "9f41"};
        String Data = emvHandler2.getTlvByTags(tags);
        showMessage("onFinish\n" + "retCode: "+ retCode +"\n" + Data);

    }

    @Override
    public void onInputResult(final int retCode, final byte[] data) {
        Log.d("nexgo", "onInputResult->:" + ByteUtils.byteArray2HexStringWithSpace(data));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!externalReader){
                    pwdAlertDialog.dismiss();
                }

                if (retCode == SdkResult.Success || retCode == SdkResult.PinPad_No_Pin_Input
                        || retCode == SdkResult.PinPad_Input_Cancel) {
                    if (data != null) {
                        byte[] temp = new byte[8];
                        System.arraycopy(data, 0, temp, 0, 8);

                    }
                    emvHandler2.onSetPinInputResponse(retCode != SdkResult.PinPad_Input_Cancel, retCode == SdkResult.PinPad_No_Pin_Input);
                } else {
                    Toast.makeText(EmvActivity2.this,"pin enter failed", Toast.LENGTH_SHORT).show();
                    emvHandler2.onSetPinInputResponse(false, false);
                }
            }
        });
    }

    @Override
    public void onSendKey(final byte keyCode) {
        if(!externalReader){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (keyCode == PinPadKeyCode.KEYCODE_CLEAR) {
                        pwdText = "";
                    } else {
                        pwdText += "* ";
                    }
                    pwdTv.setText(pwdText);
                }
            });
        }
    }

    private void showInputPin(boolean isOnlinePin) {

        if(!externalReader){
            pwdText = "";
            pwdAlertDialog.show();
            pwdTv.setText(pwdText);
            PinPad pinPad = deviceEngine.getPinPad();
            pinPad.initPinPad(PinPadTypeEnum.INTERNAL);
            pinPad.setPinKeyboardMode(PinKeyboardModeEnum.FIXED);
            if (isOnlinePin) {
                if(cardNo == null){
                    cardNo = emvHandler2.getEmvCardDataInfo().getCardNo();
                }
                pinPad.inputOnlinePin(new int[]{0x00, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c},
                        60, cardNo.getBytes(), 0, PinAlgorithmModeEnum.ISO9564FMT0, this);
            } else {
                pinPad.inputOfflinePin(new int[]{0x00, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c},
                        60, this);
            }
        }else{
            ExtPinPad extPinPad = deviceEngine.getExtPinPad();
            Log.d("nexgo",  "onCardHolderInputPin port = " + pinpadPort);
            //if external reader, N86 with base port is 101, N5 with base port is 0
            extPinPad.initExtPinPad(pinpadPort, null);
            if(isOnlinePin){
                if(cardNo == null){
                    cardNo = emvHandler2.getEmvCardDataInfo().getCardNo();
                }
                extPinPad.inputPin(0, ExtPinPadKeyAlgTypeEnum.DES, PinAlgorithmModeEnum.ISO9564FMT0, 4, 12, cardNo, 60, new OnExtPinPadInputPinListener() {
                    @Override
                    public void onInputPinResult(int retCode, byte[] data) {
                        LogUtils.debug("dukptInputPin retCode:{} pinBlock:{}", retCode, ByteUtils.byteArray2HexString(data));
                        if (retCode == SdkResult.Success || retCode == SdkResult.PinPad_No_Pin_Input
                                || retCode == SdkResult.PinPad_Input_Cancel) {
                            if (data != null) {
                                byte[] temp = new byte[8];
                                System.arraycopy(data, 0, temp, 0, 8);

                            }
                            emvHandler2.onSetPinInputResponse(retCode != SdkResult.PinPad_Input_Cancel, retCode == SdkResult.PinPad_No_Pin_Input);
                        } else {
                            emvHandler2.onSetPinInputResponse(false, false);
                        }
                    }
                });

            }else{
                extPinPad.inputOfflinePin(4, 12,
                        60, this);
            }


        }

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
