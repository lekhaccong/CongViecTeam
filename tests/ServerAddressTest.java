import vn.lekhaccong.congviecteam.ServerAddress;
public class ServerAddressTest {
 public static void main(String[] args){
  String[] good={"http://192.168.100.111:8080","http://10.0.0.1:8080","http://172.16.0.1","https://example.com"};
  for(String s:good)if(!s.equals(ServerAddress.normalize(s)))throw new AssertionError(s);
  if(!ServerAddress.normalize(" 192.168.1.2:8080/ ").equals("http://192.168.1.2:8080"))throw new AssertionError();
  String[] bad={"http://8.8.8.8","http://localhost:8080","http://127.0.0.1","http://172.32.0.1","http://192.168.1.999","http://192.168.01.2","https://a.com@evil.com","javascript:alert(1)","file:///etc/passwd","http://192.168.1.1/path","http://192.168.1.1?x=1","http://192.168.1.1:99999"};
  for(String s:bad){boolean rejected=false;try{ServerAddress.normalize(s);}catch(IllegalArgumentException e){rejected=true;}if(!rejected)throw new AssertionError(s);}
  if(!ServerAddress.sameOrigin("https://example.com","https://example.com:443/api"))throw new AssertionError();
  if(ServerAddress.sameOrigin(good[0],"http://evil.com/api"))throw new AssertionError();
  if(ServerAddress.sameOrigin(good[0],"http://192.168.100.111:8081/api"))throw new AssertionError();
  System.out.println("PASS: URL normalization and same-origin checks");
 }
}
