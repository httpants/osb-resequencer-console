package au.com.cyberavenue.osb.resequencer.web;

import static org.springframework.web.util.UriComponentsBuilder.fromUri;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

import com.querydsl.core.types.Predicate;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.RetryMessageEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageGroupReportProjection;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageGroupStatus;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.MessageStatus;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbMsgEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbResequencerMessageEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.QOsbResequencerMessageEntity;
import au.com.cyberavenue.osb.resequencer.repository.seqretryprocessor.RetryMessageRepository;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbGroupStatusRepository;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbMsgRepository;
import au.com.cyberavenue.osb.resequencer.repository.soainfra.OsbResequencerMessageRepository;
import au.com.cyberavenue.osb.resequencer.service.MessageService;

@Controller
public class MessageGroupsController {

    private static final Logger log = LoggerFactory.getLogger(MessageGroupsController.class);

    @Autowired
    private OsbGroupStatusRepository osbGroupStatusRepository;

    @Autowired
    private OsbResequencerMessageRepository osbResequencerMessageRepository;

    @Autowired
    private OsbMsgRepository osbMsgRepository;

    @Autowired
    private RetryMessageRepository retryMessageRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PagedResourcesAssembler<MessageGroupReportProjection> assembler;

    @GetMapping("/groups")
    public ModelAndView getGroups(
            @RequestParam(required = false) String componentDn,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer faulted,
            @RequestParam(required = false) Integer aborted,
            @RequestParam(required = false) Integer ready,
            @RequestParam(required = false) Integer total,
            @SortDefault(sort = "lastReceivedTime", direction = Direction.DESC) Pageable pageable) {

        Page<MessageGroupReportProjection> page = osbGroupStatusRepository
                .getMessageGroupReport(status, componentDn, groupId, faulted, aborted, ready, total, pageable);

        PagedModel<EntityModel<MessageGroupReportProjection>> pagedModel = assembler
                .toModel(page);

        List<String> distinctComponents = osbGroupStatusRepository.findDistinctComponentDn()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        ModelAndView model = new ModelAndView("message-groups");
        model.addObject("messageGroups", pagedModel);
        model.addObject("sort", page.getSort());
        model.addObject("distinctComponents", distinctComponents);
        model.addObject("messageGroupStatuses", MessageGroupStatus.values());
        return model;
    }

    @PostMapping(value = "/groups", params = "action=suspend")
    public RedirectView suspendGroups(
            @RequestHeader String referer,
            @RequestParam(name = "gid") List<String> groupIds,
            Pageable pageable,
            HateoasPageableHandlerMethodArgumentResolver pageResolver,
            RedirectAttributes redirectAttributes)
            throws IOException {

        osbGroupStatusRepository.updateMessageGroupStatus(MessageGroupStatus.SUSPENDED.intValue(), groupIds);

        UriTemplate uriTemplate = UriTemplate.of(referer);
        URI refererURI = uriTemplate.expand();
        UriComponentsBuilder builder = fromUri(refererURI);
        pageResolver.enhance(builder, null, pageable);

        redirectAttributes.addFlashAttribute("clearSelection", true);
        return redirect(builder);
    }

    @PostMapping(value = "/groups", params = "action=resume")
    public RedirectView resumeGroups(
            @RequestHeader String referer,
            @RequestParam(name = "gid") List<String> groupIds,
            Pageable pageable,
            HateoasPageableHandlerMethodArgumentResolver pageResolver,
            RedirectAttributes redirectAttributes)
            throws IOException {

        osbResequencerMessageRepository.recoverMessagesByGroups(groupIds);
        osbGroupStatusRepository.updateMessageGroupStatus(MessageGroupStatus.READY.intValue(), groupIds);

        UriTemplate uriTemplate = UriTemplate.of(referer);
        URI refererURI = uriTemplate.expand();
        UriComponentsBuilder builder = fromUri(refererURI);
        pageResolver.enhance(builder, null, pageable);

        redirectAttributes.addFlashAttribute("clearSelection", true);
        return redirect(builder);
    }

