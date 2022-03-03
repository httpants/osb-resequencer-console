<#import "/spring.ftl" as spring />
<#import "layout.ftlh" as layout />
<@layout.page "Dashboard">
	<div class="container mt-1">
		<h5>Groups</h5>
		<div class="d-flex">
			<figure>
				<img src="<@spring.url '/dashboard/component-groups-bar-chart.png'/>" class="figure-img img-fluid founded" alt="Groups">
			</figure>
		</div>
		
		<h5>Messages</h5>
		<div class="d-flex">
			<figure>
				<img src="<@spring.url '/dashboard/component-messages-bar-chart.png'/>" class="figure-img img-fluid founded" alt="Messages">
			</figure>
		</div>
	</div>
</@layout.page>