#ReleaseNote
v1.0.1

1. 实现独立画笔
2. 修改一处小问题会导致滑动时产生误差
3. 修改创建IndexBar逻辑,解决某种情况下宽高为0的bug
4. 支持横屏

1.0.1版本,每一处都使用独立的画笔绘制,可以对控件进行比较自由的设置，同时支持横屏。不过需要注意的是,当屏幕高度不够时,indexbarFontSize属性将会失效，字体大小将会自适应高度。