    @GetMapping("/groups/{id}")
    public ModelAndView getGroup(
            @PathVariable String id,
            @QuerydslPredicate(root = OsbResequencerMessageEntity.class) Predicate predicate,
            @Qualifier("gm") @SortDefault(sort = "creationDate") Pageable pageable,
            PagedResourcesAssembler<OsbResequencerMessageEntity> assembler) {

        MessageGroupReportProjection groupReport = osbGroupStatusRepository.getMessageGroupReport(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find message group with id " + id));

        Page<OsbResequencerMessageEntity> messages = osbResequencerMessageRepository
                .findAll(QOsbResequencerMessageEntity.osbResequencerMessageEntity.ownerId.eq(id).and(predicate),
                        pageable);

        PagedModel<EntityModel<OsbResequencerMessageEntity>> pagedModel = assembler
                .toModel(messages);

        ModelAndView model = new ModelAndView("message-group-detail");
        model.addObject("group", groupReport);
        model.addObject("messages", pagedModel);
        model.addObject("messageGroupStatuses", MessageGroupStatus.values());
        model.addObject("messageStatuses", MessageStatus.values());
        return model;
    }

    @PostMapping(value = "/groups/{id}", params = "action=resume")
    public RedirectView resumeGroup(
            @RequestHeader String referer,
            @PathVariable String id,
            Pageable pageable,
            HateoasPageableHandlerMethodArgumentResolver pageResolver)
            throws IOException {

        osbResequencerMessageRepository.recoverMessagesByGroup(id);

        osbGroupStatusRepository.updateMessageGroupStatus(MessageGroupStatus.READY.intValue(),
                Collections.singletonList(id));

        UriTemplate uriTemplate = UriTemplate.of(referer);
        URI refererURI = uriTemplate.expand();
        UriComponentsBuilder builder = fromUri(refererURI);
        pageResolver.enhance(builder, null, pageable);
        return redirect(builder);
    }

    @PostMapping(value = "/groups/{id}", params = "action=suspend")
    public RedirectView suspendGroup(
            @RequestHeader String referer,
            @PathVariable String id,
            Pageable pageable,
            HateoasPageableHandlerMethodArgumentResolver pageResolver)
            throws IOException {

        osbGroupStatusRepository.updateMessageGroupStatus(MessageGroupStatus.SUSPENDED.intValue(),
                Collections.singletonList(id));

        UriTemplate uriTemplate = UriTemplate.of(referer);
        URI refererURI = uriTemplate.expand();
        UriComponentsBuilder builder = fromUri(refererURI);
        pageResolver.enhance(builder, null, pageable);
        return redirect(builder);
    }

    @PostMapping(value = "/groups/{ownerId}/messages", params = "action=recover")
    public RedirectView recoverGroupMessages(
            @RequestHeader String referer,
            @PathVariable("ownerId") String ownerId,
            @RequestParam(name = "id") List<String> messageIds,
            Pageable pageable,
            HateoasPageableHandlerMethodArgumentResolver pageResolver,
            RedirectAttributes redirectAttributes)
            throws IOException {

        messageIds.forEach(message -> log.info("recovering messages {}", message));

        // validate message ids belong to group and all have a status of
        // ABORTED, ERROR or TIMEOUT
        List<OsbResequencerMessageEntity> groupMessages = osbResequencerMessageRepository
                .findByOwnerIdWithIdIn(ownerId, messageIds);
        if (groupMessages.size() != messageIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "specified messages ids dont all exist in specified group");
        }

        List<MessageStatus> recoverableStatuses = Arrays.asList(
                MessageStatus.FAULTED,
                MessageStatus.TIMEOUT,
                MessageStatus.ABORTED);

        groupMessages.forEach(m -> {
            if (!recoverableStatuses.contains(MessageStatus.of(m.getStatus()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "messages must have FAULTED, TIMEOUT or ABORTED status to be recovered");
            }
        });

        // Update the message statuses to READY
        osbResequencerMessageRepository.updateMessageStatus(MessageStatus.READY.intValue(), messageIds);

        // If all messages in the group are READY
        List<MessageStatus> autoResumeStatuses = Arrays.asList(
                MessageStatus.READY);
        List<MessageStatus> messageStatuses = osbResequencerMessageRepository.findDistinctStatusByOwnerId(ownerId)
                .stream()
                .map(MessageStatus::of)
                .filter(ms -> !autoResumeStatuses.contains(ms))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(messageStatuses)) {
            osbGroupStatusRepository.recoverMessageGroup(ownerId);
        }

        // Send a redirect to the referring page
        UriTemplate uriTemplate = UriTemplate.of(referer);
        URI refererURI = uriTemplate.expand();
        UriComponentsBuilder builder = fromUri(refererURI);
        pageResolver.enhance(builder, null, pageable);

        redirectAttributes.addFlashAttribute("clearSelection", true);
        return redirect(builder);
    }

