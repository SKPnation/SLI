package com.right.ayomide.tabianconsulting;

import android.app.ProgressDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.right.ayomide.tabianconsulting.Common.Common;
import com.right.ayomide.tabianconsulting.Interface.ItemClickListener;
import com.right.ayomide.tabianconsulting.models.User;
import com.right.ayomide.tabianconsulting.utility.EmployeeViewHolder;
import com.right.ayomide.tabianconsulting.utility.EmployeesAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.text.TextUtils.isEmpty;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";

    private ProgressDialog mProgressDialog;

    //widgets
    private TextView mDepartments;
    private Button mAddDepartment, mSendMessage;
    private RecyclerView mRecyclerView;
    private EditText mMessage, mTitle;

    //vars
    private List<String> mDepartmentsList;
    private Set<String> mSelectedDepartments;
    private EmployeesAdapter mEmployeeAdapter;
    private ArrayList<User> mUsers;
    private Set<String> mTokens;
    private String mServerKey;
    public static boolean isActivityRunning;

    FirebaseDatabase db;
    DatabaseReference User;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<User, EmployeeViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_admin );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        toolbar.setTitle( "Skiplab Innovation" );
        setSupportActionBar( toolbar );

        db = FirebaseDatabase.getInstance();
        User = db.getReference("users");

        mDepartments = (TextView) findViewById(R.id.broadcast_departments);
        mAddDepartment = (Button) findViewById(R.id.add_department);
        mSendMessage = (Button) findViewById(R.id.btn_send_message);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mMessage = (EditText) findViewById(R.id.input_message);
        mTitle = (EditText) findViewById(R.id.input_title);

        //Load employees
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(Common.isConnectedToTheInternet(getBaseContext()))
        {
            loadEmployees();
        }
        else
        {
            Toast.makeText(getBaseContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
            return;
        }

        mDepartmentsList = new ArrayList<>();

        //setupEmployeeList();
        init();


    }

    /**
     * Get a list of all employees
     */
    private void loadEmployees()
    {
        adapter = new FirebaseRecyclerAdapter<User, EmployeeViewHolder>(
                User.class, R.layout.layout_employee_list, EmployeeViewHolder.class, User) {
            @Override
            protected void populateViewHolder(EmployeeViewHolder viewHolder, User model, int position) {

                Log.d(TAG, "getEmployeeList: getting a list of all employees");
                viewHolder.name.setText( model.getName() );
                if (model.getProfile_image().isEmpty())
                {
                    viewHolder.profileImage.setImageDrawable( ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_android));
                }
                else
                    {
                        Picasso.with( AdminActivity.this ).load( model.getProfile_image() ).into( viewHolder.profileImage );
                    }
                viewHolder.department.setText( model.getDepartment() );

                viewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Log.d(TAG, "onClick: selected employee: " + adapter.getItem( position ).getUser_id() + " " + adapter.getItem( position ).getName());

                        //setDepartmentDialog(adapter.getRef( position ).getKey(), adapter.getItem( position ));
                    }
                } );
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter( adapter );
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

        mDepartments.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: opening departments selector dialog.");

                AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
                builder.setIcon(R.drawable.ic_departments);
                builder.setTitle("Select Departments:");

                builder.show();
            }
        } );

        mSendMessage.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: attempting to send the message.");
                String message = mMessage.getText().toString();
                String title = mTitle.getText().toString();
                if(!isEmpty(message) && !isEmpty(title)){

                    //send message
                    sendMessageToDepartment(title, message);

                    mMessage.setText("");
                    mTitle.setText("");
                }else{
                    Toast.makeText(AdminActivity.this, "Fill out the title and message fields", Toast.LENGTH_SHORT).show();
                }
            }
        } );
    }

    private void sendMessageToDepartment(String title, String message)
    {
        //...
    }


    /**
     * Get all the tokens of the users who are in the selected departments
     */
    //private void getDepartmentTokens() {}


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
}
