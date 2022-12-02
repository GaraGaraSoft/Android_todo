package and.todo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateDialogFragment extends DialogFragment{
    private DateDialogListener listener;

    // インスタンスを生成するメソッド
    public static DateDialogFragment newInstance() {
        return new DateDialogFragment();
    }

    public interface DateDialogListener {
        void onDateDialog(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (DateDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            // 親フラグメントがインターフェースを実装していない場合は例外を投げる
            throw new ClassCastException(getTargetFragment().toString() + "はインターフェースを実装していません");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar cal = Calendar.getInstance();
        Activity activity = requireActivity();


        EditText editDate = activity.findViewById(R.id.editDate); //日付入力

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try{
            cal.setTime(sdf.parse(editDate.getText().toString()));

        }catch(ParseException e){
            e.printStackTrace();
        }

        Dialog dialog = new DatePickerDialog(
                activity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                listener.onDateDialog(datePicker, year, monthOfYear, dayOfMonth);
            }
        },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        return dialog;
    }


}
