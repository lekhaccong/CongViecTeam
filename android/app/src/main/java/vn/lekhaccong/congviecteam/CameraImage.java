package vn.lekhaccong.congviecteam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import java.io.*;

/** Produces a report-sized JPEG and applies the camera's EXIF orientation. */
public final class CameraImage {
    private CameraImage() {}
    public static File prepare(File source) throws IOException {
        BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(source.getPath(),options);
        if(options.outWidth<=0 || options.outHeight<=0)throw new IOException("Ảnh trống hoặc không hợp lệ");
        options.inSampleSize=1;
        while(Math.max(options.outWidth,options.outHeight)/options.inSampleSize>2400)options.inSampleSize*=2;
        options.inJustDecodeBounds=false;
        Bitmap bitmap=BitmapFactory.decodeFile(source.getPath(),options);
        if(bitmap==null)throw new IOException("Không đọc được ảnh");
        File output=null;
        try{
            float scale=Math.min(1f,1600f/Math.max(bitmap.getWidth(),bitmap.getHeight()));
            if(scale<1){Bitmap smaller=Bitmap.createScaledBitmap(bitmap,Math.max(1,Math.round(bitmap.getWidth()*scale)),Math.max(1,Math.round(bitmap.getHeight()*scale)),true);if(smaller!=bitmap)bitmap.recycle();bitmap=smaller;}
            int orientation=new ExifInterface(source.getPath()).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix=new Matrix();
            switch(orientation){
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:matrix.setScale(-1,1);break;
                case ExifInterface.ORIENTATION_ROTATE_180:matrix.setRotate(180);break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:matrix.setScale(1,-1);break;
                case ExifInterface.ORIENTATION_TRANSPOSE:matrix.setRotate(90);matrix.postScale(-1,1);break;
                case ExifInterface.ORIENTATION_ROTATE_90:matrix.setRotate(90);break;
                case ExifInterface.ORIENTATION_TRANSVERSE:matrix.setRotate(-90);matrix.postScale(-1,1);break;
                case ExifInterface.ORIENTATION_ROTATE_270:matrix.setRotate(-90);break;
            }
            if(!matrix.isIdentity()){Bitmap rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);if(rotated!=bitmap)bitmap.recycle();bitmap=rotated;}
            output=File.createTempFile("report-", ".jpg",source.getParentFile());
            for(int quality=85;quality>=25;quality-=15){
                try(OutputStream out=new FileOutputStream(output)){if(!bitmap.compress(Bitmap.CompressFormat.JPEG,quality,out))throw new IOException("Không nén được ảnh");}
                if(output.length()<=2*1024*1024)return output;
            }
            throw new IOException("Ảnh vượt quá 2 MB");
        }catch(IOException|RuntimeException e){if(output!=null)output.delete();throw e;}
        finally{bitmap.recycle();}
    }
}
