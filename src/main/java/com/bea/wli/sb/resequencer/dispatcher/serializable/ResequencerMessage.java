package com.bea.wli.sb.resequencer.dispatcher.serializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.net.URI;

import com.bea.wli.config.Ref;
import com.bea.wli.sb.services.dispatcher.CommunicationPattern;
import com.bea.wli.sb.services.dispatcher.DispatchContext;
import com.bea.wli.sb.services.dispatcher.impl.DefaultSourceMetadata;
import com.bea.wli.sb.sources.Source;
import com.bea.wli.sb.sources.SourceUtils;
import com.bea.wli.sb.transports.RequestMetaData;
import com.bea.wli.sb.transports.RequestMetaDataXML;
import com.bea.wli.sb.transports.TransportException;

public class ResequencerMessage implements Serializable {
    private SourceMetadata sourceMetadata;

    private ResequencerDispatchOptions options;

    private String messageID;

    private String messageID_PK;

    private RequestMetaDataXML rmdXML;

    private Source payload;

    private Ref pipelineRef;

    public ResequencerMessage(SourceMetadata sourceMetadata, ResequencerDispatchOptions options, String messageID,
            RequestMetaData requestMetaData, Source payload, Ref pipelineRef) {
        this.sourceMetadata = sourceMetadata;
        this.options = options;
        this.messageID = messageID;
        this.pipelineRef = pipelineRef;
        this.messageID_PK = messageID + pipelineRef.getLocalName();
        try {
            this.rmdXML = requestMetaData.toXML();
        } catch (TransportException e) {
            e.printStackTrace();
        }
        this.payload = payload;
    }

    public String getMessageID_PK() {
        return this.messageID_PK;
    }

    public SourceMetadata getSourceMetadata() {
        return this.sourceMetadata;
    }

    public ResequencerDispatchOptions getDispatchOptions() {
        return this.options;
    }

    public Source getPayload() {
        return this.payload;
    }

    public void setPayload(Source payload) {
        this.payload = payload;
    }

    public RequestMetaDataXML getRequestMetaDataXML() {
        return this.rmdXML;
    }

    public String getMessageID() {
        return this.messageID;
    }

    public Ref getPipelineRef() {
        return this.pipelineRef;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this.sourceMetadata);
        out.writeObject(this.options);
        out.writeObject(this.messageID);
        out.writeObject(this.pipelineRef);
        ByteArrayOutputStream mainBaos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(mainBaos);
        oos.writeObject(this.rmdXML);
        SourceUtils.serializeSource(this.payload, oos);
        oos.close();
        out.writeInt(mainBaos.size());
        out.write(mainBaos.toByteArray(), 0, mainBaos.size());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois;
        this.sourceMetadata = (SourceMetadata) in.readObject();
        this.options = (ResequencerDispatchOptions) in.readObject();
        this.messageID = (String) in.readObject();
        this.pipelineRef = (Ref) in.readObject();
        int size = in.readInt();
        byte[] buf = new byte[size];
        in.readFully(buf);
        try {
            ByteArrayInputStream mainBais = new ByteArrayInputStream(buf);
            Ref ref = this.sourceMetadata.getRef();
            if (ref != null && ref.getTypeId() == "ProxyService") {
//                final ClassLoader loader = ALSBTransportManager.getInstance().getEndPointClassLoader(ref);
                ois = new ObjectInputStream(mainBais) {
                    @Override
                    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                        String name = desc.getName();
                        return Class.forName(name, false, this.getClass().getClassLoader());
                    }
                };
            } else {
                ois = new ObjectInputStream(mainBais);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        this.rmdXML = (RequestMetaDataXML) ois.readObject();
        this.payload = SourceUtils.deserializeSource(ois);
    }

    public static class SourceMetadata implements Serializable {
        boolean transactional = false;

        private Ref ref;

        private URI uri = null;

        public SourceMetadata() {}

        public SourceMetadata(DispatchContext.SourceMetadata sourceMetadata) {
            this.ref = sourceMetadata.getRef();
            this.uri = sourceMetadata.getURI();
            this.transactional = sourceMetadata.isTransactional();
        }

        public Ref getRef() {
            return this.ref;
        }

        public URI getURI() {
            return this.uri;
        }

        public boolean isTransactional() {
            return this.transactional;
        }

        public DefaultSourceMetadata toDefaultSourceMetadata() {
            DefaultSourceMetadata dsmData = new DefaultSourceMetadata(getRef());
            dsmData.setUri(getURI());
            dsmData.setTransactional(isTransactional());
            dsmData.setCommunicationPattern(CommunicationPattern.ONE_WAY);
            return dsmData;
        }
    }
}
