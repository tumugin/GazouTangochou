package jisakuroom.gazoutangochou;

import jisakuroom.jisakuclass.io.FileCtrl;
import jp.ac.wakhok.tomoharu.csv.CSVLine;
import jp.ac.wakhok.tomoharu.csv.CSVTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class edit extends Activity{
	String enc;
	ArrayAdapter<String> Listadapter;
	ListView listView;
	String fileName;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		//ビュー初期化
		Listadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		listView = (ListView) findViewById(R.id.listView1);
		listView.setOnItemClickListener(new OnItemClick());
		listView.setOnItemLongClickListener(new OnItemLongClick());
		//パス取得
        Intent intent = getIntent();
        fileName = intent.getStringExtra("path");
        //エンコード設定取得
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		enc = sharedPreferences.getString("encode", "SJIS");
		try {
			String csvDataString = FileCtrl.loadStr(fileName, enc);
			String csvDataSplitString[] = csvDataString.split("\n",0);
			if(csvDataString.equals("") == true){
				return;
			}
			for (int i = 0; i < csvDataSplitString.length; i++) {
				CSVTokenizer csvTokenizer = new CSVTokenizer(csvDataSplitString[i]);
				if(csvTokenizer.hasMoreTokens()){
					String data = csvTokenizer.nextToken();
					Listadapter.add(data);
				}
			}
			listView.setAdapter(Listadapter);
		} catch (Exception e) {
			StackTraceElement[] stackTraceElements = e.getStackTrace();
			String err = "";
			for(StackTraceElement stackTraceElement : stackTraceElements){
				err = err + stackTraceElement.toString() + "\n";
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
	//メニュー生成
    @SuppressWarnings("unused")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューアイテムを追加します
    	MenuItem menuItem1 = menu.add(Menu.NONE, 0, Menu.NONE, "追加").setIcon(android.R.drawable.ic_input_add);
    	MenuItem menuItem2 = menu.add(Menu.NONE, 1, Menu.NONE, "保存して終了").setIcon(android.R.drawable.ic_menu_save);
    	MenuItem menuItem3 = menu.add(Menu.NONE, 2, Menu.NONE, "上書き保存").setIcon(android.R.drawable.ic_menu_save);
    	MenuItem menuItem4 = menu.add(Menu.NONE, 3, Menu.NONE, "保存せずに終了").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        int ver = Build.VERSION.SDK_INT;
        if(ver >= 11){
        	menuItem1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        	menuItem2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if(item.getItemId() == 0){
    		final EditText editText = new EditText(this);
    		editText.setHint("例:柏木由紀");
    		editText.setInputType(InputType.TYPE_CLASS_TEXT);
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("新しい問題を追加");
    		builder.setMessage("新しい問題の内容を設定してください");
    		builder.setView(editText);
    		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					Listadapter.add(editText.getText().toString());
					listView.setAdapter(Listadapter);
				}
			});
    		builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					
				}
			});
    		builder.show();
    	}else if (item.getItemId() == 1) {
			SaveData(fileName, true);
		}else if (item.getItemId() == 2) {
			SaveData(fileName, false);
		}else if (item.getItemId() == 3) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("確認");
			builder.setMessage("本当に保存せずに終了しても宜しいでしょうか?");
			builder.setCancelable(false);
			builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					finish();
				}
			});
			builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					
				}
			});
			builder.show();
		}
    	
    	return true;
    }
    
    public void SaveData(String filepath,boolean ExitAfterSave){
    	try {
			String CSVSave = "";
			for (int i = 0; i < Listadapter.getCount(); i++) {
				String str = Listadapter.getItem(i);
				CSVLine csvLine = new CSVLine();
				csvLine.addItem(str);
				CSVSave = CSVSave + csvLine.getLine() + "\n";
			}
			FileCtrl.saveString(filepath, CSVSave, enc);
			if(ExitAfterSave == true){
				finish();
			}
			Toast.makeText(this, "保存しました", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			StackTraceElement[] stackTraceElements = e.getStackTrace();
			String err = "";
			for(StackTraceElement stackTraceElement : stackTraceElements){
				err = err + stackTraceElement.toString() + "\n";
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("処理エラー発生");
			builder.setMessage("ファイルの保存時にエラーが発生しました\n\n内部スタックトレース:\n" + err + "\nエラーメッセージ:\n" + e.getMessage());
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					
				}
			});
			builder.show();
		}
    }
	//ListViewアイテムタップイベント
	public class OnItemClick implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> adapter, View view, final int position, long id) {
			String[] str_items = {"削除する","キャンセル"};
			AlertDialog.Builder builder = new AlertDialog.Builder(edit.this);
			builder.setTitle("メニュー");
			builder.setItems(str_items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					if(paramInt == 0){
						Listadapter.remove(Listadapter.getItem(position));
						listView.setAdapter(Listadapter);
					}
				}
			});
			builder.show();
		}
	}
	//ListViewアイテムロングタップイベント
	public class OnItemLongClick implements OnItemLongClickListener{
		@Override
		public boolean onItemLongClick(AdapterView<?> adapter, View view, final int position, long id) {
			final EditText editText = new EditText(edit.this);
    		editText.setHint("例:柏木由紀");
    		editText.setInputType(InputType.TYPE_CLASS_TEXT);
    		editText.setText(Listadapter.getItem(position));
    		AlertDialog.Builder builder = new AlertDialog.Builder(edit.this);
    		builder.setTitle("問題を編集");
    		builder.setMessage("問題の内容を設定してください");
    		builder.setView(editText);
    		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					@SuppressWarnings("unused")
					String item = Listadapter.getItem(position);
					item = editText.getText().toString();
					listView.setAdapter(Listadapter);
				}
			});
    		builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					
				}
			});
    		builder.show();
			return false;
		}
	}
}
