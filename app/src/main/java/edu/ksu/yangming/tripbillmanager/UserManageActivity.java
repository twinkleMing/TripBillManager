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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;


public class UserManageActivity extends Activity {
    private ListView users;
    private ArrayAdapter<String> adapter;
    private Data.Account account;
    List<String> user_names;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);
        Bundle b = getIntent().getExtras();
        int pos = b.getInt("itemPos");
        account = ((Data)getApplication()).accounts.get(pos);
        users = (ListView) findViewById(R.id.users);
        user_names = account.users();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, user_names);
        users.setAdapter(adapter);
        users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int itemPosition = position;
                String itemValue = (String) users.getItemAtPosition(position);
                createUserDialog(itemPosition, itemValue);
                adapter.notifyDataSetChanged();
            }
        });
        final Button addUser = (Button) findViewById(R.id.add_user);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUserDialog(-1, new String());

                
            }
        });

    }

    protected void createUserDialog(final int itemPosition, final String originalName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setText(originalName);
        builder.setView(input);

        // Set up the buttons
        AlertDialog.Builder ok = builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                if (originalName.length() == 0)  {
                    account.addUser(newName);
                    user_names.add(newName);
                }
                else  {
                    account.changeUser(originalName, newName);
                    user_names.set(itemPosition, newName);
                }
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
