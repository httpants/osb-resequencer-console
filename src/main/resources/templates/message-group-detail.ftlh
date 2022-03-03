<#import "layout.ftlh" as layout />
<#import "/spring.ftl" as spring />
<@layout.page group.groupId + " - " + layout.componentDnName(group.componentDn) + " - " + group.operation>
	<#assign pagedModel=messages>
	<#assign content=pagedModel.content>
	
	<div class="mt-1 container">
		<div class="sticky-top bg-white">
			<nav aria-label="breadcrumb">
			  <ol class="breadcrumb">
			    <li class="breadcrumb-item"><a href="${Session.groupsHref!}">Groups</a></li>
			    <li class="breadcrumb-item active" aria-current="page">${group.groupId}</li>
			  </ol>
			</nav>
			<div class="d-flex align-items-center">
				<h5 class="me-auto">Group Detail</h5>
			</div>
			<div class="d-flex mb-2">
				<div class="me-2">
					<form method="post" id="resumeGroupForm" action="<@spring.url '/groups/' + group.id/>">
						<input type="hidden" name="action" value="resume">
						<button id="resumeGroupButton" type="submit" class="btn btn-sm btn-secondary"<#if ["READY", "LOCKED"]?seq_contains(layout.groupStatusName(group.groupStatus))> disabled</#if>  style="width: 100px"><i class="fas fa-play me-2"></i>Resume</button>
					</form>
				</div>
				<div class="me-2">
					<form method="post" id="suspendGroupForm" action="<@spring.url '/groups/' + group.id/>">
						<input type="hidden" name="action" value="suspend">
						<button id="suspendGroupButton" type="submit" class="btn btn-sm btn-secondary"${["SUSPENDED"]?seq_contains(layout.groupStatusName(group.groupStatus))?then(' disabled','')}  style="width: 100px"><i class="fas fa-pause me-2"></i>Suspend</button>
					</form>
				</div>
				<div class="ms-auto">
					<form id="searchForm" method="get">
						<button type="submit" class="btn btn-sm btn-secondary"  style="width: 100px"><i class="fas fa-sync-alt me-2"></i>Refresh</button>
					</form>
				</div>
			</div>
			
			<div class="border rounded px-2 mb-2">
				<table class="table table-borderless table-sm small" style="table-layout: fixed">
					<thead class="align-middle">
						<tr>
							<th class="text-overflow-ellipsis" scope="col" width="164px">Last Updated</th>
							<th class="text-overflow-ellipsis" scope="col" width="35%">Component</th>
							<th class="text-overflow-ellipsis" scope="col" width="20%">Operation</th>
							<th class="text-overflow-ellipsis" scope="col" width="17%">Group Id</th>
							<th scope="col" width="85px">Faulted</th>
							<th scope="col" width="85px">Aborted</th>
							<th scope="col" width="85px">Ready</th>
							<th scope="col" width="85px">Total</th>
							<th class="text-overflow-ellipsis" scope="col" width="10%">Status</th>
						</tr>
					</thead>
					<tbody>
						<tr>	
							<td class="text-overflow-ellipsis" width="164px">${group.lastReceivedTime}</td>
							<td class="text-overflow-ellipsis" width="35%">${layout.componentDnName(group.componentDn)}</td>
							<td class="text-overflow-ellipsis" width="20%">${group.operation}</td>
							<td class="text-overflow-ellipsis" width="17%" style="overflow: hidden">${group.groupId}</td>
							<td width="85px">${group.faultedCount}</td>
							<td width="85px">${group.abortedCount}</td>
							<td width="85px">${group.readyCount}</td>
							<td width="85px">${group.messageCount}</td>
							<td class="text-overflow-ellipsis" width="10%"><@layout.groupStatus group.groupStatus/></td>
						</tr>
					</tbody>
				</table>
			</div>
			
			<h5>Messages</h5>
			
			<#if content?size gt 0>
			
			<div class="d-flex">
				<div class="d-flex align-self-center mb-3 ms-0 me-auto">
					<div class="me-2">
						<form method="post" id="retryMessagesForm" action="#" onsubmit="submitMessagesForm(this)">
							<input type="hidden" name="action" value="recover">
							<button id="retryButton" type="submit" class="btn btn-sm btn-secondary"  style="width: 100px"><i class="fas fa-recycle me-2"></i>Recover</button>
						</form>
					</div>
					<div class="m3-2 flex-fill">
						<form method="post" id="abortMessagesForm" action="#" onsubmit="submitMessagesForm(this)">
							<input type="hidden" name="action" value="abort">
							<button id="abortButton" type="submit" class="form-control btn btn-sm btn-secondary" style="width: 100px"><i class="fas fa-trash-alt me-2"></i>Abort</button>
						</form>
					</div>
				</div>
				
				
				<div class="me-2">
					<div class="input-group input-group-sm">
					<span class="input-group-text">
						Page Size
					</span>
					<select id="pageSizeSelect" class="form-select form-select-sm" onchange="setPageSize(this)">
						<option <#if gm_size == 15> selected</#if>>15</option>
						<option <#if gm_size == 20> selected</#if>>20</option>
						<option <#if gm_size == 50> selected</#if>>50</option>
						<option <#if gm_size == 100> selected</#if>>100</option>
						<option <#if gm_size == 200> selected</#if>>200</option>
						<option <#if gm_size == 500> selected</#if>>500</option>
						<option <#if gm_size == 1000> selected</#if>>1000</option>
					</select>
					</div>
				</div>
				
				<@layout.paginator messages, "mb-0"/>
			</div>
			
			<table class="table table-light small table-bordered table-striped table-sm"  style="table-layout: fixed">
				<thead>
					<tr>
						<td width="31px" class="text-center"><input class="form-check-input" style="cursor: pointer; font-size: .875rem; margin-top: 4px" type="checkbox" value="" id="selectAllCheck" onclick="toggleSelectAll(this)"></td>
						<th class="text-overflow-ellipsis" scope="col" width="184px">Date</th>
						<th class="text-overflow-ellipsis" scope="col" width="72%">Message Id</th>
						<th class="text-overflow-ellipsis" scope="col" width="10%">Status</th>
					</tr>
				</thead>
			</table>
			
			<#else>
			<div class="alert alert-success" role="alert">
			  No messages found
			</div>
			</#if>
		</div>
		
		<#if content?size gt 0>
		<div style="margin-top: -1em">
			<table class="messages small table table-bordered table-hover table-sm" style="table-layout: fixed">
				<tbody>
				<#list content as em>
					<#assign m=em.content>
					
					<tr class="${layout.messageStatusName(m.status)}" style="cursor: pointer" id="${m.id}" onclick="navigateToMessages('${group.id}', '${m.id}')">

						<td width="31px" class="text-center" onclick="toggleSelection(event, '${m.id}')">
							<input class="form-check-input" style="cursor: pointer; font-size: .875rem; margin-top: 4px" type="checkbox" value="" >
						</td>
						<td class="text-overflow-ellipsis" width="184px">${m.creationDate}</td>
						<td class="text-overflow-ellipsis" width="72%" style="overflow: hidden">${m.id}</td>
						<td class="text-overflow-ellipsis" width="10%"><@layout.messageStatus m.status/></td>
					</tr>
					
				</#list>
				</tbody>
			</table>
		</div>
		</#if>
	</div>
	
