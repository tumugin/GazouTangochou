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
        //�r���[������
        button1 = (Button) findViewById(R.id.mondai_button1);
        button1.setOnClickListener(new Button1_Click());
        button2 = (Button) findViewById(R.id.mondai_button2);
        button2.setOnClickListener(new Button2_Click());
        button3 = (Button) findViewById(R.id.mondai_button3);
        button3.setOnClickListener(new Button3_Click());
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        imageView = (ImageView) findViewById(R.id.imageView1);
        //�}���o�cToast�p
        inflater = getLayoutInflater();
        maruView = inflater.inflate(R.layout.maru_toast,
                (ViewGroup) findViewById(R.id.maru_layout));
        batuView = inflater.inflate(R.layout.batu_toast,
                (ViewGroup) findViewById(R.id.batu_layout));
        //�p�X�擾
        Intent intent = getIntent();
        String fileName = intent.getStringExtra("path");
        name = intent.getStringExtra("name");
        //�V���b�t��ON OFF�m�F
        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        isShuffle = pref.getBoolean("isShuffle", false);
        //�G���R�[�h�ݒ�擾
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		enc = sharedPreferences.getString("encode", "SJIS");
		image_size = sharedPreferences.getString("image_size", "huge");
		face_only = sharedPreferences.getBoolean("face_only", true);
		toast_time = sharedPreferences.getString("toast_time", "short");
        //�t�@�C���ǂݍ���
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
			//��肪0�̏ꍇ�͏I��
			if(mondaiList.size() == 0){
				Toast.makeText(this, "��萔��0�ł��B�Œ��3�ǉ����Ă��������B", Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			if(mondaiList.size() <3){
				Toast.makeText(this, "��萔������܂���B�Œ��3�K�v�ł��B", Toast.LENGTH_LONG).show();
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
		//���ڂ�\��
		showMondai(0);
    }
    //�I�����Ƀ������[���J��
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
    //����\��
    public void showMondai(final int id){
    	thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
		    		//�{�^��������(UI�X���b�h)
					handler.post(new ButtonDisable());
		    		//���݂̖��̃��X�g���N���A�[
		        	nowMondaiList.clear();
		        	//�������擾
		        	String mondaiText = mondaiList.get(id);
		        	nowSeikai = mondaiText.toString();
		        	//�V���b�t���p�̃��X�g���N���A�[
		        	mondaiListShuffle.clear();
		        	//�N���[������(���݂̖���ǉ����Ȃ��悤�ɂ��邽��)
		        	for (int i = 0; i < mondaiList.size(); i++) {
		    			if(mondaiText.equals(mondaiList.get(i)) == false){
		    				mondaiListShuffle.add(mondaiList.get(i));
		    			}
		    		}
		        	//�V���b�t���p���X�g���V���b�t������
		        	Collections.shuffle(mondaiListShuffle);
		        	//�V���b�t���p���X�g������2�A�C�e���擾�ƒǉ�+������ǉ�
		        	nowMondaiList.add(mondaiText);
		        	nowMondaiList.add(mondaiListShuffle.get(0));
		        	nowMondaiList.add(mondaiListShuffle.get(1));
		        	//���݂̖����V���b�t��
		        	Collections.shuffle(nowMondaiList);
		        	//�摜���X�g���擾
		    		//���X�g�擾
					final List<ImageItem> imageItems = GoogleImageSearchAPI.SearchImage(mondaiText,image_size,face_only);
					if(imageItems.size() == 0){
						//��v����摜����������ꍇ
						handler.post(new Runnable() {
							@Override
							public void run() {
								button1.setText(nowMondaiList.get(0));
								button2.setText(nowMondaiList.get(1));
								button3.setText(nowMondaiList.get(2));
								imageView.setImageResource(R.drawable.ic_launcher);
								Toast.makeText(mondai.this, "��v����摜��������܂���ł���", Toast.LENGTH_LONG).show();
							}
						});
						//�{�^���L����(UI�X���b�h)
						handler.post(new ButtonEnable());
						return;
					}
					Collections.shuffle(imageItems);
					//�摜������
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
					//�摜+�e�L�X�g���Z�b�g(UI�X���b�h)
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
					//�{�^���L����(UI�X���b�h)
					handler.post(new ButtonEnable());
					//�J��
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
							builder.setTitle("�����G���[����");
							builder.setMessage("�摜�̎擾���ɃG���[���������܂���\n\n�����X�^�b�N�g���[�X:\n" + err + "\n�G���[���b�Z�[�W:\n" + e.getMessage());
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
							builder.setTitle("�����G���[����");
							builder.setMessage("�摜�̎擾���ɃG���[���������܂���\n\n�����X�^�b�N�g���[�X:\n" + err + "\n�G���[���b�Z�[�W:\n" + e.getMessage());
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
    //���j���[����
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ���j���[�A�C�e����ǉ����܂�
    	MenuItem menuItem1 = menu.add(Menu.NONE, 0, Menu.NONE, "�ʂ̉摜�Ń`�������W").setIcon(android.R.drawable.ic_popup_sync);
    	MenuItem menuItem2 = menu.add(Menu.NONE, 1, Menu.NONE, "�I��").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        int ver = Build.VERSION.SDK_INT;
        if(ver >= 11){
        	menuItem1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        	menuItem2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        return super.onCreateOptionsMenu(menu);
    }
    //���j���[�I��
    // �I�v�V�������j���[�A�C�e�����I�����ꂽ���ɌĂяo����܂�
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
    		Toast.makeText(this, "�ʂ̉摜���擾���Ă��܂�....", Toast.LENGTH_LONG).show();
    		showMondai(nowMondai);
    	}else if (item.getItemId() == 1) {
			finish();
		}
		return true;
    }
    //�{�^��������
    public class ButtonDisable implements Runnable{
		@Override
		public void run() {
			progressBar.setVisibility(View.VISIBLE);
			button1.setEnabled(false);
    		button2.setEnabled(false);
    		button3.setEnabled(false);
		}
    }
    //�{�^��������
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
    	textView.setText("�����́u" + nowSeikai + "�v");
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
    	textView.setText("�����́u" + nowSeikai + "�v");
    	Toast toast = new Toast(getApplicationContext());
    	toast.setGravity(Gravity.CENTER, 0, 0);
    	toast.setDuration(lenth);
    	toast.setView(batuView);
    	toast.show();
    }
    public void ShowScoreDialog(){
    	//�r���[�擾
    	View layout = inflater.inflate(R.layout.score_dlg,(ViewGroup) findViewById(R.id.score_dlg_layout));
    	TextView seikai_mon = (TextView) layout.findViewById(R.id.seikai_mon);
    	seikai_mon.setText(String.valueOf(seikaiCount)+ "��");
    	TextView fuseikai_mon = (TextView) layout.findViewById(R.id.fuseikai_mon);
    	fuseikai_mon.setText(String.valueOf(fuseikaiCount)+ "��");
    	TextView seikai_ritu = (TextView) layout.findViewById(R.id.seikai_ritu);
    	//���𗦌v�Z
    	int mondaiCount = seikaiCount + fuseikaiCount;
    	float Seikairitu = (float)seikaiCount / (float)mondaiCount;
    	Seikairitu = Seikairitu *100;
    	final int Seikairitu_floor = (int) Math.floor(Seikairitu);
    	//�e�L�X�g���Z�b�g
    	seikai_ritu.setText(Seikairitu_floor + "%");
    	//�_�C�A���O�\��
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setView(layout);
    	builder.setTitle("����");
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
    	builder.setNeutralButton("���ʂ����L", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, "�摜�P�꒠�Łu" + name + "�v����K�������A���𗦂�" + String.valueOf(Seikairitu_floor) + "%�ł��� #�摜�P�꒠");
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
				//����
				ShowMaruToast();
				seikaiCount = seikaiCount + 1;
				//���֐i��or�I��
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//��肪�܂�����ꍇ
					showMondai(nowMondai);
				}else{
					//��肪���������ꍇ
					//���ʉ�ʂ�\������
					ShowScoreDialog();
				}
				
			}else {
				//�s����
				ShowBatuToast();
				fuseikaiCount = fuseikaiCount + 1;
				//���֐i��or�I��
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//��肪�܂�����ꍇ
					showMondai(nowMondai);
				}else{
					//��肪���������ꍇ
					//���ʉ�ʂ�\������
					ShowScoreDialog();
				}
			}
		}
    }
    public class Button2_Click implements View.OnClickListener{
		@Override
		public void onClick(View paramView) {
			if(nowSeikai.matches(button2.getText().toString())){
				//����
				ShowMaruToast();
				seikaiCount = seikaiCount + 1;
				//���֐i��or�I��
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//��肪�܂�����ꍇ
					showMondai(nowMondai);
				}else{
					//��肪���������ꍇ
					//���ʉ�ʂ�\������
					ShowScoreDialog();
				}
			}else {
				//�s����
				ShowBatuToast();
				fuseikaiCount = fuseikaiCount + 1;
				//���֐i��or�I��
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//��肪�܂�����ꍇ
					showMondai(nowMondai);
				}else{
					//��肪���������ꍇ
					//���ʉ�ʂ�\������
					ShowScoreDialog();
				}
			}
		}
    }
    public class Button3_Click implements View.OnClickListener{
		@Override
		public void onClick(View paramView) {
			if(nowSeikai.matches(button3.getText().toString())){
				//����
				ShowMaruToast();
				seikaiCount = seikaiCount + 1;
				//���֐i��or�I��
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//��肪�܂�����ꍇ
					showMondai(nowMondai);
				}else{
					//��肪���������ꍇ
					//���ʉ�ʂ�\������
					ShowScoreDialog();
				}
			}else {
				//�s����
				ShowBatuToast();
				fuseikaiCount = fuseikaiCount + 1;
				//���֐i��or�I��
				nowMondai = nowMondai + 1;
				if(nowMondai != mondaiList.size()){
					//��肪�܂�����ꍇ
					showMondai(nowMondai);
				}else{
					//��肪���������ꍇ
					//���ʉ�ʂ�\������
					ShowScoreDialog();
				}
			}
		}
    }
}
