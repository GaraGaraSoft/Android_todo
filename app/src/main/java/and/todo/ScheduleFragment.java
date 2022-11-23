package and.todo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("all") //ToDo　後で削除してできるかぎり警告文修正
public class ScheduleFragment extends Fragment implements ProgressDialogFragment.ProgressDialogListener,DelDialogFragment.DelDialogListener{
    private ArrayList<HashMap<String,String>> futureScheData = new ArrayList<>();//今後の予定
    private ArrayList<String> futureScheTitle = new ArrayList<>();
    private ArrayList<HashMap<String,String>> todayScheData = new ArrayList<>();//本日の予定
    private ArrayList<String> todayScheTitle = new ArrayList<>();
    private ArrayList<HashMap<String,String>> pastScheData = new ArrayList<>();//過去の予定
    private ArrayList<String> pastScheTitle = new ArrayList<>();
    private EditDatabaseHelper helper;
    private RecyclerView futureList,todayList,pastList;
    private int futureid=0,todayid=0,pastid=0; //選択したID
    private ImageButton futureEdit,futureDel,todayEdit,todayDel,pastEdit,pastDel;
    int futureDelIndex=0,todayDelIndex=0,pastDelIndex=0;//データ削除時配列からも削除するためのインデックス変数
    private String dlevel = ""; //削除データの項目変数
    private int did = 0; //削除データのID変数
    Bundle sendData;
    private boolean progress = false;
    private boolean content = false;
    private int progressLevel = 0;

