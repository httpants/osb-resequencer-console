package au.com.cyberavenue.osb.resequencer.service;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bea.wli.sb.resequencer.dispatcher.serializable.ResequencerMessage;
import com.bea.wli.sb.sources.SourceUtils;

import au.com.cyberavenue.osb.resequencer.batch.config.ApplicationProperties;
import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.RetryMessageEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageGroupStatus;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbGroupStatusEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbMsgEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbResequencerMessageEntity;
import au.com.cyberavenue.osb.resequencer.repository.seqretryprocessor.RetryMessageRepository;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.MessageGroupDao;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbGroupStatusRepository;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbResequencerMessageRepository;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    @Autowired
    private OsbGroupStatusRepository messageGroupRepository;

    @Autowired
    private OsbResequencerMessageRepository messageRepository;

    @Autowired
    private RetryMessageRepository retryMessageRepository;

    @Autowired
    private MessageGroupDao messageGroupDao;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Override
    public List<OsbGroupStatusEntity> getSuspendedGroups() {
        return messageGroupRepository
                .findAllByStatusOrderByLastReceivedTimeDesc(MessageGroupStatus.SUSPENDED.intValue());
    }

    @Override
    public List<OsbResequencerMessageEntity> getMessageGroup(String groupId) {
        return messageRepository.findAllByOwnerIdEqualsOrderByStatusDesc(groupId);
    }

    @Override
    public List<RetryMessageEntity> getMessageRetries(String messageId) {
        return retryMessageRepository.findByMessageIdOrderByRetryDateDesc(messageId);
    }

    @Override
    public int recoverMessageGroup(String groupId) {
        return messageGroupDao.recoverGroup(groupId);
    }

    @Override
    public int recoverAllMessageGroups() {
        return messageGroupDao.recoverAllMessageGroups(
                getSuspendedGroups().stream().map(OsbGroupStatusEntity::getId).collect(Collectors.toList()));
    }

    @Override
    public String getPayload(OsbMsgEntity osbMsg) {

        try {
            ByteArrayInputStream is = new ByteArrayInputStream(osbMsg.getMsgBin());
            ObjectInput in = new ObjectInputStream(is);
            ResequencerMessage rm = (ResequencerMessage) in.readObject();

            Source xmlInput = new StreamSource(new StringReader(SourceUtils.toString(rm.getPayload())));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            log.error("Exception deserializing ResequencerMessage", e);
            return new String(osbMsg.getMsgBin());
        }
    }

    @Override
    public String getIConsoleLink(OsbMsgEntity osbMsg, String groupId, Date lastUpdated) {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(osbMsg.getMsgBin());
            ObjectInput in = new ObjectInputStream(is);
            ResequencerMessage rm = (ResequencerMessage) in.readObject();

            Source xmlInput = new StreamSource(new StringReader(SourceUtils.toString(rm.getPayload())));
            DOMResult domResult = new DOMResult();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, domResult);
            Node doc = domResult.getNode();

            UriComponentsBuilder b = UriComponentsBuilder
                    .fromHttpUrl(applicationProperties.getIconsoleSearchUrl());

            Map<String, String> namespaceBindings = new HashMap<>();
            namespaceBindings.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
            namespaceBindings.put("spq", "http://www.sparq.com.au/Envelope");
            SimpleNamespaceContext nc = new SimpleNamespaceContext();
            nc.setBindings(namespaceBindings);
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(nc);
            Node energyQAudit = (Node) xpath.evaluate(".//spq:EnergyQAudit", doc, XPathConstants.NODE);
            if (energyQAudit != null) {
                addQueryParamsFromEnergyQAudit(b, xpath, groupId, energyQAudit);
                if (lastUpdated != null) {
                    LocalDateTime ldt = lastUpdated.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    LocalDateTime startTime = ldt.minus(2, ChronoUnit.MINUTES);
                    LocalDateTime endTime = ldt.plus(2, ChronoUnit.MINUTES);
                    b.queryParam("startTime", startTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
                    b.queryParam("endTime", endTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
                }
                return b.toUriString();
            }

            Node header = (Node) xpath.evaluate(".//spq:Envelope/spq:Header", doc, XPathConstants.NODE);
            if (header != null) {
                addQueryParamsFromSparqHeader(b, xpath, groupId, header);
                if (lastUpdated != null) {
                    LocalDateTime ldt = lastUpdated.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    LocalDateTime startTime = ldt.minus(2, ChronoUnit.MINUTES);
                    LocalDateTime endTime = ldt.plus(2, ChronoUnit.MINUTES);
                    b.queryParam("startTime", startTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
                    b.queryParam("endTime", endTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
                }
                return b.toUriString();
            }

            return null;
        } catch (Exception e) {
            log.error("Exception building iconsole link", e);
            return null;
        }
    }

    private void addQueryParamsFromEnergyQAudit(UriComponentsBuilder b, XPath xpath, String groupId,
            Node energyQAudit) throws XPathExpressionException {

        addQueryParam(b, "serviceName", xpath, "spq:MessageType", energyQAudit);

        String metaDataValue = null;
        String externalReference = xpath.evaluate("spq:ExternalReference", energyQAudit);

        // search for groupId as a MetaData value
        Node metaDataElement = (Node) xpath.evaluate(
                "spq:MetaData/spq:MetaDataElement[spq:Value = '" + groupId + "']", energyQAudit,
                XPathConstants.NODE);
        if (metaDataElement != null) {
            metaDataValue = groupId;
        }

        if (metaDataValue == null) {
            // search for ExternalReference as a MetaData value
            if (StringUtils.isNotBlank(externalReference)) {
                metaDataElement = (Node) xpath.evaluate(
                        "spq:MetaData/spq:MetaDataElement[spq:Value = '" + externalReference + "']",
                        energyQAudit, XPathConstants.NODE);
                if (metaDataElement != null) {
                    metaDataValue = externalReference;
                }
            }
        }

        if (metaDataValue == null) {
            NodeList metaDataElements = (NodeList) xpath.evaluate("spq:MetaData/spq:MetaDataElement/spq:Value",
                    energyQAudit, XPathConstants.NODESET);
            if (metaDataElements != null) {
                for (int i = 0; i < metaDataElements.getLength(); i++) {
                    String value = metaDataElements.item(i).getTextContent();
                    if (StringUtils.contains(externalReference, value)) {
                        metaDataValue = value;
                        break;
                    }
                }

                if (metaDataValue == null) {
                    metaDataValue = metaDataElements.item(0).getTextContent();
                }
            }
        }

        if (metaDataValue != null) {
            b.queryParam("metaDataValue", metaDataValue);
        }
    }

    private void addQueryParamsFromSparqHeader(UriComponentsBuilder b, XPath xpath, String groupId,
            Node energyQAudit) throws XPathExpressionException {

        // MessageType is not reliable as serviceName
        // addQueryParam(b, "serviceName", xpath, "spq:MessageType", energyQAudit);
        addQueryParam(b, "realmCode", xpath, "spq:Realm", energyQAudit);
        addQueryParam(b, "systemCode", xpath, "spq:SourceSystem", energyQAudit);
        b.queryParam("metaDataValue", groupId);
    }

    private void addQueryParam(UriComponentsBuilder b, String name, XPath xpath, String expression, Node source)
            throws XPathExpressionException {
        String value = xpath.evaluate(expression, source);
        if (value != null) {
            b.queryParam(name, value);
        }
    }
}
