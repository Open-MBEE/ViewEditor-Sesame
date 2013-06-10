<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<html>
<head>
  <title>View Editor Login</title>
<link rel="stylesheet" type="text/css" href="css/styles.css">
</head>
<body>
<div id="page_title"><h1>View Editor</h1></div>
<c:if test='${not empty param.error}'>  
  <font color='red'>  
    Login error. <br />  
    Reason : ${sessionScope['SPRING_SECURITY_LAST_EXCEPTION'].message}  
  </font>  
</c:if>  
<div id="subTitle">
<h1>Login</h1>
</div>
<p>JPL username and password required. Must be a US person</p>
<form method='POST' action='<c:url value='/j_spring_security_check' />'>  
  <table style='border:none'>
   <tbody>
    <tr>  
      <td style='border:none' align='left'><label>Username</label></td>
      <td style='border:none'><input type='text' name='j_username' /></td> 
    </tr>  
    <tr>  
      <td style='border:none' align='left'><label>Password</label></td>
      <td style='border:none'><input type='password' name='j_password' /></td>
    </tr>  
    <tr>  
      <td style='border:none' colspan='2' align='left'>
        <input type='submit' value='login' />  
      </td>  
    </tr>  
   </tbody>
  </table>  
</form>  
</body>  
</html>  
