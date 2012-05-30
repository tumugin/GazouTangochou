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
	//画像ダウンロード用関数
	public static Bitmap downloadImage(String imgURL) throws Exception{
		URL url;
		url = new URL(imgURL);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
    	http.setRequestMethod("GET");
    	http.setConnectTimeout(10000);//10秒でタイムアウト
    	//http.addRequestProperty("REFERER",imgURL);//リファラー偽装
    	http.connect();
		
    	InputStream input= http.getInputStream();
    	Bitmap bitmap= BitmapFactory.decodeStream(input);
    	return bitmap;
	}
	//生存確認機能付き画像ダウンロード
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
		throw new Exception("[エラー]Google画像検索APIから検索した画像リストの中の画像のダウンロードをすべて試しましたが、すべてアクセス出来ませんでした");
	}
	//テキストダウンロード用関数
	public static String downloadString(String urlStr) throws Exception{
		// URLを作成してGET通信を行う
    	URL url;
		url = new URL(urlStr);
		HttpURLConnection http = (HttpURLConnection)url.openConnection();
    	http.setRequestMethod("GET");
    	http.setConnectTimeout(30000);//30秒でタイムアウト
    	http.connect();

    	// サーバーからのレスポンスを標準出力へ出す
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
