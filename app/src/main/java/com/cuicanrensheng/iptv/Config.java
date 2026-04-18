public class Config {

    // 你指定的远程PHP源
    public static final String ONLINE_URL = "https://gitee.com/qf_1111/iptv/raw/master/playlist%20.m3u";

    // APP内置本地源（默认启动用这个）
    public static final String LOCAL_URL = "file:///android_asset/channel.txt";

    // 启动默认加载：本地源
    public static final String DEFAULT_URL = LOCAL_URL;
}
