package uni.processor.output;

import uni.processor.input.InputMessage;

public class OutputMessage  {
    
    public InputMessage msg;

    public OutputMessage(InputMessage msg) {
        this.msg = msg;
    }

    public boolean isLateEvent() {
        return msg.createdAt.after(msg.sampledAt);
    }
}
