package au.com.cyberavenue.osb.resequencer.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import au.com.cyberavenue.osb.resequencer.entity.seqretryprocessor.RetryMessageEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbGroupStatusEntity;
import au.com.cyberavenue.osb.resequencer.entity.soainfra.OsbResequencerMessageEntity;
import au.com.cyberavenue.osb.resequencer.service.MessageService;

@RestController
@RequestMapping("/groups")
public class MessageGroupRestController {

    private static final Logger log = LoggerFactory.getLogger(MessageGroupRestController.class);

    @Autowired
    MessageService messageService;

    @RequestMapping("/suspended")
    ResponseEntity<List<OsbGroupStatusEntity>> suspendedGroups() {
        log.debug("Getting list of suspended groups.");
        return new ResponseEntity<>(messageService.getSuspendedGroups(), HttpStatus.OK);
    }

    @RequestMapping("/suspended/{id}")
    ResponseEntity<List<OsbResequencerMessageEntity>> getMessagesForGroup(@PathVariable("id") String groupId) {
        return new ResponseEntity<>(messageService.getMessageGroup(groupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/recover/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<Integer> recoverMessageGroup(@PathVariable("id") String groupId) {
        return new ResponseEntity<>(messageService.recoverMessageGroup(groupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/recover/all")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<Integer> recoverMessageGroup() {
        return new ResponseEntity<>(messageService.recoverAllMessageGroups(), HttpStatus.OK);
    }

    @RequestMapping("message/{messageId}")
    ResponseEntity<List<RetryMessageEntity>> getMessageRetries(@PathVariable("messageId") String messageId) {
        return new ResponseEntity<>(messageService.getMessageRetries(messageId), HttpStatus.OK);
    }

}
