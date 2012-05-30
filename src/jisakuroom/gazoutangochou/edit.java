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
		//�r���[������
		Listadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		listView = (ListView) findViewById(R.id.listView1);
		listView.setOnItemClickListener(new OnItemClick());
		listView.setOnItemLongClickListener(new OnItemLongClick());
		//�p�X�擾
        Intent intent = getIntent();
        fileName = intent.getStringExtra("path");
        //�G���R�[�h�ݒ�擾
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
			builder.setTitle("�����G���[����");
			builder.setMessage("�t�@�C���̓ǂݍ��ݎ��ɃG���[���������܂���\n\n�����X�^�b�N�g���[�X:\n" + err + "\n�G���[���b�Z�[�W:\n" + e.getMessage());
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
	//���j���[����
    @SuppressWarnings("unused")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ���j���[�A�C�e����ǉ����܂�
    	MenuItem menuItem1 = menu.add(Menu.NONE, 0, Menu.NONE, "�ǉ�").setIcon(android.R.drawable.ic_input_add);
    	MenuItem menuItem2 = menu.add(Menu.NONE, 1, Menu.NONE, "�ۑ����ďI��").setIcon(android.R.drawable.ic_menu_save);
    	MenuItem menuItem3 = menu.add(Menu.NONE, 2, Menu.NONE, "�㏑���ۑ�").setIcon(android.R.drawable.ic_menu_save);
    	MenuItem menuItem4 = menu.add(Menu.NONE, 3, Menu.NONE, "�ۑ������ɏI��").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
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
    		editText.setHint("��:���ؗR�I");
    		editText.setInputType(InputType.TYPE_CLASS_TEXT);
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("�V��������ǉ�");
    		builder.setMessage("�V�������̓��e��ݒ肵�Ă�������");
    		builder.setView(editText);
    		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					Listadapter.add(editText.getText().toString());
					listView.setAdapter(Listadapter);
				}
			});
    		builder.setNegativeButton("�L�����Z��", new DialogInterface.OnClickListener() {
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
			builder.setTitle("�m�F");
			builder.setMessage("�{���ɕۑ������ɏI�����Ă��X�����ł��傤��?");
			builder.setCancelable(false);
			builder.setPositiveButton("�͂�", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					finish();
				}
			});
			builder.setNegativeButton("������", new DialogInterface.OnClickListener() {
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
			Toast.makeText(this, "�ۑ����܂���", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			StackTraceElement[] stackTraceElements = e.getStackTrace();
			String err = "";
			for(StackTraceElement stackTraceElement : stackTraceElements){
				err = err + stackTraceElement.toString() + "\n";
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("�����G���[����");
			builder.setMessage("�t�@�C���̕ۑ����ɃG���[���������܂���\n\n�����X�^�b�N�g���[�X:\n" + err + "\n�G���[���b�Z�[�W:\n" + e.getMessage());
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
	//ListView�A�C�e���^�b�v�C�x���g
	public class OnItemClick implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> adapter, View view, final int position, long id) {
			String[] str_items = {"�폜����","�L�����Z��"};
			AlertDialog.Builder builder = new AlertDialog.Builder(edit.this);
			builder.setTitle("���j���[");
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
	//ListView�A�C�e�������O�^�b�v�C�x���g
	public class OnItemLongClick implements OnItemLongClickListener{
		@Override
		public boolean onItemLongClick(AdapterView<?> adapter, View view, final int position, long id) {
			final EditText editText = new EditText(edit.this);
    		editText.setHint("��:���ؗR�I");
    		editText.setInputType(InputType.TYPE_CLASS_TEXT);
    		editText.setText(Listadapter.getItem(position));
    		AlertDialog.Builder builder = new AlertDialog.Builder(edit.this);
    		builder.setTitle("����ҏW");
    		builder.setMessage("���̓��e��ݒ肵�Ă�������");
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
    		builder.setNegativeButton("�L�����Z��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					
				}
			});
    		builder.show();
			return false;
		}
	}
}
