package com.cuicanrensheng.iptv;

public class Config {

    // 远程直播源1（你指定的PHP源）
    public static final String LIVE_URL = "https://raw.githubusercontent.com/cuicanrensheng/IPTV/refs/heads/main/666aaa.php";

    // APP内置本地直播源
    public static final String LOCAL_LIVE_URL = "file:///android_asset/channel.txt";

    // 默认加载远程源
    public static final String DEFAULT_URL = LIVE_URL;
}
