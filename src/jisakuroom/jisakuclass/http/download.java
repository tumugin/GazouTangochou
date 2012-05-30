package jisakuroom.jisakuclass.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class download {
	//�摜�_�E�����[�h�p�֐�
	public static Bitmap downloadImage(String imgURL) throws Exception{
		URL url;
		url = new URL(imgURL);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
    	http.setRequestMethod("GET");
    	http.setConnectTimeout(10000);//10�b�Ń^�C���A�E�g
    	//http.addRequestProperty("REFERER",imgURL);//���t�@���[�U��
    	http.connect();
		
    	InputStream input= http.getInputStream();
    	Bitmap bitmap= BitmapFactory.decodeStream(input);
    	return bitmap;
	}
	//�����m�F�@�\�t���摜�_�E�����[�h
	public static Bitmap downloadImageFromImageItemListWith404Check(List<ImageItem> list) throws Exception{
		for(ImageItem item : list){
			try {
				Log.d("gazou-tangochou", "Downloading image:" + item.GetImageUrl());
				Bitmap bitmap = downloadImage(item.GetImageUrl());
				Log.d("gazou-tangochou", "Downloading image OK!!:" + item.GetImageUrl());
				if(bitmap != null){
					return bitmap;
				}else {
					Log.e("gazou-tangochou", "Image is null:" + item.GetImageUrl());
				}
			} catch (Exception e) {
				Log.e("gazou-tangochou", "Error:" + item.GetImageUrl());
			}
		}
		throw new Exception("[�G���[]Google�摜����API���猟�������摜���X�g�̒��̉摜�̃_�E�����[�h�����ׂĎ����܂������A���ׂăA�N�Z�X�o���܂���ł���");
	}
	//�e�L�X�g�_�E�����[�h�p�֐�
	public static String downloadString(String urlStr) throws Exception{
		// URL���쐬����GET�ʐM���s��
    	URL url;
		url = new URL(urlStr);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
    	http.setRequestMethod("GET");
    	http.setConnectTimeout(30000);//30�b�Ń^�C���A�E�g
    	http.connect();

    	// �T�[�o�[����̃��X�|���X��W���o�͂֏o��
    	BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
    	String xml = "", line = "";
    	while((line = reader.readLine()) != null){
    		xml += line + "\n";
    	}
    	reader.close();
    	return xml;
		//return null;
	}
}
