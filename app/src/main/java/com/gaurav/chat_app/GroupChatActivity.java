package com.gaurav.chat_app;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private String currentgroupname, currentuserid, currentusername, currentdate, currenttime;
    private FirebaseAuth mAuth;
    private DatabaseReference usersref,groupnameref, groupmessagekeyref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentgroupname = getIntent().getExtras().get("groupname").toString();

        mAuth = FirebaseAuth.getInstance();
        currentuserid= mAuth.getCurrentUser().getUid();
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");
        groupnameref = FirebaseDatabase.getInstance().getReference().child("Group").child(currentgroupname);

        InitialiseFields();
        GetUserInfo();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmessageinfotodatabase();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupnameref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatdate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatmessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatname = (String) ((DataSnapshot)iterator.next()).getValue();
            String chattime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatname + ":\n" + chatmessage + "\n" + chattime + "   " + chatdate + "\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void sendmessageinfotodatabase() {
        String message = userMessageInput.getText().toString().trim();
        String messagekey = groupnameref.push().getKey();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this,"enter message", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calfordate = Calendar.getInstance();
            SimpleDateFormat currentDataFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentdate = currentDataFormat.format(calfordate.getTime());

            Calendar calfortime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currenttime = currentTimeFormat.format(calfortime.getTime());

            HashMap<String, Object> groupmessagekey = new HashMap<>();
            groupnameref.updateChildren(groupmessagekey);

            groupmessagekeyref = groupnameref.child(messagekey);
            HashMap<String, Object> messageinfomap = new HashMap<>();
            messageinfomap.put("name", currentusername);
            messageinfomap.put("message", message);
            messageinfomap.put("date", currentdate);
            messageinfomap.put("time", currenttime);
            groupmessagekeyref.updateChildren(messageinfomap);

        }
    }

    private void GetUserInfo() {
        usersref.child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentusername = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitialiseFields() {
        mtoolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentgroupname);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
    }
}