    public static ScheduleFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        ScheduleFragment fragment = new ScheduleFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }

    @Nullable
    @Override //フラグメントのviewを作成する処理
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_sche,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        helper = new EditDatabaseHelper(requireActivity());

        getSchedule(); //スケジュールデータを取得する


        if(futureScheData.size()==0){ //スケジュールデータがない時レイアウトを消す
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.futureScheduleLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("今後のスケジュール予定なし");
        }else {

            //今後のスケジュールリストにデータ設定
            futureList = view.findViewById(R.id.futureScheduleList);
            futureList.setHasFixedSize(true);
            LinearLayoutManager rLayoutManager = new LinearLayoutManager(requireActivity());
            rLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); //縦方向に設定

            futureList.setLayoutManager(rLayoutManager);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(requireActivity(), rLayoutManager.getOrientation());
            futureList.addItemDecoration(itemDecoration);


            MyAdapter adapter = new MyAdapter(futureScheTitle){//リストクリック時の処理
                @Override
                void onRecycleItemClick(View view, int position, String itemData) {
                    onFutureItemClick(view,position,itemData);
                }
            };
            futureList.setAdapter(adapter);

            //LayoutParamsを取得
            ViewGroup.LayoutParams params = futureList.getLayoutParams();
            if (futureScheData.size() == 1) { //リストの項目数で高さを変える
                params.height = 100;
            } else if (futureScheData.size() == 2) {
                params.height = 200;
            } else if(futureScheData.size() == 3){
                params.height = 300;
            } else if(futureScheData.size() == 4){
                params.height = 400;
            }else if(futureScheData.size() == 5){
                params.height = 500;
            }else if (futureScheData.size() == 6) {
                params.height = 600;
            } else if(futureScheData.size() == 7){
                params.height = 700;
            }else if(futureScheData.size() == 8){
                params.height = 800;
            }else if (futureScheData.size() >= 9) {
                params.height = 900;
            }
            futureList.setLayoutParams(params);

            futureEdit = (ImageButton) view.findViewById(R.id.futureEditButton);
            futureEdit.setEnabled(false); //項目選択まで無効化
            futureEdit.setOnClickListener(new editClicker());
            futureDel = (ImageButton) view.findViewById(R.id.futureDelButton);
            futureDel.setEnabled(false); //項目選択まで削除ボタン無効化
            futureDel.setOnClickListener(new editClicker());

        }

        if(todayScheData.size()==0){ //スケジュールデータがない時レイアウトを消す
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.todayListLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("本日のスケジュール予定なし");
        }else {

            //本日のスケジュールデータ設定
            todayList = view.findViewById(R.id.todayScheduleList);
            todayList.setHasFixedSize(true);
            LinearLayoutManager rLayoutManager = new LinearLayoutManager(requireActivity());
            rLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); //縦方向に設定

            todayList.setLayoutManager(rLayoutManager);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(requireActivity(), rLayoutManager.getOrientation());
            todayList.addItemDecoration(itemDecoration);


            MyAdapter adapter = new MyAdapter(todayScheTitle){//リストクリック時の処理
                @Override
                void onRecycleItemClick(View view, int position, String itemData) {
                    onTodayItemClick(view,position,itemData);
                }
            };
            todayList.setAdapter(adapter);


            //LayoutParamsを取得
            ViewGroup.LayoutParams params = todayList.getLayoutParams();
            if (todayScheData.size() == 1) { //リストの項目数で高さを変える
                params.height = 100;
            } else if (todayScheData.size() == 2) {
                params.height = 200;
            } else if(todayScheData.size() == 3){
                params.height = 300;
            } else if(todayScheData.size() == 4){
                params.height = 400;
            }else if(todayScheData.size() == 5){
                params.height = 500;
            }else if (todayScheData.size() == 6) {
                params.height = 600;
            } else if(todayScheData.size() == 7){
                params.height = 700;
            }else if(todayScheData.size() == 8){
                params.height = 800;
            }else if (todayScheData.size() >= 9) {
                params.height = 900;
            }
            todayList.setLayoutParams(params);

            todayEdit = (ImageButton) view.findViewById(R.id.todayEditButton);
            todayEdit.setEnabled(false); //項目選択まで無効化
            todayEdit.setOnClickListener(new editClicker());
            todayDel = (ImageButton) view.findViewById(R.id.todayDelButton);
            todayDel.setEnabled(false); //項目選択まで削除ボタン無効化
            todayDel.setOnClickListener(new editClicker());

        }


        if(pastScheData.size()==0){ //過去スケジュールがない時レイアウトを消す
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.pastScheduleLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("過去スケジュールなし");
        }else {

            //過去のスケジュールデータ設定
            pastList = view.findViewById(R.id.pastScheduleList);
            pastList.setHasFixedSize(true);
            LinearLayoutManager  rLayoutManager = new LinearLayoutManager(requireActivity());
            rLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); //縦方向に設定

            pastList.setLayoutManager(rLayoutManager);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(requireActivity(), rLayoutManager.getOrientation());
            pastList.addItemDecoration(itemDecoration);


            MyAdapter adapter = new MyAdapter(pastScheTitle){//リストクリック時の処理
                @Override
                void onRecycleItemClick(View view, int position, String itemData) {
                    onPastItemClick(view,position,itemData);
                }
            };
            pastList.setAdapter(adapter);

            //LayoutParamsを取得
            ViewGroup.LayoutParams params = pastList.getLayoutParams();
            if (pastScheData.size() == 1) { //リストの項目数で高さを変える
                params.height = 100;
            } else if (pastScheData.size() == 2) {
                params.height = 200;
            } else if(pastScheData.size() == 3){
                params.height = 300;
            } else if(pastScheData.size() == 4){
                params.height = 400;
            }else if(pastScheData.size() == 5){
                params.height = 500;
            }else if (pastScheData.size() == 6) {
                params.height = 600;
            } else if(pastScheData.size() == 7){
                params.height = 700;
            }else if(pastScheData.size() == 8){
                params.height = 800;
            }else if (pastScheData.size() >= 9) {
                params.height = 900;
            }
            pastList.setLayoutParams(params);

            pastEdit = (ImageButton) view.findViewById(R.id.pastEditButton);
            pastEdit.setEnabled(false); //項目選択まで無効化
            pastEdit.setOnClickListener(new editClicker());
            pastDel = (ImageButton) view.findViewById(R.id.pastDelButton);
            pastDel.setEnabled(false); //項目選択まで削除ボタン無効化
            pastDel.setOnClickListener(new editClicker());

        }

        CustomSpinner cspinner = view.findViewById(R.id.modeChange); //編集モード選択スピナー取得
        String[] spinnerItems = { "モード選択","標準モード", "内容表示モード", "進捗編集モード" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item,
                spinnerItems);
        cspinner.setAdapter(adapter);
        cspinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) { //大目標選択時のID取得
                if(position==1){ //標準モード（選択しても表示されない）
                    content = false;
                    progress = false;
                }else if(position==2){ //内容表示モード（項目の内容をトースト表示）
                    content = true;
                    progress = false;
                }else if(position==3){//進捗編集モード（選択した項目の進捗状況を編集）
                    content = false;
                    progress = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }


    public void onFutureItemClick(View view,int position,String itemData){//今後のスケジュール項目選択時のID取得,処理
        futureid = Integer.parseInt( futureScheData.get(position).get("id"));
        futureDelIndex = position;
        if(content){ //内容表示モード
            Toast.makeText(requireActivity(),futureScheData.get(position).get("content"),Toast.LENGTH_LONG).show();
        }

        if(progress) {
            progressLevel = 1;
            // フラグメントマネージャーを取得
            FragmentManager fragmentManager = getParentFragmentManager();

            Bundle data = new Bundle();
            data.putString("editcontent", futureScheData.get(position).get("memo"));
            data.putInt("editProg", Integer.parseInt(futureScheData.get(position).get("proceed")));
            data.putInt("editFin", Integer.parseInt(futureScheData.get(position).get("fin")));
            data.putString("editTitle", futureScheTitle.get(position));
            data.putInt("id", futureid);
            ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(data);
            dialog.setTargetFragment(ScheduleFragment.this, 0);

            dialog.show(fragmentManager,"dialog_progress");
        }
        futureEdit.setEnabled(true);
        futureDel.setEnabled(true);
    }

    public void onTodayItemClick(View view,int position,String itemData){//本日のスケジュール標項目選択時のID取得,処理
        todayid = Integer.parseInt( todayScheData.get(position).get("id"));
        todayDelIndex = position;
        if(content){ //内容表示モード
            Toast.makeText(requireActivity(),todayScheData.get(position).get("content"),Toast.LENGTH_LONG).show();
        }
        //ToDo 進捗状況入力ダイアログ
        if(progress) {
            progressLevel = 2;
            // フラグメントマネージャーを取得
            FragmentManager fragmentManager = getParentFragmentManager();

            Bundle data = new Bundle();
            data.putString("editcontent", todayScheData.get(position).get("memo"));
            data.putInt("editProg", Integer.parseInt(todayScheData.get(position).get("proceed")));
            data.putInt("editFin", Integer.parseInt(todayScheData.get(position).get("fin")));
            data.putString("editTitle", todayScheTitle.get(position));
            data.putInt("id", todayid);
            ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(data);
            dialog.setTargetFragment(ScheduleFragment.this, 0);

            dialog.show(fragmentManager,"dialog_progress");
        }
        todayEdit.setEnabled(true);
        todayDel.setEnabled(true);
    }

    public void onPastItemClick(View view,int position,String itemData){//過去スケジュール項目選択時のID取得,処理
        pastid = Integer.parseInt( pastScheData.get(position).get("id"));
        pastDelIndex = position;
        if(content){ //内容表示モード
            Toast.makeText(requireActivity(),pastScheData.get(position).get("content"),Toast.LENGTH_LONG).show();
        }
        //ToDo 進捗状況入力ダイアログ
        if(progress) {
            progressLevel = 3;
            // フラグメントマネージャーを取得
            FragmentManager fragmentManager = getParentFragmentManager();

            Bundle data = new Bundle();
            data.putString("editcontent", pastScheData.get(position).get("memo"));
            data.putInt("editProg", Integer.parseInt(pastScheData.get(position).get("proceed")));
            data.putInt("editFin", Integer.parseInt(pastScheData.get(position).get("fin")));
            data.putString("editTitle", pastScheTitle.get(position));
            data.putInt("id", pastid);
            ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(data);
            dialog.setTargetFragment(ScheduleFragment.this, 0);

            dialog.show(fragmentManager,"dialog_progress");
        }
        pastEdit.setEnabled(true);
        pastDel.setEnabled(true);
    }


    //データ削除確定時の処理
    @Override
    public void onDelDialogPositiveClick(DialogFragment dialog) {
        DeleteData del = new DeleteData(requireActivity()); //削除用クラスのインスタンス生成
        del.delete("schedule",did);

                if(dlevel.equals("fSchedule")){ //データ削除成功時、配列からも消す
                        futureScheData.remove(futureDelIndex);
                        futureScheTitle.remove(futureDelIndex);

                    MyAdapter adapter = new MyAdapter(futureScheTitle){//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onFutureItemClick(view,position,itemData);
                        }
                    };
                    futureList.setAdapter(adapter);
                    futureid = 0;
                    futureEdit.setEnabled(false);//ボタンを無効化
                    futureDel.setEnabled(false);
                    futureDelIndex=0; //削除するデータのインデックスリセット

                }
               else if(dlevel.equals("tSchedule")){ //データ削除成功時、配列からも消す
                    todayScheData.remove(todayDelIndex);
                    todayScheTitle.remove(todayDelIndex);

                    MyAdapter adapter = new MyAdapter(todayScheTitle){//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onTodayItemClick(view,position,itemData);
                        }
                    };
                    todayList.setAdapter(adapter);
                    todayid = 0;
                    todayEdit.setEnabled(false);//ボタンを無効化
                    todayDel.setEnabled(false);
                    todayDelIndex=0; //削除するデータのインデックスリセット

                }
                else if(dlevel.equals("pSchedule")){ //データ削除成功時、配列からも消す
                    pastScheData.remove(pastDelIndex);
                    pastScheTitle.remove(pastDelIndex);

                    MyAdapter adapter = new MyAdapter(pastScheTitle){//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onPastItemClick(view,position,itemData);
                        }
                    };
                    pastList.setAdapter(adapter);
                    pastid = 0;
                    pastEdit.setEnabled(false);//ボタンを無効化
                    pastDel.setEnabled(false);
                    pastDelIndex=0; //削除するデータのインデックスリセット

                }
    }

    @Override
    public void onDelDialogNegativeClick(DialogFragment dialog) { //

    }

    @Override
    public void onDelDialogNeutralClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, Bundle data) {

        String memo = data.getString("editcontent");
        int proceed = data.getInt("editProg");
        int id = data.getInt("id");
        int fin = data.getInt("fin");

        ContentValues cv = new ContentValues();
        try(SQLiteDatabase db = helper.getReadableDatabase()){
            Cursor cs = db.query("ToDoData",null,"id=?",new String[]{""+id},null,null,null,null );

            int ltitle = cs.getColumnIndex("title");
            int lcontent = cs.getColumnIndex("content");
            int llevel = cs.getColumnIndex("level");
            int lbig = cs.getColumnIndex("big");
            int lbigtitle = cs.getColumnIndex("bigtitle");
            int lbighold = cs.getColumnIndex("bighold");
            int lmiddle = cs.getColumnIndex("middle");
            int lmiddletitle = cs.getColumnIndex("middletitle");
            int lmiddlehold = cs.getColumnIndex("middlehold");
            int ldate = cs.getColumnIndex("date");
            int lhold = cs.getColumnIndex("hold");
            int limportant = cs.getColumnIndex("important");
            int lmemo = cs.getColumnIndex("memo");
            int lproceed = cs.getColumnIndex("proceed");
            int lfin = cs.getColumnIndex("fin");

            if(cs.moveToFirst()){
                cv.put("id", id);
                cv.put("title",cs.getString(ltitle));
                cv.put("level",cs.getString(llevel));
                cv.put("hold",cs.getInt(lhold));
                cv.put("big",cs.getInt(lbig));
                cv.put("bigtitle",cs.getString(lbigtitle));
                cv.put("bighold",cs.getInt(lbighold));
                cv.put("middle",cs.getInt(lmiddle));
                cv.put("middletitle",cs.getString(lmiddletitle));
                cv.put("middlehold",cs.getInt(lmiddlehold));
                cv.put("date",cs.getString(ldate));
                cv.put("content",cs.getString(lcontent));
                cv.put("important",cs.getInt(limportant));
                cv.put("memo", memo);
                cv.put("proceed", proceed);
                cv.put("fin",fin);

            }
        }

        //データベースに反映
        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.beginTransaction();
            try {
                db.insertWithOnConflict("ToDoData", null, cv, SQLiteDatabase.CONFLICT_REPLACE);


                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }
        if(progressLevel == 1){ //今後のスケジュール進捗編集時データ配列を再読み込み

            try(SQLiteDatabase db = helper.getReadableDatabase()){
                //トランザクション開始
                db.beginTransaction();
                try{

                    //スケジュールを取得
                    String[] schecols = {"id","title","date","content","important","memo","proceed","fin","hold"};//SQLデータから取得する列
                    String[] schelevel = { "schedule" };//スケジュールデータを抽出
                    //スケジュールをもっとも先の日付から順に取得
                    Cursor schecs = db.query("ToDoData",schecols,"level=?",schelevel,null,null,"date desc",null);

                    //今後のスケジュール配列を空にする
                    futureScheData.clear();
                    futureScheTitle.clear();
                    int scheduleChange = 0; //スケジュール配列切替え用変数

                    //Calendarクラスのオブジェクトを生成する
                    Calendar cl = Calendar.getInstance();
                    //本日の日付データを取得
                    String today = String.format(Locale.JAPAN,"%02d/%02d/%02d",cl.get(Calendar.YEAR),cl.get(Calendar.MONTH)+1,cl.get(Calendar.DATE));

                    boolean next = schecs.moveToFirst();//カーソルの先頭に移動
                    while(next){

                        int schedulenum = schecs.getString(2).compareTo(today);
                        if(schedulenum>0){//今後のスケジュール
                            HashMap<String,String> item = new HashMap<>();
                            item.put("id",""+schecs.getInt(0));
                            item.put("title",schecs.getString(1));
                            item.put("date",schecs.getString(2));
                            item.put("content",schecs.getString(3));
                            item.put("important",""+schecs.getInt(4));
                            item.put("memo",schecs.getString(5));
                            item.put("proceed",""+schecs.getInt(6));
                            item.put("fin",""+schecs.getInt(7));
                            item.put("hold",""+schecs.getInt(8));
                            futureScheData.add(item); //スケジュールデータ配列に追加
                            if(Integer.parseInt(item.get("fin"))==1){//完了済みデータ
                                futureScheTitle.add(String.format("%s[%s](済)",item.get("title"),item.get("date")));
                            }else{//
                                futureScheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                            }

                        }

                        next = schecs.moveToNext();
                    }

                    MyAdapter adapter = new MyAdapter(futureScheTitle){//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onFutureItemClick(view,position,itemData);
                        }
                    };
                    futureList.setAdapter(adapter);
                    futureEdit.setEnabled(false);
                    futureDel.setEnabled(false);

                    //トランザクション成功
                    db.setTransactionSuccessful();
                }catch(SQLException e){
                    e.printStackTrace();
                }finally{
                    //トランザクションを終了
                    db.endTransaction();
                }
            }

        }else if(progressLevel == 2){ //本日のスケジュール進捗編集時データ配列を再読み込み

            try(SQLiteDatabase db = helper.getReadableDatabase()){
                //トランザクション開始
                db.beginTransaction();
                try{

                    //スケジュールを取得
                    String[] schecols = {"id","title","date","content","important","memo","proceed","fin","hold"};//SQLデータから取得する列
                    String[] schelevel = { "schedule" };//スケジュールデータを抽出
                    //スケジュールをもっとも先の日付から順に取得
                    Cursor schecs = db.query("ToDoData",schecols,"level=?",schelevel,null,null,"date desc",null);

                    //今後のスケジュール配列を空にする
                    todayScheData.clear();
                    todayScheTitle.clear();
                    int scheduleChange = 0; //スケジュール配列切替え用変数

                    //Calendarクラスのオブジェクトを生成する
                    Calendar cl = Calendar.getInstance();
                    //本日の日付データを取得
                    String today = String.format(Locale.JAPAN,"%02d/%02d/%02d",cl.get(Calendar.YEAR),cl.get(Calendar.MONTH)+1,cl.get(Calendar.DATE));

                    boolean next = schecs.moveToFirst();//カーソルの先頭に移動
                    while(next){

                        int schedulenum = schecs.getString(2).compareTo(today);
                        if(schedulenum==0){//当日のスケジュール
                            HashMap<String,String> item = new HashMap<>();
                            item.put("id",""+schecs.getInt(0));
                            item.put("title",schecs.getString(1));
                            item.put("date",""+schecs.getString(2));
                            item.put("content",schecs.getString(3));
                            item.put("important",""+schecs.getInt(4));
                            item.put("memo",schecs.getString(5));
                            item.put("proceed",""+schecs.getInt(6));
                            item.put("fin",""+schecs.getInt(7));
                            item.put("hold",""+schecs.getInt(8));
                            todayScheData.add(item); //スケジュールデータ配列に追加
                            if(Integer.parseInt(item.get("fin"))==1){//完了済みデータ
                                todayScheTitle.add(String.format("%s[%s](済)",item.get("title"),item.get("date")));
                            }else{
                                todayScheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                            }

                        }

                        next = schecs.moveToNext();
                    }

                    MyAdapter adapter = new MyAdapter(todayScheTitle){//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onTodayItemClick(view,position,itemData);
                        }
                    };
                    todayList.setAdapter(adapter);
                    todayEdit.setEnabled(false);
                    todayDel.setEnabled(false);

                    //トランザクション成功
                    db.setTransactionSuccessful();
                }catch(SQLException e){
                    e.printStackTrace();
                }finally{
                    //トランザクションを終了
                    db.endTransaction();
                }
            }
        }else if(progressLevel == 3){ //過去のスケジュール進捗編集時データ配列を再読み込み

            try(SQLiteDatabase db = helper.getReadableDatabase()){
                //トランザクション開始
                db.beginTransaction();
                try{

                    //スケジュールを取得
                    String[] schecols = {"id","title","date","content","important","memo","proceed","fin","hold"};//SQLデータから取得する列
                    String[] schelevel = { "schedule" };//スケジュールデータを抽出
                    //スケジュールをもっとも先の日付から順に取得
                    Cursor schecs = db.query("ToDoData",schecols,"level=?",schelevel,null,null,"date desc",null);

                    //今後のスケジュール配列を空にする
                    pastScheData.clear();
                    pastScheTitle.clear();
                    int scheduleChange = 0; //スケジュール配列切替え用変数

                    //Calendarクラスのオブジェクトを生成する
                    Calendar cl = Calendar.getInstance();
                    //本日の日付データを取得
                    String today = String.format(Locale.JAPAN,"%02d/%02d/%02d",cl.get(Calendar.YEAR),cl.get(Calendar.MONTH)+1,cl.get(Calendar.DATE));

                    boolean next = schecs.moveToFirst();//カーソルの先頭に移動
                    while(next){

                        int schedulenum = schecs.getString(2).compareTo(today);
                        if(schedulenum<0){//当日のスケジュール
                            HashMap<String,String> item = new HashMap<>();
                            item.put("id",""+schecs.getInt(0));
                            item.put("title",schecs.getString(1));
                            item.put("date",""+schecs.getString(2));
                            item.put("content",schecs.getString(3));
                            item.put("important",""+schecs.getInt(4));
                            item.put("memo",schecs.getString(5));
                            item.put("proceed",""+schecs.getInt(6));
                            item.put("fin",""+schecs.getInt(7));
                            item.put("hold",""+schecs.getInt(8));
                            pastScheData.add(item); //スケジュールデータ配列に追加
                            if(Integer.parseInt(item.get("fin"))==1){//完了済みデータ
                                pastScheTitle.add(String.format("%s[%s](済)",item.get("title"),item.get("date")));
                            }else{
                                pastScheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                            }

                        }

                        next = schecs.moveToNext();
                    }

                    MyAdapter adapter = new MyAdapter(pastScheTitle){//リストクリック時の処理
                        @Override
                        void onRecycleItemClick(View view, int position, String itemData) {
                            onPastItemClick(view,position,itemData);
                        }
                    };
                    pastList.setAdapter(adapter);
                    pastEdit.setEnabled(false);
                    pastDel.setEnabled(false);

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

    class editClicker implements View.OnClickListener { //編集ボタンクリックで編集画面へ飛ばす

        Bundle editData = new Bundle(); //データ送信用
        @Override
        public void onClick(View view) {
            if(view == futureEdit ){ //スケジュール編集ボタン
                editData.putString("level","schedule");
                editData.putInt("id",futureid);

                //編集画面へ飛ばす
                // BackStackを設定
                FragmentManager fragmentManager = getParentFragmentManager();
                // FragmentTransactionのインスタンスを取得
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);

                // パラメータを設定
                fragmentTransaction.replace(R.id.MainFragment,
                        EditFragment.newInstance(editData));
                fragmentTransaction.commit();

            }else if(view == todayEdit){
                editData.putString("level","schedule");
                editData.putInt("id",todayid);

                //編集画面へ飛ばす
                // BackStackを設定
                FragmentManager fragmentManager = getParentFragmentManager();
                // FragmentTransactionのインスタンスを取得
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);

                // パラメータを設定
                fragmentTransaction.replace(R.id.MainFragment,
                        EditFragment.newInstance(editData));
                fragmentTransaction.commit();

            }else if(view == pastEdit){
                editData.putString("level","schedule");
                editData.putInt("id",pastid);

                //編集画面へ飛ばす
                // BackStackを設定
                FragmentManager fragmentManager = getParentFragmentManager();
                // FragmentTransactionのインスタンスを取得
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);

                // パラメータを設定
                fragmentTransaction.replace(R.id.MainFragment,
                        EditFragment.newInstance(editData));
                fragmentTransaction.commit();

            }else if(view == futureDel){//今後のスケジュールデータ削除
                editData.putString("title",futureScheTitle.get(futureDelIndex));
                editData.putString("level","schedule");
                dlevel = "fSchedule";
                did = futureid;

                // 削除確認ダイアログへ飛ぶ

                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                DelDialogFragment dialog = DelDialogFragment.newInstance(editData);
                dialog.setTargetFragment(ScheduleFragment.this, 0);

                dialog.show(fragmentManager,"dialog_delete");

            }else if(view == todayDel){//本日のスケジュールデータ削除
                editData.putString("title",todayScheTitle.get(todayDelIndex));
                editData.putString("level","schedule");
                dlevel = "tSchedule";
                did = todayid;
                // 削除確認ダイアログへ飛ぶ

                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                DelDialogFragment dialog = DelDialogFragment.newInstance(editData);
                dialog.setTargetFragment(ScheduleFragment.this, 0);

                dialog.show(fragmentManager,"dialog_delete");

            }else if(view == pastDel){//過去のスケジュールデータ削除

                editData.putString("title",pastScheTitle.get(pastDelIndex));
                editData.putString("level","schedule");
                dlevel = "pSchedule";
                did = pastid;
                //ToDo 削除確認ダイアログへ飛ぶ

                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                DelDialogFragment dialog = DelDialogFragment.newInstance(editData);
                dialog.setTargetFragment(ScheduleFragment.this, 0);

                dialog.show(fragmentManager,"dialog_delete");

            }

        }
    }


    void getSchedule(){ //データベースから取得してデータ配列に挿入する

        try(SQLiteDatabase db = helper.getReadableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try{

                //スケジュールを取得
                String[] schecols = {"id","title","date","content","important","memo","proceed","fin","hold"};//SQLデータから取得する列
                String[] schelevel = { "schedule" };//スケジュールデータを抽出
                //スケジュールをもっとも先の日付から順に取得
                Cursor schecs = db.query("ToDoData",schecols,"level=?",schelevel,null,null,"date desc",null);

                //各スケジュール配列を空にする
                futureScheData.clear();
                futureScheTitle.clear();
                todayScheData.clear();
                todayScheTitle.clear();
                pastScheData.clear();
                pastScheTitle.clear();
                int scheduleChange = 0; //スケジュール配列切替え用変数

                //Calendarクラスのオブジェクトを生成する
                Calendar cl = Calendar.getInstance();
                //本日の日付データを取得
                String today = String.format(Locale.JAPAN,"%02d/%02d/%02d",cl.get(Calendar.YEAR),cl.get(Calendar.MONTH)+1,cl.get(Calendar.DATE));

                boolean next = schecs.moveToFirst();//カーソルの先頭に移動
                while(next){

                    int schedulenum = schecs.getString(2).compareTo(today);
                    if(schedulenum>0){//今後のスケジュール
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+schecs.getInt(0));
                        item.put("title",schecs.getString(1));
                        item.put("date",""+schecs.getString(2));
                        item.put("content",schecs.getString(3));
                        item.put("important",""+schecs.getInt(4));
                        item.put("memo",schecs.getString(5));
                        item.put("proceed",""+schecs.getInt(6));
                        item.put("fin",""+schecs.getInt(7));
                        item.put("hold",""+schecs.getInt(8));
                        futureScheData.add(item); //スケジュールデータ配列に追加
                        if(Integer.parseInt(item.get("fin"))==1){//完了済みデータ
                            futureScheTitle.add(String.format("%s[%s](済)",item.get("title"),item.get("date")));
                        }else{//
                            futureScheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                        }

                    }else if(schedulenum==0){//当日のスケジュール
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+schecs.getInt(0));
                        item.put("title",schecs.getString(1));
                        item.put("date",""+schecs.getString(2));
                        item.put("content",schecs.getString(3));
                        item.put("important",""+schecs.getInt(4));
                        item.put("memo",schecs.getString(5));
                        item.put("proceed",""+schecs.getInt(6));
                        item.put("fin",""+schecs.getInt(7));
                        item.put("hold",""+schecs.getInt(8));
                        todayScheData.add(item); //スケジュールデータ配列に追加
                        if(Integer.parseInt(item.get("fin"))==1){//完了済みデータ
                            todayScheTitle.add(String.format("%s[%s](済)",item.get("title"),item.get("date")));
                        }else{
                            todayScheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                        }

                    }else {//過去のスケジュール
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+schecs.getInt(0));
                        item.put("title",schecs.getString(1));
                        item.put("date",""+schecs.getString(2));
                        item.put("content",schecs.getString(3));
                        item.put("important",""+schecs.getInt(4));
                        item.put("memo",schecs.getString(5));
                        item.put("proceed",""+schecs.getInt(6));
                        item.put("fin",""+schecs.getInt(7));
                        item.put("hold",""+schecs.getInt(8));
                        pastScheData.add(item); //スケジュールデータ配列に追加
                        if(Integer.parseInt(item.get("fin"))==1){//完了済みデータ
                            pastScheTitle.add(String.format("%s[%s](済)",item.get("title"),item.get("date")));
                        }else{
                            pastScheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                        }

                    }

                    next = schecs.moveToNext();
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
