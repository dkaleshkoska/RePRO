package org.mjd.sandbox.nio.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mjd.sandbox.nio.handlers.message.SubscriptionRegistrar;
import org.mjd.sandbox.nio.handlers.message.SubscriptionRegistrar.Subscriber;
import org.mjd.sandbox.nio.support.Broadcaster.Listener;

/**
 * Fake target for RPC calls. A server will have direct access to something like this when it's setup.
 */
public final class FakeRpcTarget implements AutoCloseable, Listener {
	public static final Map<String, Object> methodNamesAndReturnValues = new HashMap<>();
	private final List<Subscriber> subs = Collections.synchronizedList(new ArrayList<>());
	private Thing thing;

	public FakeRpcTarget() {
		methodNamesAndReturnValues.put("callMeString", "callMeString");
		methodNamesAndReturnValues.put("genString", "generated string");
		methodNamesAndReturnValues.put("whatIsTheString", "it's this string...");
	}

	public void callMeVoid() {
		/* Intentionall empty, test support class */ }

	public String callMeString() {
		return "callMeString";
	}

	public String genString() {
		return "generated string";
	}

	public String whatIsTheString() {
		return "it's this string...";
	}

	public String hackTheGibson(int password) {
		return "password sent: " + password;
	}

	@SubscriptionRegistrar
	public void subscribe(Subscriber sub, Object... args) {
		if(thing == null) {
			thing = new Thing();
		}
		thing.register(this);
		subs.add(sub);
	}

	@Override
	public void close() throws Exception {
		thing.deregister(this);
	}

	@Override
	public void notify(String notification) {
		for (Subscriber sub : subs) {
			sub.receive(notification);
		}
	}

}
