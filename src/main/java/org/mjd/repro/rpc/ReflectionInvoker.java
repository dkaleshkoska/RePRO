package org.mjd.repro.rpc;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.mjd.repro.message.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link RpcRequestMethodInvoker} that uses reflection to invoke an {@link RpcRequest} on the {@code rpcTarget}.
 * The rpcTarget is provided at construction time and is used for every invocation.
 *
 * @NotThreadSafe The rpcTarget method invocation may not be threadsafe.
 */
public final class ReflectionInvoker implements RpcRequestMethodInvoker {
	private static final Logger LOG = LoggerFactory.getLogger(ReflectionInvoker.class);
	private Object rpcTarget;

	/**
	 * Constructs a fully initialised {@link ReflectionInvoker} for the given {@code rpcTarget}
	 *
	 * @param rpcTarget the object to invoke the methods upon
	 */
	public ReflectionInvoker(final Object rpcTarget) {
		this.rpcTarget = rpcTarget;
	}

	/**
	 * Creates an uninitialised {@link ReflectionInvoker}. This cannot invoke methods until an RPC target
	 * {@link Object} as been set via {@link #changeTarget(Object)}
	 *
	 * I don't really like this but you, as a client, don't necessarily know the RPC target object up front.
	 */
	public ReflectionInvoker() {
		this.rpcTarget = null;
	}

	// Return type is the callers responsibility for now. I don't see an efficient way without passing it in as part
	// of the request.
	@SuppressWarnings("unchecked")
	@Override
	public <T> T invoke(final RpcRequest request) throws InvocationException {
		if(rpcTarget == null) {
			throw new IllegalStateException("RPC target has not been set");
		}
		try {
			LOG.debug("Invoking {} with args {}", request.getMethod(), request.getArgValues());
			return (T) MethodUtils.invokeMethod(rpcTarget, request.getMethod(), request.getArgValues());
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
			throw new InvocationException("Error invoking " + request, ex);
		}
	}

	@Override
	public void changeTarget(final Object newTarget) {
		rpcTarget = newTarget;
	}
}
