<#import "layout.ftlh" as layout />
<#import "/spring.ftl" as spring />
<@layout.page "Messages">
	<#assign pagedModel=messages>
	<#assign content=pagedModel.content>
	
	<div class="container">
		<div class="sticky-top bg-white pt-1" style="top: 56px">
			<h5>Messages</h5>
			
			<form class="border rounded px-3 py-1 mb-3 small" method="get" id="searchForm">
				<#if RequestParameters.sort??>
				<input type="hidden" name="sort" value="${RequestParameters.sort}"/>
				</#if>
				<div class="d-flex flex-wrap align-items-end">
					<div class="me-2 mb-2">
						<label for="inputComponent" class="form-label form-label-sm">Component</label>
						<select id="inputComponent" name="componentDn" class="form-select form-select-sm">
							<option value="">All</option>
							<#list distinctComponents as component>
							<option value="${component}"<#if (RequestParameters.componentDn?? && RequestParameters.componentDn == component)> selected</#if>>${layout.componentDnName(component)}</option>
							</#list>
						</select>
					</div>
					<div class="me-2 mb-2">
						<label for="inputmessageId" class="form-label form-label-sm">Group Id</label>
						<input type="text" class="form-control form-control-sm" id="inputGroupId" name="groupId" value="${RequestParameters.groupId!''}">
					</div>
					<div class="me-2 mb-2">
						<label for="inputGroupStatus" class="form-label form-label-sm">Group Status</label>
						<select id="inputGroupStatus" name="groupStatus" class="form-select form-select-sm">
							<option value="">All</option>
							<#list messageGroupStatuses as status>
							<option value="${status.intValue()}"<#if (RequestParameters.groupStatus?? && RequestParameters.groupStatus == status.intValue()?c)> selected</#if>>${status.name()}</option>
							</#list>
						</select>
					</div>
					<div class="me-2 mb-2">
						<label for="inputStatus" class="form-label form-label-sm">Message Status</label>
						<select id="inputStatus" name="messageStatus" class="form-select form-select-sm">
							<option value="">All</option>
							<#list messageStatuses as status>
							<option value="${status.intValue()}"<#if (RequestParameters.messageStatus?? && RequestParameters.messageStatus == status.intValue()?c)> selected</#if>>${status.name()}</option>
							</#list>
						</select>
					</div>
					<div class="ms-auto mb-2">
						<a class="btn btn-sm btn-secondary me-1" style="width: 100px" href="<@spring.url '/messages'/>" onclick="clearSelection()">Clear</a>
						<button type="submit" class="btn btn-sm btn-primary" style="width: 100px" onclick="clearSelection()"><i class="fas fa-search me-2"></i>Search</button>
					</div>
				</div>
			</form>
			
			<div class="d-flex">
				<div class="d-flex align-self-center mb-3 ms-0">
					<div class="me-2">
						<form method="post" id="abortMessagesForm" onsubmit="submitAbortMessagesForm(this)">
							<#if RequestParameters.sort?? && false><input type="hidden" name="sort" value="${RequestParameters.sort}"/></#if>
							<input type="hidden" name="action" value="abort">
							<button id="abortButton" type="submit" class="btn btn-sm btn-secondary" style="width: 100px"><i class="fas fa-trash-alt me-2"></i>Abort</button>
						</form>
					</div>
				</div>
				
				<div class="ms-auto me-2">
					<div class="input-group input-group-sm">
					<span class="input-group-text">
						Page Size
					</span>
					<select id="pageSizeSelect" class="form-select form-select-sm" onchange="setPageSize(this)">
						<option <#if size == 15> selected</#if>>15</option>
						<option <#if size == 20> selected</#if>>20</option>
						<option <#if size == 50> selected</#if>>50</option>
						<option <#if size == 100> selected</#if>>100</option>
						<option <#if size == 200> selected</#if>>200</option>
						<option <#if size == 500> selected</#if>>500</option>
						<option <#if size == 1000> selected</#if>>1000</option>
					</select>
					</div>
				</div>
				
				<@layout.paginator messages, "mb-0"/>
			</div>
			
			<#if content?size gt 0>
			<table class="table table-light  table-bordered table-striped table-sm small"  style="table-layout: fixed">
				<thead>
					<tr>
						<td width="31px" class="text-center"><input class="form-check-input" style="cursor: pointer; font-size: .875rem; margin-top: 4px" type="checkbox" value="" id="selectAllCheck" onclick="toggleSelectAll(this)"></td>
						<th class="text-overflow-ellipsis" scope="col" width="164px"><@layout.sortableTableHeader "Creation Date", "creationDate", pagedModel, sort/></th>
						<th class="text-overflow-ellipsis" scope="col" width="25%"><@layout.sortableTableHeader "Component", "componentDn", pagedModel, sort/></th>
						<th class="text-overflow-ellipsis" scope="col" width="20%"><@layout.sortableTableHeader "Operation", "operation", pagedModel, sort/></th>
						<th class="text-overflow-ellipsis" scope="col" width="17%"><@layout.sortableTableHeader "Group Id", "groupId", pagedModel, sort/></th>
						<th class="text-overflow-ellipsis" scope="col" width="10%"><@layout.sortableTableHeader "Group Status", "groupStatus", pagedModel, sort/></th>
						<th class="text-overflow-ellipsis" scope="col" width="10%"><@layout.sortableTableHeader "Message Status", "messageStatus", pagedModel, sort/></th>
					</tr>
				</thead>
			</table>
			
			<#else>
			<div class="alert alert-warning" role="alert">
			  No messages found
			</div>
			</#if>
		</div>
		
		<#if content?size gt 0>
		<div style="margin-top: -1em">
			<table class="messages table table-bordered table-hover table-sm small" style="table-layout: fixed">
				<tbody>
				<#list content as em>
					<#assign m=em.content>
					<tr style="cursor: pointer" id="${m.id}"  onclick="navigateToMessage('${m.id}')">	
						<td width="31px" class="text-center" onclick="toggleSelection('${m.id}')">
							<input class="form-check-input" style="cursor: pointer; font-size: .875rem; margin-top: 0.3rem" type="checkbox" value="" >
						</td>
						<td class="text-overflow-ellipsis" width="164px">${m.creationDate}</td>
						<td class="text-overflow-ellipsis" width="25%">${layout.componentDnName(m.componentDn)}</td>
						<td class="text-overflow-ellipsis" width="20%">${m.operation}</td>
						<td class="text-overflow-ellipsis" width="17%" style="overflow: hidden">${m.groupId}</td>
						<td class="text-overflow-ellipsis" width="10%"><@layout.groupStatus m.groupStatus/></td>
						<td class="text-overflow-ellipsis" width="10%"><@layout.messageStatus m.messageStatus/></td>
					</tr>
				</#list>
				</tbody>
			</table>
		</div>
		</#if>
	</div>
	
