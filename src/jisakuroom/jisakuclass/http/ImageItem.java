package jisakuroom.jisakuclass.http;

public class ImageItem {
	String image_url = null;
	String image_title = null;
	
	public void SetImageUrl(String newurl){
		image_url = newurl;
	}
	public String GetImageUrl(){
		return image_url;
	}
	public void SetImageTitle(String newtitle){
		image_title = newtitle;
	}
	public String GetImageTitle(){
		return image_title;
	}
}
