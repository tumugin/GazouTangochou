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
    	//����s�f�o�b�O���̂�!!�}���`�X���b�h��������O������!!
    	int ver = Build.VERSION.SDK_INT;
    	if(ver >= 11){
    		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
    	}
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        listView = (ListView) findViewById(R.id.listView1);//ListView�擾
        //Onclick�ݒ�
        listView.setOnItemClickListener(new ClickEvent());
        listView.setOnItemLongClickListener(new LongClick());
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);//���X�g�쐬
        fileList = new ArrayList<File>();//���X�g�쐬(�t�@�C�����X�g)
        listView.setAdapter(adapter);//�A�_�v�^�[�ݒ�
        try {
			GetFileList();
		} catch (Exception e) {
			// TODO: �G���[���b�Z�[�W�\��
			
		}
		SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
		Editor editor = pref.edit();
		boolean isFirst = pref.getBoolean("isFirst", true);
		if(isFirst == true){
			//����N����false�ɂ���
			editor.putBoolean("isFirst", false);
			editor.commit();
			//�_�C�A���O�\��
			//WriteFirstData();//�P�꒠�����L�@�\��������הp�~
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("�悤����!!");
			dialog.setMessage("���̃A�v���͒P�꒠�ɏ����ꂽ���t��Google�摜�����Ō�����3���N�C�Y�����A�v���ł��B\n" +
					"�����ō�邱�Ƃ��A�P�꒠���_�E�����[�h���邱�Ƃ��o���܂�\n" +
					"�P�꒠���_�E�����[�h����ꍇ�́u�P�꒠���_�E�����[�h�v��I�����ĉ�����\n" +
					"������ȍ~�͂��̃_�C�A���O�͕\������܂���B�_�E�����[�h�̓��j���[����s�������ł��܂��B");
			dialog.setPositiveButton("�P�꒠���_�E�����[�h", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Main.this, tango_dl.class);
					startActivity(intent);
				}
			});
			dialog.setNegativeButton("����", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			dialog.show();
		}
    }
    //�_�E�����[�h�ナ�X�g�ǂݒ���
    @Override
    public void onActivityResult(int requestCode,int resCode,Intent it){
    	boolean IsDownload = it.getBooleanExtra("IsDownload", false);
    	if(IsDownload == true){
    		GetFileList();
    	}else {
    		super.onActivityResult(requestCode, resCode, it);
		}
    }
    //���j���[����
    @SuppressWarnings("unused")
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ���j���[�A�C�e����ǉ����܂�
    	MenuItem menuItem1 = menu.add(Menu.NONE, 0, Menu.NONE, "�ǉ�").setIcon(android.R.drawable.ic_menu_add);
    	MenuItem menuItem2 = menu.add(Menu.NONE, 1, Menu.NONE, "�����V���b�t��").setIcon(android.R.drawable.ic_popup_sync);
    	MenuItem menuItem3 = menu.add(Menu.NONE, 2, Menu.NONE, "���X�g���X�V").setIcon(android.R.drawable.ic_popup_sync);
    	MenuItem menuItem4 = menu.add(Menu.NONE, 3, Menu.NONE, "�ݒ�").setIcon(android.R.drawable.ic_menu_preferences);
    	MenuItem menuItem5 = menu.add(Menu.NONE, 4, Menu.NONE, "�P�꒠���_�E�����[�h").setIcon(android.R.drawable.ic_menu_add);
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
    		//�t�@�C���쐬
			final EditText edtInput = new EditText(Main.this);
            edtInput.setHint("�摜�P�꒠�̖��O");
            edtInput.setInputType(InputType.TYPE_CLASS_TEXT);
            //�_�C�A���O�쐬
            AlertDialog.Builder textEnterDlg =  new AlertDialog.Builder(Main.this);
            textEnterDlg.setTitle("�V�K�쐬");
            textEnterDlg.setMessage("�t�@�C��������͂��Ă�������(.csv�͓���Ȃ��ł�������)");
            textEnterDlg.setView(edtInput);
            textEnterDlg.setPositiveButton("�V�K�쐬", new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
            		/*OK�{�^�����N���b�N�������̏��� */
            		try {
						if(FolderCtrl.IsFolderExists(currentPath + edtInput.getText().toString() + ".csv") == false){
							FileCtrl.saveString(currentPath + edtInput.getText().toString() + ".csv", "", "SJIS");
							Toast.makeText(Main.this, "�쐬���܂���", Toast.LENGTH_LONG).show();
						}else {
							Toast.makeText(Main.this, "�t�@�C�������d�����Ă��܂�", Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						Toast.makeText(Main.this, "�쐬�Ɏ��s���܂���", Toast.LENGTH_LONG).show();
					}
            		GetFileList();
            	}
            });
            textEnterDlg.setNegativeButton("�L�����Z��", new DialogInterface.OnClickListener() {
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
    			Toast.makeText(Main.this, "�V���b�t����ON�ɂ��܂���", Toast.LENGTH_LONG).show();
    		}else {
    			editor.putBoolean("isShuffle", false);
    			Toast.makeText(Main.this, "�V���b�t����OFF�ɂ��܂���", Toast.LENGTH_LONG).show();
			}
    		editor.commit();
		}
    	else if (item.getItemId() == 2) {
			GetFileList();
			Toast.makeText(Main.this, "���X�g���X�V���܂���", Toast.LENGTH_LONG).show();
		}else if (item.getItemId() == 3){
			Intent intent = new Intent(this, setting.class);
			startActivity(intent);
		}else if (item.getItemId() == 4) {
			Intent intent = new Intent(this, tango_dl.class);
			startActivityForResult(intent, 1);
		}
    	return true;
    }
    //ListView�^�b�v�C�x���g
    class ClickEvent implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> adapter2, View view, final int position, long id) {
			String[] str_items = {"���K����","�ҏW����","�P�꒠�����J����","�L�����Z��"};
			AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
			builder.setTitle("������I�����ĉ�����");
			builder.setItems(str_items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface paramDialogInterface, int paramInt) {
					//�t�@�C���p�X�擾
					final String filePath = fileList.get(position).getPath();
					if(paramInt == 0){
						//Intent����
						Intent intent = new Intent(Main.this, mondai.class);
						//�p�X��ǉ�
						intent.putExtra("path", filePath);
						//�P�꒠�̖��O��ǉ�
						intent.putExtra("name", adapter.getItem(position));
						//�N��!!
						startActivity(intent);
					}else if (paramInt == 1) {
						//Intent����
						Intent intent = new Intent(Main.this, edit.class);
						//�p�X��ǉ�
						intent.putExtra("path", filePath);
						//�N��!!
						startActivity(intent);
					}else if (paramInt == 2) {
						AlertDialog.Builder dialog = new AlertDialog.Builder(Main.this);
						dialog.setTitle("�P�꒠�����J");
						dialog.setMessage("�P�꒠�����J���܂����H\n�u�͂��v��I������ƊJ���҂ɒP�꒠��Y�t�t�@�C���Ƃ��đ��M���܂�(���[���\�t�g���N�����܂�)\n\n" +
								"�P�꒠�͓��A�v���́u�P�꒠���_�E�����[�h�v�̃��X�g�Ɍf�ڂ��܂�\n" +
								"���R����A���ʂ�ԐM�����Ă��������܂��B\n" +
								"�����[���̖{���ɕK���P�꒠�̐����ƃ��X�g�Ɍf�ڂ��閼�O�������Ă�������");
						dialog.setPositiveButton("�͂�", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								//���[���\�t�g�N��
								// �C���e���g�̃C���X�^���X����
								Intent intent = new Intent();
								// �C���e���g�ɃA�N�V�����y�ё��M�����Z�b�g
								intent.setAction(Intent.ACTION_SEND);
								String[] mailto = {"oishikazuki+yukirin@gmail.com"};
								intent.putExtra(Intent.EXTRA_EMAIL, mailto);
								intent.putExtra(Intent.EXTRA_SUBJECT, "�P�꒠�̌��J");
								// �摜��Y�t
								intent.setType("text/html");
								intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + filePath));
								// ���[���N��
								startActivity(intent);
							}
						});
						dialog.setNegativeButton("������", new DialogInterface.OnClickListener() {
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
    //ListView�����O�^�b�v�C�x���g
    class LongClick implements OnItemLongClickListener{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			//�t�@�C���p�X�擾
			final String filePath = fileList.get(position).getPath();
			final String fileName = fileList.get(position).getName();
			//���j���[�A�C�e��
			String[] str_items = {"�폜����","���l�[������","�L�����Z������"};
			AlertDialog.Builder alertDialog =  new AlertDialog.Builder(Main.this);
			alertDialog.setTitle("�����I�����Ă�������");
			alertDialog.setIcon(R.drawable.ic_launcher);
			alertDialog.setItems(str_items, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == 0){
						//�t�@�C���폜
						AlertDialog.Builder yesnoDialog =  new AlertDialog.Builder(Main.this);
						yesnoDialog.setTitle("�m�F");
						yesnoDialog.setMessage("�t�@�C��(" + fileName + ")���폜���Ă���낵���ł��傤��?");
						yesnoDialog.setPositiveButton("�폜", new DialogInterface.OnClickListener() {
			            	public void onClick(DialogInterface dialog, int whichButton) {
			            		/*OK�{�^�����N���b�N�������̏��� */
			            		try {
									FileCtrl.deleteFile(filePath);
									Toast.makeText(Main.this, "�폜���܂���", Toast.LENGTH_LONG).show();
								} catch (Exception e) {
									Toast.makeText(Main.this, "�폜�Ɏ��s���܂���", Toast.LENGTH_LONG).show();
								}
			            		GetFileList();
			            	}
			            });
						yesnoDialog.setNegativeButton("�L�����Z��", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						});
						yesnoDialog.show();
					}else if (which == 1) {
						//���l�[��
						final EditText edtInput = new EditText(Main.this);
	                    edtInput.setHint("�摜�P�꒠�̖��O");
	                    edtInput.setInputType(InputType.TYPE_CLASS_TEXT);
	                    //�_�C�A���O�쐬
	                    AlertDialog.Builder textEnterDlg =  new AlertDialog.Builder(Main.this);
	                    textEnterDlg.setTitle("�t�@�C������ύX");
	                    textEnterDlg.setMessage("�t�@�C��(" + fileName + ")�̐V�����t�@�C��������͂��Ă�������(.csv�͓���Ȃ��ł�������)");
	                    textEnterDlg.setView(edtInput);
	                    textEnterDlg.setPositiveButton("�ύX", new DialogInterface.OnClickListener() {
			            	public void onClick(DialogInterface dialog, int whichButton) {
			            		/*OK�{�^�����N���b�N�������̏��� */
			            		try {
			            			if(FolderCtrl.IsFolderExists(currentPath + edtInput.getText().toString() + ".csv") == false){
			            				FileCtrl.changeFileName(filePath, currentPath + edtInput.getText().toString() + ".csv");
			            				Toast.makeText(Main.this, "�ύX���܂���", Toast.LENGTH_LONG).show();
			            			}else{
			            				Toast.makeText(Main.this, "�t�@�C�������d�����Ă��܂�", Toast.LENGTH_LONG).show();
			            			}
								} catch (Exception e) {
									Toast.makeText(Main.this, "�ύX�Ɏ��s���܂���", Toast.LENGTH_LONG).show();
								}
			            		GetFileList();
			            	}
			            });
	                    textEnterDlg.setNegativeButton("�L�����Z��", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						});
	                    textEnterDlg.show();
					}else if (which == 2) {
						//�L�����Z��(�Ȃ�������Ȃ��Ă悵!!)
					}
				}
			});
			alertDialog.show();
			return false;
		}
    }
    //�t�@�C�����X�g�擾(�X�V����)
    public void GetFileList(){
    	adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);//���X�g�쐬
        fileList = new ArrayList<File>();//���X�g�쐬(�t�@�C�����X�g)
        //�t�@�C�����X�g�擾
        String status = Environment.getExternalStorageState();//�O���X�g���[�W��
        if (status.equals(Environment.MEDIA_MOUNTED)){//SD�}���ς�
        	//SD�J�[�h�f�B���N�g���ɐݒ�
        	currentPath = Environment.getExternalStorageDirectory().getPath() + "/" + "GazouTangochou" + "/";
        	//�t�H���_�[���݃`�F�b�N
        	if(!FolderCtrl.IsFolderExists(currentPath)){
        		FolderCtrl.MakeFolder(currentPath);
        	}
        }else{
        	//�����������[�ɐݒ�
        	currentPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName() + "/";
        }
        //�t�@�C�����X�g�擾
        File[] fileListGet = FolderCtrl.GetFileList(currentPath, ".csv");
        for(File file : fileListGet){
        	fileList.add(file);
        	adapter.add(file.getName().replace(".csv", ""));
        }
        listView.setAdapter(adapter);//�A�_�v�^�[�ݒ�
    }
    public void WriteFirstData(){
		//����邩�m�F����_�C�A���O��\��
		AlertDialog.Builder alertDialog =  new AlertDialog.Builder(Main.this);
		alertDialog.setTitle("�v���Z�b�g�f�[�^�[�̃C���X�g�[��");
		alertDialog.setMessage("�v���Z�b�g�f�[�^�[�̃C���X�g�[�������܂����H\n" +
				"�v���Z�b�g�f�[�^�[�ɂ͈ȉ��̕����܂܂�܂�\n\n" +
				"1.AKB48���f�B�A�I�������o�[(��O��I�����I��)\n" +
				"2.AKB48�S�����o�[\n" +
				"3.AKB48�`�[��A\n" +
				"4.AKB48�`�[��K\n" +
				"5.AKB48�`�[��B\n" +
				"6.AKB48�`�[��4\n" +
				"7.AKB48�`�[��������\n\n" +
				"���ݒ肩�炢�ł��C���X�g�[�����邱�Ƃ��o���܂�\n" +
				"���f�[�^�[��2011/11/20���݂̕��ł�");
		alertDialog.setPositiveButton("�C���X�g�[��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					//�C���X�g�[������
					FileCtrl.saveString(currentPath + "AKB48���f�B�A�I�������o�[(��O��I�����I��).csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_senbatu.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48�S�����o�[.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_member_all.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48�`�[��A.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_team_a.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48�`�[��K.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_team_k.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48�`�[��B.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_team_b.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48�`�[��4.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_team_4.csv"), "SJIS");
					FileCtrl.saveString(currentPath + "AKB48�`�[��������.csv", FileCtrl.loadStrFromResource(getAssets(), "SJIS", "akb_team_kenkiyusei.csv"), "SJIS");
					GetFileList();
				} catch (Exception e) {
					StackTraceElement[] stackTraceElements = e.getStackTrace();
					String err = "";
					for(StackTraceElement stackTraceElement : stackTraceElements){
						err = err + stackTraceElement.toString() + "\n";
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
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
		});
		alertDialog.setNegativeButton("�L�����Z��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		alertDialog.show();
    }
}