<script>
function navigateToMessage(msgId) {
	location.href='<@spring.url '/messages/'/>'  
		+ encodeURIComponent(msgId);
}

function submitAbortMessagesForm(form) {
	getSelectedMessages().forEach(m => {
		$("<input>").attr({
			name: "id",
			type: "hidden",
			value: m
		}).appendTo($(form));
	});
}

function setPageSize(select) {
	Cookies.set("size", select.value, {path: '${springMacroRequestContext.requestUri}'});
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

function toggleSelection(messageId) {
	row = $("#" + $.escapeSelector(messageId));
	selectedMessages = getSelectedMessages();
	selected = selectedMessages.includes(messageId);
	selected ? deselectMessage(messageId, row) : selectMessage(messageId, row); 
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
}

function updateSelectAllCheck() {
	$("#selectAllCheck").prop('checked', ($(".messages > tbody > tr:not(.table-active)").length == 0));
}

function updateAbortButton() {
	$("#abortButton").prop('disabled', ($(".messages > tbody > tr.table-active").length == 0));
}

function clearSelection() {
	sessionStorage.removeItem("messages.selected");
}

$(window).on('load', function() {
	<#if clearSelection??>
	sessionStorage.removeItem("messages.selected");
	<#else>
	getSelectedMessages().forEach(g => {selectMessage(g);});
	</#if>
	updateToolbar();
});
</script>
	
	
</@layout.page>