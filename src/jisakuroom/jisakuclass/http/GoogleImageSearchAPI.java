package jisakuroom.jisakuclass.http;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoogleImageSearchAPI {
	public static List<ImageItem> SearchImage(String keyword,String size,boolean faceOnly) throws Exception{
		String EncodedKeyword = URLEncoder.encode(keyword, "UTF-8");//UTF8�ŃL�[���[�h���G���R�[�h
		String faceSearchString = "";
		if(faceOnly == true){
			faceSearchString = "&imgtype=face";
		}
		String jsonString = download.downloadString("http://ajax.googleapis.com/ajax/services/search/images?hl=ja&v=1.0&rsz=large&imgsz=" + size + "&q=" + EncodedKeyword + faceSearchString);//�_�E�����[�h
		JSONObject jsonObject = new JSONObject(jsonString);//JSON���p�[�X
		JSONObject responseData = jsonObject.getJSONObject("responseData");//responseData���擾
		JSONArray imagelistArray = responseData.getJSONArray("results");//results Array�擾
		List<ImageItem> imageItems = new ArrayList<ImageItem>();//ImageItem��List�𐶐�
		//For���łЂ�����ǂݍ���!!
		for (int i = 0; i < imagelistArray.length(); i++) {
			JSONObject jsonObjectFor = imagelistArray.getJSONObject(i);
			ImageItem imageItem = new ImageItem();
			imageItem.SetImageUrl(jsonObjectFor.getString("url"));
			imageItem.SetImageTitle(jsonObjectFor.getString("contentNoFormatting"));
			imageItems.add(imageItem);
		}
		//For������!!���Return���邾���`
		return imageItems;//Return�������`
	}
}
