package and.todo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("all") //ToDo　後で削除してできるかぎり警告文修正
public class NewFragment extends Fragment implements DateDialogFragment.DateDialogListener {
    private String level=""; //目標レベル
    private String title=""; //目標タイトル
    private String content=""; //目標内容
    private Spinner spinLevel; //目標Spinner
    private int titleNum; //タイトル文字数
    private Button editBtn; //編集完了ボタン
    //private String beforelevel="none";
    private int big=0; //上目標
    private String bigtitle=""; // 上目標タイトル
    private int bighold=0;
    private int middle=0; //中目標
    private String middletitle="";// 中目標タイトル
    private int middlehold=0;
    private String date = ""; //日付
    private int hold = 0; //保留判定(0は保留なし）
    private int important = 0; //優先度
    private String memo = ""; //進捗内容
    private int proceed = 0; //進捗度合い
    private int fin = 0; //完了判定
    ArrayList<String> bigTitle = new ArrayList<>(); //編集画面で表示する大目標のタイトルを保管する配列
    ArrayList<HashMap<String,String>> bigData = new ArrayList<>(); //編集画面で表示する大目標のIDを保管するための配列
    ArrayList<String> middleTitle = new ArrayList<>();//編集画面で表示する中目標のタイトルを保管する配列
    ArrayList<HashMap<String,String>> middleData = new ArrayList<>();//編集画面で表示する中目標データを保管するための配列
    private EditDatabaseHelper helper;
    private TextView txtCheck = null; //日付チェック表示用TextView
    private EditText editDate = null; //日付入力用EditText
    private boolean dateok = false;

    public static NewFragment newInstance(Bundle Data){ //インスタンス作成時にまず呼び出す
        // インスタンス生成
        NewFragment fragment = new NewFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }


