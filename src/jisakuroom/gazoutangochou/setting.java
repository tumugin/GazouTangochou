package jisakuroom.gazoutangochou;

import jisakuroom.jisakuclass.io.FileCtrl;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class setting extends PreferenceActivity{
	String currentPath;
	Preference install_preset;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        //Preference�擾
        install_preset = findPreference("preset_install");
        install_preset.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				WriteFirstData();
				return false;
			}
		});
        //�t�@�C�����X�g�擾
        String status = Environment.getExternalStorageState();//�O���X�g���[�W��
        if (status.equals(Environment.MEDIA_MOUNTED)){//SD�}���ς�
        	//SD�J�[�h�f�B���N�g���ɐݒ�
        	currentPath = Environment.getExternalStorageDirectory().getPath() + "/" + "GazouTangochou" + "/";
        }else{
        	//�����������[�ɐݒ�
        	currentPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName() + "/";
        }
    }
	
	public void WriteFirstData(){
		//����邩�m�F����_�C�A���O��\��
		AlertDialog.Builder alertDialog =  new AlertDialog.Builder(this);
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
				"���f�[�^�[��2011/11/20���݂̕��ł�\n" +
				"���C���X�g�[����A�t�@�C���I����ʂŃ��X�g�̍X�V���s�Ȃ��Ă�������");
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
				} catch (Exception e) {
					StackTraceElement[] stackTraceElements = e.getStackTrace();
					String err = "";
					for(StackTraceElement stackTraceElement : stackTraceElements){
						err = err + stackTraceElement.toString() + "\n";
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(setting.this);
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
