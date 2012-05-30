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
	//�A�C�e��
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
		//�G���R�[�h�ݒ�擾
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		enc = sharedPreferences.getString("encode", "SJIS");
		//ListView�擾
		listView = (ListView) findViewById(R.id.listView1);
		listView.setOnItemClickListener(new OnItemClick());
		list = new ArrayList<FileDLItem>();
		//�_�C�A���O������
		Loaddialog = new ProgressDialog(this);
        Loaddialog.setMessage("�ǂݍ��ݒ�...");
        //Loaddialog.setCancelable(false);
        Loaddialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		Loaddialog.show();
		//�f�B���N�g���[�擾
        String status = Environment.getExternalStorageState();//�O���X�g���[�W��
        if (status.equals(Environment.MEDIA_MOUNTED)){//SD�}���ς�
        	//SD�J�[�h�f�B���N�g���ɐݒ�
        	currentPath = Environment.getExternalStorageDirectory().getPath() + "/" + "GazouTangochou" + "/";
        }else{
        	//�����������[�ɐݒ�
        	currentPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName() + "/";
        }
        //�t�@�C�����X�g�擾
		new Thread(new Runnable() {
			@Override
			public void run() {
				getItemList();//���X�g���擾
				handler.post(new SetListbox());//���X�g�{�b�N�X���Z�b�g
				Loaddialog.dismiss();
			}
		}).start();
	}
	//���j���[����
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem1 = menu.add(Menu.NONE, 0, Menu.NONE, "���X�g���X�V").setIcon(android.R.drawable.ic_popup_sync);
    	//MenuItem menuItem2 = menu.add(Menu.NONE, 1, Menu.NONE, "�P�꒠�����J").setIcon(android.R.drawable.ic_menu_share);
        int ver = Build.VERSION.SDK_INT;
        if(ver >= 11){
        	menuItem1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        	//menuItem2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
		return super.onCreateOptionsMenu(menu);
	}
	//���j���[�C�x���g
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == 0){
			Loaddialog.show();
			//�t�@�C�����X�g�擾
			new Thread(new Runnable() {
				@Override
				public void run() {
					getItemList();//���X�g���擾
					handler.post(new SetListbox());//���X�g�{�b�N�X���Z�b�g
					Loaddialog.dismiss();
				}
			}).start();
		}
		return true;
	}
	//�A�C�e���擾
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
					builder.setTitle("�����G���[����");
					builder.setMessage("�f�[�^�[���X�g�̎擾���ɃG���[���������܂���\n\n�����X�^�b�N�g���[�X:\n" + err + "\n�G���[���b�Z�[�W:\n" + e.getMessage());
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
			dialog.setTitle("�P�꒠�̃_�E�����[�h");
			dialog.setMessage("�P�꒠(" + item.Title + ")���_�E�����[�h���܂����H\n\n�^�C�g��:" + item.Title +
					"\n����:" + item.Description);
			dialog.setPositiveButton("�_�E�����[�h", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//�_�E�����[�h����
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								handler.post(new Runnable() {
									public void run() {
										Loaddialog.setMessage("�P�꒠���_�E�����[�h���Ă��܂�...");
										Loaddialog.show();
									}
								});
								//�_�E�����[�h����
								String data = download.downloadString(item.URL);
								FileCtrl.saveString(currentPath + item.Title + ".csv", data, enc);
								handler.post(new Runnable() {
									public void run() {
										Loaddialog.dismiss();
										Loaddialog.setMessage("�ǂݍ��ݒ�...");
										Toast.makeText(tango_dl.this, "�_�E�����[�h���܂���", Toast.LENGTH_LONG).show();
									}
								});
							} catch (final Exception e) {
								handler.post(new Runnable() {
									@Override
									public void run() {
										Loaddialog.dismiss();
										Loaddialog.setMessage("�ǂݍ��ݒ�...");
										StackTraceElement[] stackTraceElements = e.getStackTrace();
										String err = "";
										for(StackTraceElement stackTraceElement : stackTraceElements){
											err = err + stackTraceElement.toString() + "\n";
										}
										AlertDialog.Builder builder = new AlertDialog.Builder(tango_dl.this);
										builder.setTitle("�����G���[����");
										builder.setMessage("�P�꒠�̃_�E�����[�h���ɃG���[���������܂���\n\n�����X�^�b�N�g���[�X:\n" + err + "\n�G���[���b�Z�[�W:\n" + e.getMessage());
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
			dialog.setNegativeButton("�L�����Z��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			dialog.show();
		}
	}
	//ListView�J�X�^�}�C�Y
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
			// �r���[���󂯎��  
			View view = convertView;  
			if (view == null) {  
				// �󂯎�����r���[��null�Ȃ�V�����r���[�𐶐�  
				view = inflater.inflate(R.layout.tango_dl_listview, null);  
			}
			// �\�����ׂ��f�[�^�̎擾  
			FileDLItem item = (FileDLItem)items.get(position); 
			TextView title = (TextView) view.findViewById(R.id.textView1);
			title.setText(item.Title);
			TextView description = (TextView) view.findViewById(R.id.textView2);
			description.setText(item.Description);
			return view;  
		}  
	}
	//ListView�ɃA�_�v�^�[���Z�b�g
	public class SetListbox implements Runnable{
		@Override
		public void run() {
			FileListAdapter fileListAdapter = new FileListAdapter(tango_dl.this, R.layout.tango_dl_listview, list);
			listView.setAdapter(fileListAdapter);
		}
	}
}
