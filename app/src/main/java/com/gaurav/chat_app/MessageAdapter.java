package com.gaurav.chat_app;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewholder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersref;

    public MessageAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout, viewGroup, false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewholder messageViewholder, int i) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        String fromuserid = messages.getFrom();
        String fromMessageType = messages.getType();

        usersref = FirebaseDatabase.getInstance().getReference().child("Users").child(fromuserid);
        usersref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image")){
                    String receiverimage = dataSnapshot.child("image").getValue().toString();
                    //Picasso.get().load(receiverimage).placeholder(R.drawable.profile_image).into(messageViewholder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(fromMessageType.equals("text")){
            messageViewholder.receiverMessageText.setVisibility(View.INVISIBLE);
            //messageViewholder.receiverProfileImage.setVisibility(View.INVISIBLE);
            messageViewholder.senderMessageText.setVisibility(View.INVISIBLE);

            if(fromuserid.equals(messageSenderId)){
                messageViewholder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewholder.senderMessageText.setBackgroundResource(R.drawable.my_message);
                messageViewholder.senderMessageText.setText(messages.getMessage());
            }
            else {
                //messageViewholder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewholder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewholder.receiverMessageText.setBackgroundResource(R.drawable.their_message);
                messageViewholder.receiverMessageText.setText(messages.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewholder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;
        //public CircleImageView receiverProfileImage;

        public MessageViewholder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            //receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }
}
