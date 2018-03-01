/*
 * @author Ray, ray@sysolve.com
 * Copyright 2018, Sysolve IoT Open Source
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sysolve.androidthings.digitaldisplay;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.sysolve.androidthings.utils.BoardSpec;

import java.io.IOException;

public class FourDigitalActivity extends Activity {
    private static final String TAG = FourDigitalActivity.class.getSimpleName();

    Gpio[] digital = new Gpio[8];
    Gpio[] showDigital = new Gpio[4];

    public Handler handler = new Handler();

    public static boolean[][] DIGITAL_DISPLAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();

        try {
            /*
            定义数码管的各个显示段
            Define the digital segments GPIO like:
              --2--
            1|     |3
              --0--
            4|     |6
              --5--  .7
             */
            digital[0] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_22));
            digital[1] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_35));
            digital[2] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_31));
            digital[3] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_29));

            digital[4] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_13));
            digital[5] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_15));
            digital[6] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_36));
            digital[7] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_37));

            //定义数码管的4个数字是否显示
            showDigital[0] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_16));
            showDigital[1] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_18));
            showDigital[2] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_38));
            showDigital[3] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_40));

            //set the digital segments DIRECTION_OUT and INITIALLY_HIGH
            //设置各个段默认为高电平，即不显示
            for (Gpio g : digital) {
                g.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            }

            //设置各个数字默认为低电平，即不显示
            for (Gpio g : showDigital) {
                g.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //first test the digital segments
                    //首先，依次测试数码管的每段的显示
                    testDigitalSegment();

                    //we will define the digitals which segments ON
                    //eg. 8: all segments ON, 1: [3],[6] ON
                    DIGITAL_DISPLAY = new boolean[10][];
                    DIGITAL_DISPLAY[0] = new boolean[]{false, true, true, true, true, true, true, false};
                    DIGITAL_DISPLAY[1] = new boolean[]{false, false, false, true, false, false, true, false};
                    DIGITAL_DISPLAY[2] = new boolean[]{true, false, true, true, true, true, false, false};
                    DIGITAL_DISPLAY[3] = new boolean[]{true, false, true, true, false, true, true, false};
                    DIGITAL_DISPLAY[4] = new boolean[]{true, true, false, true, false, false, true, false};
                    DIGITAL_DISPLAY[5] = new boolean[]{true, true, true, false, false, true, true, false};
                    DIGITAL_DISPLAY[6] = new boolean[]{true, true, true, false, true, true, true, false};
                    DIGITAL_DISPLAY[7] = new boolean[]{false, false, true, true, false, false, true, false};
                    DIGITAL_DISPLAY[8] = new boolean[]{true, true, true, true, true, true, true, false};
                    DIGITAL_DISPLAY[9] = new boolean[]{true, true, true, true, false, true, true, false};

                    //依次在每位数码管上显示数字
                    testDisplayDigital();

                    //自动计数
                    autoIncNumber();
                    //将自动计数的数值显示在4位数码管上
                    display4Digitals();
                }
            }).start();


        } catch (Exception e) {

        }
    }

    boolean testing = false;

    private void testDigitalSegment() {
                testing = true;

                while (testing) {
                    for (int d = 0; d < showDigital.length; ++d) {
                        if (!testing) break;

                        //设置显示的数字
                        try {
                            showDigital[d].setValue(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //let the segment ON
                        for (int i = 0; i < digital.length; ++i) {
                            if (!testing) break;
                            try {
                                digital[i].setValue(false);

                                Thread.sleep(200);
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }

                        //let the segment OFF
                        for (int i = 0; i < digital.length; ++i) {
                            if (!testing) break;
                            try {
                                digital[i].setValue(true);

                                Thread.sleep(200);
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }

                        //关闭显示的数字
                        try {
                            showDigital[d].setValue(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    testing = false;
                }

    }

    private void testDisplayDigital() {
                //display from 0 to 9
                for (int i = -1; i < 10; ++i) {
                    //依次在每位数码管上显示
                    for (int d = 0; d < showDigital.length; ++d) {
                        displayDigital(i);

                        try {
                            showDigital[d].setValue(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        try {
                            showDigital[d].setValue(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
    }

    public void setDigitalForDisplay(int i) {
        digitalForDisplay[0] = i/1000;
        i = i%1000;
        digitalForDisplay[1] = i/100;
        i = i%100;
        digitalForDisplay[2] = i/10;
        i = i%10;
        digitalForDisplay[3] = i;
    }

    int number = 0;
    public int[] digitalForDisplay = new int[4];

    public void display4Digitals() {
                while (true) {      //持续刷新显示
                    //依次在每位数码管上显示
                    for (int d = 0; d < showDigital.length; ++d) {
                        //先设置显示的数码管段
                        displayDigital(digitalForDisplay[d]);

                        try {
                            //设置对应的数码管位开启显示
                            showDigital[d].setValue(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        try {
                            //设置对应的数码管位关闭显示
                            showDigital[d].setValue(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
    }

    public void autoIncNumber() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setDigitalForDisplay(number++);
                autoIncNumber();
            }
        }, 1000);
    }

    public void displayDigital(int d) {
        testing = false;
        try {
            if (d<0) {
                //let All Segments OFF when d<0
                for (int i = 0; i < 8; ++i) {
                    digital[i].setValue(true);
                }
            } else {
                d = d % 10;
                //get the ON/OFF map for the digital
                boolean[] segments = DIGITAL_DISPLAY[d];

                //set digital segment ON/OFF
                for (int i = 0; i < 8; ++i) {
                    //数码管显示段是低电平触发显示，所以要将segments[i]的值取反
                    digital[i].setValue(!segments[i]);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (digital!=null) {
            for (Gpio g:digital) {
                try {
                    if (g!=null) g.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        if (showDigital!=null) {
            for (Gpio g:showDigital) {
                try {
                    if (g!=null) g.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

}
