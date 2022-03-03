<#import "layout.ftlh" as layout />
<#import "/spring.ftl" as spring />
<@layout.page "Groups">
	<#assign pagedModel=messageGroups>
	<#assign content=pagedModel.content>
	
	<div class="container">
		<div class="sticky-top bg-white pt-1" style="top: 56px">
			<h5>Groups</h5>
			
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
						<label for="inputGroupId" class="form-label form-label-sm">Group Id</label>
						<input type="text" class="form-control form-control-sm" id="inputGroupId" name="groupId" value="${RequestParameters.groupId!''}">
					</div>
					<div class="me-2 mb-2">
						<label for="inputStatus" class="form-label form-label-sm">Status</label>
						<select id="inputStatus" name="status" class="form-select form-select-sm">
							<option value="">All</option>
							<#list messageGroupStatuses as status>
							<option value="${status.intValue()}"<#if (RequestParameters.status?? && RequestParameters.status == status.intValue()?c)> selected</#if>>${status.name()}</option>
							</#list>
						</select>
					</div>
					<div class="me-2 mb-2">
						<label for="inputFaulted" class="form-label form-label-sm">Faulted</label>
						<select id="inputFaulted" name="faulted" class="form-select form-select-sm">
							<option value=""></option>
							<option value="0" <#if (RequestParameters.faulted?? && RequestParameters.faulted == '0')> selected</#if>>0</option>
							<option value="1" <#if (RequestParameters.faulted?? && RequestParameters.faulted == '1')> selected</#if>>1+</option>
						</select>
					</div>
					<div class="me-2 mb-2">
						<label for="inputAborted" class="form-label form-label-sm">Aborted</label>
						<select id="inputAborted" name="aborted" class="form-select form-select-sm">
							<option value=""></option>
							<option value="0" <#if (RequestParameters.aborted?? && RequestParameters.aborted == '0')> selected</#if>>0</option>
							<option value="1" <#if (RequestParameters.aborted?? && RequestParameters.aborted == '1')> selected</#if>>1+</option>
						</select>
					</div>
					<div class="me-2 mb-2">
						<label for="inputReady" class="form-label form-label-sm">Ready</label>
						<select id="inputReady" name="ready" class="form-select form-select-sm">
							<option value=""></option>
							<option value="0" <#if (RequestParameters.ready?? && RequestParameters.ready == '0')> selected</#if>>0</option>
							<option value="1" <#if (RequestParameters.ready?? && RequestParameters.ready == '1')> selected</#if>>1+</option>
						</select>
					</div>
					<div class="me-2 mb-2">
						<label for="inputTotal" class="form-label form-label-sm">Total</label>
						<select id="inputTotal" name="total" class="form-select form-select-sm">
							<option value=""></option>
							<option value="0" <#if (RequestParameters.total?? && RequestParameters.total == '0')> selected</#if>>0</option>
							<option value="1" <#if (RequestParameters.total?? && RequestParameters.total == '1')> selected</#if>>1+</option>
						</select>
					</div>
					<div class="ms-auto mb-2">
						<a class="btn-sm btn btn-secondary me-1" style="width: 100px" href="<@spring.url '/groups'/>" onclick="clearSelection()">Clear</a>
						<button type="submit" class="btn-sm btn btn-primary"  style="width: 100px" onclick="clearSelection()"><i class="fas fa-search me-2"></i>Search</button>
					</div>
				</div>
			</form>
			
			<#if content?size gt 0>
			
			<div class="d-flex">
				<div class="d-flex align-self-center mb-3 ms-0">
					<div class="me-2">
						<form method="post" id="resumeGroupsForm" onsubmit="submitGroupsForm(this)">
							<input type="hidden" name="action" value="resume">
							<button id="resumeButton" type="submit" class="btn btn-sm btn-secondary" style="width: 100px"><i class="fas fa-play me-2"></i>Resume</button>
						</form>
					</div>
					<div class="me-2">
						<form method="post" id="suspendGroupsForm" onsubmit="submitGroupsForm(this)">
							<input type="hidden" name="action" value="suspend">
							<button id="suspendButton" type="submit" class="btn btn-sm btn-secondary" style="width: 100px" ><i class="fas fa-pause me-2"></i>Suspend</button>
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
				
				<@layout.paginator messageGroups, "mb-0"/>
			</div>
			
			<table class="table table-light  table-bordered table-striped table-sm small"  style="table-layout: fixed">
				<thead class="align-middle">
					<tr>
						<td width="31px" class="text-center"><input class="form-check-input" style="cursor: pointer; font-size: .875rem; margin-top: 4px" type="checkbox" value="" id="selectAllCheck" onclick="toggleSelectAll(this)"></td>
						<th class="text-overflow-ellipsis" scope="col" width="164px"><@layout.sortableTableHeader "Last Updated", "lastReceivedTime", pagedModel, sort/></th>
						<th class="text-overflow-ellipsis" scope="col" width="35%"><@layout.sortableTableHeader "Component", "componentDn", pagedModel, sort/></th>
						<th class="text-overflow-ellipsis" scope="col" width="20%"><@layout.sortableTableHeader "Operation", "operation", pagedModel, sort/></th>
						<th class="text-overflow-ellipsis" scope="col" width="17%"><@layout.sortableTableHeader "Group Id", "groupId", pagedModel, sort/></th>
						<th scope="col" width="85px"><@layout.sortableTableHeader "Faulted", "faultedCount", pagedModel, sort/></th>
						<th scope="col" width="85px"><@layout.sortableTableHeader "Aborted", "abortedCount", pagedModel, sort/></th>
						<th scope="col" width="85px"><@layout.sortableTableHeader "Ready", "readyCount", pagedModel, sort/></th>
						<th scope="col" width="85px"><@layout.sortableTableHeader "Total", "messageCount", pagedModel, sort/></th>
						<th class="text-overflow-ellipsis" scope="col" width="10%"><@layout.sortableTableHeader "Status", "groupStatus", pagedModel, sort/></th>
					</tr>
				</thead>
			</table>
			<#else>
			<div class="alert alert-warning" role="alert">
			  No groups found
			</div>
			</#if>
		</div>
		
		<#if content?size gt 0>
		<div style="margin-top: -1em">
			<table class="message-groups table table-bordered table-hover table-sm small" style="table-layout: fixed">
				<tbody>
				<#list content as em>
					<#assign g=em.content>
					<tr style="cursor: pointer" id="${g.id}" onclick="navigateToMessages('${g.id}')">	
						<td width="31px" class="text-center" onclick="toggleSelection(event, '${g.id}')">
							<input class="form-check-input" style="cursor: pointer; font-size: .875rem; margin-top: 4px" type="checkbox" value="" >
						</td>
						<td class="text-overflow-ellipsis" width="164px">${g.lastReceivedTime}</td>
						<td class="text-overflow-ellipsis" width="35%">${layout.componentDnName(g.componentDn)}</td>
						<td class="text-overflow-ellipsis" width="20%">${g.operation}</td>
						<td class="text-overflow-ellipsis" width="17%" style="overflow: hidden">${g.groupId}</td>
						<td width="85px">${g.faultedCount}</td>
						<td width="85px">${g.abortedCount}</td>
						<td width="85px">${g.readyCount}</td>
						<td width="85px">${g.messageCount}</td>
						<td class="text-overflow-ellipsis" width="10%"><@layout.groupStatus g.groupStatus/></td>
					</tr>
				</#list>
				</tbody>
			</table>
		</div>
		</#if>
	</div>
	