    @Nullable
    @Override //フラグメントのviewを作成する処理
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_edit,container,false);
    }



    @Override //フラグメントのview作成後の処理
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Bundleデータを取得
        Bundle Data = getArguments();
        if(Data != null ){ //Dataにデータがあるときの処理
        }

        View decor = requireActivity().getWindow().getDecorView();//バーを含めたぜびゅー全体

        ConstraintLayout top = view.findViewById(R.id.editConstraint);
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


        //データベースヘルパー準備
        helper = new EditDatabaseHelper(requireActivity());

        getbigTarget();
        int bigsize = bigData.size();//大目標が存在するかの判定変数
        level = "small";
        getbigTarget();
        int middlesize = bigData.size();//中目標が存在するかの判定変数
        level = "big";//初期状態を大目標にセット

        ArrayAdapter<String> bigAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,bigTitle);
        ArrayAdapter<String> middleAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);


        //目標タイトル取得
        EditText editTitle = view.findViewById(R.id.EditTitle);
        editTitle.addTextChangedListener(new EditTextWatcher(editTitle));

        //目標内容取得
        EditText editContent = view.findViewById(R.id.EditContent);

        //目標選択スピナー取得
        spinLevel = (Spinner) view.findViewById(R.id.spinLevel);
        ArrayList<String> levelList = new ArrayList<>();
        if(bigsize >0 && middlesize >0){ //大中目標が存在するとき
            levelList.add("大目標");
            levelList.add("中目標");
            levelList.add("小目標");
            levelList.add("スケジュール");
            levelList.add("やること");
        }else if(bigsize>0){//大目標が存在するとき
            levelList.add("大目標");
            levelList.add("中目標");
            levelList.add("スケジュール");
            levelList.add("やること");
        }else{
            levelList.add("大目標");
            levelList.add("スケジュール");
            levelList.add("やること");
        }
        ArrayAdapter<String> ladapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,levelList);
        spinLevel.setAdapter(ladapter);

        //保留チェックボックス取得
        CheckBox checkhold = view.findViewById(R.id.checkHold);
        checkhold.setOnCheckedChangeListener((CompoundButton buttonview,boolean ischecked)->{
            if(ischecked==true){ //チェック時には保留状態（1）とする
                hold = 1;
            }else{
                hold = 0;
            }
        });

        //編集完了ボタン取得
        editBtn = (Button) view.findViewById(R.id.EditDone);
        editBtn.setOnClickListener(v->{ //ボタンクリック時にはデータベース登録処理



            try(SQLiteDatabase db = helper.getWritableDatabase()) {
                //トランザクション開始
                db.beginTransaction();
                try {

                    ContentValues cv = new ContentValues(); //データベース挿入用のContentValues
                    ContentValues lcv = new ContentValues(); //ログデータベース挿入用のContentValues
                    lcv.put("ope","insert");
                    cv.put("title", editTitle.getText().toString());
                    lcv.put("aftertitle",editTitle.getText().toString());
                    cv.put("content", editContent.getText().toString());
                    lcv.put("aftercontent",editContent.getText().toString());
                    cv.put("level", level);
                    lcv.put("afterlevel",level);
                    if (level.equals("middle") || level.equals("small")) {
                        cv.put("big", big);
                        cv.put("bigtitle",bigtitle);
                        cv.put("bighold",bighold);
                        lcv.put("afterbig",big);
                        lcv.put("afterbigtitle",bigtitle);
                        lcv.put("afterbighold",bighold);
                    } else {
                        cv.put("big", 0);
                        cv.put("bigtitle","");
                        cv.put("bighold",0);
                        lcv.put("afterbig",0);
                        lcv.put("afterbigtitle","");
                        lcv.put("afterbighold",0);
                    }
                    if (level.equals("small")) {
                        cv.put("middle", middle);
                        cv.put("middletitle",middletitle);
                        cv.put("middlehold",middlehold);
                        lcv.put("aftermiddle",middle);
                        lcv.put("aftermiddletitle",middletitle);
                        lcv.put("aftermiddlehold",middlehold);
                    } else {
                        cv.put("middle", 0);
                        cv.put("middletitle","");
                        cv.put("middlehold",0);
                        lcv.put("aftermiddle",0);
                        lcv.put("aftermiddletitle","");
                        lcv.put("aftermiddlehold",0);
                    }
                    if (level.equals("schedule")) {
                        cv.put("date", date);
                        lcv.put("afterdate",date);
                    } else {
                        cv.put("date", "");
                        lcv.put("afterdate","");
                    }

                    if(level.equals("middle")) {
                        if(hold == 0) { //非保留状態のとき
                            if(bighold==1){//大目標が保留状態のとき下の中目標データも保留にする
                                cv.put("hold",1);
                                lcv.put("afterhold",1);
                            }else{//大目標が非保留状態のときは下のデータは入力されたデータのまま
                                cv.put("hold",hold);
                                lcv.put("afterhold",hold);
                            }

//                            for (int b = 0; b < bigData.size(); b++) {
//                                if (big == Integer.parseInt(bigData.get(b).get("id"))) {
//                                    if (Integer.parseInt(bigData.get(b).get("hold")) == 1) {
//                                        cv.put("hold", 1);
//                                        lcv.put("afterhold",1);
//                                    }else{
//                                        cv.put("hold",hold);
//                                        lcv.put("afterhold",hold);
//                                    }
//                                    break;
//                                }
//                            }
                        }else{
                            cv.put("hold",hold);
                            lcv.put("afterhold",hold);
                        }
                    }else if(level.equals("small")){
                        if(hold == 0) { //非保留状態のとき
                            if(middlehold==1){//中目標が保留状態のとき下の小目標も保留にする
                                cv.put("hold",1);
                                lcv.put("afterhold",1);
                            }else{
                                cv.put("hold",hold);
                                lcv.put("afterhold",hold);
                            }

                        }else{
                            cv.put("hold",hold);
                            lcv.put("afterhold",hold);
                        }
                    }else{
                        cv.put("hold", hold);
                        lcv.put("afterhold",hold);
                    }

                    cv.put("important", important);
                    lcv.put("afterimportant",important);
                    cv.put("memo", memo);
                    lcv.put("aftermemo",memo);
                    cv.put("proceed", proceed);
                    lcv.put("afterproceed",proceed);
                    cv.put("fin", fin);
                    lcv.put("afterfin",fin);
                    db.insert("ToDoData", null, cv);

                    String sql = "select id from ToDoData where id= last_insert_rowid();";
                    SQLiteCursor cs = (SQLiteCursor) db.rawQuery(sql,null); //新規データのIDを取得
                    cs.moveToFirst();
                    lcv.put("id",cs.getInt(0));

                    db.insert("LogData",null,lcv); //新規追加をログに追加

                    Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                    if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                        String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                        db.execSQL(dsql);
                    }

                    Toast.makeText(requireActivity(), "データ登録に成功しました", Toast.LENGTH_SHORT).show();

                    //トランザクション成功
                    db.setTransactionSuccessful();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    //トランザクションを終了
                    db.endTransaction();

                    //TopFragmentに移動
                    // FragmentManagerのインスタンス生成
                    FragmentManager fragmentManager = getParentFragmentManager();
                    // FragmentTransactionのインスタンスを取得
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    // インスタンスに対して張り付け方を指定する

                    Bundle sendData = new Bundle(); //データやり取り用のBundleデータ
                    fragmentTransaction.replace(R.id.MainFragment, TopFragment.newInstance(sendData));
                    // 張り付けを実行
                    fragmentTransaction.commit();
                }

            }

        });



        spinLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //レベル項目選択時の処理
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v, int i, long l) {
                Spinner sp = (Spinner) adapterView;
                String level1 = (String)sp.getSelectedItem();

                //選択された目標を取得
                if(level1.equals("大目標")){
                    level = "big";


                    LinearLayout layout;
                    layout = (LinearLayout) view.findViewById(R.id.editItems);
                    /*if(beforelevel.equals("middle")){
                        layout = (LinearLayout) view.findViewById(R.id.biglayout);
                    }else if(beforelevel.equals("small")){
                        layout = (LinearLayout) view.findViewById(R.id.middlelayout);
                    }else if(beforelevel.equals("date")){
                        layout = (LinearLayout) view.findViewById(R.id.datelayout);
                    }else{
                        layout = (LinearLayout) view.findViewById(R.id.editItems);
                    }*/

                    // 内容を全部消す
                    layout.removeAllViews();

                    big = 0;
                    bigtitle= "";
                    bighold=0;
                    middle = 0;
                    middletitle = "";
                    middlehold=0;
                    date = "";
                    dateok = false;

                    //getLayoutInflater().inflate(R.id.editItems,layout);

                    //beforelevel = level;

                }else if(level1.equals("中目標")){
                    level = "middle";


                    //大目標の選択項目を表示
                    LinearLayout layout;
                    layout = (LinearLayout) view.findViewById(R.id.editItems);
                    // 内容を全部消す
                    layout.removeAllViews();
                    // レイアウトをR.layout.bigtargetに変更する
                    getLayoutInflater().inflate(R.layout.bigtarget, layout);

                    //中目標を選択時、大目標の選択肢を出す
                    Spinner bSpin = (Spinner) view.findViewById(R.id.bSpin);
                    getbigTarget();//大目標配列を取得
                    ArrayAdapter<String> bigAdapter =
                            new ArrayAdapter<>(requireActivity(),
                                    android.R.layout.simple_spinner_dropdown_item,bigTitle);
                    bSpin.setAdapter(bigAdapter);//Spinnerに大目標を再設定
                    middle=0; //中目標を初期化
                    middletitle = "";
                    middlehold=0;
                    date = ""; //日付を初期化
                    dateok = false;
                    big = Integer.parseInt(bigData.get(0).get("id")); //大目標の一番上を設定
                    bigtitle = bigData.get(0).get("title"); //大目標一番上のタイトルを設定
                    bighold = Integer.parseInt(bigData.get(0).get("hold")); //大目標一番上の保留状態
                    //beforelevel = level;


                    bSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            big = Integer.parseInt(bigData.get(i).get("id"));
                            bigtitle = bigData.get(i).get("title");
                            bighold = Integer.parseInt(bigData.get(i).get("hold"));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                }else if(level1.equals("小目標")){
                    level = "small";

                    //大、中目標の選択項目を表示
                    LinearLayout layout;
                    layout = (LinearLayout) view.findViewById(R.id.editItems);
                    // 内容を全部消す
                    layout.removeAllViews();
                    // レイアウトをR.layout.sampleに変更する
                    getLayoutInflater().inflate(R.layout.middletarget, layout);

                    //小目標を選択時、大中目標の選択肢を出す
                    Spinner bSpin = (Spinner) view.findViewById(R.id.bSpin);
                    Spinner mSpin = (Spinner) view.findViewById(R.id.mSpin);

                    getbigTarget();//大目標データを取得
                    ArrayAdapter<String> bigAdapter =
                            new ArrayAdapter<>(requireActivity(),
                                    android.R.layout.simple_spinner_dropdown_item,bigTitle);
                    bSpin.setAdapter(bigAdapter);//Spinnerに大目標を再設定
                    big = Integer.parseInt(bigData.get(0).get("id")); //大目標の一番上を設定
                    bigtitle = bigData.get(0).get("title"); //大目標一番上のタイトルを設定
                    bighold = Integer.parseInt(bigData.get(0).get("hold")); //大目標一番上の保留状態

                    getmiddleTarget(0);//一番上の大目標下の中目標データ配列を取得
                    ArrayAdapter<String> middleAdapter =
                            new ArrayAdapter<>(requireActivity(),
                                    android.R.layout.simple_spinner_dropdown_item,middleTitle);
                    mSpin.setAdapter(middleAdapter);
                    middle = Integer.parseInt(middleData.get(0).get("id")); //中目標の一番上を設定
                    middletitle = middleData.get(0).get("title"); //中目標一番上のタイトル設定
                    middlehold = Integer.parseInt(middleData.get(0).get("hold"));

                    date = ""; //日付情報を初期化
                    dateok = false;
                    //beforelevel = level;

                    bSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            big = Integer.parseInt(bigData.get(i).get("id"));
                            bigtitle = bigData.get(i).get("title");
                            bighold = Integer.parseInt(bigData.get(i).get(
                                    "hold"));

                            getmiddleTarget(i);//選択した大目標下の中目標データ配列を取得
                            ArrayAdapter<String> middleAdapter =
                                    new ArrayAdapter<>(requireActivity(),
                                            android.R.layout.simple_spinner_dropdown_item,middleTitle);
                            mSpin.setAdapter(middleAdapter);
                            middle = Integer.parseInt(middleData.get(0).get("id")); //中目標の一番上を設定
                            middletitle = middleData.get(0).get("title"); //中目標一番上のタイトル設定
                            middlehold = Integer.parseInt(middleData.get(0).get("hold"));


                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    mSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            middle = Integer.parseInt(middleData.get(i).get("id"));
                            middletitle = middleData.get(i).get("title");
                            middlehold = Integer.parseInt(middleData.get(i).get("hold"));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                }else if(level1.equals("スケジュール")){
                    level = "schedule";

                    //スケジュールの選択項目を表示
                    LinearLayout layout;
                    layout = (LinearLayout) view.findViewById(R.id.editItems);
                    // 内容を全部消す
                    layout.removeAllViews();

                    getLayoutInflater().inflate(R.layout.date,layout);

                    editDate = (EditText) layout.findViewById(R.id.editDate);

                    final Calendar cal = Calendar.getInstance();

                    date =String.format(Locale.JAPAN,"%02d-%02d-%02d",cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));
                    editDate.setText(date);

                    editDate.addTextChangedListener(new EditTextWatcher(editDate));
                    //ToDo　編集内容チェック欄
                    txtCheck = (TextView) layout.findViewById(R.id.txtCheck);

                    dateok = true;
                    middle = 0;//中目標を初期化
                    middletitle = "";
                    middlehold = 0;
                    big = 0;//大目標を初期化
                    bigtitle = "";
                    bighold = 0;
                    Button btnDate = (Button) layout.findViewById(R.id.btnDate);

                    btnDate.setOnClickListener(dateView->{
                        // フラグメントマネージャーを取得
                        FragmentManager fragmentManager = getParentFragmentManager();

                        DialogFragment dialog = DateDialogFragment.newInstance();
                        dialog.setTargetFragment(NewFragment.this, 0);
                        dialog.show(fragmentManager,"dialog_date");

                        //日付データ取得
                        //EditText editdate = requireActivity().findViewById(R.id.editDate);
                        //date = editdate.getText().toString();
                    });

                    //beforelevel = level;

                }else if(level1.equals("やること")){
                    level = "todo";

                    LinearLayout layout;
                    layout = (LinearLayout) view.findViewById(R.id.editItems);

                    // 内容を全部消す
                    layout.removeAllViews();

                    big = 0;
                    bigtitle = "";
                    bighold = 0;
                    middle = 0;
                    middletitle = "";
                    middlehold = 0;
                    date = "";
                    dateok = false;

                }

                if(titleNum>0){ //タイトルも入力されていたら編集完了ボタンを有効化
                    editBtn.setEnabled(true);
                }
            }

            //項目未選択時の処理
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                level = "";
                editBtn.setEnabled(false);
            }
        });





    }

    @Override
    public void onDateDialog(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        date =String.format(Locale.JAPAN,"%02d-%02d-%02d",year,monthOfYear+1,dayOfMonth);
        EditText editDate = requireActivity().findViewById(R.id.editDate);
        editDate.setText(date);
    }

    public void getbigTarget(){ //中小目標選択時の大目標配列を取得

        try(SQLiteDatabase db = helper.getReadableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try{
                //大目標を取得

                String[] bcols = {"id","title","hold"};//SQLデータから取得する列
                String[] blevel = { "big","0" };//大目標のみを抽出
                Cursor bcs = db.query("ToDoData",bcols,"level=? and fin=?",blevel,null,null,null,null);
                bigData.clear(); //いったん配列を空にする
                bigTitle.clear();
                boolean next = bcs.moveToFirst();//カーソルの先頭に移動
                while(next){ //Cursorデータが空になるまでbigTitle,bigDataに加えていく
                    int big = bcs.getInt(0);

                    if(level.equals("small")) {
                        Cursor bigExist = db.query("ToDoData", new String[]{
                                        "count" +
                                                "(*)"},
                                "level" +
                                        "=? " +
                                        "and" +
                                        " big=?", new String[]{"middle", "" + big}, null,
                                null, null,
                                null);//大目標の下にある中目標の数

                        bigExist.moveToFirst();

                        if (bigExist.getInt(0) == 0) {//下に中目標が存在しない大目標は飛ばす
                            next = bcs.moveToNext();
                            continue;
                        }
                    }

                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+big);//大目標のID
                    item.put("title",bcs.getString(1));
                    int h = bcs.getInt(2);
                    item.put("hold",""+h);//大目標の待機状態
                    bigData.add(item);
                    if(h==1){ //保留中
                        bigTitle.add(bcs.getString(1)+"(保)");//大目標のタイトル
                    }
                    else{ //非保留中
                        bigTitle.add(bcs.getString(1));//大目標のタイトル
                    }
                    next = bcs.moveToNext();

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

    public void getmiddleTarget(int sel){ //小目標選択時の中目標配列を取得

        try(SQLiteDatabase db = helper.getReadableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try{

                //中目標を取得
                String[] mcols = {"id", "title", "big","bigtitle",
                        "bighold","hold"};//SQLデータから取得する列
                String[] mlevel = {"middle",""+bigData.get(sel).get("id")};
                //中目標のみを抽出
                Cursor mcs = db.query("ToDoData", mcols, "level=? and" +
                        " big=?", mlevel, null, null, null, null);
                middleData.clear(); //いったん配列を空にする
                middleTitle.clear();
                boolean next = mcs.moveToFirst();//カーソルの先頭に移動
                while (next) {
                    HashMap<String, String> item = new HashMap<>();
                    item.put("id", "" + mcs.getInt(0));
                    item.put("title",mcs.getString(1));
                    item.put("big", "" + mcs.getInt(2));
                    item.put("bigtitle",mcs.getString(3));
                    item.put("bighold",""+mcs.getInt(4));
                    int h = mcs.getInt(5);
                    item.put("hold",""+h);
                    middleData.add(item); //中目標データ配列に追加
                    if(h==1){//保留中
                        middleTitle.add(mcs.getString(1)+"(保)");
                    }
                    else{
                        middleTitle.add(mcs.getString(1));
                    }
                    next = mcs.moveToNext();
                }

/*
                //中目標を取得
                String[] mcols = {"id","title","big","bigtitle","bighold","hold"};//SQLデータから取得する列
                String[] mlevel = { "middle","0" };//中目標のみを抽出
                Cursor mcs = db.query("ToDoData",mcols,"level=? and fin=?",mlevel,null,null,null,null);
                middleData.clear(); //いったん配列を空にする
                middleTitle.clear();
                next = mcs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+mcs.getInt(0));
                    item.put("title",mcs.getString(1));
                    item.put("big",""+mcs.getInt(2));
                    item.put("bigtitle",mcs.getString(3));
                    item.put("bighold",""+mcs.getInt(4));
                    item.put("hold",""+mcs.getInt(5));
                    middleData.add(item); //中目標データ配列に追加

                    if(Integer.parseInt( item.get("hold") ) == 1){ //中目標が保留中
                        if(Integer.parseInt(item.get("bighold"))==1){ //大目標保留中
                            middleTitle.add(String.format("(%s(保))-%s(保)",item.get("bigtitle"),item.get("title")));
                        }else{ //大目標保留なし
                            middleTitle.add(String.format("(%s)-%s(保)",item.get("bigtitle"),item.get("title")));
                        }
                    }else{ //中目標が非保留中
                        if(Integer.parseInt( item.get("bighold") ) == 1){ //大目標保留中
                            middleTitle.add(String.format("(%s(保))-%s",item.get("bigtitle"),item.get("title")));
                        }else{ //大目標保留なし
                            middleTitle.add(String.format("(%s)-%s",item.get("bigtitle"),item.get("title")));
                        }
                    }

                    next = mcs.moveToNext();
                }
*/



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


    private class EditTextWatcher implements TextWatcher {

        private View view;

        private EditTextWatcher(View view) {
            this.view = view;
        }

        //タイトル文字列修正直前に呼び出される
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }
        //タイトル文字入力時に呼び出される
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }
        //タイトル入力の最後に呼び出される
        @Override
        public void afterTextChanged(Editable editable) {

            switch (view.getId()) {
                case R.id.EditTitle:

                    titleNum = editable.toString().length();
                    if(titleNum>0 && !level.equals("")){ //タイトル入力、目標選択時にボタン有効化

                        if(!level.equals("schedule")){ //スケジュール以外はボタン有効化
                            editBtn.setEnabled(true);
                        }else if(dateok){ //スケジュールの日付が正しく入力されていたらボタン有効化
                            editBtn.setEnabled(true);
                        }else{
                            editBtn.setEnabled(false);
                        }

                    }else{
                        editBtn.setEnabled(false);
                    }

                    break;
                case R.id.editDate:

                    // テキスト変更後に変更されたテキストを取り出す
                    String inputDate= editable.toString();

                    // 入力された文字をチェック
                    if(inputDate.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")){
                        String str = "OK";
                        txtCheck.setText(str);
                        dateok = true;
                        if(titleNum>0){ //タイトル入力時にボタン有効化
                            editBtn.setEnabled(true);
                        }
                    }
                    else {
                        txtCheck.setText("〇〇〇〇-××-△△の形で記入");
                        dateok = false;
                        editBtn.setEnabled(false);
                    }
                    break;
            }

        }

    }

}
