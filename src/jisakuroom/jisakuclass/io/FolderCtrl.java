package jisakuroom.jisakuclass.io;

import java.io.File;
import java.io.FilenameFilter;

public class FolderCtrl {
	public static void MakeFolder(String path){
		File fout;
		fout = new File(path);
		fout.mkdirs();
	}
	public static boolean IsFolderExists(String path){
		File fout;
		fout = new File(path);
		return fout.exists();
	}
	public static File[] GetFileList(String path,String extention){
		File fout;
		File[] files;
		fout = new File(path);
		files = fout.listFiles(getFileExtensionFilter(extention));
		return files;
	}
	public static FilenameFilter getFileExtensionFilter(String extension) {  
        final String _extension = extension;  
        return new FilenameFilter() {  
            public boolean accept(File file, String name) {  
                boolean ret = name.endsWith(_extension);   
                return ret;  
            }  
        };  
    }  
}
