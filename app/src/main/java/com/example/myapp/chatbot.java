package com.example.myapp;

import static androidx.core.content.ContextCompat.getMainExecutor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;

public class chatbot extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private EditText editText;
    private Button sendButton;
    private ImageButton backButton;
    private List<String> messageList = new ArrayList<>();

    private static final String API_KEY = "AIzaSyCPI2IuX6eqAc7NMa8ePAg0l3O9LMvXcXU";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        editText = findViewById(R.id.edit_text);
        sendButton = findViewById(R.id.button_send);
        backButton = findViewById(R.id.backButton);
        recyclerView = findViewById(R.id.recycler_view);

        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        generateInitialWorkout();

        sendButton.setOnClickListener(v -> {
            String userInput = editText.getText().toString();
            if (!userInput.isEmpty()) {
                sendMessage(userInput);
                editText.setText("");
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void generateInitialWorkout() {
        String prompt = "Generate a random workout word and its definition.";
        modelCall(prompt);
    }

    private void sendMessage(String message) {
        // Add user message to the chat
        messageList.add("You: " + message);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        modelCall(message);
    }

    @SuppressLint("NewApi")
    private void modelCall(String userMessage) {
        GenerativeModel gm =
                new GenerativeModel(
                        "gemini-1.5-flash",
                        API_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder().addText(userMessage).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String resultText = result.getText();
                        // Add bot response to the chat
                        messageList.add("Bot: " + resultText);
                        runOnUiThread(() -> {
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            recyclerView.scrollToPosition(messageList.size() - 1);
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("ChatbotActivity", "Error calling model", t);
                    }
                },
                getMainExecutor());
    }
}
