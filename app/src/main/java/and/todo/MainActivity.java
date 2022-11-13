package and.todo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.Button;

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

    }
}