package and.todo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("all") //　後で削除してできるかぎり警告文修正
public class TopFragment extends Fragment implements ProgressDialogFragment.ProgressDialogListener,DelDialogFragment.DelDialogListener,HoldDialogFragment.HoldDialogListener,FinDialogFragment.FinDialogListener,DailyDialogFragment.DailyDialogListener { //目標・スケジュール一覧画面
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
    //ArrayList<Integer> smallRoutineData = new ArrayList<>();
    //その日に完了した小目標タスクのデータ配列
    //ArrayList<Integer> todoRoutineData = new ArrayList<>(); //その日に完了したTODO
    // タスクのデータ配列
    int bid=0,mid=0,sid=0,scheid=0,todoid=0;//項目選択時のID保管用変数
    ImageButton bedit,medit,sedit,scheedit,todoedit;//各編集ボタン変数
    ImageButton bdl,mdl,sdl,schedl,tododl;//各削除ボタン変数
    Button bhold,mhold,shold,schehold,todohold;//各保留ボタン変数
    Button bfin,mfin,sfin,schefin,todofin;//各完了ボタン変数
    private boolean content = false; //内容表示モード判定
    private boolean progress = false; //進捗モード判定
    private boolean daily = false;//デイリールーティンチェック判定
    private boolean dailydone = false; //日課タスク終了時に作業量も入力するモード
    private int progressLevel = 0;//進捗編集した項目判定変数
    DeleteData del = null; //データ削除用クラス
    int bigDel=0,middleDel=0,smallDel=0,scheDel=0,todoDel=0; //データ削除時に配列から消去するための配列インデックス変数
    String dlevel; //削除のデータの目標
    int did=0;//削除するデータのID
    String dailylevel;
    private boolean bigFinReset=false,middlFinReset=false;//大中目標完了後一番上の項目設定し直されたときの進捗状況ダイアログ防止変数

    String today;//本日の日付データを取得


    CustomSpinner bigTarget,middleTarget; //大中目標のスピナー変数
    RecyclerView sList,scheList,todoList; //小目標スケジュールのリスト変数

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
        //ToDo ワンクリックで保留に移動するボタン
        //ToDo ワンクリックで完了に移動するボタン

        //Calendarクラスのオブジェクトを生成する
        Calendar cl = Calendar.getInstance();
        //本日の日付データを取得
        today = String.format(Locale.JAPAN,"%02d-%02d-%02d",cl.get(Calendar.YEAR),cl.get(Calendar.MONTH)+1,cl.get(Calendar.DATE));

        View decor = requireActivity().getWindow().getDecorView();//アクションバー含む全ビュー取得

        ConstraintLayout top = view.findViewById(R.id.topConstraint);
        top.setOnClickListener(v-> {

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

        //Bundleデータ取得
        Bundle Data = getArguments();


        //データベースから各データを取得
        helper = new EditDatabaseHelper(requireActivity());

        //TOPに表示するデータの配列を取得
        getArrays();

        del = new DeleteData(requireActivity()); //削除用クラスのインスタンス生成

        Button dailyButton = view.findViewById(R.id.dailyTask);
        dailyButton.setOnClickListener(v->{
            //日々のタスクの達成量確認画面へ

            // BackStackを設定
            FragmentManager fragmentManager = getParentFragmentManager();
            // FragmentTransactionのインスタンスを取得
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);

            // パラメータを設定
            fragmentTransaction.replace(R.id.MainFragment,
                    new DailyLogFragment());
            fragmentTransaction.commit();

        });

        //大目標にデータ設定
        bigTarget = (CustomSpinner) view.findViewById(R.id.bigTarget);
        ArrayAdapter<String> bAdapter = null;
        if(bigTitle.size()==0){//大目標がない時
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.bigLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("大目標なし");
        }else {
            bAdapter = new ArrayAdapter<>(requireActivity(),
                    android.R.layout.simple_spinner_dropdown_item, bigTitle);
            bigTarget.setAdapter(bAdapter);
            bid = Integer.parseInt(bigData.get(0).get("id")); //大目標の初期位置ID

            bigTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) { //大目標選択時のID取得
                    bid = Integer.parseInt(bigData.get(position).get("id"));
                    bigDel = position;
                    if (content) { //内容表示モード
                        Toast.makeText(requireActivity(), bigTitle.get(position) + "の内容:\n" + bigData.get(position).get("content"), Toast.LENGTH_LONG).show();
                    }

                    if (progress && bigFinReset == false) { //進捗状況編集モード
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

                        dialog.show(fragmentManager, "dialog_progress");
                    }
                    bigFinReset = false;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            //ToDo データ再設定テスト
            Log.e("Big",bigData.get(bigData.size()-1).get("id")+
                    " "+bigData.get(bigData.size()-1).get("title"));
        }

        //中目標にデータ設定
        middleTarget = (CustomSpinner) view.findViewById(R.id.middleTarget);
        if(middleTitle.size()==0){
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.middleLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("中目標なし");
        }else {
            ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, middleTitle);
            middleTarget.setAdapter(mAdapter);
            mid = Integer.parseInt(middleData.get(0).get("id")); //中目標の初期位置ID

            middleTarget.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {//中目標選択時のID取得
                    mid = Integer.parseInt(middleData.get(position).get("id"));
                    middleDel = position;
                    if (content) { //内容表示モード
                        Toast.makeText(requireActivity(), middleTitle.get(position) + "の内容:\n" + middleData.get(position).get("content"), Toast.LENGTH_LONG).show();
                    }

                    if (progress && middlFinReset == false) {
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

                        dialog.show(fragmentManager, "dialog_progress");
                    }
                    middlFinReset = false;

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            //ToDo データ再設定テスト
            Log.e("Middle",middleData.get(middleData.size()-1).get(
                    "id")+
                    " "+middleData.get(middleData.size()-1).get(
                    "title"));
        }

        if(smallData.size()==0){ //小目標がない時レイアウトを消す
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.smallLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("小目標なし");
        }else {

            //小目標にデータ設定
            sList = view.findViewById(R.id.smallTargetList);
            sList.setHasFixedSize(true);
            LinearLayoutManager  rLayoutManager = new LinearLayoutManager(requireActivity());
            rLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); //縦方向に設定

            sList.setLayoutManager(rLayoutManager);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(requireActivity(), rLayoutManager.getOrientation());
            sList.addItemDecoration(itemDecoration);


            MyAdapter adapter = new MyAdapter(smallTitle){//リストクリック時の処理
                @Override
                void onRecycleItemClick(View view, int position, String itemData) {
                    onSmallItemClick(view,position,itemData);
                }
            };
            sList.setAdapter(adapter);
            //ToDo データ再設定テスト
            Log.e("Small",smallData.get(smallData.size()-1).get(
                    "id")+
                    " "+smallData.get(smallData.size()-1).get(
                    "title"));



  /*            sList.setOnTouchListener(this);*/
            //LayoutParamsを取得
            ViewGroup.LayoutParams params = sList.getLayoutParams();
        if (smallData.size() == 1) { //リストの項目数で高さを変える
            params.height = 200;
            } else if (smallData.size() == 2) {
            params.height = 300;
            } else if(smallData.size() == 3){
            params.height = 400;
            } else if(smallData.size() == 4){
            params.height = 500;
            }else if(smallData.size() == 5){
            params.height = 600;
            }else if (smallData.size() == 6) {
            params.height = 700;
            } else if(smallData.size() == 7){
            params.height = 800;
            }else if(smallData.size() == 8){
                params.height = 900;
            }else if (smallData.size() >= 9) {
                params.height = 1000;
            }
            sList.setLayoutParams(params);

        }
            sedit = (ImageButton) view.findViewById(R.id.SeditButton);
            sedit.setEnabled(false); //項目選択まで無効化
            sedit.setOnClickListener(new editClicker());
            sdl = (ImageButton) view.findViewById(R.id.SdeleteButton);
            sdl.setEnabled(false); //項目選択まで削除ボタン無効化
            sdl.setOnClickListener(new editClicker());
            shold = view.findViewById(R.id.SholdButton);//小目標、保留ボタン
            shold.setEnabled(false);
            shold.setOnClickListener(new editClicker());
            sfin = view.findViewById(R.id.SfinishButton);//小目標、完了ボタン
            sfin.setEnabled(false);
            sfin.setOnClickListener(new editClicker());



        if(scheData.size()==0){ //スケジュールがないとき当日のスケジュール枠を消す
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.scheduleLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("当日スケジュールなし");
        }else{


            //スケジュールにデータ設定
            scheList = view.findViewById(R.id.todayScheduleList);
            scheList.setHasFixedSize(true);
            LinearLayoutManager  rLayoutManager = new LinearLayoutManager(requireActivity());
            rLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); //縦方向に設定

            scheList.setLayoutManager(rLayoutManager);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(requireActivity(), rLayoutManager.getOrientation());
            scheList.addItemDecoration(itemDecoration);


