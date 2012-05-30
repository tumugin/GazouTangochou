package jisakuroom.gazoutangochou;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import jisakuroom.jisakuclass.http.download;
import jisakuroom.jisakuclass.io.FileCtrl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class tango_dl extends Activity{
	ListView listView;
	ArrayList<FileDLItem> list;
	Handler handler= new Handler();
	ProgressDialog Loaddialog;
	String currentPath;
	String enc;
	//アイテム
	public class FileDLItem{
		public String Title = "";
		public String Description = "";
		public String URL = null;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tango_dl);
		Intent it = new Intent();
		it.putExtra("IsDownload", true);
		setResult(RESULT_OK, it);
		//エンコード設定取得
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		enc = sharedPreferences.getString("encode", "SJIS");
		//ListView取得
		listView = (ListView) findViewById(R.id.listView1);
		listView.setOnItemClickListener(new OnItemClick());
		list = new ArrayList<FileDLItem>();
		//ダイアログ初期化
		Loaddialog = new ProgressDialog(this);
        Loaddialog.setMessage("読み込み中...");
        //Loaddialog.setCancelable(false);
        Loaddialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		Loaddialog.show();
		//ディレクトリー取得
        String status = Environment.getExternalStorageState();//外部ストレージ状況
        if (status.equals(Environment.MEDIA_MOUNTED)){//SD挿入済み
        	//SDカードディレクトリに設定
        	currentPath = Environment.getExternalStorageDirectory().getPath() + "/" + "GazouTangochou" + "/";
        }else{
        	//内蔵メモリーに設定
        	currentPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName() + "/";
        }
        //ファイルリスト取得
		new Thread(new Runnable() {
			@Override
			public void run() {
				getItemList();//リストを取得
				handler.post(new SetListbox());//リストボックスをセット
				Loaddialog.dismiss();
			}
		}).start();
	}
	//メニュー生成
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem1 = menu.add(Menu.NONE, 0, Menu.NONE, "リストを更新").setIcon(android.R.drawable.ic_popup_sync);
    	//MenuItem menuItem2 = menu.add(Menu.NONE, 1, Menu.NONE, "単語帳を公開").setIcon(android.R.drawable.ic_menu_share);
        int ver = Build.VERSION.SDK_INT;
        if(ver >= 11){
        	menuItem1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        	//menuItem2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
		return super.onCreateOptionsMenu(menu);
	}
	//メニューイベント
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == 0){
			Loaddialog.show();
			//ファイルリスト取得
			new Thread(new Runnable() {
				@Override
				public void run() {
					getItemList();//リストを取得
					handler.post(new SetListbox());//リストボックスをセット
					Loaddialog.dismiss();
				}
			}).start();
		}
		return true;
	}
	//アイテム取得
	public void getItemList(){
		try {
			list.clear();
			String jsonString = download.downloadString("http://tango.jisakuroom.net/list.json");
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray jsonArray = jsonObject.getJSONArray("DataList");
			for (int i = 0; i < jsonArray.length(); i++) {
				FileDLItem fileDLItem = new FileDLItem();
				JSONObject data = jsonArray.getJSONObject(i);
				fileDLItem.Title = data.getString("Title");
				fileDLItem.Description = data.getString("Description");
				fileDLItem.URL = data.getString("URL");
				list.add(fileDLItem);
			}
		} catch (final Exception e) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Loaddialog.dismiss();
					StackTraceElement[] stackTraceElements = e.getStackTrace();
					String err = "";
					for(StackTraceElement stackTraceElement : stackTraceElements){
						err = err + stackTraceElement.toString() + "\n";
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(tango_dl.this);
					builder.setTitle("処理エラー発生");
					builder.setMessage("データーリストの取得時にエラーが発生しました\n\n内部スタックトレース:\n" + err + "\nエラーメッセージ:\n" + e.getMessage());
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
					builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							finish();
						}
					});
					builder.show();
				}
			});
		}
	}
	public class OnItemClick implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> adapter, View view, final int position, long id) {
			final FileDLItem item = list.get(position);
			AlertDialog.Builder dialog = new AlertDialog.Builder(tango_dl.this);
			dialog.setTitle("単語帳のダウンロード");
			dialog.setMessage("単語帳(" + item.Title + ")をダウンロードしますか？\n\nタイトル:" + item.Title +
					"\n説明:" + item.Description);
			dialog.setPositiveButton("ダウンロード", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//ダウンロード処理
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								handler.post(new Runnable() {
									public void run() {
										Loaddialog.setMessage("単語帳をダウンロードしています...");
										Loaddialog.show();
									}
								});
								//ダウンロード処理
								String data = download.downloadString(item.URL);
								FileCtrl.saveString(currentPath + item.Title + ".csv", data, enc);
								handler.post(new Runnable() {
									public void run() {
										Loaddialog.dismiss();
										Loaddialog.setMessage("読み込み中...");
										Toast.makeText(tango_dl.this, "ダウンロードしました", Toast.LENGTH_LONG).show();
									}
								});
							} catch (final Exception e) {
								handler.post(new Runnable() {
									@Override
									public void run() {
										Loaddialog.dismiss();
										Loaddialog.setMessage("読み込み中...");
										StackTraceElement[] stackTraceElements = e.getStackTrace();
										String err = "";
										for(StackTraceElement stackTraceElement : stackTraceElements){
											err = err + stackTraceElement.toString() + "\n";
										}
										AlertDialog.Builder builder = new AlertDialog.Builder(tango_dl.this);
										builder.setTitle("処理エラー発生");
										builder.setMessage("単語帳のダウンロード時にエラーが発生しました\n\n内部スタックトレース:\n" + err + "\nエラーメッセージ:\n" + e.getMessage());
										builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												finish();
											}
										});
										builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
											@Override
											public void onCancel(DialogInterface dialog) {
												finish();
											}
										});
										builder.show();
									}
								});
							}
						}
					}).start();
				}
			});
			dialog.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			dialog.show();
		}
	}
	//ListViewカスタマイズ
	@SuppressWarnings("rawtypes")
	public class FileListAdapter extends ArrayAdapter {  
		private ArrayList items;  
		private LayoutInflater inflater;
		
		@SuppressWarnings("unchecked")
		public FileListAdapter(Context context, int textViewResourceId,ArrayList items) {  
			super(context, textViewResourceId, items);  
			this.items = items;  
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
		}  
		@Override  
		public View getView(int position, View convertView, ViewGroup parent) {  
			// ビューを受け取る  
			View view = convertView;  
			if (view == null) {  
				// 受け取ったビューがnullなら新しくビューを生成  
				view = inflater.inflate(R.layout.tango_dl_listview, null);  
			}
			// 表示すべきデータの取得  
			FileDLItem item = (FileDLItem)items.get(position); 
			TextView title = (TextView) view.findViewById(R.id.textView1);
			title.setText(item.Title);
			TextView description = (TextView) view.findViewById(R.id.textView2);
			description.setText(item.Description);
			return view;  
		}  
	}
	//ListViewにアダプターをセット
	public class SetListbox implements Runnable{
		@Override
		public void run() {
			FileListAdapter fileListAdapter = new FileListAdapter(tango_dl.this, R.layout.tango_dl_listview, list);
			listView.setAdapter(fileListAdapter);
		}
	}
}