    @PostMapping(value = "/groups/{ownerId}/messages", params = "action=abort")
    public RedirectView abortGroupMessages(
            @RequestHeader String referer,
            @PathVariable("ownerId") String ownerId,
            @RequestParam(name = "id") List<String> messageIds,
            Pageable pageable,
            HateoasPageableHandlerMethodArgumentResolver pageResolver,
            RedirectAttributes redirectAttributes)
            throws IOException {

        messageIds.forEach(message -> log.info("aborting message {}", message));

        // validate message ids belong to group
        List<OsbResequencerMessageEntity> groupMessages = osbResequencerMessageRepository
                .findByOwnerIdWithIdIn(ownerId, messageIds);
        if (groupMessages.size() != messageIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "specified messages ids dont all exist in specified group");
        }

        osbResequencerMessageRepository.updateMessageStatus(MessageStatus.ABORTED.intValue(), messageIds);

        // If all messages in the group are COMPLETE or ABORTED, update the
        // message status to READY
        List<MessageStatus> manualResumeStatuses = Arrays.asList(
                MessageStatus.FAULTED,
                MessageStatus.TIMEOUT,
                MessageStatus.READY);
        List<MessageStatus> messageStatuses = osbResequencerMessageRepository.findDistinctStatusByOwnerId(ownerId)
                .stream()
                .map(MessageStatus::of)
                .filter(manualResumeStatuses::contains)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(messageStatuses)) {
            osbGroupStatusRepository.recoverMessageGroup(ownerId);
        }

        redirectAttributes.addFlashAttribute("clearSelection", true);

        UriTemplate uriTemplate = UriTemplate.of(referer);
        URI refererURI = uriTemplate.expand();
        UriComponentsBuilder builder = fromUri(refererURI);
        pageResolver.enhance(builder, null, pageable);
        return redirect(builder);
    }

    @GetMapping("/groups/{id}/messages/{messageId}")
    public String getGroupMessage(Model model,
            @PathVariable String id,
            @PathVariable String messageId,
            @QuerydslPredicate(root = OsbResequencerMessageEntity.class) Predicate predicate,
            @SortDefault(sort = "creationDate") Pageable pageable) {

        MessageGroupReportProjection groupReport = osbGroupStatusRepository.getMessageGroupReport(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find message group with id " + id));

        OsbResequencerMessageEntity osbResequencerMessage = osbResequencerMessageRepository
                .findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find resequencer message with id " + messageId));

        OsbMsgEntity osbMsg = osbMsgRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Could not find osb message with id " + messageId));

        List<RetryMessageEntity> retries = retryMessageRepository.findByMessageIdOrderByRetryDateDesc(messageId);

        model.addAttribute("group", groupReport);
        model.addAttribute("messageGroupStatuses", MessageGroupStatus.values());
        model.addAttribute("messageStatuses", MessageStatus.values());
        model.addAttribute("osbResequencerMessage", osbResequencerMessage);
        model.addAttribute("osbMsg", osbMsg);
        model.addAttribute("osbMsgPayload", messageService.getPayload(osbMsg));

        if (!CollectionUtils.isEmpty(retries)) {
            model.addAttribute("retries", retries);
        }

        if (Arrays.asList(MessageStatus.FAULTED, MessageStatus.ABORTED)
                .contains(MessageStatus.of(osbResequencerMessage.getStatus()))) {
            model.addAttribute("iconsoleLink",
                    messageService.getIConsoleLink(
                            osbMsg,
                            groupReport.getGroupId(),
                            groupReport.getLastReceivedTime()));
        }

        return "message-group-message-detail";
    }

    private RedirectView redirect(UriComponentsBuilder builder) {
        RedirectView view = new RedirectView(builder.build().toUriString());
        view.setHosts("disable-encoding");
        return view;
    }

}
