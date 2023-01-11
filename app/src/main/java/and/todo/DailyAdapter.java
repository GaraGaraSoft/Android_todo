package and.todo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DailyAdapter extends ArrayAdapter {

    private LayoutInflater dInflater;
    private Context dContext;
    private int dTextViewResourceId;
    private ArrayList<String> dItem;

    public DailyAdapter(Context context, int textViewResourceId,
                        ArrayList<String> objects) {
        super(context, textViewResourceId, objects);
        dContext = context;
        dTextViewResourceId = textViewResourceId;
        dItem = objects;

        dInflater = (LayoutInflater) dContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = dInflater.inflate(dTextViewResourceId, null);
            holder = new ViewHolder();
            holder.dailyTaskDate = (TextView) convertView
                    .findViewById(R.id.dailyTaskDate);
            holder.dailyTaskTitle = (TextView) convertView
                    .findViewById(R.id.dailyTaskTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String itemStr = dItem.get(position);

        // 見出し
        if (!isEnabled(position)) {
            holder.dailyTaskDate.setVisibility(View.VISIBLE);
            holder.dailyTaskDate.setText(itemStr.replace("#", ""));
            holder.dailyTaskTitle.setVisibility(View.GONE);
        } else {
            holder.dailyTaskTitle.setVisibility(View.VISIBLE);
            holder.dailyTaskTitle.setText(itemStr);
            holder.dailyTaskDate.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return !(dItem.get(position).toString().startsWith("#"));
    }

    class ViewHolder {
        TextView dailyTaskDate;
        TextView dailyTaskTitle;

    }
}