            MyAdapter adapter = new MyAdapter(scheTitle){//リストクリック時の処理
                @Override
                void onRecycleItemClick(View view, int position, String itemData) {
                    onScheItemClick(view,position,itemData);
                }
            };
            scheList.setAdapter(adapter);

            //ToDo データ再設定テスト
            Log.e("Sche",scheData.get(scheData.size()-1).get("id")+
                    " "+scheData.get(scheData.size()-1).get("title"));

/**/
            //LayoutParamsを取得
            ViewGroup.LayoutParams params = scheList.getLayoutParams();
            if (scheData.size() == 1) {
                params.height = 200;
            } else if (scheData.size() == 2) {
                params.height = 300;
            } else if(scheData.size() == 3){
                params.height = 400;
            } else if(scheData.size() == 4){
                params.height = 500;
            }else if(scheData.size() == 5){
                params.height = 600;
            }else if (scheData.size() == 6) {
                params.height =700;
            } else if(scheData.size() == 7){
                params.height = 800;
            }else if(scheData.size() == 8){
                params.height = 900;
            }else if (scheData.size() >= 9) {
                params.height = 1000;
            }
            scheList.setLayoutParams(params);

        }
            scheedit = (ImageButton) view.findViewById(R.id.ScheEditButton);
            scheedit.setEnabled(false);
            scheedit.setOnClickListener(new editClicker());

            schedl = (ImageButton) view.findViewById(R.id.ScheDeleteButton);
            schedl.setEnabled(false);
            schedl.setOnClickListener(new editClicker());

            schehold = view.findViewById(R.id.ScheholdButton);//スケジュール、保留ボタン
            schehold.setEnabled(false);
            schehold.setOnClickListener(new editClicker());
            schefin = view.findViewById(R.id.SchefinishButton);//スケジュール、完了ボタン
            schefin.setEnabled(false);
            schefin.setOnClickListener(new editClicker());



        if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
            //レイアウトを取得して消去しブランクにする
            ConstraintLayout layout;
            layout = (ConstraintLayout) view.findViewById(R.id.todoLayout);
            layout.removeAllViews();
            getLayoutInflater().inflate(R.layout.non_items, layout);
            TextView non = layout.findViewById(R.id.noItems);
            non.setText("TODOリストなし");
        }else {

            //やることリストにデータ設定
            todoList = view.findViewById(R.id.todoList);
            todoList.setHasFixedSize(true);
            LinearLayoutManager  rLayoutManager = new LinearLayoutManager(requireActivity());
            rLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); //縦方向に設定

            todoList.setLayoutManager(rLayoutManager);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(requireActivity(), rLayoutManager.getOrientation());
            todoList.addItemDecoration(itemDecoration);


            MyAdapter adapter = new MyAdapter(todoTitle){//リストクリック時の処理
                @Override
                void onRecycleItemClick(View view, int position, String itemData) {
                    onToDoItemClick(view,position,itemData);
                }
            };
            todoList.setAdapter(adapter);

            //ToDo データ再設定テスト
            Log.e("ToDo",todoData.get(todoData.size()-1).get("id")+
                    " "+todoData.get(todoData.size()-1).get("title"));

            //LayoutParamsを取得
            ViewGroup.LayoutParams params = todoList.getLayoutParams();
            if (todoData.size() == 1) {
                params.height = 200;
            } else if (todoData.size() == 2) {
                params.height = 300;
            } else if(todoData.size() == 3){
                params.height = 400;
            } else if(todoData.size() == 4){
                params.height = 500;
            }else if(todoData.size() == 5){
                params.height = 600;
            }else if (todoData.size() == 6) {
                params.height =700;
            } else if(todoData.size() == 7){
                params.height = 800;
            }else if(todoData.size() == 8){
                params.height = 900;
            }else if (todoData.size() >= 9) {
                params.height = 1000;
            }
            todoList.setLayoutParams(params);

        }
            todoedit = (ImageButton) view.findViewById(R.id.todoEdit);
            todoedit.setEnabled(false);
            todoedit.setOnClickListener(new editClicker());
            tododl = (ImageButton) view.findViewById(R.id.todoDelete);
            tododl.setEnabled(false);
            tododl.setOnClickListener(new editClicker());

            todohold = view.findViewById(R.id.todoholdButton);//やること、保留ボタン
            todohold.setEnabled(false);
            todohold.setOnClickListener(new editClicker());
            todofin = view.findViewById(R.id.todofinishButton);//やること、完了ボタン
            todofin.setEnabled(false);
            todofin.setOnClickListener(new editClicker());


        CustomSpinner cspinner = view.findViewById(R.id.modeChange); //編集モード選択スピナー取得
        String[] spinnerItems = { "標準モード", "内容表示モード", "進捗編集モード","日課タスク終了","日課タスク作業量入力" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item,
                spinnerItems);
        cspinner.setAdapter(adapter);
        cspinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) { //大目標選択時のID取得
                if(position==0){ //標準モード（全モードリセット）
                    content = false;
                    progress = false;
                    daily = false;
                    dailydone = false;
                }else if(position==1){ //内容表示モード（項目の内容をトースト表示）
                    content = true;
                    progress = false;
                    daily = false;
                    dailydone = false;
                }else if(position==2){//進捗編集モード（選択した項目の進捗状況を編集）
                    content = false;
                    progress = true;
                    daily = false;
                    dailydone = false;
                }else if(position==3){//デイリールーティンチェック（選択した項目を当日のタスク欄から消す）
                    content = false;
                    progress = false;
                    daily = true;
                    dailydone = false;
                }else if(position==4){
                    content = false;
                    progress = false;
                    daily = false;
                    dailydone = true;
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
        }else{ //中目標データがなければ編集ボタン無効化
            medit.setEnabled(false);
        }
        //各編集ボタンのイベントリスナー
        bedit.setOnClickListener(new editClicker());
        medit.setOnClickListener(new editClicker());

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
        //各削除ボタンのイベントリスナー
        bdl.setOnClickListener(new editClicker());
        mdl.setOnClickListener(new editClicker());

        //各保留ボタン要素を取得
        bhold = view.findViewById(R.id.BholdButton);
        if(bigData.size()>0){ //大目標データがあれば保留ボタン有効化
            bhold.setEnabled(true);
        }else{ //大目標データがなければ保留ボタン無効化
            bhold.setEnabled(false);
        }
        mhold = view.findViewById(R.id.MholdButton);
        if(middleData.size()>0){ // 中目標データがあれば保留ボタン有効化
            mhold.setEnabled(true);
        }else{ //中目標データがなければ保留ボタン無効化
            mhold.setEnabled(false);
        }
        //各保留ボタンのイベントリスナー
        bhold.setOnClickListener(new editClicker());
        mhold.setOnClickListener(new editClicker());

        //各完了ボタン要素を取得
        bfin = view.findViewById(R.id.BfinishButton);
        if(bigData.size()>0){ //大目標データがあれば完了ボタン有効化
            bfin.setEnabled(true);
        }else{ //大目標データがなければ完了ボタン無効化
            bfin.setEnabled(false);
        }
        mfin = view.findViewById(R.id.MfinishButton);
        if(middleData.size()>0){ // 中目標データがあれば完了ボタン有効化
            mfin.setEnabled(true);
        }else{ //中目標データがなければ完了ボタン無効化
            mfin.setEnabled(false);
        }
        //各完了ボタンのイベントリスナー
        bfin.setOnClickListener(new editClicker());
        mfin.setOnClickListener(new editClicker());
    }

