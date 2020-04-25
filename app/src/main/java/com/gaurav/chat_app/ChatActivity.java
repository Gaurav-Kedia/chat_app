package com.gaurav.chat_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String messagereceiverid, messagereceivername, messagereceiverimage, messagesenderid;
    private TextView username, userlastseen;
    private CircleImageView userimage;
    private Toolbar chat_toolbar;
    private ImageButton sendmessagebutton;
    private EditText messageinputtext;
    private FirebaseAuth mAuth;
    private DatabaseReference rootref;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messagesenderid = mAuth.getCurrentUser().getUid();
        rootref = FirebaseDatabase.getInstance().getReference();

        messagereceiverid = getIntent().getExtras().get("visit_user_id").toString();
        messagereceivername = getIntent().getExtras().get("visit_user_name").toString();
        messagereceiverimage = getIntent().getExtras().get("visit_image").toString();

        initialisecontrollers();
        username.setText(messagereceivername);
        Picasso.get().load(messagereceiverimage).placeholder(R.drawable.profile_image).into(userimage);

        sendmessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmessage();
            }
        });
    }

    private void initialisecontrollers() {
        chat_toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chat_toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutinflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarview = layoutinflater.inflate(R.layout.custom_chat_bar, null);
        actionbar.setCustomView(actionbarview);

        username = (TextView) findViewById(R.id.custom_profile_name);
        userlastseen = (TextView) findViewById(R.id.custom_user_last_seen);
        userimage = (CircleImageView) findViewById(R.id.custom_profile_image);
        sendmessagebutton = (ImageButton) findViewById(R.id.send_message_btn);
        messageinputtext = (EditText) findViewById(R.id.input_message);
        loadingbar = new ProgressDialog(this);
        loadingbar.setTitle("Loading");
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.show();

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        rootref.child("Messages").child(messagesenderid).child(messagereceiverid)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                        loadingbar.dismiss();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
        loadingbar.dismiss();
    }

    private void sendmessage(){
        String messagetext = messageinputtext.getText().toString().trim();
        if(TextUtils.isEmpty(messagetext)){}
        else {
            String messagesenderref = "Messages/" + messagesenderid + "/" + messagereceiverid;
            String messagereceiverref = "Messages/" + messagereceiverid + "/" + messagesenderid;

            DatabaseReference usermessagekeyref = rootref.child("Messages")
                    .child(messagesenderid).child(messagereceiverid).push();

            String messagepushid = usermessagekeyref.getKey();
            Map messagetextbody = new HashMap();
            messagetextbody.put("message",messagetext);
            messagetextbody.put("type","text");
            messagetextbody.put("from",messagesenderid);

            Map messagebodydetails = new HashMap();
            messagebodydetails.put(messagesenderref + "/" + messagepushid, messagetextbody);
            messagebodydetails.put(messagereceiverref + "/" + messagepushid, messagetextbody);

            rootref.updateChildren(messagebodydetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){}
                    else {
                        String msg = task.getException().toString();
                        Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                    messageinputtext.setText("");
                }
            });
        }
    }

    private void DisplayLastSeen(){
        rootref.child("Users").child(messagesenderid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("userState").hasChild("state")){
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();

                            if(state.equals("online")){
                                userlastseen.setText("online");
                            }
                            else if(state.equals("offline")){
                                userlastseen.setText("Last seen : " + date + " " + time);
                            }
                        }
                        else {
                            userlastseen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
