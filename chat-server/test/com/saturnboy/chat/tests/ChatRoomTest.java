package com.saturnboy.chat.tests;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.SuspendToken;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.junit.Test;

import com.saturnboy.chat.ChatSocket;

public class ChatRoomTest {

	@Test
	public void testMessageQueue() throws InterruptedException {
		MockSession session1 = new MockSession();
		ChatSocket member1 = new ChatSocket();
		member1.onConnect(session1);
		member1.onText("hello1");
		member1.onText("hello2");

		// verify that there are now two messages in the queue
		assertThat(session1.testMessages().size(), is(2));
		assertThat(session1.testMessages(), hasItems("hello1", "hello2"));

		MockSession session2 = new MockSession();
		ChatSocket member2 = new ChatSocket();
		member2.onConnect(session2);

		// immediately upon connection session2 should get the queued messages
		assertThat(session2.testMessages().size(), is(2));
		assertThat(session2.testMessages(), hasItems("hello1", "hello2"));

		member2.onText("hello3");

		// now both sessions should have all 3 messages
		assertThat(session1.testMessages().size(), is(3));
		assertThat(session1.testMessages(), hasItems("hello1", "hello2", "hello3"));

		assertThat(session2.testMessages().size(), is(3));
		assertThat(session2.testMessages(), hasItems("hello1", "hello2", "hello3"));
	}

	public static class MockRemote implements RemoteEndpoint {
		List<String> messages = new ArrayList<String>();

		public List<String> testMessages() {
			return messages;
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public BatchMode getBatchMode() {
			return null;
		}

		@Override
		public void sendBytes(ByteBuffer data) throws IOException {
		}

		@Override
		public Future<Void> sendBytesByFuture(ByteBuffer data) {
			return null;
		}

		@Override
		public void sendBytes(ByteBuffer data, WriteCallback callback) {
		}

		@Override
		public void sendPartialBytes(ByteBuffer fragment, boolean isLast) throws IOException {
		}

		@Override
		public void sendPartialString(String fragment, boolean isLast) throws IOException {
		}

		@Override
		public void sendPing(ByteBuffer applicationData) throws IOException {
		}

		@Override
		public void sendPong(ByteBuffer applicationData) throws IOException {
		}

		@Override
		public void sendString(String text) throws IOException {
		}

		@Override
		public Future<Void> sendStringByFuture(String text) {
			messages.add(text);
			return null;
		}

		@Override
		public void sendString(String text, WriteCallback callback) {
		}
	}

	public static class MockSession implements Session {
		private MockRemote mockRemote = new MockRemote();

		public List<String> testMessages() {
			return mockRemote.testMessages();
		}

		@Override
		public void close() {
		}

		@Override
		public void close(CloseStatus closeStatus) {
		}

		@Override
		public void close(int statusCode, String reason) {
		}

		@Override
		public void disconnect() throws IOException {
		}

		@Override
		public long getIdleTimeout() {
			return 0;
		}

		@Override
		public InetSocketAddress getLocalAddress() {
			return null;
		}

		@Override
		public WebSocketPolicy getPolicy() {
			return null;
		}

		@Override
		public String getProtocolVersion() {
			return null;
		}

		@Override
		public RemoteEndpoint getRemote() {
			return mockRemote;
		}

		@Override
		public InetSocketAddress getRemoteAddress() {
			return null;
		}

		@Override
		public UpgradeRequest getUpgradeRequest() {
			return null;
		}

		@Override
		public UpgradeResponse getUpgradeResponse() {
			return null;
		}

		@Override
		public boolean isOpen() {
			return false;
		}

		@Override
		public boolean isSecure() {
			return false;
		}

		@Override
		public void setIdleTimeout(long ms) {
		}

		@Override
		public SuspendToken suspend() {
			return null;
		}
	}
}
