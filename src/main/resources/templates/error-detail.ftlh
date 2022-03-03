<#import "layout.ftlh" as layout />
<#import "/spring.ftl" as spring />
<@layout.page>
	<div class="mt-4 container">
		<div class="row justify-content-center">
			<div class="col-8">
				<div class="alert alert-danger" role="alert">
					<h4 class="alert-heading">${status} - ${error}</h4>
				</div>
			</div>
		</div>
	</div>
</@layout.page>