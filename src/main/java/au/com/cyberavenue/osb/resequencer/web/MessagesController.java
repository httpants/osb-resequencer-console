package au.com.cyberavenue.osb.resequencer.web;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.RetryMessageEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageGroupReportProjection;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageGroupStatus;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageProjection;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageStatus;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbMsgEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbResequencerMessageEntity;
import au.com.cyberavenue.osb.resequencer.repository.seqretryprocessor.RetryMessageRepository;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbGroupStatusRepository;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbMsgRepository;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbResequencerMessageRepository;
import au.com.cyberavenue.osb.resequencer.service.MessageService;

@Controller
public class MessagesController {

    private static final Logger log = LoggerFactory.getLogger(MessagesController.class);

    @Autowired
    private OsbMsgRepository osbMsgRepository;

    @Autowired
    private OsbGroupStatusRepository osbGroupStatusRepository;

    @Autowired
    private OsbResequencerMessageRepository messageRepository;

    @Autowired
    private RetryMessageRepository retryMessageRepository;

    @Autowired
    private PagedResourcesAssembler<MessageProjection> pagedResourcesAssember;

    @Autowired
    private MessageService messageService;

    @GetMapping("/messages")
    public ModelAndView getMessages(
            @RequestParam(required = false) String componentDn,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) Integer groupStatus,
            @RequestParam(required = false) Integer messageStatus,
            @SortDefault(sort = "creationDate") Pageable pageable) {

        Page<MessageProjection> messages = messageRepository
                .findMessages(componentDn, groupId, groupStatus, messageStatus, pageable);
        PagedModel<EntityModel<MessageProjection>> pagedModel = pagedResourcesAssember.toModel(messages);

        List<String> distinctComponents = messageRepository.findDistinctComponentDn()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        ModelAndView model = new ModelAndView("messages");
        model.addObject("messages", pagedModel);
        model.addObject("sort", messages.getSort());
        model.addObject("distinctComponents", distinctComponents);
        model.addObject("messageGroupStatuses", MessageGroupStatus.values());
        model.addObject("messageStatuses", MessageStatus.values());
        return model;
    }

    @PostMapping(value = "/messages", params = "action=abort")
    public RedirectView abortMessages(
            @RequestHeader String referer,
            @RequestParam(name = "id") List<String> messageIds,
            Pageable pageable,
            HateoasPageableHandlerMethodArgumentResolver pageResolver,
            RedirectAttributes redirectAttributes)
            throws IOException {

        messageIds.forEach(message -> log.info("aborting message {}", message));

        messageRepository.updateMessageStatus(MessageStatus.ABORTED.intValue(), messageIds);

        redirectAttributes.addFlashAttribute("clearSelection", true);

        UriTemplate uriTemplate = UriTemplate.of(referer);
        URI refererURI = uriTemplate.expand();
        UriComponentsBuilder builder = fromUri(refererURI);
        pageResolver.enhance(builder, null, pageable);
        return redirect(builder);
    }

    @GetMapping("/messages/{messageId}")
    public ModelAndView getMessage(@PathVariable String messageId) {

        OsbResequencerMessageEntity osbResequencerMessage = messageRepository
                .findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find resequencer message with id " + messageId));

        OsbMsgEntity osbMsg = osbMsgRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find osb message with id " + messageId));

        MessageGroupReportProjection groupReport = osbGroupStatusRepository
                .getMessageGroupReport(osbResequencerMessage.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find message group with id " + osbResequencerMessage.getOwnerId()));

        List<RetryMessageEntity> retries = retryMessageRepository.findByMessageIdOrderByRetryDateDesc(messageId);

        ModelAndView model = new ModelAndView("message-detail");
        model.addObject("group", groupReport);
        model.addObject("messageGroupStatuses", MessageGroupStatus.values());
        model.addObject("messageStatuses", MessageStatus.values());
        model.addObject("osbResequencerMessage", osbResequencerMessage);
        model.addObject("osbMsg", osbMsg);
        model.addObject("osbMsgPayload", messageService.getPayload(osbMsg));

        if (!CollectionUtils.isEmpty(retries)) {
            model.addObject("retries", retries);
        }

        if (Arrays.asList(MessageStatus.FAULTED, MessageStatus.ABORTED)
                .contains(MessageStatus.of(osbResequencerMessage.getStatus()))) {
            model.addObject("iconsoleLink",
                    messageService.getIConsoleLink(osbMsg, groupReport.getGroupId(),
                            groupReport.getLastReceivedTime()));
        }

        return model;
    }

    private RedirectView redirect(UriComponentsBuilder builder) {
        RedirectView view = new RedirectView(builder.build().toUriString());
        view.setHosts("disable-encoding");
        return view;
    }

}
