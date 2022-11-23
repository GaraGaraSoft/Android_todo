package and.todo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button top = findViewById(R.id.topButton); //Topボタン要素を取得
        top.setOnClickListener(v->{ //TOPを再生成
            // FragmentManagerのインスタンス生成
            FragmentManager fragmentManager = getSupportFragmentManager();
            // FragmentTransactionのインスタンスを取得
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // インスタンスに対して張り付け方を指定する

            Bundle sendData = new Bundle(); //データやり取り用のBundleデータ
            fragmentTransaction.replace(R.id.MainFragment, TopFragment.newInstance(sendData));
            // 張り付けを実行
            fragmentTransaction.commit();

        });

        if(savedInstanceState == null) {
            // FragmentManagerのインスタンス生成
            FragmentManager fragmentManager = getSupportFragmentManager();
            // FragmentTransactionのインスタンスを取得
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // インスタンスに対して張り付け方を指定する

            Bundle sendData = new Bundle(); //データやり取り用のBundleデータ
            fragmentTransaction.replace(R.id.MainFragment, TopFragment.newInstance(sendData));
            // 張り付けを実行
            fragmentTransaction.commit();

        }

        ConstraintLayout menuLayout;
        menuLayout = (ConstraintLayout) findViewById(R.id.menuLayout);
        menuLayout.removeAllViews();
        getLayoutInflater().inflate(R.layout.fragment_menu, menuLayout);

        Button hold = menuLayout.findViewById(R.id.holdButton); //保留ボタン取得
        hold.setOnClickListener(v->{
            //保留データ画面へ飛ばす

            // BackStackを設定
            FragmentManager fragmentManager = getSupportFragmentManager();
            // FragmentTransactionのインスタンスを取得
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);

            // パラメータを設定
            fragmentTransaction.replace(R.id.MainFragment,
                    new HoldFragment());
            fragmentTransaction.commit();

        });

        Button log = menuLayout.findViewById(R.id.logButton); //ログボタン取得
        log.setOnClickListener(v->{
            //ログ画面へ飛ばす

            // BackStackを設定
            FragmentManager fragmentManager = getSupportFragmentManager();
            // FragmentTransactionのインスタンスを取得
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);

            // パラメータを設定
            fragmentTransaction.replace(R.id.MainFragment,
                    new LogFragment());
            fragmentTransaction.commit();

        });

        Button btnFin = menuLayout.findViewById(R.id.btnFin); //完了ボタン取得
        btnFin.setOnClickListener(v->{
            //完了タスク画面へ飛ばす

            // BackStackを設定
            FragmentManager fragmentManager = getSupportFragmentManager();
            // FragmentTransactionのインスタンスを取得
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);

            // パラメータを設定
            fragmentTransaction.replace(R.id.MainFragment,
                    new FinishFragment());
            fragmentTransaction.commit();

        });

        Button sche = menuLayout.findViewById(R.id.scheButton);
        sche.setOnClickListener(v->{
            //スケジュール画面へ飛ばす

            // BackStackを設定
            FragmentManager fragmentManager = getSupportFragmentManager();
            // FragmentTransactionのインスタンスを取得
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);

            // パラメータを設定
            fragmentTransaction.replace(R.id.MainFragment,
                    new ScheduleFragment());
            fragmentTransaction.commit();

        });

        // 新規追加ボタン要素を取得
        Button newButton = (Button) menuLayout.findViewById(R.id.newButton);
        // Buttonのクリックした時の処理を書きます
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //新規登録画面へ飛ばす

                // BackStackを設定
                FragmentManager fragmentManager = getSupportFragmentManager();
                // FragmentTransactionのインスタンスを取得
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);

                // パラメータを設定
                fragmentTransaction.replace(R.id.MainFragment,
                        new NewFragment());
                fragmentTransaction.commit();

            }
        });


    }
}