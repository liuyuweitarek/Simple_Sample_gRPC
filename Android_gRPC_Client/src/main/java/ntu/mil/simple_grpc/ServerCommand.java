package ntu.mil.simple_grpc;

public class ServerCommand {
    private String intent;
    private String value;

    public ServerCommand(String intent, String value){
        this.intent = intent;
        this.value = value;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ServerCommand{" +
                "intent='" + intent + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
