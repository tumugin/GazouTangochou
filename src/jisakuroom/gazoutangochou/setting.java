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
        //Preference取得
        install_preset = findPreference("preset_install");
        install_preset.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				WriteFirstData();
				return false;
			}
		});
        //ファイルリスト取得
        String status = Environment.getExternalStorageState();//外部ストレージ状況
        if (status.equals(Environment.MEDIA_MOUNTED)){//SD挿入済み
        	//SDカードディレクトリに設定
        	currentPath = Environment.getExternalStorageDirectory().getPath() + "/" + "GazouTangochou" + "/";
        }else{
        	//内蔵メモリーに設定
        	currentPath = Environment.getDataDirectory().getPath() + "/data/" + this.getPackageName() + "/";
        }
    }
	
	public void WriteFirstData(){
		//入れるか確認するダイアログを表示
		AlertDialog.Builder alertDialog =  new AlertDialog.Builder(this);
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
				"※データーは2011/11/20現在の物です\n" +
				"※インストール後、ファイル選択画面でリストの更新を行なってください");
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
				} catch (Exception e) {
					StackTraceElement[] stackTraceElements = e.getStackTrace();
					String err = "";
					for(StackTraceElement stackTraceElement : stackTraceElements){
						err = err + stackTraceElement.toString() + "\n";
					}
					AlertDialog.Builder builder = new AlertDialog.Builder(setting.this);
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
