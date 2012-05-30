package jisakuroom.gazoutangochou;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jisakuroom.jisakuclass.http.GoogleImageSearchAPI;
import jisakuroom.jisakuclass.http.ImageItem;
import jisakuroom.jisakuclass.http.download;
import jisakuroom.jisakuclass.io.FileCtrl;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import jp.ac.wakhok.tomoharu.csv.*;

public class mondai extends Activity {
	Button button1;
	Button button2;
	Button button3;
	ProgressBar progressBar;
	ImageView imageView;
	List<String> mondaiList = new ArrayList<String>();
	List<String> mondaiListShuffle = new ArrayList<String>();
	List<String> nowMondaiList = new ArrayList<String>();
	String nowSeikai = "";
	int nowMondai = 0;
	Handler handler= new Handler();
	int seikaiCount = 0;
	int fuseikaiCount = 0;
	LayoutInflater inflater;
	View maruView;
	View batuView;
	Bitmap Image;
	String enc;
	String image_size;
	String name;
	String toast_time;
	boolean face_only;
	boolean isShuffle;
	Thread thread;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.mondai);
        //ビュー初期化
        button1 = (Button) findViewById(R.id.mondai_button1);
        button1.setOnClickListener(new Button1_Click());
        button2 = (Button) findViewById(R.id.mondai_button2);
        button2.setOnClickListener(new Button2_Click());
        button3 = (Button) findViewById(R.id.mondai_button3);
        button3.setOnClickListener(new Button3_Click());
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        imageView = (ImageView) findViewById(R.id.imageView1);
        //マルバツToast用
        inflater = getLayoutInflater();
        maruView = inflater.inflate(R.layout.maru_toast,
                (ViewGroup) findViewById(R.id.maru_layout));
        batuView = inflater.inflate(R.layout.batu_toast,
                (ViewGroup) findViewById(R.id.batu_layout));
        //パス取得
        Intent intent = getIntent();
        String fileName = intent.getStringExtra("path");
        name = intent.getStringExtra("name");
        //シャッフルON OFF確認
        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        isShuffle = pref.getBoolean("isShuffle", false);
        //エンコード設定取得
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		enc = sharedPreferences.getString("encode", "SJIS");
		image_size = sharedPreferences.getString("image_size", "huge");
		face_only = sharedPreferences.getBoolean("face_only", true);
		toast_time = sharedPreferences.getString("toast_time", "short");
        //ファイル読み込み
        try {
        	String csvDataString = FileCtrl.loadStr(fileName, enc);
			String csvDataSplitString[] = csvDataString.split("\n",0);
			for (int i = 0; i < csvDataSplitString.length; i++) {
				CSVTokenizer csvTokenizer = new CSVTokenizer(csvDataSplitString[i]);
				if(csvTokenizer.hasMoreTokens() && csvDataSplitString[i].equals("") == false){
					String data = csvTokenizer.nextToken();
					mondaiList.add(data);
				}
			}
			//問題が0の場合は終了
			if(mondaiList.size() == 0){
				Toast.makeText(this, "問題数が0です。最低限3つ追加してください。", Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			if(mondaiList.size() <3){
				Toast.makeText(this, "問題数が足りません。最低限3つ必要です。", Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			if(isShuffle == true){
				Collections.shuffle(mondaiList);
			}
		} catch (Exception e) {
			StackTraceElement[] stackTraceElements = e.getStackTrace();
			String err = "";
			for(StackTraceElement stackTraceElement : stackTraceElements){
				err = err + stackTraceElement.toString() + "\n";
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(mondai.this);
			builder.setTitle("処理エラー発生");
			builder.setMessage("ファイルの読み込み時にエラーが発生しました\n\n内部スタックトレース:\n" + err + "\nエラーメッセージ:\n" + e.getMessage());
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
		//一問目を表示
		showMondai(0);
    }
    //終了時にメモリーを開放
    @Override
	protected void onDestroy() {
    	try {
			imageView.setImageBitmap(null);
			if(Image != null){
				Image.recycle();
			}
		} catch (Exception e) {
		}
    	super.onDestroy();
    }
    //問題を表示
    public void showMondai(final int id){
    	thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
		    		//ボタン無効化(UIスレッド)
					handler.post(new ButtonDisable());
		    		//現在の問題のリストをクリアー
		        	nowMondaiList.clear();
		        	//正解を取得
		        	String mondaiText = mondaiList.get(id);
		        	nowSeikai = mondaiText.toString();
		        	//シャッフル用のリストをクリアー
		        	mondaiListShuffle.clear();
		        	//クローン生成(現在の問題を追加しないようにするため)
		        	for (int i = 0; i < mondaiList.size(); i++) {
		    			if(mondaiText.equals(mondaiList.get(i)) == false){
		    				mondaiListShuffle.add(mondaiList.get(i));
		    			}
		    		}
		        	//シャッフル用リストをシャッフルする
		        	Collections.shuffle(mondaiListShuffle);
		        	//シャッフル用リストから上位2アイテム取得と追加+正解を追加
		        	nowMondaiList.add(mondaiText);
		        	nowMondaiList.add(mondaiListShuffle.get(0));
		        	nowMondaiList.add(mondaiListShuffle.get(1));
		        	//現在の問題をシャッフル
		        	Collections.shuffle(nowMondaiList);
		        	//画像リストを取得
		    		//リスト取得
					final List<ImageItem> imageItems = GoogleImageSearchAPI.SearchImage(mondaiText,image_size,face_only);
					if(imageItems.size() == 0){
						//一致する画像が一つも無い場合
						handler.post(new Runnable() {
							@Override
							public void run() {
								button1.setText(nowMondaiList.get(0));
								button2.setText(nowMondaiList.get(1));
								button3.setText(nowMondaiList.get(2));
								imageView.setImageResource(R.drawable.ic_launcher);
								Toast.makeText(mondai.this, "一致する画像が見つかりませんでした", Toast.LENGTH_LONG).show();
							}
						});
						//ボタン有効化(UIスレッド)
						handler.post(new ButtonEnable());
						return;
					}
					Collections.shuffle(imageItems);
					//画像を消す
					handler.post(new Runnable() {
						@Override
						public void run() {
							if(Image != null){
								imageView.setImageBitmap(null);
								Image.recycle();
							}
							//Image.recycle();
						}
					});
					Image = download.downloadImageFromImageItemListWith404Check(imageItems);
					//画像+テキストをセット(UIスレッド)
					handler.post(new Runnable() {
						@Override
						public void run() {
							button1.setText(nowMondaiList.get(0));
							button2.setText(nowMondaiList.get(1));
							button3.setText(nowMondaiList.get(2));
							imageView.setImageBitmap(Image);
							//Image.recycle();
						}
					});
					//ボタン有効化(UIスレッド)
					handler.post(new ButtonEnable());
					//開放
					//Image.recycle();
				} catch (final Exception e) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							StackTraceElement[] stackTraceElements = e.getStackTrace();
							String err = "";
							for(StackTraceElement stackTraceElement : stackTraceElements){
								err = err + stackTraceElement.toString() + "\n";
							}
							AlertDialog.Builder builder = new AlertDialog.Builder(mondai.this);
							builder.setTitle("処理エラー発生");
							builder.setMessage("画像の取得時にエラーが発生しました\n\n内部スタックトレース:\n" + err + "\nエラーメッセージ:\n" + e.getMessage());
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
					return;
				} catch(final OutOfMemoryError e){
					handler.post(new Runnable() {
						@Override
						public void run() {
							StackTraceElement[] stackTraceElements = e.getStackTrace();
							String err = "";
							for(StackTraceElement stackTraceElement : stackTraceElements){
								err = err + stackTraceElement.toString() + "\n";
							}
							AlertDialog.Builder builder = new AlertDialog.Builder(mondai.this);
							builder.setTitle("処理エラー発生");
							builder.setMessage("画像の取得時にエラーが発生しました\n\n内部スタックトレース:\n" + err + "\nエラーメッセージ:\n" + e.getMessage());
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
					return;
				}
			}
    	});
    	thread.start();
    }
    //メニュー生成
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューアイテムを追加します
    	MenuItem menuItem1 = menu.add(Menu.NONE, 0, Menu.NONE, "別の画像でチャレンジ").setIcon(android.R.drawable.ic_popup_sync);
    	MenuItem menuItem2 = menu.add(Menu.NONE, 1, Menu.NONE, "終了").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        int ver = Build.VERSION.SDK_INT;
        if(ver >= 11){
        	menuItem1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        	menuItem2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        return super.onCreateOptionsMenu(menu);
    }
    //メニュー選択
    // オプションメニューアイテムが選択された時に呼び出されます
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getItemId() == 0){
    		try {
				if(thread.isAlive() == true){
					thread.stop();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
    		Toast.makeText(this, "別の画像を取得しています....", Toast.LENGTH_LONG).show();
    		showMondai(nowMondai);
    	}else if (item.getItemId() == 1) {
			finish();
		}
		return true;
    }
    //ボタン無効化
    public class ButtonDisable implements Runnable{
		@Override
		public void run() {
			progressBar.setVisibility(View.VISIBLE);
			button1.setEnabled(false);
    		button2.setEnabled(false);
    		button3.setEnabled(false);
		}
    }
    //ボタン無効化
    public class ButtonEnable implements Runnable{
		@Override
		public void run() {
			progressBar.setVisibility(View.GONE);
			button1.setEnabled(true);
    		button2.setEnabled(true);
    		button3.setEnabled(true);
		}
    }
    public void ShowMaruToast(){
    	int lenth = Toast.LENGTH_SHORT;
    	if(toast_time.equals("short")){
    		lenth = Toast.LENGTH_SHORT;
    	}else {
    		lenth = Toast.LENGTH_LONG;
		}
    	TextView textView = (TextView) maruView.findViewById(R.id.toast_text);
    	textView.setText("正解は「" + nowSeikai + "」");
    	Toast toast = new Toast(getApplicationContext());
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.setDuration(lenth);
    	toast.setView(maruView);
    	toast.show();
    }
    public void ShowBatuToast(){
    	int lenth = Toast.LENGTH_SHORT;
    	if(toast_time.equals("short")){
    		lenth = Toast.LENGTH_SHORT;
    	}else {
    		lenth = Toast.LENGTH_LONG;
		}
    	TextView textView = (TextView) batuView.findViewById(R.id.toast_text);
    	textView.setText("正解は「" + nowSeikai + "」");
    	Toast toast = new Toast(getApplicationContext());
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.setDuration(lenth);
    	toast.setView(batuView);
    	toast.show();
    }
    public void ShowScoreDialog(){
    	//ビュー取得
    	View layout = inflater.inflate(R.layout.score_dlg,(ViewGroup) findViewById(R.id.score_dlg_layout));
    	TextView seikai_mon = (TextView) layout.findViewById(R.id.seikai_mon);
    	seikai_mon.setText(String.valueOf(seikaiCount)+ "問");
    	TextView fuseikai_mon = (TextView) layout.findViewById(R.id.fuseikai_mon);
    	fuseikai_mon.setText(String.valueOf(fuseikaiCount)+ "問");
    	TextView seikai_ritu = (TextView) layout.findViewById(R.id.seikai_ritu);
    	//正解率計算
    	int mondaiCount = seikaiCount + fuseikaiCount;
    	float Seikairitu = (float)seikaiCount / (float)mondaiCount;
    	Seikairitu = Seikairitu *100;
    	final int Seikairitu_floor = (int) Math.floor(Seikairitu);
    	//テキストをセット
    	seikai_ritu.setText(Seikairitu_floor + "%");
    	//ダイアログ表示
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setView(layout);
    	builder.setTitle("結果");
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
    	builder.setNeutralButton("結果を共有", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, "画像単語帳で「" + name + "」を練習した所、正解率は" + String.valueOf(Seikairitu_floor) + "%でした #画像単語帳");
					startActivity(intent);
				} catch (Exception e) {
				}
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
    public class Button1_Click implements View.OnClickListener{
		@Override
		public void onClick(View paramView) {
			if(nowSeikai.matches(button1.getText().toString())){
				//正解
				ShowMaruToast();
				seikaiCount = seikaiCount + 1;
				//次へ進むor終了
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//問題がまだある場合
					showMondai(nowMondai);
				}else{
					//問題がもう無い場合
					//結果画面を表示する
					ShowScoreDialog();
				}
				
			}else {
				//不正解
				ShowBatuToast();
				fuseikaiCount = fuseikaiCount + 1;
				//次へ進むor終了
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//問題がまだある場合
					showMondai(nowMondai);
				}else{
					//問題がもう無い場合
					//結果画面を表示する
					ShowScoreDialog();
				}
			}
		}
    }
    public class Button2_Click implements View.OnClickListener{
		@Override
		public void onClick(View paramView) {
			if(nowSeikai.matches(button2.getText().toString())){
				//正解
				ShowMaruToast();
				seikaiCount = seikaiCount + 1;
				//次へ進むor終了
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//問題がまだある場合
					showMondai(nowMondai);
				}else{
					//問題がもう無い場合
					//結果画面を表示する
					ShowScoreDialog();
				}
			}else {
				//不正解
				ShowBatuToast();
				fuseikaiCount = fuseikaiCount + 1;
				//次へ進むor終了
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//問題がまだある場合
					showMondai(nowMondai);
				}else{
					//問題がもう無い場合
					//結果画面を表示する
					ShowScoreDialog();
				}
			}
		}
    }
    public class Button3_Click implements View.OnClickListener{
		@Override
		public void onClick(View paramView) {
			if(nowSeikai.matches(button3.getText().toString())){
				//正解
				ShowMaruToast();
				seikaiCount = seikaiCount + 1;
				//次へ進むor終了
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//問題がまだある場合
					showMondai(nowMondai);
				}else{
					//問題がもう無い場合
					//結果画面を表示する
					ShowScoreDialog();
				}
			}else {
				//不正解
				ShowBatuToast();
				fuseikaiCount = fuseikaiCount + 1;
				//次へ進むor終了
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//問題がまだある場合
					showMondai(nowMondai);
				}else{
					//問題がもう無い場合
					//結果画面を表示する
					ShowScoreDialog();
				}
			}
		}
    }
}
