package com.example.main_activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Message;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;


public class MainActivity extends AppCompatActivity implements RoomListener {

    // Each user will have their own specific room
    // When a message is sent, their emergency contacts are pulled from the database and added
    // to the room as listeners so that they receive the message
    // Note that the name must be of the syntax 'observable-' in order to send user information
    // along with the message, which I need to update and do myself
    private String channelID = "HlNGizknmgqJqlv1";
    private String roomName = "observable-room";
    private Scaledrone scaledrone;
    private String realName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int name = (int) (Math.random() * 100);
        this.realName = Integer.toString(name);

        MemberData member = new MemberData(realName);
        scaledrone = new Scaledrone(channelID, member);
        scaledrone.connect(new Listener() {
            @Override
            public void onOpen() {
                System.out.println("Scaledrone connection open");
                scaledrone.subscribe(roomName, MainActivity.this);
            }

            @Override
            public void onOpenFailure(Exception ex) {
                System.out.println("failed open");
            }

            @Override
            public void onFailure(Exception ex) {
                System.out.println("Here is the failure for some reason");
            }

            @Override
            public void onClosed(String reason) {
                System.err.println("closed");
            }
        });
    }

    public void onSafeButtonClick(android.view.View view) {
        // Pull from database to get all of their emergency contacts and make them all listen
        // To this room specifically, then send the message to them.
        // Maybe disconnect them from the chat after they receive the message???
        // THIS WON'T WORK, NEED ANOTHER WAY MAYBE MADE WHEN THE EMERGENCY CONTACT LIST IS
        // This will be the fake emergency contacts

        /*String friend1 = "Mom";
        String friend2 = "Dad";
        String friend3 = "Best Friend";
        HashSet<String> contacts = new HashSet<String>();
        contacts.add(friend1);
        contacts.add(friend2);
        contacts.add(friend3);
        */

        Button button = findViewById(R.id.sendSafe);
        button.setText("You alerted your emergency contacts");
        // Only able to click the button once now. Will need to change this at some point
        button.setClickable(false);

        System.out.println("Button is clicked");
        scaledrone.publish(roomName, "I'm Safe");
    }

    @Override
    public void onOpen(Room room) {
        System.out.println("connected");
    }

    @Override
    public void onOpenFailure(Room room, Exception ex) {
        System.out.println("failed open here though");
    }


    @Override
    public void onMessage(Room room, Message message) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final MemberData otherMember = mapper.treeToValue(message.getMember().getClientData(), MemberData.class);
            if (!message.getClientID().equals(scaledrone.getClientID())) {
                System.out.println("This was the member: " + otherMember.getName());
                final String hold = realName + ": " + message.getData().asText();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changePage(hold);
                    }
                });
            }
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void changePage(String message) {
        TextView text = findViewById(R.id.change);
        text.setText(message);
    }
}
