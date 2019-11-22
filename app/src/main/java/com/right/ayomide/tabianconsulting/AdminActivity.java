package com.right.ayomide.tabianconsulting;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.right.ayomide.tabianconsulting.Common.Common;
import com.right.ayomide.tabianconsulting.Interface.ItemClickListener;
import com.right.ayomide.tabianconsulting.models.User;
import com.right.ayomide.tabianconsulting.utility.EmployeeViewHolder;
import com.right.ayomide.tabianconsulting.utility.EmployeesAdapter;
import com.right.ayomide.tabianconsulting.utility.FCM;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.text.TextUtils.isEmpty;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/";

    private ProgressDialog mProgressDialog;

    //widgets
    private TextView mDepartments;
    private Button mAddDepartment, mSendMessage;
    private EditText mMessage, mTitle;

    //vars
    private ArrayList<String> mDepartmentsList;
    private Set<String> mSelectedDepartments;
    private EmployeesAdapter mEmployeeAdapter;
    private ArrayList<User> mUsers;
    private Set<String> mTokens;
    private String mServerKey;
    public static boolean isActivityRunning;

    FirebaseDatabase db;
    DatabaseReference User;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseRecyclerAdapter<User, EmployeeViewHolder> adapter;

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
                    viewHolder.profileImage.setImageDrawable( ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_no_image));
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
        mSelectedDepartments = new HashSet<>();
        mTokens = new HashSet<>();

        mAddDepartment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening dialog to add new department");
                NewDepartmentDialog dialog = new NewDepartmentDialog();
                dialog.show(getSupportFragmentManager(), getString(R.string.dialog_add_department));
            }
        });

         /*
            --------- Dialog for selecting departments ---------
         */
        mDepartments.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: opening departments selector dialog.");

                AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
                builder.setIcon(R.drawable.ic_departments);
                builder.setTitle("Select Departments:");

                //create an array of the departments
                String[] departments = new String[mDepartmentsList.size()];
                for(int i = 0; i < mDepartmentsList.size(); i++){
                    departments[i] = mDepartmentsList.get(i);
                }

                boolean[] checked = new boolean[mDepartmentsList.size()];
                for(int i = 0; i < mDepartmentsList.size(); i++){
                    if(mSelectedDepartments.contains(mDepartmentsList.get(i))){
                        checked[i] = true;
                    }
                }

                builder.setPositiveButton( "done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                } );

                builder.setMultiChoiceItems(departments, checked,  new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                        if(isChecked){
                            Log.d(TAG, "onClick: adding " + mDepartmentsList.get(position) + " to the list.");
                            mSelectedDepartments.add(mDepartmentsList.get(position));
                        }else{
                            Log.d(TAG, "onClick: removing " + mDepartmentsList.get(position) + " from the list.");
                            mSelectedDepartments.remove(mDepartmentsList.get(position));
                        }
                    }
                });

                AlertDialog dialogInterface = builder.create();
                dialogInterface.show();

                dialogInterface.setOnDismissListener( new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        Log.d(TAG, "onDismiss: dismissing dialog and refreshing token list.");
                        getDepartmentTokens();
                    }
                } );
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

        getDepartments();
        getServerKey();
    }

    /**
     * Retrieves the server key for the Firebase server.
     * This is required to send FCM messages.
     */
    private void getServerKey()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbnode_server))
                .orderByValue();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: got the server key.");
                DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                mServerKey = singleSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessageToDepartment(String title, String message)
    {
        Log.d(TAG, "sendMessageToDepartment: sending message to selected departments.");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl( BASE_URL )
                .addConverterFactory( GsonConverterFactory.create() )
                .build();

        FCM fcmAPI = retrofit.create( FCM.class );
    }


    /**
     * Get all the tokens of the users who are in the selected departments
     */
    private void getDepartmentTokens()
    {
        Log.d(TAG, "getDepartmentTokens: searching for tokens.");
        //mTokens.clear(); //clear current token list in case admin has change departments
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        for(String department: mSelectedDepartments){
            Log.d(TAG, "getDepartmentTokens: department: " + department);

            Query query = reference.child(getString(R.string.dbnode_users))
                    .orderByChild(getString(R.string.field_department))
                    .equalTo(department);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                        String token = snapshot.getValue(User.class).getMessaging_token();
                        Log.d(TAG, "onDataChange: got a token for user named: "
                                + snapshot.getValue(User.class).getName());
                        mTokens.add(token);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * Retrieve a list of departments that have been added to the database.
     */
    public void getDepartments(){
        mDepartmentsList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.dbnode_departments));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String department = snapshot.getValue().toString();
                    Log.d(TAG, "onDataChange: found a department: " + department);
                    mDepartmentsList.add(department);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
}
