package ranggacikal.com.chatbotfoodorder;

public class ChatModel {

    private String msgText;
    private String msguser;

    public ChatModel(String msgText, String msguser) {
        this.msgText = msgText;
        this.msguser = msguser;
    }

    public ChatModel(){

    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public String getMsguser() {
        return msguser;
    }

    public void setMsguser(String msguser) {
        this.msguser = msguser;
    }
}