<script>
function navigateToMessages(id) {
	location.href='<@spring.url '/groups/'/>'  
		+ encodeURIComponent(id);
}

function submitGroupsForm(form) {
	var groups = getSelectedGroups();
	console.log(groups);
	groups.forEach(g => {
		$("<input>").attr({
			name: "gid",
			type: "hidden",
			value: g
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
	$(".message-groups > tbody > tr").each(function() {
		var row=$(this), 
		g = row.attr("id");
		selected ? selectGroup(g, row) : deselectGroup(g, row);
	});
}

function toggleSelection(event, groupId) {
	row = $("#" + $.escapeSelector(groupId));
	selectedGroups = getSelectedGroups();
	selected = selectedGroups.includes(groupId);
	selected ? deselectGroup(groupId, row) : selectGroup(groupId, row); 
	event.stopPropagation();
}

function selectGroup(groupId, row) {
	var selectedGroups = getSelectedGroups();
	if (selectedGroups.indexOf(groupId) == -1) {
		selectedGroups.push(groupId);
		saveSelectedGroups(selectedGroups);
	}
	if (!row) {
		row = $("#" + $.escapeSelector(groupId));
	}
	row.find("input:checkbox").prop('checked', true);
	row.addClass("table-active");
	updateToolbar();
}

function deselectGroup(groupId, row) {
	var selectedGroups = getSelectedGroups();
	const i = selectedGroups.indexOf(groupId);
	if (i > -1) {
		selectedGroups.splice(i, 1);
		saveSelectedGroups(selectedGroups);
	}
	if (!row) {
		row = $("#" + $.escapeSelector(groupId));
	}
	row.find("input:checkbox").prop('checked', false);
	row.removeClass("table-active");
	updateToolbar();
}


function getSelectedGroups() {
	var selectedGroups = sessionStorage.getItem("groups.selected");
	if (selectedGroups == null) {
		selectedGroups = [];
		sessionStorage.setItem("groups.selected", JSON.stringify(selectedGroups));
	} else {
		selectedGroups = JSON.parse(selectedGroups);
	}
	return selectedGroups;
}

function saveSelectedGroups(selectedGroups) {
	if (selectedGroups) {
		sessionStorage.setItem("groups.selected", JSON.stringify(selectedGroups));
	} else {
		sessionStorage.removeItem("groups.selected");
	}
}

function updateToolbar() {
	updateSelectAllCheck();
	updateSuspendButton();
	updateResumeButton();
}

function updateSelectAllCheck() {
	$("#selectAllCheck").prop('checked', ($(".message-groups > tbody > tr:not(.table-active)").length == 0));
}

function updateSuspendButton() {
	$("#suspendButton").prop('disabled', ($(".message-groups > tbody > tr.table-active").length == 0));
}

function updateResumeButton() {
	$("#resumeButton").prop('disabled', ($(".message-groups > tbody > tr.table-active").length == 0));
}

function clearSelection() {
	selectAll(false);
	sessionStorage.removeItem("groups.selected");
}

$(window).on('load', function() {
	<#if clearSelection??>
	sessionStorage.removeItem("groups.selected");
	<#else>
	getSelectedGroups().forEach(g => {selectGroup(g);});
	</#if>
	updateToolbar();
});
</script>
	
	
</@layout.page>