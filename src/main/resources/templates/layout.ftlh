<#import "/spring.ftl" as spring />
<#import "buildproperties.ftlh" as buildproperties />
<#assign  security=JspTaglibs["http://www.springframework.org/security/tags"] />

<#function componentDnName componentDn>
	<#return componentDn?split("$")?last>
</#function>

<#macro groupStatus intValue, statusValues=messageGroupStatuses>
<#assign nameValue=groupStatusName(intValue, statusValues)>
<#if nameValue == 'SUSPENDED'>
	<span class="badge rounded-pill bg-danger">SUSPENDED</span>
<#elseif nameValue == 'ERROR'>
	<span class="badge rounded-pill bg-warning">ERROR</span>	
<#elseif nameValue == 'READY'>
	<span class="badge rounded-pill bg-success">READY</span>	
<#else>
	${nameValue}
</#if>
</#macro>

<#function groupStatusName intValue, statusValues=messageGroupStatuses>
<#list statusValues as status><#if intValue == status.intValue()><#return status.name()></#if></#list>
<#return intValue?string>
</#function>

<#macro messageStatus intValue, statusValues=messageStatuses>
<#assign nameValue=messageStatusName(intValue, statusValues)>
<#if nameValue == 'FAULTED'>
	<span class="badge rounded-pill bg-danger">FAULTED</span>
<#elseif nameValue == 'READY'>
	<span class="badge rounded-pill bg-success">READY</span>	
<#elseif nameValue == 'ABORTED'>
	<span class="badge rounded-pill bg-secondary">ABORTED</span>
<#else>
	${nameValue}
</#if>
</#macro>

<#function messageStatusName intValue, statusValues=messageStatuses>
<#list statusValues as status><#if intValue == status.intValue()><#return status.name()></#if></#list>
</#function>


<#macro sortableTableHeader header, property, pagedModel=pagedModel, sort=sort>
<#assign link=pagedModel.getLink(property)>
<#if link.isPresent()><a class="text-reset text-decoration-none" href="${link.get().href}"><div></#if>
<#if sort.getOrderFor(property)??><i class="fas fa-sort-<#if sort.getOrderFor(property).isAscending()>up<#else>down</#if> me-1"></i></#if>
${header}
<#if link.isPresent()></div></a></#if>
</#macro>						

<#macro paginatorLink pagedModel, rel, disabled, faIcon>
	<#assign link=pagedModel.getLink(rel)>
	<#if link.isPresent()>
		<#assign href=link.get().href>
	<#else>
		<#assign href='#'>
		<#assign disabled=true>
	</#if>
	<li class="page-item<#if disabled> disabled</#if>">
		<a class="page-link link-dark" href="${href}" aria-label="${rel}">
			<i class="fas ${faIcon}"></i>
		</a>
	</li>
</#macro>

<#macro ifRequestUriStartsWith relativeUrl, value>
<#assign requestUri=springMacroRequestContext.requestUri>
<#assign contextUrl=springMacroRequestContext.getContextUrl(relativeUrl)>
<#if requestUri?starts_with(contextUrl)>${value}</#if>
</#macro>

<#macro paginator pagedModel, class="">
	<#setting number_format="computer">
	<#assign metadata=pagedModel.metadata>
	<nav aria-label="message groups navigation" class="${class}">
		<ul class="pagination pagination-sm">
			<@paginatorLink pagedModel, 'first', (metadata.number == 0), 'fa-angle-double-left'/>
			<@paginatorLink pagedModel, 'prev', (metadata.number == 0), 'fa-angle-left'/>
			
			<#assign link=pagedModel.getLink('self')>
			<#if link.isPresent()>
				<#assign href=link.get().href>
				<#assign disabled=metadata.number == 0>
			<#else>
				<#assign href='#'>
				<#assign disabled=true>
			</#if>
			<li class="page-item">
				<#assign startRow=(metadata.number * metadata.size) + 1>
				<#assign endRow=startRow + pagedModel.content?size - 1>
				<a class="page-link link-dark" href="#">Rows ${startRow} to ${endRow} of ${metadata.totalElements}</a>
			</li>
			
			<@paginatorLink pagedModel, 'next', ((metadata.number + 1) == metadata.totalPages), 'fa-angle-right'/>
			<@paginatorLink pagedModel, 'last', ((metadata.number + 1) == metadata.totalPages), 'fa-angle-double-right'/>
		</ul>
	</nav>
