package and.todo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("all") //ToDo　後で削除してできるかぎり警告文修正
public class TopFragment extends Fragment implements ProgressDialogFragment.ProgressDialogListener,DelDialogFragment.DelDialogListener { //目標・スケジュール一覧画面
    private EditDatabaseHelper helper;
    ArrayList<String> bigTitle = new ArrayList<>(); //表示する大目標のタイトルを保管する配列
    ArrayList<HashMap<String,String>> bigData = new ArrayList<>(); //表示する大目標データを保管するための配列
    ArrayList<String> middleTitle = new ArrayList<>();//表示する中目標のタイトルを保管する配列
    ArrayList<HashMap<String,String>> middleData = new ArrayList<>();//表示する中目標データを保管するための配列
    ArrayList<String> smallTitle = new ArrayList<>();
    ArrayList<HashMap<String,String>> smallData = new ArrayList<>();//表示する小目標データを保管するための配列
    ArrayList<String> scheTitle = new ArrayList<>();
    ArrayList<HashMap<String,String>> scheData = new ArrayList<>();//表示するスケジュールデータを保管するための配列
    ArrayList<String> todoTitle = new ArrayList<>();
    ArrayList<HashMap<String,String>> todoData = new ArrayList<>(); //表示するやることリストデータを保管するための配列
    int bid=0,mid=0,sid=0,scheid=0,todoid=0;//項目選択時のID保管用変数
    ImageButton bedit,medit,sedit,scheedit,todoedit;//各編集ボタン変数
    ImageButton bdl,mdl,sdl,schedl,tododl;//各削除ボタン変数
    private boolean content = false; //内容表示モード判定
    private boolean progress = false; //進捗モード判定
    private int progressLevel = 0;//進捗編集した項目判定変数
    DeleteData del = null; //データ削除用クラス
    int bigDel=0,middleDel=0,smallDel=0,scheDel=0,todoDel=0; //データ削除時に配列から消去するための配列インデックス変数
    String dlevel; //削除のデータの目標
    int did=0;//削除するデータのID

    CustomSpinner bigTarget,middleTarget; //大中目標のスピナー変数
    ListView sList,scheList,todoList; //小目標スケジュールのリスト変数

    //データ渡し用のBundleデータ
    Bundle sendData;


    public static TopFragment newInstance(Bundle Data){
        // インスタンス生成
        TopFragment fragment = new TopFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);


