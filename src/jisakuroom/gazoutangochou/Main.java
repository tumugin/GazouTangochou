package jisakuroom.gazoutangochou;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jisakuroom.jisakuclass.io.FileCtrl;
import jisakuroom.jisakuclass.io.FolderCtrl;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

public class Main extends Activity {
	ListView listView;
	ArrayAdapter<String> adapter;
	String currentPath;
	List<File> fileList;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//下一行デバッグ中のみ!!マルチスレッド化したら外すこと!!
    	int ver = Build.VERSION.SDK_INT;
    	if(ver >= 11){
    		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
    	}
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        listView = (ListView) findViewById(R.id.listView1);//ListView取得
        //Onclick設定
        listView.setOnItemClickListener(new ClickEvent());
        listView.setOnItemLongClickListener(new LongClick());
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);//リスト作成
        fileList = new ArrayList<File>();//リスト作成(ファイルリスト)
        listView.setAdapter(adapter);//アダプター設定
        try {
			GetFileList();
		} catch (Exception e) {
			// TODO: エラーメッセージ表示
			
		}
		SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
		Editor editor = pref.edit();
		boolean isFirst = pref.getBoolean("isFirst", true);
		if(isFirst == true){
			//初回起動をfalseにする
			editor.putBoolean("isFirst", false);
			editor.commit();
			//ダイアログ表示
			//WriteFirstData();//単語帳を共有機能を作った為廃止
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("ようこそ!!");
			dialog.setMessage("このアプリは単語帳に書かれた言葉をGoogle画像検索で検索し3択クイズを作るアプリです。\n" +
					"自分で作ることも、単語帳をダウンロードすることも出来ます\n" +
					"単語帳をダウンロードする場合は「単語帳をダウンロード」を選択して下さい\n" +
					"※次回以降はこのダイアログは表示されません。ダウンロードはメニューから行う事ができます。");
			dialog.setPositiveButton("単語帳をダウンロード", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Main.this, tango_dl.class);
					startActivity(intent);
				}
			});
			dialog.setNegativeButton("閉じる", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			dialog.show();
		}
    }
    //ダウンロード後リスト読み直し
    @Override
    public void onActivityResult(int requestCode,int resCode,Intent it){
    	boolean IsDownload = it.getBooleanExtra("IsDownload", false);
    	if(IsDownload == true){
    		GetFileList();
    	}else {
    		super.onActivityResult(requestCode, resCode, it);
		}
    }
    //メニュー生成
    @SuppressWarnings("unused")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューアイテムを追加します
    	MenuItem menuItem1 = menu.add(Menu.NONE, 0, Menu.NONE, "追加").setIcon(android.R.drawable.ic_menu_add);
    	MenuItem menuItem2 = menu.add(Menu.NONE, 1, Menu.NONE, "問題をシャッフル").setIcon(android.R.drawable.ic_popup_sync);
    	MenuItem menuItem3 = menu.add(Menu.NONE, 2, Menu.NONE, "リストを更新").setIcon(android.R.drawable.ic_popup_sync);
    	MenuItem menuItem4 = menu.add(Menu.NONE, 3, Menu.NONE, "設定").setIcon(android.R.drawable.ic_menu_preferences);
    	MenuItem menuItem5 = menu.add(Menu.NONE, 4, Menu.NONE, "単語帳をダウンロード").setIcon(android.R.drawable.ic_menu_add);
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
    		//ファイル作成
			final EditText edtInput = new EditText(Main.this);
            edtInput.setHint("画像単語帳の名前");
            edtInput.setInputType(InputType.TYPE_CLASS_TEXT);
            //ダイアログ作成
            AlertDialog.Builder textEnterDlg =  new AlertDialog.Builder(Main.this);
            textEnterDlg.setTitle("新規作成");
            textEnterDlg.setMessage("ファイル名を入力してください(.csvは入れないでください)");
            textEnterDlg.setView(edtInput);
            textEnterDlg.setPositiveButton("新規作成", new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
            		/*OKボタンをクリックした時の処理 */
            		try {
						if(FolderCtrl.IsFolderExists(currentPath + edtInput.getText().toString() + ".csv") == false){
							FileCtrl.saveString(currentPath + edtInput.getText().toString() + ".csv", "", "SJIS");
							Toast.makeText(Main.this, "作成しました", Toast.LENGTH_LONG).show();
						}else {
							Toast.makeText(Main.this, "ファイル名が重複しています", Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						Toast.makeText(Main.this, "作成に失敗しました", Toast.LENGTH_LONG).show();
					}
            		GetFileList();
            	}
            });
            textEnterDlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
            textEnterDlg.show();
    	}else if (item.getItemId() == 1) {
    		SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
    		Editor editor = pref.edit();
    		boolean isShuffle = pref.getBoolean("isShuffle", false);
    		if(isShuffle == false){
    			editor.putBoolean("isShuffle", true);
    			Toast.makeText(Main.this, "シャッフルをONにしました", Toast.LENGTH_LONG).show();
    		}else {
    			editor.putBoolean("isShuffle", false);
    			Toast.makeText(Main.this, "シャッフルをOFFにしました", Toast.LENGTH_LONG).show();
			}
    		editor.commit();
		}
    	else if (item.getItemId() == 2) {
			GetFileList();
			Toast.makeText(Main.this, "リストを更新しました", Toast.LENGTH_LONG).show();
		}else if (item.getItemId() == 3){
			Intent intent = new Intent(this, setting.class);
			startActivity(intent);
		}else if (item.getItemId() == 4) {
			Intent intent = new Intent(this, tango_dl.class);
			startActivityForResult(intent, 1);
		}
    	return true;
    }
    //ListViewタップイベント
    class ClickEvent implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> adapter2, View view, final int position, long id) {
			String[] str_items = {"練習する","編集する","単語帳を公開する","キャンセル"};
			AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
			builder.setTitle("処理を選択して下さい");
			builder.setItems(str_items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					//ファイルパス取得
					final String filePath = fileList.get(position).getPath();
					if(paramInt == 0){
						//Intent準備
						Intent intent = new Intent(Main.this, mondai.class);
						//パスを追加
						intent.putExtra("path", filePath);
						//単語帳の名前を追加
						intent.putExtra("name", adapter.getItem(position));
						//起動!!
						startActivity(intent);
					}else if (paramInt == 1) {
						//Intent準備
						Intent intent = new Intent(Main.this, edit.class);
						//パスを追加
						intent.putExtra("path", filePath);
						//起動!!
						startActivity(intent);
					}else if (paramInt == 2) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(Main.this);
						dialog.setTitle("単語帳を公開");
						dialog.setMessage("単語帳を公開しますか？\n「はい」を選択すると開発者に単語帳を添付ファイルとして送信します(メールソフトが起動します)\n\n" +
								"単語帳は当アプリの「単語帳をダウンロード」のリストに掲載します\n" +
								"※審査後、結果を返信させていただきます。\n" +
								"※メールの本文に必ず単語帳の説明とリストに掲載する名前を書いてください");
						dialog.setPositiveButton("はい", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//メールソフト起動
								// インテントのインスタンス生成
								Intent intent = new Intent();
								// インテントにアクション及び送信情報をセット
								intent.setAction(Intent.ACTION_SEND);
								String[] mailto = {"oishikazuki+yukirin@gmail.com"};
								intent.putExtra(Intent.EXTRA_EMAIL, mailto);
								intent.putExtra(Intent.EXTRA_SUBJECT, "単語帳の公開");
								// 画像を添付
								intent.setType("text/html");
								intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
								// メール起動
								startActivity(intent);
							}
						});
						dialog.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
							}
						});
						dialog.show();
					}
				}
			});
			builder.show();
		}
    }
    //ListViewロングタップイベント
    class LongClick implements OnItemLongClickListener{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			//ファイルパス取得
			final String filePath = fileList.get(position).getPath();
			final String fileName = fileList.get(position).getName();
			//メニューアイテム
			String[] str_items = {"削除する","リネームする","キャンセルする"};
			AlertDialog.Builder alertDialog =  new AlertDialog.Builder(Main.this);
			alertDialog.setTitle("操作を選択してください");
			alertDialog.setIcon(R.drawable.ic_launcher);
			alertDialog.setItems(str_items, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == 0){
						//ファイル削除
						AlertDialog.Builder yesnoDialog =  new AlertDialog.Builder(Main.this);
						yesnoDialog.setTitle("確認");
						yesnoDialog.setMessage("ファイル(" + fileName + ")を削除してもよろしいでしょうか?");
						yesnoDialog.setPositiveButton("削除", new DialogInterface.OnClickListener() {
			            	public void onClick(DialogInterface dialog, int whichButton) {
			            		/*OKボタンをクリックした時の処理 */
			            		try {
									FileCtrl.deleteFile(filePath);
									Toast.makeText(Main.this, "削除しました", Toast.LENGTH_LONG).show();
								} catch (Exception e) {
									Toast.makeText(Main.this, "削除に失敗しました", Toast.LENGTH_LONG).show();
								}
			            		GetFileList();
			            	}
			            });
						yesnoDialog.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						});
						yesnoDialog.show();
					}else if (which == 1) {
						//リネーム
						final EditText edtInput = new EditText(Main.this);
	                    edtInput.setHint("画像単語帳の名前");
	                    edtInput.setInputType(InputType.TYPE_CLASS_TEXT);
	                    //ダイアログ作成
	                    AlertDialog.Builder textEnterDlg =  new AlertDialog.Builder(Main.this);
	                    textEnterDlg.setTitle("ファイル名を変更");
	                    textEnterDlg.setMessage("ファイル(" + fileName + ")の新しいファイル名を入力してください(.csvは入れないでください)");
	                    textEnterDlg.setView(edtInput);
	                    textEnterDlg.setPositiveButton("変更", new DialogInterface.OnClickListener() {
			            	public void onClick(DialogInterface dialog, int whichButton) {
			            		/*OKボタンをクリックした時の処理 */
			            		try {
			            			if(FolderCtrl.IsFolderExists(currentPath + edtInput.getText().toString() + ".csv") == false){
			            				FileCtrl.changeFileName(filePath, currentPath + edtInput.getText().toString() + ".csv");
			            				Toast.makeText(Main.this, "変更しました", Toast.LENGTH_LONG).show();
			            			}else{
			            				Toast.makeText(Main.this, "ファイル名が重複しています", Toast.LENGTH_LONG).show();
			            			}
								} catch (Exception e) {
									Toast.makeText(Main.this, "変更に失敗しました", Toast.LENGTH_LONG).show();
								}
			            		GetFileList();
			            	}
			            });
	                    textEnterDlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						});
	                    textEnterDlg.show();
					}else if (which == 2) {
						//キャンセル(なんも書かなくてよし!!)
					}
				}
			});
			alertDialog.show();
			return false;
		}
    }
    //ファイルリスト取得(更新も可)
    public void GetFileList(){
    	adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);//リスト作成
        fileList = new ArrayList<File>();//リスト作成(ファイルリスト)
        //ファイルリスト取得
        String status = Environment.getExternalStorageState();//外部ストレージ状況
        if (status.equals(Environment.MEDIA_MOUNTED)){//SD挿入済み
        	//SDカードディレクトリに設定
        	currentPath = Environment.getExternalStorageDirectory().getPath() + "/" + "GazouTangochou" + "/";
        	//フォルダー存在チェック
        	if(!FolderCtrl.IsFolderExists(currentPath)){
        		FolderCtrl.MakeFolder(currentPath);
        	}
        }else{
        	//内蔵メモリーに設定
        	currentPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName() + "/";
        }
        //ファイルリスト取得
        File[] fileListGet = FolderCtrl.GetFileList(currentPath, ".csv");
        for(File file : fileListGet){
        	fileList.add(file);
        	adapter.add(file.getName().replace(".csv", ""));
        }
        listView.setAdapter(adapter);//アダプター設定
    }
    public void WriteFirstData(){
		//入れるか確認するダイアログを表示
		AlertDialog.Builder alertDialog =  new AlertDialog.Builder(Main.this);
		alertDialog.setTitle("プリセットデーターのインストール");
		alertDialog.setMessage("プリセットデーターのインストールをしますか？\n" +
				"プリセットデーターには以下の物が含まれます\n\n" +
				"1.AKB48メディア選抜メンバー(第三回選抜総選挙)\n" +
				"2.AKB48全メンバー\n" +
				"3.AKB48チームA\n" +
				"4.AKB48チームK\n" +
				"5.AKB48チームB\n" +
				"6.AKB48チーム4\n" +
				"7.AKB48チーム研究生\n\n" +
				"※設定からいつでもインストールすることが出来ます\n" +
				"※データーは2011/11/20現在の物です");
		alertDialog.setPositiveButton("インストール", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					//インストール処理
					FileCtrl.saveString(currentPath + "AKB48メディア選抜メンバー(第三回選抜総選挙).csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_senbatu.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48全メンバー.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_member_all.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48チームA.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_team_a.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48チームK.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_team_k.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48チームB.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_team_b.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48チーム4.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_team_4.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48チーム研究生.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_team_kenkiyusei.csv"), "SJIS");
					GetFileList();
				} catch (Exception e) {
					StackTraceElement[] stackTraceElements = e.getStackTrace();
					String err = "";
					for(StackTraceElement stackTraceElement : stackTraceElements){
						err = err + stackTraceElement.toString() + "\n";
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
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
			}
		});
		alertDialog.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		alertDialog.show();
    }
}