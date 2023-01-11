package and.todo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class FinDialogFragment extends DialogFragment {
    public static FinDialogFragment newInstance(Bundle Data){//インスタンス作成時にまず呼び出す
        // インスタンス生成
        FinDialogFragment fragment = new FinDialogFragment ();

        //Bundleで送られたデータを設定
        fragment.setArguments(Data);

        return fragment;
    }

    public interface FinDialogListener {
        public void onFinDialogPositiveClick(DialogFragment dialog);
        public void onFinDialogNegativeClick(DialogFragment dialog);
        public void onFinDialogNeutralClick(DialogFragment dialog);
    }

    FinDialogFragment.FinDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        listener = (FinDialogFragment.FinDialogListener) getTargetFragment();

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle data = getArguments(); //Bundleデータ取得
        String title = data.getString("title");

        FinDialogFragment.DialogClickListener listener =
                new FinDialogFragment.DialogClickListener();

        Dialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle("完了")
                .setMessage(title+"を完了にしますか？")
                .setPositiveButton("OK", listener)
                .setNegativeButton("キャンセル", listener)
                .setNeutralButton("あとで", listener)
                .create();

        return dialog;
    }



    private class DialogClickListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface d,int w){

            //switchにてタップされたボタンでの条件分岐を行う
            switch(w){
                //OKボタンがタップされたとき
                case DialogInterface.BUTTON_POSITIVE:
                    //OKボタンが押されたときのメソッドを呼び出し
                    //処理は継承先のMainActivityで実行
                    listener.onFinDialogPositiveClick(FinDialogFragment.this);
                    break;
                //Cancelボタンがタップされたとき
                case DialogInterface.BUTTON_NEGATIVE:
                    //Cancelボタンが押されたときのメソッドを呼び出し
                    //処理は継承先のMainActivityで実行
                    listener.onFinDialogNegativeClick(FinDialogFragment.this);
                    break;
                //Neutralボタンがタップされたとき
                case DialogInterface.BUTTON_NEUTRAL:
                    //Neutralボタンが押されたときのメソッドを呼び出し
                    //処理は継承先のMainActivityで実行
                    listener.onFinDialogNeutralClick(FinDialogFragment.this);
                    break;
            }

        }
    }

}
