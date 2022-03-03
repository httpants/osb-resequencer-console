<#import "layout.ftlh" as layout />
<#import "/spring.ftl" as spring />
<@layout.page "Settings">
	
	<div class="container">
		
		<div class="sticky-top bg-white pt-1" style="top: 56px">
			
			<h5>Global Configuration</h5>
			<div class="mb-4" style="width: 600px">
				<table class="table table-bordered table-sm small">
					<thead class="align-middle bg-light">
						<tr>
							<th scope="col" width="100px">Retries Enabled</th>
							<th scope="col" width="85px">Retries</th>
							<th scope="col" width="85px">Delay (secs)</th>
							<th scope="col" width="85px">Delay Factor</th>
							<td width="100px">
							</td>
						</tr>
					</thead>
					<tbody>
						<tr style="cursor: pointer">	
							<td width="100px"  class="text-center"><input class="form-check-input" type="checkbox" value="" id="flexCheckChecked" disabled <#if globalConfig.isRetriesEnabled()> checked</#if>></td>
							<td width="85px">${globalConfig.retryLimit}</td>
							<td width="85px">${globalConfig.delay}</td>
							<td width="85px">${globalConfig.delayFactor}</td>
							<td width="100px" class="text-center">
								<button type="button" class="btn btn-sm btn-secondary py-0" style="width: 100px" 
								onclick="openGlobalConfigModal();">
								  <i class="fas fa-edit me-2"></i>edit
								</button>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<h5>Component Configuration</h5>

			<#if componentRetryConfig??>			
			<table class="table table-light  table-bordered table-striped table-sm small"  style="table-layout: fixed">
				<thead class="align-middle">
					<tr>
						<th class="text-overflow-ellipsis" scope="col">Component Dn</th>
						<th class="text-overflow-ellipsis" scope="col">Operation</th>
						<th scope="col" width="100px">Retries</th>
						<th scope="col" width="100px">Delay (secs)</th>
						<th scope="col" width="100px">Delay Factor</th>
						<td width="215px">
						</td>
					</tr>
				</thead>
			</table>
			<#else>
			<div class="alert alert-warning" role="alert">
			  No retry configuration found
			</div>
			</#if>
		</div>
		
		<#if componentRetryConfig?? && componentRetryConfig?size gt 0>
		<div style="margin-top: -1em">
			<table class="message-groups table table-bordered table-hover table-sm small" style="table-layout: fixed">
				<tbody>
				<#list componentRetryConfig as rc>
					<tr style="cursor: pointer">	
						<td class="text-overflow-ellipsis">${layout.componentDnName(rc.id.componentDn)}</td>
						<td class="text-overflow-ellipsis">${rc.id.operation}</td>
						<td width="100px"><#if rc.retryLimit??>${rc.retryLimit}</#if></td>
						<td width="100px"><#if rc.delay??>${rc.delay}</#if></td>
						<td width="100px"><#if rc.delayFactor??>${rc.delayFactor}</#if></td>
						<td width="215px">
							<div class="d-flex">
								<button type="button" class="btn btn-sm btn-secondary py-0 me-2" style="width: 100px" 
								onclick="openComponentConfigModal('${rc.id.componentDn}', '${layout.componentDnName(rc.id.componentDn)}', '${rc.id.operation}', ${rc.retryLimit!"null"}, ${rc.delay!"null"}, ${rc.delayFactor!"null"});">
								  <i class="fas fa-edit me-2"></i>edit
								</button>
								<form action="<@spring.url '/settings'/>" method="POST">
									<input type="hidden" name="action" value="clearComponentRetryConfig">
									<input type="hidden" name="componentDn" value="${rc.id.componentDn}"/>
									<input type="hidden" name="operation" value="${rc.id.operation}"/>
									<button type="submit" class="btn btn-sm btn-secondary py-0" <#if !rc.retryLimit??>disabled</#if> style="width: 100px">
									  <i class="fas fa-eraser me-2"></i>clear
									</button>
								</form>
							</div>
						</td>
					</tr>
				</#list>
				</tbody>
			</table>
		</div>
		</#if>
	</div>
	
	<!-- Modal -->
	<div class="modal small" id="componentConfigModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
	  <form class="modal-dialog modal-dialog-centered  modal-lg" method="POST" action="<@spring.url '/settings'/>">
	  	<input type="hidden" name="action" value="saveComponentRetryConfig">
	    <div class="modal-content">
	      <div class="modal-header">
	        <h5 class="modal-title" id="exampleModalLabel">Component Configuration</h5>
	        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
	      </div>
	      <div class="modal-body">
	          <div class="mb-1 row">
			    <label for="staticComponent" class="col-sm-3 col-form-label">Component</label>
			    <div class="col-sm-9">
			      <input type="hidden" name="componentDn" id="componentDn" value="nam.net.evs.Asset_5.0">
			      <input type="text" readonly class="form-control-plaintext form-control-sm" id="shortComponentDn" value="nam.net.evs.Asset_5.0">
			    </div>
			  </div>
	          <div class="mb-1 row">
			    <label for="staticOperation" class="col-sm-3 col-form-label">Operation</label>
			    <div class="col-sm-9">
			      <input type="text" readonly class="form-control-plaintext form-control-sm" name="operation" id="operation" value="syncNetworkAsset">
			    </div>
			  </div>			  
			  <div class="mb-1 row">
			    <label for="inputRetries" class="col-sm-3 col-form-label">Retries</label>
			    <div class="col-sm-9">
			      <input type="text" class="form-control form-control-sm" style="width: 100px" id="retries" name="retries" value="5">
			    </div>
			  </div>
			  <div class="mb-1 row">
			    <label for="inputDelay" class="col-sm-3 col-form-label">Delay (seconds)</label>
			    <div class="col-sm-9">
			      <input type="text" class="form-control form-control-sm" style="width: 100px" id="delay" name="delay" value="300">
			    </div>
			  </div>
			  <div class="mb-1 row">
			    <label for="inputDelayFactor" class="col-sm-3 col-form-label">Delay Factor</label>
			    <div class="col-sm-9">
			      <input type="text" class="form-control form-control-sm" style="width: 100px" id="delayFactor" name="delayFactor" value="2">
			    </div>
			  </div>
	      </div>
	      <div class="modal-footer">
	        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal" style="width: 100px">Close</button>
	        <button type="submit" class="btn btn-sm btn-primary"  style="width: 100px">Save</button>
	      </div>
	    </div>
	  </form>
	</div>
	
	<!-- Global Config Modal -->
	<div class="modal small" id="globalConfigModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
	  <form class="modal-dialog modal-dialog-centered  modal-sm" method="POST" action="<@spring.url '/settings'/>">
	  	<input type="hidden" name="action" value="saveGlobalConfig">
	    <div class="modal-content">
	      <div class="modal-header">
	        <h5 class="modal-title" id="exampleModalLabel">Global Configuration</h5>
	        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
	      </div>
	      <div class="modal-body">
			  <div class="mb-1 row">
			  	<div class="col-sm-6">
			    	<label for="inputRetriesEnabled" class="form-check-label">Retries Enabled</label>
			    </div>
			    <div class="col-sm-6">
			    	<div class="form-check">
			      		<input type="checkbox" class="form-check-input form-check-input-sm" value="true" id="inputRetriesEnabled" name="retriesEnabled" <#if globalConfig.isRetriesEnabled()> checked</#if>>
			      	</div>
			    </div>
			  </div>
			  <div class="mb-1 row">
			    <label for="inputRetries" class="col-sm-6 col-form-label col-form-label-sm">Retries</label>
			    <div class="col-sm-6">
			      <input type="text" class="form-control form-control-sm" style="width: 100px" id="globalretries" name="retries" value="${globalConfig.retryLimit}">
			    </div>
			  </div>
			  <div class="mb-1 row">
			    <label for="inputDelay" class="col-sm-6 col-form-label col-form-label-sm">Delay (seconds)</label>
			    <div class="col-sm-6">
			      <input type="text" class="form-control form-control-sm" style="width: 100px" id="globaldelay" name="delay" value="${globalConfig.delay}">
			    </div>
			  </div>
			  <div class="mb-1 row">
			    <label for="inputDelayFactor" class="col-sm-6 col-form-label col-form-label-sm">Delay Factor</label>
			    <div class="col-sm-6">
			      <input type="text" class="form-control form-control-sm" style="width: 100px" id="globaldelayFactor" name="delayFactor" value="${globalConfig.delayFactor}">
			    </div>
			  </div>
	      </div>
	      <div class="modal-footer">
	        <button type="button" class="btn btn-sm btn-secondary" data-bs-dismiss="modal" style="width: 100px">Close</button>
	        <button type="submit" class="btn btn-sm btn-primary"  style="width: 100px">Save</button>
	      </div>
	    </div>
	  </form>
	</div>
	
<script>
var componentConfigModal = null;
var globalConfigModal = null;

$(window).on('load', function() {
	componentConfigModal = new bootstrap.Modal(document.getElementById('componentConfigModal'), {
		backdrop: 'static'
	});
	globalConfigModal = new bootstrap.Modal(document.getElementById('globalConfigModal'), {
		backdrop: 'static'
	});
});

function openComponentConfigModal(componentDn, shortComponentDn, operation, retryLimit, delay, delayFactor) {
	$('#componentDn').attr('value', componentDn);
	$('#shortComponentDn').attr('value', shortComponentDn);
	$('#operation').attr('value', operation);
	$('#retries').attr('value', retryLimit ? retryLimit : ${globalConfig.retryLimit});
	$('#delay').attr('value', delay ? delay : ${globalConfig.delay} );
	$('#delayFactor').attr('value', delayFactor ? delayFactor : ${globalConfig.delayFactor});
	componentConfigModal.show();
}

function openGlobalConfigModal() {
	globalConfigModal.show();
}


</script>
</@layout.page>