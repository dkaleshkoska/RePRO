package org.mjd.repro.readers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import org.mjd.repro.message.Message;
import org.mjd.repro.message.factory.MessageFactory.MessageCreationException;

public interface MessageReader<T>
{
    ByteBuffer[] read(ByteBuffer headerBuffer, ByteBuffer bodyBuffer) throws MessageCreationException, IOException;

    ByteBuffer[] readPreloaded(ByteBuffer headerBuffer, ByteBuffer bodyBuffer) throws IOException;

    Optional<Message<T>> getMessage();

    boolean messageComplete();

    boolean isEndOfStream();

    int getHeaderSize();
}