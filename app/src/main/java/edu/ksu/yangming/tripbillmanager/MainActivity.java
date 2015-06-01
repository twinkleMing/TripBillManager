package edu.ksu.yangming.tripbillmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {


    private ListView mAccountViews;
    private ArrayAdapter<String> adapter;
    List<String> account_strs = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<Data.Account> accounts = ((Data)getApplication()).accounts;
        for (Data.Account acc : accounts) {
            account_strs.add(acc.account_name);
        }
        mAccountViews = (ListView) this.findViewById(R.id.accounts);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, account_strs);
        mAccountViews.setAdapter(adapter);
        mAccountViews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int itemPosition = position;
                Bundle accountBundle = new Bundle();
                accountBundle.putInt("account", itemPosition);
                Intent intent = new Intent(MainActivity.this, AccountManageActivity.class);
                intent.putExtras(accountBundle);
                startActivity(intent);
                adapter.notifyDataSetChanged();
            }
        });


        final Button createAccount = (Button) findViewById(R.id.create_account);
        // createUser.setText("create user");
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccountDialog();
            }
        });
    }

    protected void createAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Account Name");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        builder.setView(input);

        // Set up the buttons
        AlertDialog.Builder ok = builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String acc_name = input.getText().toString();
                Data.Account account = new Data.Account(acc_name);
                ((Data)getApplication()).accounts.add(account);
                account_strs.add(acc_name);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
