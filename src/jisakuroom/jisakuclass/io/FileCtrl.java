package jisakuroom.jisakuclass.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.res.AssetManager;

public class FileCtrl {
	public static String loadStr(String fileName,String enc) throws Exception{
		FileInputStream is = new FileInputStream(fileName);
    	InputStreamReader in = new InputStreamReader(is, enc);
    	StringBuffer sb = new StringBuffer();
        int i;
        while((i = in.read()) != -1){
            sb.append((char)i);
        }
        return sb.toString();
	}
	public static String loadStrFromResource(AssetManager as,String enc,String fileName) throws Exception{
		InputStream is = as.open(fileName);
		InputStreamReader in = new InputStreamReader(is, enc);
    	StringBuffer sb = new StringBuffer();
        int i;
        while((i = in.read()) != -1){
            sb.append((char)i);
        }
        return sb.toString();
	}
	public static void saveString(String fileName, String str, String enc) throws Exception{
    	FileOutputStream os = new FileOutputStream(fileName);
    	OutputStreamWriter out = new OutputStreamWriter(os, enc);
    	out.write(str);
    	out.flush();
    	out.close();
	}
	public static void deleteFile(String fileName){
		File fout;
		fout = new File(fileName);
		fout.delete();
	}
	public static void changeFileName(String fileName,String toFileName){
		File fout;
		fout = new File(fileName);
		fout.renameTo(new File(toFileName));
	}
}