        return inflater.inflate(R.layout.fragment_top,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //ToDo　Listの最大数と複数ページに変更
        //ToDo　最初の数十件以降はスクロールしたら読み込み

        //Bundleデータ取得
        Bundle Data = getArguments();


        //データベースから各データを取得
        helper = new EditDatabaseHelper(requireActivity());

        //TOPに表示するデータの配列を取得
        getArrays();

        del = new DeleteData(requireActivity()); //削除用クラスのインスタンス生成

        //大目標にデータ設定
        bigTarget = (CustomSpinner) view.findViewById(R.id.bigTarget);
        ArrayAdapter<String> bAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,bigTitle);
        bigTarget.setAdapter(bAdapter);
        bigTarget.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) { //大目標選択時のID取得
                bid = Integer.parseInt( bigData.get(position).get("id"));
                bigDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),bigData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
                if(progress) { //進捗状況編集モード
                    progressLevel = 1;
                    // フラグメントマネージャーを取得
                    FragmentManager fragmentManager = getParentFragmentManager();

                    Bundle data = new Bundle();
                    data.putString("editcontent", bigData.get(position).get("memo"));
                    data.putInt("editProg", Integer.parseInt(bigData.get(position).get("proceed")));
                    data.putInt("editFin", Integer.parseInt(bigData.get(position).get("fin")));
                    data.putInt("id", bid);
                    data.putString("editTitle", bigTitle.get(position));
                    ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(data);
                    dialog.setTargetFragment(TopFragment.this, 0);

                    dialog.show(fragmentManager,"dialog_progress");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if(bigData.size()>0) { //大目標が１つ以上あるときの初期設定
            bid = Integer.parseInt(bigData.get(0).get("id")); //大目標の初期位置ID
        }

        //中目標にデータ設定
        middleTarget = (CustomSpinner) view.findViewById(R.id.middleTarget);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle);
        middleTarget.setAdapter(mAdapter);
        middleTarget.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {//中目標選択時のID取得
                mid = Integer.parseInt( middleData.get(position).get("id"));
                middleDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),middleData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
                if(progress) {
                    progressLevel = 2;
                    // フラグメントマネージャーを取得
                    FragmentManager fragmentManager = getParentFragmentManager();

                    Bundle data = new Bundle();
                    data.putString("editcontent", middleData.get(position).get("memo"));
                    data.putInt("editProg", Integer.parseInt(middleData.get(position).get("proceed")));
                    data.putInt("editFin", Integer.parseInt(middleData.get(position).get("fin")));
                    data.putInt("id", mid);
                    data.putString("editTitle", middleTitle.get(position));
                    ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(data);
                    dialog.setTargetFragment(TopFragment.this, 0);

                    dialog.show(fragmentManager,"dialog_progress");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if(middleData.size()>0) { //中目標が１つ以上あるときの初期設定
            mid = Integer.parseInt(middleData.get(0).get("id")); //中目標の初期位置ID
        }

        //小目標にデータ設定
        sList = view.findViewById(R.id.smallTargetList);
        sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,smallTitle));
        sList.setOnItemClickListener(new ListSelecter());

        //ToDo その日の予定数によってスケジュールの大きさ変更

        if(scheData.size()==0){ //スケジュールがないとき当日のスケジュール枠を消す
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.scheduleLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.blank, layout);
        }else{
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.scheduleLayout);
            //スケジュールにデータ設定
            scheList = view.findViewById(R.id.todayScheduleList);
            scheList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,scheTitle));
            scheList.setOnItemClickListener(new ListSelecter());


            if(scheData.size()==1){
                layout.setMinHeight(200);
            }else if(scheData.size()==2){
                layout.setMinHeight(350);
            }else{
                layout.setMinHeight(500);
            }

            scheedit = (ImageButton) view.findViewById(R.id.ScheEditButton);
            scheedit.setEnabled(false);
            scheedit.setOnClickListener(new editClicker());

            schedl = (ImageButton) view.findViewById(R.id.ScheDeleteButton);
            schedl.setEnabled(false);
            schedl.setOnClickListener(new editClicker());

        }


        //やることリストにデータ設定
        todoList = view.findViewById(R.id.todoList);
        todoList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,todoTitle));
        todoList.setOnItemClickListener(new ListSelecter());


        CustomSpinner cspinner = view.findViewById(R.id.modeChange); //編集モード選択スピナー取得
        String[] spinnerItems = { "標準モード", "内容表示モード", "進捗編集モード" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item,
                spinnerItems);
        cspinner.setAdapter(adapter);
        cspinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) { //大目標選択時のID取得
                if(position==0){ //標準モード（選択しても表示されない）
                    content = false;
                    progress = false;
                }else if(position==1){ //内容表示モード（項目の内容をトースト表示）
                    content = true;
                    progress = false;
                }else{//進捗編集モード（選択した項目の進捗状況を編集）
                    content = false;
                    progress = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //各編集ボタン要素を取得
        bedit = (ImageButton) view.findViewById(R.id.BeditButton);
        if(bigData.size()>0){ //大目標データがあれば編集ボタン有効化
            bedit.setEnabled(true);
        }else{ //大目標データがなければ編集ボタン無効化
            bedit.setEnabled(false);
        }
        medit = (ImageButton) view.findViewById(R.id.MeditButton);
        if(middleData.size()>0){ //中目標データがあれば編集ボタン有効化
            medit.setEnabled(true);
        }else{ //中目標データがあれば編集ボタン無効化
            medit.setEnabled(false);
        }
        sedit = (ImageButton) view.findViewById(R.id.SeditButton);
        sedit.setEnabled(false); //項目選択まで無効化
        todoedit = (ImageButton) view.findViewById(R.id.todoEdit);
        todoedit.setEnabled(false);
        //各編集ボタンのイベントリスナー
        bedit.setOnClickListener(new editClicker());
        medit.setOnClickListener(new editClicker());
        sedit.setOnClickListener(new editClicker());
        todoedit.setOnClickListener(new editClicker());

        //各削除ボタン要素を取得
        bdl = (ImageButton) view.findViewById(R.id.BdeleteButton);
        if(bigData.size()>0){ //大目標データがあれば削除ボタン有効化
            bdl.setEnabled(true);
        }else{ //大目標データがなければ削除ボタン無効化
            bdl.setEnabled(false);
        }
        mdl = (ImageButton) view.findViewById(R.id.MdeleteButton);
        if(middleData.size()>0){ // 中目標データがあれば削除ボタン有効化
            mdl.setEnabled(true);
        }else{ //中目標データがなければ削除ボタン無効化
            mdl.setEnabled(false);
        }
        sdl = (ImageButton) view.findViewById(R.id.SdeleteButton);
        sdl.setEnabled(false); //項目選択まで削除ボタン無効化
        tododl = (ImageButton) view.findViewById(R.id.todoDelete);
        tododl.setEnabled(false);
        //各削除ボタンのイベントリスナー
        bdl.setOnClickListener(new editClicker());
        mdl.setOnClickListener(new editClicker());
        sdl.setOnClickListener(new editClicker());
        tododl.setOnClickListener(new editClicker());


    }

    class editClicker implements View.OnClickListener { //編集ボタンクリックで編集画面へ飛ばす

        Bundle editData = new Bundle(); //編集、削除データ送信用
        @Override
        public void onClick(View view) {
            if(view == bedit){ //大目標の編集ボタン
                editData.putString("level","big");
                editData.putInt("id",bid);
            }else if(view == medit){ //中目標の編集ボタン
                editData.putString("level","middle");
                editData.putInt("id",mid);
            }else if(view == sedit){ //小目標の編集ボタン
                editData.putString("level","small");
                editData.putInt("id",sid);
            }else if(view == scheedit){ //スケジュール編集ボタン
                editData.putString("level","schedule");
                editData.putInt("id",scheid);
            }else if(view == todoedit){//やること編集ボタン
                editData.putString("level","todo");
                editData.putInt("id",todoid);
            }else if(view == bdl){//大目標の削除ボタン
                editData.putString("title",bigTitle.get(bigDel));
                editData.putString("level","big");
                dlevel = "big";
                did = bid;
            }else if(view == mdl){//中目標の削除ボタン
                editData.putString("title",middleTitle.get(middleDel));
                editData.putString("level","middle");
                dlevel = "middle";
                did = mid;
            }else if(view == sdl){//小目標の削除ボタン
                editData.putString("title",smallTitle.get(smallDel));
                editData.putString("level","small");
                dlevel = "small";
                did = sid;
            }else if(view == schedl){//スケジュールの削除ボタン
                editData.putString("title",scheTitle.get(scheDel));
                editData.putString("level","schedule");
                dlevel = "schedule";
                did = scheid;
            }else if(view == tododl){//やることリスト削除ボタン
                editData.putString("title",todoTitle.get(todoDel));
                editData.putString("level","todo");
                dlevel = "todo";
                did = todoid;
            }

            if(view == bedit || view == medit || view == sedit || view == todoedit || view == scheedit){
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

            }else if(view == bdl || view == mdl || view == sdl || view == schedl || view == tododl){
                //ToDo 削除確認ダイアログへ飛ぶ

                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                DelDialogFragment dialog = DelDialogFragment.newInstance(editData);
                dialog.setTargetFragment(TopFragment.this, 0);

                dialog.show(fragmentManager,"dialog_delete");

            }

        }
    }


    class ListSelecter implements AdapterView.OnItemClickListener{ //クリックされた項目のIDを取得

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if(adapterView == sList){ //小目標選択時のID取得
                sid = Integer.parseInt( smallData.get(position).get("id"));
                smallDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),smallData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
                //ToDo 進捗状況入力ダイアログ
                if(progress) {
                    progressLevel = 3;
                    // フラグメントマネージャーを取得
                    FragmentManager fragmentManager = getParentFragmentManager();

                    Bundle data = new Bundle();
                    data.putString("editcontent", smallData.get(position).get("memo"));
                    data.putInt("editProg", Integer.parseInt(smallData.get(position).get("proceed")));
                    data.putInt("editFin", Integer.parseInt(smallData.get(position).get("fin")));
                    data.putString("editTitle", smallTitle.get(position));
                    data.putInt("id", sid);
                    ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(data);
                    dialog.setTargetFragment(TopFragment.this, 0);

                    dialog.show(fragmentManager,"dialog_progress");
                }
                sedit.setEnabled(true); //編集ボタン有効化
                sdl.setEnabled(true); //削除ボタン有効化
            }else if(adapterView == scheList){ //スケジュール選択時のID取得
                scheid = Integer.parseInt( scheData.get(position).get("id"));
                scheDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),scheData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
                if(progress) {
                    progressLevel =4;
                    // フラグメントマネージャーを取得
                    FragmentManager fragmentManager = getParentFragmentManager();

                    Bundle data = new Bundle();
                    data.putString("editcontent", scheData.get(position).get("memo"));
                    data.putInt("editProg", Integer.parseInt(scheData.get(position).get("proceed")));
                    data.putInt("editFin", Integer.parseInt(scheData.get(position).get("fin")));
                    data.putInt("id", scheid);
                    data.putString("editTitle", scheTitle.get(position));
                    ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(data);
                    dialog.setTargetFragment(TopFragment.this, 0);

                    dialog.show(fragmentManager,"dialog_progress");
                }
                scheedit.setEnabled(true);
                schedl.setEnabled(true);
            }else if(adapterView == todoList){ //やることリスト選択時のID取得
                todoid = Integer.parseInt( todoData.get(position).get("id") );
                todoDel = position;
                if(content){ //内容表示モード
                    Toast.makeText(requireActivity(),todoData.get(position).get("content"),Toast.LENGTH_LONG).show();
                }
                if(progress) {
                    progressLevel =5;
                    // フラグメントマネージャーを取得
                    FragmentManager fragmentManager = getParentFragmentManager();

                    Bundle data = new Bundle();
                    data.putString("editcontent", todoData.get(position).get("memo"));
                    data.putInt("editProg", Integer.parseInt(todoData.get(position).get("proceed")));
                    data.putInt("editFin", Integer.parseInt(todoData.get(position).get("fin")));
                    data.putInt("id", todoid);
                    data.putString("editTitle", todoTitle.get(position));
                    ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(data);
                    dialog.setTargetFragment(TopFragment.this, 0);

                    dialog.show(fragmentManager,"dialog_progress");
                }
                todoedit.setEnabled(true);
                tododl.setEnabled(true);
            }
        }
    }


    //データ削除確定時の処理
    @Override
    public void onDelDialogPositiveClick(DialogFragment dialog) {
        DeleteData del = new DeleteData(requireActivity()); //削除用クラスのインスタンス生成
        del.delete(dlevel,did);

        //データ削除後、配列からも消す
        if(dlevel.equals("big")){
            bigData.remove(bigDel);
            bigTitle.remove(bigDel);
            bigTarget.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,bigTitle));

            for(int i=0;i<middleData.size();i++){//中目標のうち削除した大目標が上にあるデータを配列から削除
                if(bid == Integer.parseInt(middleData.get(i).get("big"))){
                    middleData.remove(i);
                    middleTitle.remove(i);
                    i--;//削除した分インデックスを戻す
                    middleTarget.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle));

                }
            }
            for(int i=0;i<smallData.size();i++){//小目標のうち削除した大目標が上にあるデータを配列から削除
                if(bid == Integer.parseInt(smallData.get(i).get("big"))){
                    smallData.remove(i);
                    smallTitle.remove(i);
                    i--;//削除した分インデックスを戻す
                    sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,smallTitle));

                }
            }
            if(bigData.size()>0){ //大目標が存在するとき
                bigTarget.setSelection(0);
                bid = Integer.parseInt(bigData.get(0).get("id")); //大目標IDを初期状態に
            }else{//大目標が存在しないとき
                bid = 0;
                bedit.setEnabled(false); //編集ボタン無効化
                bdl.setEnabled(false); //削除ボタン無効化
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
            }
            bigDel = 0;
        }else if(dlevel.equals("middle")){
            middleData.remove(middleDel);
            middleTitle.remove(middleDel);
            middleTarget.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle));

            for(int i=0;i<smallData.size();i++){//小目標のうち削除した中目標が上にあるデータを配列から削除
                if(mid == Integer.parseInt(smallData.get(i).get("middle"))){
                    smallData.remove(i);
                    smallTitle.remove(i);
                    i--;//削除した分インデックスを戻す
                    sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,smallTitle));

                }
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
            }
            middleDel = 0;
        }else if(dlevel.equals("small")){
            smallData.remove(smallDel);
            smallTitle.remove(smallDel);
            sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,smallTitle));
            sedit.setEnabled(false); //ボタンを無効化
            sdl.setEnabled(false);

            smallDel = 0;
        }else if(dlevel.equals("schedule")){
            scheData.remove(scheDel);
            scheTitle.remove(scheDel);
            scheList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,scheTitle));
            scheedit.setEnabled(false);//ボタンを無効化
            schedl.setEnabled(false);

            scheDel = 0;
        }else if(dlevel.equals("todo")){
            todoData.remove(todoDel);
            todoTitle.remove(todoDel);
            todoList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,todoTitle));
            todoedit.setEnabled(false);//ボタンを無効化
            tododl.setEnabled(false);

            todoDel = 0;
        }

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
    public void onDialogPositiveClick(DialogFragment dialog,Bundle data) {

        String memo = data.getString("editcontent");
        int proceed = data.getInt("editProg");
        int id = data.getInt("id");
        int fin = data.getInt("fin");

        ContentValues logcv = new ContentValues(); //ログ情報用のContentValues
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
                logcv.put("id",id);
                logcv.put("ope","update");
                logcv.put("beforetitle",cs.getString(ltitle));
                logcv.put("beforelevel",cs.getString(llevel));
                logcv.put("beforehold",cs.getInt(lhold));
                logcv.put("beforebig",cs.getInt(lbig));
                logcv.put("beforebigtitle",cs.getString(lbigtitle));
                logcv.put("beforebighold",cs.getInt(lbighold));
                logcv.put("beforemiddle",cs.getInt(lmiddle));
                logcv.put("beforemiddletitle",cs.getString(lmiddletitle));
                logcv.put("beforemiddlehold",cs.getInt(lmiddlehold));
                logcv.put("beforedate",cs.getString(ldate));
                logcv.put("beforecontent",cs.getString(lcontent));
                logcv.put("beforeimportant",cs.getInt(limportant));
                logcv.put("beforememo",cs.getString(lmemo));
                logcv.put("beforeproceed",cs.getString(lproceed));
                logcv.put("beforefin",cs.getString(lfin));
                logcv.put("aftertitle",cs.getString(ltitle));
                logcv.put("afterlevel",cs.getString(llevel));
                logcv.put("afterhold",""+cs.getInt(lhold));
                logcv.put("afterbig",cs.getInt(lbig));
                logcv.put("afterbigtitle",cs.getString(lbigtitle));
                logcv.put("afterbighold",cs.getInt(lbighold));
                logcv.put("aftermiddle",cs.getInt(lmiddle));
                logcv.put("aftermiddletitle",cs.getString(lmiddletitle));
                logcv.put("aftermiddlehold",cs.getInt(lmiddlehold));
                logcv.put("afterdate",cs.getString(ldate));
                logcv.put("aftercontent",cs.getString(lcontent));
                logcv.put("afterimportant",cs.getInt(limportant));
                logcv.put("aftermemo",memo);
                logcv.put("afterproceed",proceed);
                logcv.put("afterfin",fin);

            }
        }

        //データベースに反映
        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.beginTransaction();
            try {
                db.insertWithOnConflict("ToDoData", null, cv, SQLiteDatabase.CONFLICT_REPLACE);

                if(progressLevel==1 && fin == 1){ //大目標完了時下の中小目標も完了にする

                    cv = new ContentValues();
                    cv.put("proceed",100);
                    cv.put("fin",1);
                    db.update("ToDoData",cv,"big=?",new String[]{""+id});

                }else if(progressLevel==2 && fin==1){//中目標完了時下の小目標も完了にする

                    cv = new ContentValues();
                    cv.put("proceed",100);
                    cv.put("fin",1);
                    db.update("ToDoData",cv,"middle=?",new String[]{""+id});
                }

                db.insert("LogData",null,logcv);

                Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                    String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                    db.execSQL(dsql);
                }

                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }
        if(progressLevel == 1){ //大目標進捗編集時データ配列を再読み込み

            try(SQLiteDatabase db = helper.getReadableDatabase()){
                //トランザクション開始
                db.beginTransaction();
                try{
                    //大目標を取得

                    String[] bcols = {"id","title","content","important","memo","proceed","fin"};//SQLデータから取得する列
                    String[] blevel = { "big","0","0" };//大目標、保留なし,未終了データのみを抽出
                    Cursor bcs = db.query("ToDoData",bcols,"level=? and hold=? and fin=?",blevel,null,null,null,null);

                    bigData.clear(); //いったん配列を空にする
                    bigTitle.clear();
                    boolean next = bcs.moveToFirst();//カーソルの先頭に移動
                    while(next){ //Cursorデータが空になるまでbigTitle,bigDataに加えていく
                        bigTitle.add(bcs.getString(1));//大目標のタイトル
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+bcs.getInt(0));
                        item.put("content",bcs.getString(2));
                        item.put("important",""+bcs.getInt(3));
                        item.put("memo",bcs.getString(4));
                        item.put("proceed",""+bcs.getInt(5));
                        item.put("fin",""+bcs.getInt(6));
                        bigData.add(item);
                        next = bcs.moveToNext();
                    }
                    bigTarget.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,bigTitle));
                    if(bigData.size()>0){ //大目標データ存在時、一番上に戻す
                        bigDel = 0;
                        bigTarget.setSelection(0);
                    }else{ //大目標がないときは編集削除ボタンを無効化
                        bedit.setEnabled(false);
                        bdl.setEnabled(false);
                    }

                    if(fin==1) { //大目標タスク完了時は中小目標タスクも再読み込み
                        //中目標を取得
                        String[] mcols = {"id", "title", "big", "bigtitle", "bighold", "content", "important", "memo", "proceed", "fin"};//SQLデータから取得する列
                        String[] mlevel = {"middle", "0", "0"};//中目標,保留なし、未終了データのみを抽出
                        Cursor mcs = db.query("ToDoData", mcols, "level=? and hold=? and fin=?", mlevel, null, null, null, null);

                        middleData.clear(); //いったん配列を空にする
                        middleTitle.clear();
                        next = mcs.moveToFirst();//カーソルの先頭に移動
                        while (next) {
                            HashMap<String, String> item = new HashMap<>();
                            item.put("id", "" + mcs.getInt(0));
                            item.put("big", "" + mcs.getInt(2));
                            item.put("content", mcs.getString(5));
                            item.put("important", "" + mcs.getInt(6));
                            item.put("memo", mcs.getString(7));
                            item.put("proceed", "" + mcs.getInt(8));
                            item.put("fin", "" + mcs.getInt(9));

                            item.put("bigtitle", mcs.getString(3));
                            item.put("title", mcs.getString(1));
                            middleTitle.add(String.format("(%s)-%s", item.get("bigtitle"), mcs.getString(1)));

                            middleData.add(item); //中目標データ配列に追加
                            next = mcs.moveToNext();
                        }
                        middleTarget.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle));
                        if(middleData.size()>0){ //中目標データ存在時、一番上に戻す
                            middleDel = 0;
                            middleTarget.setSelection(0);
                        }else{ //中目標がないときは編集削除ボタンを無効化
                            medit.setEnabled(false);
                            mdl.setEnabled(false);
                        }
                        //小目標を取得
                        String[] scols = {"id", "title", "big", "bigtitle", "bighold", "middle", "middletitle", "middlehold", "content", "important", "memo", "proceed", "fin"};//SQLデータから取得する列
                        String[] slevel = {"small", "0", "0"};//小目標、保留無し、未終了データのみを抽出
                        Cursor scs = db.query("ToDoData", scols, "level=? and hold=? and fin=?", slevel, null, null, null, null);

                        smallData.clear(); //いったん配列を空にする
                        smallTitle.clear();
                        next = scs.moveToFirst();//カーソルの先頭に移動
                        while (next) {
                            HashMap<String, String> item = new HashMap<>();
                            item.put("id", "" + scs.getInt(0));
                            item.put("title", scs.getString(1));
                            item.put("big", "" + scs.getInt(2));
                            item.put("bigtitle", scs.getString(3));
                            item.put("bighold", "" + scs.getInt(4));
                            item.put("middle", "" + scs.getInt(5));
                            item.put("middletitle", scs.getString(6));
                            item.put("middlehold", "" + scs.getInt(7));
                            item.put("content", scs.getString(8));
                            item.put("important", "" + scs.getInt(9));
                            item.put("memo", scs.getString(10));
                            item.put("proceed", "" + scs.getInt(11));
                            item.put("fin", "" + scs.getInt(12));

                            smallTitle.add(String.format("(%s)-(%s)-%s", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                            smallData.add(item); //中目標データ配列に追加
                            next = scs.moveToNext();
                        }
                        sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,smallTitle));
                        sedit.setEnabled(false); //編集、削除ボタン無効化
                        sdl.setEnabled(false);
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
        }else if(progressLevel == 2){ //中目標進捗編集時データ配列を再読み込み

            try(SQLiteDatabase db = helper.getReadableDatabase()){
                //トランザクション開始
                db.beginTransaction();
                try{

                    //中目標を取得
                    String[] mcols = {"id","title","big","bigtitle","bighold","content","important","memo","proceed","fin"};//SQLデータから取得する列
                    String[] mlevel = { "middle","0","0" };//中目標,保留なし、未終了データのみを抽出
                    Cursor mcs = db.query("ToDoData",mcols,"level=? and hold=? and fin=?",mlevel,null,null,null,null);

                    middleData.clear(); //いったん配列を空にする
                    middleTitle.clear();
                    boolean next = mcs.moveToFirst();//カーソルの先頭に移動
                    while(next){
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+mcs.getInt(0));
                        item.put("big",""+mcs.getInt(2));
                        item.put("content",mcs.getString(5));
                        item.put("important",""+mcs.getInt(6));
                        item.put("memo",mcs.getString(7));
                        item.put("proceed",""+mcs.getInt(8));
                        item.put("fin",""+mcs.getInt(9));

                        item.put("bigtitle",mcs.getString(3));
                        item.put("title",mcs.getString(1));
                        middleTitle.add(String.format("(%s)-%s",item.get("bigtitle"),mcs.getString(1)));

                        middleData.add(item); //中目標データ配列に追加
                        next = mcs.moveToNext();
                    }
                    middleTarget.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,middleTitle));
                    if(middleData.size()>0){ //中目標データ存在時、一番上に戻す
                        middleDel = 0;
                        middleTarget.setSelection(0);
                    }else{ //中目標がないときは編集削除ボタンを無効化
                        medit.setEnabled(false);
                        mdl.setEnabled(false);
                    }
                    if(fin==1) { //タスク終了時は小目標も再読み込み
                        //小目標を取得
                        String[] scols = {"id", "title", "big", "bigtitle", "bighold", "middle", "middletitle", "middlehold", "content", "important", "memo", "proceed", "fin"};//SQLデータから取得する列
                        String[] slevel = {"small", "0", "0"};//小目標、保留無し、未終了データのみを抽出
                        Cursor scs = db.query("ToDoData", scols, "level=? and hold=? and fin=?", slevel, null, null, null, null);

                        smallData.clear(); //いったん配列を空にする
                        smallTitle.clear();
                        next = scs.moveToFirst();//カーソルの先頭に移動
                        while (next) {
                            HashMap<String, String> item = new HashMap<>();
                            item.put("id", "" + scs.getInt(0));
                            item.put("title", scs.getString(1));
                            item.put("big", "" + scs.getInt(2));
                            item.put("bigtitle", scs.getString(3));
                            item.put("bighold", "" + scs.getInt(4));
                            item.put("middle", "" + scs.getInt(5));
                            item.put("middletitle", scs.getString(6));
                            item.put("middlehold", "" + scs.getInt(7));
                            item.put("content", scs.getString(8));
                            item.put("important", "" + scs.getInt(9));
                            item.put("memo", scs.getString(10));
                            item.put("proceed", "" + scs.getInt(11));
                            item.put("fin", "" + scs.getInt(12));

                            smallTitle.add(String.format("(%s)-(%s)-%s", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                            smallData.add(item); //中目標データ配列に追加
                            next = scs.moveToNext();
                        }
                        sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,smallTitle));
                        sedit.setEnabled(false); //編集、削除ボタン無効化
                        sdl.setEnabled(false);
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

        }else if(progressLevel == 3){ //小目標進捗編集時データ配列を再読み込み

            try(SQLiteDatabase db = helper.getReadableDatabase()){
                //トランザクション開始
                db.beginTransaction();
                try{
                    //小目標を取得
                    String[] scols = {"id","title","big","bigtitle","bighold","middle","middletitle","middlehold","content","important","memo","proceed","fin"};//SQLデータから取得する列
                    String[] slevel = { "small","0","0" };//小目標、保留無し、未終了データのみを抽出
                    Cursor scs = db.query("ToDoData",scols,"level=? and hold=? and fin=?",slevel,null,null,null,null);

                    smallData.clear(); //いったん配列を空にする
                    smallTitle.clear();
                    boolean next = scs.moveToFirst();//カーソルの先頭に移動
                    while(next){
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+scs.getInt(0));
                        item.put("title",scs.getString(1));
                        item.put("big",""+scs.getInt(2));
                        item.put("bigtitle",scs.getString(3));
                        item.put("bighold",""+scs.getInt(4));
                        item.put("middle",""+scs.getInt(5));
                        item.put("middletitle",scs.getString(6));
                        item.put("middlehold",""+scs.getInt(7));
                        item.put("content",scs.getString(8));
                        item.put("important",""+scs.getInt(9));
                        item.put("memo",scs.getString(10));
                        item.put("proceed",""+scs.getInt(11));
                        item.put("fin",""+scs.getInt(12));

                        smallTitle.add(String.format("(%s)-(%s)-%s",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                        smallData.add(item); //中目標データ配列に追加
                        next = scs.moveToNext();
                    }
                    sList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,smallTitle));
                    sedit.setEnabled(false); //編集、削除ボタン無効化
                    sdl.setEnabled(false);

                    //トランザクション成功
                    db.setTransactionSuccessful();
                }catch(SQLException e){
                    e.printStackTrace();
                }finally{
                    //トランザクションを終了
                    db.endTransaction();
                }
            }

        }else if(progressLevel == 4){ //スケジュール進捗編集時データ配列を再読み込み


            try(SQLiteDatabase db = helper.getReadableDatabase()){
                //トランザクション開始
                db.beginTransaction();
                try{

                    //Calendarクラスのオブジェクトを生成する
                    Calendar cl = Calendar.getInstance();
                    //本日の日付データを取得
                    String today = String.format(Locale.JAPAN,"%02d/%02d/%02d",cl.get(Calendar.YEAR),cl.get(Calendar.MONTH)+1,cl.get(Calendar.DATE));

                    //スケジュールを取得
                    String[] schecols = {"id","title","date","content","important","memo","proceed","fin"};//SQLデータから取得する列
                    String[] schelevel = { "schedule","0","0",today };//中目標,保留なし,未完了,当日のデータのみを抽出
                    Cursor schecs = db.query("ToDoData",schecols,"level=? and hold=? and fin=? and date=?",schelevel,null,null,null,null);

                    scheData.clear(); //いったん配列を空にする
                    scheTitle.clear();
                    boolean next = schecs.moveToFirst();//カーソルの先頭に移動
                    while(next){
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+schecs.getInt(0));
                        item.put("title",schecs.getString(1));
                        item.put("date",""+schecs.getString(2));
                        item.put("content",schecs.getString(3));
                        item.put("important",""+schecs.getInt(4));
                        item.put("memo",schecs.getString(5));
                        item.put("proceed",""+schecs.getInt(6));
                        item.put("fin",""+schecs.getInt(7));
                        scheData.add(item); //中目標データ配列に追加
                        scheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                        next = schecs.moveToNext();
                    }
                    scheList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_spinner_dropdown_item,scheTitle));
                    scheedit.setEnabled(false); //編集、削除ボタン無効化
                    schedl.setEnabled(false);

                    //トランザクション成功
                    db.setTransactionSuccessful();
                }catch(SQLException e){
                    e.printStackTrace();
                }finally{
                    //トランザクションを終了
                    db.endTransaction();
                }
            }

        }else{ //TODOリスト進捗編集時データ配列を再読み込み

            try(SQLiteDatabase db = helper.getReadableDatabase()){
                //トランザクション開始
                db.beginTransaction();
                try{
                    //やることリストを取得
                    String[] todocols = {"id","title","content","important","memo","proceed","fin"};//SQLデータから取得する列
                    String[] todolevel = { "todo","0","0" };//中目標,保留なし,未終了データのみを抽出
                    Cursor todocs = db.query("ToDoData",todocols,"level=? and hold=? and fin=?",todolevel,null,null,null,null);

                    todoData.clear(); //いったん配列を空にする
                    todoTitle.clear();
                    boolean next = todocs.moveToFirst();//カーソルの先頭に移動
                    while(next){
                        HashMap<String,String> item = new HashMap<>();
                        item.put("id",""+todocs.getInt(0));
                        item.put("title",todocs.getString(1));
                        item.put("content",todocs.getString(2));
                        item.put("important",""+todocs.getInt(3));
                        item.put("memo",todocs.getString(4));
                        item.put("proceed",""+todocs.getInt(5));
                        item.put("fin",""+todocs.getInt(6));
                        todoData.add(item); //中目標データ配列に追加
                        todoTitle.add(String.format("%s",item.get("title")));
                        next = todocs.moveToNext();
                    }
                    todoList.setAdapter(new ArrayAdapter<>(requireActivity(),android.R.layout.simple_list_item_1,todoTitle));
                    todoedit.setEnabled(false);
                    tododl.setEnabled(false);

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

    void getArrays(){ //データベースから取得してデータ配列に挿入する

        try(SQLiteDatabase db = helper.getReadableDatabase()){
            //トランザクション開始
            db.beginTransaction();
            try{
                //大目標を取得

                String[] bcols = {"id","title","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] blevel = { "big","0","0" };//大目標、保留なし,未終了データのみを抽出
                Cursor bcs = db.query("ToDoData",bcols,"level=? and hold=? and fin=?",blevel,null,null,null,null);

                bigData.clear(); //いったん配列を空にする
                bigTitle.clear();
                boolean next = bcs.moveToFirst();//カーソルの先頭に移動
                while(next){ //Cursorデータが空になるまでbigTitle,bigDataに加えていく
                    bigTitle.add(bcs.getString(1));//大目標のタイトル
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+bcs.getInt(0));
                    item.put("content",bcs.getString(2));
                    item.put("important",""+bcs.getInt(3));
                    item.put("memo",bcs.getString(4));
                    item.put("proceed",""+bcs.getInt(5));
                    item.put("fin",""+bcs.getInt(6));
                    bigData.add(item);
                    next = bcs.moveToNext();
                }

                //中目標を取得
                String[] mcols = {"id","title","big","bigtitle","bighold","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] mlevel = { "middle","0","0" };//中目標,保留なし、未終了データのみを抽出
                Cursor mcs = db.query("ToDoData",mcols,"level=? and hold=? and fin=?",mlevel,null,null,null,null);

                middleData.clear(); //いったん配列を空にする
                middleTitle.clear();
                next = mcs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+mcs.getInt(0));
                    item.put("big",""+mcs.getInt(2));
                    item.put("content",mcs.getString(5));
                    item.put("important",""+mcs.getInt(6));
                    item.put("memo",mcs.getString(7));
                    item.put("proceed",""+mcs.getInt(8));
                    item.put("fin",""+mcs.getInt(9));

                    item.put("bigtitle",mcs.getString(3));
                    item.put("title",mcs.getString(1));
                    middleTitle.add(String.format("(%s)-%s",item.get("bigtitle"),mcs.getString(1)));

                    middleData.add(item); //中目標データ配列に追加
                    next = mcs.moveToNext();
                }

                //小目標を取得
                String[] scols = {"id","title","big","bigtitle","bighold","middle","middletitle","middlehold","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] slevel = { "small","0","0" };//小目標、保留無し、未終了データのみを抽出
                Cursor scs = db.query("ToDoData",scols,"level=? and hold=? and fin=?",slevel,null,null,null,null);

                smallData.clear(); //いったん配列を空にする
                smallTitle.clear();
                next = scs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+scs.getInt(0));
                    item.put("title",scs.getString(1));
                    item.put("big",""+scs.getInt(2));
                    item.put("bigtitle",scs.getString(3));
                    item.put("bighold",""+scs.getInt(4));
                    item.put("middle",""+scs.getInt(5));
                    item.put("middletitle",scs.getString(6));
                    item.put("middlehold",""+scs.getInt(7));
                    item.put("content",scs.getString(8));
                    item.put("important",""+scs.getInt(9));
                    item.put("memo",scs.getString(10));
                    item.put("proceed",""+scs.getInt(11));
                    item.put("fin",""+scs.getInt(12));

                    smallTitle.add(String.format("(%s)-(%s)-%s",item.get("bigtitle"),item.get("middletitle"),item.get("title")));

                    smallData.add(item); //中目標データ配列に追加
                    next = scs.moveToNext();
                }



                //Calendarクラスのオブジェクトを生成する
                Calendar cl = Calendar.getInstance();
                //本日の日付データを取得
                String today = String.format(Locale.JAPAN,"%02d/%02d/%02d",cl.get(Calendar.YEAR),cl.get(Calendar.MONTH)+1,cl.get(Calendar.DATE));

                //スケジュールを取得
                String[] schecols = {"id","title","date","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] schelevel = { "schedule","0","0",today };//中目標,保留なし,当日のデータのみを抽出
                Cursor schecs = db.query("ToDoData",schecols,"level=? and hold=? and fin=? and date=?",schelevel,null,null,null,null);

                scheData.clear(); //いったん配列を空にする
                scheTitle.clear();
                next = schecs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+schecs.getInt(0));
                    item.put("title",schecs.getString(1));
                    item.put("date",""+schecs.getString(2));
                    item.put("content",schecs.getString(3));
                    item.put("important",""+schecs.getInt(4));
                    item.put("memo",schecs.getString(5));
                    item.put("proceed",""+schecs.getInt(6));
                    item.put("fin",""+schecs.getInt(7));
                    scheData.add(item); //中目標データ配列に追加
                    scheTitle.add(String.format("%s[%s]",item.get("title"),item.get("date")));
                    next = schecs.moveToNext();
                }

                //やることリストを取得
                String[] todocols = {"id","title","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] todolevel = { "todo","0","0" };//中目標,保留なし,未終了データのみを抽出
                Cursor todocs = db.query("ToDoData",todocols,"level=? and hold=? and fin=?",todolevel,null,null,null,null);

                todoData.clear(); //いったん配列を空にする
                todoTitle.clear();
                next = todocs.moveToFirst();//カーソルの先頭に移動
                while(next){
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+todocs.getInt(0));
                    item.put("title",todocs.getString(1));
                    item.put("content",todocs.getString(2));
                    item.put("important",""+todocs.getInt(3));
                    item.put("memo",todocs.getString(4));
                    item.put("proceed",""+todocs.getInt(5));
                    item.put("fin",""+todocs.getInt(6));
                    todoData.add(item); //中目標データ配列に追加
                    todoTitle.add(String.format("%s",item.get("title")));
                    next = todocs.moveToNext();
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