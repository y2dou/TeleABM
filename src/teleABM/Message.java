package teleABM;

public interface Message {
public enum MessageType { MARKET_PRICES}
	
	public MessageType getMessageType();
}
