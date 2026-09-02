package vn.lekhaccong.congviecteam;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.net.Uri;
import android.graphics.Color;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.*;

public class MainActivity extends Activity {
    private WebView web;
    private LinearLayout root;
    private TextView status;
    private String base="", pendingUrl, pendingMethod, pendingCookie;
    private ValueCallback<Uri[]> upload;
    private boolean saving=false, pageFailed=false;
    private static final int PICK=10, SAVE=11;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(242,247,246));
        root.setOnApplyWindowInsetsListener((v,insets)->{
            v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        setContentView(root);
        base=getPreferences(0).getString("server", "");
        if(base.isEmpty()) setup(); else open();
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    private TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setPadding(dp(16),dp(12),dp(16),dp(12));return t;}
    private Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->r.run());return b;}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private void destroyWeb(){if(web!=null){web.stopLoading();web.destroy();web=null;}}
    private void setup(){
        destroyWeb(); root.removeAllViews();
        root.addView(text("CongViecTeam",28));
        root.addView(text("Kết nối hệ thống giao việc của bạn",18));
        root.addView(text("Nhập địa chỉ điện thoại cùng Wi-Fi được in trong cửa sổ máy chủ. Máy tính cần bật khi sử dụng.",16));
        EditText input=new EditText(this);input.setSingleLine(true);input.setText(base);input.setHint("http://192.168.1.10:8080");input.setInputType(17);root.addView(input);
        root.addView(button("Lưu và kết nối",()->{
            try {
                String next=ServerAddress.normalize(input.getText().toString());
                if(!next.equals(base)) CookieManager.getInstance().removeAllCookies(null);
                base=next;getPreferences(0).edit().putString("server",base).apply();open();
            }catch(Exception e){input.setError(e.getMessage());}
        }));
        root.addView(text("Lần đầu: tạo quản trị trên máy tính tại localhost:8080. Sau đó đăng nhập tài khoản được cấp trong app.",16));
    }
    @android.annotation.SuppressLint("SetJavaScriptEnabled")
    private void open(){
        root.removeAllViews();destroyWeb();
        LinearLayout bar=new LinearLayout(this);
        TextView title=text("CongViecTeam",18);bar.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        bar.addView(button("Máy chủ",()->new AlertDialog.Builder(this).setMessage("Rời trang để đổi máy chủ? Báo cáo chưa gửi sẽ không được lưu.").setPositiveButton("Tiếp tục",(d,w)->setup()).setNegativeButton("Ở lại",null).show()));
        bar.addView(button("Tải lại",()->new AlertDialog.Builder(this).setMessage("Tải lại trang? Nội dung chưa gửi sẽ bị mất.").setPositiveButton("Tải lại",(d,w)->web.loadUrl(base)).setNegativeButton("Hủy",null).show()));root.addView(bar);
        status=text("Đang kết nối…",12);root.addView(status);
        web=new WebView(this);root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){
                String target=r.getUrl().toString();
                if(target.equals("congviecteam://backup") && r.isForMainFrame() && ServerAddress.sameOrigin(base,v.getUrl())){
                    save(base+"/api/backup","POST","congviec-backup-"+System.currentTimeMillis()+".sqlite");return true;
                }
                if(!ServerAddress.sameOrigin(base,target)){toast("Liên kết ngoài máy chủ đã bị chặn.");return true;}return false;
            }
            @Override public void onPageStarted(WebView v,String url,android.graphics.Bitmap icon){pageFailed=false;}
            @Override public void onPageFinished(WebView v,String url){
                if(!ServerAddress.sameOrigin(base,url))return;
                if(!pageFailed)status.setText("Máy chủ: "+base);
                CookieManager.getInstance().flush();
                // The server creates backups through POST and then a browser blob URL.
                // Use a native document picker for this action, with the same login cookie.
                v.evaluateJavascript("if(!window.teamBackup){window.teamBackup=true;document.addEventListener('click',function(e){if(e.target.closest('#backup')){e.preventDefault();e.stopImmediatePropagation();location.href='congviecteam://backup';}},true);}",null);
            }
            @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){if(r.isForMainFrame()){pageFailed=true;status.setText("Không kết nối được. Kiểm tra Wi-Fi, địa chỉ và máy chủ, rồi bấm Tải lại.");}}
            @Override public void onReceivedHttpError(WebView v,WebResourceRequest r,WebResourceResponse e){if(r.isForMainFrame()){pageFailed=true;status.setText("Máy chủ trả lỗi "+e.getStatusCode()+". Thử tải lại.");}}
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> cb,FileChooserParams p){
                if(upload!=null)upload.onReceiveValue(null);upload=cb;
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,p.getMode()==FileChooserParams.MODE_OPEN_MULTIPLE);
                try{startActivityForResult(i,PICK);}catch(ActivityNotFoundException e){upload.onReceiveValue(null);upload=null;toast("Không mở được bộ chọn tệp.");}return true;
            }
        });
        web.setDownloadListener((url,ua,disposition,mime,length)->{
            if(ServerAddress.sameOrigin(base,url))save(url,"GET",URLUtil.guessFileName(url,disposition,mime));
            else toast("Không hỗ trợ tải liên kết này. Hãy dùng nút Sao lưu trong app.");
        });
        web.loadUrl(base);
    }
    private void save(String url,String method,String name){
        if(saving || pendingUrl!=null){toast("Đang xử lý một tệp, vui lòng chờ.");return;}
        pendingUrl=url;pendingMethod=method;pendingCookie=CookieManager.getInstance().getCookie(url);
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/octet-stream");i.putExtra(Intent.EXTRA_TITLE,name);
        try{startActivityForResult(i,SAVE);}catch(ActivityNotFoundException e){pendingUrl=null;toast("Không mở được nơi lưu tệp.");}
    }
    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
        if(req==PICK && upload!=null){upload.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));upload=null;}
        if(req==SAVE){
            String url=pendingUrl, method=pendingMethod, cookie=pendingCookie;pendingUrl=null;pendingCookie=null;
            if(result!=RESULT_OK || data==null || data.getData()==null || url==null)return;
            Uri dest=data.getData();saving=true;toast("Đang tải và lưu tệp…");
            new Thread(()->{
                HttpURLConnection c=null;
                try{
                    c=(HttpURLConnection)new URL(url).openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(15000);c.setReadTimeout(60000);c.setRequestMethod(method);
                    if(cookie!=null)c.setRequestProperty("Cookie",cookie);
                    if(method.equals("POST")){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");try(OutputStream o=c.getOutputStream()){o.write("{}".getBytes(java.nio.charset.StandardCharsets.UTF_8));}}
                    if(c.getResponseCode()!=200)throw new IOException("Máy chủ trả mã "+c.getResponseCode()+". Kiểm tra đăng nhập và quyền quản trị.");
                    try(InputStream in=c.getInputStream();OutputStream out=getContentResolver().openOutputStream(dest,"wt")){
                        if(out==null)throw new IOException("Không mở được tệp đích");byte[] buf=new byte[32768];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
                    }
                    runOnUiThread(()->toast("Đã lưu tệp tại nơi bạn chọn."));
                }catch(Exception e){
                    try{android.provider.DocumentsContract.deleteDocument(getContentResolver(),dest);}catch(Exception ignored){}
                    runOnUiThread(()->toast("Lưu thất bại: "+e.getMessage()));
                }finally{if(c!=null)c.disconnect();runOnUiThread(()->saving=false);}
            }).start();
        }
    }
    @Override public void onBackPressed(){if(web!=null && web.canGoBack())web.goBack();else super.onBackPressed();}
    @Override protected void onPause(){super.onPause();CookieManager.getInstance().flush();}
    @Override protected void onDestroy(){if(upload!=null)upload.onReceiveValue(null);destroyWeb();super.onDestroy();}
}
