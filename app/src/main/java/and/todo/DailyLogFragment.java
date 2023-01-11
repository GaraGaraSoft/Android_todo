package and.todo;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class DailyLogFragment extends Fragment implements DailyDialogFragment.DailyDialogListener,DelDialogFragment.DelDialogListener { //日々こなしたタスクをチェック
    private ArrayList<String> DailyLogs; //指定期間のログデータ配列
    private ArrayList<String> DailyContents; //指定期間にこなしたタスク内容のデータ配列
    private ArrayList<Integer> DailyLogId; //ログデータのID配列
    private EditDatabaseHelper helper;
    private int page = 0;//表示するログのページ数
    private Button first,previous,forward,last,logEdit;//ページを移動するボタン要素
    DailyAdapter dAdapter;
    Activity activity;
    TextView lognum;
    ListView dailyLogList;
    boolean Toastable = false,Editable = false,Deletable = false;//モードチェンジ時に利用する変数
    private int selectid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_dailylog,container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity = requireActivity();


        View decor = activity.getWindow().getDecorView();//アクションバー含む全ビュー取得

        ConstraintLayout day = view.findViewById(R.id.dailyLayout);
        day.setOnClickListener(v-> {

            if(decor.getSystemUiVisibility() == 0) {//アクションバーを隠す

                decor.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                |View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }else{//アクションバーを再表示

                decor.setSystemUiVisibility(0);
            }

        });


        //データベースから各データを取得
        helper = new EditDatabaseHelper(activity);
        //データ取得
        getArrayDailyDatas();


        dAdapter = new DailyAdapter(activity,R.layout.daily_list_item, DailyLogs);
        dailyLogList = view.findViewById(R.id.dailyloglist);
        dailyLogList.setAdapter(dAdapter);
        dailyLogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectid = DailyLogId.get(position);
                if(Editable){//編集モード

                    // フラグメントマネージャーを取得
                    FragmentManager fragmentManager = getParentFragmentManager();

                    Bundle data = new Bundle();
                    data.putString("title", DailyLogs.get(position));
                    data.putString("editContent", DailyContents.get(position));
                    data.putInt("id", selectid);
                    DailyDialogFragment dialog = DailyDialogFragment.newInstance(data);
                    dialog.setTargetFragment(DailyLogFragment.this, 0);

                    dialog.show(fragmentManager,"dialog_daily");
                } else if(Toastable){//非編集モード時は内容をトースト表示
                    Toast.makeText(requireActivity(),DailyLogs.get(position)+":\n"+DailyContents.get(position),Toast.LENGTH_LONG).show();

                } else if(Deletable){//削除モード
                    //削除確認ダイアログへ飛ぶ

                    Bundle editData = new Bundle();
                    editData.putString("title",DailyLogs.get(position));
                    // フラグメントマネージャーを取得
                    FragmentManager fragmentManager = getParentFragmentManager();

                    DelDialogFragment dialog = DelDialogFragment.newInstance(editData);
                    dialog.setTargetFragment(DailyLogFragment.this, 0);

                    dialog.show(fragmentManager,"dialog_delete");
                }
            }
        });


        lognum = view.findViewById(R.id.dailyLogNum);
        lognum.setText(String.format("%s から %s の達成ログ", firstDayCal(page), lastDayCal(page)));

        first = view.findViewById(R.id.first); //最初の一週間分を取得して表示
        first.setOnClickListener(new PageChange());
        previous = view.findViewById(R.id.previous); //前の一週間分を取得して表示
        previous.setOnClickListener(new PageChange());
        forward = view.findViewById(R.id.forward);
        forward.setOnClickListener(new PageChange());

        CustomSpinner cspinner = view.findViewById(R.id.modeChange); //編集モード選択スピナー取得
        String[] spinnerItems = { "モード選択","内容表示モード", "内容編集モード","削除モード" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item,
                spinnerItems);
        cspinner.setAdapter(adapter);
        cspinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) { //大目標選択時のID取得
                if(position==1){ //内容表示モード
                    Toastable = true;
                    Deletable = false;
                    Editable = false;
                }else if(position==2){ //内容編集モード（項目の内容を編集）
                    Toastable = false;
                    Deletable = false;
                    Editable = true;
                }else if(position==3){//削除モード（選択した項目を削除）
                    Toastable = false;
                    Deletable = true;
                    Editable = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    String firstDayCal(int page){//ページ最初の日付データを取得
        //Calendarクラスのオブジェクトを生成する
        Calendar cl = Calendar.getInstance();
        cl.add(Calendar.DATE, -7*page);
        String fDay = String.format(Locale.JAPAN,"%02d-%02d-%02d",cl.get(Calendar.YEAR),cl.get(Calendar.MONTH)+1,cl.get(Calendar.DATE));

        return fDay;
    }

    String lastDayCal(int page){//ページ最後の日付データを取得
        //Calendarクラスのオブジェクトを生成する
        Calendar cl = Calendar.getInstance();
        cl.add(Calendar.DATE, -7*page-6);
        String lDay = String.format(Locale.JAPAN,"%02d-%02d-%02d",cl.get(Calendar.YEAR),cl.get(Calendar.MONTH)+1,cl.get(Calendar.DATE));

        return lDay;
    }


    class PageChange implements View.OnClickListener{ //ページ移動時、データベースから再取得して表示
        @Override
        public void onClick(View view) {
            if(view == first){ //最初のページ
                if(page != 0) {
                    page = 0;
                    getArrayDailyDatas();
                    dAdapter = new DailyAdapter(activity,R.layout.daily_list_item, DailyLogs);
                    dailyLogList.setAdapter(dAdapter);
                }
            }else if(view == previous){ //前のページへ戻る
                if(page>0)
                {
                    page--;
                    getArrayDailyDatas();
                    dAdapter = new DailyAdapter(activity,R.layout.daily_list_item, DailyLogs);
                    dailyLogList.setAdapter(dAdapter);
                    lognum.setText(String.format("%s から %s の達成ログ", firstDayCal(page), lastDayCal(page)));

                }
            }else if(view == forward){//次のページへ進む
                page++;
                getArrayDailyDatas();
                dAdapter = new DailyAdapter(activity,R.layout.daily_list_item, DailyLogs);
                dailyLogList.setAdapter(dAdapter);
                lognum.setText(String.format("%s から %s の達成ログ", firstDayCal(page), lastDayCal(page)));
                Log.e("SIZE",""+DailyLogs.size());
            }
        }
    }

    //データ削除確定時の処理
    @Override
    public void onDelDialogPositiveClick(DialogFragment dialog) {
        try(SQLiteDatabase db = helper.getWritableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try {

                String[] params = {"" + selectid};
                db.delete("DailyData", "id=?", params);


                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                //トランザクションを終了
                db.endTransaction();

            }
        }
        //ログの1ページ目に戻す
        page = 0;
        getArrayDailyDatas();
        dAdapter = new DailyAdapter(activity,R.layout.daily_list_item, DailyLogs);
        dailyLogList.setAdapter(dAdapter);
        lognum.setText(String.format("%s から %s の達成ログ", firstDayCal(page), lastDayCal(page)));

    }
    //データ削除キャンセル時
    @Override
    public void onDelDialogNegativeClick(DialogFragment dialog) {
    }
    //データ削除スルー
    @Override
    public void onDelDialogNeutralClick(DialogFragment dialog) {
    }

    @Override
    public void onDailyDialogPositiveClick(DialogFragment dialog, Bundle data) { //こなしたタスク内容編集後の処理

        String content = data.getString("editcontent");
        int id = data.getInt("id");

        ContentValues cv = new ContentValues();
        try(SQLiteDatabase db = helper.getWritableDatabase()){
            db.beginTransaction();
            try{

                Cursor cs = db.query("DailyData",new String[]{"dailytaskid","dailytasktitle","level","date"},"id=?",new String[]{""+id},null,null,null,null );

                if(cs.moveToFirst()){
                    cv.put("id", id);
                    cv.put("dailytaskid",cs.getInt(0));
                    cv.put("dailytasktitle",cs.getString(1));
                    cv.put("dailycontent",content);
                    cv.put("level",cs.getString(2));
                    cv.put("date",cs.getString(3));

                }

                db.insertWithOnConflict("DailyData", null, cv, SQLiteDatabase.CONFLICT_REPLACE);

                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }



        }
        //ログの1ページ目に戻す
        page = 0;
        getArrayDailyDatas();
        dAdapter = new DailyAdapter(activity,R.layout.daily_list_item, DailyLogs);
        dailyLogList.setAdapter(dAdapter);
        lognum.setText(String.format("%s から %s の達成ログ", firstDayCal(page), lastDayCal(page)));

    }

    void getArrayDailyDatas(){

        try(SQLiteDatabase db = helper.getReadableDatabase()){

            //トランザクション開始
            db.beginTransaction();
            try{
                //デイリー完了タスクデータを取得
                String[] dcols = {"dailytasktitle","date","id","dailycontent"};//SQLデータから取得する列
                Cursor dcs = db.query("DailyData",dcols,"date <= date('now','localtime','-"+(page*7)+" day') and date('now', 'localtime', '-"+(page*7+6)+" day') <= date",null,null,null,"date desc",null);

                DailyLogs = new ArrayList<>();//配列再生成
                DailyContents = new ArrayList<>();
                DailyLogId = new ArrayList<>();
                boolean next = dcs.moveToFirst();//カーソルの先頭に移動

                String date="";

                while(next){

                    if(!date.equals(dcs.getString(1))){//日付データ切り替わり時の処理
                        date = dcs.getString(1);
                        DailyLogs.add("#"+date+"にこなしたタスク"); //次の日付の見出しタイトル追加
                        DailyContents.add("");
                        DailyLogId.add(0);
                    }

                    DailyLogs.add(dcs.getString(0)); //こなしたタスクデータ登録
                    DailyContents.add(dcs.getString(3));
                    DailyLogId.add(dcs.getInt(2));

                    next = dcs.moveToNext();
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
