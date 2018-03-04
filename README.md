# androidthings-digitalDisplay
Android Things samples from Sysolve IoT Open Source Project

Android Things Simple PIO扩展案例 - 数码管显示
====

这是一个Android Things Simple PIO的扩展案例，使用入门开发配件包中的以下配件：
1. 1位数码管
2. 1KΩ电阻 8个  （1%电阻-蓝色，色环：棕黑黑棕棕）
3. 10KΩ电阻 1个 （为方便初学者区分，配件包中的10KΩ电阻为5%电阻-土黄色，色环：棕黑红金；早期发货的10KΩ电阻为1%电阻-蓝色，色环：棕黑黑红棕）
4. 透明热缩管
5. 按键
6. 104电容

![实物效果](https://github.com/sysolve/androidthings-digitalDisplay/blob/master/photo.png)
![面包板接线图](https://github.com/sysolve/androidthings-digitalDisplay/blob/master/digitalDisplay_Sketch.png)

目前树莓派、IMX6UL_PICO、IMX7D_PICO三种开发板，在扩展接口的定义和名称上有所差别，端口功能基本一致，名称有所不同。
我已汇总如下，代码中com.sysolve.androidthings.utils.BoardSpec根据运行的设备会自动选择端口配置：
![三种开发板的端口配置](https://github.com/sysolve/androidthings-digitalDisplay/blob/master/port_define.png)

数码管针脚，先串接1K电阻后，再按图中标注的针脚号接入开发板。

为方便使用不同开发板的开发者，可直接通过 PIN 脚编号获取GPIO名称：
```Java
String gpioName = BoardSpec.getInstance().getGpioPin(BoardSpec.PIN_29);
```

为方便调试，AndroidManifest.xml文件中的IOT_LAUNCHER项已注释，开机不会自动运行。如要开机自动运行，请自行将注释去掉。
```html
<!--
    <category android:name="android.intent.category.IOT_LAUNCHER" />
-->
```

4位数码管显示
====
FourDigitalActivity.java即为控制4位数码管显示的程序，运行前需要将AndroidManifest.xml中FourDigitalActivity的启动设置注释取消，并将MainActivity的启动设置注销掉。

![运行FourDigitalActivity](https://github.com/sysolve/androidthings-digitalDisplay/blob/master/4digital_run.png)

说明文档参见：

![接线方式](https://github.com/sysolve/androidthings-digitalDisplay/blob/master/4digital.png)

![连接效果](https://github.com/sysolve/androidthings-digitalDisplay/blob/master/4digital_show.png)

各位数字的各段依次显示的效果
![接线方式](https://github.com/sysolve/androidthings-digitalDisplay/blob/master/4digital1.webp)

0~9的数字，依次在数码管的各位上显示
![连接效果](https://github.com/sysolve/androidthings-digitalDisplay/blob/master/4digita2.webp)

数码管显示4位数字
![连接效果](https://github.com/sysolve/androidthings-digitalDisplay/blob/master/4digita3.webp)
