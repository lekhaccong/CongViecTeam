package vn.lekhaccong.congviecteam;
import java.net.URI;
public final class ServerAddress {
    public static String normalize(String value) {
        try {
            String s = value.trim();
            if (!s.contains("://")) s = "http://" + s;
            URI u = new URI(s);
            String host = u.getHost();
            if (host == null || u.getUserInfo() != null || u.getQuery() != null || u.getFragment() != null
                || !(u.getPath().isEmpty() || u.getPath().equals("/")) || u.getPort() == 0 || u.getPort() > 65535)
                throw new IllegalArgumentException();
            boolean https = "https".equals(u.getScheme());
            if (!https && !("http".equals(u.getScheme()) && privateIPv4(host))) throw new IllegalArgumentException();
            return u.getScheme() + "://" + host + (u.getPort() == -1 ? "" : ":" + u.getPort());
        } catch (Exception e) {
            throw new IllegalArgumentException("Nhập địa chỉ LAN dạng http://192.168.1.10:8080 hoặc máy chủ HTTPS.");
        }
    }
    static boolean privateIPv4(String host) {
        String[] p = host.split("\\.");
        if (p.length != 4) return false;
        int[] n = new int[4];
        try { for (int i=0;i<4;i++) { n[i]=Integer.parseInt(p[i]); if(n[i]<0||n[i]>255 || !Integer.toString(n[i]).equals(p[i])) return false; } }
        catch (Exception e) { return false; }
        return n[0]==10 || (n[0]==192 && n[1]==168) || (n[0]==172 && n[1]>=16 && n[1]<=31);
    }
    public static boolean sameOrigin(String base, String target) {
        try {
            URI a=new URI(base), b=new URI(target);
            return b.getUserInfo()==null && a.getScheme().equals(b.getScheme()) && a.getHost().equalsIgnoreCase(b.getHost()) && port(a)==port(b);
        } catch(Exception e) { return false; }
    }
    private static int port(URI u) { return u.getPort()<0 ? ("https".equals(u.getScheme())?443:80) : u.getPort(); }
}
