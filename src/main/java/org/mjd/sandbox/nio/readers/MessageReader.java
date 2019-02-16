package org.mjd.sandbox.nio.readers;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.mjd.sandbox.nio.message.Message;
import org.mjd.sandbox.nio.message.factory.MessageFactory.MessageCreationException;

public interface MessageReader<T>
{
    void read(ByteBuffer headerBuffer, ByteBuffer bodyBuffer) throws MessageCreationException;
    
    Optional<Message<T>> getMessage();

    boolean messageComplete();

    boolean isEndOfStream();
}