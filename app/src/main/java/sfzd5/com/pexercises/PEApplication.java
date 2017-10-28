package sfzd5.com.pexercises;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by fsp on 17-9-23.
 */

public class PEApplication extends Application {

    public static final String dbDirPath = "pedbpath";

    public boolean dbInitSuccess = false;
    public List<String> subjectDBs;

    HashMap<String, String> subjectMap = new HashMap<>();
    HashMap<String, String> name2DbFile = new HashMap<>();
    HashMap<String, String> dbFile2Name = new HashMap<>();

    public List<String> dbNames = new ArrayList<>();

    public boolean isSetSubject(){
        SharedPreferences mSharedPreferences = this.getSharedPreferences("suject", Context.MODE_PRIVATE);
        String subject = mSharedPreferences.getString("config","");
        return !subject.isEmpty();
    }

    public String getSubject(){
        SharedPreferences mSharedPreferences = this.getSharedPreferences("suject", Context.MODE_PRIVATE);
        String subject = mSharedPreferences.getString("config","");
        if(subject.isEmpty()){
            subject = "物理";
            mSharedPreferences.edit().putString("config",subject).commit();
        }
        return subject;
    }

    public void setSubject(String subject){
        SharedPreferences mSharedPreferences = this.getSharedPreferences("suject", Context.MODE_PRIVATE);
        mSharedPreferences.edit().putString("config",subject).commit();
    }

    public boolean initDbHelper(String dbName){
        File dbDir = getSubjectDbDir();

        String dbFileName = name2DbFile.get(dbName);
        File dbFile = new File(dbDir, dbFileName);
        if(!dbFile.exists()){
            return false;
        }

        DBHelper.init(dbFile);
        return true;
    }

    private void checkDbFile(){
        File dbDir = DirectoryUtils.findDirAtExternalSdCardPath(dbDirPath);
        if(dbDir==null){
            dbInitSuccess=false;
            return;
        } else {

            File[] fs = dbDir.listFiles();
            for (File f : fs) {
                String dbFile = f.getName();
                if (subjectMap.containsKey(dbFile)) {
                    subjectDBs.add(subjectMap.get(dbFile));
                }
            }

            if (subjectDBs.size() > 0)
                dbInitSuccess = true;
            else
                dbInitSuccess = false;
        }
    }

    public File getSubjectDbDir(){
        String dbDirName = "";
        String subject = getSubject();
        if(subject.equals("物理")) {
            dbDirName="wldb";
        } else if(subject.equals("数学")) {
            dbDirName="wldb";
        } else if(subject.equals("化学")) {
            dbDirName="hxdb";
        }
        File dbDir = DirectoryUtils.findDirAtExternalSdCardPath(dbDirPath);
        return new File(dbDir, dbDirName);
    }

    //多选题，计算题，简答题，实验题，填空题，选择题，综合题，做图题
    @Override
    public void onCreate() {
        super.onCreate();

        name2DbFile.put("多选题", "kqkd_dx.db");
        name2DbFile.put("计算题", "kqkd_js.db");
        name2DbFile.put("简答题", "kqkd_jd.db");
        name2DbFile.put("实验题", "kqkd_sy.db");
        name2DbFile.put("填空题", "kqkd_tk.db");
        name2DbFile.put("选择题", "kqkd_xz.db");
        name2DbFile.put("综合题", "kqkd_zh.db");
        name2DbFile.put("做图题", "kqkd_zt.db");
        for(String k : name2DbFile.keySet()){
            dbFile2Name.put(name2DbFile.get(k), k);
            dbNames.add(k);
        }

        subjectMap.put("wldb", "物理");
        subjectMap.put("sxdb", "数学");
        subjectMap.put("hxdb", "化学");

        subjectDBs = new ArrayList<>();
        checkDbFile();
    }

    @Override
    public void onTerminate() {
        DBHelper.takeDBHelper().close();
        super.onTerminate();
    }

    private boolean copyAssetsToFilesystem(String assetsSrc, String des){
        Logger.getLogger(DBHelper.class.getName()).info("Copy "+assetsSrc+" to "+des);
        InputStream istream = null;
        OutputStream ostream = null;
        try{
            AssetManager am = getAssets();
            istream = am.open(assetsSrc);
            ostream = new FileOutputStream(des);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = istream.read(buffer))>0){
                ostream.write(buffer, 0, length);
            }
            istream.close();
            ostream.close();
        }
        catch(Exception e){
            e.printStackTrace();
            try{
                if(istream!=null)
                    istream.close();
                if(ostream!=null)
                    ostream.close();
            }
            catch(Exception ee){
                ee.printStackTrace();
            }
            return false;
        }
        return true;
    }
}
