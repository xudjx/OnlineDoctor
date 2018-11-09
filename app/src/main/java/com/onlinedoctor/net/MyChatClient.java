package com.onlinedoctor.net;

import com.onlinedoctor.application.MyApplication;
import com.onlinedoctor.log.Logger;
import com.onlinedoctor.pojo.Common;
import com.onlinedoctor.pojo.RunDataContainer;
import com.onlinedoctor.pojo.chats.SimpleMessageDTO;
import com.onlinedoctor.util.JsonUtil;
import com.onlinedoctor.util.NetworkUtil;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public class MyChatClient extends org.java_websocket.client.WebSocketClient {

	private static final String TAG = "JavaWebsocket";

	private static volatile boolean IsConnectting = false;

	private AtomicReference<MyChatClient> r = new AtomicReference<MyChatClient>();;
	private ChatClientCallBack callBack; // 连接建立成功与否的回调方法

	public static final int CODE_CONNECT_OK = 0;
	public static final int CODE_TOKEN_INVALID = -1;
	public static final int CODE_TIMEOUT = -2;
	public static final int CODE_UNREACHABLE = -3;

	public static final String ERROR_TOKEN_INVALID = "ERROR_TOKEN_INVALID";
	public static final String ERROR_TIMEOUT = "ERROR_TIMEOUT";
	public static final String ERROR_UNREACHABLE = "Network Unreachable";

	private StringBuffer resultMsgBuffer = new StringBuffer();

	public MyChatClient(URI uri, ChatClientCallBack callBack) {
		this(uri, new Draft_17());
		Logger.d(TAG, "MyChatClient Create " + uri.toString());
		this.callBack = callBack;
		this.mConnect();
	}

	public MyChatClient(URI serverURI) {
		super(serverURI);
	}

	public MyChatClient(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	private void mConnect() {
		MyApplication.lock.lock();
		if(IsConnectting){
			return;
		}
		IsConnectting = true;
		MyApplication.lock.unlock();
		try {
			if(NetworkUtil.checkNetworkStatus(MyApplication.context)){
				this.connect();
			}else{
				IsConnectting = false;
				callBack.handle(CODE_UNREACHABLE, ERROR_TIMEOUT);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			IsConnectting = false;
		}
		return;
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		Logger.d(TAG, "Open LocalAddress: " + this.getLocalSocketAddress(this.getConnection()));
		Logger.d(TAG, "Open RemoteAddress: " + this.getRemoteSocketAddress(this.getConnection()));
		r.set(this);
		MyApplication.sendChatClient(this);
		IsConnectting = true;
		callBack.handle(CODE_CONNECT_OK, null);
		new Thread(new Runnable() {
			@Override
			public void run() {
				RunDataContainer.getContainer().sendSynAtnMessage();
			}
		}).start();
	}

	/* 测试方法，不停的发送消息
	 * JavaWebSocket能够保持一分钟的连接，所以心跳包的时间设置为30s
	 */
	public void beChatty(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleMessageDTO pingMessage = new SimpleMessageDTO("ping");
				String msg = JsonUtil.objectToJson(pingMessage);
				try{
					while (r.get().getConnection() != null) {
						Logger.i(TAG,"<ChatClient: keep alive:> " + msg);
						r.get().send(msg);
						Thread.sleep(30000);
					}
				}catch(Exception e){
					e.printStackTrace();
					return;
				}
			}
		}).start();
	}


	@Override
	public void onMessage(String message) {
		try{
			Logger.i(TAG, "MyChatClient receive message"+message.length());
			WebMessageDispatcher.getDispatcher().dispatchMessage(message);
		}catch (Exception e){
			e.printStackTrace();
			return;
		}
	}

	/* 对于比较长的数据，JavaWebSocket是以数据帧的形式发送给客户端
	 */
	@Override
	public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
		try {
			System.out.println(frame.isFin());
			if (frame.isFin()) {
				resultMsgBuffer.append(new String(frame.getPayloadData().array(), "UTF-8"));
				Logger.i(TAG, "receive msg length: " + resultMsgBuffer.length());
				// 交给数据分发函数处理
				WebMessageDispatcher.getDispatcher().dispatchMessage(resultMsgBuffer.toString());

				// 准备接收下一个函数
				resultMsgBuffer = new StringBuffer();
			} else {
				resultMsgBuffer.append(new String(frame.getPayloadData().array(), "UTF-8"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClose(int i, String s, boolean b) {
		Logger.i(TAG, "Websocket Close reason:" + s);
		if(b){
			Logger.i(TAG, "+++ Websocket Close from " + r.get().getConnection().getRemoteSocketAddress());
		} else {
			Logger.i(TAG, "+++ Websocket Close from " + r.get().getConnection().getLocalSocketAddress());
		}
		IsConnectting = false;
		MyApplication.chatClientDisconnect();
	}

	@Override
	public void onError(Exception e) {
		if(e instanceof SocketException){
			callBack.handle(CODE_UNREACHABLE, ERROR_UNREACHABLE);
		}
		else if(e instanceof SocketTimeoutException){
			callBack.handle(CODE_TIMEOUT, ERROR_TIMEOUT);
		}
		IsConnectting = false;
	}

	@Override
	public void onClosing(int code, String reason, boolean remote) {
		Logger.d(TAG," onClosing：" + "reason = " + reason + " code=" + code);
		if((code == -1) && reason.contains("refuses handshake")){
			callBack.handle(CODE_TOKEN_INVALID, ERROR_TOKEN_INVALID);
		}
		IsConnectting = false;
	}

	@Override
	public void onCloseInitiated(int code, String reason) {
		Logger.d(TAG, " onCloseInitiated：" + "reason = " + reason + " code=" + code);
		IsConnectting = false;
	}

	@Override
	public void onWebsocketCloseInitiated(WebSocket conn, int code, String reason) {
		Logger.d(TAG, " onWebsocketCloseInitiated：" + "reason = " + reason + " code=" + code);
		onCloseInitiated(code, reason);
		IsConnectting = false;
	}

	@Override
	public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
		Logger.d(TAG, " onWebsocketClosing：" + "reason = " + reason + " code=" + code);
		onClosing(code, reason, remote);
		IsConnectting = false;
	}

	public void onClose() {
		Logger.i(TAG, "call close");
		r.get().close();
		IsConnectting = false;
	}

	@SuppressWarnings("unused")
	public int sendMessage(String msg) {
		Logger.i(TAG, "ChatClient: Send Message: " + msg + this.toString());
		final String message = msg;
		try {
			if (r.get().getConnection() != null) {
				r.get().send(message);
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return -1;
	}

	public void logout() {
		SimpleMessageDTO message = new SimpleMessageDTO("logout");
		String msg = JsonUtil.objectToJson(message);
		sendMessage(msg);
	}

	// 回调接口
	public interface ChatClientCallBack {
		public void handle(int statudCode, String result);
	}

	/*
	private Random random;
	private int ID;
	private final AtomicReference<Session> r;
	private ChatClientCallBack callBack; // 连接建立成功与否的回调方法

	public static final String ERROR_TOKEN_INVALID = "ERROR_TOKEN_INVALID";
	public static final String ERROR_TIMEOUT = "ERROR_TIMEOUT";

	public MyChatClient(String sid, String token, ChatClientCallBack callBack) {
		// 测试token失效的情况
		random = new Random();
		ID = random.nextInt(1000);
		Logger.d("MyChatClient","MyChatClient Create "+ID);
		r = new AtomicReference<Session>();
		this.callBack = callBack;
		try {
			this.connect(new URI(Common.RootChatUrl + sid + "/" + token));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 测试方法，不停的发送消息
	public void beChatty() throws InterruptedException {
		SimpleMessageDTO pingMessage = new SimpleMessageDTO("ping");
		String msg = JsonUtil.objectToJson(pingMessage);
		try{
			while (r.get().isOpen()) {
				Logger.i("ChatClient: keep alive: ", msg);
				r.get().getRemote().sendStringByFuture(msg);
				Thread.sleep(10000);
			}
		}catch(WebSocketException e){
			e.printStackTrace();
			return;
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
	}

	private void connect(final URI destServerURI) {
		WebSocketClient client = new WebSocketClient();
		client.setMaxIdleTimeout(150000);
		client.setMaxTextMessageBufferSize(65536 * 10);
		try {
			if(NetworkUtil.checkNetworkStatus(MyApplication.context)){
				client.start();
				client.connect(this, destServerURI).get();
			}else{
				callBack.handle(-1, ERROR_TIMEOUT);
				return;
			}
		} catch (UpgradeException e2){
			callBack.handle(-2, ERROR_TOKEN_INVALID);
		}catch (Exception e1) {
			e1.printStackTrace();
		}
		return;
	}

	// 连接关闭的回调方法
	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		Logger.i("ChatClient", "+++ Websocket Close from " + r.get().getRemoteAddress().getAddress());
		Logger.i("Websocket Close reason:", reason);
		MyApplication.chatClientDisconnect();
	}

	// 连接建立成功
	@Override
	public void onWebSocketConnect(Session sess) {
		r.set(sess);
		Logger.i("ChatClient local addr: %s%n", sess.getLocalAddress().toString());
		Common.CONNECT_STATUS = true;
		MyApplication.sendChatClient(this);
		callBack.handle(0, null);
		RunDataContainer.getContainer().sendSynAtnMessage();
	}

	// 连接建立失败
	@Override
	public void onWebSocketError(Throwable cause) {
		cause.printStackTrace();
		Common.CONNECT_STATUS = false;
		if(cause instanceof UpgradeException){
			callBack.handle(-2, ERROR_TOKEN_INVALID);
			return;
		}
		callBack.handle(-1, ERROR_TIMEOUT);
	}

	@Override
	public void onWebSocketText(String message) {
		try{
			Logger.i("MyChatClient receive message: ", message + this.toString());
			WebMessageDispatcher.getDispatcher().dispatchMessage(message);
		}catch (WebSocketException e){
			e.printStackTrace();
			return;
		}
	}

	// 主动关闭连接
	public void close() {
		r.get().close();
		Log.i("ChatClient", "close");
	}

	@SuppressWarnings("unused")
	public int sendMessage(String msg) {
		Logger.i("ChatClient: Send Message", msg + this.toString());
		final String message = msg;
		try {
			if (r.get().isOpen()) {
				Future<Void> future = r.get().getRemote().sendStringByFuture(message);
				return 0;
			}
		} catch (WebSocketException e) {
			e.printStackTrace();
			return -1;
		}
		return -1;
	}

	public void logout() {
		SimpleMessageDTO message = new SimpleMessageDTO("logout");
		String msg = JsonUtil.objectToJson(message);
		sendMessage(msg);
	}

	// 回调接口
	public interface ChatClientCallBack {
		public void handle(int statudCode, String result);
	}
	*/
}
