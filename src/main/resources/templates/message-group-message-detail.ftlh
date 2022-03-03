<#import "layout.ftlh" as layout />
<#import "/spring.ftl" as spring />
<@layout.page osbMsg.msgId + " - " + group.groupId + " - " + layout.componentDnName(group.componentDn) + " - " + group.operation>
	
	<div class="mt-1 container">
		
		
		<nav aria-label="breadcrumb">
		  <ol class="breadcrumb">
		    <li class="breadcrumb-item"><a href="${Session.groupsHref}">Groups</a></li>
		    <li class="breadcrumb-item"><a href="${Session.groupDetailHref}">${group.groupId}</a></li>
		    <li class="breadcrumb-item active" aria-current="page">${osbMsg.msgId}</li>
		  </ol>
		</nav>
		<div class="d-flex align-items-center">
			<h5 class="me-auto">Group Detail</h5>
		</div>
			
		<div class="d-flex mb-2">
			<div class="me-2">
				<form method="post" id="resumeGroupForm" action="<@spring.url '/groups/' + group.id/>">
					<input type="hidden" name="action" value="resume">
					<button id="resumeGroupButton" type="submit" class="btn btn-sm btn-secondary"<#if ["READY", "LOCKED"]?seq_contains(layout.groupStatusName(group.groupStatus))> disabled</#if> style="width: 100px"><i class="fas fa-play me-2"></i>Resume</button>
				</form>
			</div>
			<div class="me-2">
				<form method="post" id="suspendGroupForm" action="<@spring.url '/groups/' + group.id/>">
					<input type="hidden" name="action" value="suspend">
					<button id="suspendGroupButton" type="submit" class="btn btn-sm btn-secondary"${["SUSPENDED"]?seq_contains(layout.groupStatusName(group.groupStatus))?then(' disabled','')} style="width: 100px"><i class="fas fa-pause me-2"></i>Suspend</button>
				</form>
			</div>
			<div class="ms-auto">
				<form method="get">
					<button type="submit" class="btn btn-sm btn-secondary"  style="width: 100px"><i class="fas fa-sync-alt me-2"></i>Refresh</button>
				</form>
			</div>
		</div>
			
		<div>
			<div class="d-flex border rounded px-2 mb-2">
				<table class="table table-borderless table-hover table-sm small" style="table-layout: fixed">
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
						<tr style="cursor: pointer" onclick="navigateToGroup('${group.id}')">	
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
		</div>
		
		
		
		<div class="row">
			<div class="col col-md-10">
		
				<h5>Message Detail</h5>
				
				<div class="d-flex mb-3">
					<div class="me-2">
						<form method="post" id="retryMessagesForm" action="<@spring.url '/groups/' + group.id + '/messages'/>">
							<input type="hidden" name="action" value="recover">
							<input type="hidden" name="id" value="${osbMsg.msgId}">
							<button id="retryButton" type="submit" class="btn btn-sm btn-secondary" <#if ["READY"]?seq_contains(layout.messageStatusName(osbResequencerMessage.status))> disabled</#if> style="width: 100px"><i class="fas fa-recycle me-2"></i>Recover</button>
						</form>
					</div>
					<div class="me-2">
						<form method="post" id="abortMessagesForm" action="<@spring.url '/groups/' + group.id + '/messages'/>">
							<input type="hidden" name="action" value="abort">
							<input type="hidden" name="id" value="${osbMsg.msgId}">
							<button id="abortButton" type="submit" class="form-control btn btn-sm btn-secondary" <#if ["ABORTED"]?seq_contains(layout.messageStatusName(osbResequencerMessage.status))> disabled</#if> style="width: 100px"><i class="fas fa-trash-alt me-2"></i>Abort</button>
						</form>
					</div>
				</div>
				
				<dl class="row small mb-2">
					<dt class="col-sm-3">Id</td>
					<dd class="col-sm-9">${osbMsg.msgId}</dd>
					<dt class="col-sm-3">Message Status</td>
					<dd class="col-sm-9">
						<@layout.messageStatus osbResequencerMessage.status/>
						<#if iconsoleLink??>
							<a class="btn btn-sm btn-link" href="${iconsoleLink}" target="_blank"><i class="fas fa-external-link-alt me-2"></i>iconsole</a>
						</#if>
					</dd>
				</dl>
		
		
				
				<pre class="mb-0 border rounded small" style="height: 450px">${osbMsgPayload}</pre>
			</div>
			
			<div class="col col-md-2">
				<#if retries??>
				<div class="border rounded px-2 mb-4  w-100 h-100">
				<table class="table small table-sm">
					<thead>
						<tr>
							<th scope="col">Retry Attempts (${retries?size})</th>
						</tr>
					</thead>
					<tbody>
					<#list retries as retry>
					<tr>
						<td>${retry.retryDate}</td>
					</tr>
					</#list>
					</tbody>
				</table>
				</div>
				<#else>
				<div class="border rounded px-2 mb-4  w-100 h-100">
				<table class="table small table-sm">
					<thead>
						<tr>
							<th scope="col">Retry Attempts (0)</th>
						</tr>
					</thead>
				</table>
				</div>
				</#if>
			</div>
		</div>
		
	</div>
	
<script>
function navigateToGroup(gId) {
	location.href='<@spring.url '/groups/'/>'  
		+ encodeURIComponent(gId);
}

$(window).on('load', function() {
	

});
</script>
	
	
</@layout.page>