/*    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int touchid = view.getId();
        switch(touchid){
            case R.id.smallTargetList:
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return true;
        }
        return false;
    }*/

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
            }else if(view == bhold){//大目標の保留ボタン
                editData.putString("title",bigTitle.get(bigDel));
                editData.putString("level","big");
                dlevel = "big";
                did = bid;
            }else if(view == mhold){//中目標の保留ボタン
                editData.putString("title",middleTitle.get(middleDel));
                editData.putString("level","middle");
                dlevel = "middle";
                did = mid;
            }else if(view == shold){//小目標の保留ボタン
                editData.putString("title",smallTitle.get(smallDel));
                editData.putString("level","small");
                dlevel = "small";
                did = sid;
            }else if(view == schehold){//スケジュールの保留ボタン
                editData.putString("title",scheTitle.get(scheDel));
                editData.putString("level","schedule");
                dlevel = "schedule";
                did = scheid;
            }else if(view == todohold){//やることリスト保留ボタン
                editData.putString("title",todoTitle.get(todoDel));
                editData.putString("level","todo");
                dlevel = "todo";
                did = todoid;
            }else if(view == bfin){//大目標の完了ボタン
                editData.putString("title",bigTitle.get(bigDel));
                editData.putString("level","big");
                dlevel = "big";
                did = bid;
            }else if(view == mfin){//中目標の完了ボタン
                editData.putString("title",middleTitle.get(middleDel));
                editData.putString("level","middle");
                dlevel = "middle";
                did = mid;
            }else if(view == sfin){//小目標の完了ボタン
                editData.putString("title",smallTitle.get(smallDel));
                editData.putString("level","small");
                dlevel = "small";
                did = sid;
            }else if(view == schefin){//スケジュールの完了ボタン
                editData.putString("title",scheTitle.get(scheDel));
                editData.putString("level","schedule");
                dlevel = "schedule";
                did = scheid;
            }else if(view == todofin){//やることリスト完了ボタン
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
                //削除確認ダイアログへ飛ぶ

                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                DelDialogFragment dialog = DelDialogFragment.newInstance(editData);
                dialog.setTargetFragment(TopFragment.this, 0);

                dialog.show(fragmentManager,"dialog_delete");

            }else if(view == bhold || view == mhold || view == shold || view == schehold || view == todohold){
                //保留確認ダイアログへ飛ぶ

                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                HoldDialogFragment dialog =
                        HoldDialogFragment.newInstance(editData);
                dialog.setTargetFragment(TopFragment.this, 0);

                dialog.show(fragmentManager,"dialog_hold");

            }else if(view == bfin || view == mfin || view == sfin || view == schefin || view == todofin){
                //完了確認ダイアログへ飛ぶ

                // フラグメントマネージャーを取得
                FragmentManager fragmentManager = getParentFragmentManager();

                FinDialogFragment dialog =
                        FinDialogFragment.newInstance(editData);
                dialog.setTargetFragment(TopFragment.this, 0);

                dialog.show(fragmentManager,"dialog_finish");

            }

        }
    }

    public void onSmallItemClick(View view,int position,String itemData){//小目標項目選択時のID取得,処理
        sid = Integer.parseInt( smallData.get(position).get("id"));
        smallDel = position;
        //ToDo
        Log.e("SMALLSELECT",
                smallData.get(position).get("id")+" "+ smallData.get(position).get("title"));
        if(content){ //内容表示モード
            Toast.makeText(requireActivity(),smallTitle.get(position)+"の内容:\n"+smallData.get(position).get("content"),Toast.LENGTH_LONG).show();
        }else if(progress) {//進捗状況入力ダイアログ
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
        shold.setEnabled(true);//保留ボタン有効化
        sfin.setEnabled(true);//完了ボタン有効化
        if(daily){ //デイリールーティンチェック
            // チェックしたデータをデイリーデータに登録
            dailyRoutineClick("s"); //データベースに登録する

        }else if(dailydone){
            dailylevel = "s";
            // フラグメントマネージャーを取得
            FragmentManager fragmentManager = getParentFragmentManager();

            Bundle data = new Bundle();
            data.putString("title", smallTitle.get(position));
            data.putString("editContent", "");
            data.putInt("id",0);
            DailyDialogFragment dialog = DailyDialogFragment.newInstance(data);
            dialog.setTargetFragment(TopFragment.this, 0);

            dialog.show(fragmentManager,"dialog_topdaily");
        }
    }

    public void onScheItemClick(View view,int position,String itemData){ //スケジュール項目選択時のID取得,処理
        scheid = Integer.parseInt( scheData.get(position).get("id"));
        scheDel = position;
        if(content){ //内容表示モード
            Toast.makeText(requireActivity(),scheTitle.get(position)+"の内容:\n"+scheData.get(position).get("content"),Toast.LENGTH_LONG).show();
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
        schehold.setEnabled(true);
        schefin.setEnabled(true);
    }


    public void onToDoItemClick(View view,int position,String itemData){  //やることリスト項目選択時のID取得,処理
        todoid = Integer.parseInt( todoData.get(position).get("id") );
        todoDel = position;
        //ToDo
        Log.e("TODOSELECT",
                position+" "+todoData.get(position).get("id")+" "+ todoData.get(position).get("title"));
        if(content){ //内容表示モード
            Toast.makeText(requireActivity(),todoTitle.get(position)+"の内容:\n"+todoData.get(position).get("content"),Toast.LENGTH_LONG).show();
        }else if(progress) {
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
        todohold.setEnabled(true);
        todofin.setEnabled(true);
        if(daily){ //デイリールーティンチェック
            //チェックしたデータをデイリーデータに登録
            dailyRoutineClick("t"); //データベースに登録する

        }else if(dailydone){
            dailylevel = "t";
            // フラグメントマネージャーを取得
            FragmentManager fragmentManager = getParentFragmentManager();

            Bundle data = new Bundle();
            data.putString("title", todoTitle.get(position));
            data.putString("editContent", "");
            DailyDialogFragment dialog = DailyDialogFragment.newInstance(data);
            dialog.setTargetFragment(TopFragment.this, 0);

            dialog.show(fragmentManager,"dialog_topdaily");
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

            if(bigTitle.size()==0){//大目標がない時
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout = (ConstraintLayout) requireActivity().findViewById(R.id.bigLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("大目標なし");
            }else {
                bigTarget.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, bigTitle));
                //ToDo データ再設定テスト
                Log.e("Big",bigData.get(bigData.size()-1).get("id")+
                        " "+bigData.get(bigData.size()-1).get("title"));
            }

            for(int i=0;i<middleData.size();i++){//中目標のうち削除した大目標が上にあるデータを配列から削除
                if(bid == Integer.parseInt(middleData.get(i).get("big"))){
                    middleData.remove(i);
                    middleTitle.remove(i);
                    i--;//削除した分インデックスを戻す

                    if(middleTitle.size()==0){
                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("中目標なし");
                    }else {
                        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, middleTitle);
                        middleTarget.setAdapter(mAdapter);
                        //ToDo データ再設定テスト
                        Log.e("Middle",middleData.get(middleData.size()-1).get(
                                "id")+
                                " "+middleData.get(middleData.size()-1).get(
                                        "title"));
                    }

                }
            }
            for(int i=0;i<smallData.size();i++){//小目標のうち削除した大目標が上にあるデータを配列から削除
                if(bid == Integer.parseInt(smallData.get(i).get("big"))){
                    smallData.remove(i);
                    smallTitle.remove(i);
                    i--;//削除した分インデックスを戻す

                    if(smallData.size()==0){//小目標データが無いとき

                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("小目標なし");
                    }else {

                        MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onSmallItemClick(view, position, itemData);
                            }
                        };
                        sList.setAdapter(adapter);

                        //ToDo データ再設定テスト
                        Log.e("Small",smallData.get(smallData.size()-1).get(
                                "id")+
                                " "+smallData.get(smallData.size()-1).get(
                                        "title"));
                    }
                    sid = 0;
                    sedit.setEnabled(false);
                    sdl.setEnabled(false);
                    shold.setEnabled(false);
                    sfin.setEnabled(false);
                }
            }
            if(bigData.size()>0){ //大目標が存在するとき
                bigTarget.setSelection(0);
                bid = Integer.parseInt(bigData.get(0).get("id")); //大目標IDを初期状態に
                bigDel = 0; //大目標の位置を一番上に
            }else{//大目標が存在しないとき
                bid = 0;
                bedit.setEnabled(false); //編集ボタン無効化
                bdl.setEnabled(false); //削除ボタン無効化
                bhold.setEnabled(false);
                bfin.setEnabled(false);
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                middleDel = 0;//中目標の位置を一番上に
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
                mhold.setEnabled(false);
                mfin.setEnabled(false);
            }
        }else if(dlevel.equals("middle")){
            middleData.remove(middleDel);
            middleTitle.remove(middleDel);
            if(middleTitle.size()==0){
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("中目標なし");
            }else {
                ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, middleTitle);
                middleTarget.setAdapter(mAdapter);
                //ToDo データ再設定テスト
                Log.e("Middle",middleData.get(middleData.size()-1).get(
                        "id")+
                        " "+middleData.get(middleData.size()-1).get(
                        "title"));
            }

            for(int i=0;i<smallData.size();i++){//小目標のうち削除した中目標が上にあるデータを配列から削除
                if(mid == Integer.parseInt(smallData.get(i).get("middle"))){
                    smallData.remove(i);
                    smallTitle.remove(i);
                    i--;//削除した分インデックスを戻す

                    if(smallData.size()==0){//小目標データが無いとき

                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("小目標なし");
                    }else {
                        MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onSmallItemClick(view, position, itemData);
                            }
                        };
                        sList.setAdapter(adapter);
                        //ToDo データ再設定テスト
                        Log.e("Small",smallData.get(smallData.size()-1).get(
                                "id")+
                                " "+smallData.get(smallData.size()-1).get(
                                "title"));
                    }
                    sid = 0;
                    sedit.setEnabled(false);
                    sdl.setEnabled(false);
                    shold.setEnabled(false);
                    sfin.setEnabled(false);
                }
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                middleDel = 0;
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
                mhold.setEnabled(false);
                mfin.setEnabled(false);

            }
        }else if(dlevel.equals("small")){
            smallData.remove(smallDel);
            smallTitle.remove(smallDel);

            if(smallData.size()==0){//小目標データが無いとき

                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("小目標なし");
            }else {
                MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onSmallItemClick(view, position, itemData);
                    }
                };
                sList.setAdapter(adapter);
                //ToDo データ再設定テスト
                Log.e("Small",smallData.get(smallData.size()-1).get(
                        "id")+
                        " "+smallData.get(smallData.size()-1).get(
                        "title"));
            }
            sid = 0;
            sedit.setEnabled(false); //ボタンを無効化
            sdl.setEnabled(false);
            shold.setEnabled(false);
            sfin.setEnabled(false);

            smallDel = 0;
        }else if(dlevel.equals("schedule")){
            scheData.remove(scheDel);
            scheTitle.remove(scheDel);
            if(scheData.size()==0){ //スケジュールがないとき当日のスケジュール枠を消す
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.scheduleLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("当日スケジュールなし");
            }else {

                MyAdapter adapter = new MyAdapter(scheTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onScheItemClick(view, position, itemData);
                    }
                };
                scheList.setAdapter(adapter);
                //ToDo データ再設定テスト
                Log.e("Sche",scheData.get(scheData.size()-1).get("id")+
                        " "+scheData.get(scheData.size()-1).get("title"));
            }
            scheid = 0;
            scheedit.setEnabled(false);//ボタンを無効化
            schedl.setEnabled(false);
            schehold.setEnabled(false);
            schefin.setEnabled(false);

            scheDel = 0;
        }else if(dlevel.equals("todo")){
            todoData.remove(todoDel);
            todoTitle.remove(todoDel);


            if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout = (ConstraintLayout) requireActivity().findViewById(R.id.todoLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("TODOリストなし");
            }else {
                MyAdapter adapter = new MyAdapter(todoTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onToDoItemClick(view, position, itemData);
                    }
                };
                todoList.setAdapter(adapter);
                //ToDo データ再設定テスト
                Log.e("ToDo",todoData.get(todoData.size()-1).get("id")+
                        " "+todoData.get(todoData.size()-1).get("title"));
            }
            todoid = 0;
            todoedit.setEnabled(false);//ボタンを無効化
            tododl.setEnabled(false);
            todohold.setEnabled(false);
            todofin.setEnabled(false);

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


    //データ保留確定時の処理
    @Override
    public void onHoldDialogPositiveClick(DialogFragment dialog) {
        // データを保留状態にする
        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.beginTransaction();
            try {

                ContentValues cv = new ContentValues();
                ContentValues lcv = new ContentValues(); //データ変更時のContentValues
                cv.put("hold",1);
                db.update("ToDoData",cv,"id=?",new String[]{""+did});

                //大目標を保留にした時、中小目標も保留状態にする
                if(dlevel.equals("big")){
                    cv.put("bighold",1);
                    //中目標を保留状態にする
                    db.update("ToDoData",cv,"level=? and big=?",new String[]{
                            "middle",""+did});
                    //小目標を保留状態にする
                    cv.put("middlehold",1);//小目標の上の中目標を保留状態にする
                    db.update("ToDoData",cv,"level=? and big=?",new String[]{
                            "small",""+did});

                }
                //中目標を保留にしたとき、小目標も保留状態にする
                if(dlevel.equals("middle")){
                    //小目標を保留状態にする
                    cv.put("middlehold",1);//小目標の上の中目標を保留状態にする
                    db.update("ToDoData",cv,"middle=?",new String[]{""+did});
                }

                //更新ログへ書き込み
                String[] cols = {"title","content","big","bigtitle","bighold"
                        ,"middle","middletitle","middlehold","date",
                        "important","memo","proceed","fin"};
                // データから取得する列
                Cursor cs = db.query("ToDoData", cols, "id=?",
                        new String[]{""+did}, null, null, null, null);

                boolean next = cs.moveToFirst();
                if(next){
                    lcv.put("ope","update");
                    lcv.put("id",did);
                    lcv.put("beforetitle",cs.getString(0));
                    lcv.put("aftertitle",cs.getString(0));
                    lcv.put("beforecontent",cs.getString(1));
                    lcv.put("aftercontent",cs.getString(1));
                    lcv.put("beforelevel",dlevel);
                    lcv.put("afterlevel",dlevel);
                    lcv.put("beforebig",cs.getInt(2));
                    lcv.put("afterbig",cs.getInt(2));
                    lcv.put("beforebigtitle",cs.getString(3));
                    lcv.put("afterbigtitle",cs.getString(3));
                    lcv.put("beforebighold",cs.getInt(4));
                    lcv.put("afterbighold",cs.getInt(4));
                    lcv.put("beforemiddle",cs.getInt(5));
                    lcv.put("aftermiddle",cs.getInt(5));
                    lcv.put("beforemiddletitle",cs.getString(6));
                    lcv.put("aftermiddletitle",cs.getString(6));
                    lcv.put("beforemiddlehold",cs.getInt(7));
                    lcv.put("aftermiddlehold",cs.getInt(7));
                    lcv.put("beforedate",cs.getString(8));
                    lcv.put("afterdate",cs.getString(8));
                    lcv.put("beforehold",0);
                    lcv.put("afterhold",1);
                    lcv.put("beforeimportant",cs.getInt(9));
                    lcv.put("afterimportant",cs.getInt(9));
                    lcv.put("beforememo",cs.getString(10));
                    lcv.put("aftermemo",cs.getString(10));
                    lcv.put("beforeproceed",cs.getInt(11));
                    lcv.put("afterproceed",cs.getInt(11));
                    lcv.put("beforefin",cs.getInt(12));
                    lcv.put("afterfin",cs.getInt(12));

                    db.insert("LogData",null,lcv); //データ変更をログに追加

                    Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                    if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                        String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                        db.execSQL(dsql);
                    }
                }


                Toast.makeText(requireActivity(), "データを保留状態にしました",
                        Toast.LENGTH_SHORT).show();


                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }


        //データを保留にした後、配列から消す
        if(dlevel.equals("big")){
            bigData.remove(bigDel);
            bigTitle.remove(bigDel);
            if(bigTitle.size()==0){//大目標がない時
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout = (ConstraintLayout) requireActivity().findViewById(R.id.bigLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("大目標なし");
            }else {
                bigTarget.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, bigTitle));
                //ToDo データ再設定テスト
                Log.e("Big",bigData.get(bigData.size()-1).get("id")+
                        " "+bigData.get(bigData.size()-1).get("title"));
            }

            for(int i=0;i<middleData.size();i++){//中目標のうち削除した大目標が上にあるデータを配列から削除
                if(bid == Integer.parseInt(middleData.get(i).get("big"))){
                    middleData.remove(i);
                    middleTitle.remove(i);
                    i--;//削除した分インデックスを戻す
                    if(middleTitle.size()==0){
                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("中目標なし");
                    }else {
                        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, middleTitle);
                        middleTarget.setAdapter(mAdapter);
                        //ToDo データ再設定テスト
                        Log.e("Middle",middleData.get(middleData.size()-1).get(
                                "id")+
                                " "+middleData.get(middleData.size()-1).get(
                                "title"));
                    }

                }
            }
            for(int i=0;i<smallData.size();i++){//小目標のうち削除した大目標が上にあるデータを配列から削除
                if(bid == Integer.parseInt(smallData.get(i).get("big"))){
                    smallData.remove(i);
                    smallTitle.remove(i);
                    i--;//削除した分インデックスを戻す

                    if(smallData.size()==0){//小目標データが無いとき

                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("小目標なし");
                    }else {
                        MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onSmallItemClick(view, position, itemData);
                            }
                        };
                        sList.setAdapter(adapter);
                        //ToDo データ再設定テスト
                        Log.e("Small",smallData.get(smallData.size()-1).get(
                                "id")+
                                " "+smallData.get(smallData.size()-1).get(
                                "title"));
                    }
                    sid = 0;
                    sedit.setEnabled(false);
                    sdl.setEnabled(false);
                    shold.setEnabled(false);
                    sfin.setEnabled(false);
                }
            }
            if(bigData.size()>0){ //大目標が存在するとき
                bigTarget.setSelection(0);
                bid = Integer.parseInt(bigData.get(0).get("id")); //大目標IDを初期状態に
                bigDel = 0; //大目標の位置を一番上に
            }else{//大目標が存在しないとき
                bid = 0;
                bedit.setEnabled(false); //編集ボタン無効化
                bdl.setEnabled(false); //削除ボタン無効化
                bhold.setEnabled(false);
                bfin.setEnabled(false);
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                middleDel = 0;//中目標の位置を一番上に
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
                mhold.setEnabled(false);
                mfin.setEnabled(false);
            }
        }else if(dlevel.equals("middle")){
            middleData.remove(middleDel);
            middleTitle.remove(middleDel);
            if(middleTitle.size()==0){
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("中目標なし");
            }else {
                ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, middleTitle);
                middleTarget.setAdapter(mAdapter);
                //ToDo データ再設定テスト
                Log.e("Middle",middleData.get(middleData.size()-1).get(
                        "id")+
                        " "+middleData.get(middleData.size()-1).get(
                        "title"));
            }

            for(int i=0;i<smallData.size();i++){//小目標のうち削除した中目標が上にあるデータを配列から削除
                if(mid == Integer.parseInt(smallData.get(i).get("middle"))){
                    smallData.remove(i);
                    smallTitle.remove(i);
                    i--;//削除した分インデックスを戻す

                    if(smallData.size()==0){//小目標データが無いとき

                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("小目標なし");
                    }else {
                        MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onSmallItemClick(view, position, itemData);
                            }
                        };
                        sList.setAdapter(adapter);
                        //ToDo データ再設定テスト
                        Log.e("Small",smallData.get(smallData.size()-1).get(
                                "id")+
                                " "+smallData.get(smallData.size()-1).get(
                                "title"));
                    }
                    sid = 0;
                    sedit.setEnabled(false);
                    sdl.setEnabled(false);
                    shold.setEnabled(false);
                    sfin.setEnabled(false);
                }
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                middleDel = 0;
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
                mhold.setEnabled(false);
                mfin.setEnabled(false);
            }
        }else if(dlevel.equals("small")){
            smallData.remove(smallDel);
            smallTitle.remove(smallDel);

            if(smallData.size()==0){//小目標データが無いとき

                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("小目標なし");
            }else {
                MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onSmallItemClick(view, position, itemData);
                    }
                };
                sList.setAdapter(adapter);
                //ToDo データ再設定テスト
                Log.e("Small",smallData.get(smallData.size()-1).get(
                        "id")+
                        " "+smallData.get(smallData.size()-1).get(
                        "title"));
            }
            sid = 0;
            sedit.setEnabled(false); //ボタンを無効化
            sdl.setEnabled(false);
            shold.setEnabled(false);
            sfin.setEnabled(false);

            smallDel = 0;
        }else if(dlevel.equals("schedule")){
            scheData.remove(scheDel);
            scheTitle.remove(scheDel);
            if(scheData.size()==0){ //スケジュールがないとき当日のスケジュール枠を消す
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.scheduleLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("当日スケジュールなし");
            }else {
                MyAdapter adapter = new MyAdapter(scheTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onScheItemClick(view, position, itemData);
                    }
                };
                scheList.setAdapter(adapter);
            }
            scheid = 0;
            scheedit.setEnabled(false);//ボタンを無効化
            schedl.setEnabled(false);
            schehold.setEnabled(false);
            schefin.setEnabled(false);

            scheDel = 0;
        }else if(dlevel.equals("todo")){
            todoData.remove(todoDel);
            todoTitle.remove(todoDel);

            if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout = (ConstraintLayout) requireActivity().findViewById(R.id.todoLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("TODOリストなし");
            }else {
                MyAdapter adapter = new MyAdapter(todoTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onToDoItemClick(view, position, itemData);
                    }
                };
                todoList.setAdapter(adapter);
            }
            todoid = 0;
            todoedit.setEnabled(false);//ボタンを無効化
            tododl.setEnabled(false);
            todohold.setEnabled(false);
            todofin.setEnabled(false);

            todoDel = 0;
        }

    }
    //データ保留キャンセル時
    @Override
    public void onHoldDialogNegativeClick(DialogFragment dialog) {
    }
    //データ保留スルー
    @Override
    public void onHoldDialogNeutralClick(DialogFragment dialog) {
    }


    //データ完了確定時の処理
    @Override
    public void onFinDialogPositiveClick(DialogFragment dialog) {
        // データを完了状態にする
        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.beginTransaction();
            try {

                ContentValues cv = new ContentValues();
                ContentValues lcv = new ContentValues(); //データ変更時のContentValues
                cv.put("fin",1);
                cv.put("proceed",100);
                db.update("ToDoData",cv,"id=?",new String[]{""+did});

                //大目標を完了にした時、中小目標も完了状態にする
                if(dlevel.equals("big")){
                    //中小目標を完了状態にする
                    db.update("ToDoData",cv,"big=?",new String[]{""+did});

                }
                //中目標を完了にしたとき、小目標も完了状態にする
                if(dlevel.equals("middle")){
                    //小目標を完了状態にする
                    db.update("ToDoData",cv,"middle=?",new String[]{""+did});
                }

                //更新ログへ書き込み
                String[] cols = {"title","content","big","bigtitle","bighold"
                        ,"middle","middletitle","middlehold","date",
                        "important","memo","proceed","hold"};
                // データから取得する列
                Cursor cs = db.query("ToDoData", cols, "id=?",
                        new String[]{""+did}, null, null, null, null);

                boolean next = cs.moveToFirst();
                if(next){
                    lcv.put("ope","update");
                    lcv.put("id",did);
                    lcv.put("beforetitle",cs.getString(0));
                    lcv.put("aftertitle",cs.getString(0));
                    lcv.put("beforecontent",cs.getString(1));
                    lcv.put("aftercontent",cs.getString(1));
                    lcv.put("beforelevel",dlevel);
                    lcv.put("afterlevel",dlevel);
                    lcv.put("beforebig",cs.getInt(2));
                    lcv.put("afterbig",cs.getInt(2));
                    lcv.put("beforebigtitle",cs.getString(3));
                    lcv.put("afterbigtitle",cs.getString(3));
                    lcv.put("beforebighold",cs.getInt(4));
                    lcv.put("afterbighold",cs.getInt(4));
                    lcv.put("beforemiddle",cs.getInt(5));
                    lcv.put("aftermiddle",cs.getInt(5));
                    lcv.put("beforemiddletitle",cs.getString(6));
                    lcv.put("aftermiddletitle",cs.getString(6));
                    lcv.put("beforemiddlehold",cs.getInt(7));
                    lcv.put("aftermiddlehold",cs.getInt(7));
                    lcv.put("beforedate",cs.getString(8));
                    lcv.put("afterdate",cs.getString(8));
                    lcv.put("beforehold",cs.getInt(12));
                    lcv.put("afterhold",cs.getInt(12));
                    lcv.put("beforeimportant",cs.getInt(9));
                    lcv.put("afterimportant",cs.getInt(9));
                    lcv.put("beforememo",cs.getString(10));
                    lcv.put("aftermemo",cs.getString(10));
                    lcv.put("beforeproceed",0);
                    lcv.put("afterproceed",100);
                    lcv.put("beforefin",0);
                    lcv.put("afterfin",1);

                    db.insert("LogData",null,lcv); //データ変更をログに追加

                    Cursor lcs = db.query("LogData",new String[]{"logid"},null,null,null,null,null,null);
                    if(lcs.getCount()>300){ //ログ件数が３００件を超えたら古いのから削除
                        String dsql = "delete from LogData order by logid asc limit "+(lcs.getCount()-300);
                        db.execSQL(dsql);
                    }
                }


                Toast.makeText(requireActivity(), "データを完了状態にしました",
                        Toast.LENGTH_SHORT).show();


                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }


        //データを完了にした後、配列から消す
        if(dlevel.equals("big")){
            bigData.remove(bigDel);
            bigTitle.remove(bigDel);
            if(bigTitle.size()==0){//大目標がない時
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout = (ConstraintLayout) requireActivity().findViewById(R.id.bigLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("大目標なし");
            }else {
                bigTarget.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, bigTitle));
            }


            for(int i=0;i<middleData.size();i++){//中目標のうち削除した大目標が上にあるデータを配列から削除
                if(bid == Integer.parseInt(middleData.get(i).get("big"))){
                    middleData.remove(i);
                    middleTitle.remove(i);
                    i--;//削除した分インデックスを戻す
                    if(middleTitle.size()==0){
                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("中目標なし");
                    }else {
                        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, middleTitle);
                        middleTarget.setAdapter(mAdapter);
                    }

                }
            }
            for(int i=0;i<smallData.size();i++){//小目標のうち削除した大目標が上にあるデータを配列から削除
                if(bid == Integer.parseInt(smallData.get(i).get("big"))){
                    smallData.remove(i);
                    smallTitle.remove(i);
                    i--;//削除した分インデックスを戻す

                    if(smallData.size()==0){//小目標データが無いとき

                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("小目標なし");
                    }else {
                        MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onSmallItemClick(view, position, itemData);
                            }
                        };
                        sList.setAdapter(adapter);
                    }
                    sid = 0;
                    sedit.setEnabled(false);
                    sdl.setEnabled(false);
                    shold.setEnabled(false);
                    sfin.setEnabled(false);
                }
            }
            if(bigData.size()>0){ //大目標が存在するとき
                bigTarget.setSelection(0);
                bid = Integer.parseInt(bigData.get(0).get("id")); //大目標IDを初期状態に
                bigDel = 0; //大目標の位置を一番上に
            }else{//大目標が存在しないとき
                bid = 0;
                bedit.setEnabled(false); //編集ボタン無効化
                bdl.setEnabled(false); //削除ボタン無効化
                bhold.setEnabled(false);
                bfin.setEnabled(false);
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                middleDel = 0;//中目標の位置を一番上に
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
                mhold.setEnabled(false);
                mfin.setEnabled(false);
            }
        }else if(dlevel.equals("middle")){
            middleData.remove(middleDel);
            middleTitle.remove(middleDel);
            if(middleTitle.size()==0){
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("中目標なし");
            }else {
                ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, middleTitle);
                middleTarget.setAdapter(mAdapter);
            }

            for(int i=0;i<smallData.size();i++){//小目標のうち削除した中目標が上にあるデータを配列から削除
                if(mid == Integer.parseInt(smallData.get(i).get("middle"))){
                    smallData.remove(i);
                    smallTitle.remove(i);
                    i--;//削除した分インデックスを戻す

                    if(smallData.size()==0){//小目標データが無いとき

                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("小目標なし");
                    }else {
                        MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onSmallItemClick(view, position, itemData);
                            }
                        };
                        sList.setAdapter(adapter);
                    }
                    sid = 0;
                    sedit.setEnabled(false);
                    sdl.setEnabled(false);
                    shold.setEnabled(false);
                    sfin.setEnabled(false);
                }
            }
            if(middleData.size()>0){ //中目標が存在するとき
                middleTarget.setSelection(0);
                mid = Integer.parseInt(middleData.get(0).get("id")); //中目標を初期状態に
                middleDel = 0;
            }else{//中目標が存在しないとき
                mid = 0;
                medit.setEnabled(false); //編集ボタン無効化
                mdl.setEnabled(false); //削除ボタン無効化
                mhold.setEnabled(false);
                mfin.setEnabled(false);
            }
        }else if(dlevel.equals("small")){
            smallData.remove(smallDel);
            smallTitle.remove(smallDel);

            if(smallData.size()==0){//小目標データが無いとき

                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("小目標なし");
            }else {
                MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onSmallItemClick(view, position, itemData);
                    }
                };
                sList.setAdapter(adapter);
            }
            sid = 0;
            sedit.setEnabled(false); //ボタンを無効化
            sdl.setEnabled(false);
            shold.setEnabled(false);
            sfin.setEnabled(false);

            smallDel = 0;
        }else if(dlevel.equals("schedule")){
            scheData.remove(scheDel);
            scheTitle.remove(scheDel);
            if(scheData.size()==0){ //スケジュールがないとき当日のスケジュール枠を消す
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.scheduleLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("当日スケジュールなし");
            }else {
                MyAdapter adapter = new MyAdapter(scheTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onScheItemClick(view, position, itemData);
                    }
                };
                scheList.setAdapter(adapter);
            }
            scheid = 0;
            scheedit.setEnabled(false);//ボタンを無効化
            schedl.setEnabled(false);
            schehold.setEnabled(false);
            schefin.setEnabled(false);

            scheDel = 0;
        }else if(dlevel.equals("todo")){
            todoData.remove(todoDel);
            todoTitle.remove(todoDel);


            if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout = (ConstraintLayout) requireActivity().findViewById(R.id.todoLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("TODOリストなし");
            }else {
                MyAdapter adapter = new MyAdapter(todoTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onToDoItemClick(view, position, itemData);
                    }
                };
                todoList.setAdapter(adapter);
            }
            todoid = 0;
            todoedit.setEnabled(false);//ボタンを無効化
            tododl.setEnabled(false);
            todohold.setEnabled(false);
            todofin.setEnabled(false);

            todoDel = 0;
        }

    }
    //データ完了キャンセル時
    @Override
    public void onFinDialogNegativeClick(DialogFragment dialog) {
    }
    //データ完了スルー
    @Override
    public void onFinDialogNeutralClick(DialogFragment dialog) {
    }


    public void dailyRoutineClick(String level){ //当日の作業が終わった時の処理

        //当日完了済みの日課タスクをDailyDataに記録
        try(SQLiteDatabase db = helper.getWritableDatabase()) {
            db.beginTransaction();
            try {
                //クリックしたデータの各情報を入力するContentValues
                ContentValues cv = new ContentValues();
                if (level.equals("s")){
                    cv.put("dailytaskid",sid);
                    cv.put("dailytasktitle",smallTitle.get(smallDel));
                }
                else if(level.equals("t")){
                    cv.put("dailytaskid",todoid);
                    cv.put("dailytasktitle",todoTitle.get(todoDel));
                }

                cv.put("date",today);//本日の日付データ入力
                cv.put("level",level);
                cv.put("dailycontent","");

                db.insert("DailyData", null, cv);

                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }
/*
        //当日のデータ配列から削除
        if(level.equals("s")){
            smallData.remove(smallDel);
            smallTitle.remove(smallDel);

            if(smallData.size()==0){//小目標データが無いとき

                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("小目標なし");
            }else {
                MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onSmallItemClick(view, position, itemData);
                    }
                };
                sList.setAdapter(adapter);
            }
            sid = 0;
            sedit.setEnabled(false); //ボタンを無効化
            sdl.setEnabled(false);
            shold.setEnabled(false);
            sfin.setEnabled(false);

            smallDel = 0;
        }else if(level.equals("t")){
            todoData.remove(todoDel);
            todoTitle.remove(todoDel);
            if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout = (ConstraintLayout) requireActivity().findViewById(R.id.todoLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("TODOリストなし");
            }else {
                MyAdapter adapter = new MyAdapter(todoTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onToDoItemClick(view, position, itemData);
                    }
                };
                todoList.setAdapter(adapter);
            }
            todoid = 0;
            todoedit.setEnabled(false);//ボタンを無効化
            tododl.setEnabled(false);
            todohold.setEnabled(false);
            todofin.setEnabled(false);

            todoDel = 0;
        }*/
    }

    @Override
    public void onDailyDialogPositiveClick(DialogFragment dialog, Bundle data) { //日々のタスクを内容含めて編集後の処理

        ContentValues cv = new ContentValues();
        try(SQLiteDatabase db = helper.getWritableDatabase()){
            db.beginTransaction();
            try{

                if (dailylevel.equals("s")){
                    cv.put("dailytaskid",sid);
                    cv.put("dailytasktitle",smallTitle.get(smallDel));
                }
                else if(dailylevel.equals("t")){
                    cv.put("dailytaskid",todoid);
                    cv.put("dailytasktitle",todoTitle.get(todoDel));
                }

                cv.put("date",today);//本日の日付データ入力
                cv.put("level",dailylevel);
                cv.put("dailycontent",data.getString("editcontent"));

                db.insert("DailyData", null, cv);

                db.setTransactionSuccessful();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }

        }
        /*
        //当日のデータ配列から削除
        if(dailylevel.equals("s")){
            smallData.remove(smallDel);
            smallTitle.remove(smallDel);

            if(smallData.size()==0){//小目標データが無いとき

                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout =
                        (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("小目標なし");
            }else {
                MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onSmallItemClick(view, position, itemData);
                    }
                };
                sList.setAdapter(adapter);
            }
            sid = 0;
            sedit.setEnabled(false); //ボタンを無効化
            sdl.setEnabled(false);
            shold.setEnabled(false);
            sfin.setEnabled(false);

            smallDel = 0;
        }else if(dailylevel.equals("t")){
            todoData.remove(todoDel);
            todoTitle.remove(todoDel);
            if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
                //レイアウトを取得して消去しブランクにする
                ConstraintLayout layout;
                layout = (ConstraintLayout) requireActivity().findViewById(R.id.todoLayout);
                layout.removeAllViews();
                getLayoutInflater().inflate(R.layout.non_items, layout);
                TextView non = layout.findViewById(R.id.noItems);
                non.setText("TODOリストなし");
            }else {
                MyAdapter adapter = new MyAdapter(todoTitle) {//リストクリック時の処理
                    @Override
                    void onRecycleItemClick(View view, int position, String itemData) {
                        onToDoItemClick(view, position, itemData);
                    }
                };
                todoList.setAdapter(adapter);
            }
            todoid = 0;
            todoedit.setEnabled(false);//ボタンを無効化
            tododl.setEnabled(false);
            todohold.setEnabled(false);
            todofin.setEnabled(false);

            todoDel = 0;
        }*/
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog,Bundle data) { //進捗度編集後の処理

        String memo = data.getString("editcontent");
        int proceed = data.getInt("editProg");
        int id = data.getInt("id");
        int fin = data.getInt("fin");

        ContentValues cv = new ContentValues();
        try(SQLiteDatabase db = helper.getWritableDatabase()){
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

            //データベースに反映
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

                //データ完了時のみ、ログ情報に登録
                if(fin==1) {
                    ContentValues lcv = new ContentValues();
                    //ログデータ用のContentValues

                    String[] cols = {"title", "content", "big", "bigtitle", "bighold"
                            , "middle", "middletitle", "middlehold", "date",
                            "important", "memo", "proceed", "hold"};
                    // データから取得する列
                    cs = db.query("ToDoData", cols, "id=?",
                            new String[]{"" + did}, null, null, null, null);

                    boolean next = cs.moveToFirst();
                    if (next) {
                        lcv.put("ope", "update");
                        lcv.put("id", did);
                        lcv.put("beforetitle", cs.getString(0));
                        lcv.put("aftertitle", cs.getString(0));
                        lcv.put("beforecontent", cs.getString(1));
                        lcv.put("aftercontent", cs.getString(1));
                        lcv.put("beforelevel", dlevel);
                        lcv.put("afterlevel", dlevel);
                        lcv.put("beforebig", cs.getInt(2));
                        lcv.put("afterbig", cs.getInt(2));
                        lcv.put("beforebigtitle", cs.getString(3));
                        lcv.put("afterbigtitle", cs.getString(3));
                        lcv.put("beforebighold", cs.getInt(4));
                        lcv.put("afterbighold", cs.getInt(4));
                        lcv.put("beforemiddle", cs.getInt(5));
                        lcv.put("aftermiddle", cs.getInt(5));
                        lcv.put("beforemiddletitle", cs.getString(6));
                        lcv.put("aftermiddletitle", cs.getString(6));
                        lcv.put("beforemiddlehold", cs.getInt(7));
                        lcv.put("aftermiddlehold", cs.getInt(7));
                        lcv.put("beforedate", cs.getString(8));
                        lcv.put("afterdate", cs.getString(8));
                        lcv.put("beforehold", cs.getInt(12));
                        lcv.put("afterhold", cs.getInt(12));
                        lcv.put("beforeimportant", cs.getInt(9));
                        lcv.put("afterimportant", cs.getInt(9));
                        lcv.put("beforememo", cs.getString(10));
                        lcv.put("aftermemo", cs.getString(10));
                        lcv.put("beforeproceed", cs.getInt(11));
                        lcv.put("afterproceed", cs.getInt(11));
                        lcv.put("beforefin", 0);
                        lcv.put("afterfin", 1);

                        db.insert("LogData", null, lcv); //データ変更をログに追加

                        Cursor lcs = db.query("LogData", new String[]{"logid"}, null, null, null, null, null, null);
                        if (lcs.getCount() > 300) { //ログ件数が３００件を超えたら古いのから削除
                            String dsql = "delete from LogData order by logid asc limit " + (lcs.getCount() - 300);
                            db.execSQL(dsql);
                        }
                    }


                    Toast.makeText(requireActivity(), "データを完了状態にしました",
                            Toast.LENGTH_SHORT).show();
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

                    if(bigTitle.size()==0){//大目標がない時
                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout = (ConstraintLayout) requireActivity().findViewById(R.id.bigLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("大目標なし");
                    }else {
                        bigTarget.setAdapter(new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, bigTitle));
                    }

                    if(bigData.size()>0){ //大目標データ存在時、一番上に戻す
                        bigDel = 0;
                        bid = Integer.parseInt(bigData.get(0).get("id"));
                        //大目標を完了にしたときの初期化での進捗状況出現対策
                        bigFinReset = true;
                    }else{ //大目標がないときは編集削除ボタンを無効化
                        bedit.setEnabled(false);
                        bdl.setEnabled(false);
                        bhold.setEnabled(false);
                        bfin.setEnabled(false);
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
                        if(middleTitle.size()==0){
                            //レイアウトを取得して消去しブランクにする
                            ConstraintLayout layout;
                            layout =
                                    (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                            layout.removeAllViews();
                            getLayoutInflater().inflate(R.layout.non_items, layout);
                            TextView non = layout.findViewById(R.id.noItems);
                            non.setText("中目標なし");
                        }else {
                            ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, middleTitle);
                            middleTarget.setAdapter(mAdapter);
                        }
                        if(middleData.size()>0){ //中目標データ存在時、一番上に戻す
                            middleDel = 0;
                            mid = Integer.parseInt(middleData.get(0).get("id"));
                            middlFinReset = true;
                        }else{ //中目標がないときは編集削除ボタンを無効化
                            medit.setEnabled(false);
                            mdl.setEnabled(false);
                            mhold.setEnabled(false);
                            mfin.setEnabled(false);
                        }
                        //小目標を取得
                        String[] scols = {"id", "title", "big", "bigtitle", "bighold", "middle", "middletitle", "middlehold", "content", "important", "memo", "proceed", "fin"};//SQLデータから取得する列
                        String[] slevel = {"small", "0", "0"};//小目標、保留無し、未終了データのみを抽出
                        Cursor scs = db.query("ToDoData", scols, "level=? and hold=? and fin=?", slevel, null, null, null, null);

                        smallData.clear(); //いったん配列を空にする
                        smallTitle.clear();
                        next = scs.moveToFirst();//カーソルの先頭に移動
                        int smallId;
                        while (next) {
                            through:
                            {
                                smallId = scs.getInt(0);
/*                                for (int i = 0; i < smallRoutineData.size(); i++) {
                                    if (smallId == smallRoutineData.get(i)) { //小目標に本日こなしたタスクがあれば飛ばす
                                        break through;
                                    }
                                }*/
                                HashMap<String, String> item = new HashMap<>();
                                item.put("id", "" + smallId);
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

                                smallTitle.add(String.format(" ・ (%s)-(%s)-%s", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                                smallData.add(item); //中目標データ配列に追加
                            }
                            next = scs.moveToNext();
                        }

                        if(smallData.size()==0){//小目標データが無いとき

                            //レイアウトを取得して消去しブランクにする
                            ConstraintLayout layout;
                            layout =
                                    (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                            layout.removeAllViews();
                            getLayoutInflater().inflate(R.layout.non_items, layout);
                            TextView non = layout.findViewById(R.id.noItems);
                            non.setText("小目標なし");
                        }else {
                            MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                                @Override
                                void onRecycleItemClick(View view, int position, String itemData) {
                                    onSmallItemClick(view, position, itemData);
                                }
                            };
                            sList.setAdapter(adapter);
                        }
                        sedit.setEnabled(false); //編集、削除ボタン無効化
                        sdl.setEnabled(false);
                        shold.setEnabled(false);
                        sfin.setEnabled(false);
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
                    if(middleTitle.size()==0){
                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.middleLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("中目標なし");
                    }else {
                        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_dropdown_item, middleTitle);
                        middleTarget.setAdapter(mAdapter);
                    }
                    if(middleData.size()>0){ //中目標データ存在時、一番上に戻す
                        middleDel = 0;
                        mid = Integer.parseInt(middleData.get(0).get("id"));
                        //中目標完了時の初期化での進捗再表示対策
                        middlFinReset = true;
                    }else{ //中目標がないときは編集削除ボタンを無効化
                        medit.setEnabled(false);
                        mdl.setEnabled(false);
                        mhold.setEnabled(false);
                        mfin.setEnabled(false);
                    }
                    if(fin==1) { //タスク終了時は小目標も再読み込み
                        //小目標を取得
                        String[] scols = {"id", "title", "big", "bigtitle", "bighold", "middle", "middletitle", "middlehold", "content", "important", "memo", "proceed", "fin"};//SQLデータから取得する列
                        String[] slevel = {"small", "0", "0"};//小目標、保留無し、未終了データのみを抽出
                        Cursor scs = db.query("ToDoData", scols, "level=? and hold=? and fin=?", slevel, null, null, null, null);

                        smallData.clear(); //いったん配列を空にする
                        smallTitle.clear();
                        next = scs.moveToFirst();//カーソルの先頭に移動
                        int smallId;
                        while (next) {

                            through:
                            {
                                smallId = scs.getInt(0);
/*                                for (int i = 0; i < smallRoutineData.size(); i++) {
                                    if (smallId == smallRoutineData.get(i)) { //小目標に本日こなしたタスクがあれば飛ばす
                                        break through;
                                    }
                                }*/
                                HashMap<String, String> item = new HashMap<>();
                                item.put("id", "" + smallId);
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

                                smallTitle.add(String.format(" ・ (%s)-(%s)-%s", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                                smallData.add(item); //中目標データ配列に追加
                            }
                            next = scs.moveToNext();
                        }
                        if(smallData.size()==0){//小目標データが無いとき

                            //レイアウトを取得して消去しブランクにする
                            ConstraintLayout layout;
                            layout =
                                    (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                            layout.removeAllViews();
                            getLayoutInflater().inflate(R.layout.non_items, layout);
                            TextView non = layout.findViewById(R.id.noItems);
                            non.setText("小目標なし");
                        }else {
                            MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                                @Override
                                void onRecycleItemClick(View view, int position, String itemData) {
                                    onSmallItemClick(view, position, itemData);
                                }
                            };
                            sList.setAdapter(adapter);
                        }
                        sedit.setEnabled(false); //編集、削除ボタン無効化
                        sdl.setEnabled(false);
                        shold.setEnabled(false);
                        sfin.setEnabled(false);
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
                    int smallId;
                    while(next){
                        through:
                        {
                            smallId = scs.getInt(0);
/*                            for (int i = 0; i < smallRoutineData.size(); i++) {
                                if (smallId == smallRoutineData.get(i)) { //小目標に本日こなしたタスクがあれば飛ばす
                                    break through;
                                }
                            }*/
                            HashMap<String, String> item = new HashMap<>();
                            item.put("id", "" + smallId);
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

                            smallTitle.add(String.format(" ・ (%s)-(%s)-%s", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                            smallData.add(item); //中目標データ配列に追加
                        }
                        next = scs.moveToNext();
                    }

                    if(smallData.size()==0){//小目標データが無いとき

                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.smallLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("小目標なし");
                    }else {
                        MyAdapter adapter = new MyAdapter(smallTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onSmallItemClick(view, position, itemData);
                            }
                        };
                        sList.setAdapter(adapter);
                    }
                    sedit.setEnabled(false); //編集、削除ボタン無効化
                    sdl.setEnabled(false);
                    shold.setEnabled(false);
                    sfin.setEnabled(false);

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
                        scheTitle.add(String.format(" ・ %s[%s]",item.get("title"),item.get("date")));
                        next = schecs.moveToNext();
                    }
                    if(scheData.size()==0){ //スケジュールがないとき当日のスケジュール枠を消す
                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout =
                                (ConstraintLayout) requireActivity().findViewById(R.id.scheduleLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("当日スケジュールなし");
                    }else {
                        MyAdapter adapter = new MyAdapter(scheTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onScheItemClick(view, position, itemData);
                            }
                        };
                        scheList.setAdapter(adapter);
                    }
                    scheedit.setEnabled(false); //編集、削除ボタン無効化
                    schedl.setEnabled(false);
                    schehold.setEnabled(false);
                    schefin.setEnabled(false);

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
                    int tdId;
                    while(next){
                        through:
                        {
                            tdId = todocs.getInt(0);
/*                            for (int i = 0; i < todoRoutineData.size(); i++) {
                                if (tdId == todoRoutineData.get(i)) { //小目標に本日こなしたタスクがあれば飛ばす
                                    break through;
                                }
                            }*/
                            HashMap<String, String> item = new HashMap<>();
                            item.put("id", "" + tdId);
                            item.put("title", todocs.getString(1));
                            item.put("content", todocs.getString(2));
                            item.put("important", "" + todocs.getInt(3));
                            item.put("memo", todocs.getString(4));
                            item.put("proceed", "" + todocs.getInt(5));
                            item.put("fin", "" + todocs.getInt(6));
                            todoData.add(item); //中目標データ配列に追加
                            todoTitle.add(String.format(" ・ %s", item.get("title")));
                        }
                        next = todocs.moveToNext();
                    }

                    if(todoData.size()==0){ //TODOリストにデータがないときレイアウトを消す
                        //レイアウトを取得して消去しブランクにする
                        ConstraintLayout layout;
                        layout = (ConstraintLayout) requireActivity().findViewById(R.id.todoLayout);
                        layout.removeAllViews();
                        getLayoutInflater().inflate(R.layout.non_items, layout);
                        TextView non = layout.findViewById(R.id.noItems);
                        non.setText("TODOリストなし");
                    }else {
                        MyAdapter adapter = new MyAdapter(todoTitle) {//リストクリック時の処理
                            @Override
                            void onRecycleItemClick(View view, int position, String itemData) {
                                onToDoItemClick(view, position, itemData);
                            }
                        };
                        todoList.setAdapter(adapter);
                    }
                    todoedit.setEnabled(false);
                    tododl.setEnabled(false);
                    todohold.setEnabled(false);
                    todofin.setEnabled(false);

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
                boolean next = false;
/*                //今日終了済みのデイリータスクIDを取得
                Cursor rcs = db.query("DailyData",new String[]{"dailytaskid","level"},"date=?",new String[]{today},null,null,null,null);
                smallRoutineData.clear();
                todoRoutineData.clear();
                next = rcs.moveToFirst();
                while(next){
                    String levelCheck = rcs.getString(1);
                    if(levelCheck.equals("s"))
                        smallRoutineData.add(rcs.getInt(0));
                    else if(levelCheck.equals("t"))
                        todoRoutineData.add(rcs.getInt(0));
                    next = rcs.moveToNext();
                }*/

                //大目標を取得

                String[] bcols = {"id","title","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] blevel = { "big","0","0" };//大目標、保留なし,未終了データのみを抽出
                Cursor bcs = db.query("ToDoData",bcols,"level=? and hold=? and fin=?",blevel,null,null,null,null);

                bigData.clear(); //いったん配列を空にする
                bigTitle.clear();
                next = bcs.moveToFirst();//カーソルの先頭に移動
                while(next){ //Cursorデータが空になるまでbigTitle,bigDataに加えていく
                    bigTitle.add(" ・ "+bcs.getString(1));//大目標のタイトル
                    HashMap<String,String> item = new HashMap<>();
                    item.put("id",""+bcs.getInt(0));
                    item.put("title",bcs.getString(1));
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
                    middleTitle.add(String.format(" ・ (%s)-%s",item.get("bigtitle"),mcs.getString(1)));

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
                int smallId;
                while(next){
                    through:
                    {
                        smallId = scs.getInt(0);
/*                        for (int i = 0; i < smallRoutineData.size(); i++) {
                            if (smallId == smallRoutineData.get(i)) { //小目標に本日こなしたタスクがあれば飛ばす
                                break through;
                            }
                        }*/
                        HashMap<String, String> item = new HashMap<>();
                        item.put("id", "" + smallId);
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

                        smallTitle.add(String.format(" ・ (%s)-(%s)-%s", item.get("bigtitle"), item.get("middletitle"), item.get("title")));

                        smallData.add(item); //中目標データ配列に追加
                    }
                    next = scs.moveToNext();
                }


                //スケジュールを取得
                String[] schecols = {"id","title","date","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] schelevel = { "schedule","0","0",today };//中目標,保留なし,当日のデータのみを抽出
                Cursor schecs = db.query("ToDoData",schecols,"level=? and hold=? and fin=? and date=?",schelevel,null,null,null,null);

                scheData.clear(); //いったん配列を空にする
                scheTitle.clear();
                next = schecs.moveToFirst();//カーソルの先頭に移動
                while(next){
                        HashMap<String, String> item = new HashMap<>();
                        item.put("id", "" + schecs.getInt(0));
                        item.put("title", schecs.getString(1));
                        item.put("date", "" + schecs.getString(2));
                        item.put("content", schecs.getString(3));
                        item.put("important", "" + schecs.getInt(4));
                        item.put("memo", schecs.getString(5));
                        item.put("proceed", "" + schecs.getInt(6));
                        item.put("fin", "" + schecs.getInt(7));
                        scheData.add(item); //中目標データ配列に追加
                        scheTitle.add(String.format(" ・ %s[%s]", item.get("title"), item.get("date")));

                    next = schecs.moveToNext();
                }

                //やることリストを取得
                String[] todocols = {"id","title","content","important","memo","proceed","fin"};//SQLデータから取得する列
                String[] todolevel = { "todo","0","0" };//中目標,保留なし,未終了データのみを抽出
                Cursor todocs = db.query("ToDoData",todocols,"level=? and hold=? and fin=?",todolevel,null,null,null,null);

                todoData.clear(); //いったん配列を空にする
                todoTitle.clear();
                next = todocs.moveToFirst();//カーソルの先頭に移動
                int tdId;
                while(next){
                    through:
                    {
                        tdId = todocs.getInt(0);
/*                        for (int i = 0; i < todoRoutineData.size(); i++) {
                            if (tdId == todoRoutineData.get(i)) {//TODOリストに本日こなしたタスクがあれば飛ばす
                                break through;
                            }
                        }*/
                        HashMap<String, String> item = new HashMap<>();
                        item.put("id", "" + tdId);
                        item.put("title", todocs.getString(1));
                        item.put("content", todocs.getString(2));
                        item.put("important", "" + todocs.getInt(3));
                        item.put("memo", todocs.getString(4));
                        item.put("proceed", "" + todocs.getInt(5));
                        item.put("fin", "" + todocs.getInt(6));
                        todoData.add(item); //中目標データ配列に追加
                        todoTitle.add(String.format(" ・ %s", item.get("title")));
                    }
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