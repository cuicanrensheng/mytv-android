public class Config {

    // 你指定的远程IPTV源（保留原链接，包含空格）
    public static final String ONLINE_URL = "https://gitee.com/qf_1111/iptv/raw/master/playlist%20.m3u";

    // APP内置本地源（保留，备用）
    public static final String LOCAL_URL = "file:///android_asset/channel.txt";

    // 关键：默认启动直接加载远程源
    public static final String DEFAULT_URL = ONLINE_URL;
}
