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
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.sysolve.androidthings.utils.BoardSpec;

import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    Gpio[] digital = new Gpio[8];
    Gpio mButtonGpio = null;

    public static boolean[][] DIGITAL_DISPLAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManagerService service = new PeripheralManagerService();

        try {
            /*Define the digital segments GPIO like:
              --2--
            1|     |3
              --0--
            4|     |6
              --5--  .7
             */
            digital[0] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_37));
            digital[1] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_35));
            digital[2] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_31));
            digital[3] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_29));

            digital[4] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_38));
            digital[5] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_36));
            digital[6] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_18));
            digital[7] = service.openGpio(BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_16));

            //set the digital segments DIRECTION_OUT and INITIALLY_LOW
            for (Gpio g:digital) {
                g.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            }

            //first test the digital segments
            testDigitalSegment();

            //we will define the digitals which segments ON
            //eg. 8: all segments ON, 1: [3],[6] ON
            DIGITAL_DISPLAY = new boolean[10][];
            DIGITAL_DISPLAY[0] = new boolean[] { false,  true,  true,  true,  true,  true,  true,  false  };
            DIGITAL_DISPLAY[1] = new boolean[] { false, false, false,  true, false, false,  true,  false  };
            DIGITAL_DISPLAY[2] = new boolean[] {  true, false,  true,  true,  true,  true, false,  false  };
            DIGITAL_DISPLAY[3] = new boolean[] {  true, false,  true,  true, false,  true,  true,  false  };
            DIGITAL_DISPLAY[4] = new boolean[] {  true,  true, false,  true, false, false,  true,  false  };
            DIGITAL_DISPLAY[5] = new boolean[] {  true,  true,  true, false, false,  true,  true,  false  };
            DIGITAL_DISPLAY[6] = new boolean[] {  true,  true,  true, false,  true,  true,  true,  false  };
            DIGITAL_DISPLAY[7] = new boolean[] { false, false,  true,  true, false, false,  true,  false  };
            DIGITAL_DISPLAY[8] = new boolean[] {  true,  true,  true,  true,  true,  true,  true,  false  };
            DIGITAL_DISPLAY[9] = new boolean[] {  true,  true,  true,  true, false,  true,  true,  false  };

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //wait for the DigitalSegment testing
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //display from 0 to 9
                    for (int i = -1;i<10;++i) {
                        displayDigital(i);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

            //define a button for counter
            mButtonGpio = service.openGpio(BoardSpec.getGoogleSampleButtonGpioPin());
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButtonGpio.registerGpioCallback(new GpioCallback() {
                @Override
                public boolean onGpioEdge(Gpio gpio) {
                    buttonPressedTimes++;
                    Log.i(TAG, "GPIO changed, button pressed "+ buttonPressedTimes);
                    displayDigital(buttonPressedTimes);

                    // Return true to continue listening to events
                    return true;
                }
            });

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    int buttonPressedTimes = 0;

    boolean testing = false;
    private void testDigitalSegment() {
        testing = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (testing) {
                    //let the segment ON
                    for (int i = 0; i < digital.length; ++i) {
                        if (!testing) break;
                        try {
                            digital[i].setValue(true);

                            Thread.sleep(500);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }

                    //let the segment OFF
                    for (int i = 0; i < digital.length; ++i) {
                        if (!testing) break;
                        try {
                            digital[i].setValue(false);

                            Thread.sleep(500);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                }
            }
        }).start();
    }

    public void displayDigital(int d) {
        testing = false;
        try {
            if (d<0) {
                //let All Segments OFF when d<0
                for (int i = 0; i < 8; ++i) {
                    digital[i].setValue(false);
                }
            } else {
                d = d % 10;
                //get the ON/OFF map for the digital
                boolean[] segments = DIGITAL_DISPLAY[d];

                //set digital segment ON/OFF
                for (int i = 0; i < 8; ++i) {
                    digital[i].setValue(segments[i]);
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

        if (mButtonGpio!=null) try {
            mButtonGpio.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

}