<script>
function navigateToMessages(groupId, msgId) {
	location.href='<@spring.url '/groups/'/>'  
		+ encodeURIComponent(groupId)
		+ '/messages/'
		+ encodeURIComponent(msgId);
}

function submitMessagesForm(form) {
	getSelectedMessages().forEach(m => {
		$("<input>").attr({
			name: "id",
			type: "hidden",
			value: m
		}).appendTo($(form));
	});
}

function setPageSize(select) {
	Cookies.set("gm_size", select.value, {path: ''});
	$("#searchForm").submit();
}

function toggleSelectAll(checkbox) {
	selectAll($(checkbox).prop('checked'));
}

function selectAll(selected) {
	$(".messages > tbody > tr").each(function() {
		var row=$(this), 
		m = row.attr("id");
		selected ? selectMessage(m, row) : deselectMessage(m, row);
	});
}

function toggleSelection(event, messageId) {
	row = $("#" + $.escapeSelector(messageId));
	selectedMessages = getSelectedMessages();
	selected = selectedMessages.includes(messageId);
	selected ? deselectMessage(messageId, row) : selectMessage(messageId, row); 
	event.stopPropagation();
}

function selectMessage(messageId, row) {
	var selectedMessages = getSelectedMessages();
	if (selectedMessages.indexOf(messageId) == -1) {
		selectedMessages.push(messageId);
		saveSelectedMessages(selectedMessages);
	}
	if (!row) {
		row = $("#" + $.escapeSelector(messageId));
	}
	row.find("input:checkbox").prop('checked', true);
	row.addClass("table-active");
	updateToolbar();
}

