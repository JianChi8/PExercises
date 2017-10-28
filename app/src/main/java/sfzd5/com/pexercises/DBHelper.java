package sfzd5.com.pexercises;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 public long sharedTime;
 public String answer;
 public String answer_id;
 public int hard;
 */

public class DBHelper {

    private File dbFile;
    private SQLiteDatabase conn;
    private static DBHelper dbHelper;
    private DBHelper(){}
    public DBHelper(File dbFile){
        this.dbFile = dbFile;
        conn = openDatabase();
    }

    public String getDBFileName(){
        return dbFile.getName();
    }

    public static void init(File dbFile) {
        if(dbHelper!=null){
            dbHelper.close();
        }
        dbHelper = new DBHelper();
        dbHelper.dbFile=dbFile;
        dbHelper.conn = dbHelper.openDatabase();
    }

    public static DBHelper takeDBHelper(){
        return dbHelper;
    }

    private SQLiteDatabase openDatabase() {
        try {
            SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
            return database;
        } catch (Exception e) {
        }
        return null;
    }

    public void updateShow(int id, boolean show) {
        ContentValues cv = new ContentValues();
        cv.put("show", show?1:0);
        conn.update("test", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void updateShared(int id, boolean Shared) {
        ContentValues cv = new ContentValues();
        cv.put("Shared", Shared?1:0);
        if(Shared) cv.put("sharedTime", System.currentTimeMillis());
        conn.update("test", cv, "id=?", new String[]{String.valueOf(id)});
    }

    public byte[] takePic(String picname){
        byte[] bytes = null;
        String sql = "select picname,data from pic where picname='"+picname+"'";
        try {
            Cursor cursor = conn.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                bytes=cursor.getBlob(1);
            }
            cursor.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public int countAllTestQuestion() {
        int count= -1;
        String sql = "select count(*) as c from test";
        try {
            Cursor rs = conn.rawQuery(sql, null);
            if (rs.moveToNext()) {
                count = rs.getInt(0);
            }
            rs.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int countSharedTestQuestion() {
        int count= -1;
        String sql = "select count(*) as c from test where shared=1";
        try {
            Cursor rs = conn.rawQuery(sql, null);
            if (rs.moveToNext()) {
                count = rs.getInt(0);
            }
            rs.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int countShowTestQuestion() {
        int count= -1;
        String sql = "select count(*) as c from test where show=1";
        try {
            Cursor rs = conn.rawQuery(sql, null);
            if (rs.moveToNext()) {
                count = rs.getInt(0);
            }
            rs.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return count;
    }


    /**
     *
     public long sharedTime;
     public String answer;
     public String answer_id;
     public int hard;
     */
    public List<TestQuestion> takeTestQuestions100() {
        List<TestQuestion> testQuestions = new ArrayList<>();
        String sql = "select id,question,type,knowledge,shared,show,sharedTime,answer,answer_id,hard from test  where show=1 and shared=0 ORDER BY RANDOM() limit 100;";
        try {
            Cursor rs = conn.rawQuery(sql, null);
            while (rs.moveToNext()) {
                TestQuestion testQuestion = new TestQuestion();
                testQuestion = new TestQuestion();
                testQuestion.id = rs.getInt(0);
                testQuestion.question = rs.getString(1);
                testQuestion.type = rs.getString(2);
                testQuestion.knowledge = rs.getString(3);
                testQuestion.shared = rs.getInt(4)==1;
                testQuestion.show = rs.getInt(5)==1;
                testQuestion.sharedTime = rs.getLong(6);
                testQuestion.answer = rs.getString(7);
                testQuestion.answer_id = rs.getString(8);
                testQuestion.hard = rs.getInt(9);
                testQuestions.add(testQuestion);
            }
            rs.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return testQuestions;
    }

    public List<TestQuestion> takeTestQuestionsShared() {
        List<TestQuestion> testQuestions = new ArrayList<>();
        String sql = "select id,question,type,knowledge,shared,show,sharedTime,answer,answer_id,hard from test  where shared=1;";
        try {
            Cursor rs = conn.rawQuery(sql, null);
            while (rs.moveToNext()) {
                TestQuestion testQuestion = new TestQuestion();
                testQuestion = new TestQuestion();
                testQuestion.id = rs.getInt(0);
                testQuestion.question = rs.getString(1);
                testQuestion.type = rs.getString(2);
                testQuestion.knowledge = rs.getString(3);
                testQuestion.shared = rs.getInt(4)==1;
                testQuestion.show = rs.getInt(5)==1;
                testQuestion.sharedTime = rs.getLong(6);
                testQuestion.answer = rs.getString(7);
                testQuestion.answer_id = rs.getString(8);
                testQuestion.hard = rs.getInt(9);
                testQuestions.add(testQuestion);
            }
            rs.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return testQuestions;
    }

    public List<TestQuestion> takeTestQuestionsHidden() {
        List<TestQuestion> testQuestions = new ArrayList<>();
        String sql = "select id,question,type,knowledge,shared,show,sharedTime,answer,answer_id,hard from test  where show=0;";
        try {
            Cursor rs = conn.rawQuery(sql, null);
            while (rs.moveToNext()) {
                TestQuestion testQuestion = new TestQuestion();
                testQuestion = new TestQuestion();
                testQuestion.id = rs.getInt(0);
                testQuestion.question = rs.getString(1);
                testQuestion.type = rs.getString(2);
                testQuestion.knowledge = rs.getString(3);
                testQuestion.shared = rs.getInt(4)==1;
                testQuestion.show = rs.getInt(5)==1;
                testQuestion.sharedTime = rs.getLong(6);
                testQuestion.answer = rs.getString(7);
                testQuestion.answer_id = rs.getString(8);
                testQuestion.hard = rs.getInt(9);
                testQuestions.add(testQuestion);
            }
            rs.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return testQuestions;
    }

    public TestQuestion takeTestQuestion(int id) {
        TestQuestion testQuestion = null;
        String sql = "select id,question,type,knowledge,shared,show,sharedTime,answer,answer_id,hard from test where id=" + id;
        try {
            Cursor rs = conn.rawQuery(sql, null);
            if (rs.moveToNext()) {
                testQuestion = new TestQuestion();
                testQuestion.id = rs.getInt(0);
                testQuestion.question = rs.getString(1);
                testQuestion.type = rs.getString(2);
                testQuestion.knowledge = rs.getString(3);
                testQuestion.shared = rs.getInt(4)==1;
                testQuestion.show = rs.getInt(5)==1;
                testQuestion.sharedTime = rs.getLong(6);
                testQuestion.answer = rs.getString(7);
                testQuestion.answer_id = rs.getString(8);
                testQuestion.hard = rs.getInt(9);
            }
            rs.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return testQuestion;
    }

    public List<TestQuestion> takeTestQuestionsByKey100(String key) {
        List<TestQuestion> testQuestions = new ArrayList<>();
        String sql = "select id,question,type,knowledge,shared,show,sharedTime,answer,answer_id,hard from test  where show=1 and shared=0 and knowledge like '%"+key+"%';";
        try {
            Cursor rs = conn.rawQuery(sql, null);
            while (rs.moveToNext()) {
                TestQuestion testQuestion = new TestQuestion();
                testQuestion = new TestQuestion();
                testQuestion.id = rs.getInt(0);
                testQuestion.question = rs.getString(1);
                testQuestion.type = rs.getString(2);
                testQuestion.knowledge = rs.getString(3);
                testQuestion.shared = rs.getInt(4)==1;
                testQuestion.show = rs.getInt(5)==1;
                testQuestion.sharedTime = rs.getLong(6);
                testQuestion.answer = rs.getString(7);
                testQuestion.answer_id = rs.getString(8);
                testQuestion.hard = rs.getInt(9);
                testQuestions.add(testQuestion);
            }
            rs.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return testQuestions;
    }

    public HashMap<String,List<String>> takeKnowledgeMap() {
        HashMap<String,List<String>> knowledgeMap = new HashMap<>();
        String sql = "SELECT type,knowledge,count(*) as c FROM test WHERE show=1 AND shared=0 GROUP BY knowledge;";
        try {
            Cursor rs = conn.rawQuery(sql, null);
            while (rs.moveToNext()) {
                String type = rs.getString(0);
                String knowledge = rs.getString(1);
                int c = rs.getInt(2);
                if(knowledgeMap.containsKey(type)){
                    knowledgeMap.get(type).add(knowledge+"("+String.valueOf(c)+")");
                } else {
                    List<String> knowledges = new ArrayList<>();
                    knowledges.add(knowledge+"("+String.valueOf(c)+")");
                    knowledgeMap.put(type, knowledges);
                }
            }
            rs.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return knowledgeMap;
    }

    public List<TestQuestion> takeTestQuestionsByKnolage(String key) {
        List<TestQuestion> testQuestions = new ArrayList<>();
        String sql = "select id,question,type,knowledge,shared,show,sharedTime,answer,answer_id,hard from test  where show=1 and shared=0 and knowledge='"+key+"';";
        try {
            Cursor rs = conn.rawQuery(sql, null);
            while (rs.moveToNext()) {
                TestQuestion testQuestion = new TestQuestion();
                testQuestion = new TestQuestion();
                testQuestion.id = rs.getInt(0);
                testQuestion.question = rs.getString(1);
                testQuestion.type = rs.getString(2);
                testQuestion.knowledge = rs.getString(3);
                testQuestion.shared = rs.getInt(4)==1;
                testQuestion.show = rs.getInt(5)==1;
                testQuestion.sharedTime = rs.getLong(6);
                testQuestion.answer = rs.getString(7);
                testQuestion.answer_id = rs.getString(8);
                testQuestion.hard = rs.getInt(9);
                testQuestions.add(testQuestion);
            }
            rs.close();
        } catch (android.database.SQLException e) {
            e.printStackTrace();
        }
        return testQuestions;
    }

    public void delHidden() {
        conn.execSQL("delete from test where show=0;");
    }

    public void close() {
        if (conn != null && conn.isOpen()) {
            conn.close();
            conn = null;
        }
    }
}