</#macro>

<#macro page title="">
<#setting number_format="computer">
<!DOCTYPE html>  
<html lang="en">  
<head>  
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="<@spring.url '/webjars/bootstrap/5.1.3/css/bootstrap.min.css'/>"/>
    <link rel="stylesheet" href="<@spring.url '/webjars/font-awesome/5.15.4/css/all.min.css'/>"/>
    <link rel="icon" type="image/x-icon" href="<@spring.url '/img/icons8-stack-of-paper-30.png'/>">
    
    <style>
    .fas.btn {
    	font-weight: 900;
    }
    
	.text-overflow-ellipsis {
		overflow: hidden;
		white-space: nowrap;
		text-overflow: ellipsis;
	}
    </style>

	<title><#if title != "">${title} - </#if>OSB Sequencer<#if applicationProperties.environment??> - ${applicationProperties.environment}</#if></title>
	
	<script src="<@spring.url '/webjars/jquery/3.6.0/jquery.min.js'/>"></script>
	<script src="<@spring.url '/webjars/js-cookie/2.2.1/js.cookie.min.js'/>"></script>
</head>  
<body style="padding-top: 56px">  
	<nav class="navbar navbar-expand-lg navbar-light fixed-top" style="background-color: ${applicationProperties.navbarBackgroundColor!'rgb(248, 249, 250)'};">
	  <div class="container">
	    <a class="navbar-brand" href="<@spring.url '/'/>">OSB Sequencer</a>
	    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
	      <span class="navbar-toggler-icon"></span>
	    </button>
	    <div class="collapse navbar-collapse" id="navbarSupportedContent">
	      <ul class="navbar-nav me-auto mb-2 mb-lg-0">
	        <li class="nav-item">
          	  <a class="nav-link<@ifRequestUriStartsWith '/dashboard', ' active'/>" href="<@spring.url '/dashboard'/>">Dashboard</a>
            </li>
            <li class="nav-item">
          	  <a class="nav-link<@ifRequestUriStartsWith '/groups', ' active'/>" href="${groupsHref}">Groups</a>
            </li>
	        <li class="nav-item">
          	  <a class="nav-link<@ifRequestUriStartsWith '/messages', ' active'/>" href="${messagesHref}">Messages</a>
            </li>
            <li class="nav-item">
          	  <a class="nav-link<@ifRequestUriStartsWith '/settings', ' active'/>" href="<@spring.url '/settings'/>">Settings</a>
            </li>
	      </ul>
	      
	      <#if applicationProperties.environment??><span class="navbar-brand">${applicationProperties.environment}</span></#if>
	      <@security.authorize access="isAuthenticated()">
 		  <ul class="navbar-nav mb-2 mb-lg-0">
      		<li class="nav-item dropdown">
      			<a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
      				<@security.authentication property="principal.username" />
      			</a>
      			<ul class="dropdown-menu dropdown-menu-end" style="z-index: 2000" aria-labelledby="navbarDropdown">
      				<li><form action="<@spring.url '/logout'/>" method="GET"><button type="submit" class="dropdown-item" href="#">Logout</button></form></li>
      			</ul>
      		</li>
	      </ul>
	      </@security.authorize>

	    </div>
	  </div>
	</nav>	
	<#nested>
	<nav class="navbar invisible py-1 small" >
	  <div class="container">
	  	<span class="navbar-text py-0">1</span>
	  </div>
	</nav>
	<nav class="navbar fixed-bottom navbar-light py-1 small" style="background-color: rgb(248, 249, 250);">
	  <div class="container">
	  	<span class="ms-auto navbar-text py-0"><#if applicationProperties.environment??>environment: ${applicationProperties.environment}<#else>&nbsp;</#if></span>
	  	<span class="ms-2 navbar-text py-0">version: ${buildproperties.version}</span>
	  </div>
	</nav>
    <script src="<@spring.url '/webjars/bootstrap/5.1.3/js/bootstrap.bundle.min.js'/>"></script>
</body>  
</html>
</#macro>