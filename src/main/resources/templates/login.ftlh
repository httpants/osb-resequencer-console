<#import "layout.ftlh" as layout />
<#import "/spring.ftl" as spring />
<#import "buildproperties.ftlh" as buildproperties />

<!DOCTYPE html>  
<html lang="en">  
<head>  
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="<@spring.url '/webjars/bootstrap/5.1.3/css/bootstrap.min.css'/>"/>
    <link rel="stylesheet" href="<@spring.url '/webjars/font-awesome/5.15.4/css/all.min.css'/>"/>
    
    <style>
    .fas.btn {
    	font-weight: 900;
    }
    </style>

	<title>OSB Sequencer</title>
	
	<script src="<@spring.url '/webjars/jquery/3.6.0/jquery.min.js'/>"></script>
	<script src="<@spring.url '/webjars/js-cookie/2.2.1/js.cookie.min.js'/>"></script>
</head>  
<body>  
	<nav class="navbar navbar-expand-lg navbar-light" style="background-color: ${applicationProperties.navbarBackgroundColor!'rgb(248, 249, 250)'};">
	  <div class="container">
	    <a class="navbar-brand" href="<@spring.url '/'/>">OSB Sequencer</a>
	    <#if applicationProperties.environment??><span class="ms-auto navbar-brand">${applicationProperties.environment}</span></#if>
	  </div>
	</nav>	
	
	<div class="mt-5 container">
		<div class="row justify-content-center">
			<div class="col-8">
				<#if Request['javax.servlet.error.message']??>
				<div class="alert alert-danger alert-dismissible fade show mb-5" role="alert">
					${Request['javax.servlet.error.message']}
					<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
				</div>
				</#if>
				<form class="card" method="POST" action="j_security_check">
					<div class="card-header">
						<h5>Enter your login details</h5>
					</div>
					<div class="card-body">
						<div class="mb-3">
							<label for="InputUserName" class="form-label">User Name</label>
							<input name="j_username" class="form-control" id="InputUserName" aria-describedby="userNameHelp">
						</div>
						<div class="mb-3">
							<label for="InputPassword" class="form-label">Password</label>
							<input name="j_password" type="password" class="form-control" id="InputPassword">
						</div>
						<button type="submit" class="btn btn-primary">Login</button>
					</div>
				</form>
			</div>
		</div>
	</div>
	
	<nav class="navbar invisible py-1 small" >
	  <div class="container">
	  	<span class="navbar-text py-0">1</span>
	  </div>
	</nav>
	<nav class="navbar fixed-bottom navbar-light py-1 small" style="background-color: rgb(248, 249, 250);">
	  <div class="container">
	  	<span class="navbar-text py-0">&copy; 2022 Energy Queensland</span>
	  	<span class="ms-auto navbar-text py-0"><#if applicationProperties.environment??>environment: ${applicationProperties.environment}<#else>&nbsp;</#if></span>
	  	<span class="ms-2 navbar-text py-0">version: ${buildproperties.version}</span>
	  </div>
	</nav>
	
    <script src="<@spring.url '/webjars/bootstrap/5.1.3/js/bootstrap.bundle.min.js'/>"></script>
    
    <script>
    sessionStorage.clear();
    </script>
</body>  
</html>