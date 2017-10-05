package se.kodarkatten.casual.network.messages.queue;

import se.kodarkatten.casual.network.io.writers.utils.CasualNetworkWriterUtils;
import se.kodarkatten.casual.network.messages.CasualNWMessageType;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.parseinfo.CommonSizes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.nio.ByteBuffer;

public final class CasualEnqueueReplyMessage implements CasualNetworkTransmittable
{
    private final UUID execution;
    private final UUID id;
    private CasualEnqueueReplyMessage(final UUID execution, final UUID id)
    {
        this.execution = execution;
        this.id = id;
    }
    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.ENQUEUE_REPLY;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        ByteBuffer b = ByteBuffer.allocate(CommonSizes.EXECUTION.getNetworkSize() +  CommonSizes.UUID_ID.getNetworkSize());
        CasualNetworkWriterUtils.writeUUID(execution, b);
        CasualNetworkWriterUtils.writeUUID(id, b);
        List<byte[]> l = new ArrayList<>();
        l.add(b.array());
        return l;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        CasualEnqueueReplyMessage that = (CasualEnqueueReplyMessage) o;
        return Objects.equals(execution, that.execution) &&
            Objects.equals(id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, id);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CasualEnqueueReplyMessage{");
        sb.append("execution=").append(execution);
        sb.append(", id=").append(id);
        sb.append('}');
        return sb.toString();
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public UUID getExecution()
    {
        return execution;
    }

    public UUID getId()
    {
        return id;
    }

    public static final class Builder
    {
        private UUID execution;
        private UUID id;

        private Builder()
        {
        }

        public Builder withExecution(final UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public Builder withId(final UUID id)
        {
            this.id = id;
            return this;
        }

        public CasualEnqueueReplyMessage build()
        {
            Objects.requireNonNull(execution, "execution is not allowed to be null");
            Objects.requireNonNull(id, "id is not allowed to be null");
            return new CasualEnqueueReplyMessage(execution, id);
        }
    }
}
