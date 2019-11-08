package com.right.ayomide.tabianconsulting;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.right.ayomide.tabianconsulting.Common.Common;
import com.right.ayomide.tabianconsulting.models.User;
import com.right.ayomide.tabianconsulting.utility.EmployeesAdapter;

import java.util.ArrayList;
import java.util.Set;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";

    //widgets
    private TextView mDepartments;
    private Button mAddDepartment, mSendMessage;
    private RecyclerView mRecyclerView;
    private EditText mMessage, mTitle;

    //vars
    private ArrayList<String> mDepartmentsList;
    private Set<String> mSelectedDepartments;
    private EmployeesAdapter mEmployeeAdapter;
    private ArrayList<User> mUsers;
    private Set<String> mTokens;
    private String mServerKey;
    public static boolean isActivityRunning;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_admin );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        toolbar.setTitle( "Skiplab Innovation" );
        setSupportActionBar( toolbar );
        mDepartments = (TextView) findViewById(R.id.broadcast_departments);
        mAddDepartment = (Button) findViewById(R.id.add_department);
        mSendMessage = (Button) findViewById(R.id.btn_send_message);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mMessage = (EditText) findViewById(R.id.input_message);
        mTitle = (EditText) findViewById(R.id.input_title);

        setupEmployeeList();
        init();
    }

    private void init()
    {
        mAddDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to add new department");
                NewDepartmentDialog dialog = new NewDepartmentDialog();
                dialog.show(getSupportFragmentManager(), getString(R.string.dialog_add_department));
            }
        });

        if (Common.isConnectedToTheInternet( getBaseContext() ))
        {
            getDepartments();
            getEmployeeList();
        }
    }

    /**
     * Get a list of all employees
     * @throws NullPointerException
     */
    private void getEmployeeList()
    {
        Log.d(TAG, "getEmployeeList: getting a list of all employees");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbnode_users));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    Log.d(TAG, "onDataChange: found a user: " + user.getName());
                    mUsers.add(user);
                }
                mEmployeeAdapter.notifyDataSetChanged();
                //getDepartmentTokens();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Setup the list of employees
     */
    private void setupEmployeeList()
    {
        //load deals list
        mUsers = new ArrayList<>();
        mEmployeeAdapter = new EmployeesAdapter( mUsers, AdminActivity.this );
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( this );
        recyclerView.setLayoutManager( layoutManager );
        recyclerView.setAdapter(mEmployeeAdapter);
    }

    void getDepartments() {
    }

    @Override
    public void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityRunning = false;
    }

    public void setDepartmentDialog(final User user)
    {
        Log.d(TAG, "setDepartmentDialog: setting the department of: " + user.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
        builder.setIcon(R.drawable.ic_departments);
        builder.setTitle("Set a Department for " + user.getName() + ":");

        builder.setPositiveButton("done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        builder.show();
    }
}
