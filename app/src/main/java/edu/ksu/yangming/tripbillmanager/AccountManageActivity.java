package edu.ksu.yangming.tripbillmanager;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.List;


public class AccountManageActivity extends Activity {
    private ListView mEntryViews;
    private ArrayAdapter<String> adapter;
    private Data.Account account;
    List<String> entry_strs = new ArrayList<String>();
    private Bundle bundle;
    private static final int ENTRY_MANAGE_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manage);
        bundle = getIntent().getExtras();
        int pos = bundle.getInt("account");
        account = ((Data)getApplication()).accounts.get(pos);

        final Button manageUsers = (Button) findViewById(R.id.manage_user);
        manageUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountManageActivity.this, UserManageActivity.class);
                intent.putExtras(bundle);
                startActivity( intent);
            }
        });
        final Button createEntry = (Button) findViewById(R.id.create_entry);
        // createUser.setText("create user");
        createEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle entryBundle = bundle;
                int entryPos = account.entries.size();
                entryBundle.putInt("entry", entryPos);
                Intent intent = new Intent(AccountManageActivity.this, EntryManageActivity.class);
                intent.putExtras(entryBundle);
                startActivityForResult(intent, ENTRY_MANAGE_REQUEST);

            }
        });
        for (Data.Entry entry : account.entries) {
            entry_strs.add(entry.toString());
        }
        mEntryViews = (ListView) this.findViewById(R.id.entries);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, entry_strs);
        mEntryViews.setAdapter(adapter);
        mEntryViews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int itemPosition = position;
                Bundle entryBundle = bundle;
                entryBundle.putInt("entry", itemPosition);
                Intent intent = new Intent(AccountManageActivity.this, EntryManageActivity.class);
                intent.putExtras(entryBundle);
                startActivityForResult(intent, ENTRY_MANAGE_REQUEST);

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ENTRY_MANAGE_REQUEST) {
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    boolean isSave = res.getBoolean("isSave");
                    int pos = res.getInt("entry");
                    if (isSave) {
                        if (pos == entry_strs.size()) {
                            entry_strs.add(account.entries.get(pos).toString());
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            if (!entry_strs.get(pos).equals(account.entries.get(pos).toString())) {
                                entry_strs.set(pos, account.entries.get(pos).toString());
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                    else {
                        entry_strs.remove(pos);
                        adapter.notifyDataSetChanged();
                    }
                }
        }
    }

}
