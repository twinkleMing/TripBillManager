package edu.ksu.yangming.tripbillmanager;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 6/2/15.
 */
public class UserBillPayAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> users;
    private double billSum;
    private List<Double> billSplits;
    public UserBillPayAdapter(Context context, int resource, List<String> users, double sum) {
        super(context, resource, users);
        this.context = context;
        this.users = users;
        this.billSum = sum;
        billSplits = new ArrayList<Double>();
        int n = users.size();
        double split = Math.round(billSum / (double) n * 100) / 100;
        for (int i = 0; i < n; ++i) billSplits.add(split);
    }

    public View getView(final int position,  View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_view_user_billpay, parent, false);
        TextView billUser = (TextView) rowView.findViewById(R.id.bill_user);
        billUser.setText(users.get(position));
        TextView billNum = (TextView) rowView.findViewById(R.id.bill_num);
        billNum.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        billNum.setText(Double.toString(billSplits.get(position)));
        return rowView;
    }

    public void changeSum(double sum) {
        this.billSum = sum;
        int n = billSplits.size();
        double split = Math.round(billSum / (double) n * 100) / 100;
        for (int i = 0; i < n; ++i) billSplits.set(i, split);
        notifyDataSetChanged();
    }

}
