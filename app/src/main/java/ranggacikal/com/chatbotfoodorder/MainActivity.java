package ranggacikal.com.chatbotfoodorder;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener {

    RecyclerView recyclerView;
    EditText edtMessage;
    RelativeLayout addButton;
    DatabaseReference ref;
    FirebaseRecyclerAdapter<ChatModel, ChatViewHolder> adapter;

    private AIService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv_chat);
        edtMessage = findViewById(R.id.edtChat);
        addButton = findViewById(R.id.btnSend);

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);

        final AIConfiguration config = new AIConfiguration("32564e78740f4ba2bbf0783467737ba4",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        final AIDataService aiDataService = new AIDataService(this, config);
        final AIRequest aiRequest = new AIRequest();

        addButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                String message = edtMessage.getText().toString().trim();

                if (!message.equals("")){
                    final ChatModel chatModel = new ChatModel(message, "user");
                    ref.child("chat").push().setValue(chatModel);

                    aiRequest.setQuery(message);
                    new AsyncTask<AIRequest, Void, AIResponse>(){

                        @Override
                        protected AIResponse doInBackground(AIRequest... aiRequests) {
                            final AIRequest request = aiRequests[0];
                            try{
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            }catch (AIServiceException e){

                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(AIResponse aiResponse) {
                            if(aiResponse != null){
                                Result result = aiResponse.getResult();
                                String reply = result.getFulfillment().getSpeech();
                                ChatModel chatModel1 = new ChatModel(reply, "bot");
                                ref.child("chat").push().setValue(chatModel1);
                            }
                        }
                    }.execute(aiRequest);
                }else{
                    aiService.startListening();
                }
                edtMessage.setText("");
            }
        });

        adapter = new FirebaseRecyclerAdapter<ChatModel, ChatViewHolder>(ChatModel.class, R.layout.chatlist, ChatViewHolder.class, ref.child("chat")) {
            @Override
            protected void populateViewHolder(ChatViewHolder chatViewHolder, ChatModel chatModel, int i) {

                if (chatModel.getMsguser().equals("user")){
                    chatViewHolder.rightText.setText(chatModel.getMsgText());
                    chatViewHolder.rightText.setVisibility(View.VISIBLE);
                    chatViewHolder.leftText.setVisibility(View.GONE);
                }else{
                    chatViewHolder.leftText.setText(chatModel.getMsgText());

                    chatViewHolder.rightText.setVisibility(View.GONE);
                    chatViewHolder.leftText.setVisibility(View.VISIBLE);
                }
            }
        };


        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePoint = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePoint == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePoint == (positionStart - 1))){
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResult(AIResponse response) {

        Result result = response.getResult();

        String message = result.getResolvedQuery();
        ChatModel chatModel = new ChatModel(message, "user");
        ref.child("chat").push().setValue(chatModel);

        String reply = result.getFulfillment().getSpeech();
        ChatModel chatModel1 = new ChatModel(reply, "bot");
        ref.child("chat").push().setValue(chatModel1);
    }

    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}
