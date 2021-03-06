/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package com.mrboss.m11sdk;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;

import org.apache.cordova.*;
import org.apache.cordova.engine.*;

import java.io.IOException;
import java.io.InputStream;

import android.os.AsyncTask;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Context;
import android.app.AlertDialog;

import java.io.*;

import com.posin.device.CashDrawer;
import com.posin.device.CustomerDisplay;
import com.posin.device.Printer;
import com.posin.device.SDK;

import java.util.Set;
import android.util.Base64;

public class m11sdk extends CordovaPlugin {
  private static final String LOG_TAG = "m11sdkPlugin";
  static CashDrawer mCashDrawer = null;
  static boolean isinit = false;
  static String mCustDspType = null;
  static String mLedPort = null;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    try {
      init();

      if ("Print".equals(action)) {
        String printtext = args.getString(0);
        Print(printtext);
        callbackContext.success(200);
        return true;
      } else if ("CustomerDisplay".equals(action)) {
        String showtext = args.getString(0);
        CustomerDisplay(showtext);
        callbackContext.success(200);
        return true;
      } else if ("OpenMoneyBox".equals(action)) {
        OpenMoneyBox();
        callbackContext.success(200);
        return true;
      } else if ("LedCustomerDisplay".equals(action)) {
        String showtext = args.getString(0);
        String showtype = args.getString(1);
        LedCustomerDisplay(showtext, showtype);
        callbackContext.success(200);
        return true;
      } else if ("TestPrint".equals(action)) {
        Print("hello world");
        callbackContext.success(200);
        return true;
      }
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
      return false;
    } catch (Throwable e1) {
      callbackContext.error(e1.getMessage());
      return false;
    }
    callbackContext.error("No This Method");
    return false;
  }

  public void init() throws Throwable {
    if (!isinit) {
      SDK.init(this.cordova.getActivity());

      // init compenent
      mCashDrawer = CashDrawer.newInstance();

      loadSystemProperties();
      
      isinit = true;
    }
  }

  public void Print(String printtext) throws Throwable {
    Printer prt = null;
    printtext = printtext + "\n";
    try {
      byte[] data = printtext.getBytes("GBK");

      prt = Printer.newInstance();
      if (!prt.ready()) {
      }
      prt.getOutputStream().write(data);
    } finally {
      if (prt != null) {
        prt.close();
      }
    }
  }

  public void CustomerDisplay(String showtext) throws Throwable {
    String[] lines = showtext.split("\\|\\|\\|");

    CustomerDisplay dsp = null;

    try {
      int count = lines.length;
      if (count > 4) {
        count = 4;
      }
      dsp = CustomerDisplay.newInstance();
      dsp.clear();
      for (int i = 0; i < count; i++) {
        dsp.setCursorPos(i, 0);
        dsp.write(lines[i]);
      }
    } finally {
      if (dsp != null)
        dsp.close();
    }
  }

  public void LedCustomerDisplay(String showtext, String showtype) throws Throwable {
    LedCustomerDisplay cd = null;
    try {
      cd = new LedCustomerDisplay(mLedPort);
      if("Clear".equals(showtype)){
        cd.clear();
      }
      else if("Price".equals(showtype)){
        cd.displayPrice(showtext);
      }
      else if("Total".equals(showtype)){
        cd.displayTotal(showtext);
      }
      else if("Payment".equals(showtype)){
        cd.displayPayment(showtext);
      }
      else if("Change".equals(showtype)){
        cd.displayChange(showtext);
      }
      else{
        cd.clear();
      }
    } finally {
      if (cd != null) {
        cd.close();
      }
    }
  }

  public void OpenMoneyBox() throws Throwable {
    mCashDrawer.kickOutPin2(100);
    //mCashDrawer.kickOutPin5(100);
  }

  private Properties loadSystemProperties() {
    Properties p = new Properties();
    FileInputStream is = null;

    try {
      is = new FileInputStream("/system/build.prop");
      p.load(is);
      mCustDspType = p.getProperty("ro.customerdisplay.type", "lcd");
      mLedPort = p.getProperty("ro.customerdisplay.port", "/dev/ttyACM0");
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return p;
  }

  private void Alert(String msg) {
    Dialog alertDialog = new AlertDialog.Builder(this.cordova.getActivity()).
    setTitle("对话框的标题").
    setMessage(msg).
    setCancelable(false).
    setNegativeButton("确定", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
      }
    }).
    create();
    alertDialog.show();
  }
}