package jisakuroom.jisakuclass.http;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoogleImageSearchAPI {
	public static List<ImageItem> SearchImage(String keyword,String size,boolean faceOnly) throws Exception{
		String EncodedKeyword = URLEncoder.encode(keyword, "UTF-8");//UTF8でキーワードをエンコード
		String faceSearchString = "";
		if(faceOnly == true){
			faceSearchString = "&imgtype=face";
		}
		String jsonString = download.downloadString("http://ajax.googleapis.com/ajax/services/search/images?hl=ja&v=1.0&rsz=large&imgsz=" + size + "&q=" + EncodedKeyword + faceSearchString);//ダウンロード
		JSONObject jsonObject = new JSONObject(jsonString);//JSONをパース
		JSONObject responseData = jsonObject.getJSONObject("responseData");//responseDataを取得
		JSONArray imagelistArray = responseData.getJSONArray("results");//results Array取得
		List<ImageItem> imageItems = new ArrayList<ImageItem>();//ImageItemのListを生成
		//For文でひたすら読み込む!!
		for (int i = 0; i < imagelistArray.length(); i++) {
			JSONObject jsonObjectFor = imagelistArray.getJSONObject(i);
			ImageItem imageItem = new ImageItem();
			imageItem.SetImageUrl(jsonObjectFor.getString("url"));
			imageItem.SetImageTitle(jsonObjectFor.getString("contentNoFormatting"));
			imageItems.add(imageItem);
		}
		//For文完了!!後はReturnするだけ～
		return imageItems;//Returnしたぞ～
	}
}
