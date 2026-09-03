```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>${messageSource.getMessage("product.title", null, locale)}</title>
</head>
<body>
    <h1>${messageSource.getMessage("product.title", null, locale)}</h1>
    <!-- Additional product details -->
</body>
</html>
```