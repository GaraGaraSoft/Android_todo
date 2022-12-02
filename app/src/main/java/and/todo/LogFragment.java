package and.todo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("all") //ToDo　後で削除してできるかぎり警告文修正
public class LogFragment extends Fragment {
    private ArrayList<HashMap<String,String>> logData = new ArrayList<>();//全ログデータ
    private ArrayList<String> logTitle = new ArrayList<>();
    private long logNum; //総ログ件数
    private int page = 0;//現在のページ
    private EditDatabaseHelper helper;
    private ListView logList;
    private int selectId = 0; //選択したログのID
    private Button first,previous,forward,last,logBack;//ページを移動するボタン要素
    private TextView txtlogNum;//ページ、ログ番号を表示
    private int pageLogNum;//表示ページ内のログ件数

    public static LogFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        LogFragment fragment = new LogFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }

    @Nullable
    @Override //フラグメントのviewを作成する処理
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_log,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View decor = requireActivity().getWindow().getDecorView();//バーを含めたぜびゅー全体

        ConstraintLayout top = view.findViewById(R.id.topConstraint);
        top.setOnClickListener(v-> {

            if(decor.getSystemUiVisibility() == 0) {//アクションバーに表示されているとき

                decor.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                |View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }else{

                decor.setSystemUiVisibility(0);
            }

        });

        helper = new EditDatabaseHelper(requireActivity());

        getLogs(); //ログデータを取得する

        logList = view.findViewById(R.id.logList);//リストを設定
        logList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,logTitle));
        logList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectId = position; //リストクリック時に選択したデータIDを取得
                logBack.setEnabled(true); //項目選択で復元ボタン有効化
            }
        });


        //ログ情報をページで分割

        pageLogNum = (((int)logNum - page*30)>30) ? 30: ((int)logNum - page*30) ; //1ページに表示するログ件数取得

        txtlogNum = view.findViewById(R.id.logNum);
        txtlogNum.setText(String.format("ログ(No.%d～No.%d)まで表示(%d/%dページ)",page*30+1,page*30+pageLogNum,page+1,(int)logNum/30+1));

        first = view.findViewById(R.id.first); //最初の30件を取得して表示
        first.setOnClickListener(new PageChange());
        previous = view.findViewById(R.id.previous); //前の３０件を取得して表示
        previous.setOnClickListener(new PageChange());
        forward = view.findViewById(R.id.forward);
        forward.setOnClickListener(new PageChange());
        last = view.findViewById(R.id.last);
        last.setOnClickListener(new PageChange());

        logBack = requireActivity().findViewById(R.id.logBack);
        logBack.setEnabled(false); //項目選択まで復元ボタン無効化
        logBack.setOnClickListener(v->{
            //ログからデータを復旧する

            String ope = "";
            int id = Integer.parseInt(logData.get(selectId).get("id"));
            String beforetitle = logData.get(selectId).get("aftertitle");
            String beforelevel = logData.get(selectId).get("afterlevel");
            int beforehold = Integer.parseInt(logData.get(selectId).get("afterhold"));
            int beforebig =  Integer.parseInt(logData.get(selectId).get("afterbig"));
            String beforebigtitle = logData.get(selectId).get("afterbigtitle");
            int beforebighold = Integer.parseInt(logData.get(selectId).get("afterbighold"));
            int beforemiddle = Integer.parseInt(logData.get(selectId).get("aftermiddle"));
            String beforemiddletitle = logData.get(selectId).get("aftermiddletitle");
            int beforemiddlehold = Integer.parseInt(logData.get(selectId).get("aftermiddlehold"));
            String beforedate = logData.get(selectId).get("afterdate");
            String beforecontent = logData.get(selectId).get("aftercontent");
            int beforeimportant = Integer.parseInt(logData.get(selectId).get("afterimportant"));
            String beforememo = logData.get(selectId).get("beforememo"); //メモ,進捗度,終了判定はそのまま
            int beforeproceed = Integer.parseInt(logData.get(selectId).get("beforeproceed"));
            int beforefin = Integer.parseInt(logData.get(selectId).get("beforefin"));
            String aftertitle = logData.get(selectId).get("beforetitle");
            String afterlevel = logData.get(selectId).get("beforelevel");
            int afterhold = Integer.parseInt(logData.get(selectId).get("beforehold"));
            int afterbig = Integer.parseInt(logData.get(selectId).get("beforebig"));
            String afterbigtitle = logData.get(selectId).get("beforebigtitle");
            int afterbighold = Integer.parseInt(logData.get(selectId).get("beforebighold"));
            int aftermiddle  = Integer.parseInt(logData.get(selectId).get("beforemiddle"));
            String aftermiddletitle = logData.get(selectId).get("beforemiddletitle");
            int aftermiddlehold = Integer.parseInt(logData.get(selectId).get("beforemiddlehold"));
            String afterdate = logData.get(selectId).get("beforedate");
            String aftercontent = logData.get(selectId).get("beforecontent");
            int afterimportant = Integer.parseInt(logData.get(selectId).get("beforeimportant"));
            String aftermemo = logData.get(selectId).get("aftermemo");//メモ,進捗度,終了判定はそのまま
            int afterproceed = Integer.parseInt(logData.get(selectId).get("afterproceed"));
            int afterfin = Integer.parseInt(logData.get(selectId).get("afterfin"));

            if(logData.get(selectId).get("ope").equals("insert")){ //データ挿入ログの大中目標データを確認
                ope = "delete";
                if(logData.get(selectId).get("afterlevel").equals("middle")){
                    //大目標があるか確認しあったら最新情報を取得
                    try (SQLiteDatabase db = helper.getReadableDatabase()) {
                        db.beginTransaction();
                        try {
                            String[] cols = {"title", "hold"};
                            String[] sel = {"" + logData.get(selectId).get("afterbig")};
                            Cursor big = db.query("ToDoData", cols, "id=?", sel, null, null, null, null);
                            if (big.moveToFirst()) {
                                beforebigtitle = big.getString(0);
                                beforebighold = big.getInt(1);
                            } else {
                                beforebigtitle = "大目標未設定";
                                beforebighold = 0;
                            }

                            db.setTransactionSuccessful();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            db.endTransaction();
                        }
                    }
                }else if(logData.get(selectId).get("afterlevel").equals("small")){
                    //大中目標があるか確認しあれば最新情報を取得
                    try(SQLiteDatabase db = helper.getReadableDatabase()){
                        db.beginTransaction();
                        try {
                            String[] cols = {"title", "hold"};
                            String[] bigsel = {"" + logData.get(selectId).get("afterbig")};
                            Cursor big = db.query("ToDoData", cols, "id=?", bigsel, null, null, null, null);
                            if (big.moveToFirst()) {
                                beforebigtitle = big.getString(0);
                                beforebighold = big.getInt(1);
                            } else {
                                beforebigtitle = "大目標未設定";
                                beforebighold = 0;
                            }

                            String[] middlesel = {"" + logData.get(selectId).get("aftermiddle")};
                            Cursor middle = db.query("ToDoData", cols, "id=?", middlesel, null, null, null, null);
                            if (middle.moveToFirst()) {
                                beforemiddletitle = middle.getString(0);
                                beforemiddlehold = middle.getInt(1);
                            } else {
                                beforemiddletitle = "中目標未設定";
                                beforemiddlehold = 0;
                            }
                            db.setTransactionSuccessful();
                        }catch(Exception e){
                            e.printStackTrace();
                        }finally {
                            db.endTransaction();
                        }

                    }
                }
            }else if(logData.get(selectId).get("ope").equals("update")){ //データ変更時の大中目標を確認
                ope = "update";
                if(logData.get(selectId).get("afterlevel").equals("middle")){
                    //変更前、変更後の大目標があるか確認してあれば最新情報を取得

                    try (SQLiteDatabase db = helper.getReadableDatabase()) {
                        db.beginTransaction();
                        try {
                            String[] cols = {"title", "hold"};
                            String[] beforesel = {"" + logData.get(selectId).get("beforebig")};
                            Cursor big = db.query("ToDoData", cols, "id=?", beforesel, null, null, null, null);
                            if (big.moveToFirst()) {
                                afterbigtitle = big.getString(0);
                                afterbighold = big.getInt(1);
                            } else {
                                afterbigtitle = "大目標未設定";
                                afterbighold = 0;
                                afterhold = 1; //大目標がなければ保留状態にする
                            }

                            String[] aftersel = {"" + logData.get(selectId).get("afterbig")};
                            big = db.query("ToDoData", cols, "id=?", aftersel, null, null, null, null);
                            if (big.moveToFirst()) {
                                beforebigtitle = big.getString(0);
                                beforebighold = big.getInt(1);
                            } else {
                                beforebigtitle = "大目標未設定";
                                beforebighold = 0;
                            }

                            db.setTransactionSuccessful();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            db.endTransaction();
                        }
                    }

                }else if(logData.get(selectId).get("afterlevel").equals("small")){

                    //変更前、変更後の大中目標があるか確認してあれば最新情報を取得
                    try(SQLiteDatabase db = helper.getReadableDatabase()){
                        db.beginTransaction();
                        try {
                            String[] cols = {"title", "hold"};
                            String[] beforebigsel = {"" + logData.get(selectId).get("beforebig")};
                            Cursor big = db.query("ToDoData", cols, "id=?", beforebigsel, null, null, null, null);
                            if (big.moveToFirst()) {
                                afterbigtitle = big.getString(0);
                                afterbighold = big.getInt(1);
                            } else {
                                afterbigtitle = "大目標未設定";
                                afterbighold = 0;
                                afterhold = 1; //大目標がなければ保留状態にする
                            }

                            String[] beforemiddlesel = {"" + logData.get(selectId).get("beforemiddle")};
                            Cursor middle = db.query("ToDoData", cols, "id=?", beforemiddlesel, null, null, null, null);
                            if (middle.moveToFirst()) {
                                aftermiddletitle = middle.getString(0);
                                aftermiddlehold = middle.getInt(1);
                            } else {
                                aftermiddletitle = "中目標未設定";
                                aftermiddlehold = 0;
                                afterhold = 1;
                            }

                            String[] afterbigsel = {"" + logData.get(selectId).get("afterbig")};
                            big = db.query("ToDoData", cols, "id=?", afterbigsel, null, null, null, null);
                            if (big.moveToFirst()) {
                                beforebigtitle = big.getString(0);
                                beforebighold = big.getInt(1);
                            } else {
                                beforebigtitle = "大目標未設定";
                                beforebighold = 0;
                            }

                            String[] aftermiddlesel = {"" + logData.get(selectId).get("aftermiddle")};
                            middle = db.query("ToDoData", cols, "id=?", aftermiddlesel, null, null, null, null);
                            if (middle.moveToFirst()) {
                                beforemiddletitle = middle.getString(0);
                                beforemiddlehold = middle.getInt(1);
                            } else {
                                beforemiddletitle = "中目標未設定";
                                beforemiddlehold = 0;
                            }

                            db.setTransactionSuccessful();
                        }catch(Exception e){
                            e.printStackTrace();
                        }finally {
                            db.endTransaction();
                        }

                    }

                }
            }else if(logData.get(selectId).get("ope").equals("delete")){ //データ削除時の大中目標を確認
                ope = "insert";
                if(logData.get(selectId).get("beforelevel").equals("middle")){
                    //大目標データがあるか確認あれば最新情報を取得

                    try (SQLiteDatabase db = helper.getReadableDatabase()) {
                        db.beginTransaction();
                        try {
                            String[] cols = {"title", "hold"};
                            String[] sel = {"" + logData.get(selectId).get("beforebig")};
                            Cursor big = db.query("LogData", cols, "id=?", sel, null, null, null, null);
                            if (big.moveToFirst()) {
                                afterbigtitle = big.getString(0);
                                afterbighold = big.getInt(1);
                            } else {
                                afterbigtitle = "大目標未設定";
                                afterbighold = 0;
                                afterhold = 1; //大目標がなければ保留状態にする
                            }

                            db.setTransactionSuccessful();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            db.endTransaction();
                        }
                    }

                }else if(logData.get(selectId).get("beforelevel").equals("small")){
                    //大中目標データがあるか確認しあれば最新情報を取得
                    try(SQLiteDatabase db = helper.getReadableDatabase()){
                        db.beginTransaction();
                        try {
                            String[] cols = {"title", "hold"};
                            String[] bigsel = {"" + logData.get(selectId).get("beforebig")};
                            Cursor big = db.query("LogData", cols, "id=?", bigsel, null, null, null, null);
                            if (big.moveToFirst()) {
                                afterbigtitle = big.getString(0);
                                afterbighold = big.getInt(1);
                            } else {
                                afterbigtitle = "大目標未設定";
                                afterbighold = 0;
                                afterhold = 1; //大目標がなければ保留状態にする
                            }

                            String[] middlesel = {"" + logData.get(selectId).get("beforemiddle")};
                            Cursor middle = db.query("LogData", cols, "id=?", middlesel, null, null, null, null);
                            if (middle.moveToFirst()) {
                                aftermiddletitle = middle.getString(0);
                                aftermiddlehold = middle.getInt(1);
                            } else {
                                aftermiddletitle = "中目標未設定";
                                aftermiddlehold = 0;
                                afterhold = 1;
                            }
                            db.setTransactionSuccessful();
                        }catch(Exception e){
                            e.printStackTrace();
                        }finally {
                            db.endTransaction();
                        }

                    }
                }
            }
            //データベース更新
            try(SQLiteDatabase db = helper.getWritableDatabase()) {
                //トランザクション開始
                db.beginTransaction();
                try {

                    ContentValues lcv = new ContentValues(); //ログ用のContentValues
                    ContentValues cv = new ContentValues(); //変更用のContentValues
                    lcv.put("ope",ope);
                    lcv.put("id",id);
                    cv.put("id",id);

                    lcv.put("beforetitle",beforetitle);
                    lcv.put("beforelevel",beforelevel);
                    lcv.put("beforehold",beforehold);
                    lcv.put("beforebig",beforebig);
                    lcv.put("beforebigtitle",beforebigtitle);
                    lcv.put("beforebighold",beforebighold);
                    lcv.put("beforemiddle",beforemiddle);
                    lcv.put("beforemiddletitle",beforemiddletitle);
                    lcv.put("beforemiddlehold",beforemiddlehold);
                    lcv.put("beforedate",beforedate);
                    lcv.put("beforecontent",beforecontent);
                    lcv.put("beforeimportant",beforeimportant);
                    lcv.put("beforememo",beforememo);
                    lcv.put("beforeproceed",beforeproceed);
                    lcv.put("beforefin",beforefin);
                    if(ope.equals("insert") || ope.equals("update"))
                    {
                        cv.put("title",aftertitle);
                        cv.put("level",afterlevel);
                        cv.put("hold",afterhold);
                        cv.put("big",afterbig);
                        cv.put("bigtitle",afterbigtitle);
                        cv.put("bighold",afterbighold);
                        cv.put("middle",aftermiddle);
                        cv.put("middletitle",aftermiddletitle);
                        cv.put("middlehold",aftermiddlehold);
                        cv.put("date",afterdate);
                        cv.put("content",aftercontent);
                        cv.put("important",afterimportant);
                        cv.put("memo",aftermemo);
                        cv.put("proceed",afterproceed);
                        cv.put("fin",afterfin);
                    }
                    lcv.put("aftertitle",aftertitle);
                    lcv.put("afterlevel",afterlevel);
                    lcv.put("afterhold",afterhold);
                    lcv.put("afterbig",afterbig);
                    lcv.put("afterbigtitle",afterbigtitle);
                    lcv.put("afterbighold",afterbighold);
                    lcv.put("aftermiddle",aftermiddle);
                    lcv.put("aftermiddletitle",aftermiddletitle);
                    lcv.put("aftermiddlehold",aftermiddlehold);
                    lcv.put("afterdate",afterdate);
                    lcv.put("aftercontent",aftercontent);
                    lcv.put("afterimportant",afterimportant);
                    lcv.put("aftermemo",aftermemo);
                    lcv.put("afterproceed",afterproceed);
                    lcv.put("afterfin",afterfin);

                    if (ope.equals("delete")) { //挿入したデータを削除する


                        String[] params = {beforelevel, "" + id};
                        db.delete("ToDoData", "level=? and id=?", params);

                        if(beforelevel.equals("big")) { //大目標削除時中小目標のbigを削除
                            cv = new ContentValues();
                            cv.put("big",0);
                            cv.put("bigtitle","");
                            cv.put("bighold",0);
                            cv.put("hold",1); //大目標削除時保留に移動
                            db.update("ToDoData",cv,"level=? and big=?",new String[]{"middle",""+id});
                            cv.put("middlehold",1); //小目標の上の中目標も保留に移動
                            db.update("ToDoData",cv,"level=? and big=?",new String[]{"small",""+id});
                        }

                        if(beforelevel.equals("middle")){ //中目標削除時小目標のbig,middleを削除
                            cv = new ContentValues();
                            cv.put("big",0);
                            cv.put("bigtitle","");
                            cv.put("bighold",0);
                            cv.put("middle",0);
                            cv.put("middletitle","");
                            cv.put("middlehold",0);
                            cv.put("hold",1); //中目標削除時保留に移動
                            db.update("ToDoData",cv,"middle=?",new String[]{""+id});
                        }


                        db.insert("LogData",null,lcv); //データ変更をログに追加

                        Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                        if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                            String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                            db.execSQL(dsql);
                        }

                        Toast.makeText(requireActivity(), "データ削除しました", Toast.LENGTH_SHORT).show();


                    } else if (ope.equals("update")) { //変更したデータを元に戻す

                        db.insertWithOnConflict("ToDoData", null, cv,SQLiteDatabase.CONFLICT_REPLACE);

                        ContentValues bcv = new ContentValues();
                        ContentValues mcv = new ContentValues();
                        if(afterlevel.equals("big")){
                            bcv.put("big",id);
                            bcv.put("bigtitle",aftertitle);
                            bcv.put("bighold",afterhold);
                            if(afterhold==1){ //大目標を保留にしたとき中小目標も保留状態にする
                                bcv.put("hold",1);
                            }
                        }else if(afterlevel.equals("middle")){
                            mcv.put("big",afterbig);
                            mcv.put("bigtitle",afterbigtitle);
                            mcv.put("bighold",afterbighold);
                            mcv.put("middle",id);
                            mcv.put("middletitle",aftertitle);
                            mcv.put("middlehold",afterhold);
                            if(afterhold==1){//中目標を保留にしたとき小目標も保留状態にする
                                mcv.put("hold",1);
                            }else{ //中目標非保留状態でも大目標が保留状態ならば状態変わらず
                                if(afterbighold==1){
                                    mcv.put("middlehold",1);
                                }
                            }
                        }

                        if(afterlevel.equals("big")){ //大目標変更時その下の中小目標の大目標を変更
                            if(afterhold==1){//大目標保留時小目標の上の中目標も保留に
                                bcv.put("middlehold",afterhold);
                                db.update("ToDoData",bcv,"level=? and big=?",new String[]{"small",""+id});
                            }else{//大目標が非保留時中小目標の大目標の保留状態も非保留とする
                                bcv.put("bighold",afterhold);
                                db.update("ToDoData",bcv,"level=? and big=?",new String[]{"small",""+id});
                            }
                            db.update("ToDoData",bcv,"level=? and big=?",new String[]{"middle",""+id});
                        }else if(afterlevel.equals("middle")){ //中目標変更時その下の小目標の中目標を変更
                            db.update("ToDoData",mcv,"middle=?",new String[]{""+id});
                        }

                        db.insert("LogData",null,lcv); //データ変更をログに追加

                        Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                        if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                            String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                            db.execSQL(dsql);
                        }

                        Toast.makeText(requireActivity(), "データ更新に成功しました", Toast.LENGTH_SHORT).show();


                    } else if (ope.equals("insert")) { //削除したデータを復旧する

                        db.insert("ToDoData", null, cv);

                        db.insert("LogData",null,lcv); //新規追加をログに追加

                        Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                        if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                            String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                            db.execSQL(dsql);
                        }

                        Toast.makeText(requireActivity(), "データ登録に成功しました", Toast.LENGTH_SHORT).show();

                    }
                    //トランザクション成功
                    db.setTransactionSuccessful();
                }catch(SQLException e){
                    e.printStackTrace();
                }finally{
                    //トランザクションを終了
                    db.endTransaction();
                }
            }
            //ログの1ページ目に戻す
            page = 0;
            getLogs();
            logList.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, logTitle));
            pageLogNum = (((int) logNum - page * 30) > 30) ? 30 : ((int) logNum - page * 30); //1ページに表示するログ件数取得
            txtlogNum.setText(String.format("ログ(No.%d～No.%d)まで表示(%d/%dページ)", page * 30 + 1, page * 30 + pageLogNum, page + 1, (int) logNum / 30 + 1));
            logBack.setEnabled(false); //復元ボタン無効化

        });
    }

    class PageChange implements View.OnClickListener{ //ページ移動時、データベースから再取得して表示
        @Override
        public void onClick(View view) {
            if(view == first){
                if(page != 0) {
                    page = 0;
                    getLogs();
                    logList.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, logTitle));
                    pageLogNum = (((int) logNum - page * 30) > 30) ? 30 : ((int) logNum - page * 30); //1ページに表示するログ件数取得
                    txtlogNum.setText(String.format("ログ(No.%d～No.%d)まで表示(%d/%dページ)", page * 30 + 1, page * 30 + pageLogNum, page + 1, (int) logNum / 30 + 1));
                    logBack.setEnabled(false); //ページ切替え時復元ボタン無効化
                }
            }else if(view == previous){
                if(page>0)
                {
                    page--;
                    getLogs();
                    logList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,logTitle));
                    pageLogNum = (((int)logNum - page*30)>30) ? 30: ((int)logNum - page*30) ; //1ページに表示するログ件数取得
                    txtlogNum.setText(String.format("ログ(No.%d～No.%d)まで表示(%d/%dページ)",page*30+1,page*30+pageLogNum,page+1,(int)logNum/30+1));
                    logBack.setEnabled(false); //ページ切替え時復元ボタン無効化
                }
            }else if(view == forward){
                if( page < (int)logNum/30 )
                {
                    page++;
                    getLogs();
                    logList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,logTitle));
                    pageLogNum = (((int)logNum - page*30)>30) ? 30: ((int)logNum - page*30) ; //1ページに表示するログ件数取得
                    txtlogNum.setText(String.format("ログ(No.%d～No.%d)まで表示(%d/%dページ)",page*30+1,page*30+pageLogNum,page+1,(int)logNum/30+1));
                    logBack.setEnabled(false); //ページ切替え時復元ボタン無効化
                }
            }else if(view == last){
                if( page != (int)logNum/30) {
                    page = (int) logNum / 30;
                    getLogs();
                    logList.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, logTitle));
                    pageLogNum = (((int) logNum - page * 30) > 30) ? 30 : ((int) logNum - page * 30); //1ページに表示するログ件数取得
                    txtlogNum.setText(String.format("ログ(No.%d～No.%d)まで表示(%d/%dページ)", page * 30 + 1, page * 30 + pageLogNum, page + 1, (int) logNum / 30 + 1));
                    logBack.setEnabled(false); //ページ切替え時復元ボタン無効化
                }
            }
        }
    }

    void getLogs(){ //データベースから取得してデータ配列に挿入する

        try(SQLiteDatabase db = helper.getReadableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try{
                //ログデータ件数を取得
                logNum = DatabaseUtils.queryNumEntries(db, "LogData");
                //ログデータを取得
                Cursor logcs = db.query("LogData",null,null, null,null,null,"logid desc",page*30+", "+30);
                logData.clear(); //いったん配列を空にする
                logTitle.clear();
                //各項目のインデックスを取得
                int logid = logcs.getColumnIndex("logid");
                int ope = logcs.getColumnIndex("ope");
                int id = logcs.getColumnIndex("id");
                int beforetitle = logcs.getColumnIndex("beforetitle");
                int beforelevel = logcs.getColumnIndex("beforelevel");
                int beforehold = logcs.getColumnIndex("beforehold");
                int beforebig = logcs.getColumnIndex("beforebig");
                int beforebigtitle = logcs.getColumnIndex("beforebigtitle");
                int beforebighold = logcs.getColumnIndex("beforebighold");
                int beforemiddle = logcs.getColumnIndex("beforemiddle");
                int beforemiddletitle = logcs.getColumnIndex("beforemiddletitle");
                int beforemiddlehold = logcs.getColumnIndex("beforemiddlehold");
                int beforedate = logcs.getColumnIndex("beforedate");
                int beforecontent = logcs.getColumnIndex("beforecontent");
                int beforeimportant = logcs.getColumnIndex("beforeimportant");
                int beforememo = logcs.getColumnIndex("beforememo");
                int beforeproceed = logcs.getColumnIndex("beforeproceed");
                int beforefin = logcs.getColumnIndex("beforefin");
                int aftertitle = logcs.getColumnIndex("aftertitle");
                int afterlevel = logcs.getColumnIndex("afterlevel");
                int afterhold = logcs.getColumnIndex("afterhold");
                int afterbig = logcs.getColumnIndex("afterbig");
                int afterbigtitle = logcs.getColumnIndex("afterbigtitle");
                int afterbighold = logcs.getColumnIndex("afterbighold");
                int aftermiddle = logcs.getColumnIndex("aftermiddle");
                int aftermiddletitle = logcs.getColumnIndex("aftermiddletitle");
                int aftermiddlehold = logcs.getColumnIndex("aftermiddlehold");
                int afterdate = logcs.getColumnIndex("afterdate");
                int aftercontent = logcs.getColumnIndex("aftercontent");
                int afterimportant = logcs.getColumnIndex("afterimportant");
                int aftermemo = logcs.getColumnIndex("aftermemo");
                int afterproceed = logcs.getColumnIndex("afterproceed");
                int afterfin = logcs.getColumnIndex("afterfin");
                boolean next = logcs.moveToFirst();//カーソルの先頭に移動
                while(next){ //Mapに各項目を入れていく
                    HashMap<String,String> item = new HashMap<>();
                    item.put("logid",""+logcs.getInt(logid));
                    item.put("ope",logcs.getString(ope));
                    item.put("id",""+""+logcs.getInt(id));
                    item.put("beforetitle",logcs.getString(beforetitle));
                    item.put("beforelevel",logcs.getString(beforelevel));
                    item.put("beforehold",""+logcs.getInt(beforehold));
                    item.put("beforebig",""+logcs.getInt(beforebig));
                    item.put("beforebigtitle",logcs.getString(beforebigtitle));
                    item.put("beforebighold",""+logcs.getInt(beforebighold));
                    item.put("beforemiddle",""+logcs.getInt(beforemiddle));
                    item.put("beforemiddletitle",logcs.getString(beforemiddletitle));
                    item.put("beforemiddlehold",""+logcs.getInt(beforemiddlehold));
                    item.put("beforedate",logcs.getString(beforedate));
                    item.put("beforecontent",logcs.getString(beforecontent));
                    item.put("beforeimportant",""+logcs.getInt(beforeimportant));
                    item.put("beforememo",logcs.getString(beforememo));
                    item.put("beforeproceed",""+logcs.getInt(beforeproceed));
                    item.put("beforefin",""+logcs.getInt(beforefin));
                    item.put("aftertitle",logcs.getString(aftertitle));
                    item.put("afterlevel",logcs.getString(afterlevel));
                    item.put("afterhold",""+logcs.getInt(afterhold));
                    item.put("afterbig",""+logcs.getInt(afterbig));
                    item.put("afterbigtitle",logcs.getString(afterbigtitle));
                    item.put("afterbighold",""+logcs.getInt(afterbighold));
                    item.put("aftermiddle",""+logcs.getInt(aftermiddle));
                    item.put("aftermiddletitle",logcs.getString(aftermiddletitle));
                    item.put("aftermiddlehold",""+logcs.getInt(aftermiddlehold));
                    item.put("afterdate",logcs.getString(afterdate));
                    item.put("aftercontent",logcs.getString(aftercontent));
                    item.put("afterimportant",""+logcs.getInt(afterimportant));
                    item.put("aftermemo",logcs.getString(aftermemo));
                    item.put("afterproceed",""+logcs.getInt(afterproceed));
                    item.put("afterfin",""+logcs.getInt(afterfin));
                    logData.add(item); //ログデータ配列に追加
                    if(item.get("ope").equals("insert")){ //新規挿入データ
                        if(item.get("afterlevel").equals("big")){ //大目標を挿入したとき
                            if(Integer.parseInt(item.get("afterhold")) ==1){ //データが保留状態のとき
                                logTitle.add(String.format("大目標に[%s(保)]を追加しました",item.get("aftertitle")));
                            }else{ //データが非保留状態のとき
                                logTitle.add(String.format("大目標に[%s]を追加しました",item.get("aftertitle")));
                            }
                        }else if(item.get("afterlevel").equals("middle")){ //中目標・挿入
                            if(Integer.parseInt(item.get("afterbighold"))==1){ //大目標・保留中
                                logTitle.add(String.format("中目標に[(%s(保))-%s(保)]を追加しました",item.get("afterbigtitle"),item.get("aftertitle")));
                            }else{ //大目標・非保留中
                                if(Integer.parseInt(item.get("afterhold")) == 1){ //中目標・保留中
                                    logTitle.add(String.format("中目標に[(%s)-%s(保)]を追加しました",item.get("afterbigtitle"),item.get("aftertitle")));
                                }else{
                                    logTitle.add(String.format("中目標に[(%s)-%s]を追加しました",item.get("afterbigtitle"),item.get("aftertitle")));
                                }
                            }
                        }else if(item.get("afterlevel").equals("small")){ //小目標・挿入
                            if(Integer.parseInt(item.get("afterbighold"))==1){ //大目標・保留
                                logTitle.add(String.format("小目標に[(%s(保))-(%s(保))-%s(保)]を追加しました",item.get("afterbigtitle"),item.get("aftermiddletitle"),item.get("aftertitle")));
                            }else{//大目標・保留なし
                                if(Integer.parseInt(item.get("aftermiddlehold"))==1){//中目標・保留
                                    logTitle.add(String.format("小目標に[(%s)-(%s(保))-%s(保)]を追加しました",item.get("afterbigtitle"),item.get("aftermiddletitle"),item.get("aftertitle")));
                                }else{//中目標・保留無し
                                    if(Integer.parseInt(item.get("afterhold"))==1){//小目標・保留中
                                        logTitle.add(String.format("小目標に[(%s)-(%s)-%s(保)]を追加しました",item.get("afterbigtitle"),item.get("aftermiddletitle"),item.get("aftertitle")));
                                    }else{//小目標・保留なし
                                        logTitle.add(String.format("小目標に[(%s)-(%s)-%s]を追加しました",item.get("afterbigtitle"),item.get("aftermiddletitle"),item.get("aftertitle")));

                                    }
                                }

                            }
                        }else if(item.get("afterlevel").equals("schedule")){//スケジュール・挿入
                            if(Integer.parseInt(item.get("afterhold"))==1){
                                logTitle.add(String.format("スケジュールに[%s[%s](保)]を追加しました",item.get("aftertitle"),item.get("afterdate")));
                            }else{
                                logTitle.add(String.format("スケジュールに[%s(%s)]を追加しました",item.get("aftertitle"),item.get("afterdate")));
                            }
                        }else if(item.get("afterlevel").equals("todo")){//やることリスト・挿入
                            if(Integer.parseInt(item.get("afterhold"))==1){
                                logTitle.add(String.format("やることリストに[%s(保)]を追加しました",item.get("aftertitle")));
                            }else{
                                logTitle.add(String.format("やることリストに[%s]を追加しました",item.get("aftertitle")));
                            }
                        }
                    }else if(item.get("ope").equals("update")){ //更新データ
                        String edit = ""; //更新情報の文字列
                        if(item.get("beforelevel").equals("big")){ //大目標データ編集
                            if(Integer.parseInt(item.get("beforehold"))==1){ //更新前データが保留状態のとき
                                edit = String.format("大目標の[%s(保)]を",item.get("beforetitle"));
                            }else{ //更新前データが非保留状態のとき
                                edit = String.format("大目標の[%s]を",item.get("beforetitle"));
                            }
                            if(Integer.parseInt(item.get("afterhold"))==1){ //更新後データが保留状態のとき
                                edit += String.format("大目標の[%s(保)]に変更しました",item.get("aftertitle"));
                            }else{ //更新後データが非保留状態のとき
                                edit += String.format("大目標の[%s]に変更しました",item.get("aftertitle"));
                            }
                        }else if(item.get("beforelevel").equals("middle")){//中目標データ編集

                            if(Integer.parseInt(item.get("beforebighold"))==1){ //大目標・保留中
                                edit = String.format("中目標の[(%s(保))-%s(保)]を",item.get("beforebigtitle"),item.get("beforetitle"));
                            }else{ //大目標・非保留中
                                if(Integer.parseInt(item.get("beforehold")) == 1){ //中目標・保留中
                                    edit = String.format("中目標の[(%s)-%s(保)]を",item.get("beforebigtitle"),item.get("beforetitle"));
                                }else{
                                    edit = String.format("中目標の[(%s)-%s]を",item.get("beforebigtitle"),item.get("beforetitle"));
                                }
                            }
                            if(Integer.parseInt(item.get("afterbighold"))==1){ //大目標・保留中
                                edit += String.format("中目標の[(%s(保))-%s(保)]に変更しました",item.get("afterbigtitle"),item.get("aftertitle"));
                            }else{ //大目標・非保留中
                                if(Integer.parseInt(item.get("afterhold")) == 1){ //中目標・保留中
                                    edit += String.format("中目標の[(%s)-%s(保)]に変更しました",item.get("afterbigtitle"),item.get("aftertitle"));
                                }else{
                                    edit += String.format("中目標の[(%s)-%s]に変更しました",item.get("afterbigtitle"),item.get("aftertitle"));
                                }
                            }

                        }else if(item.get("beforelevel").equals("small")){//小目標データ編集

                            if(Integer.parseInt(item.get("beforebighold"))==1){ //大目標・保留
                                edit = String.format("小目標の[(%s(保))-(%s(保))-%s(保)]を",item.get("beforebigtitle"),item.get("beforemiddletitle"),item.get("beforetitle"));
                            }else{//大目標・保留なし
                                if(Integer.parseInt(item.get("beforemiddlehold"))==1){//中目標・保留
                                    edit = String.format("小目標の[(%s)-(%s(保))-%s(保)]を",item.get("beforebigtitle"),item.get("beforemiddletitle"),item.get("beforetitle"));
                                }else{//中目標・保留無し
                                    if(Integer.parseInt(item.get("beforehold"))==1){//小目標・保留中
                                        edit = String.format("小目標の[(%s)-(%s)-%s(保)]を",item.get("beforebigtitle"),item.get("beforemiddletitle"),item.get("beforetitle"));
                                    }else{//小目標・保留なし
                                        edit = String.format("小目標の[(%s)-(%s)-%s]を",item.get("beforebigtitle"),item.get("beforemiddletitle"),item.get("beforetitle"));
                                    }
                                }
                            }
                            if(Integer.parseInt(item.get("afterbighold"))==1){ //大目標・保留
                                edit += String.format("小目標の[(%s(保))-(%s(保))-%s(保)]に変更しました",item.get("afterbigtitle"),item.get("aftermiddletitle"),item.get("aftertitle"));
                            }else{//大目標・保留なし
                                if(Integer.parseInt(item.get("aftermiddlehold"))==1){//中目標・保留
                                    edit += String.format("小目標の[(%s)-(%s(保))-%s(保)]に変更しました",item.get("afterbigtitle"),item.get("aftermiddletitle"),item.get("aftertitle"));
                                }else{//中目標・保留無し
                                    if(Integer.parseInt(item.get("afterhold"))==1){//小目標・保留中
                                        edit += String.format("小目標の[(%s)-(%s)-%s(保)]に変更しました",item.get("afterbigtitle"),item.get("aftermiddletitle"),item.get("aftertitle"));
                                    }else{//小目標・保留なし
                                        edit += String.format("小目標の[(%s)-(%s)-%s]に変更しました",item.get("afterbigtitle"),item.get("aftermiddletitle"),item.get("aftertitle"));
                                    }
                                }

                            }

                        }else if(item.get("beforelevel").equals("schedule")){//スケジュールデータ編集
                            if(Integer.parseInt(item.get("beforehold"))==1){
                                edit = String.format("スケジュールの[%s[%s](保)]を",item.get("beforetitle"),item.get("beforedate"));
                            }else{
                                edit = String.format("スケジュールの[%s(%s)]を",item.get("beforetitle"),item.get("beforedate"));
                            }
                            if(Integer.parseInt(item.get("afterhold"))==1){
                                edit += String.format("スケジュールの[%s[%s](保)]に変更しました",item.get("aftertitle"),item.get("afterdate"));
                            }else{
                                edit += String.format("スケジュールの[%s(%s)]に変更しました",item.get("aftertitle"),item.get("afterdate"));
                            }
                        }else if(item.get("beforelevel").equals("todo")){//やることリスト編集

                            if(Integer.parseInt(item.get("beforehold"))==1){
                                edit = String.format("やることリストの[%s(保)]を",item.get("beforetitle"));
                            }else{
                                edit = String.format("やることリストの[%s]を",item.get("beforetitle"));
                            }
                            if(Integer.parseInt(item.get("afterhold"))==1){
                                edit += String.format("やることリストの[%s(保)]に変更しました",item.get("aftertitle"));
                            }else{
                                edit += String.format("やることリストの[%s]に変更しました",item.get("aftertitle"));
                            }
                        }

                        logTitle.add(edit);

                    }else if(item.get("ope").equals("delete")){ //データ削除
                        if(item.get("beforelevel").equals("big")){ //大目標を削除したとき
                            if(Integer.parseInt(item.get("beforehold"))==1){ //データが保留状態のとき
                                logTitle.add(String.format("大目標に[%s(保)]を削除しました",item.get("beforetitle")));
                            }else{ //データが非保留状態のとき
                                logTitle.add(String.format("大目標に[%s]を削除しました",item.get("beforetitle")));
                            }
                        }else if(item.get("beforelevel").equals("middle")){ //中目標・挿入
                            if(Integer.parseInt(item.get("beforebighold"))==1){ //大目標・保留中
                                logTitle.add(String.format("中目標に[(%s(保))-%s(保)]を削除しました",item.get("beforebigtitle"),item.get("beforetitle")));
                            }else{ //大目標・非保留中
                                if(Integer.parseInt(item.get("beforehold")) == 1){ //中目標・保留中
                                    logTitle.add(String.format("中目標に[(%s)-%s(保)]を削除しました",item.get("beforebigtitle"),item.get("beforetitle")));
                                }else{
                                    logTitle.add(String.format("中目標に[(%s)-%s]を削除しました",item.get("beforebigtitle"),item.get("beforetitle")));
                                }
                            }
                        }else if(item.get("beforelevel").equals("small")){ //小目標・挿入
                            if(Integer.parseInt(item.get("beforebighold"))==1){ //大目標・保留
                                logTitle.add(String.format("小目標に[(%s(保))-(%s(保))-%s(保)]を削除しました",item.get("beforebigtitle"),item.get("beforemiddletitle"),item.get("beforetitle")));
                            }else{//大目標・保留なし
                                if(Integer.parseInt(item.get("beforemiddlehold"))==1){//中目標・保留
                                    logTitle.add(String.format("小目標に[(%s)-(%s(保))-%s(保)]を削除しました",item.get("beforebigtitle"),item.get("beforemiddletitle"),item.get("beforetitle")));
                                }else{//中目標・保留無し
                                    if(Integer.parseInt(item.get("beforehold"))==1){//小目標・保留中
                                        logTitle.add(String.format("小目標に[(%s)-(%s)-%s(保)]を削除しました",item.get("beforebigtitle"),item.get("beforemiddletitle"),item.get("beforetitle")));
                                    }else{//小目標・保留なし
                                        logTitle.add(String.format("小目標に[(%s)-(%s)-%s]を削除しました",item.get("beforebigtitle"),item.get("beforemiddletitle"),item.get("beforetitle")));

                                    }
                                }

                            }
                        }else if(item.get("beforelevel").equals("schedule")){//スケジュール・挿入
                            if(Integer.parseInt(item.get("beforehold"))==1){
                                logTitle.add(String.format("スケジュールに[%s[%s](保)]を削除しました",item.get("beforetitle"),item.get("beforedate")));
                            }else{
                                logTitle.add(String.format("スケジュールに[%s(%s)]を削除しました",item.get("beforetitle"),item.get("beforedate")));
                            }
                        }else if(item.get("beforelevel").equals("todo")){//やることリスト・挿入
                            if(Integer.parseInt(item.get("beforehold"))==1){
                                logTitle.add(String.format("やることリストに[%s(保)]を削除しました",item.get("beforetitle")));
                            }else{
                                logTitle.add(String.format("やることリストに[%s]を削除しました",item.get("beforetitle")));
                            }
                        }
                    }

                    next = logcs.moveToNext();
                }

                //トランザクション成功
                db.setTransactionSuccessful();
            }catch(SQLException e){
                e.printStackTrace();
            }finally{
                //トランザクションを終了
                db.endTransaction();
            }
        }
    }

}