function deselectMessage(messageId, row) {
	var selectedMessages = getSelectedMessages();
	const i = selectedMessages.indexOf(messageId);
	if (i > -1) {
		selectedMessages.splice(i, 1);
		saveSelectedMessages(selectedMessages);
	}
	if (!row) {
		row = $("#" + $.escapeSelector(messageId));
	}
	row.find("input:checkbox").prop('checked', false);
	row.removeClass("table-active");
	updateToolbar();
}


function getSelectedMessages() {
	var selectedMessages = sessionStorage.getItem("messages.selected");
	if (selectedMessages == null) {
		selectedMessages = [];
		sessionStorage.setItem("messages.selected", JSON.stringify(selectedMessages));
	} else {
		selectedMessages = JSON.parse(selectedMessages);
	}
	return selectedMessages;
}

function saveSelectedMessages(selectedMessages) {
	if (selectedMessages) {
		sessionStorage.setItem("messages.selected", JSON.stringify(selectedMessages));
	} else {
		sessionStorage.removeItem("messages.selected");
	}
}

function updateToolbar() {
	updateSelectAllCheck();
	updateAbortButton();
	updateRetryButton();
}

function updateSelectAllCheck() {
	$("#selectAllCheck").prop('checked', ($(".messages > tbody > tr:not(.table-active)").length == 0));
}

function updateAbortButton() {
	$("#abortButton").prop('disabled', ($(".messages > tbody > tr.table-active").length == 0));
}

function updateRetryButton() {
	selectedRows = $(".messages > tbody > tr.table-active").length;
	selectedErrorRows = 
	$(".messages > tbody > tr.table-active.ERROR").length +
		$(".messages > tbody > tr.table-active.TIMEOUT").length + 
		$(".messages > tbody > tr.table-active.ABORTED").length;
	retryDisabled = (selectedErrorRows == 0 || selectedErrorRows != selectedRows);
	$("#retryButton").prop('disabled', retryDisabled);
}

function clearSelectedMessagesOnLoad() {
	Cookies.set("clearSelectedMessages", "true", {path: ''});
}

$(window).on('load', function() {
	<#if clearSelection??>
	sessionStorage.removeItem("messages.selected");
	<#else>
	getSelectedMessages().forEach(g => {selectMessage(g);});
	</#if>
	
	updateToolbar();
	
	messagesUrl ='<@spring.url '/groups/'/>'  
		+ encodeURIComponent('${group.id}')
		+ '/messages';
	
	$('#retryMessagesForm').attr('action', messagesUrl);
	$('#abortMessagesForm').attr('action', messagesUrl); 
});
</script>
	
	
</@layout.page>